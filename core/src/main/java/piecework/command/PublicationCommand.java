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

import org.apache.log4j.Logger;
import piecework.Constants;
import piecework.ServiceLocator;
import piecework.exception.ForbiddenError;
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.model.Process;
import piecework.model.ProcessDeployment;
import piecework.model.ProcessDeploymentVersion;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.repository.DeploymentRepository;
import piecework.repository.ProcessRepository;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.util.ProcessUtility;

/**
 * Command to publish deployment for a process
 *
 * @author James Renfro
 */
public class PublicationCommand extends AbstractCommand<ProcessDeployment, ProcessDeploymentProvider> {

    private static final Logger LOG = Logger.getLogger(PublicationCommand.class);

    private final String deploymentId;

    PublicationCommand(CommandExecutor commandExecutor, ProcessDeploymentProvider deploymentProvider, String deploymentId) {
        super(commandExecutor, deploymentProvider);
        this.deploymentId = deploymentId;
    }

    @Override
    ProcessDeployment execute(ServiceLocator serviceLocator) throws PieceworkException {
        DeploymentRepository deploymentRepository = serviceLocator.getService(DeploymentRepository.class);
        ProcessRepository processRepository = serviceLocator.getService(ProcessRepository.class);

        return execute(deploymentRepository, processRepository);
    }

    ProcessDeployment execute(DeploymentRepository deploymentRepository, ProcessRepository processRepository) throws PieceworkException {
        if (LOG.isDebugEnabled())
            LOG.debug("Executing publication command " + this.toString());

        Process process = modelProvider.process();

        // Verify that this deployment belongs to this process
        ProcessDeploymentVersion selectedDeploymentVersion = ProcessUtility.deploymentVersion(process, deploymentId);
        if (selectedDeploymentVersion == null)
            throw new NotFoundError();

        // Grab the actual deployment object back from the repository
        ProcessDeployment original = deploymentRepository.findOne(deploymentId);
        if (original == null)
            throw new NotFoundError();

        if (!original.isDeployed())
            throw new ForbiddenError(Constants.ExceptionCodes.process_not_deployed);

        // Update the deployment to indicate that it is published
        PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
        ProcessDeployment updatedDeployment = new ProcessDeployment.Builder(original, passthroughSanitizer, true)
                .publish()
                .build();

        // Make sure we persist that fact
        ProcessDeployment persistedDeployment = deploymentRepository.save(updatedDeployment);

        // And update the process definition to indicate that this is the officially published
        // deployment for the process
        Process updatedProcess = new Process.Builder(process, passthroughSanitizer)
                .deploy(selectedDeploymentVersion, persistedDeployment)
                .build();
        // Persist that too
        processRepository.save(updatedProcess);

        return persistedDeployment;
    }

}
