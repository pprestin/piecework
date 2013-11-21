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

import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import piecework.*;
import piecework.authorization.AuthorizationRole;
import piecework.model.RequestDetails;
import piecework.enumeration.ActionType;
import piecework.form.LegacyFormFactory;
import piecework.handler.SubmissionHandler;
import piecework.persistence.concrete.ExportInstanceProvider;
import piecework.service.ProcessHistoryService;
import piecework.service.ProcessInstanceService;
import piecework.service.ProcessService;
import piecework.service.ValuesService;
import piecework.ui.ExportStreamingOutput;
import piecework.util.ManyMap;
import piecework.validation.SubmissionTemplate;
import piecework.validation.SubmissionTemplateFactory;
import piecework.exception.*;
import piecework.model.*;
import piecework.model.Process;
import piecework.process.*;
import piecework.identity.IdentityHelper;
import piecework.resource.ProcessInstanceResource;
import piecework.security.Sanitizer;
import piecework.model.SearchResults;
import piecework.common.ViewContext;
import piecework.handler.RequestHandler;
import piecework.security.SecuritySettings;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.service.TaskService;
import piecework.ui.StreamingAttachmentContent;
import piecework.util.ProcessInstanceUtility;

import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
@Service
public class ProcessInstanceResourceVersion1 implements ProcessInstanceResource {

    private static final Logger LOG = Logger.getLogger(ProcessInstanceResourceVersion1.class);

    @Autowired
    AttachmentService attachmentService;

    @Autowired
    IdentityHelper helper;

    @Autowired
    ProcessHistoryService historyService;

    @Autowired
    ProcessService processService;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    RequestHandler requestHandler;

    @Autowired
    SubmissionHandler submissionHandler;

    @Autowired
    SubmissionTemplateFactory submissionTemplateFactory;

	@Autowired
	Sanitizer sanitizer;

    @Autowired
    SecuritySettings securitySettings;

    @Autowired
    TaskService taskService;

    @Autowired
    ValuesService valuesService;

    @Autowired
    Versions versions;

    @Override
    public Response activate(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawReason) throws StatusCodeError {
        Entity principal = helper.getPrincipal();
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, false);
        String reason = sanitizer.sanitize(rawReason);

        if (!principal.hasRole(process, AuthorizationRole.OVERSEER) && !taskService.hasAllowedTask(process, instance, principal, false))
            throw new ForbiddenError(Constants.ExceptionCodes.task_required);

