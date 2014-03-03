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
package piecework.service;

import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import piecework.identity.IdentityHelper;
import piecework.model.SearchResults;
import piecework.repository.ContentRepository;
import piecework.settings.UserInterfaceSettings;
import piecework.ui.CustomJaxbJsonProvider;

import java.io.BufferedInputStream;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class UserInterfaceServiceTest {

    @InjectMocks
    UserInterfaceService userInterfaceService;

    @Mock
    ContentRepository contentRepository;

    @Mock
    CacheService cacheService;

    @Mock
    FormTemplateService formTemplateService;

    @Mock
    IdentityHelper helper;

    @Mock
    CustomJaxbJsonProvider jsonProvider;

    @Mock
    UserInterfaceSettings settings;

    @Test
    public void verifySearchResultsHasPage() {
        Assert.assertTrue(userInterfaceService.hasPage(SearchResults.class));
    }

    @Test
    public void verifyInputStreamDoesNotHavePage() {
        Assert.assertFalse(userInterfaceService.hasPage(BufferedInputStream.class));
    }

}
