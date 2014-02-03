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

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.service.AttachmentService;
import piecework.Constants;
import piecework.Versions;
import piecework.authorization.AuthorizationRole;
import piecework.common.ViewContext;
import piecework.exception.ForbiddenError;
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.exception.StatusCodeError;
import piecework.identity.IdentityHelper;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.concrete.ExportInstanceProvider;
import piecework.process.AttachmentQueryParameters;
import piecework.service.HistoryFactory;
import piecework.resource.ProcessInstanceResource;
import piecework.security.AccessTracker;
import piecework.security.Sanitizer;
import piecework.settings.SecuritySettings;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.service.*;
import piecework.ui.Streamable;
import piecework.ui.streaming.ExportStreamingOutput;
import piecework.ui.streaming.StreamingAttachmentContent;
import piecework.common.ManyMap;
import piecework.util.FormUtility;
import piecework.util.ProcessInstanceUtility;

import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
@Service
public class ProcessInstanceResourceVersion1 implements ProcessInstanceResource {

    private static final Logger LOG = Logger.getLogger(ProcessInstanceResourceVersion1.class);

    @Autowired
    AccessTracker accessTracker;

    @Autowired
    AttachmentService attachmentService;

    @Autowired
    DeploymentService deploymentService;

    @Autowired
    IdentityHelper helper;

    @Autowired
    HistoryFactory historyFactory;

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
    TaskService taskService;

    @Autowired
    ValuesService valuesService;

    @Autowired
    Versions versions;

    @Override
    public Response activate(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawReason) throws PieceworkException {
        processInstanceService.activate(helper.getPrincipal(), rawProcessDefinitionKey, rawProcessInstanceId, rawReason);
        return Response.noContent().build();
    }

    @Override
    public Response activate(String rawProcessDefinitionKey, String rawProcessInstanceId, OperationDetails details) throws PieceworkException {
        String reason = sanitizer.sanitize(details.getReason());
        return activate(rawProcessDefinitionKey, rawProcessInstanceId, reason);
    }

    @Override
    public Response attach(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, MultivaluedMap<String, String> formData) throws PieceworkException {
        return doAttach(context, rawProcessDefinitionKey, rawProcessInstanceId, formData, Map.class);
    }

    @Override
    public Response attach(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, MultipartBody body) throws PieceworkException {
        return doAttach(context, rawProcessDefinitionKey, rawProcessInstanceId, body, MultipartBody.class);
    }

    @Override
    public Response attachments(String rawProcessDefinitionKey, String rawProcessInstanceId, AttachmentQueryParameters queryParameters) throws PieceworkException {
        Entity principal = helper.getPrincipal();
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, true);
        ProcessDeployment deployment = deploymentService.read(process, instance);

        if (!taskService.hasAllowedTask(process, instance, principal, false))
            throw new ForbiddenError();

