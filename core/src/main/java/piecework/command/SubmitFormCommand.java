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
import piecework.common.ServiceLocator;
import piecework.enumeration.ActionType;
import piecework.exception.ForbiddenError;
import piecework.exception.PieceworkException;
import piecework.manager.StorageManager;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ModelProviderFactory;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.persistence.TaskProvider;
import piecework.service.RequestService;
import piecework.util.ModelUtility;
import piecework.util.ProcessInstanceUtility;
import piecework.util.SecurityUtility;
import piecework.validation.Validation;

import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
public class SubmitFormCommand<P extends ProcessDeploymentProvider> extends AbstractCommand<SubmissionCommandResponse, P> {

    private final Validation validation;
    private final ActionType actionType;
    private final RequestDetails requestDetails;
    private final FormRequest request;

    SubmitFormCommand(CommandExecutor commandExecutor, P modelProvider, Validation validation, ActionType actionType, RequestDetails requestDetails, FormRequest request) {
        super(commandExecutor, modelProvider);
        this.validation = validation;
        this.actionType = actionType;
        this.requestDetails = requestDetails;
        this.request = request;
    }

    @Override
    SubmissionCommandResponse execute(ServiceLocator serviceLocator) throws PieceworkException {
        CommandFactory commandFactory = serviceLocator.getService(CommandFactory.class);
        RequestService requestService = serviceLocator.getService(RequestService.class);
        StorageManager storageManager = serviceLocator.getService(StorageManager.class);
        ModelProviderFactory modelProviderFactory = serviceLocator.getService(ModelProviderFactory.class);

        return execute(commandFactory, modelProviderFactory, requestService, storageManager);
    }

    SubmissionCommandResponse execute(CommandFactory commandFactory, ModelProviderFactory modelProviderFactory, RequestService requestService, StorageManager storageManager) throws PieceworkException {
        // This is an operation that anonymous users should not be able to cause unless the process is set up to allow it explicitly
        Entity principal = modelProvider.principal();
        Process process = modelProvider.process();
        // If this process doesn't allow anonymous submission, then it's important to verify that the
        // principal is non-null and has the initiator role
        SecurityUtility.verifyEntityCanInitiate(process, principal);

        // Decide if this is a 'create instance' or 'complete task' form submission
        Task task = ModelUtility.task(modelProvider);
        ProcessInstance stored = null;

        // Can't submit a form for a task that is inactive or that is not assigned to you
        if (task != null && (!task.isActive() || !task.isAssignee(principal)))
            throw new ForbiddenError(Constants.ExceptionCodes.active_task_required);

        ActionType validatedActionType = actionType;
        Submission submission = validation.getSubmission();
        if (submission != null && submission.getAction() != null)
            validatedActionType = submission.getAction();
        else if (actionType == ActionType.CREATE)
            validatedActionType = ActionType.COMPLETE;

        ProcessInstance instance = ModelUtility.instance(modelProvider);
        AbstractCommand<ProcessInstance, ? extends ProcessDeploymentProvider> command = null;

        if (task == null)
            command = commandFactory.createInstance(modelProvider, validation);
        else if (instance != null && principal != null && (validatedActionType == ActionType.COMPLETE || validatedActionType == ActionType.REJECT))
            command = commandFactory.completeTask(TaskProvider.class.cast(modelProvider), validation, validatedActionType);

        if (command != null)
            stored = command.execute();

        FormRequest nextRequest = request;

        ProcessDeploymentProvider provider;
        if (task != null)
            provider = modelProviderFactory.taskProvider(process.getProcessDefinitionKey(), task.getTaskInstanceId(), principal);
        else if (stored != null)
            provider = modelProviderFactory.instanceProvider(process.getProcessDefinitionKey(), stored.getProcessInstanceId(), principal);
        else
            provider = modelProviderFactory.deploymentProvider(process.getProcessDefinitionKey(), principal);

        switch (validatedActionType) {
            case CREATE:
            case COMPLETE:
            case REJECT:
                if (stored != null) {
                    nextRequest = requestService.create(requestDetails, provider, validatedActionType);
                    return new SubmissionCommandResponse(provider, submission, nextRequest);
                }
            case SAVE:
                Map<String, List<Value>> validationData = validation != null ? validation.getData() : null;
                String submissionLabel = submission != null ? submission.getProcessInstanceLabel() : null;
                String label = ProcessInstanceUtility.processInstanceLabel(process, instance, validationData, submissionLabel);
                stored = storageManager.store(label, instance, validation.getData(), submission);
                nextRequest = requestService.create(requestDetails, provider, validatedActionType);
                break;
        }

        return new SubmissionCommandResponse(provider, submission, nextRequest);
    }

}
