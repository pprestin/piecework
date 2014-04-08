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
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.apache.commons.lang.StringUtils;
import org.bson.BSON;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import piecework.Constants;
import piecework.common.SearchCriteria;
import piecework.model.SearchFacet;
import piecework.security.Sanitizer;
import piecework.util.SearchUtility;

import java.util.*;
import java.util.regex.Pattern;

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

        if (StringUtils.isNotEmpty(searchCriteria.getProcessInstanceId()))
            query.addCriteria(where("processInstanceId").is(searchCriteria.getProcessInstanceId()));

        Map<SearchFacet, Object> facetParameters = searchCriteria.getFacetParameters();
        if (facetParameters != null && !facetParameters.isEmpty()) {
            for (Map.Entry<SearchFacet, Object> entry : facetParameters.entrySet()) {
                if (entry.getKey() == null)
                    continue;
                if (entry.getValue() == null)
                    continue;

                query.addCriteria(entry.getKey().criteria(entry.getValue()));
            }
        }

        if (StringUtils.isNotBlank(searchCriteria.getProcessStatus())) {
            if (!searchCriteria.getProcessStatus().equalsIgnoreCase("all"))
                query.addCriteria(where("processStatus").is(searchCriteria.getProcessStatus()));
        }

        if (StringUtils.isNotEmpty(searchCriteria.getInitiatedBy()))
            query.addCriteria(where("initiatorId").is(searchCriteria.getInitiatedBy()));

        if (!searchCriteria.getKeywords().isEmpty()) {
            List<String> tokens = new ArrayList<String>();
            for (String keyword : searchCriteria.getKeywords()) {
                String resultString = keyword.replaceAll("[^\\p{L}\\p{Nd}]\\-", ",");
                String[] components = resultString.split(",");
                if (components != null && components.length > 0)
                    tokens.addAll(Arrays.asList(components));
            }

            int count = 0;
            Criteria criteria = where("keywords");
            List<Pattern> dbObjects = new ArrayList<Pattern>();
            for (String keyword : tokens) {
                dbObjects.add(Pattern.compile(keyword, 0));
                count++;
            }
            if (count > 0)
                query.addCriteria(criteria.all(dbObjects));
        }

        return query;
    }

}
