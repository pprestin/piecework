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
package piecework.util;

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;
import piecework.common.FacetSort;
import piecework.common.SearchCriteria;
import piecework.common.SearchQueryParameters;
import piecework.model.*;
import piecework.model.Process;
import piecework.security.Sanitizer;

import java.util.*;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author James Renfro
 */
public class SearchUtility {

    public static final Query query(SearchQueryParameters queryParameters, Set<String> allowedProcessDefinitionKeys, Sanitizer sanitizer) {
        Query query = new Query();

        Set<String> filteredProcessDefinitionKeys;

        if (queryParameters.getProcessDefinitionKey() != null && !queryParameters.getProcessDefinitionKey().isEmpty())
            filteredProcessDefinitionKeys = Sets.intersection(queryParameters.getProcessDefinitionKey(), allowedProcessDefinitionKeys);
        else
            filteredProcessDefinitionKeys = allowedProcessDefinitionKeys;

        query.addCriteria(where("processDefinitionKey").in(filteredProcessDefinitionKeys));


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
//
//        if (StringUtils.isNotEmpty(searchCriteria.getInitiatedBy()))
//            query.addCriteria(where("initiatorId").is(searchCriteria.getInitiatedBy()));
//
//        if (searchCriteria.getStartedBefore() != null && searchCriteria.getStartedAfter() != null)
//            query.addCriteria(where("startTime").lt(searchCriteria.getStartedBefore()).gt(searchCriteria.getStartedAfter()));
//        else if (searchCriteria.getStartedBefore() != null)
//            query.addCriteria(where("startTime").lt(searchCriteria.getStartedBefore()));
//        else if (searchCriteria.getStartedAfter() != null)
//            query.addCriteria(where("startTime").gt(searchCriteria.getStartedAfter()));
//
//        if (searchCriteria.getCompletedBefore() != null)
//            query.addCriteria(where("endTime").lt(searchCriteria.getCompletedBefore()));
//        if (searchCriteria.getCompletedAfter() != null)
//            query.addCriteria(where("endTime").gt(searchCriteria.getCompletedAfter()));
//
//        if (!searchCriteria.getKeywords().isEmpty()) {
//            for (String keyword : searchCriteria.getKeywords())
//                query.addCriteria(where("keywords").regex(keyword.toLowerCase()));
//        }
//
//        if (searchCriteria.getMaxResults() != null)
//            query.limit(searchCriteria.getMaxResults());
//
//        if (searchCriteria.getFirstResult() != null)
//            query.skip(searchCriteria.getFirstResult());
//
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

    public static final Pageable pageable(SearchCriteria criteria, Sanitizer sanitizer) {
        int firstResult = criteria.getFirstResult() != null ? criteria.getFirstResult() : 0;
        int maxResult = criteria.getMaxResults() != null ? criteria.getMaxResults() : 1000;
        Sort sort = SearchUtility.sort(criteria, sanitizer);
        PageRequest pageRequest = new PageRequest(firstResult, maxResult, sort);
        return pageRequest;
    }

    public static final Sort sort(SearchCriteria criteria, Sanitizer sanitizer) {
        Sort sort = null;
        if (criteria.getSortBy() != null && !criteria.getSortBy().isEmpty()) {
            for (FacetSort facetSort : criteria.getSortBy()) {
                if (facetSort == null)
                    continue;
                if (facetSort.getFacet() instanceof SearchFacet) {
                    Sort.Order order = new Sort.Order(facetSort.getDirection(), ((SearchFacet) facetSort.getFacet()).getQuery());
                    if (sort == null)
                        sort = new Sort(order);
                    else
                        sort.and(new Sort(order));
                }
            }
        }
        return sort;
    }

}
