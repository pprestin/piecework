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


import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;
import piecework.Constants;
import piecework.common.SearchCriteria;
import piecework.model.SearchFacet;
import piecework.security.Sanitizer;
import piecework.util.SearchUtility;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author James Renfro
 */
public class SearchQueryBuilder {

    private final SearchCriteria searchCriteria;

    public SearchQueryBuilder(final SearchCriteria searchCriteria) {
        this.searchCriteria = searchCriteria;
    }

    public Query build(Set<String> allowedProcessDefinitionKeys, Sanitizer sanitizer) {
        Query query = new Query();

        Set<String> filteredProcessDefinitionKeys;

        if (searchCriteria.getProcessDefinitionKeys() != null && !searchCriteria.getProcessDefinitionKeys().isEmpty())
            filteredProcessDefinitionKeys = Sets.intersection(searchCriteria.getProcessDefinitionKeys(), allowedProcessDefinitionKeys);
        else
            filteredProcessDefinitionKeys = allowedProcessDefinitionKeys;

        query.addCriteria(where("processDefinitionKey").in(filteredProcessDefinitionKeys));

//        List<String> sortBy = searchCriteria.getSortBy();

        Map<SearchFacet, String> facetParameters = searchCriteria.getFacetParameters();
        if (facetParameters != null && !facetParameters.isEmpty()) {
            for (Map.Entry<SearchFacet, String> entry : facetParameters.entrySet()) {
                if (entry.getKey() == null)
                    continue;
                if (StringUtils.isEmpty(entry.getValue()))
                    continue;

                query.addCriteria(entry.getKey().criteria(entry.getValue()));
            }
        }

//        if (!searchCriteria.getProcessInstanceIds().isEmpty())
//            query.addCriteria(where("processInstanceId").in(searchCriteria.getProcessInstanceIds()));
//        if (StringUtils.isNotEmpty(searchCriteria.getBusinessKey()))
//            query.addCriteria(where("alias").is(searchCriteria.getBusinessKey()));
//        if (StringUtils.isNotEmpty(searchCriteria.getProcessDefinitionLabel()))
//            query.addCriteria(where("processDefinitionLabel").regex(searchCriteria.getProcessDefinitionLabel(), "i"));
//        if (StringUtils.isNotEmpty(searchCriteria.getProcessInstanceLabel()))
//            query.addCriteria(where("processInstanceLabel").regex(searchCriteria.getProcessInstanceLabel(), "i"));
//        if (StringUtils.isNotBlank(searchCriteria.getApplicationStatus()))
//            query.addCriteria(where("applicationStatus").is(searchCriteria.getApplicationStatus()));
//        if (StringUtils.isNotBlank(searchCriteria.getApplicationStatusExplanation()))
//            query.addCriteria(where("applicationStatus").regex(searchCriteria.getApplicationStatusExplanation(), "i"));
//
//        if (StringUtils.isNotBlank(searchCriteria.getProcessStatus())) {
//            if (!searchCriteria.getProcessStatus().equalsIgnoreCase("all"))
//                query.addCriteria(where("processStatus").is(searchCriteria.getProcessStatus()));
//        } else if (searchCriteria.getComplete() != null && searchCriteria.getComplete().booleanValue())
//            query.addCriteria(where("processStatus").is(Constants.ProcessStatuses.COMPLETE));
//        else if (searchCriteria.getSuspended() != null && searchCriteria.getSuspended().booleanValue())
//            query.addCriteria(where("processStatus").is(Constants.ProcessStatuses.SUSPENDED));
//        else if (searchCriteria.getCancelled() != null && searchCriteria.getCancelled().booleanValue())
//            query.addCriteria(where("processStatus").is(Constants.ProcessStatuses.CANCELLED));
//        else if (searchCriteria.getQueued() != null && searchCriteria.getQueued().booleanValue())
//            query.addCriteria(where("processStatus").is(Constants.ProcessStatuses.QUEUED));
//        else if (searchCriteria.getAll() == null || !searchCriteria.getAll().booleanValue())
//            query.addCriteria(where("processStatus").is(Constants.ProcessStatuses.OPEN));

        if (StringUtils.isNotEmpty(searchCriteria.getInitiatedBy()))
            query.addCriteria(where("initiatorId").is(searchCriteria.getInitiatedBy()));

        if (searchCriteria.getStartedBefore() != null && searchCriteria.getStartedAfter() != null)
            query.addCriteria(where("startTime").lt(searchCriteria.getStartedBefore()).gt(searchCriteria.getStartedAfter()));
        else if (searchCriteria.getStartedBefore() != null)
            query.addCriteria(where("startTime").lt(searchCriteria.getStartedBefore()));
        else if (searchCriteria.getStartedAfter() != null)
            query.addCriteria(where("startTime").gt(searchCriteria.getStartedAfter()));

        if (searchCriteria.getCompletedBefore() != null)
            query.addCriteria(where("endTime").lt(searchCriteria.getCompletedBefore()));
        if (searchCriteria.getCompletedAfter() != null)
            query.addCriteria(where("endTime").gt(searchCriteria.getCompletedAfter()));

        if (!searchCriteria.getKeywords().isEmpty()) {
            for (String keyword : searchCriteria.getKeywords())
                query.addCriteria(where("keywords").regex(keyword.toLowerCase()));
        }

        if (searchCriteria.getMaxResults() != null)
            query.limit(searchCriteria.getMaxResults());

        if (searchCriteria.getFirstResult() != null)
            query.skip(searchCriteria.getFirstResult());

        if (searchCriteria.getSortBy() != null) {
            boolean hasSearchFilter = false;

            query.with(SearchUtility.sort(searchCriteria, sanitizer));
        }

//        else {
//            query.with(new Sort(Sort.Direction.DESC, "lastModifiedTime")
//                .and(new Sort(Sort.Direction.DESC, "startTime")));
//        }

//        if (searchCriteria.getOrderBy() != null) {
//            switch (searchCriteria.getOrderBy()) {
//                case START_TIME_ASC:
//                    query.with(new Sort(Sort.Direction.ASC, "startTime"));
//                    break;
//                case START_TIME_DESC:
//                    query.with(new Sort(Sort.Direction.DESC, "startTime"));
//                    break;
//                case END_TIME_ASC:
//                    query.with(new Sort(Sort.Direction.ASC, "endTime"));
//                    break;
//                case END_TIME_DESC:
//                    query.with(new Sort(Sort.Direction.DESC, "endTime"));
//                    break;
//            }
//        } else {
//            query.with(new Sort(Sort.Direction.DESC, "startTime"));
//        }

        return query;
    }

}
