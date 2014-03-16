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

import org.junit.Assert;
import org.junit.Test;
import piecework.common.FacetFactory;
import piecework.common.ManyMap;
import piecework.common.SearchCriteria;
import piecework.model.DataFilterFacet;
import piecework.model.Facet;
import piecework.model.Process;
import piecework.model.SearchFacet;
import piecework.security.concrete.PassthroughSanitizer;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * @author James Renfro
 */
public class SearchCriteriaTest {

    @Test
    public void limitToProcessDefinitionKeys() {
        SearchCriteria criteria = new SearchCriteria.Builder()
                .processDefinitionKey("TEST-1")
                .processDefinitionKey("TEST-2")
                .build();

        Assert.assertEquals(2, criteria.getProcessDefinitionKeys().size());
    }

    @Test
    public void limitToProcessDefinitionKeysQuery() {
        ManyMap<String, String> queryParameters = new ManyMap<String, String>();
        queryParameters.putOne("processDefinitionKey", "TEST-1");
        queryParameters.putOne("processDefinitionKey", "TEST-2");
        SearchCriteria criteria =
                new SearchCriteria.Builder(queryParameters, Collections.<Process>emptySet(), FacetFactory.facetMap(Collections.<Process>emptySet()), new PassthroughSanitizer())
                    .processDefinitionKey("TEST-1")
                    .processDefinitionKey("TEST-3")
                    .build();

        Assert.assertEquals(1, criteria.getProcessDefinitionKeys().size());
    }

    @Test
    public void unmatchedQuery() {
        ManyMap<String, String> queryParameters = new ManyMap<String, String>();
        queryParameters.putOne("xyz123", "Some value");

        Map<String, Facet> facetMap = FacetFactory.facetMap(Collections.<Process>emptySet());
        SearchCriteria criteria =
                new SearchCriteria.Builder(queryParameters, Collections.<Process>emptySet(), facetMap, new PassthroughSanitizer())
                        .build();

        Map<SearchFacet, Object> facetParameters = criteria.getFacetParameters();
        Assert.assertEquals(0, facetParameters.size());
    }

    @Test
    public void processInstanceLabelQuery() {
        ManyMap<String, String> queryParameters = new ManyMap<String, String>();
        queryParameters.putOne("processInstanceLabel", "Some label");

        Map<String, Facet> facetMap = FacetFactory.facetMap(Collections.<Process>emptySet());
        SearchCriteria criteria =
                new SearchCriteria.Builder(queryParameters, Collections.<Process>emptySet(), facetMap, new PassthroughSanitizer())
                        .build();

        Map<SearchFacet, Object> facetParameters = criteria.getFacetParameters();
        Assert.assertEquals(1, facetParameters.size());
        String label = String.class.cast(facetParameters.values().iterator().next());
        Assert.assertEquals("Some label", label);
    }

    @Test
    public void taskStatusQuery() {
        ManyMap<String, String> queryParameters = new ManyMap<String, String>();
        queryParameters.putOne("taskStatus", "Open");

        Map<String, Facet> facetMap = FacetFactory.facetMap(Collections.<Process>emptySet());
        SearchCriteria criteria =
                new SearchCriteria.Builder(queryParameters, Collections.<Process>emptySet(), facetMap, new PassthroughSanitizer())
                        .build();

        Map<SearchFacet, Object> facetParameters = criteria.getFacetParameters();
        Assert.assertEquals(0, facetParameters.size());
        Map<DataFilterFacet, String> filterFacetParameters = criteria.getFilterFacetParameters();
        Assert.assertEquals(1, filterFacetParameters.size());
        String label = String.class.cast(filterFacetParameters.values().iterator().next());
        Assert.assertEquals("Open", label);
        DataFilterFacet facet = filterFacetParameters.keySet().iterator().next();
        Assert.assertEquals("taskStatus", facet.getName());
    }

    @Test
    public void taskLabelQuery() {
        ManyMap<String, String> queryParameters = new ManyMap<String, String>();
        queryParameters.putOne("taskLabel", "Review Something");

        Map<String, Facet> facetMap = FacetFactory.facetMap(Collections.<Process>emptySet());
        SearchCriteria criteria =
                new SearchCriteria.Builder(queryParameters, Collections.<Process>emptySet(), facetMap, new PassthroughSanitizer())
                        .build();

        Map<SearchFacet, Object> facetParameters = criteria.getFacetParameters();
        Assert.assertEquals(0, facetParameters.size());
        Map<DataFilterFacet, String> filterFacetParameters = criteria.getFilterFacetParameters();
        Assert.assertEquals(1, filterFacetParameters.size());
        String label = String.class.cast(filterFacetParameters.values().iterator().next());
        Assert.assertEquals("Review Something", label);
        DataFilterFacet facet = filterFacetParameters.keySet().iterator().next();
        Assert.assertEquals("taskLabel", facet.getName());
    }

