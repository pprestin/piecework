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

import org.junit.Assert;
import org.junit.Test;
import piecework.common.FacetFactory;
import piecework.common.ManyMap;
import piecework.common.SearchCriteria;
import piecework.model.Process;
import piecework.security.concrete.PassthroughSanitizer;

import java.util.Collections;
import java.util.Date;

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
    public void startedAfterQuery() {
        ManyMap<String, String> queryParameters = new ManyMap<String, String>();
        queryParameters.putOne("startedAfter", "2014-02-11T05:35:26.608Z");

        SearchCriteria criteria =
                new SearchCriteria.Builder(queryParameters, Collections.<Process>emptySet(), FacetFactory.facetMap(Collections.<Process>emptySet()), new PassthroughSanitizer())
                        .build();

        Date date = criteria.getStartedAfter();
        Assert.assertEquals(1392096926608l, date.getTime());
    }

    @Test
    public void startedBeforeQuery() {
        ManyMap<String, String> queryParameters = new ManyMap<String, String>();
        queryParameters.putOne("startedBefore", "2014-02-11T05:35:26.608Z");

        SearchCriteria criteria =
                new SearchCriteria.Builder(queryParameters, Collections.<Process>emptySet(), FacetFactory.facetMap(Collections.<Process>emptySet()), new PassthroughSanitizer())
                        .build();

        Date date = criteria.getStartedBefore();
        Assert.assertEquals(1392096926608l, date.getTime());
    }

}
