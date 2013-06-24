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
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import piecework.Constants;
import piecework.common.RequestDetails;
import piecework.test.config.UnitTestConfiguration;
import piecework.exception.ForbiddenError;
import piecework.model.FormRequest;
import piecework.model.Interaction;
import piecework.model.Process;
import piecework.model.Screen;
import piecework.test.ExampleFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * @author James Renfro
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={UnitTestConfiguration.class})
@ActiveProfiles("test")
public class RequestHandlerTest {

    @Autowired
    RequestHandler requestHandler;

    HttpServletRequest servletRequest;
    Process process;
    String processInstanceId;

    @Before
    public void setUp() throws Exception {
        this.servletRequest = Mockito.mock(HttpServletRequest.class);
        this.process = ExampleFactory.exampleProcess();
        this.processInstanceId = "123";
    }

    @Test
    public void testCreateAndHandleInitialRequest() throws Exception {
        Mockito.when(servletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        Mockito.when(servletRequest.getRemoteHost()).thenReturn("127.0.0.1");
        Mockito.when(servletRequest.getRemotePort()).thenReturn(8000);
        Mockito.when(servletRequest.getRemoteUser()).thenReturn("tester");

        RequestDetails requestDetails = new RequestDetails.Builder().build();
        Interaction firstInteraction = process.getInteractions().iterator().next();
        FormRequest formRequest = requestHandler.create(requestDetails, process);
        assertValid(formRequest);

        FormRequest handleRequest = requestHandler.handle(requestDetails, Constants.RequestTypes.SUBMISSION, formRequest.getRequestId());
        assertEqual(formRequest, handleRequest);
    }

    @Test
    public void testCreateAndHandleInitialRequestWrongUser() throws Exception {
        Mockito.when(servletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        Mockito.when(servletRequest.getRemoteHost()).thenReturn("127.0.0.1");
        Mockito.when(servletRequest.getRemotePort()).thenReturn(8000);
        Mockito.when(servletRequest.getRemoteUser()).thenReturn("tester").thenReturn("somebodyelse");

        RequestDetails requestDetails = new RequestDetails.Builder().build();
        Interaction firstInteraction = process.getInteractions().iterator().next();
        FormRequest formRequest = requestHandler.create(requestDetails, process);
        assertValid(formRequest);

        boolean isExceptionThrown = false;
        try {
            requestHandler.handle(requestDetails, Constants.RequestTypes.SUBMISSION, formRequest.getRequestId());
        } catch (ForbiddenError error) {
            isExceptionThrown = true;
        }

        Assert.assertTrue(isExceptionThrown);
    }

    private void assertValid(FormRequest formRequest) {
        Assert.assertNotNull(formRequest);
        Assert.assertNotNull(formRequest.getRequestId());
        Assert.assertEquals(process.getProcessDefinitionKey(), formRequest.getProcessDefinitionKey());
        Assert.assertNull(formRequest.getProcessInstanceId());

        // Interaction should be first interaction from example factory
        Interaction actualInteraction = formRequest.getInteraction();
        Interaction expectedInteraction = ExampleFactory.exampleInteractionWithTwoScreens();
        Assert.assertEquals(expectedInteraction.getLabel(), actualInteraction.getLabel());

        Screen actualScreen = formRequest.getScreen();
        Screen expectedScreen = ExampleFactory.exampleScreenWithTwoSections(Constants.ScreenTypes.WIZARD);
        Assert.assertEquals(expectedScreen.getTitle(), actualScreen.getTitle());
    }

    private void assertEqual(FormRequest expected, FormRequest actual) {
        assertValid(actual);

        Assert.assertEquals(expected.getRequestId(), actual.getRequestId());
        Assert.assertEquals(expected.getProcessDefinitionKey(), actual.getProcessDefinitionKey());
    }

}
