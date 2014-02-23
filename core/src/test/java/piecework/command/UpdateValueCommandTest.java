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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import piecework.authorization.AuthorizationRole;
import piecework.engine.ProcessEngineFacade;
import piecework.enumeration.ActionType;
import piecework.exception.ForbiddenError;
import piecework.exception.PieceworkException;
import piecework.manager.StorageManager;
import piecework.model.*;
import piecework.service.RequestService;
import piecework.validation.Validation;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class UpdateValueCommandTest {

    @Mock
    CommandFactory commandFactory;

    @Mock
    CreateInstanceCommand create;

    @Mock
    CompleteTaskCommand complete;

    @Mock
    ProcessDeployment deployment;

    @Mock
    FormRequest formRequest;

    @Mock
    Entity principal;

    @Mock
    ProcessInstance instance;

    @Mock
    piecework.model.Process process;

    @Mock
    private ProcessEngineFacade processEngineFacade;

    @Mock
    RequestDetails requestDetails;

    @Mock
    RequestService requestService;

    @Mock
    StorageManager storageManager;

    @Mock
    Task task;

    @Mock
    Validation validation;

    @Before
    public void setup() throws PieceworkException {
        Mockito.doReturn(instance)
                .when(create).execute();
        Mockito.doReturn(instance)
                .when(complete).execute();
        Mockito.doReturn(create)
                .when(commandFactory).createInstance(principal, validation);
        Mockito.doReturn(create)
                .when(commandFactory).createInstance(null, validation);
        Mockito.doReturn(complete)
                .when(commandFactory).completeTask(principal, deployment, validation, ActionType.COMPLETE);
        Mockito.when(validation.getProcess())
                .thenReturn(process);
        Mockito.when(validation.getInstance())
                .thenReturn(instance);
    }

    @Test(expected = ForbiddenError.class)
    public void testAnonymousFail() throws PieceworkException {
        Mockito.when(validation.getTask())
                .thenReturn(task);
        Mockito.when(process.isAnonymousSubmissionAllowed())
                .thenReturn(Boolean.FALSE);
        UpdateValueCommand update = new UpdateValueCommand(null, null, task, validation);
        update.execute(processEngineFacade, storageManager);
    }

    @Test(expected = ForbiddenError.class)
    public void testUnauthorizedFail() throws PieceworkException {
        Mockito.when(validation.getTask())
                .thenReturn(task);
        Mockito.when(process.isAnonymousSubmissionAllowed())
                .thenReturn(Boolean.FALSE);
        UpdateValueCommand update = new UpdateValueCommand(null, principal, task, validation);
        update.execute(processEngineFacade, storageManager);
    }

    @Test(expected = ForbiddenError.class)
    public void testNotAssignedTaskFail() throws PieceworkException {
        doReturn(Boolean.FALSE)
                .when(principal)
                .hasRole(process, AuthorizationRole.OVERSEER, AuthorizationRole.SUPERUSER);
        doReturn(Boolean.FALSE)
                .when(task)
                .isCandidateOrAssignee(principal);
        doReturn(Boolean.TRUE)
                .when(principal)
                .hasRole(process, AuthorizationRole.USER);

        Mockito.when(validation.getTask())
                .thenReturn(task);
        Mockito.when(process.isAnonymousSubmissionAllowed())
                .thenReturn(Boolean.FALSE);
        UpdateValueCommand update = new UpdateValueCommand(null, principal, task, validation);
        update.execute(processEngineFacade, storageManager);
    }

    @Test
    public void testUserAssignedTaskSuccess() throws PieceworkException {
        doReturn(Boolean.FALSE)
                .when(principal)
                .hasRole(process, AuthorizationRole.OVERSEER, AuthorizationRole.SUPERUSER);
        doReturn(Boolean.TRUE)
                .when(task)
                .isCandidateOrAssignee(principal);
        doReturn(Boolean.TRUE)
                .when(principal)
                .hasRole(process, AuthorizationRole.USER);

        Mockito.when(validation.getTask())
                .thenReturn(task);
        Mockito.when(process.isAnonymousSubmissionAllowed())
                .thenReturn(Boolean.FALSE);
        UpdateValueCommand update = new UpdateValueCommand(null, principal, task, validation);
        update.execute(processEngineFacade, storageManager);

        Mockito.verify(storageManager).store(instanceProvider, eq(validation), eq(ActionType.SAVE));
    }

    @Test
    public void testOverseerSuccess() throws PieceworkException {
        doReturn(Boolean.TRUE)
                .when(principal)
                .hasRole(process, AuthorizationRole.OVERSEER, AuthorizationRole.SUPERUSER);
        doReturn(Boolean.FALSE)
                .when(task)
                .isCandidateOrAssignee(principal);
        doReturn(Boolean.FALSE)
                .when(principal)
                .hasRole(process, AuthorizationRole.USER);

        Mockito.when(validation.getTask())
                .thenReturn(task);
        Mockito.when(process.isAnonymousSubmissionAllowed())
                .thenReturn(Boolean.FALSE);
        UpdateValueCommand update = new UpdateValueCommand(null, principal, task, validation);
        update.execute(processEngineFacade, storageManager);

        Mockito.verify(storageManager).store(instanceProvider, eq(validation), eq(ActionType.SAVE));
    }

}
