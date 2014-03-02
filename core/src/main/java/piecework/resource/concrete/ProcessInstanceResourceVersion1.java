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
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import piecework.command.FieldValidationCommand;
import piecework.content.ContentResource;
import piecework.enumeration.ActionType;
import piecework.enumeration.OperationType;
import piecework.identity.IdentityHelper;
import piecework.persistence.AllowedTaskProvider;
import piecework.persistence.HistoryProvider;
import piecework.persistence.ProcessInstanceProvider;
import piecework.resource.ProcessInstanceApplicationResource;
import piecework.Constants;
import piecework.common.ViewContext;
import piecework.exception.ForbiddenError;
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.process.AttachmentQueryParameters;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.service.*;
import piecework.settings.UserInterfaceSettings;
import piecework.ui.streaming.StreamingResource;
import piecework.common.ManyMap;
import piecework.util.FormUtility;
import piecework.util.ProcessInstanceUtility;
import piecework.validation.Validation;

import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
@Service
public class ProcessInstanceResourceVersion1 extends AbstractInstanceResource implements ProcessInstanceApplicationResource {

    private static final Logger LOG = Logger.getLogger(ProcessInstanceResourceVersion1.class);

    @Autowired
    IdentityHelper helper;

    @Autowired
    RequestService requestService;

    @Autowired
    UserInterfaceSettings settings;

    @Override
    public Response activate(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawReason) throws PieceworkException {
        ProcessInstanceProvider instanceProvider = doOperation(OperationType.ACTIVATION, rawProcessDefinitionKey, rawProcessInstanceId, rawReason, helper.getPrincipal());
        return FormUtility.noContentResponse(settings, instanceProvider, false);
    }

    @Override
    public Response attach(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, MultivaluedMap<String, String> formData) throws PieceworkException {
        return doAttach(context, rawProcessDefinitionKey, rawProcessInstanceId, formData, Map.class, helper.getPrincipal());
    }

    @Override
    public Response attach(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, MultipartBody body) throws PieceworkException {
        return doAttach(context, rawProcessDefinitionKey, rawProcessInstanceId, body, MultipartBody.class, helper.getPrincipal());
    }

    @Override
    public Response attachOptions(String rawProcessDefinitionKey, String rawProcessInstanceId) throws PieceworkException {
        ProcessInstanceProvider instanceProvider = modelProviderFactory.instanceProvider(rawProcessDefinitionKey, rawProcessInstanceId, helper.getPrincipal());
        return FormUtility.optionsResponse(settings, instanceProvider, false, "GET", "POST");
    }

    @Override
     public Response attachments(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, AttachmentQueryParameters queryParameters) throws PieceworkException {
        AllowedTaskProvider allowedTaskProvider = modelProviderFactory.allowedTaskProvider(rawProcessDefinitionKey, rawProcessInstanceId, helper.getPrincipal());
        Task allowedTask = allowedTaskProvider.allowedTask(true);

        if (allowedTask == null)
            throw new ForbiddenError();

        SearchResults searchResults = allowedTaskProvider.attachments(queryParameters, new ViewContext(settings, VERSION));
        return FormUtility.okResponse(settings, allowedTaskProvider, searchResults, null, false);
    }

    @Override
    public Response attachment(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawAttachmentId) throws PieceworkException {
        String attachmentId = sanitizer.sanitize(rawAttachmentId);

        AllowedTaskProvider allowedTaskProvider = modelProviderFactory.allowedTaskProvider(rawProcessDefinitionKey, rawProcessInstanceId, helper.getPrincipal());
        Task allowedTask = allowedTaskProvider.allowedTask(true);

        if (allowedTask == null)
            throw new ForbiddenError();

        ContentResource attachment = allowedTaskProvider.attachment(attachmentId);

        if (attachment == null)
            throw new NotFoundError(Constants.ExceptionCodes.attachment_does_not_exist, attachmentId);

        String contentDisposition = new StringBuilder("attachment; filename=").append(attachment.getFilename()).toString();

        return FormUtility.okResponse(settings, allowedTaskProvider, attachment, attachment.contentType(), Collections.<Header>singletonList(new BasicHeader("Content-Disposition", contentDisposition)), false);
    }

