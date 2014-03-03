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
import piecework.engine.ProcessEngineFacade;
import piecework.exception.MisconfiguredProcessException;
import piecework.exception.PieceworkException;
import piecework.manager.StorageManager;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.util.SecurityUtility;
import piecework.validation.Validation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Creates a new process instance and starts execution.
 *
 * @author James Renfro
 */
public class CreateInstanceCommand extends AbstractEngineStorageCommand<ProcessInstance, ProcessDeploymentProvider> {

    private static final Logger LOG = Logger.getLogger(CreateInstanceCommand.class);
//    private final Validation validation;
    private final Submission submission;
    private final Map<String, List<Value>> data;
    private final Collection<Attachment> attachments;

    CreateInstanceCommand(CommandExecutor commandExecutor, ProcessDeploymentProvider modelProvider, Validation validation) {
        this(commandExecutor, modelProvider, validation.getData(), validation.getAttachments(), validation.getSubmission());
    }

    CreateInstanceCommand(CommandExecutor commandExecutor, ProcessDeploymentProvider modelProvider, Map<String, List<Value>> data, Collection<Attachment> attachments, Submission submission) {
        super(commandExecutor, modelProvider);
        this.data = data;
        this.attachments = attachments;
        this.submission = submission;
    }

    @Override
    ProcessInstance execute(ProcessEngineFacade processEngineFacade, StorageManager storageManager) throws PieceworkException {
        Process process = modelProvider.process();
        if (process == null)
            throw new MisconfiguredProcessException("No process provided to create new instance");
        if (StringUtils.isEmpty(process.getProcessDefinitionKey()))
            throw new MisconfiguredProcessException("No process definition key provided to create new instance");

        if (LOG.isDebugEnabled())
            LOG.debug("Creating a new process instance for " + process.getProcessDefinitionKey());

        ProcessDeployment deployment = modelProvider.deployment();
        if (deployment == null)
            throw new MisconfiguredProcessException("No deployment on process");

        Entity principal = modelProvider.principal();

        // If this process doesn't allow anonymous submission, then it's important to verify that the
        // principal is non-null and has the initiator role
        SecurityUtility.verifyEntityCanInitiate(process, principal);

        String initiatorId = principal != null ? principal.getEntityId() : null;
        if (principal != null && principal.getEntityType() == Entity.EntityType.SYSTEM && StringUtils.isNotEmpty(submission.getSubmitterId())) {
            User submitter = submission.getSubmitter();
            if (submitter != null)
                initiatorId = submitter.getUserId();
        }

        // Create/store the process instance first before sending it to the engine
        ProcessInstance stored = storageManager.create(process, deployment, data, attachments, submission, initiatorId);

        String processInstanceId = stored.getProcessInstanceId();
        String engineInstanceId = processEngineFacade.start(process, deployment, stored);
        boolean isUpdated = storageManager.store(stored.getProcessInstanceId(), engineInstanceId);

        LOG.info("Created a new process instance " + processInstanceId + " mapped to engine id " + engineInstanceId);
        if (!isUpdated)
            LOG.error("Unable to update the engine id in the process instance " + processInstanceId);

        if (LOG.isDebugEnabled())
            LOG.debug("Created a new process instance for " + process.getProcessDefinitionKey() + " with identifier " + stored.getProcessInstanceId());

        return stored;
    }

}
