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
import piecework.engine.ProcessEngineFacade;
import piecework.enumeration.ActionType;
import piecework.exception.ForbiddenError;
import piecework.exception.PieceworkException;
import piecework.manager.StorageManager;
import piecework.model.Entity;
import piecework.model.Process;
import piecework.model.ProcessInstance;
import piecework.model.Task;
import piecework.persistence.AllowedTaskProvider;
import piecework.validation.Validation;

/**
 * Updates a specific field value of a process instance
 *
 * @author James Renfro
 */
public class UpdateValueCommand extends AbstractEngineStorageCommand<ProcessInstance, AllowedTaskProvider> {

    private final Validation validation;

    UpdateValueCommand(CommandExecutor commandExecutor, AllowedTaskProvider allowedTaskProvider, Validation validation) {
        super(commandExecutor, allowedTaskProvider);
        this.validation = validation;
    }

    @Override
    ProcessInstance execute(ProcessEngineFacade processEngineFacade, StorageManager storageManager) throws PieceworkException {
        // This is an operation that anonymous users should never be able to cause
        Entity principal = modelProvider.principal();
        if (principal == null)
            throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);

        ActionType actionType = ActionType.SAVE;

        // Users are only allowed to complete tasks if they have been assigned a task, and
        // only process overseers or superusers are allowed to complete tasks otherwise
        Process process = modelProvider.process();
        if (!principal.hasRole(process, AuthorizationRole.OVERSEER, AuthorizationRole.SUPERUSER)) {
            Task task = modelProvider.allowedTask(true);
            if (task == null || !task.isCandidateOrAssignee(principal) || !principal.hasRole(process, AuthorizationRole.USER))
                throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);
        }

        return storageManager.store(modelProvider, validation, actionType);
    }
}
