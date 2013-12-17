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
import piecework.authorization.AuthorizationRole;
import piecework.engine.ProcessEngineFacade;
import piecework.exception.ForbiddenError;
import piecework.exception.PieceworkException;
import piecework.manager.StorageManager;
import piecework.model.Entity;
import piecework.model.Process;
import piecework.model.ProcessInstance;
import piecework.model.Task;

import static org.mockito.Mockito.*;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class DetachmentCommandTest {

    @Mock
    Entity principal;

    @Mock
    ProcessInstance instance;

    @Mock
    Process process;

    @Mock
    ProcessEngineFacade processEngineFacade;

    @Mock
    StorageManager storageManager;

    @Mock
    Task task;

    private String attachmentId = "TESTATTACHMENT1";

    @Test(expected = ForbiddenError.class)
    public void testAnonymous() throws PieceworkException {
        DetachmentCommand detachment = new DetachmentCommand(null, null, process, instance, task, attachmentId);
        detachment.execute(processEngineFacade, storageManager);
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
        DetachmentCommand detachment = new DetachmentCommand(null, principal, process, instance, task, attachmentId);
        detachment.execute(processEngineFacade, storageManager);
    }

    @Test
    public void testAuthorizedOverseer() throws PieceworkException {
        doReturn(Boolean.TRUE)
                .when(principal)
                .hasRole(process, AuthorizationRole.OVERSEER, AuthorizationRole.SUPERUSER);
        DetachmentCommand detachment = new DetachmentCommand(null, principal, process, instance, task, attachmentId);
        detachment.execute(processEngineFacade, storageManager);

        verify(storageManager).minusAttachment(eq(instance), eq(attachmentId), eq(principal));
    }

}
