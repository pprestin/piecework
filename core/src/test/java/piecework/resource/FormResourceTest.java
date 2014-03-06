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
package piecework.resource;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import piecework.authorization.AuthorizationRole;
import piecework.enumeration.ActionType;
import piecework.exception.BadRequestError;
import piecework.exception.ForbiddenError;
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.identity.IdentityHelper;
import piecework.model.*;
import piecework.model.Process;
import piecework.repository.ProcessInstanceRepository;
import piecework.repository.ProcessRepository;
import piecework.repository.RequestRepository;
import piecework.test.ProcessFactory;
import piecework.test.config.IntegrationTestConfiguration;
import piecework.validation.Validation;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.eq;

/**
 * @author James Renfro
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={IntegrationTestConfiguration.class})
public class FormResourceTest {

    private final static String TEST_REMOTE_PROCESS_DEFINITION_KEY = "REMOTE-TEST";
    private final static String TEST_MULTISTEP_PROCESS_DEFINITION_KEY = "MULTISTEP-TEST";
    private final static String TEST_USER_ID = "09234";
    private final static String TEST_REMOTE_INSTANCE_ID = "333";
    private final static String TEST_MULTISTEP_INSTANCE_ID = "444";
    private final static String TEST_REMOTE_TASK_ID = "1234";
    private final static String TEST_MULTISTEP_TASK_ID = "1235";
    private final static String TEST_MULTISTEP_DEPLOYMENT_ID = "88230";
    private final static String TEST_REMOTE_DEPLOYMENT_ID = "992340";

    @Autowired
    FormResource formResource;

    @Autowired
    IdentityHelper mockIdentityHelper;

    @Autowired
    ProcessRepository mockProcessRepository;

    @Autowired
    ProcessInstanceRepository mockProcessInstanceRepository;

    @Autowired
    RequestRepository mockRequestRepository;


    private Action createAction;
    private Process remoteProcess;
    private Process multiStepProcess;
    private User mockPrincipal;

    @Before
    public void setup() {
        mockPrincipal = Mockito.mock(User.class);
        Mockito.doReturn(mockPrincipal)
               .when(mockIdentityHelper).getPrincipal();
        Mockito.doReturn(TEST_USER_ID)
               .when(mockPrincipal).getEntityId();
        Mockito.doReturn(TEST_USER_ID)
                .when(mockPrincipal).getUserId();

        createAction = Mockito.mock(Action.class);

        Activity startActivity = Mockito.mock(Activity.class);
        Mockito.doReturn(createAction)
               .when(startActivity).action(eq(ActionType.CREATE));

        ProcessDeployment remoteDeployment = ProcessFactory.remoteStrategyProcessDeployment(TEST_REMOTE_DEPLOYMENT_ID);
        remoteProcess = ProcessFactory.process(TEST_REMOTE_PROCESS_DEFINITION_KEY, "Some Test Process", remoteDeployment);

        ProcessDeployment multiStepDeployment = ProcessFactory.multistepProcessDeployment(TEST_MULTISTEP_DEPLOYMENT_ID);
        multiStepProcess = ProcessFactory.process(TEST_MULTISTEP_PROCESS_DEFINITION_KEY, "Another Process", multiStepDeployment);

        Mockito.doReturn(remoteProcess)
               .when(mockProcessRepository).findOne(eq(TEST_REMOTE_PROCESS_DEFINITION_KEY));
        Mockito.doReturn(multiStepProcess)
               .when(mockProcessRepository).findOne(eq(TEST_MULTISTEP_PROCESS_DEFINITION_KEY));
    }

    @Test(expected = NotFoundError.class)
    public void readByTaskIdNoProcess() throws PieceworkException {
        formResource.read(null, "INVALID_PROCESS_KEY", "INVALID_PROCESS_INSTANCE_ID", null, null, null, null);
    }

    @Test(expected = ForbiddenError.class)
    public void readByTaskIdNoInstance() throws PieceworkException {
        formResource.read(null, TEST_REMOTE_PROCESS_DEFINITION_KEY, "INVALID_TASK_INSTANCE_ID", null, null, null, null);
    }

    @Test
    public void readByTaskIdHtml() throws PieceworkException {
        setupValidInstanceData();

        Response response = formResource.read(null, TEST_REMOTE_PROCESS_DEFINITION_KEY, "1234", null, null, null, "1");

        Assert.assertEquals(303, response.getStatus());
        String location = response.getHeaderString(HttpHeaders.LOCATION);
        Assert.assertEquals("https://some.institution.org/path/to/CustomWebPage.html?taskId=1234&redirectCount=1", location);
    }

    @Test
    public void readByTaskIdJson() throws PieceworkException {
        MessageContext context = Mockito.mock(MessageContext.class);
        HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);

        setupValidInstanceData();

        Mockito.doReturn(Collections.singletonList(MediaType.APPLICATION_JSON_TYPE))
               .when(httpHeaders).getAcceptableMediaTypes();

        Mockito.doReturn(httpHeaders)
               .when(context).getHttpHeaders();

        Response response = formResource.read(context, TEST_REMOTE_PROCESS_DEFINITION_KEY, "1234", null, null, null, "1");

        Assert.assertEquals(200, response.getStatus());
        Form form = Form.class.cast(response.getEntity());
        Assert.assertNotNull(form);
    }

    @Test(expected = ForbiddenError.class)
    public void verifyRequestProcessDefinitionKeyMustMatch() throws PieceworkException {
        MessageContext context = Mockito.mock(MessageContext.class);
        HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);

        setupValidInstanceData();

        String testRequestId = "992003";

        FormRequest request = new FormRequest.Builder()
                .processDefinitionKey(TEST_REMOTE_PROCESS_DEFINITION_KEY)
                .requestId(testRequestId)
                .build();

        Mockito.doReturn(request)
                .when(mockRequestRepository).findOne(eq(testRequestId));

        MultivaluedMap<String, String> formData = new MetadataMap<String, String>();

        formResource.validate(TEST_MULTISTEP_PROCESS_DEFINITION_KEY, testRequestId, "general", context, formData);
    }

    @Test(expected = BadRequestError.class)
    public void verifyPostalCodeRequired() throws PieceworkException {
        setupValidInstanceData();

        String testRequestId = "992003";

        FormRequest request = new FormRequest.Builder()
                .processDefinitionKey(TEST_MULTISTEP_PROCESS_DEFINITION_KEY)
                .requestId(testRequestId)
                .build();

        Mockito.doReturn(request)
               .when(mockRequestRepository).findOne(eq(testRequestId));

        MultivaluedMap<String, String> formData = new MetadataMap<String, String>();
        formData.putSingle("Name", "Joe Tester");
        formData.putSingle("Street1", "10 Test Ave");
        formData.putSingle("City", "Testington");
        formData.putSingle("State", "WI");

        try {
            formResource.validate(TEST_MULTISTEP_PROCESS_DEFINITION_KEY, testRequestId, "general", null, formData);
        } catch (BadRequestError e) {
            Validation validation = e.getValidation();
            Map<String, List<Message>> results = validation.getResults();
            Assert.assertEquals(1, results.size());
            Assert.assertTrue(results.containsKey("PostalCode"));
            throw e;
        }
    }

    @Test
    public void verifyValidationAcceptsAllRequiredFields() throws PieceworkException {
        setupValidInstanceData();

        String testRequestId = "992003";

        FormRequest request = new FormRequest.Builder()
                .processDefinitionKey(TEST_MULTISTEP_PROCESS_DEFINITION_KEY)
                .requestId(testRequestId)
                .build();

        Mockito.doReturn(request)
                .when(mockRequestRepository).findOne(eq(testRequestId));

        MultivaluedMap<String, String> formData = new MetadataMap<String, String>();
        formData.putSingle("Name", "Joe Tester");
        formData.putSingle("Street1", "10 Test Ave");
        formData.putSingle("City", "Testington");
        formData.putSingle("State", "WI");
        formData.putSingle("PostalCode", "99999");

        formResource.validate(TEST_MULTISTEP_PROCESS_DEFINITION_KEY, testRequestId, "general", null, formData);
    }

    private void setupValidInstanceData() {
        Mockito.doReturn(true)
                .when(mockPrincipal).hasRole(eq(remoteProcess), eq(AuthorizationRole.OVERSEER));
        Mockito.doReturn(true)
                .when(mockPrincipal).hasRole(eq(remoteProcess), eq(AuthorizationRole.USER));
        Task remoteTask = new Task.Builder()
                .taskInstanceId(TEST_REMOTE_TASK_ID)
                .candidateAssigneeId(TEST_USER_ID)
                .taskDefinitionKey("preliminary")
                .active()
                .build();

        ProcessInstance remoteInstance = new ProcessInstance.Builder()
                .processDefinitionKey(TEST_REMOTE_PROCESS_DEFINITION_KEY)
                .processInstanceId(TEST_REMOTE_INSTANCE_ID)
                .deploymentId(TEST_REMOTE_DEPLOYMENT_ID)
                .task(remoteTask)
                .build();

        Mockito.doReturn(remoteInstance)
                .when(mockProcessInstanceRepository).findByTaskId(eq(TEST_REMOTE_PROCESS_DEFINITION_KEY), eq(TEST_REMOTE_TASK_ID));
        Mockito.doReturn(remoteInstance)
                .when(mockProcessInstanceRepository).findOne(TEST_REMOTE_INSTANCE_ID);

        Task multiStepTask = new Task.Builder()
                .taskInstanceId(TEST_REMOTE_TASK_ID)
                .candidateAssigneeId(TEST_USER_ID)
                .taskDefinitionKey("contact")
                .active()
                .build();

        ProcessInstance multistepInstance = new ProcessInstance.Builder()
                .processDefinitionKey(TEST_MULTISTEP_PROCESS_DEFINITION_KEY)
                .processInstanceId(TEST_MULTISTEP_INSTANCE_ID)
                .deploymentId(TEST_MULTISTEP_DEPLOYMENT_ID)
                .task(multiStepTask)
                .build();

        Mockito.doReturn(multistepInstance)
                .when(mockProcessInstanceRepository).findByTaskId(eq(TEST_MULTISTEP_PROCESS_DEFINITION_KEY), eq(TEST_MULTISTEP_TASK_ID));
        Mockito.doReturn(multistepInstance)
                .when(mockProcessInstanceRepository).findOne(TEST_MULTISTEP_INSTANCE_ID);
    }

}