        SearchResults searchResults = attachmentService.search(instance, queryParameters);
        return FormUtility.allowCrossOriginResponse(deployment, searchResults);
    }

    @Override
    public Response attachment(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawAttachmentId) throws PieceworkException {
        Entity principal = helper.getPrincipal();
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, true);
        ProcessDeployment deployment = deploymentService.read(process, instance);

        String attachmentId = sanitizer.sanitize(rawAttachmentId);

        if (!taskService.hasAllowedTask(process, instance, principal, true))
            throw new ForbiddenError();

        StreamingAttachmentContent content = attachmentService.content(process, instance, attachmentId);

        if (content == null)
            throw new NotFoundError(Constants.ExceptionCodes.attachment_does_not_exist, attachmentId);

        String contentDisposition = new StringBuilder("attachment; filename=").append(content.getAttachment().getDescription()).toString();

        return FormUtility.allowCrossOriginResponse(deployment, content, content.getAttachment().getContentType(), new BasicHeader("Content-Disposition", contentDisposition));
    }

    @Override
    public Response cancel(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawReason) throws PieceworkException {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, true);
        ProcessDeployment deployment = deploymentService.read(process, instance);

        processInstanceService.cancel(helper.getPrincipal(), rawProcessDefinitionKey, rawProcessInstanceId, rawReason);
        return FormUtility.allowCrossOriginNoContentResponse(deployment);
    }

    @Override
    public Response cancel(String rawProcessDefinitionKey, String rawProcessInstanceId, OperationDetails details) throws PieceworkException {
        return cancel(rawProcessDefinitionKey, rawProcessInstanceId, details.getReason());
    }

    @Override
	public Response create(MessageContext context, String rawProcessDefinitionKey, Submission rawSubmission) throws PieceworkException {
        return doCreate(context, rawProcessDefinitionKey, rawSubmission, Submission.class);
	}
	
	@Override
	public Response create(MessageContext context, String rawProcessDefinitionKey, MultivaluedMap<String, String> formData) throws PieceworkException {
        return doCreate(context, rawProcessDefinitionKey, formData, Map.class);
    }

	@Override
	public Response createMultipart(MessageContext context, String rawProcessDefinitionKey, MultipartBody body) throws PieceworkException {
        return doCreate(context, rawProcessDefinitionKey, body, MultipartBody.class);
    }

    @Override
    public Response detachment(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawAttachmentId) throws PieceworkException {
        return detach(rawProcessDefinitionKey, rawProcessInstanceId, rawAttachmentId);
    }

    @Override
    public Response detach(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawAttachmentId) throws PieceworkException {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, true);
        ProcessDeployment deployment = deploymentService.read(process, instance);

        Entity principal = helper.getPrincipal();
        processInstanceService.deleteAttachment(principal, rawProcessDefinitionKey, rawProcessInstanceId, rawAttachmentId);
        return FormUtility.allowCrossOriginNoContentResponse(deployment);
    }

    @Override
    public Response diagram(String rawProcessDefinitionKey, String rawProcessInstanceId) throws StatusCodeError {
        Streamable diagram = processInstanceService.getDiagram(rawProcessDefinitionKey, rawProcessInstanceId);
        StreamingAttachmentContent streamingAttachmentContent = new StreamingAttachmentContent(diagram);
        ResponseBuilder responseBuilder = Response.ok(streamingAttachmentContent);
        return responseBuilder.build();
    }

    @Override
    public Response history(String rawProcessDefinitionKey, String rawProcessInstanceId) throws PieceworkException {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, true);
        ProcessDeployment deployment = deploymentService.read(process, instance);

        History history = historyFactory.history(rawProcessDefinitionKey, rawProcessInstanceId);
        return FormUtility.allowCrossOriginResponse(deployment, history);
    }

    @Override
	public Response read(String rawProcessDefinitionKey, String rawProcessInstanceId) throws PieceworkException {
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
    public Response suspendOptions(String rawProcessDefinitionKey, String rawProcessInstanceId) throws PieceworkException {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessDeployment deployment = process.getDeployment();

        LOG.debug("Options for " + process.getProcessDefinitionKey());

        return FormUtility.allowCrossOriginResponse(deployment, null);
    }

    @Override
    public Response suspend(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawReason) throws PieceworkException {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, true);
        ProcessDeployment deployment = deploymentService.read(process, instance);

        processInstanceService.suspend(helper.getPrincipal(), rawProcessDefinitionKey, rawProcessInstanceId, rawReason);
        return FormUtility.allowCrossOriginNoContentResponse(deployment);
    }

    @Override
    public Response suspend(String rawProcessDefinitionKey, String rawProcessInstanceId, OperationDetails details) throws PieceworkException {
        return suspend(rawProcessDefinitionKey, rawProcessInstanceId, details.getReason());
    }

    @Override
    public Response update(String rawProcessDefinitionKey, String rawProcessInstanceId, ProcessInstance rawInstance) throws PieceworkException {
        Entity principal = helper.getPrincipal();

        ProcessInstance instance = processInstanceService.update(principal, rawProcessDefinitionKey, rawProcessInstanceId, rawInstance);

        ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
        ViewContext context = versions.getVersion1();
        String location = context != null ? context.getApplicationUri(instance.getProcessDefinitionKey(), instance.getProcessInstanceId()) : null;
        if (location != null)
            responseBuilder.location(UriBuilder.fromPath(location).build());
        return responseBuilder.build();
    }

    @Override
	public Response delete(String rawProcessDefinitionKey, String rawProcessInstanceId) throws PieceworkException {
        Entity principal = helper.getPrincipal();

        ProcessInstance instance = processInstanceService.cancel(principal, rawProcessDefinitionKey, rawProcessInstanceId, null);
        ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
        ViewContext context = versions.getVersion1();
        String location = context != null ? context.getApplicationUri(instance.getProcessDefinitionKey(), instance.getProcessInstanceId()) : null;
        if (location != null)
            responseBuilder.location(UriBuilder.fromPath(location).build());
        return responseBuilder.build();
	}

	@Override
	public Response search(MessageContext context) throws PieceworkException {
        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, false, false);

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
    public Response restart(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawReason) throws PieceworkException {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, true);
        ProcessDeployment deployment = deploymentService.read(process, instance);

        processInstanceService.restart(helper.getPrincipal(), rawProcessDefinitionKey, rawProcessInstanceId, rawReason);
        return FormUtility.allowCrossOriginNoContentResponse(deployment);
    }

    @Override
    public Response restart(String rawProcessDefinitionKey, String rawProcessInstanceId, OperationDetails details) throws PieceworkException {
        String reason = sanitizer.sanitize(details.getReason());
        return restart(rawProcessDefinitionKey, rawProcessInstanceId, reason);
    }

    @Override
    public Response remove(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawFieldName, String rawValueId) throws PieceworkException {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, true);
        ProcessDeployment deployment = deploymentService.read(process, instance);

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, true, false);
        valuesService.delete(rawProcessDefinitionKey, rawProcessInstanceId, rawFieldName, rawValueId, requestDetails, helper.getPrincipal());
        return FormUtility.allowCrossOriginNoContentResponse(deployment);
    }

    @Override
    public Response readValue(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawFieldName, String rawValueId, Boolean inline) throws PieceworkException {
        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, false, false);

        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, false);
        String fieldName = sanitizer.sanitize(rawFieldName);
        String valueId = sanitizer.sanitize(rawValueId);

        Entity principal = helper.getPrincipal();

        if (!principal.hasRole(process, AuthorizationRole.OVERSEER) && !taskService.hasAllowedTask(process, instance, principal, true))
            throw new ForbiddenError(Constants.ExceptionCodes.active_task_required);

        return valuesService.read(process, instance, fieldName, valueId, inline);
    }

    @Override
    public Response value(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawFieldName, String value) throws PieceworkException {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, true);
        ProcessDeployment deployment = deploymentService.read(process, instance);

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, true, false);

        String fieldName = sanitizer.sanitize(rawFieldName);

        ManyMap<String, Value> data = new ManyMap<String, Value>();
        data.putOne(fieldName, new Value(sanitizer.sanitize(value)));
        processInstanceService.updateField(requestDetails, rawProcessDefinitionKey, rawProcessInstanceId, fieldName, data, Map.class, helper.getPrincipal());

        return FormUtility.allowCrossOriginNoContentResponse(deployment);
    }

    @Override
    public Response value(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawFieldName, MultipartBody body) throws PieceworkException {
        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, true, false);
        String fieldName = sanitizer.sanitize(rawFieldName);

        ProcessInstance stored = processInstanceService.updateField(requestDetails, rawProcessDefinitionKey, rawProcessInstanceId, fieldName, body, MultipartBody.class, helper.getPrincipal());
        return valueLocation(stored, fieldName);
    }

    private Response valueLocation(ProcessInstance stored, String fieldName) {
        Map<String, List<Value>> data = stored.getData();

        ViewContext version1 = versions.getVersion1();
        String location = null;
        File file = null;
        if (data != null) {
            file = ProcessInstanceUtility.firstFile(fieldName, data);

            if (file != null) {
                file = new File.Builder(file, new PassthroughSanitizer())
                                .processDefinitionKey(stored.getProcessDefinitionKey())
                                .processInstanceId(stored.getProcessInstanceId())
                                .fieldName(fieldName)
                                .build(version1);
                location = file.getLink();
            }
        }

        ResponseBuilder builder = file != null ? Response.ok(file) : Response.noContent();

        if (location != null)
            builder.header(HttpHeaders.LOCATION, location);

        return builder.build();
    }

    @Override
    public Response values(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawFieldName) throws PieceworkException {
        Entity principal = helper.getPrincipal();
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, false);
        ProcessDeployment deployment = deploymentService.read(process, instance);
        String fieldName = sanitizer.sanitize(rawFieldName);

        if (!principal.hasRole(process, AuthorizationRole.OVERSEER) && !taskService.hasAllowedTask(process, instance, principal, true))
            throw new ForbiddenError(Constants.ExceptionCodes.active_task_required);

        List<Value> files = valuesService.searchValues(process, instance, fieldName);
        SearchResults searchResults = new SearchResults.Builder()
                .items(files)
                .build();

        return FormUtility.allowCrossOriginResponse(deployment, searchResults);
    }

    @Override
    public String getVersion() {
        return versions.getVersion1().getVersion();
    }

    private <T> Response doAttach(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, T data, Class<T> type) throws PieceworkException {

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, true, false);

        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.attach(helper.getPrincipal(), requestDetails, rawProcessDefinitionKey, rawProcessInstanceId, data, type);
        ProcessDeployment deployment = deploymentService.read(process, instance);

        SearchResults searchResults = attachmentService.search(instance, new AttachmentQueryParameters());

        return FormUtility.allowCrossOriginResponse(deployment, searchResults);
    }

    private <T> Response doCreate(MessageContext context, String rawProcessDefinitionKey, T data, Class<T> type) throws PieceworkException {

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, true, false);

        ProcessInstance instance = processInstanceService.create(helper.getPrincipal(), requestDetails, rawProcessDefinitionKey, data, type);

        return Response.ok(new ProcessInstance.Builder(instance).build(versions.getVersion1())).build();
    }

}
