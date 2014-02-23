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
import piecework.model.Content;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author James Renfro
 */
public class RemoteContentProviderTest {

    private RemoteContentProvider contentProvider = new RemoteContentProvider();

    @Before
    public void setup() {
        contentProvider.init();
    }

    @After
    public void shutdown() {
        contentProvider.destroy();
    }

    @Test
    public void retrieveContentHappyPath() throws IOException {
        Content content = contentProvider.findByPath(null, "https://raw.github.com/", "piecework/piecework/master/README.md", null);
        Assert.assertEquals("README.md", content.getFilename());
        Assert.assertEquals("https://raw.github.com/piecework/piecework/master/README.md", content.getContentId());
        Assert.assertEquals("text/plain; charset=utf-8", content.getContentType());
        byte[] array = null;
        InputStream input = content.getInputStream();
        try {
            array = IOUtils.toByteArray(input);
        } finally {
            IOUtils.closeQuietly(input);
        }
        Assert.assertNotNull(array);
        Assert.assertTrue(array.length > 0);
    }

}
