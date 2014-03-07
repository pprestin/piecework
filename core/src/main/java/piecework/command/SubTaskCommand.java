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
import piecework.engine.ProcessEngineFacade;
import piecework.engine.exception.ProcessEngineException;
import piecework.exception.ForbiddenError;
import piecework.exception.InternalServerError;
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.manager.StorageManager;
import piecework.model.Entity;
import piecework.model.Process;
import piecework.model.ProcessInstance;
import piecework.model.Task;
import piecework.persistence.TaskProvider;
import piecework.validation.Validation;

public class SubTaskCommand extends AbstractEngineStorageCommand<ProcessInstance, TaskProvider> {

    private static final Logger LOG = Logger.getLogger(SubTaskCommand.class);

    private final Validation validation;

    public SubTaskCommand(CommandExecutor executor, TaskProvider taskProvider, Validation validation) {
        super(executor, taskProvider);
        this.validation = validation;
    }

    @Override
    ProcessInstance execute(final ProcessEngineFacade processEngineFacade, final StorageManager storageManager) throws PieceworkException {
        Task parentTask = modelProvider.task();
        if (parentTask == null)
            throw new NotFoundError();

        Entity principal = modelProvider.principal();
        // This is an operation that anonymous users should not be able to take
        if (principal == null)
            throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);

        Process process = modelProvider.process();
        // Users are only allowed to complete tasks if they have been assigned a task, and
        // only process overseers or superusers are allowed to complete tasks otherwise
        if (!principal.hasRole(process, AuthorizationRole.OVERSEER, AuthorizationRole.SUPERUSER)) {
            if (parentTask == null || !parentTask.isCandidateOrAssignee(principal) || !principal.hasRole(process, AuthorizationRole.USER))
                throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);
        }

        Task result = null;
        ProcessInstance instance = null;
        try {
            result = processEngineFacade.createSubTask(modelProvider, validation);

            ProcessInstance.Builder builder = new ProcessInstance.Builder(modelProvider.instance());
            builder.task(result);

            instance = storageManager.store(builder.build());

            if (result == null || instance == null) {
                throw new InternalServerError(Constants.ExceptionCodes.subtask_create_invalid);
            }
        } catch (ProcessEngineException e) {
            LOG.error(e);
            throw new InternalServerError(e);
        }


        return instance;

    }

}
