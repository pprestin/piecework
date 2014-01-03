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
import piecework.enumeration.ActionType;
import piecework.exception.ForbiddenError;
import piecework.exception.PieceworkException;
import piecework.manager.StorageManager;
import piecework.model.*;
import piecework.service.RequestService;
import piecework.validation.Validation;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class SubmitFormCommandTest {

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
    public void testAnonymousFailed() throws PieceworkException {
        Mockito.when(process.isAnonymousSubmissionAllowed())
                .thenReturn(Boolean.FALSE);
        SubmitFormCommand submit = new SubmitFormCommand(null, null, deployment, validation, ActionType.CREATE, requestDetails, formRequest);
        submit.execute(commandFactory, requestService);
    }

    @Test
    public void testAnonymousSucceededCreate() throws PieceworkException {
        Mockito.when(process.isAnonymousSubmissionAllowed())
                .thenReturn(Boolean.TRUE);
        SubmitFormCommand submit = new SubmitFormCommand(null, null, deployment, validation, ActionType.CREATE, requestDetails, formRequest);
        submit.execute(commandFactory, requestService);

        Mockito.verify(requestService).create(requestDetails, process, instance, null, ActionType.COMPLETE);
    }

    @Test(expected = ForbiddenError.class)
    public void testUnauthorizedUser() throws PieceworkException {
        Mockito.when(validation.getTask())
                .thenReturn(task);
        doReturn(Boolean.FALSE)
                .when(principal)
                .hasRole(process, AuthorizationRole.OVERSEER, AuthorizationRole.SUPERUSER);
        doReturn(Boolean.FALSE)
                .when(task)
                .isCandidateOrAssignee(principal);
        doReturn(Boolean.TRUE)
                .when(principal)
                .hasRole(process, AuthorizationRole.USER);
        SubmitFormCommand submit = new SubmitFormCommand(null, null, deployment, validation, ActionType.CREATE, requestDetails, formRequest);
        submit.execute(commandFactory, requestService);
    }

    @Test
    public void testComplete() throws PieceworkException {
        Mockito.when(validation.getTask())
                .thenReturn(task);
        Mockito.when(process.isAnonymousSubmissionAllowed())
                .thenReturn(Boolean.FALSE);
        SubmitFormCommand submit = new SubmitFormCommand(null, principal, deployment, validation, ActionType.COMPLETE, requestDetails, formRequest);
        submit.execute(commandFactory, requestService);

        Mockito.verify(requestService).create(requestDetails, process, instance, task, ActionType.COMPLETE);
    }

}
