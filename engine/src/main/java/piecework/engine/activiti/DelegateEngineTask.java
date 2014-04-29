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

import org.activiti.bpmn.model.Task;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.task.IdentityLink;
import org.apache.commons.lang.StringUtils;
import piecework.engine.EngineTask;
import piecework.enumeration.ActionType;

import java.util.*;

/**
 * @author James Renfro
 */
public class DelegateEngineTask implements EngineTask {

    private final DelegateTask delegateTask;
    private final String processDefinitionKey;
    private final String processInstanceId;
    private final String taskDefinitionKey;
    private final Set<String> candidateAssigneeIds;
    private final Set<String> candidateGroupIds;
    private final ActionType actionType;

    // More logic than normal for a constructor but all in the interest of gathering the right data together
    public DelegateEngineTask(DelegateTask delegateTask, String processDefinitionKey, String processInstanceID) {
        this.delegateTask = delegateTask;
        Map<String, Object> variables = delegateTask.getVariables();

        this.processDefinitionKey = StringUtils.isNotEmpty(processDefinitionKey)? processDefinitionKey:  String.class.cast(variables.get("PIECEWORK_PROCESS_DEFINITION_KEY"));
        this.processInstanceId = StringUtils.isNotEmpty(processInstanceID)? processInstanceID : String.class.cast(variables.get("PIECEWORK_PROCESS_INSTANCE_ID"));

        this.taskDefinitionKey = delegateTask.getTaskDefinitionKey();
        String actionValue = StringUtils.isNotEmpty(taskDefinitionKey) ? String.class.cast(delegateTask.getVariableLocal(taskDefinitionKey + "_action")) : null;
        ActionType actionType = ActionType.COMPLETE;
        if (StringUtils.isNotEmpty(actionValue))
            actionType = ActionType.valueOf(actionValue);
        this.actionType = actionType;

        Set<String> candidateAssigneeIds = new LinkedHashSet<String>();
        Set<String> candidateGroupIds = new LinkedHashSet<String>();
        Set<IdentityLink> candidates = delegateTask.getCandidates();
        if (candidates != null) {
            for (IdentityLink candidate : candidates) {
                if (StringUtils.isNotEmpty(candidate.getUserId())) {
                    candidateAssigneeIds.add(candidate.getUserId());
                } else if (StringUtils.isNotEmpty(candidate.getGroupId())) {
                    candidateGroupIds.add(candidate.getGroupId());
                }
            }
        }
        this.candidateAssigneeIds = Collections.unmodifiableSet(candidateAssigneeIds);
        this.candidateGroupIds = Collections.unmodifiableSet(candidateGroupIds);
    }

    @Override
    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    @Override
    public String getProcessInstanceId() {
        return processInstanceId;
    }

    @Override
    public String getTaskDefinitionKey() {
        return taskDefinitionKey;
    }

    @Override
    public String getTaskId() {
        return delegateTask.getId();
    }

    @Override
    public String getEngineProcessInstanceId() {
        return delegateTask.getProcessInstanceId();
    }

    @Override
    public String getName() {
        return delegateTask.getName();
    }

    @Override
    public String getDescription() {
        return delegateTask.getDescription();
    }

    @Override
    public Date getStartTime() {
        return delegateTask.getCreateTime();
    }

    @Override
    public Date getDueDate() {
        return delegateTask.getDueDate();
    }

    @Override
    public int getPriority() {
        return delegateTask.getPriority();
    }

    @Override
    public String getAssigneeId() {
        return delegateTask.getAssignee();
    }

    @Override
    public Set<String> getCandidateAssigneeIds() {
        return candidateAssigneeIds;
    }

    @Override
    public Set<String> getCandidateGroupIds() {
        return candidateGroupIds;
    }

    @Override
    public ActionType getActionType() {
        return actionType;
    }
}
