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
package piecework.command;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import piecework.authorization.AuthorizationRole;
import piecework.engine.ProcessEngineFacade;
import piecework.engine.exception.ProcessEngineException;
import piecework.enumeration.ActionType;
import piecework.exception.ForbiddenError;
import piecework.exception.PieceworkException;
import piecework.manager.StorageManager;
import piecework.model.*;
import piecework.persistence.TaskProvider;
import piecework.persistence.test.TaskProviderStub;
import piecework.validation.Validation;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class SubTaskCommandTest extends TestCase {

    @Mock
    private Entity principal;

    @Mock
    private piecework.model.Process process;

    @Mock
    private ProcessDeployment deployment;

    @Mock
    private ProcessEngineFacade processEngineFacade;

    private ProcessInstance instance;

    @Mock
    private StorageManager storageManager;

    @Mock
    private Task task;

    @Mock
    private User assignee;

    @Mock
    private Validation validation;

    private final String parentTaskId = "1324";

    private TaskProvider taskProvider;

    @Before
    public void setup() throws PieceworkException {

        Mockito.when(task.getTaskInstanceId())
                .thenReturn(parentTaskId);

        this.instance = new ProcessInstance.Builder()
                .processDefinitionKey("TESTPROCESS1")
                .processInstanceId("9876")
                .task(task)
                .build();

        taskProvider = new TaskProviderStub(process, deployment, instance, task, principal);

        Mockito.doReturn(task)
                .when(processEngineFacade)
                .createSubTask(eq(taskProvider), eq(validation));

        Mockito.doReturn(instance)
                .when(storageManager)
                .store(any(ProcessInstance.class));
    }

    @Test
    public void testCreateAsCandidateOrAssignee() throws PieceworkException {
        // Grant the mock principal access
        Mockito.when(principal.hasRole(process, AuthorizationRole.OVERSEER, AuthorizationRole.SUPERUSER))
                .thenReturn(Boolean.FALSE);

        Mockito.when(task.isCandidateOrAssignee(principal))
                .thenReturn(Boolean.TRUE);

        Mockito.when(principal.hasRole(process, AuthorizationRole.USER))
                .thenReturn(Boolean.TRUE);

        Mockito.when(storageManager.store(taskProvider, validation, ActionType.COMPLETE))
                .thenReturn(instance);

        String taskId = "123456";

        Mockito.when(task.getTaskInstanceId())
                .thenReturn(taskId);

        Mockito.when(processEngineFacade.completeTask(process, deployment, taskId, ActionType.COMPLETE, validation, principal))
                .thenReturn(Boolean.TRUE);

        SubTaskCommand command = new SubTaskCommand(null, taskProvider, validation);
        ProcessInstance actual = command.execute(processEngineFacade, storageManager);
        Mockito.verify(storageManager).store(any(ProcessInstance.class));
        Mockito.verify(processEngineFacade).createSubTask(eq(taskProvider), eq(validation));
        Assert.assertEquals(instance, actual);
    }

    @Test
    public void testSaveNotUser() throws PieceworkException {
        // Grant the mock principal access
        Mockito.when(principal.hasRole(process, AuthorizationRole.OVERSEER, AuthorizationRole.SUPERUSER))
                .thenReturn(Boolean.FALSE);

        Mockito.when(task.isCandidateOrAssignee(principal))
                .thenReturn(Boolean.TRUE);

        Mockito.when(principal.hasRole(process, AuthorizationRole.USER))
                .thenReturn(Boolean.FALSE);

        Mockito.when(storageManager.store(taskProvider, validation, ActionType.SAVE))
                .thenReturn(instance);

        SubTaskCommand command = new SubTaskCommand(null, taskProvider, validation);

        try {
            Mockito.when(command.execute(processEngineFacade, storageManager))
                    .thenThrow(ForbiddenError.class);
        } catch (ForbiddenError fe) {
            // Excellent
        }
    }

}
