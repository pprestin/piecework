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
import piecework.exception.*;
import piecework.manager.StorageManager;
import piecework.model.*;
import piecework.model.Process;

/**
 * Detaches a file or comment from its process instance - effectively soft-deleting it so it cannot be
 * retrieved by the users.
 *
 * @author James Renfro
 */
public class DetachmentCommand extends AbstractEngineStorageCommand<ProcessInstance> {

    private final Task task;
    private final String attachmentId;

    public DetachmentCommand(CommandExecutor commandExecutor, Entity principal, Process process, ProcessInstance instance, Task task, String attachmentId) {
        super(commandExecutor, principal, process, instance);
        this.task = task;
        this.attachmentId = attachmentId;
    }

    @Override
    ProcessInstance execute(ProcessEngineFacade processEngineFacade, StorageManager storageManager) throws PieceworkException {
        // This is an operation that anonymous users should never be able to cause
        if (principal == null)
            throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);

        // Users are only allowed to delete attachments if they have been assigned a task, and
        // only process overseers or superusers are allowed to delete attachments without a task
        if (!principal.hasRole(process, AuthorizationRole.OVERSEER, AuthorizationRole.SUPERUSER)) {
            if (task == null || !task.isCandidateOrAssignee(principal) || !principal.hasRole(process, AuthorizationRole.USER))
                throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);
        }

        if (instance == null)
            throw new NotFoundError();

        return storageManager.minusAttachment(instance, attachmentId, principal);
    }

}
