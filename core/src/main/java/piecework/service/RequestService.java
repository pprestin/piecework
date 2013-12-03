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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.common.RequestFactory;
import piecework.model.RequestDetails;
import piecework.enumeration.ActionType;
import piecework.exception.*;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.RequestRepository;
import piecework.util.ActivityUtil;
import piecework.util.AuthorizationUtility;
import piecework.validation.FormValidation;

import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 * @author James Renfro
 */
@Service
public class RequestService {

    private static final Logger LOG = Logger.getLogger(RequestService.class);

    @Autowired
    RequestRepository requestRepository;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    TaskService taskService;

    public FormRequest create(RequestDetails requestDetails, Process process) throws MisconfiguredProcessException {
        return create(requestDetails, process, null, (Task)null, ActionType.CREATE, null);
    }

    public FormRequest create(RequestDetails requestDetails, Process process, ProcessInstance processInstance, Task task, ActionType actionType) throws MisconfiguredProcessException {
        return create(requestDetails, process, processInstance, task, actionType, null);
    }

    public FormRequest create(RequestDetails requestDetails, Process process, ProcessInstance processInstance, Task task, ActionType actionType, FormValidation validation) throws MisconfiguredProcessException {
        FormRequest formRequest = new RequestFactory().request(requestDetails, process, processInstance, task, actionType, validation);
        return requestRepository.save(formRequest);
    }

    public FormRequest create(Entity principal, RequestDetails requestDetails, Process process, String taskId, FormRequest previousFormRequest) throws MisconfiguredProcessException, StatusCodeError {
        ProcessInstance instance = processInstanceService.findByTaskId(process, taskId);
        if (instance == null) {
            LOG.warn("Forbidden: No instance found for the task id passed " + process.getProcessDefinitionKey() + " task: " + taskId);
            throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);
        }
        Task task = taskService.read(instance, taskId);
        AuthorizationUtility.verifyEntityIsAuthorized(process, task, principal);

        ActionType actionType = ActionType.CREATE;

        return create(requestDetails, process, instance, task, actionType);
    }

    public FormRequest read(RequestDetails request, String requestId) throws StatusCodeError {
        FormRequest formRequest = requestRepository.findOne(requestId);

        if (formRequest == null)
            return null;

        AuthorizationUtility.verifyRequestIntegrity(formRequest, request);
        ProcessInstance instance = formRequest.getInstance();

        if (instance == null && StringUtils.isNotEmpty(formRequest.getProcessDefinitionKey()) && StringUtils.isNotEmpty(formRequest.getProcessInstanceId()))
            instance = processInstanceService.read(formRequest.getProcessDefinitionKey(), formRequest.getProcessInstanceId(), false);

        FormRequest.Builder builder = new FormRequest.Builder(formRequest)
                .instance(instance)
                .task(taskService.read(instance, formRequest.getTaskId()));

        return builder.build();
    }

}
