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
import piecework.common.UuidGenerator;
import piecework.engine.ProcessDeploymentResource;
import piecework.engine.ProcessEngineFacade;
import piecework.engine.exception.ProcessEngineException;
import piecework.exception.*;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ActivityRepository;
import piecework.persistence.ContentRepository;
import piecework.persistence.DeploymentRepository;
import piecework.persistence.ProcessRepository;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.util.ProcessUtility;

import java.io.IOException;
import java.util.Map;

/**
 * @author James Renfro
 */
public class DeploymentCommand extends AbstractCommand<ProcessDeployment> {

    private static final Logger LOG = Logger.getLogger(PublicationCommand.class);

    private final String deploymentId;
    private final ProcessDeploymentResource resource;

    DeploymentCommand(CommandExecutor commandExecutor, Process process, String deploymentId, ProcessDeploymentResource resource) {
        super(commandExecutor, null, process);
        this.deploymentId = deploymentId;
        this.resource = resource;
    }

    @Override
    ProcessDeployment execute(ServiceLocator serviceLocator) throws PieceworkException {
        ActivityRepository activityRepository = serviceLocator.getService(ActivityRepository.class);
        ContentRepository contentRepository = serviceLocator.getService(ContentRepository.class);
        DeploymentRepository deploymentRepository = serviceLocator.getService(DeploymentRepository.class);
        ProcessEngineFacade facade = serviceLocator.getService(ProcessEngineFacade.class);
        ProcessRepository processRepository = serviceLocator.getService(ProcessRepository.class);
        UuidGenerator uuidGenerator = serviceLocator.getService(UuidGenerator.class);
        return execute(activityRepository, contentRepository, deploymentRepository, facade, processRepository, uuidGenerator);
    }

    ProcessDeployment execute(ActivityRepository activityRepository, ContentRepository contentRepository, DeploymentRepository deploymentRepository,
                              ProcessEngineFacade facade, ProcessRepository processRepository,
                              UuidGenerator uuidGenerator) throws PieceworkException {

        if (LOG.isDebugEnabled())
            LOG.debug("Executing deployment command " + this.toString());

        String processDefinitionKey = process != null ? process.getProcessDefinitionKey() : null;

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
            String directory = process.getProcessDefinitionKey();
            String id = uuidGenerator.getNextId();
            String location = "/" + directory + "/" + id;

            if (resource.getInputStream() == null)
                throw new BadRequestError();

            Content content = new Content.Builder()
                    .contentType(resource.getContentType())
                    .filename(resource.getName())
                    .location(location)
                    .inputStream(resource.getInputStream())
                    .build();

            try {
                contentRepository.save(process, content);
            } catch (IOException ioe) {
                LOG.error("Error saving to content repo", ioe);
                throw new InternalServerError(Constants.ExceptionCodes.attachment_could_not_be_saved);
            }

            content = contentRepository.findByLocation(process, location);

            // Try to deploy it in the engine -- this is the step that's most likely to fail because
            // the artifact is not formatted correctly, etc..
            persistedDeployment = facade.deploy(process, original, content);

            persistedDeployment = cascadeSave(activityRepository, deploymentRepository, persistedDeployment);

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

    public String getProcessDefinitionKey() {
        return process != null ? process.getProcessDefinitionKey() : null;
    }

    private ProcessDeployment cascadeSave(ActivityRepository activityRepository, DeploymentRepository deploymentRepository, ProcessDeployment deployment) {
        PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
        ProcessDeployment.Builder builder = new ProcessDeployment.Builder(deployment, passthroughSanitizer, false);

        builder.clearActivities();
        builder.published(false);

        Map<String, Activity> activityMap = deployment.getActivityMap();
        if (activityMap != null) {
            for (Map.Entry<String, Activity> entry : deployment.getActivityMap().entrySet()) {
                String key = passthroughSanitizer.sanitize(entry.getKey());
                if (key == null)
                    continue;
                if (entry.getValue() == null)
                    continue;

                builder.activity(key, activityRepository.save(new Activity.Builder(entry.getValue(), passthroughSanitizer).build()));
            }
        }

        return deploymentRepository.save(builder.build());
    }

}
