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
import piecework.Constants;
import piecework.Versions;
import piecework.common.RequestDetails;
import piecework.engine.ProcessEngineFacade;
import piecework.enumeration.ActionType;
import piecework.handler.SubmissionHandler;
import piecework.validation.SubmissionTemplate;
import piecework.validation.SubmissionTemplateFactory;
import piecework.model.SearchResults;
import piecework.common.ViewContext;
import piecework.exception.*;
import piecework.identity.IdentityDetails;
import piecework.identity.IdentityHelper;
import piecework.security.SecuritySettings;
import piecework.engine.exception.ProcessEngineException;
import piecework.handler.RequestHandler;
import piecework.handler.ResponseHandler;
import piecework.validation.FormValidation;
import piecework.model.*;
import piecework.model.Process;
import piecework.security.Sanitizer;

import javax.servlet.http.HttpServletRequest;
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
    ProcessEngineFacade facade;

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


    public Response delete(HttpServletRequest request, Process process, String rawRequestId, MultipartBody body) throws StatusCodeError {
        String requestId = sanitizer.sanitize(rawRequestId);

        if (StringUtils.isEmpty(requestId))
            throw new ForbiddenError(Constants.ExceptionCodes.request_id_required);

        String taskId;
        RequestDetails requestDetails = requestDetails(request);
        try {
            FormRequest formRequest = requestHandler.handle(requestDetails, requestId);
            taskId = formRequest.getTaskId();
        } catch (NotFoundError e) {
            taskId = requestId;
        }

        if (StringUtils.isEmpty(taskId))
            throw new ForbiddenError(Constants.ExceptionCodes.task_id_required);

        IdentityDetails user = helper.getAuthenticatedPrincipal();
        String participantId = user != null ? user.getInternalId() : null;

        try {
            Task task = taskService.allowedTask(process, taskId, true);
            ProcessInstance processInstance = processInstanceService.read(process, task.getProcessInstanceId(), false);
            facade.cancel(process, processInstance);

        } catch (ProcessEngineException e) {
            LOG.error("Could not delete task", e);
        }

        return Response.noContent().build();
    }

    public Response provideFormResponse(HttpServletRequest request, Process process, List<PathSegment> pathSegments) throws StatusCodeError {
        String requestId = null;
        boolean isStatic = false;
        boolean isSubmissionResource;

        if (pathSegments != null && !pathSegments.isEmpty()) {
            Iterator<PathSegment> pathSegmentIterator = pathSegments.iterator();
            requestId = sanitizer.sanitize(pathSegmentIterator.next().getPath());

            isSubmissionResource = StringUtils.isNotEmpty(requestId) && requestId.equals("submission");

            if (isSubmissionResource && pathSegmentIterator.hasNext()) {
                requestId = sanitizer.sanitize(pathSegmentIterator.next().getPath());
            }
            isStatic = StringUtils.isNotEmpty(requestId) && requestId.equals("static");

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
        }

        RequestDetails requestDetails = requestDetails(request);

        FormRequest formRequest = null;

        if (StringUtils.isNotEmpty(requestId)) {
            try {
                formRequest = requestHandler.create(requestDetails, process, requestId, null);
            } catch (NotFoundError e) {
                formRequest = requestHandler.handle(requestDetails, requestId);
            }
        } else
            formRequest = requestHandler.create(requestDetails, process);

        if (formRequest.getProcessDefinitionKey() == null || process.getProcessDefinitionKey() == null || !formRequest.getProcessDefinitionKey().equals(process.getProcessDefinitionKey()))
            throw new BadRequestError();

        return responseHandler.handle(formRequest, process);
    }

    public SearchResults search(MultivaluedMap<String, String> rawQueryParameters, ViewContext viewContext) throws StatusCodeError {

        SearchResults results = taskService.allowedTasksDirect(rawQueryParameters);

        SearchResults.Builder resultsBuilder = new SearchResults.Builder()
                .resourceLabel("Tasks")
                .resourceName(Form.Constants.ROOT_ELEMENT_NAME)
                .link(viewContext.getApplicationUri(Form.Constants.ROOT_ELEMENT_NAME))
                .parameters(results.getParameters());

        List<?> definitions = results.getDefinitions();
        if (definitions != null) {
            for (Object definition : definitions) {
                Process allowedProcess = Process.class.cast(definition);
                resultsBuilder.definition(new Form.Builder().processDefinitionKey(allowedProcess.getProcessDefinitionKey()).task(new Task.Builder().processDefinitionKey(allowedProcess.getProcessDefinitionKey()).processDefinitionLabel(allowedProcess.getProcessDefinitionLabel()).build(viewContext)).build(viewContext));
            }
        }

        List<?> items = results.getList();
        if (items != null && !items.isEmpty()) {
            ViewContext version = versions.getVersion1();

            for (Object item : items) {
                Task task = Task.class.cast(item);
                resultsBuilder.item(new Form.Builder()
                        .formInstanceId(task.getTaskInstanceId())
                        .taskSubresources(task.getProcessDefinitionKey(), task, version)
                        .processDefinitionKey(task.getProcessDefinitionKey())
                        .instanceSubresources(task.getProcessDefinitionKey(), task.getProcessInstanceId(), null, 0, version)
                        .build(viewContext));
            }
        }

        resultsBuilder.firstResult(results.getFirstResult());
        resultsBuilder.maxResults(results.getMaxResults());
        resultsBuilder.total(Long.valueOf(results.getTotal()));

        return resultsBuilder.build(viewContext);
    }

    public Response saveForm(HttpServletRequest request, ViewContext viewContext, Process process, String rawRequestId, MultipartBody body) throws StatusCodeError {
        String requestId = sanitizer.sanitize(rawRequestId);

        if (StringUtils.isEmpty(requestId))
            throw new ForbiddenError(Constants.ExceptionCodes.request_id_required);

        RequestDetails requestDetails = requestDetails(request);
        FormRequest formRequest = requestHandler.handle(requestDetails, requestId);
        Task task = formRequest.getTaskId() != null ? taskService.allowedTask(process, formRequest.getTaskId(), true) : null;
        ProcessInstance instance = null;

        if (task != null && task.getProcessInstanceId() != null)
            instance = processInstanceService.read(process, task.getProcessInstanceId(), false);

        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, formRequest.getScreen());
        Submission submission = submissionHandler.handle(process, template, body, formRequest);

        processInstanceService.save(process, instance, task, template, submission);

        return responseHandler.redirect(formRequest);
    }

    public Response submitForm(HttpServletRequest request, Process process, String rawRequestId, MultipartBody body) throws StatusCodeError {
        String requestId = sanitizer.sanitize(rawRequestId);

        if (StringUtils.isEmpty(requestId))
            throw new ForbiddenError(Constants.ExceptionCodes.request_id_required);

        RequestDetails requestDetails = requestDetails(request);
        FormRequest formRequest = requestHandler.handle(requestDetails, requestId);

        Task task = formRequest.getTaskId() != null ? taskService.allowedTask(process, formRequest.getTaskId(), true) : null;
        String processInstanceId = null;

        if (task != null)
            processInstanceId = task.getProcessInstanceId();

        if (StringUtils.isEmpty(processInstanceId))
            processInstanceId = formRequest.getProcessInstanceId();

        ProcessInstance instance = null;
        if (StringUtils.isNotEmpty(processInstanceId))
            instance = processInstanceService.read(process, task.getProcessInstanceId(), false);

        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, formRequest.getScreen());
        Submission submission = submissionHandler.handle(process, template, body, formRequest);

        try {
            ActionType action = submission.getAction();
            if (action == null)
                action = ActionType.COMPLETE;

            switch (action) {
                case COMPLETE:
                    ProcessInstance stored = processInstanceService.submit(process, instance, task, template, submission);

                    FormRequest nextFormRequest = null;

                    if (!formRequest.getSubmissionType().equals(Constants.SubmissionTypes.FINAL))
                        nextFormRequest = requestHandler.create(requestDetails, process, stored, Task.class.cast(null), formRequest, action);

                    // FIXME: If the request handler doesn't have another request to process, then provide the generic thank you page back to the user
                    if (nextFormRequest == null) {
                        return Response.noContent().build();
                    }

                    return responseHandler.redirect(nextFormRequest);

                case REJECT:
                    stored = processInstanceService.reject(process, instance, task, template, submission);

                    nextFormRequest = null;

                    if (!formRequest.getSubmissionType().equals(Constants.SubmissionTypes.FINAL))
                        nextFormRequest = requestHandler.create(requestDetails, process, stored, Task.class.cast(null), formRequest, action);

                    // FIXME: If the request handler doesn't have another request to process, then provide the generic thank you page back to the user
                    if (nextFormRequest == null) {
                        return Response.noContent().build();
                    }

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

            return responseHandler.handle(formRequest, process, instance, task, validation);
        }

        return Response.noContent().build();
    }

    public Response validateForm(HttpServletRequest request, ViewContext viewContext, Process process, MultipartBody body, String rawRequestId, String rawValidationId) throws StatusCodeError {
        String requestId = sanitizer.sanitize(rawRequestId);
        String validationId = sanitizer.sanitize(rawValidationId);

        if (StringUtils.isEmpty(requestId))
            throw new ForbiddenError(Constants.ExceptionCodes.request_id_required);

        RequestDetails requestDetails = requestDetails(request);
        FormRequest formRequest = requestHandler.handle(requestDetails, requestId);
        Task task = formRequest.getTaskId() != null ? taskService.allowedTask(process, formRequest.getTaskId(), true) : null;
        ProcessInstance instance = null;

        if (task != null && task.getProcessInstanceId() != null)
            instance = processInstanceService.read(process, task.getProcessInstanceId(), false);

        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, formRequest.getScreen(), validationId);
        Submission submission = submissionHandler.handle(process, template, body, formRequest);

        validationService.validate(process, instance, task, template, submission, true);

        return Response.noContent().build();
    }

    private RequestDetails requestDetails(HttpServletRequest request) {
        return new RequestDetails.Builder(request, securitySettings).build();
    }

    private Response readStatic(Process process, String name) throws StatusCodeError {

        String base = process.getBase();

        if (StringUtils.isNotEmpty(base)) {
            Content content = responseHandler.content(base + "/" + name);

            if (content != null)
                return Response.ok(content.getInputStream()).type(content.getContentType()).build();
        }

        throw new NotFoundError();
    }
}
