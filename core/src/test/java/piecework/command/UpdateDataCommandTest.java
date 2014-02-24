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
import piecework.common.ManyMap;
import piecework.engine.ProcessEngineFacade;
import piecework.exception.ForbiddenError;
import piecework.exception.MisconfiguredProcessException;
import piecework.exception.PieceworkException;
import piecework.manager.StorageManager;
import piecework.model.*;
import piecework.persistence.AllowedTaskProvider;
import piecework.persistence.test.AllowedTaskProviderStub;
import piecework.validation.Validation;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class UpdateDataCommandTest {

    @Mock
    private Entity principal;

    @Mock
    private piecework.model.Process process;

    @Mock
    private ProcessDeployment deployment;

    @Mock
    private ProcessInstance instance;

    @Mock
    private ProcessEngineFacade processEngineFacade;

    @Mock
    private StorageManager storageManager;

    @Mock
    private Submission submission;

    @Mock
    private Task task;

    @Mock
    private User assignee;

    @Mock
    private Validation validation;

    private ManyMap<String, Value> data;
    private ManyMap<String, Message> messages;
    private List<Attachment> attachments;
    private String applicationStatusExplanation;

    @Before
    public void setup() {
        data = new ManyMap<String, Value>();
        attachments = new ArrayList<Attachment>();

        Mockito.when(validation.getSubmission())
                .thenReturn(submission);

        Mockito.when(validation.getData())
                .thenReturn(data);

        Mockito.when(validation.getAttachments())
                .thenReturn(attachments);
    }

    @Test(expected = ForbiddenError.class)
    public void testUserNotAssignedTask() throws PieceworkException {
        doReturn(Boolean.FALSE)
                .when(principal)
                .hasRole(process, AuthorizationRole.OVERSEER, AuthorizationRole.SUPERUSER);
        doReturn(Boolean.FALSE)
                .when(task)
                .isCandidateOrAssignee(principal);
        doReturn(Boolean.TRUE)
                .when(principal)
                .hasRole(process, AuthorizationRole.USER);

        AllowedTaskProvider allowedTaskProvider = new AllowedTaskProviderStub(process, deployment, instance, task, principal);
        new UpdateDataCommand(null, allowedTaskProvider, data, messages, applicationStatusExplanation)
                .execute(processEngineFacade, storageManager);
    }

    @Test
    public void testOverseer() throws PieceworkException {
        doReturn(Boolean.TRUE)
                .when(principal)
                .hasRole(process, AuthorizationRole.OVERSEER, AuthorizationRole.SUPERUSER);
        doReturn(Boolean.FALSE)
                .when(task)
                .isCandidateOrAssignee(principal);
        doReturn(Boolean.FALSE)
                .when(principal)
                .hasRole(process, AuthorizationRole.USER);

        AllowedTaskProvider allowedTaskProvider = new AllowedTaskProviderStub(process, deployment, instance, task, principal);
        new UpdateDataCommand(null, allowedTaskProvider, data, messages, applicationStatusExplanation)
                .execute(processEngineFacade, storageManager);
    }

    @Test
    public void testUserIsAssignedTask() throws PieceworkException {
        doReturn(Boolean.FALSE)
                .when(principal)
                .hasRole(process, AuthorizationRole.OVERSEER, AuthorizationRole.SUPERUSER);
        doReturn(Boolean.TRUE)
                .when(task)
                .isCandidateOrAssignee(principal);
        doReturn(Boolean.TRUE)
                .when(principal)
                .hasRole(process, AuthorizationRole.USER);

        AllowedTaskProvider allowedTaskProvider = new AllowedTaskProviderStub(process, deployment, instance, task, principal);
        new UpdateDataCommand(null, allowedTaskProvider, data, messages, applicationStatusExplanation)
                .execute(processEngineFacade, storageManager);
    }

    @Test(expected = ForbiddenError.class)
    public void testAnonymousFail() throws PieceworkException {
        AllowedTaskProvider allowedTaskProvider = new AllowedTaskProviderStub(process, deployment, instance, task, null);
        new UpdateDataCommand(null, allowedTaskProvider, data, messages, applicationStatusExplanation)
                .execute(processEngineFacade, storageManager);
    }

    @Test
    public void testSucceed() throws PieceworkException {
        doReturn(Boolean.TRUE)
                .when(principal)
                .hasRole(process, AuthorizationRole.OVERSEER, AuthorizationRole.SUPERUSER);

        Mockito.when(process.getProcessDefinitionKey())
                .thenReturn("TESTPROCESS1");

        Mockito.when(process.getDeployment())
                .thenReturn(deployment);

        Mockito.when(process.isAnonymousSubmissionAllowed())
                .thenReturn(Boolean.TRUE);

        Mockito.when(instance.getProcessInstanceId())
                .thenReturn("1234");

        Mockito.when(storageManager.create(process, deployment, data, attachments, submission, null))
                .thenReturn(instance);

        Mockito.when(processEngineFacade.start(process, deployment, instance))
                .thenReturn("9876");

        Mockito.doReturn(instance)
                .when(storageManager).store(eq(instance), eq(data), eq(messages), any(Submission.class), eq(applicationStatusExplanation));

        AllowedTaskProvider allowedTaskProvider = new AllowedTaskProviderStub(process, deployment, instance, task, principal);
        new UpdateDataCommand(null, allowedTaskProvider, data, messages, applicationStatusExplanation)
                .execute(processEngineFacade, storageManager);

        Mockito.verify(storageManager).store(eq(instance), eq(data), eq(messages), any(Submission.class), eq(applicationStatusExplanation));
    }


}
