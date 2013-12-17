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
import piecework.persistence.ProcessInstanceRepository;
import piecework.service.ProcessService;
import piecework.service.TaskService;
import piecework.task.TaskFactory;
import piecework.service.NotificationService;
import piecework.Versions;
import piecework.common.ViewContext;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;
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
    ProcessInstanceRepository processInstanceRepository;

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

            try {
                instance = processInstanceRepository.findOne(processInstanceId);
                Process process = processService.read(instance.getProcessDefinitionKey());
                mediator.notify(new StateChangeEvent.Builder(type).context(context).process(process).instance(instance).build());
                doStartNotification(type, context);
            } catch (StatusCodeError error) {
                LOG.error("Unable to read process instance: " + processInstanceId, error);
            }   
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

    // local helper function to append a new stirng value into a map
    private void appendValue(Map<String, StringBuilder> map, String key, String val)
    {
        if ( key == null || val == null ) {
            return;
        }

        if ( map.containsKey(key) ) {
            StringBuilder sb = map.get(key);
            sb.append(","+val);
        } else {
            map.put(key, new StringBuilder(val));
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
        ViewContext viewContext = versions.getVersion1();
        String taskUrl = viewContext.getApplicationUri(ProcessInstance.Constants.ROOT_ELEMENT_NAME, task.getProcessDefinitionKey(), task.getTaskInstanceId());
        scope.put("TASK_URL", taskUrl);

        // add instance data into scope
        Map<String, List<Value>> data = instance.getData();
        if (data != null && !data.isEmpty()) {
            for (Map.Entry<String, List<Value>> entry : data.entrySet()) {
                String key = entry.getKey();
                List<Value> values = entry.getValue();
                if (values != null && !values.isEmpty() ) {
                    if ( values.size() == 1 ) {
                        scope.put(key, values.get(0));  // flattern it
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
        
        Map<String, Object> scope = createScope(type, process, instance, task);

        if ( scope == null || scope.isEmpty() ) {
            return;
        }
        
        // send out notifications
        notificationService.send(notifications, scope, type);
    }

}
