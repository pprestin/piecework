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
package piecework.service;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import piecework.authorization.AuthorizationRole;
import piecework.model.RequestDetails;
import piecework.identity.IdentityHelper;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.RequestRepository;
import piecework.settings.SecuritySettings;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.test.ExampleFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * @author James Renfro
 */
@Ignore
@RunWith(MockitoJUnitRunner.class)
public class RequestServiceTest {

    @InjectMocks
    RequestService requestService;

    @Mock
    RequestRepository requestRepository;

    @Mock
    IdentityHelper identityHelper;

    @Mock
    TaskService taskService;

    @Mock
    SecuritySettings securitySettings;

    private MessageContext context;
    private HttpServletRequest servletRequest;
    private Process process;

    @Before
    public void setUp() throws Exception {
        this.context = Mockito.mock(MessageContext.class);
        this.servletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(context.getHttpServletRequest()).thenReturn(servletRequest);
        this.process = ExampleFactory.exampleProcess();
        User user = Mockito.mock(User.class);
        Mockito.when(user.getUserId()).thenReturn("123456789");
        Mockito.when(user.getDisplayName()).thenReturn("Test User");
        Mockito.when(user.getVisibleId()).thenReturn("testuser");
        Mockito.when(identityHelper.getPrincipal()).thenReturn(user);
        Mockito.when(user.hasRole(process, AuthorizationRole.INITIATOR)).thenReturn(true);

        Mockito.when(requestRepository.save(Mockito.any(FormRequest.class))).thenAnswer(new Answer<FormRequest>() {
            @Override
            public FormRequest answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                FormRequest formRequest = FormRequest.class.cast(args[0]);
                return new FormRequest.Builder(formRequest, new PassthroughSanitizer()).requestId("1").build();
            }
        });
    }

    @Test
    public void testCreateAndHandleInitialRequestAsOverseer() throws Exception {
        User user = Mockito.mock(User.class);
        Mockito.when(user.getUserId()).thenReturn("123456789");
        Mockito.when(user.getDisplayName()).thenReturn("Test User");
        Mockito.when(user.getVisibleId()).thenReturn("testuser");
        Mockito.when(user.hasRole(process, AuthorizationRole.OVERSEER)).thenReturn(true);
        Mockito.when(servletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        Mockito.when(servletRequest.getRemoteHost()).thenReturn("127.0.0.1");
        Mockito.when(servletRequest.getRemotePort()).thenReturn(8000);
        Mockito.when(servletRequest.getRemoteUser()).thenReturn("tester");

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        FormRequest formRequest = requestService.create(requestDetails, process);
        assertValid(formRequest);

        Mockito.when(requestRepository.findOne(Mockito.any(String.class))).thenReturn(formRequest);

        FormRequest handleRequest = requestService.read(requestDetails, formRequest.getRequestId());
        assertEqual(formRequest, handleRequest);
    }

//    @Test
//    public void testCreateAndHandleInitialRequestAsOverseerWrongUser() throws Exception {
//        Mockito.when(identityHelper.hasRole(process, AuthorizationRole.OVERSEER)).thenReturn(true);
//        Mockito.when(servletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
//        Mockito.when(servletRequest.getRemoteHost()).thenReturn("127.0.0.1");
//        Mockito.when(servletRequest.getRemotePort()).thenReturn(8000);
//        Mockito.when(servletRequest.getRemoteUser()).thenReturn("tester").thenReturn("somebodyelse");
//
//        RequestDetails firstRequest = new RequestDetails.Builder(context, securitySettings).build();
//        FormRequest formRequest = requestService.create(firstRequest, process);
//        assertValid(formRequest);
//
//        boolean isExceptionThrown = false;
//        try {
//            RequestDetails secondRequest = new RequestDetails.Builder(context, securitySettings).build();
//            requestService.handle(secondRequest, formRequest.getRequestId());
//        } catch (ForbiddenError error) {
//            isExceptionThrown = true;
//        }
//
//        Assert.assertTrue(isExceptionThrown);
//    }

    private void assertValid(FormRequest formRequest) {
        Assert.assertNotNull(formRequest);
        Assert.assertNotNull(formRequest.getRequestId());
        Assert.assertEquals(process.getProcessDefinitionKey(), formRequest.getProcessDefinitionKey());
        Assert.assertNull(formRequest.getProcessInstanceId());

        // Interaction should be first interaction from example factory
//        Interaction actualInteraction = formRequest.getInteraction();
//        Interaction expectedInteraction = ExampleFactory.exampleInteractionWithTwoScreens();
//        Assert.assertEquals(expectedInteraction.getLabel(), actualInteraction.getLabel());

//        Screen actualScreen = formRequest.getScreen();
//        Screen expectedScreen = ExampleFactory.exampleContainer(Constants.ScreenTypes.WIZARD);
//        Assert.assertEquals(expectedScreen.getTitle(), actualScreen.getTitle());
    }

    private void assertEqual(FormRequest expected, FormRequest actual) {
        assertValid(actual);

        Assert.assertEquals(expected.getRequestId(), actual.getRequestId());
        Assert.assertEquals(expected.getProcessDefinitionKey(), actual.getProcessDefinitionKey());
    }

}
