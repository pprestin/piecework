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
package piecework.common;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.data.domain.Sort;
import piecework.model.*;
import piecework.model.Process;
import piecework.security.Sanitizer;
import piecework.util.SearchUtility;

import java.util.*;

/**
 * @author James Renfro
 */
public class SearchCriteria {
    private static final Logger LOG = Logger.getLogger(SearchCriteria.class);

    private final Set<String> processDefinitionKeys;
    private final String processInstanceId;
    private final Set<String> engines;
    private final Set<String> engineProcessDefinitionKeys;
    private final String processStatus;
    private final String taskStatus;
    private final List<String> keywords;
    private final List<String> executionIds;
    private final String initiatedBy;
    private final Integer pageNumber;
    private final Integer pageSize;
    private final boolean includeVariables;
    private final Map<SearchFacet, Object> facetParameters;
    private final Map<DataFilterFacet, String> filterFacetParameters;
    private final Map<String, List<String>> contentParameters;
    private final Map<String, List<String>> sanitizedParameters;
    private final List<FacetSort> sortBy;

    private SearchCriteria() {
        this(new Builder());
    }

    private SearchCriteria(Builder builder) {
        this.processDefinitionKeys = Collections.unmodifiableSet(builder.processDefinitionKeys);
        this.processInstanceId = builder.processInstanceId;
        this.engines = builder.engines != null ? Collections.unmodifiableSet(builder.engines) : Collections.<String>emptySet();
        this.engineProcessDefinitionKeys = Collections.unmodifiableSet(builder.engineProcessDefinitionKeys);
        this.processStatus = builder.processStatus;
        this.taskStatus = builder.taskStatus;
        this.keywords = Collections.unmodifiableList(builder.keywords);
        this.executionIds = builder.executionIds;
        this.initiatedBy = builder.initiatedBy;
        this.pageNumber = builder.page;
        this.pageSize= builder.pageSize;
        this.facetParameters = Collections.unmodifiableMap(builder.facetParameters);
        this.filterFacetParameters = Collections.unmodifiableMap(builder.filterFacetParameters);
        this.contentParameters = Collections.unmodifiableMap(builder.contentParameters);
        this.sanitizedParameters = Collections.unmodifiableMap(builder.sanitizedParameters);
        this.includeVariables = builder.includeVariables;
        this.sortBy = Collections.unmodifiableList(builder.sortBy);
    }

