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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.common.ViewContext;
import piecework.content.ContentResource;
import piecework.enumeration.OperationType;
import piecework.exception.ForbiddenError;
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.identity.IdentityHelper;
import piecework.model.*;
import piecework.persistence.AllowedTaskProvider;
import piecework.persistence.HistoryProvider;
import piecework.persistence.ProcessInstanceProvider;
import piecework.process.AttachmentQueryParameters;
import piecework.resource.ProcessInstanceApiResource;
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

    @Autowired
    IdentityHelper helper;

    @Override
    public Response activate(String rawProcessDefinitionKey, String rawProcessInstanceId, OperationDetails details) throws PieceworkException {
        String rawReason = details.getReason();
        doOperation(OperationType.ACTIVATION, rawProcessDefinitionKey, rawProcessInstanceId, rawReason, helper.getPrincipal());
        return Response.noContent().build();
    }

    @Override
    public Response attachment(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawAttachmentId) throws PieceworkException {
        String attachmentId = sanitizer.sanitize(rawAttachmentId);

        AllowedTaskProvider allowedTaskProvider = modelProviderFactory.allowedTaskProvider(rawProcessDefinitionKey, rawProcessInstanceId, helper.getPrincipal());
        Task allowedTask = allowedTaskProvider.allowedTask(true);

        if (allowedTask == null)
            throw new ForbiddenError();

        ContentResource content = allowedTaskProvider.attachment(attachmentId);

        if (content == null)
            throw new NotFoundError(Constants.ExceptionCodes.attachment_does_not_exist, attachmentId);

        String contentDisposition = new StringBuilder("attachment; filename=").append(content.getFilename()).toString();
        return Response.ok(content, content.contentType()).header("Content-Disposition", contentDisposition).build();
    }

    @Override
    public Response attachments(String rawProcessDefinitionKey, String rawProcessInstanceId, AttachmentQueryParameters queryParameters) throws PieceworkException {
        AllowedTaskProvider allowedTaskProvider = modelProviderFactory.allowedTaskProvider(rawProcessDefinitionKey, rawProcessInstanceId, helper.getPrincipal());
        Task allowedTask = allowedTaskProvider.allowedTask(true);

        if (allowedTask == null)
            throw new ForbiddenError();

        SearchResults searchResults = allowedTaskProvider.attachments(queryParameters, new ViewContext(settings, VERSION));
        return Response.ok(searchResults).build();
    }

    @Override
    public Response cancel(String rawProcessDefinitionKey, String rawProcessInstanceId, OperationDetails details) throws PieceworkException {
        doOperation(OperationType.CANCELLATION, rawProcessDefinitionKey, rawProcessInstanceId, details.getReason(), helper.getPrincipal());
        return Response.noContent().build();
    }

    @Override
    public Response create(MessageContext context, String rawProcessDefinitionKey, Submission rawSubmission) throws PieceworkException {
        return doCreate(context, rawProcessDefinitionKey, rawSubmission, Submission.class, helper.getPrincipal());
    }

    @Override
    public Response create(MessageContext context, String rawProcessDefinitionKey, MultivaluedMap<String, String> formData) throws PieceworkException {
        return doCreate(context, rawProcessDefinitionKey, formData, Map.class, helper.getPrincipal());
    }

    @Override
    public Response createMultipart(MessageContext context, String rawProcessDefinitionKey, MultipartBody body) throws PieceworkException {
        return doCreate(context, rawProcessDefinitionKey, body, MultipartBody.class, helper.getPrincipal());
    }

    @Override
    public Response delete(String rawProcessDefinitionKey, String rawProcessInstanceId) throws PieceworkException {
        Entity principal = helper.getPrincipal();
        String reason = null;
        ProcessInstanceProvider instanceProvider = modelProviderFactory.instanceProvider(rawProcessDefinitionKey, rawProcessInstanceId, principal);
        ProcessInstance instance = commandFactory.cancellation(instanceProvider, reason).execute();
        Response.ResponseBuilder responseBuilder = Response.status(Response.Status.NO_CONTENT);
        ViewContext context = new ViewContext(settings, VERSION);
        String location = context != null ? context.getApplicationUri(instance.getProcessDefinitionKey(), instance.getProcessInstanceId()) : null;
        if (location != null)
            responseBuilder.location(UriBuilder.fromPath(location).build());
        return responseBuilder.build();
    }

    @Override
    public Response detach(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawAttachmentId) throws PieceworkException {
        doDetach(context, rawProcessDefinitionKey, rawProcessInstanceId, rawAttachmentId, helper.getPrincipal());
        return Response.noContent().build();
    }

    @Override
    public Response history(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId) throws PieceworkException {
        HistoryProvider historyProvider = modelProviderFactory.historyProvider(rawProcessDefinitionKey, rawProcessInstanceId, helper.getPrincipal());
        return FormUtility.okResponse(settings, historyProvider, historyProvider.history(new ViewContext(settings, VERSION)), null, false);
    }

    @Override
    public Response read(String rawProcessDefinitionKey, String rawProcessInstanceId) throws PieceworkException {
        ProcessInstanceProvider instanceProvider = modelProviderFactory.instanceProvider(rawProcessDefinitionKey, rawProcessInstanceId, helper.getPrincipal());
        ProcessInstance instance = instanceProvider.instance(new ViewContext(settings, VERSION));
        return Response.ok(instance).build();
    }

    @Override
    public Response remove(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawFieldName, String rawValueId) throws PieceworkException {
        doRemove(context, rawProcessDefinitionKey, rawProcessInstanceId, rawFieldName, rawValueId, helper.getPrincipal());
        return Response.noContent().build();
    }

    @Override
    public Response restart(String rawProcessDefinitionKey, String rawProcessInstanceId, OperationDetails details) throws PieceworkException {
        String reason = sanitizer.sanitize(details.getReason());
        doOperation(OperationType.RESTART, rawProcessDefinitionKey, rawProcessInstanceId, reason, helper.getPrincipal());
        return Response.noContent().build();
    }

    @Override
    public Response search(MessageContext context) throws PieceworkException {
        return doSearch(context, helper.getPrincipal());
    }

    @Override
    public Response suspend(String rawProcessDefinitionKey, String rawProcessInstanceId, OperationDetails details) throws PieceworkException {
        doOperation(OperationType.SUSPENSION, rawProcessDefinitionKey, rawProcessInstanceId, details.getReason(), helper.getPrincipal());
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
