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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import piecework.authorization.AuthorizationRole;
import piecework.engine.ProcessEngineFacade;
import piecework.enumeration.ActionType;
import piecework.exception.PieceworkException;
import piecework.manager.StorageManager;
import piecework.model.*;
import piecework.persistence.TaskProvider;
import piecework.persistence.test.TaskProviderStub;
import piecework.validation.Validation;

import static org.mockito.Matchers.eq;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class AttachmentCommandTest {

    @Mock
    private Entity principal;

    @Mock
    private piecework.model.Process process;

    @Mock
    private ProcessDeployment deployment;

    @Mock
    private ProcessEngineFacade processEngineFacade;

    @Mock
    private ProcessInstance instance;

    @Mock
    private StorageManager storageManager;

    @Mock
    private Task task;

    @Mock
    private User assignee;

    @Mock
    private Validation validation;

    @Test
    public void test() throws PieceworkException {
        // Grant the mock principal access
        Mockito.when(principal.hasRole(process, AuthorizationRole.OVERSEER, AuthorizationRole.SUPERUSER))
                .thenReturn(Boolean.FALSE);

        Mockito.when(task.isCandidateOrAssignee(principal))
                .thenReturn(Boolean.TRUE);

        Mockito.when(principal.hasRole(process, AuthorizationRole.USER))
                .thenReturn(Boolean.TRUE);

        TaskProvider taskProvider = new TaskProviderStub(process, deployment, instance, task, principal);

        Mockito.doReturn(instance)
               .when(storageManager).store(eq(taskProvider), eq(validation), eq(ActionType.ATTACH));

        AttachmentCommand command = new AttachmentCommand(null, taskProvider, validation);
        ProcessInstance actual = command.execute(processEngineFacade, storageManager);
        Mockito.verify(storageManager).store(eq(taskProvider), eq(validation), eq(ActionType.ATTACH));
        Assert.assertEquals(instance, actual);
    }

}
