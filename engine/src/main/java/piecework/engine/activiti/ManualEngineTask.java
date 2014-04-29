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

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import piecework.engine.EngineTask;
import piecework.enumeration.ActionType;

import java.util.*;

/**
 * This task is a workaround for the fact that Activiti doesn't provide the same events
 * for manual tasks as for user tasks, so this is a fictionalized task representation
 * to allow us to store something in MongoDB to stand in for a manual task
 *
 * @author James Renfro
 */
public class ManualEngineTask implements EngineTask {

    private final String processDefinitionKey;
    private final String processInstanceId;
    private final String taskDefinitionKey;
    private final String taskId;
    private final String engineProcessInstanceId;
    private final String name;
    private final String description;
    private final String assigneeId;
    private final Set<String> candidateAssigneeIds;
    private final Set<String> candidateGroupIds;
    private final Date startTime;
    private final Date dueDate;
    private final int priority;
    private final ActionType actionType;

    private ManualEngineTask() {
        this(new Builder());
    }

    private ManualEngineTask(Builder builder) {
        this.processDefinitionKey = builder.processDefinitionKey;
        this.processInstanceId = builder.processInstanceId;
        this.taskDefinitionKey = builder.taskDefinitionKey;
        this.taskId = builder.taskId;
        this.engineProcessInstanceId = builder.engineProcessInstanceId;
        this.name = builder.name;
        this.description = builder.description;
        this.assigneeId = builder.assigneeId;
        this.candidateAssigneeIds = Collections.unmodifiableSet(builder.candidateAssigneeIds);
        this.candidateGroupIds = Collections.unmodifiableSet(builder.candidateGroupIds);
        this.startTime = builder.startTime != null ? Date.class.cast(builder.startTime.clone()) : null;
        this.dueDate = builder.dueDate != null ? Date.class.cast(builder.dueDate.clone()) : null;
        this.priority = builder.priority;
        this.actionType = builder.actionType;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public String getTaskDefinitionKey() {
        return taskDefinitionKey;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getEngineProcessInstanceId() {
        return engineProcessInstanceId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getAssigneeId() {
        return assigneeId;
    }

    public Set<String> getCandidateAssigneeIds() {
        return candidateAssigneeIds;
    }

    public Set<String> getCandidateGroupIds() {
        return candidateGroupIds;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public int getPriority() {
        return priority;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public static final class Builder {

        private String processDefinitionKey;
        private String processInstanceId;
        private String taskDefinitionKey;
        private String taskId;
        private String engineProcessInstanceId;
        private String name;
        private String description;
        private String assigneeId;
        private Set<String> candidateAssigneeIds;
        private Set<String> candidateGroupIds;
        private Date startTime;
        private Date dueDate;
        private int priority;
        private ActionType actionType;

        public Builder() {
            this.candidateAssigneeIds = new HashSet<String>();
            this.candidateGroupIds = new HashSet<String>();
        }

        public ManualEngineTask build() {
            return new ManualEngineTask(this);
        }

        public Builder activityExecution(ActivityExecution activityExecution) {
            Map<String, Object> variables = activityExecution.getVariables();

            this.processDefinitionKey = String.class.cast(variables.get("PIECEWORK_PROCESS_DEFINITION_KEY"));
            this.processInstanceId = String.class.cast(variables.get("PIECEWORK_PROCESS_INSTANCE_ID"));
            this.engineProcessInstanceId = activityExecution.getProcessInstanceId();
            this.taskId = activityExecution.getId();
            this.taskDefinitionKey = activityExecution.getCurrentActivityId();
            this.name = activityExecution.getCurrentActivityName();
            this.actionType = ActionType.NONE;

            return this;
        }

        public Builder processDefinitionKey(String processDefinitionKey) {
            this.processDefinitionKey = processDefinitionKey;
            return this;
        }

        public Builder processInstanceId(String processInstanceId) {
            this.processInstanceId = processInstanceId;
            return this;
        }

        public Builder taskDefinitionKey(String taskDefinitionKey) {
            this.taskDefinitionKey = taskDefinitionKey;
            return this;
        }

        public Builder taskId(String taskId) {
            this.taskId = taskId;
            return this;
        }

        public Builder engineProcessInstanceId(String engineProcessInstanceId) {
            this.engineProcessInstanceId = engineProcessInstanceId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder assigneeId(String assigneeId) {
            this.assigneeId = assigneeId;
            return this;
        }

        public Builder candidateAssigneeId(String candidateAssigneeId) {
            this.candidateAssigneeIds.add(candidateAssigneeId);
            return this;
        }

        public Builder candidateGroupId(String candidateGroupId) {
            this.candidateGroupIds.add(candidateGroupId);
            return this;
        }

        public Builder startTime(Date startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder dueDate(Date dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder actionType(ActionType actionType) {
            this.actionType = actionType;
            return this;
        }

    }

}
