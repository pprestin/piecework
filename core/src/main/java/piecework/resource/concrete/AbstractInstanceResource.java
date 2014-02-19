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
import piecework.authorization.AuthorizationRole;
import piecework.command.CommandFactory;
import piecework.common.ViewContext;
import piecework.enumeration.OperationType;
import piecework.exception.ForbiddenError;
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.export.IteratingDataProvider;
import piecework.identity.IdentityHelper;
import piecework.model.*;
import piecework.model.Process;
import piecework.process.AttachmentQueryParameters;
import piecework.security.AccessTracker;
import piecework.security.Sanitizer;
import piecework.service.*;
import piecework.settings.SecuritySettings;
import piecework.settings.UserInterfaceSettings;
import piecework.util.FormUtility;

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
    AttachmentService attachmentService;

    @Autowired
    CommandFactory commandFactory;

    @Autowired
    DeploymentService deploymentService;

    @Autowired
    IdentityHelper helper;

    @Autowired
    ProcessService processService;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    SecuritySettings securitySettings;

    @Autowired
    UserInterfaceSettings settings;

    @Autowired
    TaskService taskService;


    protected <T> Response doAttach(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, T data, Class<T> type) throws PieceworkException {

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, true, false);

        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.attach(helper.getPrincipal(), requestDetails, rawProcessDefinitionKey, rawProcessInstanceId, data, type);
        ProcessDeployment deployment = deploymentService.read(process, instance);

        SearchResults searchResults = attachmentService.search(instance, new AttachmentQueryParameters());

        return FormUtility.allowCrossOriginResponse(deployment, searchResults);
    }

//    protected ProcessDeployment doCancel(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawReason) throws PieceworkException {
//        Process process = processService.read(rawProcessDefinitionKey);
//        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, true);
//        ProcessDeployment deployment = deploymentService.read(process, instance);
//
//        processInstanceService.cancel(helper.getPrincipal(), rawProcessDefinitionKey, rawProcessInstanceId, rawReason);
//        return deployment;
//    }

    protected <T> Response doCreate(MessageContext context, String rawProcessDefinitionKey, T data, Class<T> type) throws PieceworkException {

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, true, false);

        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.create(helper.getPrincipal(), requestDetails, rawProcessDefinitionKey, data, type);

        ProcessDeployment deployment = deploymentService.read(process, instance);
        ProcessInstance decorated = new ProcessInstance.Builder(instance).build(new ViewContext(settings, VERSION));

        Response.ResponseBuilder builder = Response.ok(decorated);
        FormUtility.addCrossOriginHeaders(builder, deployment, decorated);
        return builder.build();
    }

    protected ProcessDeployment doDetach(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawAttachmentId) throws PieceworkException {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, true);
        ProcessDeployment deployment = deploymentService.read(process, instance);
        String attachmentId = sanitizer.sanitize(rawAttachmentId);

        Entity principal = helper.getPrincipal();
        processInstanceService.deleteAttachment(process, instance, attachmentId, principal);
        return deployment;
    }

    protected ProcessDeployment doOperation(OperationType operationType, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawReason) throws PieceworkException {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, true);
        ProcessDeployment deployment = deploymentService.read(process, instance);

        Entity principal = helper.getPrincipal();
        String reason = sanitizer.sanitize(rawReason);
        switch(operationType) {
            case ACTIVATION:
                commandFactory.activation(principal, process, deployment, instance, reason).execute();
                break;
            case CANCELLATION:
                commandFactory.cancellation(principal, process, deployment, instance, reason).execute();
                break;
            case RESTART:
                commandFactory.restart(principal, process, deployment, instance, reason).execute();
                break;
            case SUSPENSION:
                commandFactory.suspension(principal, process, deployment, instance, reason).execute();
                break;
        }
        return deployment;
    }

    protected ProcessDeployment doRemove(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawFieldName, String rawValueId) throws PieceworkException {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, true);
        ProcessDeployment deployment = deploymentService.read(process, instance);

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, true, false);
        String fieldName = sanitizer.sanitize(rawFieldName);
        String valueId = sanitizer.sanitize(rawValueId);

        Entity principal = helper.getPrincipal();
        if (!principal.hasRole(process, AuthorizationRole.OVERSEER) && !taskService.hasAllowedTask(process, instance, principal, true))
            throw new ForbiddenError(Constants.ExceptionCodes.active_task_required);

        Task task = taskService.allowedTask(process, instance, principal, true);

        commandFactory.removeValue(principal, process, instance, task, fieldName, valueId).execute();

        return deployment;
    }

    protected Response doSearch(MessageContext context) throws PieceworkException {
        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        accessTracker.track(requestDetails, false, false);

        Entity principal = helper.getPrincipal();
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
