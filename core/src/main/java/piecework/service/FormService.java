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
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.CommandExecutor;
import piecework.Constants;
import piecework.Versions;
import piecework.common.RequestDetails;
import piecework.enumeration.ActionType;
import piecework.form.FormFactory;
import piecework.handler.SubmissionHandler;
import piecework.validation.SubmissionTemplate;
import piecework.validation.SubmissionTemplateFactory;
import piecework.model.SearchResults;
import piecework.common.ViewContext;
import piecework.exception.*;
import piecework.identity.IdentityHelper;
import piecework.security.SecuritySettings;
import piecework.handler.RequestHandler;
import piecework.handler.ResponseHandler;
import piecework.validation.FormValidation;
import piecework.model.*;
import piecework.model.Process;
import piecework.security.Sanitizer;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * This "service" is really just to abstract logic that is shared between the two different form resources.
 *
 * @author James Renfro
 */
@Service
public class FormService {

    private static final Logger LOG = Logger.getLogger(FormService.class);

    @Autowired
    CommandExecutor commandExecutor;

    @Autowired
    FormFactory formFactory;

    @Autowired
    IdentityHelper helper;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    RequestHandler requestHandler;

    @Autowired
    ResponseHandler responseHandler;

    @Autowired
    SecuritySettings securitySettings;

    @Autowired
    SubmissionHandler submissionHandler;

    @Autowired
    SubmissionTemplateFactory submissionTemplateFactory;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    TaskService taskService;

    @Autowired
    ValidationService validationService;

    @Autowired
    Versions versions;

    public Response startForm(MessageContext context, Process process) throws StatusCodeError {
        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();

        FormRequest formRequest = requestHandler.create(requestDetails, process);

        if (formRequest.getProcessDefinitionKey() == null || process.getProcessDefinitionKey() == null || !formRequest.getProcessDefinitionKey().equals(process.getProcessDefinitionKey()))
            throw new BadRequestError();

        return responseHandler.handle(requestDetails, formRequest, process);
    }

    public Form getForm(MessageContext context, Process process, String requestId) throws StatusCodeError {
        Entity principal = helper.getPrincipal();
        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();

        // Assume that we've been provided with a form request identifier
        FormRequest formRequest = requestHandler.handle(requestDetails, requestId);

        // But if it's null, then try to create a new form request -- this will assume that the requestId
        // is a taskId, and check to make sure that the user is authorized to create form requests for
        // that task
        if (formRequest == null)
            formRequest = requestHandler.create(requestDetails, process, requestId, null);

        if (formRequest.getProcessDefinitionKey() == null || process.getProcessDefinitionKey() == null || !formRequest.getProcessDefinitionKey().equals(process.getProcessDefinitionKey()))
            throw new BadRequestError();

        ActionType actionType = formRequest.getAction() != null ? formRequest.getAction() : ActionType.CREATE;

        return formFactory.form(formRequest, process, formRequest.getInstance(), formRequest.getTask(), null, actionType, principal);
    }

    public Response provideFormResponse(MessageContext context, Process process, List<PathSegment> pathSegments) throws StatusCodeError {
        if (pathSegments == null || pathSegments.isEmpty())
            return startForm(context, process);

        Iterator<PathSegment> pathSegmentIterator = pathSegments.iterator();
        String requestId = sanitizer.sanitize(pathSegmentIterator.next().getPath());

        boolean isStatic = StringUtils.isNotEmpty(requestId) && requestId.equals("static");

        if (pathSegmentIterator.hasNext()) {
            String staticResourceName = "";
            while (pathSegmentIterator.hasNext()) {
                staticResourceName += sanitizer.sanitize(pathSegmentIterator.next().getPath());
                if (pathSegmentIterator.hasNext())
                    staticResourceName += "/";
            }
            if (isStatic)
                return readStatic(process, staticResourceName);
        }

        if (StringUtils.isEmpty(requestId))
            throw new BadRequestError(Constants.ExceptionCodes.request_id_required);


        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();

        // Assume that we've been provided with a form request identifier
        FormRequest formRequest = requestHandler.handle(requestDetails, requestId);

        // But if it's null, then try to create a new form request -- this will assume that the requestId
        // is a taskId, and check to make sure that the user is authorized to create form requests for
        // that task
        if (formRequest == null)
            formRequest = requestHandler.create(requestDetails, process, requestId, null);

        if (formRequest.getProcessDefinitionKey() == null || process.getProcessDefinitionKey() == null || !formRequest.getProcessDefinitionKey().equals(process.getProcessDefinitionKey()))
            throw new BadRequestError();

        return responseHandler.handle(requestDetails, formRequest, process);
    }

    public SearchResults search(MultivaluedMap<String, String> rawQueryParameters, ViewContext viewContext) throws StatusCodeError {

        return taskService.allowedTasksDirect(rawQueryParameters, true);
    }

