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
import piecework.enumeration.OperationType;
import piecework.resource.ProcessInstanceApplicationResource;
import piecework.Constants;
import piecework.Versions;
import piecework.authorization.AuthorizationRole;
import piecework.common.ViewContext;
import piecework.exception.ForbiddenError;
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.exception.StatusCodeError;
import piecework.model.*;
import piecework.model.Process;
import piecework.process.AttachmentQueryParameters;
import piecework.service.HistoryFactory;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.service.*;
import piecework.ui.Streamable;
import piecework.ui.streaming.StreamingAttachmentContent;
import piecework.common.ManyMap;
import piecework.util.FormUtility;
import piecework.util.ProcessInstanceUtility;

import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
@Service
public class ProcessInstanceResourceVersion1 extends AbstractInstanceResource implements ProcessInstanceApplicationResource {

    private static final Logger LOG = Logger.getLogger(ProcessInstanceResourceVersion1.class);

    @Autowired
    HistoryFactory historyFactory;

    @Autowired
    RequestService requestService;

    @Autowired
    ValuesService valuesService;

    @Autowired
    Versions versions;

    @Override
    public Response activate(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawReason) throws PieceworkException {
        ProcessDeployment deployment = doOperation(OperationType.ACTIVATION, rawProcessDefinitionKey, rawProcessInstanceId, rawReason);
        return FormUtility.allowCrossOriginNoContentResponse(deployment);
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
    public Response attachOptions(String rawProcessDefinitionKey, String rawProcessInstanceId) throws PieceworkException {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, false);
        ProcessDeployment deployment = deploymentService.read(process, instance);

        LOG.debug("Attachment options for " + process.getProcessDefinitionKey());

        return FormUtility.allowCrossOriginResponse(deployment, null);
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
        ProcessDeployment deployment = doOperation(OperationType.CANCELLATION, rawProcessDefinitionKey, rawProcessInstanceId, rawReason);
        return FormUtility.allowCrossOriginNoContentResponse(deployment);
    }

    @Override
    public Response detachment(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawAttachmentId) throws PieceworkException {
        return detach(rawProcessDefinitionKey, rawProcessInstanceId, rawAttachmentId);
    }

    @Override
    public Response detach(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawAttachmentId) throws PieceworkException {
        ProcessDeployment deployment = doDetach(rawProcessDefinitionKey, rawProcessInstanceId, rawAttachmentId);
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
    public Response suspendOptions(String rawProcessDefinitionKey, String rawProcessInstanceId) throws PieceworkException {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessDeployment deployment = process.getDeployment();

        LOG.debug("Options for " + process.getProcessDefinitionKey());

        return FormUtility.allowCrossOriginResponse(deployment, null);
    }

    @Override
    public Response suspend(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawReason) throws PieceworkException {
        ProcessDeployment deployment = doOperation(OperationType.SUSPENSION, rawProcessDefinitionKey, rawProcessInstanceId, rawReason);
        return FormUtility.allowCrossOriginNoContentResponse(deployment);
    }

	@Override
	public Response search(MessageContext context) throws PieceworkException {
        return doSearch(context);
	}

    @Override
    public Response restart(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawReason) throws PieceworkException {
        ProcessDeployment deployment = doOperation(OperationType.RESTART, rawProcessDefinitionKey, rawProcessInstanceId, rawReason);
        return FormUtility.allowCrossOriginNoContentResponse(deployment);
    }

    @Override
    public Response remove(MessageContext context, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawFieldName, String rawValueId) throws PieceworkException {
        ProcessDeployment deployment = doRemove(context, rawProcessDefinitionKey, rawProcessInstanceId, rawFieldName, rawValueId);
        return FormUtility.allowCrossOriginNoContentResponse(deployment);
    }

    @Override
    public Response removeOptions(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawFieldName, String rawValueId) throws PieceworkException {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = processInstanceService.read(process, rawProcessInstanceId, false);
        ProcessDeployment deployment = deploymentService.read(process, instance);

        LOG.debug("Remove options for " + process.getProcessDefinitionKey());

        return FormUtility.allowCrossOriginResponse(deployment, null);
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

        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance stored = processInstanceService.updateField(requestDetails, rawProcessDefinitionKey, rawProcessInstanceId, fieldName, body, MultipartBody.class, helper.getPrincipal());
        ProcessDeployment deployment = deploymentService.read(process, stored);

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

        FormUtility.addCrossOriginHeaders(builder, deployment, null);
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

}
