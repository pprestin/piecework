package piecework.engine.activiti;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEntityEventImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.engine.EngineContext;
import piecework.engine.EngineStateSynchronizer;
import piecework.engine.EngineTask;
import piecework.enumeration.StateChangeType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service("subTaskCreateHandler")
public class GeneralSubTaskCreateListener  implements ActivitiEventListener {

    private static final Logger LOG = Logger.getLogger(GeneralSubTaskCreateListener.class);

    private static final Map<String, StateChangeType> EVENT_MAP;

    static {
        Map<String, StateChangeType> map = new HashMap<String, StateChangeType>();
        map.put("TASK_COMPLETED", StateChangeType.COMPLETE_TASK);
        map.put("TASK_ASSIGNED", StateChangeType.ASSIGN_TASK);
        EVENT_MAP = Collections.unmodifiableMap(map);
    }

    @Autowired
    EngineStateSynchronizer engineStateSynchronizer;

    @Autowired
    ActivitiEngineProxyHelper helper;

    @Override
    public void onEvent(ActivitiEvent event) {

        ActivitiEventType eventName = event.getType();

        if (eventName != null && ((ActivitiEntityEventImpl) event).getEntity() != null && ((ActivitiEntityEventImpl) event).getEntity() instanceof Task) {
            ActivitiEntityEventImpl e = (ActivitiEntityEventImpl) event;
            Task subtask = (Task)e.getEntity();

            if(subtask.getParentTaskId() != null && !subtask.getParentTaskId().isEmpty()){

                //grab the parent
                org.activiti.engine.task.Task parentTask = helper.getProcessEngine().getTaskService().createTaskQuery().taskId(subtask.getParentTaskId()).singleResult();

                //get the parent's process
                ProcessInstance p = event.getEngineServices().getRuntimeService().createProcessInstanceQuery().processInstanceId(parentTask.getProcessInstanceId()).singleResult();

                //get the piecework id
                String processInstanceId = (String)event.getEngineServices().getRuntimeService().getVariable(parentTask.getProcessInstanceId(), "PIECEWORK_PROCESS_INSTANCE_ID");

                Map<String, Object> v = ((ExecutionEntity) p).getVariables();

                //cast to it's delegate state and pass to piecework
                EngineTask q = new DelegateEngineTask((DelegateTask)subtask,
                        v.get("PIECEWORK_PROCESS_DEFINITION_KEY").toString(),
                        v.get("PIECEWORK_PROCESS_INSTANCE_ID").toString());

                StateChangeType ev = EVENT_MAP.get(eventName.toString());
                if(ev != null){
                    if (LOG.isDebugEnabled())
                        LOG.debug("Notifying engine state synchronizer of task event " + event);
                    EngineContext context = new ActivitiEngineContext(helper.getProcessEngine(), p.getProcessDefinitionId(), parentTask.getProcessInstanceId());
                    engineStateSynchronizer.onTaskEvent(ev, q, context);

                }
            }
        }

    }

    @Override
    public boolean isFailOnException() {
        return false;
    }
}
