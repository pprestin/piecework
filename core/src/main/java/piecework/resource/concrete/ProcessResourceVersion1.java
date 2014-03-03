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

import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.common.ViewContext;
import piecework.content.ContentResource;
import piecework.content.concrete.BasicContentResource;
import piecework.exception.BadRequestError;
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.identity.IdentityHelper;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ModelProviderFactory;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.persistence.ProcessProvider;
import piecework.resource.ProcessResource;
import piecework.security.Sanitizer;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.service.DeploymentService;
import piecework.service.ProcessService;
import piecework.settings.UserInterfaceSettings;
import piecework.util.FormUtility;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.List;

/**
 * @author James Renfro
 */
@Service
public class ProcessResourceVersion1 implements ProcessResource {

    private static final Logger LOG = Logger.getLogger(ProcessResourceVersion1.class);
    private static final String VERSION = "v1";

    @Autowired
    DeploymentService deploymentService;

    @Autowired
    IdentityHelper helper;

    @Autowired
    ModelProviderFactory modelProviderFactory;

	@Autowired
    ProcessService processService;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    UserInterfaceSettings settings;

	@Override
	public Response create(Process rawProcess) throws PieceworkException {
		Process result = processService.create(rawProcess);
		ResponseBuilder responseBuilder = Response.ok(new Process.Builder(result, new PassthroughSanitizer()).build(new ViewContext(settings, VERSION)));
		return responseBuilder.build();
	}
	
