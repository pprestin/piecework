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
import piecework.model.Process;
import piecework.persistence.ModelProviderFactory;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.persistence.ProcessInstanceProvider;
import piecework.persistence.TaskProvider;
import piecework.persistence.test.ProcessDeploymentProviderStub;
import piecework.persistence.test.ProcessInstanceProviderStub;
import piecework.persistence.test.TaskProviderStub;
import piecework.service.RequestService;
import piecework.validation.Validation;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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
    ModelProviderFactory modelProviderFactory;

    @Mock
    ProcessInstance instance;

    @Mock
    Process process;

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

    private ProcessDeploymentProvider deploymentProvider;
    private ProcessInstanceProvider instanceProvider;
    private TaskProvider taskProvider;

    @Before
    public void setup() throws PieceworkException {
        deploymentProvider = new ProcessDeploymentProviderStub(process, deployment, principal);
        instanceProvider = new ProcessInstanceProviderStub(process, deployment, instance, principal);
        taskProvider = new TaskProviderStub(process, deployment, instance, task, principal);


        Mockito.doReturn(instance)
                .when(create).execute();
        Mockito.doReturn(instance)
                .when(complete).execute();
        Mockito.doReturn(create)
                .when(commandFactory).createInstance(any(ProcessDeploymentProvider.class), eq(validation));
        Mockito.doReturn(complete)
                .when(commandFactory).completeTask(eq(taskProvider), eq(validation), eq(ActionType.COMPLETE));

        Mockito.doReturn(taskProvider)
               .when(modelProviderFactory).taskProvider(anyString(), anyString(), eq(principal));
        Mockito.doReturn(deploymentProvider)
                .when(modelProviderFactory).deploymentProvider(anyString(), eq(principal));
        Mockito.doReturn(instanceProvider)
                .when(modelProviderFactory).instanceProvider(anyString(), anyString(), any(Entity.class));
    }

    @Test(expected = ForbiddenError.class)
    public void testAnonymousFailed() throws PieceworkException {
        Mockito.when(process.isAnonymousSubmissionAllowed())
                .thenReturn(Boolean.FALSE);
        ProcessDeploymentProvider deploymentProvider = new ProcessDeploymentProviderStub(process, deployment, null);
        SubmitFormCommand submit = new SubmitFormCommand(null, deploymentProvider, validation, ActionType.CREATE, requestDetails, formRequest);
        submit.execute(commandFactory, modelProviderFactory, requestService, storageManager);
    }

    @Test
    public void testAnonymousSucceededCreate() throws PieceworkException {
        Mockito.when(process.isAnonymousSubmissionAllowed())
                .thenReturn(Boolean.TRUE);

        ProcessDeploymentProvider deploymentProvider = new ProcessDeploymentProviderStub(process, deployment, null);
        SubmitFormCommand submit = new SubmitFormCommand(null, deploymentProvider, validation, ActionType.CREATE, requestDetails, formRequest);
        submit.execute(commandFactory, modelProviderFactory, requestService, storageManager);

        Mockito.verify(requestService).create(requestDetails, instanceProvider, ActionType.COMPLETE);
    }

    @Test(expected = ForbiddenError.class)
    public void testUnauthorizedUser() throws PieceworkException {
        doReturn(Boolean.FALSE)
                .when(principal)
                .hasRole(process, AuthorizationRole.OVERSEER, AuthorizationRole.SUPERUSER);
        doReturn(Boolean.FALSE)
                .when(task)
                .isCandidateOrAssignee(principal);
        doReturn(Boolean.TRUE)
                .when(principal)
                .hasRole(process, AuthorizationRole.USER);

        SubmitFormCommand submit = new SubmitFormCommand(null, deploymentProvider, validation, ActionType.CREATE, requestDetails, formRequest);
        submit.execute(commandFactory, modelProviderFactory, requestService, storageManager);
    }

    @Test
    public void testComplete() throws PieceworkException {
        Mockito.when(process.isAnonymousSubmissionAllowed())
                .thenReturn(Boolean.FALSE);
        doReturn(Boolean.TRUE)
                .when(principal)
                .hasRole(process, AuthorizationRole.INITIATOR);

        SubmitFormCommand submit = new SubmitFormCommand(null, deploymentProvider, validation, ActionType.COMPLETE, requestDetails, formRequest);
        submit.execute(commandFactory, modelProviderFactory, requestService, storageManager);

        Mockito.verify(requestService).create(requestDetails, instanceProvider, ActionType.COMPLETE);
    }

}