    public Set<String> getProcessDefinitionKeys() {
        return processDefinitionKeys;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public Set<String> getEngines() {
        return engines;
    }

    public Set<String> getEngineProcessDefinitionKeys() {
        return engineProcessDefinitionKeys;
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

    public Map<SearchFacet, Object> getFacetParameters() {
        return facetParameters;
    }

    public Map<DataFilterFacet, String> getFilterFacetParameters() { return filterFacetParameters; }

    public Map<String, List<String>> getContentParameters() {
        return contentParameters;
    }

    public Map<String, List<String>> getSanitizedParameters() {
        return sanitizedParameters;
    }

    public List<String> getExecutionIds() {
        return executionIds;
    }

    public String getInitiatedBy() {
        return initiatedBy;
    }

    public List<FacetSort> getSortBy() {
        return sortBy;
    }

    public List<FacetSort> getPostQuerySortBy() {
        List<FacetSort> filtered = new ArrayList<FacetSort>();
        if (sortBy != null) {
            for (FacetSort facetSort : sortBy) {
                if (facetSort.getFacet() instanceof DataFilterFacet)
                    filtered.add(facetSort);
            }
        }

        return filtered;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public boolean isIncludeVariables() {
        return includeVariables;
    }

    public Sort getSort(Sanitizer sanitizer) {
        return SearchUtility.sort(this, sanitizer);
    }

    public final static class Builder {
        private Set<String> limitToProcessDefinitionKeys;
        private Set<String> processDefinitionKeys;
        private Set<String> engines;
        private Set<String> engineProcessDefinitionKeys;
        private String processInstanceId;
//        private Set<String> processInstanceIds;
//        private String businessKey;
//        private String processDefinitionLabel;
//        private String processInstanceLabel;
//        private String applicationStatus;
//        private String applicationStatusExplanation;
        private String processStatus;
        private String taskStatus;
        private List<String> executionIds;
//        private Boolean complete;
//        private Boolean suspended;
//        private Boolean cancelled;
//        private Boolean queued;
//        private Boolean all;
//        private Date startedBefore;
//        private Date startedAfter;
//        private Date completedBefore;
//        private Date completedAfter;
        private String initiatedBy;
//        private String direction;
        private List<FacetSort> sortBy;
        private Integer page;
        private Integer pageSize;
        private List<String> keywords;
        private Map<SearchFacet, Object> facetParameters;
        private Map<DataFilterFacet, String> filterFacetParameters;
        private ManyMap<String, String> contentParameters;
        private ManyMap<String, String> sanitizedParameters;
        private boolean includeVariables;

        public Builder() {
            this(null, null, null, null);
        }

        public Builder(Map<String, List<String>> queryParameters, Set<Process> processes, Map<String, Facet> facetMap, Sanitizer sanitizer) {
            this.limitToProcessDefinitionKeys = new HashSet<String>();
            this.processDefinitionKeys = new HashSet<String>();
//            this.processInstanceIds = new HashSet<String>();
            this.engines = new HashSet<String>();
            this.engineProcessDefinitionKeys = new HashSet<String>();
            this.keywords = new ArrayList<String>();
            this.facetParameters = new HashMap<SearchFacet, Object>();
            this.filterFacetParameters = new HashMap<DataFilterFacet, String>();
            this.contentParameters = new ManyMap<String, String>();
            this.sanitizedParameters = new ManyMap<String, String>();
            this.sortBy = new ArrayList<FacetSort>();
            if (queryParameters != null && sanitizer != null) {
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
                                    this.processInstanceId = value;
//                                else if (key.equals("complete"))
//                                    this.complete = Boolean.valueOf(value);
//                                else if (key.equals("cancelled"))
//                                    this.cancelled = Boolean.valueOf(value);
//                                else if (key.equals("suspended"))
//                                    this.suspended = Boolean.valueOf(value);
//                                else if (key.equals("queued"))
//                                    this.queued = Boolean.valueOf(value);
//                                else if (key.equals("all"))
//                                    this.all = Boolean.valueOf(value);
//                                else if (key.equals("processDefinitionLabel"))
//                                    this.processDefinitionLabel = value;
//                                else if (key.equals("processInstanceLabel"))
//                                    this.processInstanceLabel = value;
//                                else if (key.equals("applicationStatus"))
//                                    this.applicationStatus = value;
//                                else if (key.equals("applicationStatus"))
//                                    this.applicationStatus = value;
//                                else if (key.equals("applicationStatusExplanation"))
//                                    this.applicationStatusExplanation = value;
                                else if (key.equals("processStatus"))
                                    this.processStatus = value;
//                                else if (key.equals("taskStatus"))
//                                    this.taskStatus = value;
                                else if (key.equals("initiatedBy"))
                                    this.initiatedBy = value;
                                else if (key.equals("sortBy")) {
                                    if (StringUtils.isNotEmpty(value)) {
                                        int indexOf = value.indexOf(':');
                                        String facetKey = value;
                                        String direction = "desc";
                                        if (indexOf != -1 && (indexOf+1) < value.length()) {
                                            facetKey = value.substring(0, indexOf);
                                            direction = value.substring(indexOf + 1);
                                        }

                                        Facet facet = facetMap.get(facetKey);
                                        if (facet != null)
                                            this.sortBy.add(new FacetSort(facet, direction));
                                    }
                                }
//                                else if (key.equals("direction"))
//                                    this.direction = value;
//                                else if (key.equals("completedAfter"))
//                                    this.completedAfter = dateTimeFormatter.parseDateTime(value).toDate();
//                                else if (key.equals("completedBefore"))
//                                    this.completedBefore = dateTimeFormatter.parseDateTime(value).toDate();
//                                else if (key.equals("startedAfter"))
//                                    this.startedAfter = dateTimeFormatter.parseDateTime(value).toDate();
//                                else if (key.equals("startedBefore"))
//                                    this.startedBefore = dateTimeFormatter.parseDateTime(value).toDate();
                                else if (key.equals("pageNumber"))
                                    this.page = Integer.valueOf(value);
                                else if (key.equals("pageSize"))
                                    this.pageSize = Integer.valueOf(value);
                                else if (key.equals("keyword"))
                                    this.keywords.add(value);
                                else if (key.equals("verbose")) {
                                    if (value != null && value.equals("true"))
                                        this.includeVariables = true;
                                } else if (facetMap.containsKey(key)) {
                                    Facet facet = facetMap.get(key);
                                    if (facet != null && StringUtils.isNotEmpty(value)) {
                                        if (facet instanceof DateSearchFacet) {
                                            DateSearchFacet dateSearchFacet = DateSearchFacet.class.cast(facet);
                                            DateRange dateRange = DateRange.class.cast(this.facetParameters.get(dateSearchFacet));
                                            if (dateRange == null) {
                                                dateRange = new DateRange();
                                                this.facetParameters.put(dateSearchFacet, dateRange);
                                            }
                                            if (key.endsWith("After"))
                                                dateRange.setAfter(value);
                                            else if (key.endsWith("Before"))
                                                dateRange.setBefore(value);

                                        } else if (facet instanceof SearchFacet)
                                            this.facetParameters.put(SearchFacet.class.cast(facet), value);
                                        else if (facet instanceof DataFilterFacet)
                                            this.filterFacetParameters.put(DataFilterFacet.class.cast(facet), value);
                                    }
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
            if (processes != null) {
                for (Process process : processes) {
                    processDefinitionKey(process.getProcessDefinitionKey());
                }
            }
        }

        public SearchCriteria build() {
            if (this.sortBy.isEmpty())
                this.sortBy.add(new FacetSort(FacetFactory.defaultSearch(), "desc"));

            return new SearchCriteria(this);
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

//        public Builder processDefinitionLabel(String processDefinitionLabel) {
//            this.processDefinitionLabel = processDefinitionLabel;
//            return this;
//        }
//
//        public Builder processInstanceLabel(String processInstanceLabel) {
//            this.processInstanceLabel = processInstanceLabel;
//            return this;
//        }
//
//        public Builder businessKey(String businessKey) {
//            this.businessKey = businessKey;
//            return this;
//        }

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

//        public Builder complete(Boolean complete) {
//            this.complete = complete;
//            return this;
//        }

        public Builder keyword(String keyword) {
            if (this.keywords == null)
                this.keywords = new ArrayList<String>();
            if (StringUtils.isNotEmpty(keyword))
                this.keywords.add(keyword);
            return this;
        }

//        public Builder startedBefore(Date startedBefore) {
//            this.startedBefore = startedBefore;
//            return this;
//        }
//
//        public Builder startedAfter(Date startedAfter) {
//            this.startedAfter = startedAfter;
//            return this;
//        }
//
//        public Builder completedBefore(Date completedBefore) {
//            this.completedBefore = completedBefore;
//            return this;
//        }
//
//        public Builder completedAfter(Date completedAfter) {
//            this.completedAfter = completedAfter;
//            return this;
//        }

        public Builder initiatedBy(String initiatedBy) {
            this.initiatedBy = initiatedBy;
            return this;
        }

//        public Builder direction(String direction) {
//            this.direction = direction;
//            return this;
//        }
//
//        public Builder applicationStatus(String applicationStatus) {
//            this.applicationStatus = applicationStatus;
//            return this;
//        }
//
//        public Builder applicationStatusExplanation(String applicationStatusExplanation) {
//            this.applicationStatusExplanation = applicationStatusExplanation;
//            return this;
//        }

        public Builder processStatus(String processStatus) {
            this.processStatus = processStatus;
            return this;
        }

        public Builder taskStatus(String taskStatus) {
            this.taskStatus = taskStatus;
            return this;
        }

        public Builder sortBy(FacetSort sortBy) {
            if (sortBy != null)
                this.sortBy.add(sortBy);
            return this;
        }

        public Builder page(Integer page) {
            this.page = page;
            return this;
        }

        public Builder pageSize(Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder includeVariables() {
            this.includeVariables = true;
            return this;
        }

    }

}
