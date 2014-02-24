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
import piecework.exception.ForbiddenError;
import piecework.exception.MisconfiguredProcessException;
import piecework.exception.PieceworkException;
import piecework.manager.StorageManager;
import piecework.model.*;
import piecework.common.ManyMap;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.persistence.ProcessInstanceProvider;
import piecework.persistence.test.ProcessDeploymentProviderStub;
import piecework.persistence.test.ProcessInstanceProviderStub;
import piecework.validation.Validation;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.eq;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class CreateInstanceCommandTest {

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
    private List<Attachment> attachments;

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

    @Test(expected = MisconfiguredProcessException.class)
    public void testMisconfiguredNoProcess() throws PieceworkException {
        ProcessDeploymentProvider deploymentProvider = new ProcessDeploymentProviderStub(null, deployment, principal);

        new CreateInstanceCommand(null, deploymentProvider, validation)
                .execute(processEngineFacade, storageManager);
    }

    @Test(expected = MisconfiguredProcessException.class)
    public void testMisconfiguredNoProcessDefinitionKey() throws PieceworkException {
        Mockito.when(process.getProcessDefinitionKey())
                .thenReturn(null);

        ProcessDeploymentProvider deploymentProvider = new ProcessDeploymentProviderStub(process, deployment, principal);

        new CreateInstanceCommand(null, deploymentProvider, validation)
                .execute(processEngineFacade, storageManager);
    }

    @Test(expected = MisconfiguredProcessException.class)
    public void testMisconfiguredNoDeployment() throws PieceworkException {
        Mockito.when(process.getProcessDefinitionKey())
                .thenReturn("TESTPROCESS1");

        ProcessDeploymentProvider deploymentProvider = new ProcessDeploymentProviderStub(process, null, principal);

        new CreateInstanceCommand(null, deploymentProvider, validation)
                .execute(processEngineFacade, storageManager);
    }

    @Test(expected = ForbiddenError.class)
    public void testCreateAnonymousFail() throws PieceworkException {
        Mockito.when(process.getProcessDefinitionKey())
                .thenReturn("TESTPROCESS1");

        Mockito.when(process.isAnonymousSubmissionAllowed())
                .thenReturn(Boolean.FALSE);

        ProcessDeploymentProvider deploymentProvider = new ProcessDeploymentProviderStub(process, deployment, principal);

        new CreateInstanceCommand(null, deploymentProvider, validation)
                .execute(processEngineFacade, storageManager);
    }

    @Test
    public void testCreateAnonymousSucceed() throws PieceworkException {
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

        Mockito.when(storageManager.store("1234", "9876"))
                .thenReturn(Boolean.TRUE);

        ProcessDeploymentProvider deploymentProvider = new ProcessDeploymentProviderStub(process, deployment, null);

        new CreateInstanceCommand(null, deploymentProvider, validation)
                .execute(processEngineFacade, storageManager);

        Mockito.verify(storageManager).store(eq("1234"), eq("9876"));
    }

    @Test(expected = ForbiddenError.class)
    public void testCreateUnauthorizedInitiatorFail() throws PieceworkException {
        Mockito.when(process.getProcessDefinitionKey())
                .thenReturn("TESTPROCESS1");

        Mockito.when(process.getDeployment())
                .thenReturn(deployment);

        Mockito.when(process.isAnonymousSubmissionAllowed())
                .thenReturn(Boolean.FALSE);

        Mockito.when(instance.getProcessInstanceId())
                .thenReturn("1234");

        Mockito.when(storageManager.create(process, deployment, data, attachments, submission, null))
                .thenReturn(instance);

        Mockito.when(processEngineFacade.start(process, deployment, instance))
                .thenReturn("9876");

        Mockito.when(storageManager.store("1234", "9876"))
                .thenReturn(Boolean.TRUE);

        Mockito.when(principal.hasRole(process, AuthorizationRole.INITIATOR))
                .thenReturn(Boolean.FALSE);

        ProcessDeploymentProvider deploymentProvider = new ProcessDeploymentProviderStub(process, deployment, principal);

        new CreateInstanceCommand(null, deploymentProvider, validation)
                .execute(processEngineFacade, storageManager);
    }

    @Test
    public void testAuthorizedInitiatorSucceed() throws PieceworkException {

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

        Mockito.when(storageManager.store("1234", "9876"))
                .thenReturn(Boolean.TRUE);

        Mockito.when(principal.hasRole(process, AuthorizationRole.INITIATOR))
                .thenReturn(Boolean.TRUE);

        ProcessDeploymentProvider deploymentProvider = new ProcessDeploymentProviderStub(process, deployment, principal);

        new CreateInstanceCommand(null, deploymentProvider, validation)
                .execute(processEngineFacade, storageManager);

        Mockito.verify(storageManager).store(eq("1234"), eq("9876"));
    }
}
