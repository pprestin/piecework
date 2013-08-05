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
package piecework.process.concrete;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import piecework.Constants;
import piecework.authorization.AuthorizationRole;
import piecework.common.RequestDetails;
import piecework.engine.ProcessEngineFacade;
import piecework.enumeration.OperationType;
import piecework.form.handler.SubmissionHandler;
import piecework.form.validation.SubmissionTemplate;
import piecework.form.validation.SubmissionTemplateFactory;
import piecework.exception.*;
import piecework.model.*;
import piecework.model.Process;
import piecework.process.*;
import piecework.security.Sanitizer;
import piecework.model.SearchResults;
import piecework.common.ViewContext;
import piecework.form.handler.RequestHandler;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.ui.StreamingAttachmentContent;
import piecework.form.FormFactory;
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
    Environment environment;

    @Autowired
    ResourceHelper helper;

    @Autowired
    ProcessService processService;

    @Autowired
    ProcessInstanceService processInstanceService;

	@Autowired
    ProcessEngineFacade facade;

    @Autowired
    RequestHandler requestHandler;

    @Autowired
    SubmissionHandler submissionHandler;

    @Autowired
    SubmissionTemplateFactory submissionTemplateFactory;

	@Autowired
	Sanitizer sanitizer;

    @Override
    public Response activate(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawReason) throws StatusCodeError {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId);
        String reason = sanitizer.sanitize(rawReason);

        if (!helper.hasRole(process, AuthorizationRole.OVERSEER) && !processInstanceService.userHasTask(process, instance, false))
            throw new ForbiddenError(Constants.ExceptionCodes.task_required);

        processInstanceService.operate(OperationType.ACTIVATION, process, instance, null, reason);
        return Response.noContent().build();
    }

    @Override
    public Response attach(String rawProcessDefinitionKey, String rawProcessInstanceId, MultivaluedMap<String, String> formData) throws StatusCodeError {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId);

        Task task = processInstanceService.userTask(process, instance, true);
        if (task == null)
            throw new ForbiddenError();

        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process);
        Submission submission = submissionHandler.handle(process, template, formData);

        processInstanceService.attach(process, instance, task, template, submission);
        return Response.noContent().build();
    }

    @Override
    public Response attach(String rawProcessDefinitionKey, String rawProcessInstanceId, MultipartBody body) throws StatusCodeError {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId);

        Task task = processInstanceService.userTask(process, instance, true);
        if (task == null)
            throw new ForbiddenError();

        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process);
        Submission submission = submissionHandler.handle(process, template, body);

        processInstanceService.attach(process, instance, task, template, submission);
        return Response.noContent().build();
    }

    @Override
    public Response attachments(String rawProcessDefinitionKey, String rawProcessInstanceId, AttachmentQueryParameters queryParameters) throws StatusCodeError {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId);

        if (!processInstanceService.userHasTask(process, instance, false))
            throw new ForbiddenError();

        SearchResults searchResults = processInstanceService.findAttachments(process, instance, queryParameters);
        return Response.ok(searchResults).build();
    }

    @Override
    public Response attachment(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawAttachmentId) throws StatusCodeError {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId);
        String attachmentId = sanitizer.sanitize(rawAttachmentId);

        if (!processInstanceService.userHasTask(process, instance, true))
            throw new ForbiddenError();

        StreamingAttachmentContent content = processInstanceService.getAttachmentContent(process, instance, attachmentId);

        if (content == null)
            throw new NotFoundError(Constants.ExceptionCodes.attachment_does_not_exist, attachmentId);

        String contentDisposition = new StringBuilder("attachment; filename=").append(content.getAttachment().getDescription()).toString();
        return Response.ok(content, content.getAttachment().getContentType()).header("Content-Disposition", contentDisposition).build();
    }

    @Override
    public Response cancel(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawReason) throws StatusCodeError {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId);
        String reason = sanitizer.sanitize(rawReason);

        if (!helper.hasRole(process, AuthorizationRole.OVERSEER))
            throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);

        processInstanceService.operate(OperationType.CANCELLATION, process, instance, null, reason);
        return Response.noContent().build();
    }

    @Override
	public Response create(HttpServletRequest request, String rawProcessDefinitionKey, Submission rawSubmission) throws StatusCodeError {
        Process process = processService.read(rawProcessDefinitionKey);

        RequestDetails requestDetails = requestDetails(request);
        FormRequest formRequest = requestHandler.create(requestDetails, process);
        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, formRequest.getScreen());
        Submission submission = submissionHandler.handle(process, template, rawSubmission, formRequest);
        ProcessInstance instance = processInstanceService.submit(process, null, null, template, submission);

        return Response.ok(new ProcessInstance.Builder(instance).build(getViewContext())).build();
	}
	
	@Override
	public Response create(HttpServletRequest request, String rawProcessDefinitionKey, MultivaluedMap<String, String> formData) throws StatusCodeError {
        Process process = processService.read(rawProcessDefinitionKey);

        RequestDetails requestDetails = requestDetails(request);
        FormRequest formRequest = requestHandler.create(requestDetails, process);
        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, formRequest.getScreen());
        Submission submission = submissionHandler.handle(process, template, formData);
        ProcessInstance instance = processInstanceService.submit(process, null, null, template, submission);

        return Response.ok(new ProcessInstance.Builder(instance).build(getViewContext())).build();
	}

	@Override
	public Response createMultipart(HttpServletRequest request, String rawProcessDefinitionKey, MultipartBody body) throws StatusCodeError {
        Process process = processService.read(rawProcessDefinitionKey);

        RequestDetails requestDetails = requestDetails(request);
        FormRequest formRequest = requestHandler.create(requestDetails, process);
        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, formRequest.getScreen());
        Submission submission = submissionHandler.handle(process, template, body, formRequest);
        ProcessInstance instance = processInstanceService.submit(process, null, null, template, submission);

        return Response.ok(new ProcessInstance.Builder(instance).build(getViewContext())).build();
	}

    @Override
    public Response detach(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawAttachmentId) throws StatusCodeError {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId);
        String attachmentId = sanitizer.sanitize(rawAttachmentId);

        Task task = processInstanceService.userTask(process, instance, true);
        if (!helper.isAuthenticatedSystem() && task == null)
            throw new ForbiddenError();

        processInstanceService.removeAttachment(process, instance, attachmentId);
        return Response.noContent().build();
    }

    @Override
    public Response history(String rawProcessDefinitionKey, String rawProcessInstanceId) throws StatusCodeError {
        History history = processInstanceService.getHistory(rawProcessDefinitionKey, rawProcessInstanceId);
        return Response.ok(history).build();
    }

    @Override
	public Response read(String rawProcessDefinitionKey, String rawProcessInstanceId) throws StatusCodeError {
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
		String processInstanceId = sanitizer.sanitize(rawProcessInstanceId);
		
		Process process = processInstanceService.getProcess(processDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, processInstanceId);

        ProcessInstance.Builder builder = new ProcessInstance.Builder(instance)
                .processDefinitionKey(processDefinitionKey)
                .processDefinitionLabel(process.getProcessDefinitionLabel());

//        try {
//            ProcessExecution execution = facade.findExecution(new ProcessInstanceSearchCriteria.Builder().executionId(instance.getEngineProcessInstanceId()).build());
//
//            if (execution != null) {
//                builder.startTime(execution.getStartTime());
//                builder.endTime(execution.getEndTime());
//                builder.initiatorId(execution.getInitiatorId());
//            }
//
//        } catch (ProcessEngineException e) {
//            LOG.error("Process engine unable to find execution ", e);
//        }

        return Response.ok(builder.build(getViewContext())).build();
	}

    @Override
    public Response suspend(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawReason) throws StatusCodeError {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId);
        String reason = sanitizer.sanitize(rawReason);

        if (!helper.hasRole(process, AuthorizationRole.OVERSEER) && !processInstanceService.userHasTask(process, instance, true))
            throw new ForbiddenError(Constants.ExceptionCodes.active_task_required);

        processInstanceService.operate(OperationType.SUSPENSION, process, instance, null, reason);
        return Response.noContent().build();
    }

    @Override
    public Response update(String rawProcessDefinitionKey, String rawProcessInstanceId, ProcessInstance instance) throws StatusCodeError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        String processInstanceId = sanitizer.sanitize(rawProcessInstanceId);

        processInstanceService.update(processDefinitionKey, processInstanceId, instance);

        ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
        ViewContext context = getViewContext();
        String location = context != null ? context.getApplicationUri(instance.getProcessDefinitionKey(), instance.getProcessInstanceId()) : null;
        if (location != null)
            responseBuilder.location(UriBuilder.fromPath(location).build());
        return responseBuilder.build();
    }

    @Override
	public Response delete(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawReason) throws StatusCodeError {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId);
        String reason = sanitizer.sanitize(rawReason);

        processInstanceService.operate(OperationType.CANCELLATION, process, instance, null, reason);

        ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
        ViewContext context = getViewContext();
        String location = context != null ? context.getApplicationUri(instance.getProcessDefinitionKey(), instance.getProcessInstanceId()) : null;
        if (location != null)
            responseBuilder.location(UriBuilder.fromPath(location).build());
        return responseBuilder.build();
	}

	@Override
	public SearchResults search(UriInfo uriInfo) throws StatusCodeError {
		MultivaluedMap<String, String> rawQueryParameters = uriInfo != null ? uriInfo.getQueryParameters() : null;
		return processInstanceService.search(rawQueryParameters, getViewContext());
	}

    @Override
    public Response remove(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawFieldName, String rawValueId) throws StatusCodeError {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId);
        String fieldName = sanitizer.sanitize(rawFieldName);
        String valueId = sanitizer.sanitize(rawValueId);

        if (!helper.hasRole(process, AuthorizationRole.OVERSEER) && !processInstanceService.userHasTask(process, instance, true))
            throw new ForbiddenError(Constants.ExceptionCodes.active_task_required);

        processInstanceService.removeValue(process, instance, fieldName, valueId);
        return Response.noContent().build();
    }

    @Override
    public Response value(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawFieldName, String rawValueId) throws StatusCodeError {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId);
        String fieldName = sanitizer.sanitize(rawFieldName);
        String valueId = sanitizer.sanitize(rawValueId);

        if (!helper.hasRole(process, AuthorizationRole.OVERSEER) && !processInstanceService.userHasTask(process, instance, true))
            throw new ForbiddenError(Constants.ExceptionCodes.active_task_required);

        return processInstanceService.readValue(process, instance, fieldName, valueId);
    }

    @Override
    public Response value(HttpServletRequest request, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawFieldName, MultipartBody body) throws StatusCodeError {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId);
        String fieldName = sanitizer.sanitize(rawFieldName);

        Task task = processInstanceService.userTask(process, instance, true);
        if (task == null && !helper.isAuthenticatedSystem())
            throw new ForbiddenError(Constants.ExceptionCodes.active_task_required);

        RequestDetails requestDetails = requestDetails(request);
        FormRequest formRequest = requestHandler.create(requestDetails, process, instance, task);

        Screen screen = formRequest.getScreen();

        if (screen == null)
            throw new ConflictError();

        Field field = FormFactory.getField(process, screen, fieldName);
        if (field == null)
            throw new NotFoundError();

        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(field);
        Submission submission = submissionHandler.handle(process, template, body);
        ProcessInstance stored = processInstanceService.save(process, instance, task, template, submission);

        Map<String, List<Value>> data = stored.getData();

        String location = null;
        if (data != null) {
            File file = ProcessInstanceUtility.firstFile(fieldName, data);
            String hostUri = environment.getProperty("host.uri");

            if (file != null) {
                location = hostUri +
                    new File.Builder(file, new PassthroughSanitizer())
                        .processDefinitionKey(process.getProcessDefinitionKey())
                        .processInstanceId(stored.getProcessInstanceId())
                        .fieldName(fieldName)
                        .build(processInstanceService.getInstanceViewContext())
                        .getLink();
            }
        }

        return Response.noContent().header(HttpHeaders.LOCATION, location).build();
    }

    @Override
    public Response values(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawFieldName) throws StatusCodeError {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId);
        String fieldName = sanitizer.sanitize(rawFieldName);

        if (!helper.hasRole(process, AuthorizationRole.OVERSEER) && !processInstanceService.userHasTask(process, instance, true))
            throw new ForbiddenError(Constants.ExceptionCodes.active_task_required);

        List<File> files = processInstanceService.searchValues(process, instance, fieldName);
        SearchResults searchResults = new SearchResults.Builder()
                .items(files)
                .build();

        return Response.ok(searchResults).build();
    }

    @Override
	public ViewContext getViewContext() {
        return processInstanceService.getInstanceViewContext();
	}

    @Override
    public String getVersion() {
        return processInstanceService.getVersion();
    }

    private RequestDetails requestDetails(HttpServletRequest request) {
        String certificateIssuerHeader = environment.getProperty(Constants.Settings.CERTIFICATE_ISSUER_HEADER);
        String certificateSubjectHeader = environment.getProperty(Constants.Settings.CERTIFICATE_SUBJECT_HEADER);

        return new RequestDetails.Builder(request, certificateIssuerHeader, certificateSubjectHeader).build();
    }

}
