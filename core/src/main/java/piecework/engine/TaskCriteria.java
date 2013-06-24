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

import java.util.*;
import piecework.model.Process;

/**
 * @author James Renfro
 */
public class TaskCriteria {

    public enum OrderBy { CREATED_TIME_ASC, CREATED_TIME_DESC, DUE_TIME_ASC, DUE_TIME_DESC, PRIORITY_ASC, PRIORITY_DESC };

    private final Set<Process> processes;
    private final String executionId;
    private final String businessKey;
    private final List<String> taskIds;
    private final Integer minPriority;
    private final Integer maxPriority;
    private final Boolean active;
    private final Boolean complete;
    private final Date createdBefore;
    private final Date createdAfter;
    private final Date dueBefore;
    private final Date dueAfter;
    private final String assigneeId;
    private final String candidateAssigneeId;
    private final String participantId;
    private final Integer firstResult;
    private final Integer maxResults;
    private final OrderBy orderBy;

    private TaskCriteria() {
        this(new Builder());
    }

    private TaskCriteria(Builder builder) {
        this.processes = Collections.unmodifiableSet(builder.processes);
        this.executionId = builder.executionId;
        this.businessKey = builder.businessKey;
        this.taskIds = builder.taskIds;
        this.minPriority = builder.minPriority;
        this.maxPriority = builder.maxPriority;
        this.active = builder.active;
        this.complete = builder.complete;
        this.createdBefore = builder.createdBefore;
        this.createdAfter = builder.createdAfter;
        this.dueBefore = builder.dueBefore;
        this.dueAfter = builder.dueAfter;
        this.assigneeId = builder.assigneeId;
        this.candidateAssigneeId = builder.candidateAssigneeId;
        this.participantId = builder.participantId;
        this.firstResult = builder.firstResult;
        this.maxResults = builder.maxResults;
        this.orderBy = builder.orderBy;
    }

    public Set<Process> getProcesses() {
        return processes;
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public List<String> getTaskIds() {
        return taskIds;
    }

    public Integer getMinPriority() {
        return minPriority;
    }

    public Integer getMaxPriority() {
        return maxPriority;
    }

    public Boolean getActive() {
        return active;
    }

    public Boolean getComplete() {
        return complete;
    }

    public Date getCreatedBefore() {
        return createdBefore;
    }

    public Date getCreatedAfter() {
        return createdAfter;
    }

    public Date getDueBefore() {
        return dueBefore;
    }

    public Date getDueAfter() {
        return dueAfter;
    }

    public String getAssigneeId() {
        return assigneeId;
    }

    public String getCandidateAssigneeId() {
        return candidateAssigneeId;
    }

    public String getParticipantId() {
        return participantId;
    }

    public Integer getFirstResult() {
        return firstResult;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public OrderBy getOrderBy() {
        return orderBy;
    }

    public final static class Builder {

        private Set<Process> processes;
        private String executionId;
        private String businessKey;
        private List<String> taskIds;
        private Integer minPriority;
        private Integer maxPriority;
        private Boolean complete;
        private Boolean active;
        private Date createdBefore;
        private Date createdAfter;
        private Date dueBefore;
        private Date dueAfter;
        private String assigneeId;
        private String candidateAssigneeId;
        private String participantId;
        private Integer firstResult;
        private Integer maxResults;
        private OrderBy orderBy;

        public Builder() {
            super();
            this.processes = new HashSet<Process>();
        }

        public TaskCriteria build() {
            return new TaskCriteria(this);
        }

        public Builder process(Process process) {
            if (this.processes == null)
                this.processes = new HashSet<Process>();
            if (process != null)
                this.processes.add(process);
            return this;
        }

        public Builder executionId(String executionId) {
            this.executionId = executionId;
            return this;
        }

        public Builder businessKey(String businessKey) {
            this.businessKey = businessKey;
            return this;
        }

        public Builder taskId(String taskId) {
            if (this.taskIds == null)
                this.taskIds = new ArrayList<String>();
            this.taskIds.add(taskId);
            return this;
        }

        public Builder taskIds(List<String> taskIds) {
            if (this.taskIds == null)
                this.taskIds = new ArrayList<String>();
            this.taskIds.addAll(taskIds);
            return this;
        }

        public Builder minPriority(Integer minPriority) {
            this.minPriority = minPriority;
            return this;
        }

        public Builder maxPriority(Integer maxPriority) {
            this.maxPriority = maxPriority;
            return this;
        }

        public Builder active(Boolean active) {
            this.active = active;
            return this;
        }

        public Builder complete(Boolean complete) {
            this.complete = complete;
            return this;
        }

        public Builder createdBefore(Date createdBefore) {
            this.createdBefore = createdBefore;
            return this;
        }

        public Builder createdAfter(Date createdAfter) {
            this.createdAfter = createdAfter;
            return this;
        }

        public Builder dueBefore(Date dueBefore) {
            this.dueBefore = dueBefore;
            return this;
        }

        public Builder dueAfter(Date dueAfter) {
            this.dueAfter = dueAfter;
            return this;
        }

        public Builder assigneeId(String assigneeId) {
            this.assigneeId = assigneeId;
            return this;
        }

        public Builder candidateAssigneeId(String candidateAssigneeId) {
            this.candidateAssigneeId = candidateAssigneeId;
            return this;
        }

        public Builder participantId(String participantId) {
            this.participantId = participantId;
            return this;
        }

        public Builder firstResult(Integer firstResult) {
            this.firstResult = firstResult;
            return this;
        }

        public Builder maxResults(Integer maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        public Builder orderBy(OrderBy orderBy) {
            this.orderBy = orderBy;
            return this;
        }
    }
}
