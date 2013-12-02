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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.ext.multipart.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import piecework.Versions;
import piecework.engine.ProcessDeploymentResource;
import piecework.exception.BadRequestError;
import piecework.exception.NotFoundError;
import piecework.identity.IdentityHelper;
import piecework.model.*;
import piecework.model.Process;
import piecework.security.Sanitizer;
import piecework.service.ProcessService;
import piecework.common.ViewContext;
import piecework.exception.StatusCodeError;
import piecework.resource.ProcessResource;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.ui.Streamable;
import piecework.ui.streaming.StreamingAttachmentContent;

import java.io.IOException;
import java.util.List;

/**
 * @author James Renfro
 */
@Service
public class ProcessResourceVersion1 implements ProcessResource {

    private static final Logger LOG = Logger.getLogger(ProcessResourceVersion1.class);

    @Autowired
    IdentityHelper helper;

	@Autowired
    ProcessService processService;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    Versions versions;

	@Override
	public Response create(Process rawProcess) throws StatusCodeError {
		Process result = processService.create(rawProcess);
		ResponseBuilder responseBuilder = Response.ok(new Process.Builder(result, new PassthroughSanitizer()).build(versions.getVersion1()));
		return responseBuilder.build();
	}
	
	@Override
	public Response delete(String rawProcessDefinitionKey) throws StatusCodeError {
        Process result = processService.delete(rawProcessDefinitionKey);

		ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);		
		ViewContext context = versions.getVersion1();
		String location = context != null ? context.getApplicationUri(result.getProcessDefinitionKey()) : null;
		if (location != null)
			responseBuilder.location(UriBuilder.fromPath(location).build());	
		return responseBuilder.build();
	}

	@Override
	public Response update(String rawProcessDefinitionKey, Process rawProcess) throws StatusCodeError {
        Process result = processService.update(rawProcessDefinitionKey, rawProcess);
		
		ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
		ViewContext context = versions.getVersion1();
		String location = context != null ? context.getApplicationUri(result.getProcessDefinitionKey()) : null;
		if (location != null)
			responseBuilder.location(UriBuilder.fromPath(location).build());	
		
		return responseBuilder.build();
	}

	@Override
	public Response read(String rawProcessDefinitionKey) throws StatusCodeError {
        Process result = processService.read(rawProcessDefinitionKey);
				
		ResponseBuilder responseBuilder = Response.ok(new Process.Builder(result, new PassthroughSanitizer()).build(versions.getVersion1()));
		return responseBuilder.build();
	}

    @Override
    public Response createDeployment(String rawProcessDefinitionKey) throws StatusCodeError {
        ProcessDeployment result = processService.createDeployment(rawProcessDefinitionKey);

        ResponseBuilder responseBuilder = Response.ok(new ProcessDeployment.Builder(result, null, new PassthroughSanitizer(), true).build());
        return responseBuilder.build();
    }

    @Override
    public Response createDeploymentResource(String rawProcessDefinitionKey, String rawDeploymentId, MultipartBody body) throws StatusCodeError {
        ProcessDeploymentResource.Builder builder = new ProcessDeploymentResource.Builder();

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
                builder.name(sanitizer.sanitize(contentDisposition.getParameter("filename")));

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
    public Response cloneDeployment(String rawProcessDefinitionKey, String rawDeploymentId) throws StatusCodeError {
        ProcessDeployment result = processService.cloneDeployment(rawProcessDefinitionKey, rawDeploymentId);

        ResponseBuilder responseBuilder = Response.ok(new ProcessDeployment.Builder(result, null, new PassthroughSanitizer(), true).build());
        return responseBuilder.build();
    }

    @Override
    public Response deleteDeployment(String rawProcessDefinitionKey, String rawDeploymentId) throws StatusCodeError {
        processService.deleteDeployment(rawProcessDefinitionKey, rawDeploymentId);

        ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
        return responseBuilder.build();
    }

    @Override
    public Response deleteContainer(String rawProcessDefinitionKey, String rawDeploymentId, String rawActivityKey, String rawContainerId) throws StatusCodeError {
        if (rawProcessDefinitionKey == null || rawDeploymentId == null || rawActivityKey == null || rawContainerId == null)
            throw new BadRequestError();

        processService.deleteContainer(rawProcessDefinitionKey, rawDeploymentId, rawActivityKey, rawContainerId);

        ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
        return responseBuilder.build();
    }

    @Override
    public Response deleteField(String rawProcessDefinitionKey, String rawDeploymentId, String rawInteractionId, String rawSectionId, String rawFieldId) throws StatusCodeError {
//        processService.deleteField(rawProcessDefinitionKey, rawDeploymentId, rawInteractionId, rawSectionId, rawFieldId);

        ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
        return responseBuilder.build();
    }

    @Override
    public Response getDeployment(String rawProcessDefinitionKey, String rawDeploymentId) throws StatusCodeError {
        ProcessDeployment result = processService.getDeployment(rawProcessDefinitionKey, rawDeploymentId);

        ResponseBuilder responseBuilder = Response.ok(new ProcessDeployment.Builder(result, null, new PassthroughSanitizer(), true).build());
        return responseBuilder.build();
    }

    @Override
    public Response getDeploymentResource(String rawProcessDefinitionKey, String rawDeploymentId) throws StatusCodeError {
        Streamable result = processService.getDeploymentResource(rawProcessDefinitionKey, rawDeploymentId);

        StreamingAttachmentContent streamingAttachmentContent = new StreamingAttachmentContent(result);
        String contentDisposition = new StringBuilder("attachment; filename=").append(result.getName()).toString();
        return Response.ok(streamingAttachmentContent, streamingAttachmentContent.getContent().getContentType()).header("Content-Disposition", contentDisposition).build();
    }

    @Override
    public Response getDiagram(String rawProcessDefinitionKey, String rawDeploymentId) throws StatusCodeError {
        Streamable diagram = processService.getDiagram(rawProcessDefinitionKey, rawDeploymentId);
        StreamingAttachmentContent streamingAttachmentContent = new StreamingAttachmentContent(diagram);
        ResponseBuilder responseBuilder = Response.ok(streamingAttachmentContent);
        return responseBuilder.build();
    }

    @Override
    public Response getActivity(String rawProcessDefinitionKey, String rawDeploymentId, String rawActivityKey) throws StatusCodeError {
        Activity activity = processService.getActivity(rawProcessDefinitionKey, rawDeploymentId, rawActivityKey);
        if (activity == null)
            throw new NotFoundError();

        return Response.ok(activity).build();
    }

    @Override
    public Response deleteActivity(String rawProcessDefinitionKey, String rawDeploymentId, String rawActivityKey) throws StatusCodeError {
        ProcessDeployment deployment = processService.getDeployment(rawProcessDefinitionKey, rawDeploymentId);
        Activity activity = processService.deleteActivity(deployment, rawActivityKey);
        if (activity == null)
            throw new NotFoundError();

        return Response.ok(activity).build();
    }

    @Override
    public Response publishDeployment(String rawProcessDefinitionKey, String rawDeploymentId) throws StatusCodeError {
        processService.publishDeployment(rawProcessDefinitionKey, rawDeploymentId);

        ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
        return responseBuilder.build();
    }

    @Override
    public SearchResults searchDeployments(String rawProcessDefinitionKey, UriInfo uriInfo) throws StatusCodeError {
        return processService.searchDeployments(rawProcessDefinitionKey, uriInfo.getQueryParameters());
    }

    @Override
	public SearchResults search(UriInfo uriInfo) throws StatusCodeError {
        Entity principal = helper.getPrincipal();
		return processService.search(uriInfo.getQueryParameters(), principal);
	}

    @Override
    public Response updateDeployment(String rawProcessDefinitionKey, String rawDeploymentId, ProcessDeployment rawDeployment) throws StatusCodeError {
        processService.updateDeployment(rawProcessDefinitionKey, rawDeploymentId, rawDeployment);

        ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
        return responseBuilder.build();
    }

    @Override
    public Response updateActivity(String rawProcessDefinitionKey, String rawDeploymentId, String rawActivityKey, Activity rawActivity) throws StatusCodeError {
        processService.updateActivity(rawProcessDefinitionKey, rawDeploymentId, rawActivityKey, rawActivity);

        ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
        return responseBuilder.build();
    }

    @Override
    public Response updateContainer(String rawProcessDefinitionKey, String rawDeploymentId, String rawSectionId, String containerId, Container rawSection) throws StatusCodeError {
//        processService.updateSection(rawProcessDefinitionKey, rawDeploymentId, rawSectionId, rawSection);

        ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
        return responseBuilder.build();
    }

    @Override
    public Response updateField(String rawProcessDefinitionKey, String rawDeploymentId, String rawInteractionId, String rawFieldId, Field rawField) throws StatusCodeError {
//        processService.updateField(rawProcessDefinitionKey, rawDeploymentId, rawSectionId, rawFieldId, rawField);

        ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
        return responseBuilder.build();
    }

    @Override
    public String getVersion() {
        return versions.getVersion1().getVersion();
    }

}