    @Override
    public Response cancel(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawReason) throws PieceworkException {
        ProcessInstanceProvider instanceProvider = doOperation(OperationType.CANCELLATION, rawProcessDefinitionKey, rawProcessInstanceId, rawReason, helper.getPrincipal());
        return FormUtility.noContentResponse(settings, instanceProvider, false);
    }

    @Override
    public Response detachment(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawAttachmentId) throws PieceworkException {
        return detach(context, rawProcessDefinitionKey, rawProcessInstanceId, rawAttachmentId);
    }

    @Override
    public Response detach(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawAttachmentId) throws PieceworkException {
        AllowedTaskProvider taskProvider = doDetach(context, rawProcessDefinitionKey, rawProcessInstanceId, rawAttachmentId, helper.getPrincipal());
        return FormUtility.noContentResponse(settings, taskProvider, false);
    }

    @Override
    public Response diagram(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId) throws PieceworkException {
        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, false, false);

        ProcessInstanceProvider instanceProvider = modelProviderFactory.instanceProvider(rawProcessDefinitionKey, rawProcessInstanceId, helper.getPrincipal());
        ContentResource diagram = instanceProvider.diagram();
        ResponseBuilder responseBuilder = Response.ok(diagram);
        return responseBuilder.build();
    }

    @Override
    public Response history(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId) throws PieceworkException {
        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, false, false);

        HistoryProvider historyProvider = modelProviderFactory.historyProvider(rawProcessDefinitionKey, rawProcessInstanceId, helper.getPrincipal());
        return FormUtility.okResponse(settings, historyProvider, historyProvider.history(new ViewContext(settings, VERSION)), null, false);
    }

    @Override
    public Response restart(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawReason) throws PieceworkException {
        ProcessInstanceProvider instanceProvider = doOperation(OperationType.RESTART, rawProcessDefinitionKey, rawProcessInstanceId, rawReason, helper.getPrincipal());
        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, true, false);
        return FormUtility.noContentResponse(settings, instanceProvider, false);
    }

    @Override
    public Response removal(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawFieldName, String rawValueId) throws PieceworkException {
        AllowedTaskProvider allowedTaskProvider = doRemove(context, rawProcessDefinitionKey, rawProcessInstanceId, rawFieldName, rawValueId, helper.getPrincipal());
        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, true, false);
        return FormUtility.noContentResponse(settings, allowedTaskProvider, false);
    }

    @Override
    public Response remove(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawFieldName, String rawValueId) throws PieceworkException {
        AllowedTaskProvider allowedTaskProvider = doRemove(context, rawProcessDefinitionKey, rawProcessInstanceId, rawFieldName, rawValueId, helper.getPrincipal());
        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, true, false);
        return FormUtility.noContentResponse(settings, allowedTaskProvider, false);
    }

    @Override
    public Response removeOptions(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawFieldName, String rawValueId) throws PieceworkException {
        ProcessInstanceProvider instanceProvider = modelProviderFactory.instanceProvider(rawProcessDefinitionKey, rawProcessInstanceId, helper.getPrincipal());
        return FormUtility.optionsResponse(settings, instanceProvider, false, "DELETE");
    }

    @Override
    public Response readValue(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawFieldName, String rawValueId, Boolean inline) throws PieceworkException {
        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, false, false);

        AllowedTaskProvider allowedTaskProvider = modelProviderFactory.allowedTaskProvider(rawProcessDefinitionKey, rawProcessInstanceId, helper.getPrincipal());
        ContentResource contentResource = allowedTaskProvider.value(rawFieldName, rawValueId);

        boolean isInline = inline != null && inline.booleanValue();
        if (isInline)
            return FormUtility.okResponse(settings, allowedTaskProvider, contentResource, contentResource.contentType(), false);

        String contentDisposition = new StringBuilder("attachment; filename=").append(contentResource.getName()).toString();

        List<Header> headers = Collections.<Header>singletonList(new BasicHeader("Content-Disposition", contentDisposition));
        return FormUtility.okResponse(settings, allowedTaskProvider, contentResource, contentResource.contentType(), headers, false);
    }

    @Override
    public Response search(MessageContext context) throws PieceworkException {
        return doSearch(context, helper.getPrincipal());
    }

    @Override
    public Response suspendOptions(String rawProcessDefinitionKey, String rawProcessInstanceId) throws PieceworkException {
        ProcessInstanceProvider instanceProvider = modelProviderFactory.instanceProvider(rawProcessDefinitionKey, rawProcessInstanceId, helper.getPrincipal());
        return FormUtility.optionsResponse(settings, instanceProvider, false, "POST");
    }

    @Override
    public Response suspend(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawReason) throws PieceworkException {
        ProcessInstanceProvider instanceProvider = doOperation(OperationType.SUSPENSION, rawProcessDefinitionKey, rawProcessInstanceId, rawReason, helper.getPrincipal());
        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, true, false);
        return FormUtility.noContentResponse(settings, instanceProvider, false);
    }

    @Override
    public Response value(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawFieldName, String value) throws PieceworkException {
        AllowedTaskProvider allowedTaskProvider = modelProviderFactory.allowedTaskProvider(rawProcessDefinitionKey, rawProcessInstanceId, helper.getPrincipal());
        Task allowedTask = allowedTaskProvider.allowedTask(true);
        if (allowedTask == null)
            throw new ForbiddenError(Constants.ExceptionCodes.active_task_required);

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, true, false);

        String fieldName = sanitizer.sanitize(rawFieldName);

        ManyMap<String, Value> data = new ManyMap<String, Value>();
        data.putOne(fieldName, new Value(sanitizer.sanitize(value)));

        FormRequest request = requestService.create(requestDetails, allowedTaskProvider, ActionType.UPDATE);
        FieldValidationCommand<AllowedTaskProvider> validationCommand = commandFactory.fieldValidation(allowedTaskProvider, request, data, Map.class, fieldName, VERSION);
        Validation validation = validationCommand.execute();
        commandFactory.updateValue(allowedTaskProvider, validation).execute();

        return FormUtility.noContentResponse(settings, allowedTaskProvider, false);
    }

    @Override
    public Response value(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawFieldName, MultipartBody body) throws PieceworkException {
        AllowedTaskProvider allowedTaskProvider = modelProviderFactory.allowedTaskProvider(rawProcessDefinitionKey, rawProcessInstanceId, helper.getPrincipal());
        Task allowedTask = allowedTaskProvider.allowedTask(true);
        if (allowedTask == null)
            throw new ForbiddenError(Constants.ExceptionCodes.active_task_required);

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, true, false);
        String fieldName = sanitizer.sanitize(rawFieldName);

        FormRequest request = requestService.create(requestDetails, allowedTaskProvider, ActionType.UPDATE);
        FieldValidationCommand<AllowedTaskProvider> validationCommand = commandFactory.fieldValidation(allowedTaskProvider, request, body, MultipartBody.class, fieldName, VERSION);
        Validation validation = validationCommand.execute();
        ProcessInstance stored = commandFactory.updateValue(allowedTaskProvider, validation).execute();

        Map<String, List<Value>> data = stored.getData();

        String location = null;
        File file = null;
        if (data != null) {
            file = ProcessInstanceUtility.firstFile(fieldName, data);

            if (file != null) {
                file = new File.Builder(file, new PassthroughSanitizer())
                        .processDefinitionKey(stored.getProcessDefinitionKey())
                        .processInstanceId(stored.getProcessInstanceId())
                        .fieldName(fieldName)
                        .build(new ViewContext(settings, VERSION));
                location = file.getLink();
            }
        }

        List<Header> headers = null;

        if (location != null)
            headers = Collections.<Header>singletonList(new BasicHeader(HttpHeaders.LOCATION, location));

        if (file != null)
            return FormUtility.okResponse(settings, allowedTaskProvider, file, null, headers, false);

        return FormUtility.noContentResponse(settings, allowedTaskProvider, headers, false);
    }

    @Override
    public Response values(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawFieldName) throws PieceworkException {
        AllowedTaskProvider allowedTaskProvider = modelProviderFactory.allowedTaskProvider(rawProcessDefinitionKey, rawProcessInstanceId, helper.getPrincipal());
        Task allowedTask = allowedTaskProvider.allowedTask(true);
        if (allowedTask == null)
            throw new ForbiddenError(Constants.ExceptionCodes.active_task_required);

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, false, false);
        String fieldName = sanitizer.sanitize(rawFieldName);

        SearchResults searchResults = allowedTaskProvider.values(fieldName, new ViewContext(settings, VERSION));
        return FormUtility.okResponse(settings, allowedTaskProvider, searchResults, null, false);
    }

}
