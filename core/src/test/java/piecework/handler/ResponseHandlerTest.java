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
package piecework.handler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import piecework.Versions;
import piecework.model.RequestDetails;
import piecework.common.ViewContext;
import piecework.enumeration.ActionType;
import piecework.exception.StatusCodeError;
import piecework.form.LegacyFormFactory;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ContentRepository;
import piecework.test.ExampleFactory;
import piecework.validation.FormValidation;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static org.mockito.Mockito.*;

/**
 * @author James Renfro
 */
@Ignore
@RunWith(MockitoJUnitRunner.class)
public class ResponseHandlerTest {

    private static final String HTML = "<html><body><form><input name=\"employeeName\"></body></html>";
    private static final String OUTPUT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<html><head></head><body><form enctype=\"multipart/form-data\" action=\"null\" method=\"POST\"><input name=\"employeeName\" /><input name=\"PROCESS_FORM_SUBMISSION_TOKEN\" value=\"0b82440e-0c3c-4433-b629-c41e68049b8b\" type=\"hidden\" /></form></body></html>";

    @InjectMocks
    ResponseHandler responseHandler;

    @Mock
    ContentRepository contentRepository;

    @Mock
    LegacyFormFactory legacyFormFactory;

    @Mock
    Versions versions;

    private FormRequest formRequest;
    private Process process;
    private Form form;
    private Task task;

    @Before
    public void setup() throws StatusCodeError {
        process = ExampleFactory.exampleProcess();
        formRequest = new FormRequest.Builder()
                .requestId("1")
//                .screen(ExampleFactory.exampleContainer(Constants.ScreenTypes.WIZARD))
                .build();
        form = new Form.Builder().formInstanceId("123").build();
        task = new Task.Builder().taskInstanceId("456").build();

        ViewContext version1 = new ViewContext("http://localhost:8000", "/ui", "/api", "/public", "/v1");
        Mockito.when(versions.getVersion1()).thenReturn(version1);
        Mockito.when(legacyFormFactory.form(any(FormRequest.class), any(Process.class), any(ProcessInstance.class), any(Task.class), any(FormValidation.class), ActionType.CREATE, null)).thenReturn(form);
    }

    @Test
    public void testHandleInitial() throws StatusCodeError {
        RequestDetails requestDetails = Mockito.mock(RequestDetails.class);
        Response response = responseHandler.handle(requestDetails, formRequest, process);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertEquals(form.getFormInstanceId(), ((Form)response.getEntity()).getFormInstanceId());
    }

    @Test
    public void testHandleTask() throws StatusCodeError {
        RequestDetails requestDetails = Mockito.mock(RequestDetails.class);
        Response response = responseHandler.handle(requestDetails, formRequest, process, null, task, null);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertEquals(form.getFormInstanceId(), ((Form)response.getEntity()).getFormInstanceId());
    }

    @Test
    public void testRedirect() throws StatusCodeError {
        Response response = responseHandler.redirect(formRequest);
        Assert.assertEquals(Response.Status.SEE_OTHER.getStatusCode(), response.getStatus());
        Assert.assertEquals("http://localhost:8000/ui/form/1", response.getHeaderString(HttpHeaders.LOCATION));
    }

}
