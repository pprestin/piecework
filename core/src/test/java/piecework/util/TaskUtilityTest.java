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
package piecework.util;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import piecework.authorization.AuthorizationRole;
import piecework.common.ViewContext;
import piecework.model.Process;
import piecework.model.ProcessInstance;
import piecework.model.Task;
import piecework.model.User;
import piecework.settings.UserInterfaceSettings;

import static org.mockito.Matchers.eq;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class TaskUtilityTest {

    private final static String TEST_TASK_ID = "123";
    private final static String TEST_ALTERNATIVE_TASK_ID = "987";
    private final static String TEST_USER_ID = "testuser";

    @Mock
    User principal;

    private Process process;
    private UserInterfaceSettings settings = new UserInterfaceSettings();

    @Before
    public void setup() {
        this.process = new Process.Builder()
                .processDefinitionKey("TEST")
                .build();

        Mockito.doReturn(TEST_USER_ID)
                .when(principal).getEntityId();
        Mockito.doReturn(TEST_USER_ID)
               .when(principal).getUserId();
    }

    @Test
    public void verifyInstanceNull() throws Exception {
        ProcessInstance instance = null;
        Task task = TaskUtility.findTask(null, process, instance, TEST_TASK_ID, principal, true, new ViewContext(settings, "v0"));
        Assert.assertNull(task);
    }

    @Test
    public void verifyTasksEmpty() throws Exception {
        ProcessInstance instance = new ProcessInstance.Builder()
                .processDefinitionKey(process.getProcessDefinitionKey())
                .build();
        Task task = TaskUtility.findTask(null, process, instance, TEST_TASK_ID, principal, true, new ViewContext(settings, "v0"));
        Assert.assertNull(task);
    }

    @Test
    public void verifyActiveTaskForOversightRole() throws Exception {
        Mockito.doReturn(Boolean.TRUE)
               .when(principal).hasRole(eq(process), eq(AuthorizationRole.OVERSEER));

        ProcessInstance instance = new ProcessInstance.Builder()
                .processDefinitionKey(process.getProcessDefinitionKey())
                .task(new Task.Builder()
                        .taskInstanceId(TEST_TASK_ID)
                        .active()
                        .build())
                .build();
        Task task = TaskUtility.findTask(null, process, instance, TEST_TASK_ID, principal, true, new ViewContext(settings, "v0"));
        Assert.assertNotNull(task);
        Assert.assertEquals(TEST_TASK_ID, task.getTaskInstanceId());
    }

    @Test
    public void verifyNoActiveTaskForUserRole() throws Exception {
        Mockito.doReturn(Boolean.FALSE)
                .when(principal).hasRole(eq(process), eq(AuthorizationRole.OVERSEER));
        Mockito.doReturn(Boolean.TRUE)
                .when(principal).hasRole(eq(process), eq(AuthorizationRole.USER));

        ProcessInstance instance = new ProcessInstance.Builder()
                .processDefinitionKey(process.getProcessDefinitionKey())
                .task(new Task.Builder()
                        .taskInstanceId(TEST_TASK_ID)
                        .active()
                        .build())
                .build();
        Task task = TaskUtility.findTask(null, process, instance, TEST_TASK_ID, principal, true, null);
        Assert.assertNull(task);
    }

    @Test
    public void verifyActiveTaskForCandidateAssigneeWithUserRole() throws Exception {
        Mockito.doReturn(Boolean.FALSE)
                .when(principal).hasRole(eq(process), eq(AuthorizationRole.OVERSEER));
        Mockito.doReturn(Boolean.TRUE)
                .when(principal).hasRole(eq(process), eq(AuthorizationRole.USER));

        ProcessInstance instance = new ProcessInstance.Builder()
                .processDefinitionKey(process.getProcessDefinitionKey())
                .task(new Task.Builder()
                        .taskInstanceId(TEST_TASK_ID)
                        .candidateAssigneeId(TEST_USER_ID)
                        .active()
                        .build())
                .build();
        Task task = TaskUtility.findTask(null, process, instance, TEST_TASK_ID, principal, true, null);
        Assert.assertNotNull(task);
        Assert.assertEquals(TEST_TASK_ID, task.getTaskInstanceId());
    }

    @Test
    public void verifyNoInactiveTaskForCandidateAssigneeWithUserRole() throws Exception {
        Mockito.doReturn(Boolean.FALSE)
                .when(principal).hasRole(eq(process), eq(AuthorizationRole.OVERSEER));
        Mockito.doReturn(Boolean.TRUE)
                .when(principal).hasRole(eq(process), eq(AuthorizationRole.USER));

        ProcessInstance instance = new ProcessInstance.Builder()
                .processDefinitionKey(process.getProcessDefinitionKey())
                .task(new Task.Builder()
                        .taskInstanceId(TEST_TASK_ID)
                        .candidateAssigneeId(TEST_USER_ID)
                        .build())
                .build();
        Task task = TaskUtility.findTask(null, process, instance, TEST_TASK_ID, principal, true, null);
        Assert.assertNull(task);
    }

    @Test
    public void verifyInactiveTaskIfNotLimitedForCandidateAssigneeWithUserRole() throws Exception {
        Mockito.doReturn(Boolean.FALSE)
                .when(principal).hasRole(eq(process), eq(AuthorizationRole.OVERSEER));
        Mockito.doReturn(Boolean.TRUE)
                .when(principal).hasRole(eq(process), eq(AuthorizationRole.USER));

        ProcessInstance instance = new ProcessInstance.Builder()
                .processDefinitionKey(process.getProcessDefinitionKey())
                .task(new Task.Builder()
                        .taskInstanceId(TEST_TASK_ID)
                        .candidateAssigneeId(TEST_USER_ID)
                        .build())
                .build();
        Task task = TaskUtility.findTask(null, process, instance, TEST_TASK_ID, principal, false, null);
        Assert.assertNotNull(task);
        Assert.assertEquals(TEST_TASK_ID, task.getTaskInstanceId());
    }

    @Test
    public void verifyAlwaysPicksSpecifiedTaskEvenIfActiveAlternative() throws Exception {
        Mockito.doReturn(Boolean.FALSE)
                .when(principal).hasRole(eq(process), eq(AuthorizationRole.OVERSEER));
        Mockito.doReturn(Boolean.TRUE)
                .when(principal).hasRole(eq(process), eq(AuthorizationRole.USER));

        ProcessInstance instance = new ProcessInstance.Builder()
                .processDefinitionKey(process.getProcessDefinitionKey())
                .task(new Task.Builder()
                        .taskInstanceId(TEST_TASK_ID)
                        .candidateAssigneeId(TEST_USER_ID)
                        .build())
                .task(new Task.Builder()
                        .taskInstanceId(TEST_ALTERNATIVE_TASK_ID)
                        .candidateAssigneeId(TEST_USER_ID)
                        .active()
                        .build())
                .build();
        Task task = TaskUtility.findTask(null, process, instance, TEST_TASK_ID, principal, false, null);
        Assert.assertNotNull(task);
        Assert.assertEquals(TEST_TASK_ID, task.getTaskInstanceId());
    }

    @Test
    public void verifyPicksNothingIfSpecifiedTaskInactiveWithLimit() throws Exception {
        Mockito.doReturn(Boolean.FALSE)
                .when(principal).hasRole(eq(process), eq(AuthorizationRole.OVERSEER));
        Mockito.doReturn(Boolean.TRUE)
                .when(principal).hasRole(eq(process), eq(AuthorizationRole.USER));

        ProcessInstance instance = new ProcessInstance.Builder()
                .processDefinitionKey(process.getProcessDefinitionKey())
                .task(new Task.Builder()
                        .taskInstanceId(TEST_TASK_ID)
                        .candidateAssigneeId(TEST_USER_ID)
                        .build())
                .task(new Task.Builder()
                        .taskInstanceId(TEST_ALTERNATIVE_TASK_ID)
                        .candidateAssigneeId(TEST_USER_ID)
                        .active()
                        .build())
                .build();
        Task task = TaskUtility.findTask(null, process, instance, TEST_TASK_ID, principal, true, null);
        Assert.assertNull(task);
    }

    @Test
    public void verifyPicksFirstActiveWithLimitAndNoIdSpecified() throws Exception {
        Mockito.doReturn(Boolean.FALSE)
                .when(principal).hasRole(eq(process), eq(AuthorizationRole.OVERSEER));
        Mockito.doReturn(Boolean.TRUE)
                .when(principal).hasRole(eq(process), eq(AuthorizationRole.USER));

        ProcessInstance instance = new ProcessInstance.Builder()
                .processDefinitionKey(process.getProcessDefinitionKey())
                .task(new Task.Builder()
                        .taskInstanceId(TEST_TASK_ID)
                        .candidateAssigneeId(TEST_USER_ID)
                        .build())
                .task(new Task.Builder()
                        .taskInstanceId(TEST_ALTERNATIVE_TASK_ID)
                        .candidateAssigneeId(TEST_USER_ID)
                        .active()
                        .build())
                .build();
        Task task = TaskUtility.findTask(null, process, instance, null, principal, true, null);
        Assert.assertNotNull(task);
        Assert.assertEquals(TEST_ALTERNATIVE_TASK_ID, task.getTaskInstanceId());
    }

    @Test
    public void verifyPicksFirstActiveWithoutLimitAndNoIdSpecified() throws Exception {
        Mockito.doReturn(Boolean.FALSE)
                .when(principal).hasRole(eq(process), eq(AuthorizationRole.OVERSEER));
        Mockito.doReturn(Boolean.TRUE)
                .when(principal).hasRole(eq(process), eq(AuthorizationRole.USER));

        ProcessInstance instance = new ProcessInstance.Builder()
                .processDefinitionKey(process.getProcessDefinitionKey())
                .task(new Task.Builder()
                        .taskInstanceId(TEST_TASK_ID)
                        .candidateAssigneeId(TEST_USER_ID)
                        .build())
                .task(new Task.Builder()
                        .taskInstanceId(TEST_ALTERNATIVE_TASK_ID)
                        .candidateAssigneeId(TEST_USER_ID)
                        .active()
                        .build())
                .build();
        Task task = TaskUtility.findTask(null, process, instance, null, principal, false, null);
        Assert.assertNotNull(task);
        Assert.assertEquals(TEST_ALTERNATIVE_TASK_ID, task.getTaskInstanceId());
    }

    @Test
    public void verifyPicksFirstActiveWithoutLimitAndNoIdSpecifiedInOtherOrder() throws Exception {
        Mockito.doReturn(Boolean.FALSE)
                .when(principal).hasRole(eq(process), eq(AuthorizationRole.OVERSEER));
        Mockito.doReturn(Boolean.TRUE)
                .when(principal).hasRole(eq(process), eq(AuthorizationRole.USER));

        ProcessInstance instance = new ProcessInstance.Builder()
                .processDefinitionKey(process.getProcessDefinitionKey())
                .task(new Task.Builder()
                        .taskInstanceId(TEST_ALTERNATIVE_TASK_ID)
                        .candidateAssigneeId(TEST_USER_ID)
                        .active()
                        .build())
                .task(new Task.Builder()
                        .taskInstanceId(TEST_TASK_ID)
                        .candidateAssigneeId(TEST_USER_ID)
                        .build())
                .build();
        Task task = TaskUtility.findTask(null, process, instance, null, principal, false, null);
        Assert.assertNotNull(task);
        Assert.assertEquals(TEST_ALTERNATIVE_TASK_ID, task.getTaskInstanceId());
    }

}
