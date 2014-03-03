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
package piecework.engine;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.SystemUser;
import piecework.common.ViewContext;
import piecework.enumeration.StateChangeType;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ModelProviderFactory;
import piecework.persistence.ProcessInstanceProvider;
import piecework.persistence.TaskProvider;
import piecework.service.NotificationService;
import piecework.service.ProcessInstanceService;
import piecework.service.TaskService;
import piecework.settings.UserInterfaceSettings;
import piecework.task.TaskFactory;

import java.util.*;

/**
 * @author James Renfro
 */
@Service
public class EngineStateSynchronizer {

    private static final String VERSION = "v1";
    private static final Logger LOG = Logger.getLogger(EngineStateSynchronizer.class);

    @Autowired
    Mediator mediator;

    @Autowired
    ModelProviderFactory modelProviderFactory;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    TaskService taskService;

    @Autowired
    NotificationService notificationService;

    @Autowired
    UserInterfaceSettings settings;


    public void onProcessInstanceEvent(StateChangeType type, String processInstanceId, EngineContext context) {
        ProcessInstanceProvider instanceProvider = modelProviderFactory.instanceProvider(processInstanceId, new SystemUser());
        switch (type) {
        case START_PROCESS:
            if (LOG.isDebugEnabled())
                LOG.debug("Process instance started " + processInstanceId);

                mediator.notify(new StateChangeEvent.Builder(type).context(context).instanceProvider(instanceProvider).build());
//                doStartNotification(type, context);

            break;
        case COMPLETE_PROCESS:
            ProcessInstance instance = processInstanceService.complete(processInstanceId, new SystemUser());
            if (instance != null) {
                LOG.debug("Process instance completed " + processInstanceId);
                mediator.notify(new StateChangeEvent.Builder(StateChangeType.COMPLETE_PROCESS).context(context).instanceProvider(instanceProvider).build());
            } else {
                LOG.error("Unable to save final state of process instance with execution business key because the instance could not be found" + processInstanceId);
            }
        }
    }

    public void onTaskEvent(StateChangeType type, EngineTask delegateTask, EngineContext context) {
        try {
            ProcessInstanceProvider instanceProvider = null;
            Task updated;
            Process process;
            ProcessInstance instance;
            switch(type) {
                case CREATE_TASK:
                    instanceProvider = modelProviderFactory.instanceProvider(delegateTask.getProcessDefinitionKey(), delegateTask.getProcessInstanceId(), new SystemUser());
                    process = instanceProvider.process();
                    instance = instanceProvider.instance();
                    updated = TaskFactory.task(process, instance, delegateTask);
                    break;
                default:
                    TaskProvider taskProvider = modelProviderFactory.taskProvider(delegateTask.getProcessDefinitionKey(), delegateTask.getTaskId(), new SystemUser());
                    process = taskProvider.process();
                    instance = taskProvider.instance();
                    Task task = taskProvider.task();
                    instanceProvider = taskProvider;
                    updated = TaskFactory.task(task, delegateTask, type == StateChangeType.COMPLETE_TASK);
                    break;
            };

            if (updated != null) {
                if (taskService.update(instance.getProcessInstanceId(), updated)) {
                    LOG.debug("Stored task changes");
                    mediator.notify(new StateChangeEvent.Builder(type).context(context).instanceProvider(instanceProvider).task(updated).build());
                    doTaskNotification(type, process, instance, updated, context);
                } else {
                    LOG.error("Failed to store task changes");
                }
            }
        } catch (PieceworkException error) {
            LOG.error("Unable to save task state changes -- probably because the process or the instance could not be found", error);
        }
    }

    // scope is a key-value pairs used for template substitution
    private Map<String, Object> createScope(StateChangeType type, Process process, ProcessInstance instance, Task task) {
        // set up scope
        Map<String, Object> scope = new HashMap<String, Object>();
        scope.put("PIECEWORK_PROCESS_INSTANCE_LABEL", instance.getProcessInstanceLabel());
        scope.put("TASK_ID", task.getTaskInstanceId());
        scope.put("TASK_KEY", task.getTaskDefinitionKey());
        scope.put("TASK_LABEL", task.getTaskLabel());

        // task URL
        ViewContext viewContext = new ViewContext(settings, VERSION);
        String taskUrl = viewContext.getApplicationUri(Form.Constants.ROOT_ELEMENT_NAME, task.getProcessDefinitionKey(), "?taskId=" + task.getTaskInstanceId());
        scope.put("TASK_URL", taskUrl);

        // add instance data into scope
        // skip empty strings to accommodate mustache
        Map<String, List<Value>> data = instance.getData();
        if (data != null && !data.isEmpty()) {
            for (Map.Entry<String, List<Value>> entry : data.entrySet()) {
                String key = entry.getKey();
                List<Value> values = entry.getValue();
                if (values != null && !values.isEmpty() ) {
                    if ( values.size() == 1 ) {
                        Value val = values.get(0);
                        // getValue returns null for value of User type 
                        if ( val != null && ( val.getValue() == null || val.getValue().length() > 0 ) ) {
                            scope.put(key, val);  // flattern it
                        }
                    } else {
                        scope.put(key, values);
                    }
                }   
            }   
        }   


        // put assignee IDs into scope
        String assigneeId = task.getAssigneeId();
        if ( assigneeId == null || assigneeId.isEmpty() ) {
            Set<String> strs = task.getCandidateAssigneeIds();
          
            if ( strs != null && ! strs.isEmpty() ) {
                StringBuilder str = new StringBuilder();
                for (String s : strs ) {
                    if ( str.length() > 0 ) {
                        str.append(",");
                    }
                    str.append(s);
                }

                assigneeId = str.toString();
            }
        } 

        if ( assigneeId == null || assigneeId.isEmpty() ) {
            return null; // we don't send notifications if there is no assignee
        } else {
            scope.put("ASSIGNEE", assigneeId);
        }

        return scope;
    }

    private void doTaskNotification(StateChangeType type, Process process, ProcessInstance instance, Task task, EngineContext engineContext) {

        // sanity check
        if ( task == null ) {
            return;
        }

        // get notification templates
        Collection<Notification> notifications = new ArrayList<Notification>();
        String taskKey = task.getTaskDefinitionKey();
        if ( taskKey != null && process != null && process.getDeployment() != null ) {
            ProcessDeployment deployment = process.getDeployment();

            // get notifications common to all tasks for a workflow
            Collection<Notification> ns = deployment.getNotifications(Notification.Constants.COMMON);
            if ( ns != null && ns.size() >0 ) {
                notifications.addAll(ns);
            }
            
            // get notifications specific to a task
            ns = deployment.getNotifications(taskKey);
            if ( ns != null && ns.size() >0 ) {
                notifications.addAll(ns);
            }
        }

        if ( notifications.isEmpty() ) {
            return;
        }
        
        Map<String, Object> scope = createScope(type, process, instance, task);

        if ( scope == null || scope.isEmpty() ) {
            return;
        }
        
        // send out notifications
        notificationService.send(notifications, scope, type);
    }

}
