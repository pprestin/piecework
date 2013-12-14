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
import piecework.exception.ConflictError;
import piecework.exception.ForbiddenError;
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.manager.StorageManager;
import piecework.model.*;
import piecework.validation.Validation;

/**
 * Command that marks task as completed and stores any data/messages/attachments to the instance repository.
 *
 * @author James Renfro
 */
public class CompleteTaskCommand extends AbstractEngineStorageCommand<ProcessInstance> {

    private final ProcessDeployment deployment;
    private final Task task;
    private final Validation validation;
    private final Submission submission;
    private final ActionType actionType;

    CompleteTaskCommand(CommandExecutor commandExecutor, Entity principal, ProcessDeployment deployment, Validation validation, ActionType actionType) {
        super(commandExecutor, principal, validation.getProcess(), validation.getInstance());
        this.deployment = deployment;
        this.task = validation.getTask();
        this.validation = validation;
        this.submission = validation.getSubmission();
        this.actionType = actionType;
    }

    @Override
    ProcessInstance execute(ProcessEngineFacade processEngineFacade, StorageManager storageManager) throws PieceworkException {
        if (task == null)
            throw new NotFoundError();

        // This is an operation that anonymous users should not be able to take
        if (principal == null)
            throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);

        // Users are only allowed to complete tasks if they have been assigned a task, and
        // only process overseers or superusers are allowed to complete tasks otherwise
        if (!principal.hasRole(process, AuthorizationRole.OVERSEER, AuthorizationRole.SUPERUSER)) {
            if (task == null || !task.isCandidateOrAssignee(principal) || !principal.hasRole(process, AuthorizationRole.USER))
                throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);
        }

        switch (actionType) {
            case COMPLETE:
            case REJECT:
                // Only complete the task if the actionType is COMPLETE or REJECT
                completeTask(processEngineFacade, storageManager, task, validation, actionType, principal);
                // Intentional fall-through (no return or break here) on switch statement, since we need to save/attach
                // data on complete and reject as well
            case ATTACH:
            case SAVE:
                return storageManager.store(validation, actionType);
            default:
                // For example, on actionType VALIDATE
                return instance;
        }
    }

    private void completeTask(final ProcessEngineFacade facade, final StorageManager storageManager, final Task task, final Validation validation, final ActionType actionType, final Entity principal) throws PieceworkException {

        String taskId = task.getTaskInstanceId();
        boolean result = facade.completeTask(process, deployment, taskId, actionType, validation, principal);

        if (result == false) {
            // It's possible that the system has gotten out of sync -- grab a current instance of the task from the engine
            // and overwrite the one that's stored in mongo
            Task current = facade.findTask(process, deployment, taskId, false);
            storageManager.store(instance, current);
            throw new ConflictError(Constants.ExceptionCodes.active_task_required);
        }
    }

    public ProcessDeployment getDeployment() {
        return deployment;
    }

    public Validation getValidation() {
        return validation;
    }

    public Submission getSubmission() {
        return submission;
    }

    public Task getTask() {
        return task;
    }

    public ActionType getActionType() {
        return actionType;
    }
}
