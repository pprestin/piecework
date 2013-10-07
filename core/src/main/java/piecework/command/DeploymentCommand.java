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
import piecework.Command;
import piecework.CommandExecutor;
import piecework.Constants;
import piecework.engine.ProcessEngineFacade;
import piecework.engine.exception.ProcessEngineException;
import piecework.exception.BadRequestError;
import piecework.exception.NotFoundError;
import piecework.exception.StatusCodeError;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.DeploymentRepository;
import piecework.persistence.ProcessRepository;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.util.ProcessUtility;

/**
 * @author James Renfro
 */
public class DeploymentCommand implements Command<ProcessDeployment> {

    private static final Logger LOG = Logger.getLogger(PublicationCommand.class);

    private final piecework.model.Process process;
    private final String deploymentId;

    public DeploymentCommand(Process process, String deploymentId) {
        this.process = process;
        this.deploymentId = deploymentId;
    }

    @Override
    public ProcessDeployment execute(CommandExecutor commandExecutor) throws StatusCodeError {

        if (LOG.isDebugEnabled())
            LOG.debug("Executing publication command " + this.toString());

        // Instantiate local references to the service beans
        ProcessEngineFacade facade = commandExecutor.getFacade();
        DeploymentRepository deploymentRepository = commandExecutor.getDeploymentRepository();
        ProcessRepository processRepository = commandExecutor.getProcessRepository();

        // Verify that this deployment belongs to this process
        ProcessDeploymentVersion selectedDeploymentVersion = ProcessUtility.deploymentVersion(process, deploymentId);
        if (selectedDeploymentVersion == null)
            throw new NotFoundError();

        // Grab the actual deployment object back from the repository
        ProcessDeployment original = deploymentRepository.findOne(deploymentId);
        if (original == null)
            throw new NotFoundError();

        ProcessDeployment persistedDeployment = null;
        try {
            // Try to deploy it in the engine -- this is the step that's most likely to fail because
            // the artifact is not formatted correctly, etc..
            persistedDeployment = facade.deploy(process, original);

            deploymentRepository.save(persistedDeployment);

            // Assuming that we didn't hit an exception in the step above, then we can update the deployment
            // to indicated that it is deployed
            PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();

            // And update the process definition to indicate that this is the officially published
            // deployment for the process
            Process updatedProcess = new Process.Builder(process, passthroughSanitizer)
                    .deploy(selectedDeploymentVersion, persistedDeployment)
                    .build();
            // Persist that too
            processRepository.save(updatedProcess);
        } catch (ProcessEngineException e) {
            throw new BadRequestError(Constants.ExceptionCodes.process_is_misconfigured, e.getCause());
        }

        return persistedDeployment;
    }

}
