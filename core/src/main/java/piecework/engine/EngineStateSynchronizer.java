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
import piecework.enumeration.StateChangeType;
import piecework.exception.StatusCodeError;
import piecework.model.*;
import piecework.model.Process;
import piecework.service.ProcessInstanceService;
import piecework.service.ProcessService;
import piecework.service.TaskService;
import piecework.task.TaskFactory;
import piecework.service.NotificationService;
import piecework.Versions;
import piecework.common.ViewContext;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Set;

/**
 * @author James Renfro
 */
@Service
public class EngineStateSynchronizer {

    private static final Logger LOG = Logger.getLogger(EngineStateSynchronizer.class);

    @Autowired
    Mediator mediator;

    @Autowired
    ProcessService processService;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    TaskService taskService;

    @Autowired
    Versions versions;

    @Autowired
    NotificationService notificationService;

    public void onProcessInstanceEvent(StateChangeType type, String processInstanceId, EngineContext context) {
        ProcessInstance instance = null;
        switch (type) {
        case START_PROCESS:
            if (LOG.isDebugEnabled())
                LOG.debug("Process instance started " + processInstanceId);

            doStartNotification(type, context);
            break;
        case COMPLETE_PROCESS:
            instance = processInstanceService.complete(processInstanceId);
            if (instance != null) {
                try {
                    Process process = processService.read(instance.getProcessDefinitionKey());
                    LOG.debug("Process instance completed " + processInstanceId);
                    mediator.notify(new StateChangeEvent.Builder(StateChangeType.COMPLETE_PROCESS).context(context).process(process).instance(instance).build());
                } catch (StatusCodeError e) {
                    LOG.error("Unable to find the process for this process instance -- complete process event will not be thrown" + processInstanceId);
                }
            } else {
                LOG.error("Unable to save final state of process instance with execution business key because the instance could not be found" + processInstanceId);
            }
        }
    }

    public void onTaskEvent(StateChangeType type, EngineTask delegateTask, EngineContext context) {
        try {
            Process process = processService.read(delegateTask.getProcessDefinitionKey());
            if (process == null)
                return;

            ProcessInstance processInstance = processInstanceService.read(process, delegateTask.getProcessInstanceId(), true);
            if (processInstance == null)
                return;

            Task updated;

            switch(type) {
                case CREATE_TASK:
                    updated = TaskFactory.task(process, processInstance, delegateTask);
                    break;
                default:
                    Task task = taskService.read(processInstance, delegateTask.getTaskId());
                    updated = TaskFactory.task(task, delegateTask, type == StateChangeType.COMPLETE_TASK);
                    break;
            };

            if (updated != null) {
                if (taskService.update(processInstance.getProcessInstanceId(), updated)) {
                    LOG.debug("Stored task changes");
                    mediator.notify(new StateChangeEvent.Builder(type).context(context).process(process).instance(processInstance).task(updated).build());
                    doTaskNotification(type, process, processInstance, updated, context);
                } else {
                    LOG.error("Failed to store task changes");
                }
            }
        } catch (StatusCodeError error) {
            LOG.error("Unable to save task state changes -- probably because the process or the instance could not be found", error);
        }
    }

    private void doStartNotification(StateChangeType type, EngineContext engineContext) {
        //Map<String, String> map = engineContext.getStartFormProperties();
        //buildNotification(map);
    }

    private Map<String, String> createContext(StateChangeType type, Process process, ProcessInstance instance, Task task, EngineContext engineContext) {
        // set up context
        Map<String, String> context = new HashMap<String, String>();
        context.put("PIECEWORK_PROCESS_INSTANCE_LABEL", instance.getProcessInstanceLabel());
        context.put("TASK_ID", task.getTaskInstanceId());
        context.put("TASK_KEY", task.getTaskDefinitionKey());
        context.put("TASK_LABEL", task.getTaskLabel());

        // task URL
        ViewContext viewContext = versions.getVersion1();
        String taskUrl = viewContext.getApplicationUri(ProcessInstance.Constants.ROOT_ELEMENT_NAME, task.getProcessDefinitionKey(), task.getTaskInstanceId());
        context.put("TASK_URL", taskUrl);

        // put assignee IDs into context
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
            context.put("ASSIGNEE", assigneeId);
        }

        return context;
    }

    private void doTaskNotification(StateChangeType type, Process process, ProcessInstance instance, Task task, EngineContext engineContext) {

        // sanity check
        if ( task == null ) {
            return;
        }

        // get notfication templates
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
        
        Map<String, String> context = createContext(type, process, instance, task, engineContext);

        if ( context == null || context.isEmpty() ) {
            return;
        }
        
        // send out notifications
        notificationService.send(notifications, context, type);
    }

}
