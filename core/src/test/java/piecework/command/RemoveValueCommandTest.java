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
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.manager.StorageManager;
import piecework.model.*;
import piecework.common.ManyMap;

import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class RemoveValueCommandTest {

    @Mock
    Entity principal;

    @Mock
    ProcessInstance instance;

    @Mock
    piecework.model.Process process;

    @Mock
    ProcessEngineFacade processEngineFacade;

    @Mock
    StorageManager storageManager;

    @Mock
    Task task;

    private String fieldName = "VARIABLEATTACHMENT1";
    private String valueId = "TESTVALUE1";

    @Test(expected = ForbiddenError.class)
    public void testAnonymous() throws PieceworkException {
        RemoveValueCommand remove = new RemoveValueCommand(null, null, process, instance, task, fieldName, valueId);
        remove.execute(processEngineFacade, storageManager);
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
        RemoveValueCommand remove = new RemoveValueCommand(null, principal, process, instance, task, fieldName, valueId);
        remove.execute(processEngineFacade, storageManager);
    }

    @Test(expected = NotFoundError.class)
    public void testNotFound() throws PieceworkException {
        ManyMap<String, Value> data = new ManyMap<String, Value>();
        Mockito.when(instance.getData())
                .thenReturn(data);
        doReturn(Boolean.TRUE)
                .when(principal)
                .hasRole(process, AuthorizationRole.OVERSEER, AuthorizationRole.SUPERUSER);
        RemoveValueCommand remove = new RemoveValueCommand(null, principal, process, instance, task, fieldName, valueId);
        remove.execute(processEngineFacade, storageManager);
        verify(storageManager).store(eq(instance), any(Map.class), any(Submission.class));
    }

    @Test
    public void testFound() throws PieceworkException {
        ManyMap<String, Value> data = new ManyMap<String, Value>();
        data.putOne(fieldName, new File.Builder().id(valueId).build());
        Mockito.when(instance.getData())
                .thenReturn(data);
        doReturn(Boolean.TRUE)
                .when(principal)
                .hasRole(process, AuthorizationRole.OVERSEER, AuthorizationRole.SUPERUSER);
        RemoveValueCommand remove = new RemoveValueCommand(null, principal, process, instance, task, fieldName, valueId);
        remove.execute(processEngineFacade, storageManager);
        verify(storageManager).store(eq(instance), any(Map.class), any(Submission.class));
    }
}