    @Test
    public void assigneeQuery() {
        ManyMap<String, String> queryParameters = new ManyMap<String, String>();
        queryParameters.putOne("assignee", "Joe Tester");

        Map<String, Facet> facetMap = FacetFactory.facetMap(Collections.<Process>emptySet());
        SearchCriteria criteria =
                new SearchCriteria.Builder(queryParameters, Collections.<Process>emptySet(), facetMap, new PassthroughSanitizer())
                        .build();

        Map<SearchFacet, Object> facetParameters = criteria.getFacetParameters();
        Assert.assertEquals(0, facetParameters.size());
        Map<DataFilterFacet, String> filterFacetParameters = criteria.getFilterFacetParameters();
        Assert.assertEquals(1, filterFacetParameters.size());
        String label = String.class.cast(filterFacetParameters.values().iterator().next());
        Assert.assertEquals("Joe Tester", label);
        DataFilterFacet facet = filterFacetParameters.keySet().iterator().next();
        Assert.assertEquals("assignee", facet.getName());
    }

    @Test
    public void startedAfterQuery() {
        ManyMap<String, String> queryParameters = new ManyMap<String, String>();
        queryParameters.putOne("startTimeAfter", "2014-02-11T05:35:26.608Z");

        Map<String, Facet> facetMap = FacetFactory.facetMap(Collections.<Process>emptySet());
        SearchCriteria criteria =
                new SearchCriteria.Builder(queryParameters, Collections.<Process>emptySet(), facetMap, new PassthroughSanitizer())
                        .build();

        Map<SearchFacet, Object> facetParameters = criteria.getFacetParameters();
        Assert.assertEquals(1, facetParameters.size());
        DateRange dateRange = DateRange.class.cast(facetParameters.values().iterator().next());
        Assert.assertEquals("2014-02-11T05:35:26.608Z", dateRange.getAfter());
        Assert.assertNull(dateRange.getBefore());
    }

    @Test
    public void startedBeforeQuery() {
        ManyMap<String, String> queryParameters = new ManyMap<String, String>();
        queryParameters.putOne("startTimeBefore", "2014-02-15T05:35:26.608Z");

        Map<String, Facet> facetMap = FacetFactory.facetMap(Collections.<Process>emptySet());
        SearchCriteria criteria =
                new SearchCriteria.Builder(queryParameters, Collections.<Process>emptySet(), facetMap, new PassthroughSanitizer())
                        .build();

        Map<SearchFacet, Object> facetParameters = criteria.getFacetParameters();
        Assert.assertEquals(1, facetParameters.size());
        DateRange dateRange = DateRange.class.cast(facetParameters.values().iterator().next());
        Assert.assertEquals("2014-02-15T05:35:26.608Z", dateRange.getBefore());
        Assert.assertNull(dateRange.getAfter());
    }

    @Test
    public void endTimeQuery() {
        ManyMap<String, String> queryParameters = new ManyMap<String, String>();
        queryParameters.putOne("endTimeAfter", "2014-01-15T05:35:26.608Z");
        queryParameters.putOne("endTimeBefore", "2014-03-10T05:35:26.608Z");

        Map<String, Facet> facetMap = FacetFactory.facetMap(Collections.<Process>emptySet());
        SearchCriteria criteria =
                new SearchCriteria.Builder(queryParameters, Collections.<Process>emptySet(), facetMap, new PassthroughSanitizer())
                        .build();

        Map<SearchFacet, Object> facetParameters = criteria.getFacetParameters();
        Assert.assertEquals(1, facetParameters.size());
        DateRange dateRange = DateRange.class.cast(facetParameters.values().iterator().next());
        Assert.assertEquals("2014-03-10T05:35:26.608Z", dateRange.getBefore());
        Assert.assertEquals("2014-01-15T05:35:26.608Z", dateRange.getAfter());
    }

    @Test
    public void lastModifiedQuery() {
        ManyMap<String, String> queryParameters = new ManyMap<String, String>();
        queryParameters.putOne("lastModifiedTimeAfter", "2014-01-15T05:35:26.608Z");
        queryParameters.putOne("lastModifiedTimeBefore", "2014-03-10T05:35:26.608Z");

        Map<String, Facet> facetMap = FacetFactory.facetMap(Collections.<Process>emptySet());
        SearchCriteria criteria =
                new SearchCriteria.Builder(queryParameters, Collections.<Process>emptySet(), facetMap, new PassthroughSanitizer())
                        .build();

        Map<SearchFacet, Object> facetParameters = criteria.getFacetParameters();
        Assert.assertEquals(1, facetParameters.size());
        DateRange dateRange = DateRange.class.cast(facetParameters.values().iterator().next());
        Assert.assertEquals("2014-03-10T05:35:26.608Z", dateRange.getBefore());
        Assert.assertEquals("2014-01-15T05:35:26.608Z", dateRange.getAfter());
    }

}
