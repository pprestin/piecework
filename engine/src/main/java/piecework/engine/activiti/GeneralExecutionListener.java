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

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.engine.EngineContext;
import piecework.engine.EngineStateSynchronizer;
import piecework.enumeration.StateChangeType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author James Renfro
 */
@Service("generalExecutionListener")
public class GeneralExecutionListener implements ExecutionListener {

    private static final Map<String, StateChangeType> EVENT_MAP;

    static {
        Map<String, StateChangeType> map = new HashMap<String, StateChangeType>();
        map.put(EVENTNAME_START, StateChangeType.START_PROCESS);
        map.put(EVENTNAME_END, StateChangeType.COMPLETE_PROCESS);
        EVENT_MAP = Collections.unmodifiableMap(map);
    }

    @Autowired
    EngineStateSynchronizer engineStateSynchronizer;

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        String eventName = execution.getEventName();

        if (eventName != null) {
            // Business key will always be the actual Piecework process instance id
            String pieceworkProcessInstanceId = execution.getProcessBusinessKey();
            StateChangeType event = EVENT_MAP.get(eventName);
            if (event != null) {
                // Some logic to decide if it's a task
                boolean isTask = false;

                if (execution instanceof ActivityExecution) {
                    ActivityExecution activityExecution = ActivityExecution.class.cast(execution);
                    PvmActivity activity = activityExecution.getActivity();
                    String activityType = activity != null ? String.class.cast(activity.getProperty("type")) : null;

                    if (activityType != null) {
                        if (activityType.equals("manualTask")) {
                            if (eventName.equals("start"))
                                event = StateChangeType.CREATE_TASK;
                            else if (eventName.equals("end"))
                                event = StateChangeType.COMPLETE_TASK;

                            ManualEngineTask engineTask = new ManualEngineTask.Builder()
                                    .activityExecution(activityExecution)
                                    .build();
                            EngineContext context = new ActivitiEngineContext(execution.getEngineServices(), execution.getProcessDefinitionId(), execution.getProcessInstanceId());
                            engineStateSynchronizer.onTaskEvent(event, engineTask, context);
                            isTask = true;
                        }
                    }
                }



                if (!isTask) {
                    if (event == StateChangeType.COMPLETE_PROCESS) {
                        HistoricProcessInstance historicInstance = execution.getEngineServices().getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(execution.getProcessInstanceId()).singleResult();
                        if (historicInstance != null && historicInstance.getDeleteReason() != null && historicInstance.getDeleteReason().equalsIgnoreCase(Constants.DeleteReasons.CANCELLED))
                            event = StateChangeType.CANCEL_PROCESS;
                    }

                    EngineContext context = new ActivitiEngineContext(execution.getEngineServices(), execution.getProcessDefinitionId(), execution.getProcessInstanceId());
                    engineStateSynchronizer.onProcessInstanceEvent(event, pieceworkProcessInstanceId, context);
                }
            }
        }
    }

}
