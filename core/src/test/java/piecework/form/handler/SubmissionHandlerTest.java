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

import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import piecework.common.Payload;
import piecework.form.validation.SubmissionTemplate;
import piecework.form.validation.SubmissionTemplateFactory;
import piecework.test.config.UnitTestConfiguration;
import piecework.model.*;
import piecework.test.ExampleFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import java.util.List;
import java.util.UUID;

/**
 * @author James Renfro
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={UnitTestConfiguration.class})
@ActiveProfiles("test")
public class SubmissionHandlerTest {

    @Autowired
    SubmissionHandler submissionHandler;

    @Autowired
    SubmissionTemplateFactory submissionTemplateFactory;

    HttpServletRequest servletRequest;
    piecework.model.Process process;
    String processInstanceId;

    @Before
    public void setUp() throws Exception {
        this.servletRequest = Mockito.mock(HttpServletRequest.class);
        this.process = ExampleFactory.exampleProcess();
        this.processInstanceId = "123";
    }

    @Test
    public void testHandleProcessInstanceObject() throws Exception {
        MultivaluedMap<String, String> map = new MetadataMap<String, String>();

        map.putSingle("TestField", "1");
        map.putSingle("TestField", "2");
        map.putSingle("TestField", "3");

        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process);
        Submission actual = submissionHandler.handle(process, template, map);
        Assert.assertNotNull(actual);

        FormValue formValue = actual.getFormValueMap().get("TestField");
        List<String> values = formValue.getValues();
        Assert.assertEquals("1", values.get(0));
        Assert.assertEquals("2", values.get(1));
        Assert.assertEquals("3", values.get(2));
    }

}
