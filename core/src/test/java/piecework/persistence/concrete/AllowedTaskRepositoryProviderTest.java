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
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.AllowedTaskProvider;
import piecework.persistence.ProcessProvider;
import piecework.process.AttachmentQueryParameters;
import piecework.repository.*;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.service.IdentityService;
import piecework.settings.UserInterfaceSettings;
import piecework.test.ProcessFactory;

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
                .processInstanceId("334222")
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
                .formValue("SomeFile", new File.Builder()
                        .id("877")
                        .contentType("text/html")
                        .location("/TEST/some/other/path/test.html")
                        .name("Test.html")
                        .fieldName("SomeFile")
                        .processDefinitionKey("TEST")
                        .description("A quick description of the sample file")
                        .build())
                .attachmentId("233")
                .build();

        ContentResource attachmentContentResource = new BasicContentResource.Builder()
                .inputStream(new ByteArrayInputStream("This is some test data from an input stream".getBytes()))
                .build();

        ContentResource valueContentResource = new BasicContentResource.Builder()
                .inputStream(new ByteArrayInputStream("<html><body>Hello World</body></html>".getBytes()))
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

        Mockito.doReturn(attachmentContentResource)
                .when(contentRepository).findByLocation(eq(allowedTaskProvider), eq("/TEST/some/path/file.txt"));
        Mockito.doReturn(valueContentResource)
                .when(contentRepository).findByLocation(eq(allowedTaskProvider), eq("/TEST/some/other/path/test.html"));
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
        Task task = allowedTaskProvider.allowedTask(ProcessFactory.viewContext(), true);
        Assert.assertEquals(TESTUSER_ACTIVE_TASK_ID, task.getTaskInstanceId());
        Assert.assertEquals("https://somehost.org/piecework/ui/task/999", task.getLink());
        Assert.assertEquals("https://somehost.org/piecework/api/v0/task/999", task.getUri());
    }

    @Test
    public void verifyAttachments() throws Exception {
        SearchResults searchResults = allowedTaskProvider.attachments(new AttachmentQueryParameters(), ProcessFactory.viewContext());
        Assert.assertNotNull(searchResults);
        Assert.assertEquals(1, searchResults.getList().size());
        Attachment attachment = (Attachment)searchResults.getList().get(0);
        Assert.assertEquals("https://somehost.org/piecework/ui/instance/TEST/334222/attachment/233", attachment.getLink());
    }

    @Test
    public void verifyAttachmentsLimitToContentType() throws Exception {
        AttachmentQueryParameters queryParameters = new AttachmentQueryParameters();
        queryParameters.setContentType("text/plain");
        SearchResults searchResults = allowedTaskProvider.attachments(queryParameters, ProcessFactory.viewContext());
        Assert.assertEquals(1, searchResults.getList().size());
        Assert.assertEquals(1l, searchResults.getTotal().longValue());
    }

    @Test
    public void verifyAttachmentsLimitToContentTypeExclude() throws Exception {
        AttachmentQueryParameters queryParameters = new AttachmentQueryParameters();
        queryParameters.setContentType("text/html");
        SearchResults searchResults = allowedTaskProvider.attachments(queryParameters, ProcessFactory.viewContext());
        Assert.assertEquals(0l, searchResults.getTotal().longValue());
    }

    @Test
    public void verifyValue() throws Exception {
        ContentResource value = allowedTaskProvider.value("SomeFile", "877");
        Assert.assertNotNull(value);
        String expected = "<html><body>Hello World</body></html>";
        String actual = IOUtils.toString(value.getInputStream());
        Assert.assertEquals(expected, actual);
    }

    @Test(expected = NotFoundError.class)
    public void verifyInvalidValue() throws Exception {
        allowedTaskProvider.value("SomeFile", "233");
    }

    @Test
    public void verifyValues() throws Exception {
        SearchResults searchResults = allowedTaskProvider.values("SomeFile", ProcessFactory.viewContext());
        Assert.assertEquals(1l, searchResults.getTotal().longValue());
        Assert.assertEquals(1, searchResults.getList().size());
        File file = (File)searchResults.getList().get(0);
        Assert.assertEquals("https://somehost.org/piecework/ui/instance/TEST/334222/value/SomeFile/877", file.getLink());
        Assert.assertEquals("Test.html", file.getName());
    }

    @Test
    public void verifyValuesEmpty() throws Exception {
        SearchResults searchResults = allowedTaskProvider.values("SomeOtherFile", ProcessFactory.viewContext());
        Assert.assertEquals(0l, searchResults.getTotal().longValue());
    }

    private AllowedTaskProvider allowedTaskProvider(ProcessProvider processProvider) {
        return new AllowedTaskRepositoryProvider(processProvider, processInstanceRepository, facade, attachmentRepository, contentRepository, deploymentRepository, identityService, new PassthroughSanitizer(), "1234");
    }

}