    public Response saveForm(MessageContext context, Process process, String rawRequestId, MultipartBody body) throws StatusCodeError {
        String requestId = sanitizer.sanitize(rawRequestId);

        if (StringUtils.isEmpty(requestId))
            throw new ForbiddenError(Constants.ExceptionCodes.request_id_required);

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();;
        FormRequest formRequest = requestHandler.handle(requestDetails, requestId);
        if (formRequest == null) {
            LOG.error("Forbidden: Attempting to save a form for a request id that doesn't exist");
            throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);
        }

        Task task = formRequest.getTaskId() != null ? taskService.allowedTask(process, formRequest.getTaskId(), true) : null;
        ProcessInstance instance = null;

        if (task != null && task.getProcessInstanceId() != null)
            instance = processInstanceService.read(process, task.getProcessInstanceId(), false);

        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, formRequest.getActivity(), null);
        Submission submission = submissionHandler.handle(process, template, body, formRequest);

        processInstanceService.save(process, instance, task, template, submission);

        return responseHandler.redirect(formRequest);
    }

    public Response submitForm(MessageContext context, Process process, String rawRequestId, MultipartBody body) throws StatusCodeError {
        String requestId = sanitizer.sanitize(rawRequestId);

        if (StringUtils.isEmpty(requestId))
            throw new ForbiddenError(Constants.ExceptionCodes.request_id_required);

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        FormRequest formRequest = requestHandler.handle(requestDetails, requestId);
        if (formRequest == null) {
            LOG.error("Forbidden: Attempting to submit a form for a request id that doesn't exist");
            throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);
        }

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

        try {
            FormRequest nextFormRequest = null;
            switch (action) {
                case COMPLETE:
                    ProcessInstance stored = processInstanceService.submit(process, instance, task, template, submission);
                    nextFormRequest = requestHandler.create(requestDetails, process, stored, task, action);
                    return responseHandler.redirect(nextFormRequest);

                case REJECT:
                    stored = processInstanceService.reject(process, instance, task, template, submission);
                    nextFormRequest = requestHandler.create(requestDetails, process, stored, task, action);
                    return responseHandler.redirect(nextFormRequest);

                case SAVE:
                    processInstanceService.save(process, instance, task, template, submission);
                    return responseHandler.redirect(formRequest);

                case VALIDATE:
                    validationService.validate(process, instance, task, template, submission, true);
                    return responseHandler.redirect(formRequest);
            }
        } catch (BadRequestError e) {
            FormValidation validation = e.getValidation();

            Map<String, List<Message>> results = validation.getResults();

            if (results != null && !results.isEmpty()) {
                for (Map.Entry<String, List<Message>> result : results.entrySet()) {
                    LOG.warn("Validation error " + result.getKey() + " : " + result.getValue().iterator().next().getText());
                }
            }

            List<MediaType> acceptableMediaTypes = requestDetails.getAcceptableMediaTypes();
            boolean isJSON = acceptableMediaTypes.size() == 1 && acceptableMediaTypes.contains(MediaType.APPLICATION_JSON_TYPE);

            if (isJSON)
                throw e;

            FormRequest invalidRequest = requestHandler.create(requestDetails, process, instance, task, ActionType.CREATE, validation);

            return responseHandler.redirect(invalidRequest);

            //return responseHandler.handle(requestDetails, invalidRequest, process, instance, task, validation);
        }

        return Response.noContent().build();
    }

    public Response validateForm(MessageContext context, Process process, MultipartBody body, String rawRequestId, String rawValidationId) throws StatusCodeError {
        String requestId = sanitizer.sanitize(rawRequestId);
        String validationId = sanitizer.sanitize(rawValidationId);

        if (StringUtils.isEmpty(requestId))
            throw new ForbiddenError(Constants.ExceptionCodes.request_id_required);

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        FormRequest formRequest = requestHandler.handle(requestDetails, requestId);
        if (formRequest == null) {
            LOG.error("Forbidden: Attempting to validate a form for a request id that doesn't exist");
            throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);
        }

        Task task = formRequest.getTaskId() != null ? taskService.allowedTask(process, formRequest.getTaskId(), true) : null;
        ProcessInstance instance = null;

        if (task != null && task.getProcessInstanceId() != null)
            instance = processInstanceService.read(process, task.getProcessInstanceId(), false);

        Activity activity = formRequest.getActivity();

        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, activity, validationId);

        Submission submission = submissionHandler.handle(process, template, body, formRequest);

        validationService.validate(process, instance, task, template, submission, true);

        return Response.noContent().build();
    }

    private Response readStatic(Process process, String name) throws StatusCodeError {
        ProcessDeployment detail = process.getDeployment();
        if (detail == null)
            throw new ConflictError();

        String base = detail.getBase();

        if (StringUtils.isNotEmpty(base)) {
            Content content = responseHandler.content(base + "/" + name, Collections.<MediaType>emptyList());

            if (content != null)
                return Response.ok(content.getInputStream()).type(content.getContentType()).build();
        }

        throw new NotFoundError();
    }
}
