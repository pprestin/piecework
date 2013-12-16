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

import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import piecework.Constants;
import piecework.authorization.AccessAuthority;
import piecework.authorization.AuthorizationRole;
import piecework.common.OperationResult;
import piecework.engine.ProcessEngineFacade;
import piecework.enumeration.OperationType;
import piecework.exception.PieceworkException;
import piecework.manager.StorageManager;
import piecework.model.*;

import static org.mockito.Matchers.eq;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class CancellationCommandTest {

    @Mock
    private AccessAuthority accessAuthority;

    @Mock
    private piecework.model.Process process;

    @Mock
    private ProcessDeployment deployment;

    @Mock
    private ProcessEngineFacade processEngineFacade;

    @Mock
    private StorageManager storageManager;

    @Test
    public void testCancellationByInitiator() throws PieceworkException {
        ProcessInstance instance = new ProcessInstance.Builder()
                .initiatorId("testuser")
                .build();

        User principal = new User.Builder()
                .userId("testuser")
                .build();

        Mockito.when(deployment.getCancellationStatus())
                .thenReturn("Finito");

        Mockito.when(processEngineFacade.cancel(process, deployment, instance))
                .thenReturn(Boolean.TRUE);

        OperationResult result = new OperationResult("Because of a good reason", "Finito", Constants.ProcessStatuses.CANCELLED, "Because of a good reason");

        Mockito.when(storageManager.store(OperationType.CANCELLATION, result, instance, principal))
                .thenReturn(instance);

        CancellationCommand command = new CancellationCommand(null, principal, process, deployment, instance, "Because of a good reason");
        ProcessInstance actual = command.execute(processEngineFacade, storageManager);

        Mockito.verify(storageManager).store(eq(OperationType.CANCELLATION), eq(result), eq(instance), eq(principal));
        Assert.assertEquals(instance, actual);
    }

    @Test
    public void testCancellationByAdmin() throws PieceworkException {
        Mockito.when(process.getProcessDefinitionKey())
                .thenReturn("TESTPROCESS1");

        ProcessInstance instance = new ProcessInstance.Builder()
                .initiatorId("testuser")
                .build();

        Mockito.doReturn(Boolean.TRUE)
                .when(accessAuthority)
                .hasRole(process, Sets.newHashSet(AuthorizationRole.ADMIN, AuthorizationRole.SUPERUSER));

        User principal = new User.Builder()
                .userId("admin")
                .accessAuthority(accessAuthority)
                .build();

        Mockito.when(deployment.getCancellationStatus())
                .thenReturn("Finito");

        Mockito.when(processEngineFacade.cancel(process, deployment, instance))
                .thenReturn(Boolean.TRUE);

        OperationResult result = new OperationResult("Because of a good reason", "Finito", Constants.ProcessStatuses.CANCELLED, "Because of a good reason");

        Mockito.when(storageManager.store(OperationType.CANCELLATION, result, instance, principal))
                .thenReturn(instance);

        CancellationCommand command = new CancellationCommand(null, principal, process, deployment, instance, "Because of a good reason");
        ProcessInstance actual = command.execute(processEngineFacade, storageManager);
        Mockito.verify(storageManager).store(eq(OperationType.CANCELLATION), eq(result), eq(instance), eq(principal));
        Assert.assertEquals(instance, actual);
    }

}
