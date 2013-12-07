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
package piecework.resource.concrete;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.Versions;
import piecework.service.ProcessInstanceService;
import piecework.service.ProcessService;
import piecework.model.RequestDetails;
import piecework.enumeration.ActionType;
import piecework.service.ValidationService;
import piecework.submission.SubmissionHandler;
import piecework.submission.SubmissionHandlerRegistry;
import piecework.submission.SubmissionTemplate;
import piecework.submission.SubmissionTemplateFactory;
import piecework.model.SearchResults;
import piecework.common.ViewContext;
import piecework.exception.*;
import piecework.identity.IdentityHelper;
import piecework.security.SecuritySettings;
import piecework.service.TaskService;
import piecework.service.RequestService;
import piecework.model.*;
import piecework.security.Sanitizer;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.resource.TaskResource;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * @author James Renfro
 */
@Service
public class TaskResourceVersion1 implements TaskResource {

    private static final Logger LOG = Logger.getLogger(TaskResourceVersion1.class);

    @Autowired
    IdentityHelper helper;

    @Autowired
    ProcessService processService;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    RequestService requestService;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    SecuritySettings securitySettings;

    @Autowired
    SubmissionHandlerRegistry submissionHandlerRegistry;

    @Autowired
    SubmissionTemplateFactory submissionTemplateFactory;

    @Autowired
    TaskService taskService;

    @Autowired
    ValidationService validationService;

    @Autowired
    Versions versions;

    @Override
    public Response complete(String rawProcessDefinitionKey, String rawTaskId, String rawAction, MessageContext context, Submission rawSubmission) throws StatusCodeError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        String taskId = sanitizer.sanitize(rawTaskId);
        String action = sanitizer.sanitize(rawAction);

        piecework.model.Process process = processService.read(processDefinitionKey);

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();

        String actingUser = null;
        Entity principal = helper.getPrincipal();
        if (principal != null) {
            if (principal.getEntityType() == Entity.EntityType.SYSTEM && StringUtils.isNotEmpty(requestDetails.getActAsUser()))
                actingUser = requestDetails.getActAsUser();
            else
                actingUser = principal.getEntityId();
        }

        Task task = taskId != null ? taskService.read(process, taskId, true) : null;

        if (task == null)
            throw new NotFoundError();

        ProcessInstance instance = null;

        if (task != null && task.getProcessInstanceId() != null)
            instance = processInstanceService.read(process, task.getProcessInstanceId(), false);

        ActionType validatedAction = ActionType.COMPLETE;
        if (StringUtils.isNotEmpty(action)) {
            try {
                validatedAction = ActionType.valueOf(action.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestError(Constants.ExceptionCodes.task_action_invalid);
            }
        }

        SubmissionHandler handler = submissionHandlerRegistry.handler(Submission.class);
        try {
            FormRequest formRequest = requestService.create(requestDetails, process, instance, task, validatedAction);
            SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, formRequest.getActivity(), null);
            Submission submission = handler.handle(rawSubmission, template, formRequest, principal);

            if (submission != null && submission.getAction() != null)
                validatedAction = submission.getAction();

            switch (validatedAction) {
                case ASSIGN:
                    processInstanceService.assign(process, instance, task, submission.getAssignee());
                    break;
                case CLAIM:
                    processInstanceService.assign(process, instance, task, actingUser);
                    break;
                case SAVE:
                    processInstanceService.save(process, instance, task, template, submission);
                    break;
                case REJECT:
                    processInstanceService.reject(process, instance, task, template, submission);
                    break;
                case VALIDATE:
                    validationService.validate(process, instance, task, template, submission, true);
                    break;
                default:
                    processInstanceService.submit(process, instance, task, template, submission);
                    break;
            }
            return Response.noContent().build();
        } catch (MisconfiguredProcessException mpe) {
            LOG.error("Unable to complete task because process is misconfigured", mpe);
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
        }
    }

    public Response read(String rawProcessDefinitionKey, String rawTaskId) throws StatusCodeError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        String taskId = sanitizer.sanitize(rawTaskId);

        piecework.model.Process process = processService.read(processDefinitionKey);

        Task task = taskService.read(process, taskId, true);
        if (task == null)
            throw new NotFoundError();

        return Response.ok(new Task.Builder(task, new PassthroughSanitizer()).build(versions.getVersion1())).build();
    }

    @Override
    public Response update(String processDefinitionKey, String taskId, HttpServletRequest request, Task task) throws StatusCodeError {
        throw new NotImplementedException();
    }

    @Override
    public SearchResults search(UriInfo uriInfo) throws StatusCodeError {
        MultivaluedMap<String, String> rawQueryParameters = uriInfo != null ? uriInfo.getQueryParameters() : null;

        return search(rawQueryParameters);
    }

    @Override
    public SearchResults search(MultivaluedMap<String, String> rawQueryParameters) throws StatusCodeError {
        Entity principal = helper.getPrincipal();
        SearchResults results = taskService.search(rawQueryParameters, principal, false, false);

        ViewContext version = versions.getVersion1();

        SearchResults.Builder resultsBuilder = new SearchResults.Builder().resourceName(Task.Constants.ROOT_ELEMENT_NAME)
                .resourceLabel("Tasks")
                .link(version.getApplicationUri(Task.Constants.ROOT_ELEMENT_NAME))
                .uri(version.getServiceUri(Task.Constants.ROOT_ELEMENT_NAME));

        PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
        List<?> items = results.getList();
        if (items != null && !items.isEmpty()) {
            for (Object item : items) {
                Task task = Task.class.cast(item);
                resultsBuilder.item(new Task.Builder(task, passthroughSanitizer).build(version));
            }
        }

        resultsBuilder.firstResult(results.getFirstResult());
        resultsBuilder.maxResults(results.getMaxResults());
        resultsBuilder.total(Long.valueOf(results.getTotal()));

        return resultsBuilder.build(version);
    }

    @Override
    public String getVersion() {
        return versions.getVersion1().getVersion();
    }

}
