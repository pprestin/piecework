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
package piecework.process;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.data.domain.Sort;
import piecework.security.Sanitizer;
import piecework.common.ManyMap;

import java.util.*;

/**
 * @author James Renfro
 */
public class ProcessInstanceSearchCriteria {
    private static final Logger LOG = Logger.getLogger(ProcessInstanceSearchCriteria.class);

    public enum OrderBy { START_TIME_ASC, START_TIME_DESC, END_TIME_ASC, END_TIME_DESC };

    private final Set<String> processDefinitionKeys;
    private final Set<String> engines;
    private final Set<String> engineProcessDefinitionKeys;
    private final Set<String> processInstanceIds;
    private final String businessKey;
    private final String processDefinitionLabel;
    private final String processInstanceLabel;
    private final String applicationStatus;
    private final String applicationStatusExplanation;
    private final String processStatus;
    private final String taskStatus;
    private final List<String> keywords;
    private final List<String> executionIds;
    private final Boolean complete;
    private final Boolean suspended;
    private final Boolean cancelled;
    private final Boolean all;
    private final Date startedBefore;
    private final Date startedAfter;
    private final Date completedBefore;
    private final Date completedAfter;
    private final String initiatedBy;
    private final Integer firstResult;
    private final Integer maxResults;
    private final boolean includeVariables;
    private final Map<String, List<String>> contentParameters;
    private final Map<String, List<String>> sanitizedParameters;
    private final OrderBy orderBy;

    private ProcessInstanceSearchCriteria() {
        this(new Builder());
    }

    private ProcessInstanceSearchCriteria(Builder builder) {
        this.processDefinitionKeys = Collections.unmodifiableSet(builder.processDefinitionKeys);
        this.processInstanceIds = Collections.unmodifiableSet(builder.processInstanceIds);
        this.engines = Collections.unmodifiableSet(builder.engines);
        this.engineProcessDefinitionKeys = Collections.unmodifiableSet(builder.engineProcessDefinitionKeys);
        this.businessKey = builder.businessKey;
        this.applicationStatus = builder.applicationStatus;
        this.applicationStatusExplanation = builder.applicationStatusExplanation;
        this.processStatus = builder.processStatus;
        this.taskStatus = builder.taskStatus;
        this.processDefinitionLabel = builder.processDefinitionLabel;
        this.processInstanceLabel = builder.processInstanceLabel;
        this.keywords = Collections.unmodifiableList(builder.keywords);
        this.executionIds = builder.executionIds;
        this.complete = builder.complete;
        this.cancelled = builder.cancelled;
        this.suspended = builder.suspended;
        this.all = builder.all;
        this.startedBefore = builder.startedBefore;
        this.startedAfter = builder.startedAfter;
        this.completedBefore = builder.completedBefore;
        this.completedAfter = builder.completedAfter;
        this.initiatedBy = builder.initiatedBy;
        this.orderBy = builder.orderBy;
        this.firstResult = builder.firstResult;
        this.maxResults = builder.maxResults;
        this.contentParameters = Collections.unmodifiableMap(builder.contentParameters);
        this.sanitizedParameters = Collections.unmodifiableMap(builder.sanitizedParameters);
        this.includeVariables = builder.includeVariables;
    }

    public Set<String> getProcessDefinitionKeys() {
        return processDefinitionKeys;
    }

    public Set<String> getProcessInstanceIds() {
        return processInstanceIds;
    }

    public Set<String> getEngines() {
        return engines;
    }

