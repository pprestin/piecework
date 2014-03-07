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
import piecework.authorization.AuthorizationRole;
import piecework.engine.ProcessEngineFacade;
import piecework.exception.ForbiddenError;
import piecework.exception.MisconfiguredProcessException;
import piecework.exception.PieceworkException;
import piecework.manager.StorageManager;
import piecework.model.Entity;
import piecework.model.Process;
import piecework.model.ProcessDeployment;
import piecework.model.ProcessInstance;
import piecework.persistence.ProcessInstanceProvider;

/**
 * Takes an existing process instance and requeues it (attaches it to a new engine process instance)
 *
 * @author James Renfro
 */
public class RequeueInstanceCommand extends AbstractEngineStorageCommand<ProcessInstance, ProcessInstanceProvider> {

    private static final Logger LOG = Logger.getLogger(CreateInstanceCommand.class);

    RequeueInstanceCommand(CommandExecutor commandExecutor, ProcessInstanceProvider instanceProvider) {
        super(commandExecutor, instanceProvider);
    }

    @Override
    ProcessInstance execute(ProcessEngineFacade processEngineFacade, StorageManager storageManager) throws PieceworkException {
        Process process = modelProvider.process();
        if (process == null)
            throw new MisconfiguredProcessException("No process provided to requeue instance");
        if (StringUtils.isEmpty(process.getProcessDefinitionKey()))
            throw new MisconfiguredProcessException("No process definition key provided to requeue instance");

        if (LOG.isDebugEnabled())
            LOG.debug("Requeuing a process instance for " + process.getProcessDefinitionKey());

        ProcessDeployment deployment = process.getDeployment();
        if (deployment == null)
            throw new MisconfiguredProcessException("No deployment on process");

        Entity principal = modelProvider.principal();
        // If we got thru the check above and that principal is not null, then that principal
        // still needs to be an initiator of this process
        if (principal != null && !principal.hasRole(process, AuthorizationRole.INITIATOR))
            throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);

        String initiatorId = principal != null ? principal.getEntityId() : null;

        ProcessInstance instance = modelProvider.instance();
        ProcessInstance updated = new ProcessInstance.Builder(instance)
                .processStatus(Constants.ProcessStatuses.OPEN)
                .clearTasks()
                .build();
        updated = storageManager.store(updated);

        String processInstanceId = instance.getProcessInstanceId();
        String engineInstanceId = processEngineFacade.start(process, deployment, updated);
        boolean isUpdated = storageManager.store(updated.getProcessInstanceId(), engineInstanceId);

        LOG.info("Requeued process instance " + processInstanceId + " mapped to engine id " + engineInstanceId);
        if (!isUpdated)
            LOG.error("Unable to update the engine id in the process instance " + processInstanceId);

        if (LOG.isDebugEnabled())
            LOG.debug("Created a new process instance for " + process.getProcessDefinitionKey() + " with identifier " + processInstanceId);

        return storageManager.get(processInstanceId);
    }

}