        processInstanceService.activate(process, instance, reason);
        return Response.noContent().build();
    }

    @Override
    public Response activate(String rawProcessDefinitionKey, String rawProcessInstanceId, OperationDetails details) throws StatusCodeError {
        return activate(rawProcessDefinitionKey, rawProcessInstanceId, details.getReason());
    }

    @Override
    public Response attach(String rawProcessDefinitionKey, String rawProcessInstanceId, MultivaluedMap<String, String> formData) throws StatusCodeError {
        Entity principal = helper.getPrincipal();
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, false);

        Task task = taskService.allowedTask(process, instance, principal, true);
        if (task == null)
            throw new ForbiddenError();

        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, task);
        Submission submission = submissionHandler.handle(process, template, formData);

        ProcessInstance persisted = attachmentService.attach(process, instance, task, template, submission);

        SearchResults searchResults = attachmentService.search(process, persisted, new AttachmentQueryParameters());
        return Response.ok(searchResults).build();
    }

    @Override
    public Response attach(String rawProcessDefinitionKey, String rawProcessInstanceId, MultipartBody body) throws StatusCodeError {
        Entity principal = helper.getPrincipal();
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, false);

        Task task = taskService.allowedTask(process, instance, principal, true);
        if (task == null)
            throw new ForbiddenError(Constants.ExceptionCodes.task_required);

        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, task);
        Submission submission = submissionHandler.handle(process, template, body);

        ProcessInstance persisted = attachmentService.attach(process, instance, task, template, submission);

        SearchResults searchResults = attachmentService.search(process, persisted, new AttachmentQueryParameters());
        return Response.ok(searchResults).build();
    }

    @Override
    public Response attachments(String rawProcessDefinitionKey, String rawProcessInstanceId, AttachmentQueryParameters queryParameters) throws StatusCodeError {
        Entity principal = helper.getPrincipal();
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, true);

        if (!taskService.hasAllowedTask(process, instance, principal, false))
            throw new ForbiddenError();

        SearchResults searchResults = attachmentService.search(process, instance, queryParameters);
        return Response.ok(searchResults).build();
    }

    @Override
    public Response attachment(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawAttachmentId) throws StatusCodeError {
        Entity principal = helper.getPrincipal();
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, true);
        String attachmentId = sanitizer.sanitize(rawAttachmentId);

        if (!taskService.hasAllowedTask(process, instance, principal, true))
            throw new ForbiddenError();

        StreamingAttachmentContent content = attachmentService.content(process, instance, attachmentId);

        if (content == null)
            throw new NotFoundError(Constants.ExceptionCodes.attachment_does_not_exist, attachmentId);

        String contentDisposition = new StringBuilder("attachment; filename=").append(content.getAttachment().getDescription()).toString();
        return Response.ok(content, content.getAttachment().getContentType()).header("Content-Disposition", contentDisposition).build();
    }

    @Override
    public Response cancel(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawReason) throws StatusCodeError {
        Entity principal = helper.getPrincipal();
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, false);
        String reason = sanitizer.sanitize(rawReason);

        if (!instance.isInitiator(principal) && !principal.hasRole(process, AuthorizationRole.OVERSEER))
            throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);

        processInstanceService.cancel(process, instance, reason);
        return Response.noContent().build();
    }

    @Override
    public Response cancel(String rawProcessDefinitionKey, String rawProcessInstanceId, OperationDetails details) throws StatusCodeError {
        return cancel(rawProcessDefinitionKey, rawProcessInstanceId, details.getReason());
    }

    @Override
	public Response create(MessageContext context, String rawProcessDefinitionKey, Submission rawSubmission) throws StatusCodeError {
        Process process = processService.read(rawProcessDefinitionKey);

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        FormRequest formRequest = requestHandler.create(requestDetails, process);
        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, formRequest.getActivity(), null);
        Submission submission = submissionHandler.handle(process, template, rawSubmission, formRequest, ActionType.CREATE);
        ProcessInstance instance = processInstanceService.submit(process, null, null, template, submission);

        return Response.ok(new ProcessInstance.Builder(instance).build(versions.getVersion1())).build();
	}
	
	@Override
	public Response create(MessageContext context, String rawProcessDefinitionKey, MultivaluedMap<String, String> formData) throws StatusCodeError {
        Process process = processService.read(rawProcessDefinitionKey);

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        FormRequest formRequest = requestHandler.create(requestDetails, process);
        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, formRequest.getActivity(), null);
        Submission submission = submissionHandler.handle(process, template, formData);
        ProcessInstance instance = processInstanceService.submit(process, null, null, template, submission);

        return Response.ok(new ProcessInstance.Builder(instance).build(versions.getVersion1())).build();
	}

	@Override
	public Response createMultipart(MessageContext context, String rawProcessDefinitionKey, MultipartBody body) throws StatusCodeError {
        Process process = processService.read(rawProcessDefinitionKey);

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        FormRequest formRequest = requestHandler.create(requestDetails, process);
        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, formRequest.getActivity(), null);
        Submission submission = submissionHandler.handle(process, template, body, formRequest);
        ProcessInstance instance = processInstanceService.submit(process, null, null, template, submission);

        return Response.ok(new ProcessInstance.Builder(instance).build(versions.getVersion1())).build();
	}

    @Override
    public Response detach(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawAttachmentId) throws StatusCodeError {
        Entity principal = helper.getPrincipal();
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, false);
        String attachmentId = sanitizer.sanitize(rawAttachmentId);

        Task task = taskService.allowedTask(process, instance, principal, true);
        if (principal.getEntityType() != Entity.EntityType.SYSTEM && task == null)
            throw new ForbiddenError();

        attachmentService.delete(process, instance, attachmentId);
        return Response.noContent().build();
    }

    @Override
    public Response history(String rawProcessDefinitionKey, String rawProcessInstanceId) throws StatusCodeError {
        History history = historyService.read(rawProcessDefinitionKey, rawProcessInstanceId);
        return Response.ok(history).build();
    }

    @Override
	public Response read(String rawProcessDefinitionKey, String rawProcessInstanceId) throws StatusCodeError {
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
		String processInstanceId = sanitizer.sanitize(rawProcessInstanceId);
		
		Process process = processService.read(processDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, processInstanceId, false);

        ProcessInstance.Builder builder = new ProcessInstance.Builder(instance)
                .processDefinitionKey(processDefinitionKey)
                .processDefinitionLabel(process.getProcessDefinitionLabel());

        return Response.ok(builder.build(versions.getVersion1())).build();
	}

    @Override
    public Response suspend(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawReason) throws StatusCodeError {
        Entity principal = helper.getPrincipal();
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, false);
        String reason = sanitizer.sanitize(rawReason);

        if (!principal.hasRole(process, AuthorizationRole.OVERSEER) && !taskService.hasAllowedTask(process, instance, principal, true))
            throw new ForbiddenError(Constants.ExceptionCodes.active_task_required);

        processInstanceService.suspend(process, instance, reason);
        return Response.noContent().build();
    }

    @Override
    public Response suspend(String rawProcessDefinitionKey, String rawProcessInstanceId, OperationDetails details) throws StatusCodeError {
        return suspend(rawProcessDefinitionKey, rawProcessInstanceId, details.getReason());
    }

    @Override
    public Response update(String rawProcessDefinitionKey, String rawProcessInstanceId, ProcessInstance instance) throws StatusCodeError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        String processInstanceId = sanitizer.sanitize(rawProcessInstanceId);

        processInstanceService.update(processDefinitionKey, processInstanceId, instance);

        ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
        ViewContext context = versions.getVersion1();
        String location = context != null ? context.getApplicationUri(instance.getProcessDefinitionKey(), instance.getProcessInstanceId()) : null;
        if (location != null)
            responseBuilder.location(UriBuilder.fromPath(location).build());
        return responseBuilder.build();
    }

    @Override
	public Response delete(String rawProcessDefinitionKey, String rawProcessInstanceId) throws StatusCodeError {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, false);
        String reason = null;

        processInstanceService.cancel(process, instance, reason);

        ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
        ViewContext context = versions.getVersion1();
        String location = context != null ? context.getApplicationUri(instance.getProcessDefinitionKey(), instance.getProcessInstanceId()) : null;
        if (location != null)
            responseBuilder.location(UriBuilder.fromPath(location).build());
        return responseBuilder.build();
	}

	@Override
	public Response search(MessageContext context) throws StatusCodeError {
        Entity principal = helper.getPrincipal();
        UriInfo uriInfo = context.getContext(UriInfo.class);
        List<MediaType> mediaTypes = context.getHttpHeaders().getAcceptableMediaTypes();

		MultivaluedMap<String, String> rawQueryParameters = uriInfo != null ? uriInfo.getQueryParameters() : null;

        if (mediaTypes != null && mediaTypes.contains(new MediaType("text", "csv"))) {
            String fileName = "export.csv";
            ExportInstanceProvider provider = processInstanceService.exportProvider(rawQueryParameters, principal);
            ExportStreamingOutput exportStreamingOutput = new ExportStreamingOutput(provider);
            return Response.ok(exportStreamingOutput, "text/csv").header("Content-Disposition", "attachment; filename=" + fileName).build();
        } else {
            SearchResults results = processInstanceService.search(rawQueryParameters, principal);
            return Response.ok(results).build();
        }
	}

    @Override
    public Response remove(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawFieldName, String rawValueId) throws StatusCodeError {
        Entity principal = helper.getPrincipal();
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, false);
        String fieldName = sanitizer.sanitize(rawFieldName);
        String valueId = sanitizer.sanitize(rawValueId);

        if (!principal.hasRole(process, AuthorizationRole.OVERSEER) && !taskService.hasAllowedTask(process, instance, principal, true))
            throw new ForbiddenError(Constants.ExceptionCodes.active_task_required);

        valuesService.delete(process, instance, fieldName, valueId);
        return Response.noContent().build();
    }

    @Override
    public Response value(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawFieldName, String rawValueId) throws StatusCodeError {
        Entity principal = helper.getPrincipal();
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, false);
        String fieldName = sanitizer.sanitize(rawFieldName);
        String valueId = sanitizer.sanitize(rawValueId);

        if (!principal.hasRole(process, AuthorizationRole.OVERSEER) && !taskService.hasAllowedTask(process, instance, principal, true))
            throw new ForbiddenError(Constants.ExceptionCodes.active_task_required);

        return valuesService.read(process, instance, fieldName, valueId);
    }

    @Override
    public Response value(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawFieldName, String value) throws StatusCodeError {
        Entity principal = helper.getPrincipal();
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, false);
        String fieldName = sanitizer.sanitize(rawFieldName);

        Task task = taskService.allowedTask(process, instance, principal, true);
        if (task == null && principal.getEntityType() != Entity.EntityType.SYSTEM)
            throw new ForbiddenError(Constants.ExceptionCodes.active_task_required);

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        FormRequest formRequest = requestHandler.create(requestDetails, process, instance, task, ActionType.CREATE);

        Field field = LegacyFormFactory.getField(process, task, fieldName);
        if (field == null)
            throw new NotFoundError();

        ManyMap<String, String> formValueMap = new ManyMap<String, String>();
        formValueMap.putOne(fieldName, value);
        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(field);
        Submission submission = submissionHandler.handle(process, template, formValueMap);
        processInstanceService.save(process, instance, task, template, submission);

        return Response.noContent().build();
    }

    @Override
    public Response value(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawFieldName, MultipartBody body) throws StatusCodeError {
        Entity principal = helper.getPrincipal();
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, false);
        String fieldName = sanitizer.sanitize(rawFieldName);

        Task task = taskService.allowedTask(process, instance, principal, true);
        if (task == null && principal.getEntityType() != Entity.EntityType.SYSTEM)
            throw new ForbiddenError(Constants.ExceptionCodes.active_task_required);

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        FormRequest formRequest = requestHandler.create(requestDetails, process, instance, task, ActionType.CREATE);
        Activity activity = formRequest.getActivity();
        Map<String, Field> fieldMap = activity.getFieldKeyMap();

        Field field = fieldMap.get(fieldName);
        if (field == null)
            throw new NotFoundError();

        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(field);
        Submission submission = submissionHandler.handle(process, template, body);
        ProcessInstance stored = processInstanceService.save(process, instance, task, template, submission);

        Map<String, List<Value>> data = stored.getData();

        ViewContext version1 = versions.getVersion1();
        String location = null;
        if (data != null) {
            File file = ProcessInstanceUtility.firstFile(fieldName, data);

            if (file != null) {
                location =
                    new File.Builder(file, new PassthroughSanitizer())
                        .processDefinitionKey(process.getProcessDefinitionKey())
                        .processInstanceId(stored.getProcessInstanceId())
                        .fieldName(fieldName)
                        .build(version1)
                        .getLink();
            }
        }

        ResponseBuilder builder = Response.noContent();

        if (location != null)
            builder.header(HttpHeaders.LOCATION, location);

        return builder.build();
    }

    @Override
    public Response values(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawFieldName) throws StatusCodeError {
        Entity principal = helper.getPrincipal();
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, false);
        String fieldName = sanitizer.sanitize(rawFieldName);

        if (!principal.hasRole(process, AuthorizationRole.OVERSEER) && !taskService.hasAllowedTask(process, instance, principal, true))
            throw new ForbiddenError(Constants.ExceptionCodes.active_task_required);

        List<Value> files = valuesService.searchValues(process, instance, fieldName);
        SearchResults searchResults = new SearchResults.Builder()
                .items(files)
                .build();

        return Response.ok(searchResults).build();
    }

    @Override
    public String getVersion() {
        return versions.getVersion1().getVersion();
    }

}
