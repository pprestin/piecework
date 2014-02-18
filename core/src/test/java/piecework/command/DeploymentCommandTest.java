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
import org.mockito.runners.MockitoJUnitRunner;
import piecework.common.UuidGenerator;
import piecework.engine.ProcessDeploymentResource;
import piecework.engine.ProcessEngineFacade;
import piecework.exception.BadRequestError;
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ActivityRepository;
import piecework.persistence.ContentRepository;
import piecework.persistence.DeploymentRepository;
import piecework.persistence.ProcessRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class DeploymentCommandTest {

    @Mock
    ActivityRepository activityRepository;

    @Mock
    ContentRepository contentRepository;

    @Mock
    DeploymentRepository deploymentRepository;

    @Mock
    Entity principal;

    @Mock
    ProcessEngineFacade facade;

    @Mock
    ProcessRepository processRepository;

    @Mock
    UuidGenerator uuidGenerator;

    @Mock
    Process process;

    @Mock
    ProcessDeploymentResource resource;

    private ProcessDeployment processDeployment;
    private String deploymentId = "TESTDEPLOYMENT1";

    @Before
    public void setup() {
        this.processDeployment = new ProcessDeployment.Builder()
                .deploymentId(deploymentId)
                .deploymentLabel("Testing 1,2,3")
                .deploymentVersion("1")
                .build();
        List<ProcessDeploymentVersion> versions = new ArrayList<ProcessDeploymentVersion>();
        versions.add(new ProcessDeploymentVersion(this.processDeployment));
        when(process.getProcessDefinitionKey())
                .thenReturn("TESTPROCESS1");
        when(process.getVersions())
                .thenReturn(versions);
    }

    @Test(expected = NotFoundError.class)
    public void testNotFound() throws PieceworkException {
        DeploymentCommand deployment = new DeploymentCommand(null, process, deploymentId, resource);
        deployment.execute(activityRepository, contentRepository, deploymentRepository, facade,
                processRepository, uuidGenerator);
    }

    @Test(expected = BadRequestError.class)
    public void testBadRequest() throws PieceworkException {
        when(deploymentRepository.findOne(deploymentId))
                .thenReturn(processDeployment);
        DeploymentCommand deployment = new DeploymentCommand(null, process, deploymentId, resource);
        deployment.execute(activityRepository, contentRepository, deploymentRepository, facade,
                processRepository, uuidGenerator);
    }

    @Test
    public void test() throws PieceworkException, IOException {
        when(deploymentRepository.findOne(deploymentId))
                .thenReturn(processDeployment);
        when(resource.getContentType())
                .thenReturn("text/xml");
        when(resource.getInputStream())
                .thenReturn(new ByteArrayInputStream("Test".getBytes()));
        when(resource.getName())
                .thenReturn("TESTRESOURCE1");
        when(facade.deploy(any(Process.class), any(ProcessDeployment.class), any(Content.class)))
                .thenReturn(processDeployment);
        DeploymentCommand deployment = new DeploymentCommand(null, process, deploymentId, resource);
        deployment.execute(activityRepository, contentRepository, deploymentRepository, facade,
                processRepository, uuidGenerator);

        verify(contentRepository).save(eq(process), any(ProcessInstance.class), any(Content.class), any(Entity.class));
        verify(facade).deploy(eq(process), eq(processDeployment), any(Content.class));
        verify(processRepository).save(any(Process.class));
    }

}
