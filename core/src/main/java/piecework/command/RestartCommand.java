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
import piecework.authorization.AuthorizationRole;
import piecework.common.OperationResult;
import piecework.engine.ProcessEngineFacade;
import piecework.engine.exception.ProcessEngineException;
import piecework.enumeration.OperationType;
import piecework.exception.ForbiddenError;
import piecework.exception.PieceworkException;
import piecework.exception.StatusCodeError;
import piecework.manager.StorageManager;
import piecework.model.*;
import piecework.model.Process;

import java.util.Collection;

/**
 * Reactivates a process instance that has been suspended.
 *
 * @author James Renfro
 */
public class RestartCommand extends AbstractCommand<ProcessInstance> {

    private static final Logger LOG = Logger.getLogger(RestartCommand.class);
    private final ProcessDeployment deployment;
    private final OperationType operation;
    private String applicationStatusExplanation;

    RestartCommand(CommandExecutor commandExecutor, Entity principal, Process process, ProcessDeployment deployment, ProcessInstance instance, String applicationStatusExplanation) {
        super(commandExecutor, principal, process, instance);
        this.deployment = deployment;
        this.applicationStatusExplanation = applicationStatusExplanation;
        this.operation = OperationType.RESTART;
    }

    @Override
    ProcessInstance execute(ServiceLocator serviceLocator) throws PieceworkException {
        CommandFactory commandFactory = serviceLocator.getService(CommandFactory.class);
        ProcessEngineFacade processEngineFacade = serviceLocator.getService(ProcessEngineFacade.class);
        StorageManager storageManager = serviceLocator.getService(StorageManager.class);
        return execute(commandFactory, processEngineFacade, storageManager);
    }

    ProcessInstance execute(CommandFactory commandFactory, ProcessEngineFacade processEngineFacade, StorageManager storageManager) throws PieceworkException {
        if (LOG.isDebugEnabled())
            LOG.debug("Executing instance state command " + this.toString());

        String processStatus = instance.getProcessStatus();

        // This is an operation that anonymous users should never be able to cause
        if (principal == null)
            throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);
        // Only process admins or superusers are allowed to reactivate suspended processes
        if (!principal.hasRole(process, AuthorizationRole.ADMIN, AuthorizationRole.SUPERUSER))
            throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);

        if (processStatus != null && processStatus.equals(Constants.ProcessStatuses.QUEUED)) {

            return commandFactory.requeueInstance(principal, process, instance).execute();
        } else {

            Collection<Attachment> attachments = instance.getAttachments();
            ProcessInstance restarted = commandFactory.createInstance(principal, process, instance.getData(), attachments, null).execute();
            if (this.applicationStatusExplanation == null)
                this.applicationStatusExplanation = "";
            this.applicationStatusExplanation += " Restarted as " + restarted.getProcessInstanceId();

            ProcessInstance updated = null;
            try {
                OperationResult result = operation(processEngineFacade);
                updated = storageManager.store(operation, result, instance, principal);

                if (LOG.isDebugEnabled())
                    LOG.debug("Executed instance state command " + this.toString());

            } catch (ProcessEngineException e) {
                LOG.error("Process engine unable to restart execution ", e);
                throw new PieceworkException("Unable to restart execution");
            }

            return updated;
        }
    }

    protected OperationResult operation(ProcessEngineFacade facade) throws StatusCodeError, ProcessEngineException {

        return new OperationResult(applicationStatusExplanation, instance.getPreviousApplicationStatus(), instance.getProcessStatus(), applicationStatusExplanation);
    }

}