    public Set<String> getEngineProcessDefinitionKeys() {
        return engineProcessDefinitionKeys;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public String getApplicationStatus() {
        return applicationStatus;
    }

    public String getApplicationStatusExplanation() {
        return applicationStatusExplanation;
    }

    public String getProcessDefinitionLabel() {
        return processDefinitionLabel;
    }

    public String getProcessInstanceLabel() {
        return processInstanceLabel;
    }

    public String getProcessStatus() {
        return processStatus;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public Map<String, List<String>> getContentParameters() {
        return contentParameters;
    }

    public Map<String, List<String>> getSanitizedParameters() {
        return sanitizedParameters;
    }

    public List<String> getExecutionIds() {
        return executionIds;
    }

    public Boolean getComplete() {
        return complete;
    }

    public Boolean getSuspended() {
        return suspended;
    }

    public Boolean getCancelled() {
        return cancelled;
    }

    public Boolean getAll() {
        return all;
    }

    public Date getStartedBefore() {
        return startedBefore;
    }

    public Date getStartedAfter() {
        return startedAfter;
    }

    public Date getCompletedBefore() {
        return completedBefore;
    }

    public Date getCompletedAfter() {
        return completedAfter;
    }

    public String getInitiatedBy() {
        return initiatedBy;
    }

    public OrderBy getOrderBy() {
        return orderBy;
    }

    public Integer getFirstResult() {
        return firstResult;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public boolean isIncludeVariables() {
        return includeVariables;
    }

    public Sort getSort() {
        Sort.Direction direction = Sort.Direction.DESC;
        String sortProperty = "startTime";
        switch (getOrderBy()) {
            case START_TIME_ASC:
                direction = Sort.Direction.ASC;
                sortProperty = "startTime";
                break;
            case START_TIME_DESC:
                direction = Sort.Direction.DESC;
                sortProperty = "startTime";
                break;
            case END_TIME_ASC:
                direction = Sort.Direction.ASC;
                sortProperty = "endTime";
                break;
            case END_TIME_DESC:
                direction = Sort.Direction.DESC;
                sortProperty = "endTime";
                break;
        }
        return new Sort(direction, sortProperty);
    }

    public final static class Builder {
        private Set<String> limitToProcessDefinitionKeys;
        private Set<String> processDefinitionKeys;
        private Set<String> engines;
        private Set<String> engineProcessDefinitionKeys;
        private Set<String> processInstanceIds;
        private String businessKey;
        private String processDefinitionLabel;
        private String processInstanceLabel;
        private String applicationStatus;
        private String applicationStatusExplanation;
        private String processStatus;
        private String taskStatus;
        private List<String> executionIds;
        private Boolean complete;
        private Boolean suspended;
        private Boolean cancelled;
        private Boolean all;
        private Date startedBefore;
        private Date startedAfter;
        private Date completedBefore;
        private Date completedAfter;
        private String initiatedBy;
        private OrderBy orderBy = OrderBy.START_TIME_DESC;
        private Integer firstResult;
        private Integer maxResults;
        private List<String> keywords;
        private ManyMap<String, String> contentParameters;
        private ManyMap<String, String> sanitizedParameters;
        private boolean includeVariables;

        public Builder() {
            this(null, null);
        }

        public Builder(Map<String, List<String>> queryParameters, Sanitizer sanitizer) {
            this.limitToProcessDefinitionKeys = new HashSet<String>();
            this.processDefinitionKeys = new HashSet<String>();
            this.processInstanceIds = new HashSet<String>();
            this.engines = new HashSet<String>();
            this.engineProcessDefinitionKeys = new HashSet<String>();
            this.keywords = new ArrayList<String>();
            this.contentParameters = new ManyMap<String, String>();
            this.sanitizedParameters = new ManyMap<String, String>();
            if (queryParameters != null && sanitizer != null) {
                DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTimeParser();
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
                                if (key.equals("processDefinitionKey"))
                                    this.limitToProcessDefinitionKeys.add(value);
                                else if (key.equals("processInstanceId"))
                                    this.processInstanceIds.add(value);
                                else if (key.equals("complete"))
                                    this.complete = Boolean.valueOf(value);
                                else if (key.equals("cancelled"))
                                    this.cancelled = Boolean.valueOf(value);
                                else if (key.equals("suspended"))
                                    this.suspended = Boolean.valueOf(value);
                                else if (key.equals("all"))
                                    this.all = Boolean.valueOf(value);
                                else if (key.equals("processDefinitionLabel"))
                                    this.processDefinitionLabel = value;
                                else if (key.equals("processInstanceLabel"))
                                    this.processInstanceLabel = value;
                                else if (key.equals("applicationStatus"))
                                    this.applicationStatus = value;
                                else if (key.equals("applicationStatus"))
                                    this.applicationStatus = value;
                                else if (key.equals("applicationStatusExplanation"))
                                    this.applicationStatusExplanation = value;
                                else if (key.equals("processStatus"))
                                    this.processStatus = value;
                                else if (key.equals("taskStatus"))
                                    this.taskStatus = value;
                                else if (key.equals("initiatedBy"))
                                    this.initiatedBy = value;
                                else if (key.equals("orderBy"))
                                    this.orderBy = ProcessInstanceSearchCriteria.OrderBy.valueOf(value);
                                else if (key.equals("completedAfter"))
                                    this.completedAfter = dateTimeFormatter.parseDateTime(value).toDate();
                                else if (key.equals("completedBefore"))
                                    this.completedBefore = dateTimeFormatter.parseDateTime(value).toDate();
                                else if (key.equals("startedAfter"))
                                    this.startedAfter = dateTimeFormatter.parseDateTime(value).toDate();
                                else if (key.equals("startedBefore"))
                                    this.startedBefore = dateTimeFormatter.parseDateTime(value).toDate();
                                else if (key.equals("maxResults"))
                                    this.maxResults = Integer.valueOf(value);
                                else if (key.equals("firstResult"))
                                    this.firstResult = Integer.valueOf(value);
                                else if (key.equals("keyword"))
                                    this.keywords.add(value);
                                else if (key.equals("verbose")) {
                                    if (value != null && value.equals("true"))
                                        this.includeVariables = true;
                                } else {
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
        }

        public ProcessInstanceSearchCriteria build() {
            return new ProcessInstanceSearchCriteria(this);
        }

        public Builder processDefinitionKey(String processDefinitionKey) {
            if (this.limitToProcessDefinitionKeys.isEmpty() || this.limitToProcessDefinitionKeys.contains(processDefinitionKey)) {
                if (this.processDefinitionKeys == null)
                    this.processDefinitionKeys = new HashSet<String>();
                this.processDefinitionKeys.add(processDefinitionKey);
            }
            return this;
        }

        public Builder engine(String engine) {
            if (this.engines == null)
                this.engines = new HashSet<String>();
            this.engines.add(engine);
            return this;
        }

        public Builder engineProcessDefinitionKey(String engineProcessDefinitionKey) {
            if (this.engineProcessDefinitionKeys == null)
                this.engineProcessDefinitionKeys = new HashSet<String>();
            this.engineProcessDefinitionKeys.add(engineProcessDefinitionKey);
            return this;
        }

        public Builder processDefinitionLabel(String processDefinitionLabel) {
            this.processDefinitionLabel = processDefinitionLabel;
            return this;
        }

        public Builder processInstanceLabel(String processInstanceLabel) {
            this.processInstanceLabel = processInstanceLabel;
            return this;
        }

        public Builder businessKey(String businessKey) {
            this.businessKey = businessKey;
            return this;
        }

        public Builder executionId(String executionId) {
            if (this.executionIds == null)
                this.executionIds = new ArrayList<String>();
            this.executionIds.add(executionId);
            return this;
        }

        public Builder executionIds(List<String> executionIds) {
            if (this.executionIds == null)
                this.executionIds = new ArrayList<String>();
            this.executionIds.addAll(executionIds);
            return this;
        }

        public Builder complete(Boolean complete) {
            this.complete = complete;
            return this;
        }

        public Builder keyword(String keyword) {
            if (this.keywords == null)
                this.keywords = new ArrayList<String>();
            if (StringUtils.isNotEmpty(keyword))
                this.keywords.add(keyword);
            return this;
        }

        public Builder startedBefore(Date startedBefore) {
            this.startedBefore = startedBefore;
            return this;
        }

        public Builder startedAfter(Date startedAfter) {
            this.startedAfter = startedAfter;
            return this;
        }

        public Builder completedBefore(Date completedBefore) {
            this.completedBefore = completedBefore;
            return this;
        }

        public Builder completedAfter(Date completedAfter) {
            this.completedAfter = completedAfter;
            return this;
        }

        public Builder initiatedBy(String initiatedBy) {
            this.initiatedBy = initiatedBy;
            return this;
        }

        public Builder applicationStatus(String applicationStatus) {
            this.applicationStatus = applicationStatus;
            return this;
        }

        public Builder applicationStatusExplanation(String applicationStatusExplanation) {
            this.applicationStatusExplanation = applicationStatusExplanation;
            return this;
        }

        public Builder processStatus(String processStatus) {
            this.processStatus = processStatus;
            return this;
        }

        public Builder taskStatus(String taskStatus) {
            this.taskStatus = taskStatus;
            return this;
        }

        public Builder orderBy(OrderBy orderBy) {
            this.orderBy = orderBy;
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

        public Builder includeVariables() {
            this.includeVariables = true;
            return this;
        }

    }

}
