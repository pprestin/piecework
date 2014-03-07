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
package piecework.persistence.concrete;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import piecework.exception.*;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.persistence.ProcessProvider;
import piecework.repository.ProcessRepository;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessDeploymentRepositoryProviderTest {

    @Mock
    ProcessRepository processRepository;

    @Mock
    Entity principal;

    @Test(expected= BadRequestError.class)
    public void verifyErrorOnNullKey() throws PieceworkException {
        ProcessProvider processProvider = new ProcessRepositoryProvider(processRepository, null, principal);
        ProcessDeploymentProvider deploymentProvider = new ProcessDeploymentRepositoryProvider(processProvider);
        deploymentProvider.deployment();
    }

    @Test(expected= BadRequestError.class)
    public void verifyErrorOnEmptyKey() throws PieceworkException {
        ProcessProvider processProvider = new ProcessRepositoryProvider(processRepository, "", principal);
        ProcessDeploymentProvider deploymentProvider = new ProcessDeploymentRepositoryProvider(processProvider);
        deploymentProvider.deployment();
    }

    @Test(expected = NotFoundError.class)
    public void verifyErrorOnNotFound() throws PieceworkException {
        piecework.model.Process mockProcess = Mockito.mock(Process.class);
        Mockito.doReturn(mockProcess)
                .when(processRepository).findOne(eq("TEST-1"));
        ProcessProvider processProvider = new ProcessRepositoryProvider(processRepository, "TEST-2", principal);
        ProcessDeploymentProvider deploymentProvider = new ProcessDeploymentRepositoryProvider(processProvider);
        deploymentProvider.deployment();
    }

    @Test(expected = GoneError.class)
    public void verifyErrorOnDeleted() throws PieceworkException {
        Process mockProcess = Mockito.mock(Process.class);
        Mockito.doReturn(true)
                .when(mockProcess).isDeleted();
        Mockito.doReturn(mockProcess)
                .when(processRepository).findOne(eq("TEST"));
        ProcessProvider processProvider = new ProcessRepositoryProvider(processRepository, "TEST", principal);
        ProcessDeploymentProvider deploymentProvider = new ProcessDeploymentRepositoryProvider(processProvider);
        deploymentProvider.deployment();
    }

    @Test(expected = MisconfiguredProcessException.class)
    public void verifyErrorOnNullDeployment() throws PieceworkException {
        Process mockProcess = Mockito.mock(Process.class);
        Mockito.doReturn(mockProcess)
                .when(processRepository).findOne(eq("TEST"));
        ProcessProvider processProvider = new ProcessRepositoryProvider(processRepository, "TEST", principal);
        ProcessDeploymentProvider deploymentProvider = new ProcessDeploymentRepositoryProvider(processProvider);
        deploymentProvider.deployment();
    }

    @Test
    public void verifyDeploymentFromProcess() throws PieceworkException {
        Process mockProcess = Mockito.mock(Process.class);
        ProcessDeployment mockDeployment = Mockito.mock(ProcessDeployment.class);
        Mockito.doReturn(mockProcess)
                .when(processRepository).findOne(eq("TEST"));
        Mockito.doReturn(mockDeployment)
                .when(mockProcess).getDeployment();
        ProcessProvider processProvider = new ProcessRepositoryProvider(processRepository, "TEST", principal);
        ProcessDeploymentProvider deploymentProvider = new ProcessDeploymentRepositoryProvider(processProvider);
        ProcessDeployment deployment = deploymentProvider.deployment();
        Assert.assertEquals(mockDeployment, deployment);
        Mockito.verify(processRepository, times(1)).findOne(eq("TEST"));
        Mockito.verify(mockProcess, times(1)).getDeployment();
    }

    @Test
    public void verifyProcessDefinitionKey() throws PieceworkException {
        Process process = new Process.Builder()
                .processDefinitionKey("TEST")
                .build();
        Mockito.doReturn(process)
                .when(processRepository).findOne(eq("TEST"));
        ProcessProvider processProvider = new ProcessRepositoryProvider(processRepository, process.getProcessDefinitionKey(), principal);
        ProcessDeploymentProvider deploymentProvider = new ProcessDeploymentRepositoryProvider(processProvider);
        Assert.assertEquals("TEST", deploymentProvider.processDefinitionKey());
    }

    @Test
    public void verifyProfile() throws Exception {
        Process mockProcess = Mockito.mock(Process.class);
        ProcessDeployment mockDeployment = Mockito.mock(ProcessDeployment.class);
        Mockito.doReturn(mockProcess)
                .when(processRepository).findOne(eq("TEST"));
        Mockito.doReturn(mockDeployment)
                .when(mockProcess).getDeployment();
        Mockito.doReturn(new ContentProfile.Builder()
                    .baseDirectory("/etc/some/path")
                    .build())
                .when(mockDeployment).getContentProfile();

        ProcessProvider processProvider = new ProcessRepositoryProvider(processRepository, "TEST", principal);
        ProcessDeploymentProvider deploymentProvider = new ProcessDeploymentRepositoryProvider(processProvider);
        ContentProfile contentProfile = deploymentProvider.contentProfile();
        Assert.assertEquals("/etc/some/path", contentProfile.getBaseDirectory());
    }

    @Test
    public void verifyPrincipal() throws PieceworkException {
        Process process = new Process.Builder()
                .processDefinitionKey("TEST")
                .build();
        Mockito.doReturn(process)
                .when(processRepository).findOne(eq("TEST"));
        ProcessProvider processProvider = new ProcessRepositoryProvider(processRepository, process.getProcessDefinitionKey(), principal);
        ProcessDeploymentProvider deploymentProvider = new ProcessDeploymentRepositoryProvider(processProvider);
        Assert.assertEquals(principal, deploymentProvider.principal());
    }

}
