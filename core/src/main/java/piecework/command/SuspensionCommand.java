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

import piecework.Constants;
import piecework.authorization.AuthorizationRole;
import piecework.common.OperationResult;
import piecework.engine.ProcessEngineFacade;
import piecework.engine.exception.ProcessEngineException;
import piecework.enumeration.OperationType;
import piecework.exception.ConflictError;
import piecework.exception.ForbiddenError;
import piecework.exception.PieceworkException;
import piecework.model.Entity;
import piecework.model.ProcessDeployment;
import piecework.model.ProcessInstance;
import piecework.persistence.ProcessInstanceProvider;

/**
 * Suspends a process instance so that it is no longer executing. Can be reversed with the
 * ActivationCommand.
 *
 * @author James Renfro
 */
public class SuspensionCommand extends AbstractOperationCommand {

    SuspensionCommand(CommandExecutor commandExecutor, ProcessInstanceProvider instanceProvider, String applicationStatusExplanation) {
        super(commandExecutor, instanceProvider, null, OperationType.SUSPENSION, null, applicationStatusExplanation);
    }

    @Override
    protected OperationResult operation(ProcessEngineFacade facade) throws PieceworkException, ProcessEngineException {
        Entity principal = modelProvider.principal();

        // This is an operation that anonymous users should never be able to cause
        if (principal == null)
            throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);

        piecework.model.Process process = modelProvider.process();
        // Only process admins or superusers are allowed to suspend processes
        if (!principal.hasRole(process, AuthorizationRole.ADMIN, AuthorizationRole.SUPERUSER))
            throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);
        ProcessInstance instance = modelProvider.instance();
        ProcessDeployment deployment = modelProvider.deployment();
        if (!facade.suspend(process, deployment, instance))
            throw new ConflictError(Constants.ExceptionCodes.invalid_process_status);
        return new OperationResult(applicationStatusExplanation, deployment.getSuspensionStatus(), Constants.ProcessStatuses.SUSPENDED, applicationStatusExplanation, instance.getApplicationStatus());
    }
}
