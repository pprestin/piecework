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
package piecework.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.command.CommandFactory;
import piecework.enumeration.ActionType;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.validation.Validation;

import javax.ws.rs.core.MultivaluedMap;

/**
 * @author James Renfro
 */
@Service
public class FormService {

    private static final Logger LOG = Logger.getLogger(FormService.class);

    @Autowired
    CommandFactory commandFactory;

    @Autowired
    DeploymentService deploymentService;

    @Autowired
    RequestService requestService;

    @Autowired
    TaskService taskService;


    public SearchResults search(MultivaluedMap<String, String> rawQueryParameters, Entity principal) throws PieceworkException {
        return taskService.search(rawQueryParameters, principal, true, false);
    }

    public <T> FormRequest save(Process process, RequestDetails requestDetails, String requestId, T data, Class<T> type, Entity principal) throws PieceworkException {
        FormRequest request = requestService.read(requestDetails, requestId);
        ProcessDeployment deployment = deploymentService.read(process, request.getInstance());
        Validation validation = commandFactory.validation(process, deployment, request, data, type, principal).execute();
        return commandFactory.submitForm(principal, deployment, validation, ActionType.SAVE, requestDetails, request).execute();
    }

    public <T> FormRequest submit(Process process, RequestDetails requestDetails, String requestId, T data, Class<T> type, Entity principal) throws PieceworkException {
        FormRequest request = requestService.read(requestDetails, requestId);
        ProcessDeployment deployment = deploymentService.read(process, request.getInstance());
        Validation validation = commandFactory.validation(process, deployment, request, data, type, principal).execute();
        return commandFactory.submitForm(principal, deployment, validation, ActionType.COMPLETE, requestDetails, request).execute();
    }

    public <T> void validate(Process process, RequestDetails requestDetails, String requestId, final T data, final Class<T> type, String validationId, Entity principal) throws PieceworkException {
        FormRequest request = requestService.read(requestDetails, requestId);
        ProcessDeployment deployment = deploymentService.read(process, request.getInstance());
        commandFactory.validation(process, deployment, request, data, type, principal).execute();
    }

//    private <T> FormRequest doSubmitForm(Process process, FormRequest formRequest, RequestDetails requestDetails, T data, Class<T> type) throws PieceworkException {
//        SubmissionHandler handler = submissionHandlerRegistry.handler(type);
//        Entity principal = helper.getPrincipal();
//        Task task = formRequest.getTaskId() != null ? taskService.read(process, formRequest.getTaskId(), true) : null;
//        String processInstanceId = null;
//
//        if (task != null)
//            processInstanceId = task.getProcessInstanceId();
//
//        if (StringUtils.isEmpty(processInstanceId))
//            processInstanceId = formRequest.getProcessInstanceId();
//
//        ProcessInstance instance = null;
//        if (StringUtils.isNotEmpty(processInstanceId))
//            instance = processInstanceService.read(process, task.getProcessInstanceId(), false);
//
//        ProcessDeployment deployment = deploymentService.read(process, instance);
//        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, deployment, task, formRequest);
//        Submission submission = handler.handle(data, template, principal);
//
//        ActionType action = submission.getAction();
//        if (action == null)
//            action = ActionType.COMPLETE;
//
//        FormRequest nextFormRequest = null;
//        switch (action) {
//            case COMPLETE:
//                ProcessInstance stored = processInstanceService.submit(principal, process, instance, task, template, submission);
//                nextFormRequest = requestService.create(requestDetails, process, stored, task, action);
//                return nextFormRequest;
//
//            case REJECT:
//                stored = processInstanceService.reject(principal, process, instance, task, template, submission);
//                nextFormRequest = requestService.create(requestDetails, process, stored, task, action);
//                return nextFormRequest;
//
//            case SAVE:
//                processInstanceService.save(principal, process, instance, task, template, submission);
//                return formRequest;
//
//            case VALIDATE:
//                validationFactory.validate(process, instance, task, template, submission, true);
//                return formRequest;
//        }
//
//        return null;
//    }

//    private <T> void doValidateForm(Process process, FormRequest formRequest, String validationId, T data, Class<T> type) throws PieceworkException {
//        SubmissionHandler handler = submissionHandlerRegistry.handler(type);
//        Entity principal = helper.getPrincipal();
//        Task task = formRequest.getTaskId() != null ? taskService.read(process, formRequest.getTaskId(), true) : null;
//        ProcessInstance instance = null;
//
//        if (task != null && task.getProcessInstanceId() != null)
//            instance = processInstanceService.read(process, task.getProcessInstanceId(), false);
//
//        ProcessDeployment deployment = deploymentService.read(process, instance);
//        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, deployment, task, formRequest, validationId);
//        Submission submission = handler.handle(data, template, principal);
//        validationFactory.validate(process, instance, task, template, submission, true);
//    }

}
