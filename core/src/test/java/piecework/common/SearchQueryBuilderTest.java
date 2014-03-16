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
import org.springframework.data.mongodb.core.query.Query;
import piecework.model.*;
import piecework.model.Process;
import piecework.security.concrete.PassthroughSanitizer;

import java.util.Collections;
import java.util.Map;

/**
 * @author James Renfro
 */
public class SearchQueryBuilderTest {

    @Test
    public void processInstanceLabelQuery() {
        ManyMap<String, String> queryParameters = new ManyMap<String, String>();
        queryParameters.putOne("processInstanceLabel", "Some label");

        Map<String, Facet> facetMap = FacetFactory.facetMap(Collections.<piecework.model.Process>emptySet());
        SearchCriteria criteria =
                new SearchCriteria.Builder(queryParameters, Collections.<Process>emptySet(), facetMap, new PassthroughSanitizer())
                        .build();

        SearchQueryBuilder builder = new SearchQueryBuilder(criteria);
        Query query = builder.build(Collections.singleton("TEST"), new PassthroughSanitizer());

        Assert.assertEquals("{ \"processDefinitionKey\" : { \"$in\" : [ \"TEST\"]} , \"processInstanceLabel\" : { \"$all\" : [ { \"$regex\" : \"Some\" , \"$options\" : \"i\"} , { \"$regex\" : \"label\" , \"$options\" : \"i\"}]}}", query.getQueryObject().toString());
    }

    @Test
    public void keywordsQuery() {
        ManyMap<String, String> queryParameters = new ManyMap<String, String>();
        queryParameters.putOne("keyword", "33-4444");

        Map<String, Facet> facetMap = FacetFactory.facetMap(Collections.<piecework.model.Process>emptySet());
        SearchCriteria criteria =
                new SearchCriteria.Builder(queryParameters, Collections.<Process>emptySet(), facetMap, new PassthroughSanitizer())
                        .build();

        SearchQueryBuilder builder = new SearchQueryBuilder(criteria);
        Query query = builder.build(Collections.singleton("TEST"), new PassthroughSanitizer());

        Assert.assertEquals("{ \"processDefinitionKey\" : { \"$in\" : [ \"TEST\"]} , \"keywords\" : { \"$all\" : [ { \"$regex\" : \"33-4444\"}]}}", query.getQueryObject().toString());
    }

    @Test
    public void keywordsWithPageQuery() {
        ManyMap<String, String> queryParameters = new ManyMap<String, String>();
        queryParameters.putOne("keyword", "33-4444");
        queryParameters.putOne("page", "2");

        Map<String, Facet> facetMap = FacetFactory.facetMap(Collections.<piecework.model.Process>emptySet());
        SearchCriteria criteria =
                new SearchCriteria.Builder(queryParameters, Collections.<Process>emptySet(), facetMap, new PassthroughSanitizer())
                        .build();

        SearchQueryBuilder builder = new SearchQueryBuilder(criteria);
        Query query = builder.build(Collections.singleton("TEST"), new PassthroughSanitizer());

//        Assert.assertEquals(200, query.getSkip());
//        Assert.assertEquals(100, query.getLimit());
        Assert.assertEquals("{ \"processDefinitionKey\" : { \"$in\" : [ \"TEST\"]} , \"keywords\" : { \"$all\" : [ { \"$regex\" : \"33-4444\"}]}}", query.getQueryObject().toString());
    }

    @Test
    public void keywordsWithPageSizeQuery() {
        ManyMap<String, String> queryParameters = new ManyMap<String, String>();
        queryParameters.putOne("keyword", "33-4444");
        queryParameters.putOne("page", "3");
        queryParameters.putOne("pageSize", "15");

        Map<String, Facet> facetMap = FacetFactory.facetMap(Collections.<piecework.model.Process>emptySet());
        SearchCriteria criteria =
                new SearchCriteria.Builder(queryParameters, Collections.<Process>emptySet(), facetMap, new PassthroughSanitizer())
                        .build();

        SearchQueryBuilder builder = new SearchQueryBuilder(criteria);
        Query query = builder.build(Collections.singleton("TEST"), new PassthroughSanitizer());

//        Assert.assertEquals(45, query.getSkip());
//        Assert.assertEquals(15, query.getLimit());
        Assert.assertEquals("{ \"processDefinitionKey\" : { \"$in\" : [ \"TEST\"]} , \"keywords\" : { \"$all\" : [ { \"$regex\" : \"33-4444\"}]}}", query.getQueryObject().toString());
    }

    @Test
    public void initiatorIdQuery() {
        ManyMap<String, String> queryParameters = new ManyMap<String, String>();
        queryParameters.putOne("initiatedBy", "123");

        Map<String, Facet> facetMap = FacetFactory.facetMap(Collections.<piecework.model.Process>emptySet());
        SearchCriteria criteria =
                new SearchCriteria.Builder(queryParameters, Collections.<Process>emptySet(), facetMap, new PassthroughSanitizer())
                        .build();

        SearchQueryBuilder builder = new SearchQueryBuilder(criteria);
        Query query = builder.build(Collections.singleton("TEST"), new PassthroughSanitizer());

//        Assert.assertEquals(0, query.getSkip());
//        Assert.assertEquals(100, query.getLimit());
        Assert.assertEquals("{ \"processDefinitionKey\" : { \"$in\" : [ \"TEST\"]} , \"initiatorId\" : \"123\"}", query.getQueryObject().toString());
    }

}
