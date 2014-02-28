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
import piecework.exception.ForbiddenError;
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.persistence.test.ProcessDeploymentProviderStub;
import piecework.repository.ActivityRepository;
import piecework.repository.ContentRepository;
import piecework.repository.DeploymentRepository;
import piecework.repository.ProcessRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class PublicationCommandTest {

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
    piecework.model.Process process;

    @Mock
    ProcessDeploymentResource resource;

    @Mock
    ProcessDeployment deployment;

    private String deploymentId = "TESTDEPLOYMENT1";

    @Before
    public void setup() {
        ProcessDeployment processDeployment = new ProcessDeployment.Builder()
                .deploymentId(deploymentId)
                .deploymentLabel("Testing 1,2,3")
                .deploymentVersion("1")
                .build();
        List<ProcessDeploymentVersion> versions = new ArrayList<ProcessDeploymentVersion>();
        versions.add(new ProcessDeploymentVersion(processDeployment));
        when(process.getProcessDefinitionKey())
                .thenReturn("TESTPROCESS1");
        when(process.getVersions())
                .thenReturn(versions);
    }

    @Test(expected = NotFoundError.class)
    public void testNotFound() throws PieceworkException {
        ProcessDeploymentProvider deploymentProvider = new ProcessDeploymentProviderStub(process, deployment, principal);
        PublicationCommand publication = new PublicationCommand(null, deploymentProvider, deploymentId);
        publication.execute(deploymentRepository, processRepository);
    }

    @Test(expected = ForbiddenError.class)
    public void testNotDeployed() throws PieceworkException, IOException {
        doReturn(Boolean.FALSE)
                .when(deployment).isDeployed();
        when(deploymentRepository.findOne(deploymentId))
                .thenReturn(deployment);
        when(resource.getContentType())
                .thenReturn("text/xml");
        when(resource.getInputStream())
                .thenReturn(new ByteArrayInputStream("Test".getBytes()));
        when(resource.getName())
                .thenReturn("TESTRESOURCE1");
        when(facade.deploy(any(Process.class), any(ProcessDeployment.class), any(Content.class)))
                .thenReturn(deployment);

        ProcessDeploymentProvider deploymentProvider = new ProcessDeploymentProviderStub(process, deployment, principal);
        PublicationCommand publication = new PublicationCommand(null, deploymentProvider, deploymentId);
        publication.execute(deploymentRepository, processRepository);

        verify(contentRepository).save(eq(deploymentProvider), any(Content.class));
        verify(facade).deploy(eq(process), eq(deployment), any(Content.class));
        verify(processRepository).save(any(Process.class));
    }

    @Test
    public void testDeployed() throws PieceworkException, IOException {
        doReturn(Boolean.TRUE)
                .when(deployment).isDeployed();
        when(deploymentRepository.findOne(deploymentId))
                .thenReturn(deployment);
        when(resource.getContentType())
                .thenReturn("text/xml");
        when(resource.getInputStream())
                .thenReturn(new ByteArrayInputStream("Test".getBytes()));
        when(resource.getName())
                .thenReturn("TESTRESOURCE1");
        when(facade.deploy(any(Process.class), any(ProcessDeployment.class), any(Content.class)))
                .thenReturn(deployment);

        ProcessDeploymentProvider deploymentProvider = new ProcessDeploymentProviderStub(process, deployment, principal);
        PublicationCommand publication = new PublicationCommand(null, deploymentProvider, deploymentId);
        publication.execute(deploymentRepository, processRepository);

        verify(processRepository).save(any(Process.class));
    }
}
