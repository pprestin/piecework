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
import piecework.ServiceLocator;
import piecework.enumeration.ActionType;
import piecework.exception.ForbiddenError;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.service.RequestService;
import piecework.validation.Validation;

/**
 * @author James Renfro
 */
public class SubmitFormCommand extends AbstractCommand<FormRequest> {

    private final ProcessDeployment deployment;
    private final Validation validation;
    private final ActionType actionType;
    private final RequestDetails requestDetails;
    private final FormRequest request;

//    String template =

    SubmitFormCommand(CommandExecutor commandExecutor, Entity principal, ProcessDeployment deployment, Validation validation, ActionType actionType, RequestDetails requestDetails, FormRequest request) {
        super(commandExecutor, principal, validation.getProcess(), validation.getInstance());
        this.deployment = deployment;
        this.validation = validation;
        this.actionType = actionType;
        this.requestDetails = requestDetails;
        this.request = request;
    }

    @Override
    FormRequest execute(ServiceLocator serviceLocator) throws PieceworkException {
        CommandFactory commandFactory = serviceLocator.getService(CommandFactory.class);
        RequestService requestService = serviceLocator.getService(RequestService.class);

        return execute(commandFactory, requestService);
    }

    FormRequest execute(CommandFactory commandFactory, RequestService requestService) throws PieceworkException {
        // This is an operation that anonymous users should not be able to cause unless the process is set up to allow it explicitly
        if (principal == null && !process.isAnonymousSubmissionAllowed())
            throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);

        // Decide if this is a 'create instance' or 'complete task' form submission
        Task task = validation.getTask();
        ProcessInstance stored = null;

        ActionType validatedActionType = actionType;
        Submission submission = validation.getSubmission();
        if (submission != null && submission.getAction() != null)
            validatedActionType = submission.getAction();
        else if (actionType == ActionType.CREATE)
            validatedActionType = ActionType.COMPLETE;

        AbstractCommand<ProcessInstance> command = null;
        if (task == null)
            command = commandFactory.createInstance(principal, validation);
        else if (instance != null && principal != null)
            command = commandFactory.completeTask(principal, deployment, validation, validatedActionType);

        if (command != null)
            stored = command.execute();

        switch (validatedActionType) {
            case CREATE:
            case COMPLETE:
            case REJECT:
                if (stored != null)
                    return requestService.create(requestDetails, process, stored, task, validatedActionType);
            case SAVE:
            case VALIDATE:
                return request;
        }

        return request;
    }

}
