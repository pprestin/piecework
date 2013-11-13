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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import piecework.Constants;
import piecework.engine.ProcessEngineFacade;
import piecework.engine.exception.ProcessEngineException;
import piecework.exception.ConflictError;
import piecework.exception.InternalServerError;
import piecework.exception.StatusCodeError;
import piecework.identity.IdentityDetails;
import piecework.model.Process;
import piecework.service.IdentityService;
import piecework.model.*;
import piecework.persistence.ProcessInstanceRepository;
import piecework.CommandExecutor;
import piecework.identity.IdentityHelper;

import java.util.Date;

/**
 * Command to start a process instance
 * @author James Renfro
 */
public class StartInstanceCommand extends InstanceCommand {

    private static final Logger LOG = Logger.getLogger(StartInstanceCommand.class);

    public StartInstanceCommand(Process process) {
        super(process);
    }

    @Override
    public ProcessInstance execute(CommandExecutor commandExecutor) throws StatusCodeError {

        if (LOG.isDebugEnabled())
            LOG.debug("Executing start instance command " + this.toString());

        ProcessEngineFacade facade = commandExecutor.getFacade();
        IdentityHelper helper = commandExecutor.getHelper();
        IdentityService identityService = commandExecutor.getIdentityService();
        ProcessInstanceRepository repository = commandExecutor.getProcessInstanceRepository();

        ProcessInstance.Builder builder;
        try {
            ProcessDeployment deployment = process.getDeployment();
            if (deployment == null)
                throw new ConflictError(Constants.ExceptionCodes.process_is_misconfigured);

            Entity principal = helper.getPrincipal();
            String initiatorId = principal != null ? principal.getEntityId() : null;
            String initiationStatus = deployment.getInitiationStatus();

            if (principal != null && principal.getEntityType() == Entity.EntityType.SYSTEM && StringUtils.isNotEmpty(submission.getSubmitterId())) {
                User submitter = identityService.getUserByAnyId(submission.getSubmitterId());
                if (submitter != null)
                    initiatorId = submitter.getUserId();
            }

            builder = new ProcessInstance.Builder()
                    .processDefinitionKey(process.getProcessDefinitionKey())
                    .processDefinitionLabel(process.getProcessDefinitionLabel())
                    .processInstanceLabel(label)
                    .deploymentId(deployment.getDeploymentId())
                    .data(data)
                    .submission(submission)
                    .startTime(new Date())
                    .initiatorId(initiatorId)
                    .processStatus(Constants.ProcessStatuses.OPEN)
                    .attachments(attachments)
                    .applicationStatus(initiationStatus);

            if (process.isAllowPerInstanceActivities())
                builder.activityMap(submission.getActivityMap());

            // Save it before routing, then save again with the engine instance id
            ProcessInstance stored = repository.save(builder.build());

            String engineInstanceId = facade.start(process, deployment, stored);
            repository.update(stored.getProcessInstanceId(), engineInstanceId);

            if (LOG.isDebugEnabled())
                LOG.debug("Executed start instance command " + this.toString());

            return stored;

        } catch (ProcessEngineException e) {
            LOG.error("Process engine unable to start instance ", e);
            throw new InternalServerError();
        }
    }

    public String toString() {
        String processDefinitionKey = process != null ? process.getProcessDefinitionKey() : "";

        return "{ processDefinitionKey: \"" + processDefinitionKey + "\"}";
    }

}
