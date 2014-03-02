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

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import piecework.authorization.AuthorizationRole;
import piecework.common.ViewContext;
import piecework.content.ContentResource;
import piecework.content.concrete.BasicContentResource;
import piecework.engine.ProcessEngineFacade;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.AllowedTaskProvider;
import piecework.persistence.ProcessProvider;
import piecework.repository.*;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.service.IdentityService;
import piecework.settings.UserInterfaceSettings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class AllowedTaskRepositoryProviderTest {

    @Mock
    ProcessEngineFacade facade;

    @Mock
    IdentityService identityService;

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

    @Mock
    Task task;

    private AllowedTaskProvider allowedTaskProvider;

    private final static String TESTUSER_ACTIVE_TASK_ID = "999";
    private final static String ANOTHER_ACTIVE_TASK_ID = "997";

    @Before
    public void setup() throws Exception {
        Process process = new Process.Builder()
                .processDefinitionKey("TEST")
                .build();

        ProcessInstance instance = new ProcessInstance.Builder()
                .processDefinitionKey("TEST")
                .task(new Task.Builder()
                        .taskInstanceId("1000")
                        .candidateAssigneeId("testuser")
                        .build())
                .task(new Task.Builder()
                        .taskInstanceId(TESTUSER_ACTIVE_TASK_ID)
                        .candidateAssigneeId("testuser")
                        .active()
                        .build())
                .task(new Task.Builder()
                        .taskInstanceId("998")
                        .candidateAssigneeId("testuser")
                        .build())
                .task(new Task.Builder()
                        .taskInstanceId(ANOTHER_ACTIVE_TASK_ID)
                        .candidateAssigneeId("another")
                        .active()
                        .build())
                .attachmentId("233")
                .build();

        ContentResource contentResource = new BasicContentResource.Builder()
                .inputStream(new ByteArrayInputStream("This is some test data from an input stream".getBytes()))
                .build();

        Attachment att1 = new Attachment.Builder()
                .contentType("text/plain")
                .location("/TEST/some/path/file.txt")
                .attachmentId("233")
                .build();

        Mockito.doReturn(Collections.singletonList(att1))
                .when(attachmentRepository).findAll(any(Iterable.class));

        Mockito.doReturn("testuser")
                .when(principal).getEntityId();
        Mockito.doReturn(Boolean.TRUE)
                .when(principal).hasRole(eq(process), eq(AuthorizationRole.USER));

        Mockito.doReturn(process)
                .when(processRepository).findOne(eq("TEST"));
        Mockito.doReturn(instance)
                .when(processInstanceRepository).findOne(eq("1234"));

        ProcessProvider processProvider = new ProcessRepositoryProvider(processRepository, "TEST", principal);
        this.allowedTaskProvider = allowedTaskProvider(processProvider);

        Mockito.doReturn(contentResource)
                .when(contentRepository).findByLocation(eq(allowedTaskProvider), eq("/TEST/some/path/file.txt"));
    }

    @Test
    public void verifyAttachmentContent() throws PieceworkException, IOException {
        ContentResource attachment = allowedTaskProvider.attachment("233");
        Assert.assertNotNull(attachment);
        String expected = "This is some test data from an input stream";
        String actual = IOUtils.toString(attachment.getInputStream());
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void verifyNoAttachmentContentInvalidId() throws PieceworkException, IOException {
        ContentResource attachment = allowedTaskProvider.attachment("234");
        Assert.assertNull(attachment);
    }

    @Test
    public void verifyAllowedTask() throws Exception {
        Task task = allowedTaskProvider.allowedTask(true);
        Assert.assertEquals(TESTUSER_ACTIVE_TASK_ID, task.getTaskInstanceId());
    }

    @Test
     public void verifyAllowedTaskNoLimit() throws Exception {
        Task task = allowedTaskProvider.allowedTask(false);
        Assert.assertEquals(TESTUSER_ACTIVE_TASK_ID, task.getTaskInstanceId());
    }

    @Test
    public void verifyAllowedTaskViewContext() throws Exception {
        UserInterfaceSettings settings = Mockito.mock(UserInterfaceSettings.class);
        Mockito.doReturn("https://somehost.org")
                .when(settings).getHostUri();
        Mockito.doReturn("/piecework/ui")
                .when(settings).getApplicationUrl();
        Mockito.doReturn("/piecework/api")
                .when(settings).getServiceUrl();

        Task task = allowedTaskProvider.allowedTask(new ViewContext(settings, "v0"), true);
        Assert.assertEquals(TESTUSER_ACTIVE_TASK_ID, task.getTaskInstanceId());
        Assert.assertEquals("https://somehost.org/piecework/ui/task/999", task.getLink());
        Assert.assertEquals("https://somehost.org/piecework/api/v0/task/999", task.getUri());
    }

    private AllowedTaskProvider allowedTaskProvider(ProcessProvider processProvider) {
        return new AllowedTaskRepositoryProvider(processProvider, processInstanceRepository, facade, attachmentRepository, contentRepository, deploymentRepository, identityService, new PassthroughSanitizer(), "1234");
    }

}
