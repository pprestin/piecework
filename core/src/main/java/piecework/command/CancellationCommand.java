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
import piecework.authorization.AuthorizationRole;
import piecework.common.OperationResult;
import piecework.engine.ProcessEngineFacade;
import piecework.engine.exception.ProcessEngineException;
import piecework.enumeration.OperationType;
import piecework.exception.ForbiddenError;
import piecework.exception.MisconfiguredProcessException;
import piecework.exception.PieceworkException;
import piecework.model.Entity;
import piecework.model.Process;
import piecework.model.ProcessDeployment;
import piecework.model.ProcessInstance;
import piecework.persistence.ProcessInstanceProvider;

/**
 * Cancels a process instance so it can no longer be executed.
 *
 * @author James Renfro
 */
public class CancellationCommand extends AbstractOperationCommand {

    private static final Logger LOG = Logger.getLogger(CancellationCommand.class);

    CancellationCommand(CommandExecutor commandExecutor, ProcessInstanceProvider instanceProvider, String applicationStatusExplanation) {
        super(commandExecutor, instanceProvider, null, OperationType.CANCELLATION, null, applicationStatusExplanation);
    }

    @Override
    protected OperationResult operation(ProcessEngineFacade facade) throws PieceworkException, ProcessEngineException {
        Entity principal = modelProvider.principal();
        // This is an operation that anonymous users should never be able to cause
        if (principal == null)
            throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);

        Process process = modelProvider.process();
        ProcessInstance instance = modelProvider.instance();
        // Only initiators, process admins or superusers are allowed to cancel processes
        if (!instance.isInitiator(principal) && !principal.hasRole(process, AuthorizationRole.ADMIN, AuthorizationRole.SUPERUSER))
            throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);

        ProcessDeployment deployment = modelProvider.deployment();
        // Don't bother throwing an exception if we fail to cancel -- just log it
        if (!facade.cancel(process, deployment, instance))
            LOG.fatal("Unable to really cancel the process instance execution... might be a serious issue, but not going to bother the user about it");
        return new OperationResult(applicationStatusExplanation, deployment.getCancellationStatus(), Constants.ProcessStatuses.CANCELLED, applicationStatusExplanation);
    }

}
