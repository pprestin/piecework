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
package piecework.content.concrete;

import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import piecework.content.config.ContentConfiguration;
import piecework.enumeration.Scheme;
import piecework.exception.PieceworkException;
import piecework.model.Content;
import piecework.model.ContentProfile;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.persistence.test.ProcessDeploymentProviderStub;
import piecework.security.AccessTracker;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

/**
 * @author James Renfro
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={ContentConfiguration.class})
public class RemoteContentProviderTest {

    private RemoteContentProvider contentProvider = new RemoteContentProvider();

    @Before
    public void setup() {
        contentProvider.init();
        contentProvider.accessTracker = Mockito.mock(AccessTracker.class);
    }

    @After
    public void shutdown() {
        contentProvider.destroy();
    }

    @Test
    public void retrieveContentHappyPath() throws PieceworkException, IOException {
        ContentProfile contentProfile = new ContentProfile.Builder()
                .remoteResourceLocations(Collections.singleton("http://localhost:10001/external/some/resource"))
                .build();
        ProcessDeploymentProvider modelProvider = new ProcessDeploymentProviderStub(contentProfile);
        Content content = contentProvider.findByLocation(modelProvider, "http://localhost:10001/external/some/resource");
        Assert.assertEquals("resource", content.getFilename());
        Assert.assertEquals("http://localhost:10001/external/some/resource", content.getContentId());
        Assert.assertEquals("text/plain;charset=UTF-8", content.getContentType());
        byte[] array = null;
        InputStream input = content.getInputStream();
        String expected = "This is some data from an external server";
        String actual = IOUtils.toString(input);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void verifyKey() {
        Assert.assertEquals("default-remote", contentProvider.getKey());
    }

    @Test
    public void verifyScheme() {
        Assert.assertEquals(Scheme.REMOTE, contentProvider.getScheme());
    }

}
