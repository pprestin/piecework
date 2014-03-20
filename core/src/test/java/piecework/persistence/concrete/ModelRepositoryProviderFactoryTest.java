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

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import piecework.authorization.AuthorizationRole;
import piecework.common.ViewContext;
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.AllowedTaskProvider;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.persistence.ProcessInstanceProvider;
import piecework.persistence.ProcessProvider;
import piecework.repository.*;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.service.CacheService;
import piecework.settings.UserInterfaceSettings;
import piecework.util.ModelUtility;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class ModelRepositoryProviderFactoryTest {

    @InjectMocks
    ModelRepositoryProviderFactory factory;

    @Mock
    ActivityRepository activityRepository;

    @Mock
    AttachmentRepository attachmentRepository;

    @Mock
    CacheService cacheService;

    @Mock
    DeploymentRepository deploymentRepository;

    @Mock
    ProcessRepository processRepository;

    @Mock
    User principal;

    @Mock
    ProcessInstanceRepository processInstanceRepository;

    @Mock
    PassthroughSanitizer sanitizer;

    @Before
    public void setup() {
        ProcessDeployment currentDeployment = new ProcessDeployment.Builder()
                .deploymentId("2")
                .build();
        ProcessDeployment previousDeployment = new ProcessDeployment.Builder()
                .deploymentId("1")
                .build();
        piecework.model.Process process = new Process.Builder()
                .processDefinitionKey("TEST")
                .deploy(new ProcessDeploymentVersion(previousDeployment), previousDeployment)
                .deploy(new ProcessDeploymentVersion(currentDeployment), currentDeployment)
                .build();

        ProcessInstance instance = new ProcessInstance.Builder()
                .processDefinitionKey("TEST")
                .processInstanceId("1234")
                .deploymentId("1")
                .attachmentId("233")
                .task(new Task.Builder()
                        .taskInstanceId("555")
                        .taskDescription("Some task")
                        .assigneeId("testuser")
                        .active()
                        .build())
                .build();

        Mockito.doReturn("testuser")
               .when(principal).getEntityId();

        Mockito.doReturn("testuser")
               .when(principal).getUserId();

        Mockito.doReturn(Boolean.TRUE)
               .when(principal).hasRole(eq(process), eq(AuthorizationRole.USER));

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

        Mockito.doReturn(instance)
                .when(processInstanceRepository).findByTaskId(eq("TEST"), eq("555"));

        Mockito.doReturn(currentDeployment)
                .when(deploymentRepository).findOne(eq("2"));

        Mockito.doReturn(previousDeployment)
                .when(deploymentRepository).findOne(eq("1"));

        Mockito.doCallRealMethod()
               .when(sanitizer).sanitize(anyString());

        List<Attachment> attachments = new ArrayList<Attachment>();
        Attachment storedAttachment = new Attachment.Builder()
                .attachmentId("223")
                .contentType("image/png")
                .build();
        attachments.add(storedAttachment);
        Mockito.doReturn(attachments)
                .when(attachmentRepository).findAll(any(Iterable.class));
    }

    @Test
    public void verifyAllowedTaskProvider() throws PieceworkException {
        AllowedTaskProvider allowedTaskProvider = factory.allowedTaskProvider("TEST", "1234", principal);
        Assert.assertTrue(allowedTaskProvider instanceof AllowedTaskProvider);
        Assert.assertEquals("TEST", allowedTaskProvider.processDefinitionKey());
        Assert.assertEquals("TEST", allowedTaskProvider.process().getProcessDefinitionKey());

        Task task = allowedTaskProvider.allowedTask(true);
        Assert.assertEquals("555", task.getTaskInstanceId());
        Assert.assertEquals("Some task", task.getTaskDescription());
    }

    @Test(expected = NotFoundError.class)
    public void verifyAllowedTaskProviderWithUnknownTask() throws PieceworkException {
        AllowedTaskProvider allowedTaskProvider = factory.allowedTaskProvider("TEST", "1230", principal);
        Assert.assertTrue(allowedTaskProvider instanceof AllowedTaskProvider);
        Assert.assertEquals("TEST", allowedTaskProvider.processDefinitionKey());
        Assert.assertEquals("TEST", allowedTaskProvider.process().getProcessDefinitionKey());

        allowedTaskProvider.allowedTask(true);
    }

//    @Test
//    public void verifyCachingProcessProvider() throws PieceworkException {
//        ProcessProvider processProvider = factory.processProvider("TEST", principal);
//        Assert.assertTrue(processProvider instanceof CachingProcessProvider);
//        Assert.assertEquals("TEST", processProvider.processDefinitionKey());
//
//        Process process = processProvider.process();
//        Assert.assertEquals("TEST", process.getProcessDefinitionKey());
//    }

    @Test
    public void verifyDeploymentProvider() throws PieceworkException {
        ProcessDeploymentProvider deploymentProvider = factory.deploymentProvider("TEST", principal);
        Assert.assertTrue(deploymentProvider instanceof ProcessDeploymentRepositoryProvider);
        Process process = deploymentProvider.process();
        Assert.assertEquals("TEST", process.getProcessDefinitionKey());
        ProcessDeployment deployment = deploymentProvider.deployment();
        Assert.assertEquals("2", deployment.getDeploymentId());
    }

    @Test
    public void verifyDeploymentByIdProvider() throws PieceworkException {
        ProcessDeploymentProvider deploymentProvider = factory.deploymentProvider("TEST", "1", principal);
        Assert.assertTrue(deploymentProvider instanceof ProcessDeploymentRepositoryProvider);
        Process process = deploymentProvider.process();
        Assert.assertEquals("TEST", process.getProcessDefinitionKey());
        ProcessDeployment deployment = deploymentProvider.deployment();
        Assert.assertEquals("1", deployment.getDeploymentId());
    }

    @Test
    public void verifyFormRequestTaskIdProvider() throws PieceworkException {
        FormRequest request = new FormRequest.Builder()
                .processDefinitionKey("TEST")
                .taskId("555")
                .build();

        ProcessDeploymentProvider deploymentProvider = factory.provider(request, principal);
        Task task = ModelUtility.task(deploymentProvider);
        Assert.assertEquals("555", task.getTaskInstanceId());
        Assert.assertEquals("Some task", task.getTaskDescription());
    }

    @Test
    public void verifyFormRequestInstanceIdProvider() throws PieceworkException {
        FormRequest request = new FormRequest.Builder()
                .processDefinitionKey("TEST")
                .processInstanceId("1234")
                .build();

        ProcessDeploymentProvider deploymentProvider = factory.provider(request, principal);
        ProcessInstance instance = ModelUtility.instance(deploymentProvider);
        Assert.assertEquals("1234", instance.getProcessInstanceId());
    }

    @Test
    public void verifyInstanceProvider() throws PieceworkException {
        ProcessInstanceProvider instanceProvider = factory.instanceProvider("TEST", "1234", principal);
        Assert.assertTrue(instanceProvider instanceof ProcessInstanceRepositoryProvider);
        Process process = instanceProvider.process();
        Assert.assertEquals("TEST", process.getProcessDefinitionKey());
        ProcessDeployment deployment = instanceProvider.deployment();
        Assert.assertEquals("1", deployment.getDeploymentId());
        ProcessInstance instance = instanceProvider.instance();
        Assert.assertEquals("1234", instance.getProcessInstanceId());
        instance = instanceProvider.instance(new ViewContext(new UserInterfaceSettings(), "v9"));
        Attachment retrievedAttachment = instance.getAttachments().iterator().next();
        org.junit.Assert.assertEquals("223", retrievedAttachment.getAttachmentId());
        org.junit.Assert.assertEquals("image/png", retrievedAttachment.getContentType());
    }

}
