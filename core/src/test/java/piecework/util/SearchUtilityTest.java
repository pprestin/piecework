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

import junit.framework.Assert;
import org.junit.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import piecework.common.SearchCriteria;
import piecework.common.SearchQueryParameters;
import piecework.security.concrete.PassthroughSanitizer;

import java.util.Collections;

/**
 * @author James Renfro
 */
public class SearchUtilityTest {

    @Test
    public void verifySort() {
        SearchCriteria criteria = new SearchCriteria.Builder()
                .direction("asc")
                .sortBy("processInstanceLabel")
                .build();

        Sort sort = SearchUtility.sort(criteria, new PassthroughSanitizer());
        Assert.assertEquals("processInstanceLabel: ASC", sort.toString());
    }

    @Test
    public void verifySortDesc() {
        SearchCriteria criteria = new SearchCriteria.Builder()
                .direction("desc")
                .sortBy("startTime")
                .build();

        Sort sort = SearchUtility.sort(criteria, new PassthroughSanitizer());
        Assert.assertEquals("startTime: DESC", sort.toString());
    }

    @Test
    public void verifyPageable() {
        SearchCriteria criteria = new SearchCriteria.Builder()
                .direction("desc")
                .sortBy("startTime")
                .firstResult(10)
                .maxResults(30)
                .build();

        Pageable pageable = SearchUtility.pageable(criteria, new PassthroughSanitizer());
        Assert.assertEquals(300, pageable.getOffset());
        Assert.assertEquals(30, pageable.getPageSize());
        Assert.assertEquals("startTime: DESC", pageable.getSort().toString());
    }

}
