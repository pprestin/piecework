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
package piecework.form.response;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.SimpleHtmlSerializer;
import org.htmlcleaner.TagNode;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import piecework.model.Form;
import piecework.test.ExampleFactory;

/**
 * @author James Renfro
 */
public class DecoratingVisitorTest {

    private HtmlCleaner cleaner = new HtmlCleaner();

    @Test
    public void testVisit() throws Exception {
        Form form = ExampleFactory.exampleForm();
        ClassPathResource resource = new ClassPathResource("piecework/form/resources/DecoratingVisitorTest.input.html");
        TagNode node = cleaner.clean(resource.getInputStream());
        node.traverse(new DecoratingVisitor(form));
        SimpleHtmlSerializer serializer = new SimpleHtmlSerializer(cleaner.getProperties());
        serializer.writeToStream(node, System.out);
    }

}
