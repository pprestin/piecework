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
import org.junit.Test;
import piecework.model.Content;

/**
 * @author James Renfro
 */
public class ClasspathContentProviderTest {

    private ClasspathContentProvider contentProvider = new ClasspathContentProvider();

    @Test
    public void happyPathSuccess() {
        Content content = contentProvider.findByPath(null, "classpath:META-INF/piecework", "default.properties");
        Assert.assertEquals("classpath:META-INF/piecework/default.properties", content.getLocation());
    }

    @Test
    public void relativePathSuccess() {
        Content content = contentProvider.findByPath(null, "classpath:nothing/../META-INF/piecework", "default.properties");
        Assert.assertEquals("classpath:META-INF/piecework/default.properties", content.getLocation());
    }

    @Test
    public void happyPathFailure() {
        Content content = contentProvider.findByPath(null, "classpath:META-INF/does/not/exist", "default.properties");
        Assert.assertNull(content);
    }

    @Test
    public void relativePathFailure() {
        Content content = contentProvider.findByPath(null, "classpath:META-INF/piecework", "../../piecework/StatusCodeError.properties");
        Assert.assertNull(content);
    }

}
