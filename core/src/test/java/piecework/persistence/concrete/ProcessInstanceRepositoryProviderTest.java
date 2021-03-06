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
package piecework.persistence.concrete;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import piecework.content.ContentResource;
import piecework.engine.ProcessEngineFacade;
import piecework.exception.*;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ContentProfileProvider;
import piecework.persistence.ProcessInstanceProvider;
import piecework.persistence.ProcessProvider;
import piecework.repository.*;
import piecework.test.ProcessFactory;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessInstanceRepositoryProviderTest {

    @Mock
    ProcessEngineFacade facade;

    @Mock
    ProcessRepository processRepository;

    @Mock
    ProcessInstanceRepository processInstanceRepository;

    @Mock
    AttachmentRepository attachmentRepository;

    @Mock
    ContentRepository contentRepository;

    @Mock
    ContentResource contentResource;

    @Mock
    DeploymentRepository deploymentRepository;

    @Mock
    Entity principal;

    @Before
    public void setup() throws Exception {
        ProcessDeployment currentDeployment = new ProcessDeployment.Builder()
                .deploymentId("2")
                .deploymentLabel("Second")
                .build();
        ProcessDeployment previousDeployment = new ProcessDeployment.Builder()
                .deploymentId("1")
                .deploymentLabel("First")
                .build();

        ProcessDeploymentVersion version1 = new ProcessDeploymentVersion(previousDeployment);
        ProcessDeploymentVersion version2 = new ProcessDeploymentVersion(currentDeployment);

        Process process = new Process.Builder()
                .processDefinitionKey("TEST")
                .version(version1)
                .version(version2)
                .deploy(version1, previousDeployment)
                .deploy(version2, currentDeployment)
                .build();

        ProcessInstance instance = new ProcessInstance.Builder()
                .processDefinitionKey("TEST")
                .processInstanceId("1234")
                .deploymentId("1")
                .attachmentId("233")
                .build();

        ProcessInstance deletedInstance = new ProcessInstance.Builder()
                .processDefinitionKey("TEST")
                .processInstanceId("1234")
                .deploymentId("2")
                .delete()
                .build();

        Mockito.doReturn(process)
               .when(processRepository).findOne("TEST");

        Mockito.doReturn(deletedInstance)
                .when(processInstanceRepository).findOne(eq("1233"));

        Mockito.doReturn(instance)
               .when(processInstanceRepository).findOne(eq("1234"));

        Mockito.doReturn(currentDeployment)
               .when(deploymentRepository).findOne(eq("2"));

        Mockito.doReturn(previousDeployment)
               .when(deploymentRepository).findOne(eq("1"));

        List<Attachment> attachments = new ArrayList<Attachment>();
        Attachment retrievedAttachment = new Attachment.Builder()
                .attachmentId("233")
                .location("/some/location")
                .contentType("image/png")
                .build();
        attachments.add(retrievedAttachment);
        Mockito.doReturn(attachments)
                .when(attachmentRepository).findAll(any(Iterable.class));

        Mockito.doReturn(new ByteArrayInputStream("This is some test data from an input stream".getBytes()))
                .when(contentResource).getInputStream();
        Mockito.doReturn(contentResource)
                .when(contentRepository).findByLocation(any(ContentProfileProvider.class), eq("/some/location"));
    }

    @Test(expected= BadRequestError.class)
    public void verifyErrorOnNullProcessKey() throws PieceworkException {
        ProcessProvider processProvider = new ProcessRepositoryProvider(processRepository, null, principal);
        ProcessInstanceProvider instanceProvider = processInstanceProvider(processProvider);
        instanceProvider.instance();
    }

    @Test(expected= BadRequestError.class)
    public void verifyErrorOnEmptyProcessKey() throws PieceworkException {
        ProcessProvider processProvider = new ProcessRepositoryProvider(processRepository, "", principal);
        ProcessInstanceProvider instanceProvider = processInstanceProvider(processProvider);
        instanceProvider.instance();
    }

    @Test(expected = NotFoundError.class)
    public void verifyErrorOnNotFoundProcess() throws PieceworkException {
        piecework.model.Process mockProcess = Mockito.mock(piecework.model.Process.class);
        Mockito.doReturn(mockProcess)
                .when(processRepository).findOne(eq("TEST-1"));
        ProcessProvider processProvider = new ProcessRepositoryProvider(processRepository, "TEST-2", principal);
        ProcessInstanceProvider instanceProvider = processInstanceProvider(processProvider);
        instanceProvider.instance();
    }

    @Test(expected = GoneError.class)
    public void verifyErrorOnDeletedProcess() throws PieceworkException {
        Process mockProcess = Mockito.mock(Process.class);
        Mockito.doReturn(true)
                .when(mockProcess).isDeleted();
        Mockito.doReturn(mockProcess)
                .when(processRepository).findOne(eq("TEST"));
        ProcessProvider processProvider = new ProcessRepositoryProvider(processRepository, "TEST", principal);
        ProcessInstanceProvider instanceProvider = processInstanceProvider(processProvider);
        instanceProvider.instance();
    }

    @Test(expected = NotFoundError.class)
    public void verifyErrorOnNullInstanceId() throws PieceworkException {
        ProcessProvider processProvider = new ProcessRepositoryProvider(processRepository, "TEST", principal);
        ProcessInstanceProvider instanceProvider = new ProcessInstanceRepositoryProvider(processProvider, processInstanceRepository, facade, attachmentRepository, contentRepository, deploymentRepository, null);
        instanceProvider.instance();
    }

    @Test(expected = GoneError.class)
    public void verifyErrorOnDeletedInstanceId() throws PieceworkException {
        ProcessProvider processProvider = new ProcessRepositoryProvider(processRepository, "TEST", principal);
        ProcessInstanceProvider instanceProvider = new ProcessInstanceRepositoryProvider(processProvider, processInstanceRepository, facade, attachmentRepository, contentRepository, deploymentRepository, "1233");
        instanceProvider.instance();
    }

    @Test
    public void verifySuccessWithoutContext() throws PieceworkException {
        ProcessProvider processProvider = new ProcessRepositoryProvider(processRepository, "TEST", principal);
        ProcessInstanceProvider instanceProvider = processInstanceProvider(processProvider);
        ProcessInstance instance = instanceProvider.instance();
        Assert.assertEquals("1234", instance.getProcessInstanceId());
    }

    @Test
    public void verifySuccessWithContext() throws PieceworkException {
        ProcessProvider processProvider = new ProcessRepositoryProvider(processRepository, "TEST", principal);
        ProcessInstanceProvider instanceProvider = processInstanceProvider(processProvider);
        ProcessInstance instance = instanceProvider.instance(ProcessFactory.viewContext());
        Assert.assertEquals("1234", instance.getProcessInstanceId());
        Assert.assertEquals("https://somehost.org/piecework/ui/instance/TEST/1234", instance.getLink());
        Assert.assertEquals("https://somehost.org/piecework/api/v0/instance/TEST/1234", instance.getUri());
        Attachment retrievedAttachment = instance.getAttachments().iterator().next();
        Assert.assertEquals("233", retrievedAttachment.getAttachmentId());
        Assert.assertEquals("image/png", retrievedAttachment.getContentType());
    }

    @Test
    public void verifyPreviousDeployment() throws PieceworkException {
        ProcessProvider processProvider = new ProcessRepositoryProvider(processRepository, "TEST", principal);
        ProcessInstanceProvider instanceProvider = processInstanceProvider(processProvider);
        ProcessDeployment deployment = instanceProvider.deployment();
        Assert.assertEquals("1", deployment.getDeploymentId());
    }

    @Test
    public void verifyDiagram() throws PieceworkException {
        Mockito.doReturn(contentResource)
               .when(facade).resource(any(Process.class), any(ProcessDeployment.class), eq("image/png"));
        ProcessProvider processProvider = new ProcessRepositoryProvider(processRepository, "TEST", principal);
        ProcessInstanceProvider instanceProvider = processInstanceProvider(processProvider);
        ContentResource contentResource = instanceProvider.diagram();
        Assert.assertNotNull(contentResource);
        Assert.assertEquals(this.contentResource, contentResource);
    }

    private ProcessInstanceProvider processInstanceProvider(ProcessProvider processProvider) {
        return new ProcessInstanceRepositoryProvider(processProvider, processInstanceRepository, facade, attachmentRepository, contentRepository, deploymentRepository, "1234");
    }

}
