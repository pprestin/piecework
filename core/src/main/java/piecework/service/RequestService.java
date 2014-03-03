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
import piecework.common.RequestFactory;
import piecework.enumeration.ActionType;
import piecework.exception.PieceworkException;
import piecework.exception.StatusCodeError;
import piecework.model.Explanation;
import piecework.model.FormRequest;
import piecework.model.RequestDetails;
import piecework.persistence.ProcessProvider;
import piecework.repository.RequestRepository;
import piecework.security.AccessTracker;
import piecework.util.SecurityUtility;
import piecework.validation.Validation;

/**
 * @author James Renfro
 */
@Service
public class RequestService {

    private static final Logger LOG = Logger.getLogger(RequestService.class);

    @Autowired
    AccessTracker accessTracker;

    @Autowired
    RequestRepository requestRepository;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    TaskService taskService;

    public FormRequest create(RequestDetails requestDetails, ProcessProvider processProvider) throws PieceworkException {
        FormRequest formRequest = new RequestFactory().request(requestDetails, processProvider, ActionType.CREATE, null, null);
        return requestRepository.save(formRequest);
    }

    public <P extends ProcessProvider> FormRequest create(RequestDetails requestDetails, P modelProvider, ActionType actionType) throws PieceworkException {
        FormRequest formRequest = new RequestFactory().request(requestDetails, modelProvider, actionType, null, null);
        return requestRepository.save(formRequest);
    }

    public <P extends ProcessProvider> FormRequest create(RequestDetails requestDetails, P modelProvider, ActionType actionType, Validation validation, Explanation explanation) throws PieceworkException {
        FormRequest formRequest = new RequestFactory().request(requestDetails, modelProvider, actionType, validation, explanation);
        return requestRepository.save(formRequest);
    }

//    public FormRequest create(RequestDetails requestDetails, TaskProvider taskProvider, ActionType actionType, FormRequest previousFormRequest) throws PieceworkException {
//        ProcessInstance instance = processInstanceService.findByTaskId(process, taskId);
//        if (instance == null) {
//            LOG.warn("Forbidden: No instance found for the task id passed " + process.getProcessDefinitionKey() + " task: " + taskId);
//            throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);
//        }
//        Task task = taskService.read(instance, taskId);
//        SecurityUtility.verifyEntityIsAuthorized(process, task, principal);
//
//        ActionType actionType = ActionType.CREATE;
////        if (task != null)
////            actionType = ActionType.COMPLETE;
//
//        return create(requestDetails, process, instance, task, actionType);
//    }

    public FormRequest read(String processDefinitionKey, RequestDetails request, String requestId) throws StatusCodeError {
        FormRequest formRequest = requestRepository.findOne(requestId);

        if (formRequest == null)
            return null;

        SecurityUtility.verifyRequestIntegrity(accessTracker, processDefinitionKey, formRequest, request);

        return formRequest;
//        ProcessInstance instance = formRequest.getInstance();
//
//        if (instance == null && StringUtils.isNotEmpty(formRequest.getProcessDefinitionKey()) && StringUtils.isNotEmpty(formRequest.getProcessInstanceId()))
//            instance = processInstanceService.read(formRequest.getProcessDefinitionKey(), formRequest.getProcessInstanceId(), false);
//
//        FormRequest.Builder builder = new FormRequest.Builder(formRequest)
//                .instance(instance)
//                .task(taskService.read(instance, formRequest.getTaskId()));
//
//        return builder.build();
    }

}
