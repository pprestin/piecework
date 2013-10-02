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
import piecework.service.IdentityService;
import piecework.model.*;
import piecework.persistence.ProcessInstanceRepository;
import piecework.CommandExecutor;
import piecework.identity.IdentityHelper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author James Renfro
 */
public class StartInstanceCommand extends InstanceCommand {

    private static final Logger LOG = Logger.getLogger(StartInstanceCommand.class);

    public StartInstanceCommand(piecework.model.Process process) {
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

            IdentityDetails user = helper.getAuthenticatedPrincipal();
            String initiatorId = user != null ? user.getInternalId() : null;
            String initiationStatus = deployment.getInitiationStatus();

            if (helper.isAuthenticatedSystem() && StringUtils.isNotEmpty(submission.getSubmitterId())) {
                User submitter = identityService.getUserByAnyId(submission.getSubmitterId());
                if (submitter != null)
                    initiatorId = submitter.getUserId();
            }

            builder = new ProcessInstance.Builder()
                    .processDefinitionKey(process.getProcessDefinitionKey())
                    .processDefinitionLabel(process.getProcessDefinitionLabel())
                    .processInstanceLabel(label)
                    .data(data)
                    .submission(submission)
                    .startTime(new Date())
                    .initiatorId(initiatorId)
                    .processStatus(Constants.ProcessStatuses.OPEN)
                    .attachments(attachments)
                    .applicationStatus(initiationStatus);

            // Save it before routing, then save again with the engine instance id
            ProcessInstance stored = repository.save(builder.build());

            Map<String, String> variables = new HashMap<String, String>();
            variables.put("PIECEWORK_PROCESS_DEFINITION_KEY", process.getProcessDefinitionKey());
            variables.put("PIECEWORK_PROCESS_INSTANCE_ID", stored.getProcessInstanceId());
            variables.put("PIECEWORK_PROCESS_INSTANCE_LABEL", label);

            String engineInstanceId = facade.start(process, stored.getProcessInstanceId(), variables);

            builder.processInstanceId(stored.getProcessInstanceId());
            builder.engineProcessInstanceId(engineInstanceId);
            builder.deploymentId(deployment.getDeploymentId());

            repository.update(stored.getProcessInstanceId(), engineInstanceId);

            if (LOG.isDebugEnabled())
                LOG.debug("Executed start instance command " + this.toString());

        } catch (ProcessEngineException e) {
            LOG.error("Process engine unable to start instance ", e);
            throw new InternalServerError();
        }

        return builder.build();
    }

    public String toString() {
        String processDefinitionKey = process != null ? process.getProcessDefinitionKey() : "";

        return "{ processDefinitionKey: \"" + processDefinitionKey + "\"}";
    }

}
