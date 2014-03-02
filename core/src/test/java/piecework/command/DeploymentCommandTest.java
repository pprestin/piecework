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
import piecework.content.ContentResource;
import piecework.engine.ProcessDeploymentResource;
import piecework.engine.ProcessEngineFacade;
import piecework.exception.BadRequestError;
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.persistence.ProcessProvider;
import piecework.persistence.concrete.ProcessDeploymentRepositoryProvider;
import piecework.persistence.test.ProcessProviderStub;
import piecework.repository.ActivityRepository;
import piecework.repository.ContentRepository;
import piecework.repository.DeploymentRepository;
import piecework.repository.ProcessRepository;

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
    ContentResource resource;

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
        doReturn(processDeployment)
                .when(deploymentRepository).save(any(ProcessDeployment.class));
    }

    @Test(expected = NotFoundError.class)
    public void testNotFound() throws PieceworkException {
        ProcessProvider processProvider = new ProcessProviderStub(process, principal);
        ProcessDeploymentProvider modelProvider = new ProcessDeploymentRepositoryProvider(deploymentRepository, processProvider, deploymentId);
        DeploymentCommand deployment = new DeploymentCommand(null, modelProvider, deploymentId, resource);
        deployment.execute(activityRepository, contentRepository, deploymentRepository, facade,
                processRepository, uuidGenerator);
    }

    @Test(expected = BadRequestError.class)
    public void testBadRequest() throws PieceworkException {
        when(deploymentRepository.findOne(deploymentId))
                .thenReturn(processDeployment);
        when(facade.deploy(any(Process.class), any(ProcessDeployment.class), any(ContentResource.class)))
                .thenReturn(processDeployment);

        ProcessProvider processProvider = new ProcessProviderStub(process, principal);
        ProcessDeploymentProvider modelProvider = new ProcessDeploymentRepositoryProvider(deploymentRepository, processProvider, deploymentId);
        DeploymentCommand deployment = new DeploymentCommand(null, modelProvider, deploymentId, resource);
        deployment.execute(activityRepository, contentRepository, deploymentRepository, facade,
                processRepository, uuidGenerator);
    }

    @Test
    public void test() throws PieceworkException, IOException {
        when(deploymentRepository.findOne(deploymentId))
                .thenReturn(processDeployment);
        when(resource.contentType())
                .thenReturn("text/xml");
        when(resource.getInputStream())
                .thenReturn(new ByteArrayInputStream("Test".getBytes()));
        when(resource.getFilename())
                .thenReturn("TESTRESOURCE1");
        when(facade.deploy(any(Process.class), any(ProcessDeployment.class), any(ContentResource.class)))
                .thenReturn(processDeployment);

        ProcessProvider processProvider = new ProcessProviderStub(process, principal);
        ProcessDeploymentProvider modelProvider = new ProcessDeploymentRepositoryProvider(deploymentRepository, processProvider, deploymentId);
        DeploymentCommand deployment = new DeploymentCommand(null, modelProvider, deploymentId, resource);
        deployment.execute(activityRepository, contentRepository, deploymentRepository, facade,
                processRepository, uuidGenerator);

        verify(contentRepository).save(eq(modelProvider), any(ContentResource.class));
        verify(facade).deploy(eq(process), eq(processDeployment), any(ContentResource.class));
        verify(processRepository).save(any(Process.class));
    }

}
