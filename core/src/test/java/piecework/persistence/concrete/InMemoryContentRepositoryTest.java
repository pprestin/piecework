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

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import piecework.model.Content;
import piecework.persistence.concrete.InMemoryContentRepository;
import piecework.test.ExampleFactory;

import java.util.List;

/**
 * @author James Renfro
 */
public class InMemoryContentRepositoryTest {

    InMemoryContentRepository contentRepository;

    @Before
    public void setup() {
        this.contentRepository = new InMemoryContentRepository();
    }

    @Test
    public void testFindByLocation() throws Exception {
        Content expected = ExampleFactory.exampleContent();

        Content content = contentRepository.save(expected);
        Assert.assertNotNull(content.getContentId());

        Content stored = contentRepository.findByLocation(expected.getLocation());

        Assert.assertEquals(14, stored.getLength().longValue());
        Assert.assertNotNull(stored.getLastModified());
        Assert.assertEquals(expected.getContentType(), stored.getContentType());
    }

    @Test
    public void testFindByLocationPattern() throws Exception {
        Content expected = ExampleFactory.exampleContent();

        Content content = contentRepository.save(expected);
        Assert.assertNotNull(content.getContentId());

        List<Content> results = contentRepository.findByLocationPattern("/test/.+");

        Assert.assertEquals(1, results.size());
        Content stored = results.get(0);
        Assert.assertEquals(14, stored.getLength().longValue());
        Assert.assertNotNull(stored.getLastModified());
        Assert.assertEquals(expected.getContentType(), stored.getContentType());
    }

}
