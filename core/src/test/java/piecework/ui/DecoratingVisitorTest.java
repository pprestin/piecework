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
package piecework.ui;

import org.apache.commons.io.IOUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.SimpleHtmlSerializer;
import org.htmlcleaner.TagNode;
import org.junit.Assert;
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
        ClassPathResource inputResource = new ClassPathResource("piecework/ui/DecoratingVisitorTest.input.html");
        TagNode node = cleaner.clean(inputResource.getInputStream());
        node.traverse(new DecoratingVisitor(form));
        SimpleHtmlSerializer serializer = new SimpleHtmlSerializer(cleaner.getProperties());
//        serializer.writeToStream(node, System.out);

        ClassPathResource outputResource = new ClassPathResource("piecework/ui/DecoratingVisitorTest.output.html");
        Assert.assertEquals(IOUtils.toString(outputResource.getInputStream()), serializer.getAsString(node));
    }

    @Test
    public void testVisitWithWizardTemplate() throws Exception {
        Form form = ExampleFactory.exampleFormWithWizardTemplate();
        ClassPathResource inputResource = new ClassPathResource("piecework/ui/DecoratingVisitorTest.input.html");
        TagNode node = cleaner.clean(inputResource.getInputStream());
        node.traverse(new DecoratingVisitor(form));
        SimpleHtmlSerializer serializer = new SimpleHtmlSerializer(cleaner.getProperties());
//        serializer.writeToStream(node, System.out);

        ClassPathResource outputResource = new ClassPathResource("piecework/ui/DecoratingVisitorTest.template.output.html");
        Assert.assertEquals(IOUtils.toString(outputResource.getInputStream()), serializer.getAsString(node));
    }

}
