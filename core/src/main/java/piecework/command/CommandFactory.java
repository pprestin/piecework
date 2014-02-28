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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.engine.ProcessDeploymentResource;
import piecework.enumeration.ActionType;
import piecework.model.*;
import piecework.persistence.*;
import piecework.validation.Validation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
@Service
public class CommandFactory {

    private static final Logger LOG = Logger.getLogger(CommandFactory.class);

    @Autowired
    private CommandExecutor commandExecutor;

    public ActivationCommand activation(ProcessInstanceProvider instanceProvider, String applicationStatusExplanation) {

        return new ActivationCommand(commandExecutor, instanceProvider, applicationStatusExplanation);
    }

    public AssignmentCommand assignment(ProcessInstanceProvider instanceProvider, Task task, User assignee) {

        return new AssignmentCommand(commandExecutor, instanceProvider, task, assignee);
    }

    public AttachmentCommand attachment(ProcessInstanceProvider instanceProvider, Validation validation) {

        return new AttachmentCommand(commandExecutor, instanceProvider, validation);
    }

    public CancellationCommand cancellation(ProcessInstanceProvider instanceProvider, String applicationStatusExplanation) {

        return new CancellationCommand(commandExecutor, instanceProvider, applicationStatusExplanation);
    }

    public CompleteTaskCommand completeTask(TaskProvider taskProvider, Validation validation, ActionType actionType) {

        return new CompleteTaskCommand(commandExecutor, taskProvider, validation, actionType);
    }

    public CompletionCommand completion(ProcessInstanceProvider instanceProvider) {

        return new CompletionCommand(commandExecutor, instanceProvider);
    }

    public CreateInstanceCommand createInstance(ProcessDeploymentProvider modelProvider, Validation validation) {

        return new CreateInstanceCommand(commandExecutor, modelProvider, validation);
    }

    public CreateInstanceCommand createInstance(ProcessDeploymentProvider modelProvider, Map<String, List<Value>> data, Collection<Attachment> attachments, Submission submission) {

        return new CreateInstanceCommand(commandExecutor, modelProvider, data, attachments, submission);
    }

    public DeploymentCommand deployment(ProcessDeploymentProvider modelProvider, String deploymentId, ProcessDeploymentResource resource) {

        return new DeploymentCommand(commandExecutor, modelProvider, deploymentId, resource);
    }

    public DetachmentCommand detachment(AllowedTaskProvider allowedTaskProvider, String attachmentId) {

        return new DetachmentCommand(commandExecutor, allowedTaskProvider, attachmentId);
    }

    public SubTaskCommand createSubTask(TaskProvider taskProvider, Validation validation) {

        return new SubTaskCommand(commandExecutor, taskProvider, validation);
    }

    public PublicationCommand publication(ProcessDeploymentProvider deploymentProvider, String deploymentId) {

        return new PublicationCommand(commandExecutor, deploymentProvider, deploymentId);
    }

    public RequeueInstanceCommand requeueInstance(ProcessInstanceProvider instanceProvider) {

        return new RequeueInstanceCommand(commandExecutor, instanceProvider);
    }

    public RemoveValueCommand removeValue(AllowedTaskProvider allowedTaskProvider, String fieldName, String valueId) {

        return new RemoveValueCommand(commandExecutor, allowedTaskProvider, fieldName, valueId);
    }

    public RestartCommand restart(ProcessInstanceProvider instanceProvider, String applicationStatusExplanation) {

        return new RestartCommand(commandExecutor, instanceProvider, applicationStatusExplanation);
    }

    public <P extends ProcessDeploymentProvider> SubmitFormCommand submitForm(P modelProvider, Validation validation, ActionType actionType, RequestDetails requestDetails, FormRequest request) {

        return new SubmitFormCommand(commandExecutor, modelProvider, validation, actionType, requestDetails, request);
    }

    public SuspensionCommand suspension(ProcessInstanceProvider instanceProvider, String applicationStatusExplanation) {

        return new SuspensionCommand(commandExecutor, instanceProvider, applicationStatusExplanation);
    }

    public <P extends ProcessProvider> UpdateDataCommand updateData(P modelProvider, Map<String, List<Value>> data, Map<String, List<Message>> messages, String applicationStatusExplanation) {

        return new UpdateDataCommand(commandExecutor, modelProvider, data, messages, applicationStatusExplanation);
    }

    public UpdateStatusCommand updateStatus(ProcessInstanceProvider instanceProvider, String applicationStatus, String applicationStatusExplanation) {

        return new UpdateStatusCommand(commandExecutor, instanceProvider, applicationStatus, applicationStatusExplanation);
    }

    public UpdateValueCommand updateValue(AllowedTaskProvider allowedTaskProvider, Validation validation) {

        return new UpdateValueCommand(commandExecutor, allowedTaskProvider, validation);
    }

    public <P extends ProcessDeploymentProvider> ValidationCommand validation(P modelProvider, FormRequest request, Submission submission, String version) {

        return new ValidationCommand<P>(commandExecutor, modelProvider, request, submission, version);
    }

    public <P extends ProcessDeploymentProvider> ValidationCommand validation(P modelProvider, FormRequest request, Object object, Class<?> type, String validationId, String version) {

        return new ValidationCommand<P>(commandExecutor, modelProvider, request, object, type, validationId, version);
    }

    public <P extends ProcessDeploymentProvider> FieldValidationCommand fieldValidation(P modelProvider, FormRequest request, Object object, Class<?> type, String fieldName, String version) {

        return new FieldValidationCommand<P>(commandExecutor, modelProvider, request, object, type, fieldName, version);
    }

}
