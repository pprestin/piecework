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
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.identity.IdentityHelper;
import piecework.model.RequestDetails;
import piecework.enumeration.ActionType;
import piecework.exception.*;
import piecework.model.*;
import piecework.model.Process;
import piecework.submission.SubmissionHandler;
import piecework.submission.SubmissionHandlerRegistry;
import piecework.submission.SubmissionTemplate;
import piecework.submission.SubmissionTemplateFactory;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Map;

/**
 * @author James Renfro
 */
@Service
public class FormService {

    private static final Logger LOG = Logger.getLogger(FormService.class);

    @Autowired
    IdentityHelper helper;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    RequestService requestService;

    @Autowired
    SubmissionHandlerRegistry submissionHandlerRegistry;

    @Autowired
    SubmissionTemplateFactory submissionTemplateFactory;

    @Autowired
    TaskService taskService;

    @Autowired
    ValidationService validationService;

    public SearchResults search(MultivaluedMap<String, String> rawQueryParameters, Entity principal) throws StatusCodeError {
        return taskService.search(rawQueryParameters, principal, true, false);
    }

    public FormRequest saveForm(Process process, FormRequest formRequest, MultipartBody body) throws MisconfiguredProcessException, StatusCodeError {
        return doSaveForm(process, formRequest, body, MultipartBody.class);
    }

    public FormRequest submitForm(Process process, FormRequest formRequest, RequestDetails requestDetails, MultivaluedMap<String, String> formData) throws MisconfiguredProcessException, StatusCodeError {
        return doSubmitForm(process, formRequest, requestDetails, formData, Map.class);
    }

    public FormRequest submitForm(Process process, FormRequest formRequest, RequestDetails requestDetails, MultipartBody body) throws MisconfiguredProcessException, StatusCodeError {
        return doSubmitForm(process, formRequest, requestDetails, body, MultipartBody.class);
    }

    public void validateForm(Process process, FormRequest formRequest, MultivaluedMap<String, String> formData, String validationId) throws MisconfiguredProcessException, StatusCodeError {
        doValidateForm(process, formRequest, validationId, formData, Map.class);
    }

    public void validateForm(Process process, FormRequest formRequest, MultipartBody body, String validationId) throws MisconfiguredProcessException, StatusCodeError {
        doValidateForm(process, formRequest, validationId, body, MultipartBody.class);
    }

    private <T> FormRequest doSaveForm(Process process, FormRequest formRequest, T data, Class<T> type) throws MisconfiguredProcessException, StatusCodeError {
        SubmissionHandler handler = submissionHandlerRegistry.handler(type);
        Entity principal = helper.getPrincipal();
        Task task = formRequest.getTaskId() != null ? taskService.read(process, formRequest.getTaskId(), true) : null;
        ProcessInstance instance = null;

        if (task != null && task.getProcessInstanceId() != null)
            instance = processInstanceService.read(process, task.getProcessInstanceId(), false);

        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, formRequest.getActivity(), null);
        Submission submission = handler.handle(data, template, formRequest, principal);

        processInstanceService.save(process, instance, task, template, submission);

        return formRequest;
    }

    private <T> FormRequest doSubmitForm(Process process, FormRequest formRequest, RequestDetails requestDetails, T data, Class<T> type) throws MisconfiguredProcessException, StatusCodeError {
        SubmissionHandler handler = submissionHandlerRegistry.handler(type);
        Entity principal = helper.getPrincipal();
        Task task = formRequest.getTaskId() != null ? taskService.read(process, formRequest.getTaskId(), true) : null;
        String processInstanceId = null;

        if (task != null)
            processInstanceId = task.getProcessInstanceId();

        if (StringUtils.isEmpty(processInstanceId))
            processInstanceId = formRequest.getProcessInstanceId();

        ProcessInstance instance = null;
        if (StringUtils.isNotEmpty(processInstanceId))
            instance = processInstanceService.read(process, task.getProcessInstanceId(), false);

        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, formRequest.getActivity(), null);
        Submission submission = handler.handle(data, template, formRequest, principal);

        ActionType action = submission.getAction();
        if (action == null)
            action = ActionType.COMPLETE;

        FormRequest nextFormRequest = null;
        switch (action) {
            case COMPLETE:
                ProcessInstance stored = processInstanceService.submit(process, instance, task, template, submission);
                nextFormRequest = requestService.create(requestDetails, process, stored, task, action);
                return nextFormRequest;

            case REJECT:
                stored = processInstanceService.reject(process, instance, task, template, submission);
                nextFormRequest = requestService.create(requestDetails, process, stored, task, action);
                return nextFormRequest;

            case SAVE:
                processInstanceService.save(process, instance, task, template, submission);
                return formRequest;

            case VALIDATE:
                validationService.validate(process, instance, task, template, submission, true);
                return formRequest;
        }

        return null;
    }

    private <T> void doValidateForm(Process process, FormRequest formRequest, String validationId, T data, Class<T> type) throws MisconfiguredProcessException, StatusCodeError {
        SubmissionHandler handler = submissionHandlerRegistry.handler(type);
        Entity principal = helper.getPrincipal();
        Task task = formRequest.getTaskId() != null ? taskService.read(process, formRequest.getTaskId(), true) : null;
        ProcessInstance instance = null;

        if (task != null && task.getProcessInstanceId() != null)
            instance = processInstanceService.read(process, task.getProcessInstanceId(), false);

        Activity activity = formRequest.getActivity();

        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, activity, validationId);
        Submission submission = handler.handle(data, template, formRequest, principal);
        validationService.validate(process, instance, task, template, submission, true);
    }
}
