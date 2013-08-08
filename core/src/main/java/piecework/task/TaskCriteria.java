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
package piecework.task;

import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import piecework.model.Process;
import piecework.model.ProcessInstance;
import piecework.security.Sanitizer;
import piecework.util.ManyMap;

/**
 * @author James Renfro
 */
public class TaskCriteria {

    private static final Logger LOG = Logger.getLogger(TaskCriteria.class);
    public enum OrderBy { CREATED_TIME_ASC, CREATED_TIME_DESC, DUE_TIME_ASC, DUE_TIME_DESC, PRIORITY_ASC, PRIORITY_DESC };

    private final Set<Process> processes;
    private final String executionId;
    private final String businessKey;
    private final List<String> taskIds;
    private final String processDefinitionLabel;
    private final String processInstanceLabel;
    private final String processInstanceId;
    private final String applicationStatus;
    private final String applicationStatusExplanation;
    private final String processStatus;
    private final Integer minPriority;
    private final Integer maxPriority;
    private final String keyword;
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
        this.applicationStatus = builder.applicationStatus;
        this.applicationStatusExplanation = builder.applicationStatusExplanation;
        this.processStatus = builder.processStatus;
        this.processDefinitionLabel = builder.processDefinitionLabel;
        this.processInstanceLabel = builder.processInstanceLabel;
        this.processInstanceId = builder.processInstanceId;
        this.keyword = builder.keyword;
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

    public String getProcessDefinitionLabel() {
        return processDefinitionLabel;
    }

    public String getProcessInstanceLabel() {
        return processInstanceLabel;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getApplicationStatus() {
        return applicationStatus;
    }

    public String getApplicationStatusExplanation() {
        return applicationStatusExplanation;
    }

    public String getProcessStatus() {
        return processStatus;
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
        private String processDefinitionLabel;
        private String processInstanceLabel;
        private String processInstanceId;
        private String applicationStatus;
        private String applicationStatusExplanation;
        private String processStatus;
        private String keyword;
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
        private ManyMap<String, String> contentParameters;
        private ManyMap<String, String> sanitizedParameters;

        public Builder() {
            super();
            this.processes = new HashSet<Process>();
        }

        public Builder(Set<Process> allowedProcesses, Map<String, List<String>> queryParameters, Sanitizer sanitizer) {
            // Selected processes must be a subset of allowed processes
            Map<String, Process> processDefinitionKeyMap = new HashMap<String, Process>();
            if (allowedProcesses == null || allowedProcesses.isEmpty()) {
                this.processes = Collections.emptySet();
                return;
            }
            for (Process allowedProcess : allowedProcesses) {
                if (StringUtils.isEmpty(allowedProcess.getProcessDefinitionKey()))
                    continue;
                processDefinitionKeyMap.put(allowedProcess.getProcessDefinitionKey(), allowedProcess);
            }
            this.contentParameters = new ManyMap<String, String>();
            this.sanitizedParameters = new ManyMap<String, String>();
            Set<Process> selectedProcesses = null;
            if (queryParameters != null && sanitizer != null) {
                DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis();
                for (Map.Entry<String, List<String>> rawQueryParameterEntry : queryParameters.entrySet()) {
                    String key = sanitizer.sanitize(rawQueryParameterEntry.getKey());
                    if (StringUtils.isEmpty(key))
                        continue;

                    List<String> rawValues = rawQueryParameterEntry.getValue();
                    if (rawValues != null && !rawValues.isEmpty()) {
                        for (String rawValue : rawValues) {
                            String value = sanitizer.sanitize(rawValue);
                            if (StringUtils.isEmpty(value))
                                continue;

                            sanitizedParameters.putOne(key, value);

                            try {
                                boolean isEngineParameter = true;
                                if (key.equals("processDefinitionKey")) {
                                    if (selectedProcesses == null)
                                        selectedProcesses = new HashSet<Process>();

                                    Process process = processDefinitionKeyMap.get(value);
                                    if (process != null)
                                        selectedProcesses.add(process);
                                } else if (key.equals("taskId")) {
                                    if (this.taskIds == null)
                                        this.taskIds = new ArrayList<String>();
                                    this.taskIds.add(value);
                                } else if (key.equals("processInstanceLabel"))
                                    this.processInstanceLabel = value;
                                else if (key.equals("applicationStatus"))
                                    this.applicationStatus = value;
                                else if (key.equals("applicationStatus"))
                                    this.applicationStatus = value;
                                else if (key.equals("applicationStatusExplanation"))
                                    this.applicationStatusExplanation = value;
                                else if (key.equals("processStatus"))
                                    this.processStatus = value;
                                else if (key.equals("completedAfter"))
                                    this.dueAfter = dateTimeFormatter.parseDateTime(value).toDate();
                                else if (key.equals("dueAfter"))
                                    this.dueBefore = dateTimeFormatter.parseDateTime(value).toDate();
                                else if (key.equals("createdAfter"))
                                    this.createdAfter = dateTimeFormatter.parseDateTime(value).toDate();
                                else if (key.equals("createdBefore"))
                                    this.createdBefore = dateTimeFormatter.parseDateTime(value).toDate();
                                else if (key.equals("maxResults"))
                                    this.maxResults = Integer.valueOf(value);
                                else if (key.equals("firstResult"))
                                    this.firstResult = Integer.valueOf(value);
                                else if (key.equals("keyword"))
                                    this.keyword = value;
                                else if (key.equals("maxPriority"))
                                    this.maxPriority = Integer.valueOf(value);
                                else if (key.equals("minPriority"))
                                    this.minPriority = Integer.valueOf(value);
                                else if (key.equals("processInstanceId"))
                                    this.processInstanceId = value;
                                else {
                                    if (key.startsWith("__"))
                                        this.contentParameters.putOne(key.substring(2), value);
                                    else
                                        this.contentParameters.putOne(key, value);
                                }

                            } catch (NumberFormatException e) {
                                LOG.warn("Unable to parse query parameter key: " + key + " value: " + value, e);
                            } catch (IllegalArgumentException e) {
                                LOG.warn("Unable to parse query parameter key: " + key + " value: " + value, e);
                            }
                        }
                    }
                }
            }
            if (selectedProcesses == null)
                this.processes = allowedProcesses;
            else
                this.processes = selectedProcesses;
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

        public Builder processes(Collection<Process> processes) {
            if (this.processes == null)
                this.processes = new HashSet<Process>();
            if (processes != null)
                this.processes.addAll(processes);
            return this;
        }

        public Builder executionId(String executionId) {
            this.executionId = executionId;
            return this;
        }

        public Builder processInstanceId(String processInstanceId) {
            this.processInstanceId = processInstanceId;
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

        public Builder processStatus(String processStatus) {
            this.processStatus = processStatus;
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
