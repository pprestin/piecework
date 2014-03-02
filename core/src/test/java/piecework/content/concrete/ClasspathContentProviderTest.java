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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import piecework.enumeration.Scheme;
import piecework.exception.ForbiddenError;
import piecework.exception.PieceworkException;
import piecework.content.ContentResource;
import piecework.model.ContentProfile;
import piecework.persistence.ContentProfileProvider;
import piecework.persistence.test.ProcessDeploymentProviderStub;
import piecework.security.AccessTracker;

/**
 * @author James Renfro
 */
public class ClasspathContentProviderTest {

    private ClasspathContentProvider contentProvider = new ClasspathContentProvider();
    private ContentProfileProvider modelProvider;

    @Before
    public void setup() {
        ContentProfile contentProfile = new ContentProfile.Builder()
                .baseClasspath("META-INF/piecework/")
                .build();
        this.modelProvider = new ProcessDeploymentProviderStub(contentProfile);
        this.contentProvider.accessTracker = Mockito.mock(AccessTracker.class);
    }

    @Test
    public void happyPathSuccess() throws PieceworkException {
        ContentResource contentResource = contentProvider.findByLocation(modelProvider, "classpath:META-INF/piecework/default.properties");
        Assert.assertEquals("classpath:META-INF/piecework/default.properties", contentResource.getLocation());
    }

    @Test
    public void relativePathSuccess() throws PieceworkException {
        ContentResource contentResource = contentProvider.findByLocation(modelProvider, "classpath:META-INF/piecework/some/../default.properties");
        Assert.assertEquals("classpath:META-INF/piecework/default.properties", contentResource.getLocation());
    }

    @Test
    public void happyPathFailure() throws PieceworkException {
        ContentResource contentResource = contentProvider.findByLocation(modelProvider, "default.properties");
        Assert.assertNull(contentResource);
    }

    @Test(expected = ForbiddenError.class)
    public void relativePathForbidden() throws PieceworkException {
        contentProvider.findByLocation(modelProvider, "classpath:../../piecework/StatusCodeError.properties");
    }

    @Test
    public void verifyKey() {
        Assert.assertEquals("default-classpath", contentProvider.getKey());
    }

    @Test
    public void verifyScheme() {
        Assert.assertEquals(Scheme.CLASSPATH, contentProvider.getScheme());
    }

}
