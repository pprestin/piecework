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
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import piecework.common.FacetSort;
import piecework.common.SearchCriteria;
import piecework.model.SearchFacet;
import piecework.security.concrete.PassthroughSanitizer;

/**
 * @author James Renfro
 */
@Ignore
public class SearchUtilityTest {

    @Test
    public void verifySort() {
        SearchCriteria criteria = new SearchCriteria.Builder()
                .sortBy(new FacetSort(new SearchFacet("processInstanceLabel", "processInstanceLabel", "Label", false), "asc"))
                .build();

        Sort sort = SearchUtility.sort(criteria, new PassthroughSanitizer());
        Assert.assertEquals("processInstanceLabel: ASC", sort.toString());
    }

    @Test
    public void verifySortDesc() {
        SearchCriteria criteria = new SearchCriteria.Builder()
                .sortBy(new FacetSort(new SearchFacet("startTime", "startTime", "Start Time", "date", true), "desc"))
                .build();

        Sort sort = SearchUtility.sort(criteria, new PassthroughSanitizer());
        Assert.assertEquals("startTime: DESC", sort.toString());
    }

    @Test
    public void verifyPageable() {
        SearchCriteria criteria = new SearchCriteria.Builder()
                .sortBy(new FacetSort(new SearchFacet("startTime", "startTime", "Start Time", "date", true), "desc"))
                .page(2)
                .pageSize(30)
                .build();

        Pageable pageable = SearchUtility.pageable(criteria, new PassthroughSanitizer());
        Assert.assertEquals(60, pageable.getOffset());
        Assert.assertEquals(30, pageable.getPageSize());
        Assert.assertEquals("startTime: DESC", pageable.getSort().toString());
    }

}
