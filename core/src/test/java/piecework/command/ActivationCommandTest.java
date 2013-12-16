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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import piecework.Constants;
import piecework.authorization.AuthorizationRole;
import piecework.common.OperationResult;
import piecework.engine.ProcessEngineFacade;
import piecework.enumeration.OperationType;
import piecework.exception.PieceworkException;
import piecework.manager.StorageManager;
import piecework.model.Entity;
import piecework.model.Process;
import piecework.model.ProcessDeployment;
import piecework.model.ProcessInstance;

import static org.mockito.Matchers.eq;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class ActivationCommandTest {

    @Mock
    private Entity principal;

    @Mock
    private Process process;

    @Mock
    private ProcessDeployment deployment;

    @Mock
    private ProcessInstance instance;

    @Mock
    private ProcessEngineFacade processEngineFacade;

    @Mock
    private StorageManager storageManager;

    @Test
    public void test() throws PieceworkException {
        // Grant the mock principal access
        Mockito.when(principal.hasRole(process, AuthorizationRole.ADMIN, AuthorizationRole.SUPERUSER))
                .thenReturn(Boolean.TRUE);

        // Ensure that the mock facade returns success
        Mockito.when(processEngineFacade.activate(process, deployment, instance))
                .thenReturn(Boolean.TRUE);

        Mockito.when(instance.getPreviousApplicationStatus())
                .thenReturn("1,2,3");

        String applicationStatusExplanation = "Testing 1,2,3";
        OperationResult expected = new OperationResult(applicationStatusExplanation, "1,2,3", Constants.ProcessStatuses.OPEN, applicationStatusExplanation);

        ActivationCommand command = new ActivationCommand(null, principal, process, deployment, instance, applicationStatusExplanation);
        command.execute(processEngineFacade, storageManager);
        Mockito.verify(storageManager).store(eq(OperationType.ACTIVATION), eq(expected), eq(instance), eq(principal));
    }

}
