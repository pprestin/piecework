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
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.manager.StorageManager;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.AllowedTaskProvider;
import piecework.persistence.ProcessInstanceProvider;
import piecework.util.ModelUtility;
import piecework.validation.Validation;

/**
 * Attachs a file or comment to a process instance
 *
 * @author James Renfro
 */
public class AttachmentCommand extends AbstractEngineStorageCommand<ProcessInstance, ProcessInstanceProvider> {

    private final Validation validation;

    AttachmentCommand(CommandExecutor commandExecutor, ProcessInstanceProvider modelProvider, Validation validation) {
        super(commandExecutor, modelProvider);
        this.validation = validation;
    }

    @Override
    ProcessInstance execute(ProcessEngineFacade processEngineFacade, StorageManager storageManager) throws PieceworkException {
        Entity principal = modelProvider.principal();
        // This is an operation that anonymous users should never be able to cause
        if (principal == null)
            throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);

        Task task = ModelUtility.allowedTask(modelProvider);

        Process process = modelProvider.process();
        // Users are only allowed to add attachments if they have been assigned a task, and
        // only process overseers or superusers are allowed to add attachments without a task
        if (!principal.hasRole(process, AuthorizationRole.OVERSEER, AuthorizationRole.SUPERUSER)) {
            if (task == null || !task.isCandidateOrAssignee(principal) || !principal.hasRole(process, AuthorizationRole.USER))
                throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);
        }

        ProcessInstance instance = modelProvider.instance();
        // Can't attach anything unless there is an instance to attach to
        if (instance == null)
            throw new NotFoundError();

        return storageManager.store(modelProvider, validation, ActionType.ATTACH);
    }

}
