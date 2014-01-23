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
import piecework.model.Process;
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

    public ActivationCommand activation(Entity principal, Process process, ProcessDeployment deployment, ProcessInstance instance, String applicationStatusExplanation) {

        return new ActivationCommand(commandExecutor, principal, process, deployment, instance, applicationStatusExplanation);
    }

    public AssignmentCommand assignment(Entity principal, Process process, ProcessDeployment deployment, ProcessInstance instance, Task task, User assignee) {

        return new AssignmentCommand(commandExecutor, principal, process, deployment, instance, task, assignee);
    }

    public AttachmentCommand attachment(Entity principal, ProcessDeployment deployment, Validation validation) {

        return new AttachmentCommand(commandExecutor, principal, deployment, validation);
    }

    public CancellationCommand cancellation(Entity principal, Process process, ProcessDeployment deployment, ProcessInstance instance, String applicationStatusExplanation) {

        return new CancellationCommand(commandExecutor, principal, process, deployment, instance, applicationStatusExplanation);
    }

    public CompleteTaskCommand completeTask(Entity principal, ProcessDeployment deployment, Validation validation, ActionType actionType) {

        return new CompleteTaskCommand(commandExecutor, principal, deployment, validation, actionType);
    }

    public CompletionCommand completion(ProcessInstance instance, Map<String, List<Value>> data) {

        return new CompletionCommand(commandExecutor, instance, data);
    }

    public CreateInstanceCommand createInstance(Entity principal, Validation validation) {

        return new CreateInstanceCommand(commandExecutor, principal, validation);
    }

    public CreateInstanceCommand createInstance(Entity principal, Process process, Map<String, List<Value>> data, Collection<Attachment> attachments, Submission submission) {

        return new CreateInstanceCommand(commandExecutor, principal, process, data, attachments, submission);
    }

    public DeploymentCommand deployment(Process process, String deploymentId, ProcessDeploymentResource resource) {

        return new DeploymentCommand(commandExecutor, process, deploymentId, resource);
    }

    public DetachmentCommand detachment(Entity principal, Process process, ProcessInstance instance, Task task, String attachmentId) {

        return new DetachmentCommand(commandExecutor, principal, process, instance, task, attachmentId);
    }

    public SubTaskCommand createsubtask(Entity principal, Process process, ProcessInstance instance, ProcessDeployment deployment, String taskid, Validation validation) {

        return new SubTaskCommand(commandExecutor, principal, process, instance, deployment, taskid, validation);
    }

    public PublicationCommand publication(Process process, String deploymentId) {

        return new PublicationCommand(commandExecutor, process, deploymentId);
    }

    public RequeueInstanceCommand requeueInstance(Entity principal, Process process, ProcessInstance instance) {

        return new RequeueInstanceCommand(commandExecutor, principal, process, instance);
    }

    public RemoveValueCommand removeValue(Entity principal, Process process, ProcessInstance instance, Task task, String fieldName, String valueId) {

        return new RemoveValueCommand(commandExecutor, principal, process, instance, task, fieldName, valueId);
    }

    public RestartCommand restart(Entity principal, Process process, ProcessDeployment deployment, ProcessInstance instance, String applicationStatusExplanation) {

        return new RestartCommand(commandExecutor, principal, process, deployment, instance, applicationStatusExplanation);
    }

    public SubmitFormCommand submitForm(Entity principal, ProcessDeployment deployment, Validation validation, ActionType actionType, RequestDetails requestDetails, FormRequest request) {

        return new SubmitFormCommand(commandExecutor, principal, deployment, validation, actionType, requestDetails, request);
    }

    public SuspensionCommand suspension(Entity principal, Process process, ProcessDeployment deployment, ProcessInstance instance, String applicationStatusExplanation) {

        return new SuspensionCommand(commandExecutor, principal, process, deployment, instance, applicationStatusExplanation);
    }

    public UpdateDataCommand updateData(Entity principal, Process process, ProcessInstance instance, Task task, Map<String, List<Value>> data, Map<String, List<Message>> messages, String applicationStatusExplanation) {

        return new UpdateDataCommand(commandExecutor, principal, process, instance, task, data, messages, applicationStatusExplanation);
    }

    public UpdateStatusCommand updateStatus(Entity principal, Process process, ProcessInstance instance, String applicationStatus, String applicationStatusExplanation) {

        return new UpdateStatusCommand(commandExecutor, principal, process, instance, applicationStatus, applicationStatusExplanation);
    }

    public UpdateValueCommand updateValue(Entity principal, Task task, Validation validation) {

        return new UpdateValueCommand(commandExecutor, principal, task, validation);
    }

    public ValidationCommand validation(Process process, ProcessDeployment deployment, FormRequest request, Submission submission, Entity principal) {

        return new ValidationCommand(commandExecutor, process, deployment, request, submission, principal);
    }

    public ValidationCommand validation(Process process, ProcessDeployment deployment, FormRequest request, Object object, Class<?> type, Entity principal) {

        return new ValidationCommand(commandExecutor, process, deployment, request, object, type, principal, null, null);
    }

    public ValidationCommand validation(Process process, ProcessDeployment deployment, FormRequest request, Object object, Class<?> type, Entity principal, String validationId, String fieldName) {

        return new ValidationCommand(commandExecutor, process, deployment, request, object, type, principal, validationId, fieldName);
    }

}
