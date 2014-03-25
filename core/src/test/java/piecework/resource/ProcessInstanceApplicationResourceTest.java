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

import junit.framework.Assert;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import piecework.authorization.AuthorizationRole;
import piecework.enumeration.ActionType;
import piecework.identity.IdentityHelper;
import piecework.model.*;
import piecework.model.Process;
import piecework.repository.AttachmentRepository;
import piecework.repository.ProcessInstanceRepository;
import piecework.repository.ProcessRepository;
import piecework.repository.RequestRepository;
import piecework.test.ProcessFactory;
import piecework.test.config.IntegrationTestConfiguration;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

/**
 * @author James Renfro
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={IntegrationTestConfiguration.class})
public class ProcessInstanceApplicationResourceTest {

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
    ProcessInstanceApplicationResource instanceResource;

    @Autowired
    AttachmentRepository mockAttachmentRepository;

    @Autowired
    IdentityHelper mockIdentityHelper;

    @Autowired
    ProcessRepository mockProcessRepository;

    @Autowired
    ProcessInstanceRepository mockProcessInstanceRepository;

    @Autowired
    RequestRepository mockRequestRepository;

    private Action createAction;
    private piecework.model.Process remoteProcess;
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

    @Test
    public void verifyAttach() throws Exception {
        setupValidInstanceData();
        MessageContext context = Mockito.mock(MessageContext.class);

        Mockito.doReturn(Collections.singletonList(new Attachment.Builder()
                .attachmentId("Attachment-12345")
                .name("comment")
                .description("This is a test comment")
                .build()))
               .when(mockAttachmentRepository).findAll(any(Iterable.class));

        MultivaluedMap<String, String> parameters = new MetadataMap<String, String>();
        parameters.putSingle("comment", "This is a test");
        Response response = instanceResource.attach(context, TEST_REMOTE_PROCESS_DEFINITION_KEY, TEST_REMOTE_INSTANCE_ID, parameters);

        SearchResults searchResults =  (SearchResults)response.getEntity();
        List<Object> attachments = searchResults.getList();

        Attachment attachment = (Attachment)attachments.iterator().next();
        Assert.assertNotNull(attachment);
        Assert.assertEquals("comment", attachment.getName());
        Assert.assertEquals("This is a test comment", attachment.getDescription());
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
                .attachmentId("Attachment-12345")
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
