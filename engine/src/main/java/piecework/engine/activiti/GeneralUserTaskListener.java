/*
 * Copyright 2013 University of Washington
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package piecework.engine.activiti;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.*;
import org.activiti.engine.form.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import piecework.Constants;
import piecework.enumeration.ActionType;
import piecework.identity.IdentityDetails;
import piecework.service.IdentityService;
import piecework.service.NotificationService;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessInstanceRepository;
import piecework.persistence.ProcessRepository;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.Versions;
import piecework.common.ViewContext;

import javax.annotation.PostConstruct;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author James Renfro
 */
@Service("generalUserTaskListener")
public class GeneralUserTaskListener implements TaskListener {

    private static final Logger LOG = Logger.getLogger(GeneralUserTaskListener.class);

    @Autowired
    Environment environment;

    @Autowired
    Versions versions;  // needed for constructing taskUrl

    @Autowired
    ProcessRepository processRepository;

    @Autowired
    ProcessInstanceRepository processInstanceRepository;

    @Autowired
    MongoOperations mongoOperations;

    @Autowired
    IdentityService userDetailsService;

    @Autowired
    NotificationService notificationService;

    @SuppressWarnings("unused")
    @Override
    public void notify(DelegateTask delegateTask) {
        String engineProcessInstanceId = delegateTask.getProcessInstanceId();
        if (StringUtils.isEmpty(engineProcessInstanceId))
            return;

        String taskDefinitionKey = delegateTask.getTaskDefinitionKey();
        if (StringUtils.isEmpty(taskDefinitionKey))
            return;

        Map<String, Object> variables = delegateTask.getVariables();

        String processDefinitionKey = String.class.cast(variables.get("PIECEWORK_PROCESS_DEFINITION_KEY"));
        String processInstanceId = String.class.cast(variables.get("PIECEWORK_PROCESS_INSTANCE_ID"));

        String actionValue = String.class.cast(delegateTask.getVariableLocal(taskDefinitionKey + "_action"));
        ActionType action = ActionType.COMPLETE;

        if (StringUtils.isNotEmpty(actionValue))
            action = ActionType.valueOf(actionValue);

        ProcessInstance processInstance = processInstanceRepository.findOne(processInstanceId);
        if (processInstance == null)
            return;

        Process process = processRepository.findOne(processDefinitionKey);
        if (process == null)
            return;

        Set<Candidate> approvers = new HashSet<Candidate>();
        Set<Candidate> watchers = new HashSet<Candidate>();

        String taskEventType = null;

        // last assignee is the user who completed the last task
        User lastAssignee = null;

        Task.Builder taskBuilder = null;
        if (delegateTask.getEventName().equals(TaskListener.EVENTNAME_CREATE)) {
            taskEventType = Constants.TaskEventTypes.CREATE;
            String assigneeId = String.class.cast(variables.get("lastAssignee"));
            if ( assigneeId == null || assigneeId.isEmpty() ) {
                 assigneeId = processInstance.getInitiatorId();
            }
            if (StringUtils.isNotEmpty(assigneeId)) {
                lastAssignee = userDetailsService.getUser(assigneeId);
            }
        } else if (delegateTask.getEventName().equals(TaskListener.EVENTNAME_COMPLETE)) {
            taskEventType = Constants.TaskEventTypes.COMPLETE;

            String assigneeId = delegateTask.getAssignee();
            if (StringUtils.isNotEmpty(assigneeId)) {
                lastAssignee = userDetailsService.getUser(assigneeId);
                delegateTask.setVariable("lastAssignee", assigneeId); // save assignee for later use
            }
        }


        if (delegateTask.getEventName().equals(TaskListener.EVENTNAME_CREATE)) {
            taskBuilder = new Task.Builder()
                .taskInstanceId(delegateTask.getId())
                .taskDefinitionKey(delegateTask.getTaskDefinitionKey())
                .processInstanceId(processInstance.getProcessInstanceId())
                .processInstanceAlias(processInstance.getAlias())
                .processDefinitionKey(process.getProcessDefinitionKey())
                .processDefinitionLabel(process.getProcessDefinitionLabel())
                .processInstanceLabel(processInstance.getProcessInstanceLabel())
                .engineProcessInstanceId(delegateTask.getProcessInstanceId())
                .taskLabel(delegateTask.getName())
                .taskDescription(delegateTask.getDescription())
                .taskStatus(Constants.TaskStatuses.OPEN)
                .taskAction(actionValue)
                .startTime(delegateTask.getCreateTime())
                .dueDate(delegateTask.getDueDate())
                .priority(delegateTask.getPriority())
                .active();


        } else {

            Task task = null; //taskRepository.findOne(delegateTask.getId());

            Set<Task> tasks = processInstance.getTasks();
            if (tasks != null && !tasks.isEmpty()) {
                for (Task current : tasks) {
                    if (current != null && current.getTaskInstanceId().equals(delegateTask.getId())) {
                        task = current;
                        break;
                    }
                }
            }

            if (task != null) {
                taskBuilder = new Task.Builder(task, new PassthroughSanitizer());

                if (delegateTask.getEventName().equals(TaskListener.EVENTNAME_COMPLETE)) {
                    String taskStatus = action == ActionType.REJECT ? Constants.TaskStatuses.REJECTED : Constants.TaskStatuses.COMPLETE;

                    taskBuilder.taskAction(actionValue)
                        .taskStatus(taskStatus)
                        .finished()
                        .endTime(new Date());
                }
            }
        }

        if (taskBuilder != null) {

            String assigneeId = delegateTask.getAssignee();

            if (StringUtils.isNotEmpty(assigneeId)) {
                taskBuilder.assignee(userDetailsService.getUser(assigneeId));
            }

            Set<IdentityLink> candidates = delegateTask.getCandidates();
            if (candidates != null) {
                for (IdentityLink candidate : candidates) {
                    if (StringUtils.isNotEmpty(candidate.getUserId())) {
                        taskBuilder.candidateAssignee(userDetailsService.getUser(candidate.getUserId()));
                    } else if (StringUtils.isNotEmpty(candidate.getGroupId())) {
                        taskBuilder.candidateAssigneeId(candidate.getGroupId());
                    }
                }
            }

            Task updated = taskBuilder.build();
            if (processInstanceRepository.update(processInstance.getProcessInstanceId(), updated))
                LOG.debug("Stored task changes");
            else
                LOG.error("Failed to store task changes");
        }

        // build notifications
        List<Notification> notifications = buildNotifications(delegateTask);

        if ( notifications != null && !notifications.isEmpty() ) {
            // build context to be used for macro expansion 
            Map<String, String> context = new HashMap<String, String>();
            context.put("PIECEWORK_PROCESS_INSTANCE_LABEL", processInstance.getProcessInstanceLabel());
            context.put("TASK_ID", delegateTask.getId());
            context.put("TASK_NAME", delegateTask.getName());
            if ( lastAssignee != null ) {
                context.put("LAST_ASSIGNEE_NAME", lastAssignee.getDisplayName());
            }

            ViewContext taskViewContext = versions.getVersion1();
            String taskUrl = taskViewContext.getApplicationUri(ProcessInstance.Constants.ROOT_ELEMENT_NAME, processDefinitionKey, delegateTask.getId());
            context.put("TASK_URL", taskUrl);

            // send out notifications
            notificationService.send(notifications, context);
        }
    }

