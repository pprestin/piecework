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
package piecework.form.handler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import piecework.model.Field;
import piecework.model.Screen;
import piecework.ui.StreamingPageContent;
import piecework.model.Content;
import piecework.model.FormRequest;
import piecework.persistence.ContentRepository;
import piecework.test.ExampleFactory;
import piecework.test.config.UnitTestConfiguration;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * @author James Renfro
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={UnitTestConfiguration.class})
@ActiveProfiles("test")
public class ResponseHandlerTest {

    private static final String HTML = "<html><body><form><input name=\"employeeName\"></body></html>";
    private static final String OUTPUT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<html><head></head><body><form enctype=\"multipart/form-data\" action=\"null\" method=\"POST\"><input name=\"employeeName\" /><input name=\"PROCESS_FORM_SUBMISSION_TOKEN\" value=\"0b82440e-0c3c-4433-b629-c41e68049b8b\" type=\"hidden\" /></form></body></html>";

    @Autowired
    ResponseHandler responseHandler;

    @Autowired
    ContentRepository contentRepository;

    @Before
    public void setup() {
        String location = ExampleFactory.exampleFormRequest("0b82440e-0c3c-4433-b629-c41e68049b8b").getScreen().getLocation();
        Content content = new Content.Builder()
                .location(location)
                .inputStream(new ByteArrayInputStream(HTML.getBytes()))
                .build();
        contentRepository.save(content);
    }

//    @Test
//    public void testHandle() throws Exception {
//        FormRequest formRequest = ExampleFactory.exampleFormRequest("0b82440e-0c3c-4433-b629-c41e68049b8b");
//        Response response = responseHandler.handle(formRequest, null, null);
//
//        OutputStream outputStream = new ByteArrayOutputStream();
//        StreamingPageContent pageContent = StreamingPageContent.class.cast(response.getEntity());
//        pageContent.write(outputStream);
//
//        Assert.assertEquals(OUTPUT, outputStream.toString());
//
//    }

    @Test
    public void testBuildScreen() throws Exception {
        FormRequest formRequest = ExampleFactory.exampleFormRequest("0b82440e-0c3c-4433-b629-c41e68049b8b");

        Screen screen = responseHandler.buildScreen(formRequest, null, null);

        Field supervisorIdField = screen.getSections().get(1).getFields().get(0);

        Assert.assertTrue(supervisorIdField.isVisible());
    }

}
