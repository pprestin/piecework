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

import org.apache.commons.io.IOUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.SimpleHtmlSerializer;
import org.htmlcleaner.TagNode;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import piecework.model.Button;
import piecework.model.Field;
import piecework.model.Screen;
import piecework.model.Section;

import java.util.List;

/**
 * @author James Renfro
 */
public class ScreenBuildingVisitorTest {

    private HtmlCleaner cleaner = new HtmlCleaner();

    @Test
    public void testVisit() throws Exception {
        ScreenBuildingVisitor visitor = new ScreenBuildingVisitor();

        ClassPathResource inputResource = new ClassPathResource("piecework/form/response/ScreenBuildingVisitorTest.input.html");
        TagNode node = cleaner.clean(inputResource.getInputStream());
        node.traverse(visitor);

        Screen screen = visitor.build();

        List<Section> sections = screen.getSections();
        Assert.assertEquals(2, sections.size());

        Section section1 = sections.get(0);
        Section section2 = sections.get(1);

        List<Field> sections1Fields = section1.getFields();
        List<Button> section1Buttons = section1.getButtons();

        Assert.assertEquals(2, sections1Fields.size());
        Assert.assertEquals(1, section1Buttons.size());

        Field employeeIdField = sections1Fields.get(0);
        Field budgetNumberField = sections1Fields.get(1);

        Assert.assertTrue(employeeIdField.isRequired());
        Assert.assertFalse(employeeIdField.isRestricted());

        Assert.assertFalse(budgetNumberField.isRequired());
        Assert.assertTrue(budgetNumberField.isRestricted());


        List<Field> sections2Fields = section2.getFields();
        List<Button> section2Buttons = section2.getButtons();

        Assert.assertEquals(6, sections2Fields.size());
        Assert.assertEquals(1, section2Buttons.size());
    }


}
