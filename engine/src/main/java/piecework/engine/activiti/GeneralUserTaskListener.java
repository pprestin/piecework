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

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.engine.EngineContext;
import piecework.engine.EngineStateSynchronizer;
import piecework.engine.EngineTask;
import piecework.enumeration.StateChangeType;

import java.util.*;

/**
 * @author James Renfro
 */
@Service("generalUserTaskListener")
public class GeneralUserTaskListener implements TaskListener {

    private static final Logger LOG = Logger.getLogger(GeneralUserTaskListener.class);

    private static final Map<String, StateChangeType> EVENT_MAP;

    static {
        Map<String, StateChangeType> map = new HashMap<String, StateChangeType>();
        map.put(TaskListener.EVENTNAME_ASSIGNMENT, StateChangeType.ASSIGN_TASK);
        map.put(TaskListener.EVENTNAME_CREATE, StateChangeType.CREATE_TASK);
        map.put(TaskListener.EVENTNAME_COMPLETE, StateChangeType.COMPLETE_TASK);
        EVENT_MAP = Collections.unmodifiableMap(map);
    }

    @Autowired
    EngineStateSynchronizer engineStateSynchronizer;

    @Autowired
    ActivitiEngineProxyHelper helper;

    @Override
    public void notify(DelegateTask delegateTask) {
        String eventName = delegateTask.getEventName();
        // Sanity check that event name is not null or empty
        if (StringUtils.isEmpty(eventName))
            return;

        // Wrap the task in interface that is engine independent
        EngineTask engineTask = new DelegateEngineTask(delegateTask);
        StateChangeType event = EVENT_MAP.get(eventName);
        if (event != null) {
            if (LOG.isDebugEnabled())
                LOG.debug("Notifying engine state synchronizer of task event " + event);
            EngineContext context = new ActivitiEngineContext(helper.getProcessEngine(), delegateTask.getProcessDefinitionId(), delegateTask.getProcessInstanceId());
            engineStateSynchronizer.onTaskEvent(event, engineTask, context);
        }
    }

}
