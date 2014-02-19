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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.common.ViewContext;
import piecework.enumeration.OperationType;
import piecework.exception.ForbiddenError;
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.process.AttachmentQueryParameters;
import piecework.resource.ProcessInstanceApiResource;
import piecework.settings.UserInterfaceSettings;
import piecework.ui.streaming.StreamingAttachmentContent;
import piecework.util.FormUtility;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.Map;

/**
 * @author James Renfro
 */
@Service
public class ProcessInstanceApiResourceVersion1 extends AbstractInstanceResource implements ProcessInstanceApiResource {

    @Override
    public Response activate(String rawProcessDefinitionKey, String rawProcessInstanceId, OperationDetails details) throws PieceworkException {
        String rawReason = details.getReason();
        doOperation(OperationType.ACTIVATION, rawProcessDefinitionKey, rawProcessInstanceId, rawReason);
        return Response.noContent().build();
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
    public Response attachments(String rawProcessDefinitionKey, String rawProcessInstanceId, AttachmentQueryParameters queryParameters) throws PieceworkException {
        Entity principal = helper.getPrincipal();
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, false);
        ProcessDeployment deployment = deploymentService.read(process, instance);

        if (!taskService.hasAllowedTask(process, instance, principal, false))
            throw new ForbiddenError();

        SearchResults searchResults = attachmentService.search(instance, queryParameters);
        return Response.ok(searchResults).build();
    }

    @Override
    public Response cancel(String rawProcessDefinitionKey, String rawProcessInstanceId, OperationDetails details) throws PieceworkException {
        doOperation(OperationType.CANCELLATION, rawProcessDefinitionKey, rawProcessInstanceId, details.getReason());
        return Response.noContent().build();
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
    public Response delete(String rawProcessDefinitionKey, String rawProcessInstanceId) throws PieceworkException {
        Entity principal = helper.getPrincipal();

        ProcessInstance instance = processInstanceService.cancel(principal, rawProcessDefinitionKey, rawProcessInstanceId, null);
        Response.ResponseBuilder responseBuilder = Response.status(Response.Status.NO_CONTENT);
        ViewContext context = new ViewContext(settings, VERSION);
        String location = context != null ? context.getApplicationUri(instance.getProcessDefinitionKey(), instance.getProcessInstanceId()) : null;
        if (location != null)
            responseBuilder.location(UriBuilder.fromPath(location).build());
        return responseBuilder.build();
    }

    @Override
    public Response detach(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawAttachmentId) throws PieceworkException {
        doDetach(rawProcessDefinitionKey, rawProcessInstanceId, rawAttachmentId);
        return Response.noContent().build();
    }

    @Override
    public Response read(String rawProcessDefinitionKey, String rawProcessInstanceId) throws PieceworkException {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        String processInstanceId = sanitizer.sanitize(rawProcessInstanceId);

        piecework.model.Process process = processService.read(processDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, processInstanceId, false);

        ProcessInstance.Builder builder = new ProcessInstance.Builder(instance)
                .processDefinitionKey(processDefinitionKey)
                .processDefinitionLabel(process.getProcessDefinitionLabel());

        return Response.ok(builder.build(new ViewContext(settings, VERSION))).build();
    }

    @Override
    public Response remove(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawFieldName, String rawValueId) throws PieceworkException {
        doRemove(context, rawProcessDefinitionKey, rawProcessInstanceId, rawFieldName, rawValueId);
        return Response.noContent().build();
    }

    @Override
    public Response restart(String rawProcessDefinitionKey, String rawProcessInstanceId, OperationDetails details) throws PieceworkException {
        String reason = sanitizer.sanitize(details.getReason());
        doOperation(OperationType.RESTART, rawProcessDefinitionKey, rawProcessInstanceId, reason);
        return Response.noContent().build();
    }

    @Override
    public Response search(MessageContext context) throws PieceworkException {
        return doSearch(context);
    }

    @Override
    public Response suspend(String rawProcessDefinitionKey, String rawProcessInstanceId, OperationDetails details) throws PieceworkException {
        doOperation(OperationType.SUSPENSION, rawProcessDefinitionKey, rawProcessInstanceId, details.getReason());
        return Response.noContent().build();
    }

    @Override
    public Response update(String rawProcessDefinitionKey, String rawProcessInstanceId, ProcessInstance rawInstance) throws PieceworkException {
        Entity principal = helper.getPrincipal();

        ProcessInstance instance = processInstanceService.update(principal, rawProcessDefinitionKey, rawProcessInstanceId, rawInstance);

        Response.ResponseBuilder responseBuilder = Response.status(Response.Status.NO_CONTENT);
        ViewContext context = new ViewContext(settings, VERSION);
        String location = context != null ? context.getApplicationUri(instance.getProcessDefinitionKey(), instance.getProcessInstanceId()) : null;
        if (location != null)
            responseBuilder.location(UriBuilder.fromPath(location).build());
        return responseBuilder.build();
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

}
