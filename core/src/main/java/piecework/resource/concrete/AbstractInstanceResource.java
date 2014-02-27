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
import org.springframework.beans.factory.annotation.Autowired;
import piecework.Constants;
import piecework.command.CommandFactory;
import piecework.command.ValidationCommand;
import piecework.common.ViewContext;
import piecework.enumeration.ActionType;
import piecework.enumeration.OperationType;
import piecework.exception.ForbiddenError;
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.export.IteratingDataProvider;
import piecework.model.*;
import piecework.persistence.*;
import piecework.process.AttachmentQueryParameters;
import piecework.security.AccessTracker;
import piecework.security.Sanitizer;
import piecework.service.*;
import piecework.settings.SecuritySettings;
import piecework.settings.UserInterfaceSettings;
import piecework.util.FormUtility;
import piecework.validation.Validation;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * @author James Renfro
 */
public abstract class AbstractInstanceResource {

    protected static final String VERSION = "v1";

    @Autowired
    AccessTracker accessTracker;

    @Autowired
    CommandFactory commandFactory;

    @Autowired
    ModelProviderFactory modelProviderFactory;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    RequestService requestService;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    SecuritySettings securitySettings;

    @Autowired
    UserInterfaceSettings settings;


    protected <T> Response doAttach(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, T data, Class<T> type, Entity principal) throws PieceworkException {

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, true, false);

        AllowedTaskProvider taskProvider = modelProviderFactory.allowedTaskProvider(rawProcessDefinitionKey, rawProcessInstanceId, principal);
        Task task = taskProvider.allowedTask(true);
        if (task == null)
            throw new ForbiddenError(Constants.ExceptionCodes.active_task_required);

        FormRequest request = requestService.create(requestDetails, taskProvider, ActionType.ATTACH);
        ValidationCommand<AllowedTaskProvider> validationCommand = commandFactory.validation(taskProvider, request, data, type, null, VERSION);
        Validation validation = validationCommand.execute();

        commandFactory.attachment(taskProvider, validation).execute();

        ProcessInstance instance = taskProvider.instance();
        SearchResults searchResults = taskProvider.attachments(new AttachmentQueryParameters(), new ViewContext(settings, VERSION));

        return FormUtility.okResponse(settings, taskProvider, searchResults, null, false);
    }

    protected <T> Response doCreate(MessageContext context, String rawProcessDefinitionKey, T data, Class<T> type, Entity principal) throws PieceworkException {

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, true, false);

        ProcessDeploymentProvider deploymentProvider = modelProviderFactory.deploymentProvider(rawProcessDefinitionKey, principal);
        FormRequest request = requestService.create(requestDetails, deploymentProvider);
        ValidationCommand<ProcessDeploymentProvider> validationCommand = commandFactory.validation(deploymentProvider, request, data, type, null, VERSION);
        Validation validation = validationCommand.execute();
        ProcessInstance instance = commandFactory.createInstance(deploymentProvider, validation).execute();

        ProcessInstance decorated = new ProcessInstance.Builder(instance).build(new ViewContext(settings, VERSION));
//        URI location = URI.create(decorated.getUri());

        return FormUtility.okResponse(settings, deploymentProvider, decorated, null, false);
    }

    protected AllowedTaskProvider doDetach(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawAttachmentId, Entity principal) throws PieceworkException {
        String attachmentId = sanitizer.sanitize(rawAttachmentId);

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, true, false);

        AllowedTaskProvider taskProvider = modelProviderFactory.allowedTaskProvider(rawProcessDefinitionKey, rawProcessInstanceId, principal);
        Task task = taskProvider.allowedTask(new ViewContext(settings, VERSION), true);
        if (task == null)
            throw new ForbiddenError(Constants.ExceptionCodes.task_required);

        requestService.create(requestDetails, taskProvider, ActionType.REMOVE);
        commandFactory.detachment(taskProvider, attachmentId).execute();
        return taskProvider;
    }

    protected ProcessInstanceProvider doOperation(OperationType operationType, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawReason, Entity principal) throws PieceworkException {
        ProcessInstanceProvider instanceProvider = modelProviderFactory.instanceProvider(rawProcessDefinitionKey, rawProcessInstanceId, principal);

        String reason = sanitizer.sanitize(rawReason);
        switch(operationType) {
            case ACTIVATION:
                commandFactory.activation(instanceProvider, reason).execute();
                break;
            case CANCELLATION:
                commandFactory.cancellation(instanceProvider, reason).execute();
                break;
            case RESTART:
                commandFactory.restart(instanceProvider, reason).execute();
                break;
            case SUSPENSION:
                commandFactory.suspension(instanceProvider, reason).execute();
                break;
        }
        return instanceProvider;
    }

    protected AllowedTaskProvider doRemove(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawFieldName, String rawValueId, Entity principal) throws PieceworkException {
        AllowedTaskProvider allowedTaskProvider = modelProviderFactory.allowedTaskProvider(rawProcessDefinitionKey, rawProcessInstanceId, principal);

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, true, false);
        String fieldName = sanitizer.sanitize(rawFieldName);
        String valueId = sanitizer.sanitize(rawValueId);

        commandFactory.removeValue(allowedTaskProvider, fieldName, valueId).execute();
        return allowedTaskProvider;
    }

    protected Response doSearch(MessageContext context, Entity principal) throws PieceworkException {
        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, false, false);

        UriInfo uriInfo = context.getContext(UriInfo.class);
        List<MediaType> mediaTypes = context.getHttpHeaders().getAcceptableMediaTypes();

        MultivaluedMap<String, String> rawQueryParameters = uriInfo != null ? uriInfo.getQueryParameters() : null;

        if (mediaTypes != null) {
            if (mediaTypes.contains(new MediaType("text", "csv"))) {
                String fileName = "export.csv";
                IteratingDataProvider<?> provider = processInstanceService.exportProvider(rawQueryParameters, principal, true);
                return Response.ok(provider, "text/csv").header("Content-Disposition", "attachment; filename=" + fileName).build();
            } else if (mediaTypes.contains(new MediaType("application", "vnd.ms-excel"))) {
                String fileName = "export.xls";
                IteratingDataProvider<?> provider = processInstanceService.exportProvider(rawQueryParameters, principal, false);
                return Response.ok(provider, "application/vnd.ms-excel").header("Content-Disposition", "attachment; filename=" + fileName).build();
            }
        } else {
            SearchResults results = processInstanceService.search(rawQueryParameters, principal);
            return Response.ok(results).build();
        }

        throw new NotFoundError();
    }


}
