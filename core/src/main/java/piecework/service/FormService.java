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
import piecework.model.RequestDetails;
import piecework.enumeration.ActionType;
import piecework.exception.*;
import piecework.handler.RequestHandler;
import piecework.handler.SubmissionHandler;
import piecework.model.*;
import piecework.model.Process;
import piecework.validation.FormValidation;
import piecework.validation.SubmissionTemplate;
import piecework.validation.SubmissionTemplateFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
@Service
public class FormService {

    private static final Logger LOG = Logger.getLogger(FormService.class);

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    RequestHandler requestHandler;

    @Autowired
    SubmissionHandler submissionHandler;

    @Autowired
    SubmissionTemplateFactory submissionTemplateFactory;

    @Autowired
    TaskService taskService;

    @Autowired
    ValidationService validationService;


    public SearchResults search(MultivaluedMap<String, String> rawQueryParameters) throws StatusCodeError {
        return taskService.allowedTasksDirect(rawQueryParameters, true, false);
    }

    public FormRequest saveForm(Process process, FormRequest formRequest, MultipartBody body) throws StatusCodeError {
        Task task = formRequest.getTaskId() != null ? taskService.allowedTask(process, formRequest.getTaskId(), true) : null;
        ProcessInstance instance = null;

        if (task != null && task.getProcessInstanceId() != null)
            instance = processInstanceService.read(process, task.getProcessInstanceId(), false);

        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, formRequest.getActivity(), null);
        Submission submission = submissionHandler.handle(process, template, body, formRequest);

        processInstanceService.save(process, instance, task, template, submission);

        return formRequest;
    }

    public FormRequest submitForm(Process process, FormRequest formRequest, RequestDetails requestDetails, MultipartBody body) throws StatusCodeError {
        Task task = formRequest.getTaskId() != null ? taskService.allowedTask(process, formRequest.getTaskId(), true) : null;
        String processInstanceId = null;

        if (task != null)
            processInstanceId = task.getProcessInstanceId();

        if (StringUtils.isEmpty(processInstanceId))
            processInstanceId = formRequest.getProcessInstanceId();

        ProcessInstance instance = null;
        if (StringUtils.isNotEmpty(processInstanceId))
            instance = processInstanceService.read(process, task.getProcessInstanceId(), false);

        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, formRequest.getActivity(), null);
        Submission submission = submissionHandler.handle(process, template, body, formRequest);

        ActionType action = submission.getAction();
        if (action == null)
            action = ActionType.COMPLETE;

        FormRequest nextFormRequest = null;
        switch (action) {
            case COMPLETE:
                ProcessInstance stored = processInstanceService.submit(process, instance, task, template, submission);
                nextFormRequest = requestHandler.create(requestDetails, process, stored, task, action);
                return nextFormRequest;

            case REJECT:
                stored = processInstanceService.reject(process, instance, task, template, submission);
                nextFormRequest = requestHandler.create(requestDetails, process, stored, task, action);
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

    public void validateForm(Process process, FormRequest formRequest, MultipartBody body, String validationId) throws StatusCodeError {

        Task task = formRequest.getTaskId() != null ? taskService.allowedTask(process, formRequest.getTaskId(), true) : null;
        ProcessInstance instance = null;

        if (task != null && task.getProcessInstanceId() != null)
            instance = processInstanceService.read(process, task.getProcessInstanceId(), false);

        Activity activity = formRequest.getActivity();

        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, activity, validationId);

        Submission submission = submissionHandler.handle(process, template, body, formRequest);

        validationService.validate(process, instance, task, template, submission, true);
    }

}