    // add notification properties/templates to notification map
    // we use the following naming convention for notification key names
    // <notification name>.<field name>
    // <notification name> starts with "notification"
    // e.g. "notification1.subject", "notification1.recipients" etc.
    private void addNotificationProperties(Map<String, Map<String, String> > notifications, List<FormProperty> properties) {
        for (FormProperty p : properties) {
            String name = p.getName();;
            FormType type = p.getType();
            String value = p.getValue();
            if (name.startsWith("notification") ) {
                String notificationName = null;
                String fieldName = null;
                int idx = name.indexOf('.');
                if ( idx >= 0 ) {
                    notificationName = name.substring(0, idx);
                    fieldName = name.substring(idx+1);
                } else {
                    notificationName = name;
                }

                Map<String, String> notification = null;
                if ( notifications.containsKey(notificationName) ) {
                    notification = notifications.get(notificationName);
                } else {
                    notification = new HashMap<String, String> ();
                    notifications.put(notificationName, notification);
                }

                if ( fieldName != null &&  !fieldName.isEmpty() && value != null && !value.isEmpty() ) {
                    notification.put( fieldName, value );
                }
            }
        }
    }

    // build notifications from templates and process variables/form parameters stored
    // with process definitions. Use a map to avoid duplicate and to allow task parameters
    // to override process parameters.
    private List<Notification> buildNotifications(DelegateTask delegateTask) {
        Map<String, Map<String, String> > notificationsMap = new HashMap<String, Map<String, String> >();

        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        FormService formService = processEngine.getFormService();

        // default notification parameter are stored in "StartFormData"
        StartFormData formData = formService.getStartFormData(delegateTask.getProcessDefinitionId());
        List<FormProperty> properties = formData.getFormProperties();
        addNotificationProperties(notificationsMap, properties);

        // task parameters have higher precedence 
        TaskFormData taskData = formService.getTaskFormData(delegateTask.getId());
        properties = taskData.getFormProperties();
        addNotificationProperties(notificationsMap, properties);

        // get assignees
        String assigneeIds = delegateTask.getAssignee();
        if ( assigneeIds == null || assigneeIds.isEmpty() ) {
            Set<IdentityLink> candidates = delegateTask.getCandidates();
            if (candidates != null) {
                StringBuilder str = new StringBuilder();
                for (IdentityLink candidate : candidates) {
                    if (StringUtils.isNotEmpty(candidate.getUserId())) {
                        if ( str.length() > 0) {
                            str.append(',').append(candidate.getUserId());
                        } else {
                            str.append(candidate.getUserId());
                        }
                    } else if (StringUtils.isNotEmpty(candidate.getGroupId())) {
                        if ( str.length() > 0) {
                            str.append(',').append(candidate.getGroupId());
                        } else {
                            str.append(candidate.getGroupId());
                        }
                    }
                }
                assigneeIds = str.toString();
            }
        }

        // build and filter notifications
        List<Notification> notifications = new ArrayList<Notification>();
        for ( String k : notificationsMap.keySet() ) {
            Map<String, String> notification = notificationsMap.get(k);

            // filtering notifications based on task events (create, complete etc.)
            // defaults to TaskListener.EVENTNAME_CREATE.
            String onevent = notification.get("onevent");
            if (onevent == null || onevent.isEmpty() ) {
                onevent = TaskListener.EVENTNAME_CREATE;
            }

            if ( onevent.indexOf(TaskListener.EVENTNAME_ALL_EVENTS)>=0 || onevent.indexOf(delegateTask.getEventName())>=0 ) {
                Notification.Builder builder = new Notification.Builder();
                for ( Map.Entry<String, String> kv  : notification.entrySet() ) {
                    builder.put(kv.getKey(), kv.getValue());
                }
                if ( assigneeIds != null && ! assigneeIds.isEmpty() ) {
                    builder.put("assignee", assigneeIds);
                }
                notifications.add( builder.build() );
            }
        }

        return notifications;
    }

