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
package piecework.persistence.concrete;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import piecework.common.ViewContext;
import piecework.exception.NotFoundError;
import piecework.model.ContentProfile;
import piecework.model.Entity;
import piecework.model.User;
import piecework.persistence.ContentProfileProvider;
import piecework.settings.ContentSettings;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class SystemContentProfileProviderTest {

    @Mock
    ContentSettings settings;

    @Mock
    User principal;

    private ContentProfileProvider profileProvider;

    @Before
    public void setup() {
        Mockito.doReturn("/etc/some/path")
               .when(settings).getApplicationFilesystemRoot();

        this.profileProvider = new SystemContentProfileProvider(settings, principal);
    }

    @Test
    public void verifyProfile() throws Exception {
        ContentProfile contentProfile = profileProvider.contentProfile();
        Assert.assertEquals("/etc/some/path", contentProfile.getBaseDirectory());
    }

    @Test(expected = NotFoundError.class)
    public void verifyNotFoundProcess() throws Exception {
        Assert.assertNull(profileProvider.process());
    }

    @Test(expected = NotFoundError.class)
    public void verifyNotFoundProcessWithContext() throws Exception {
        Assert.assertNull(profileProvider.process(new ViewContext()));
    }

    @Test
    public void verifyNullProcessDefinitionKey() throws Exception {
        Assert.assertNull(profileProvider.processDefinitionKey());
    }

    @Test
    public void verifyPrincipal() throws Exception {
        Entity entity = profileProvider.principal();
        Assert.assertEquals(principal, entity);
    }

}
