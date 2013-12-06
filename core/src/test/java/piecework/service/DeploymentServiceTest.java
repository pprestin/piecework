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
package piecework.service;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import piecework.model.Process;
import piecework.model.ProcessDeployment;
import piecework.model.ProcessInstance;
import piecework.persistence.DeploymentRepository;
import piecework.security.Sanitizer;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class DeploymentServiceTest {

    private final String TEST_DEPLOYMENT_FROM_PROCESS_ID = "123";
    private final String TEST_DEPLOYMENT_FROM_REPO_ID = "456";

    @InjectMocks
    DeploymentService deploymentService;

    @Mock
    DeploymentRepository deploymentRepository;

    @Mock
    Process process;

    @Mock
    ProcessInstance instance;

    @Mock
    ProcessDeployment deploymentFromProcess;

    @Mock
    ProcessDeployment deploymentFromRepository;

    @Mock
    Sanitizer sanitizer;

    @Before
    public void setup() {
        // Mock deployment should always return it's id
        Mockito.when(deploymentFromProcess.getDeploymentId()).thenReturn(TEST_DEPLOYMENT_FROM_PROCESS_ID);
        Mockito.when(process.getDeployment()).thenReturn(deploymentFromProcess);
        Mockito.when(deploymentFromRepository.getDeploymentId()).thenReturn(TEST_DEPLOYMENT_FROM_REPO_ID);
        Mockito.when(deploymentRepository.findOne(TEST_DEPLOYMENT_FROM_REPO_ID)).thenReturn(deploymentFromRepository);
        Mockito.when(instance.getDeploymentId()).thenReturn(TEST_DEPLOYMENT_FROM_REPO_ID);
    }

    @Test
    public void testReadNoInstance() throws Exception {
        ProcessDeployment deployment = deploymentService.read(process, (ProcessInstance)null);

        Assert.assertNotNull(deployment);
        Assert.assertEquals(TEST_DEPLOYMENT_FROM_PROCESS_ID, deployment.getDeploymentId());

        Mockito.verifyZeroInteractions(deploymentRepository);
    }

    @Test
    public void testReadFromRepository() throws Exception {
        ProcessDeployment deployment = deploymentService.read(process, instance);

        Assert.assertNotNull(deployment);
        Assert.assertEquals(TEST_DEPLOYMENT_FROM_REPO_ID, deployment.getDeploymentId());

        Mockito.verify(deploymentRepository).findOne(TEST_DEPLOYMENT_FROM_REPO_ID);
    }

}