	@Override
	public Response delete(String rawProcessDefinitionKey) throws PieceworkException {
        Process result = processService.delete(rawProcessDefinitionKey);

		ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);		
		ViewContext context = new ViewContext(settings, VERSION);
		String location = context != null ? context.getApplicationUri(result.getProcessDefinitionKey()) : null;
		if (location != null)
			responseBuilder.location(UriBuilder.fromPath(location).build());	
		return responseBuilder.build();
	}

	@Override
	public Response update(String rawProcessDefinitionKey, Process rawProcess) throws PieceworkException {
        Process result = processService.update(rawProcessDefinitionKey, rawProcess, helper.getPrincipal());
		
		ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
        ViewContext context = new ViewContext(settings, VERSION);
        String location = context != null ? context.getApplicationUri(result.getProcessDefinitionKey()) : null;
		if (location != null)
			responseBuilder.location(UriBuilder.fromPath(location).build());	
		
		return responseBuilder.build();
	}

	@Override
    public Response read(String rawProcessDefinitionKey) throws PieceworkException {
        ProcessDeploymentProvider processProvider = modelProviderFactory.deploymentProvider(rawProcessDefinitionKey, helper.getPrincipal());
        Process process = processProvider.process(new ViewContext(settings, VERSION));
        return FormUtility.okResponse(settings, processProvider, process, null, false);
	}

    @Override
    public Response createDeployment(String rawProcessDefinitionKey) throws PieceworkException {
        ProcessProvider processProvider = modelProviderFactory.processProvider(rawProcessDefinitionKey, helper.getPrincipal());
        ProcessDeployment result = processService.createDeployment(processProvider, new ProcessDeployment.Builder().build());
        ResponseBuilder responseBuilder = Response.ok(new ProcessDeployment.Builder(result, new PassthroughSanitizer(), true).build());
        return responseBuilder.build();
    }

    @Override
    public Response createDeploymentResource(String rawProcessDefinitionKey, String rawDeploymentId, MultipartBody body) throws PieceworkException {
        BasicContentResource.Builder builder = new BasicContentResource.Builder();

        List<org.apache.cxf.jaxrs.ext.multipart.Attachment> attachments = body != null ? body.getAllAttachments() : null;
        if (attachments != null && !attachments.isEmpty()) {
            for (org.apache.cxf.jaxrs.ext.multipart.Attachment attachment : attachments) {
                MediaType mediaType = attachment.getContentType();
                ContentDisposition contentDisposition = attachment.getContentDisposition();

                // Don't process if there's no content type
                if (mediaType == null)
                    continue;
                if (contentDisposition == null)
                    continue;

                builder.contentType(mediaType.toString());
                builder.filename(sanitizer.sanitize(contentDisposition.getParameter("filename")));

                try {
                    builder.inputStream(attachment.getDataHandler().getInputStream());
                } catch (IOException ioe) {
                    LOG.error("Unable to process deployment resource", ioe);
                }
            }
        }

        ProcessDeployment deployment = processService.deploy(rawProcessDefinitionKey, rawDeploymentId, builder.build());
        return Response.ok(deployment).build();
    }

    @Override
    public Response cloneDeployment(String rawProcessDefinitionKey, String rawDeploymentId) throws PieceworkException {
        ProcessDeployment result = processService.cloneDeployment(rawProcessDefinitionKey, rawDeploymentId);

        ResponseBuilder responseBuilder = Response.ok(new ProcessDeployment.Builder(result, new PassthroughSanitizer(), true).build());
        return responseBuilder.build();
    }

    @Override
    public Response deleteDeployment(String rawProcessDefinitionKey, String rawDeploymentId) throws PieceworkException {
        processService.deleteDeployment(rawProcessDefinitionKey, rawDeploymentId, helper.getPrincipal());

        ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
        return responseBuilder.build();
    }

    @Override
    public Response deleteContainer(String rawProcessDefinitionKey, String rawDeploymentId, String rawActivityKey, String rawContainerId) throws PieceworkException {
        if (rawProcessDefinitionKey == null || rawDeploymentId == null || rawActivityKey == null || rawContainerId == null)
            throw new BadRequestError();

        ProcessProvider processProvider = modelProviderFactory.processProvider(rawProcessDefinitionKey, helper.getPrincipal());
        Process process = processProvider.process();
        deploymentService.deleteContainer(process, rawDeploymentId, rawActivityKey, rawContainerId);

        ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
        return responseBuilder.build();
    }

    @Override
    public Response deleteField(String rawProcessDefinitionKey, String rawDeploymentId, String rawInteractionId, String rawSectionId, String rawFieldId) throws PieceworkException {
//        processService.deleteField(rawProcessDefinitionKey, rawDeploymentId, rawInteractionId, rawSectionId, rawFieldId);

        ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
        return responseBuilder.build();
    }

    @Override
    public Response getDeployment(String rawProcessDefinitionKey, String rawDeploymentId) throws PieceworkException {
        ProcessProvider processProvider = modelProviderFactory.processProvider(rawProcessDefinitionKey, helper.getPrincipal());
        Process process = processProvider.process();
        ProcessDeployment result = deploymentService.read(process, rawDeploymentId);

        ResponseBuilder responseBuilder = Response.ok(new ProcessDeployment.Builder(result, new PassthroughSanitizer(), true).build());
        return responseBuilder.build();
    }

    @Override
    public Response getDeploymentResource(String rawProcessDefinitionKey, String rawDeploymentId) throws PieceworkException {
        ContentResource resource = processService.getDeploymentResource(rawProcessDefinitionKey, rawDeploymentId, helper.getPrincipal());

        String contentDisposition = new StringBuilder("attachment; filename=").append(resource.getFilename()).toString();
        return Response.ok(resource, resource.contentType()).header("Content-Disposition", contentDisposition).build();
    }

    @Override
    public Response getDiagram(String rawProcessDefinitionKey, String rawDeploymentId) throws PieceworkException {
        ContentResource diagram = processService.getDiagram(rawProcessDefinitionKey, rawDeploymentId, helper.getPrincipal());
        ResponseBuilder responseBuilder = Response.ok(diagram, diagram.contentType());
        return responseBuilder.build();
    }

    @Override
    public Response getActivity(String rawProcessDefinitionKey, String rawDeploymentId, String rawActivityKey) throws PieceworkException {
        ProcessProvider processProvider = modelProviderFactory.processProvider(rawProcessDefinitionKey, helper.getPrincipal());
        Process process = processProvider.process();
        Activity activity = deploymentService.getActivity(process, rawDeploymentId, rawActivityKey);
        if (activity == null)
            throw new NotFoundError();

        return Response.ok(activity).build();
    }

    @Override
    public Response deleteActivity(String rawProcessDefinitionKey, String rawDeploymentId, String rawActivityKey) throws PieceworkException {
        ProcessProvider processProvider = modelProviderFactory.processProvider(rawProcessDefinitionKey, helper.getPrincipal());
        Process process = processProvider.process();
        ProcessDeployment deployment = deploymentService.read(process, rawDeploymentId);
        Activity activity = deploymentService.deleteActivity(deployment, rawActivityKey);
        if (activity == null)
            throw new NotFoundError();

        return Response.ok(activity).build();
    }

    @Override
    public Response publishDeployment(String rawProcessDefinitionKey, String rawDeploymentId) throws PieceworkException {
        processService.publishDeployment(rawProcessDefinitionKey, rawDeploymentId, helper.getPrincipal());

        ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
        return responseBuilder.build();
    }

    @Override
    public SearchResults searchDeployments(String rawProcessDefinitionKey, UriInfo uriInfo) throws PieceworkException {
        ProcessProvider processProvider = modelProviderFactory.processProvider(rawProcessDefinitionKey, helper.getPrincipal());
        Process process = processProvider.process();
        return deploymentService.search(process, uriInfo.getQueryParameters());
    }

    @Override
	public SearchResults search(UriInfo uriInfo) throws PieceworkException {
        Entity principal = helper.getPrincipal();
		return processService.search(uriInfo.getQueryParameters(), principal);
	}

    @Override
    public Response updateDeployment(String rawProcessDefinitionKey, String rawDeploymentId, ProcessDeployment rawDeployment) throws PieceworkException {
        ProcessProvider processProvider = modelProviderFactory.processProvider(rawProcessDefinitionKey, helper.getPrincipal());
        Process process = processProvider.process();
        deploymentService.update(process, rawDeploymentId, rawDeployment);

        ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
        return responseBuilder.build();
    }

    @Override
    public Response updateActivity(String rawProcessDefinitionKey, String rawDeploymentId, String rawActivityKey, Activity rawActivity) throws PieceworkException {
        ProcessProvider processProvider = modelProviderFactory.processProvider(rawProcessDefinitionKey, helper.getPrincipal());
        Process process = processProvider.process();
        deploymentService.updateActivity(process, rawDeploymentId, rawActivityKey, rawActivity);
        ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
        return responseBuilder.build();
    }

    @Override
    public Response updateContainer(String rawProcessDefinitionKey, String rawDeploymentId, String rawSectionId, String containerId, Container rawSection) throws PieceworkException {
//        processService.updateSection(rawProcessDefinitionKey, rawDeploymentId, rawSectionId, rawSection);

        ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
        return responseBuilder.build();
    }

    @Override
    public Response updateField(String rawProcessDefinitionKey, String rawDeploymentId, String rawInteractionId, String rawFieldId, Field rawField) throws PieceworkException {
//        processService.updateField(rawProcessDefinitionKey, rawDeploymentId, rawSectionId, rawFieldId, rawField);

        ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
        return responseBuilder.build();
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

}