    private void sendNotification(Set<Candidate> candidates, Notification notification, Map<String, String> context) {
        if (candidates.isEmpty())
            return;

        String mailServerHost = environment.getProperty("mail.server.host");
        int mailServerPort = environment.getProperty("mail.server.port", Integer.class, 25);
        String mailFromAddress = environment.getProperty("mail.from.address");
        String mailFromLabel = environment.getProperty("mail.from.label");

        MustacheFactory mf = new DefaultMustacheFactory();

        StringWriter writer = new StringWriter();
        Mustache mustache = mf.compile(new StringReader(notification.getSubject()), "subject");
        mustache.execute(writer, context);

        String subject = writer.toString();

        writer = new StringWriter();
        mustache = mf.compile(new StringReader(notification.getText()), "text");
        mustache.execute(writer, context);

        String body = writer.toString();

        try {
            SimpleEmail email = new SimpleEmail();
            email.setHostName(mailServerHost);
            email.setSmtpPort(mailServerPort);

            for (Candidate candidate : candidates) {
                if (StringUtils.isEmpty(candidate.getType()) || StringUtils.isEmpty(candidate.getCandidateId()))
                    continue;

                if (candidate.getType().equals(Constants.CandidateTypes.PERSON)) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(candidate.getCandidateId());

                    if (userDetails instanceof IdentityDetails) {
                        IdentityDetails identityDetails = IdentityDetails.class.cast(userDetails);
                        if (StringUtils.isNotEmpty(identityDetails.getEmailAddress()))
                            email.addTo(identityDetails.getEmailAddress(), identityDetails.getDisplayName());
                    }
                }
            }
            email.setFrom(mailFromAddress, mailFromLabel);
            email.setSubject(subject);
            email.setMsg(body);

            LOG.debug("Subject: " + email.getSubject());
            LOG.debug(email.getMimeMessage());
            email.send();
        } catch (EmailException e) {
            LOG.error("Unable to send email with subject " + subject);
        }
    }
}
