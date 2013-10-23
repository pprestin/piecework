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

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import piecework.Versions;
import piecework.exception.BadRequestError;
import piecework.model.*;
import piecework.model.Process;
import piecework.service.ProcessService;
import piecework.common.ViewContext;
import piecework.exception.StatusCodeError;
import piecework.resource.ProcessResource;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.ui.Streamable;
import piecework.ui.StreamingAttachmentContent;

/**
 * @author James Renfro
 */
@Service
public class ProcessResourceVersion1 implements ProcessResource {

	@Autowired
    ProcessService processService;

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
    public Response deleteSection(String rawProcessDefinitionKey, String rawDeploymentId, String rawInteractionId, String rawActionTypeId, String rawGroupingId, String rawSectionId) throws StatusCodeError {
        if (rawProcessDefinitionKey == null || rawDeploymentId == null || rawInteractionId == null || rawActionTypeId == null|| rawSectionId == null)
            throw new BadRequestError();

        processService.deleteSection(rawProcessDefinitionKey, rawDeploymentId, rawInteractionId, rawActionTypeId, rawGroupingId, rawSectionId);

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
    public Response getInteraction(String rawProcessDefinitionKey, String rawDeploymentId, String rawInteractionId) throws StatusCodeError {
        ProcessDeployment deployment = processService.getDeployment(rawProcessDefinitionKey, rawDeploymentId);
        Interaction interaction = processService.getInteraction(deployment, rawInteractionId);

        ResponseBuilder responseBuilder = Response.ok(new Interaction.Builder(interaction, new PassthroughSanitizer()).build(versions.getVersion1()));
        return responseBuilder.build();
    }

    @Override
    public Response deleteInteraction(String rawProcessDefinitionKey, String rawDeploymentId, String rawInteractionId) throws StatusCodeError {
        ProcessDeployment deployment = processService.getDeployment(rawProcessDefinitionKey, rawDeploymentId);
        Interaction interaction = processService.deleteInteraction(deployment, rawInteractionId);

        ResponseBuilder responseBuilder = Response.ok(new Interaction.Builder(interaction, new PassthroughSanitizer()).build(versions.getVersion1()));
        return responseBuilder.build();
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
		return processService.search(uriInfo.getQueryParameters());
	}

    @Override
    public Response updateDeployment(String rawProcessDefinitionKey, String rawDeploymentId, ProcessDeployment rawDeployment) throws StatusCodeError {
        processService.updateDeployment(rawProcessDefinitionKey, rawDeploymentId, rawDeployment);

        ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
        return responseBuilder.build();
    }

    @Override
    public Response updateInteraction(String rawProcessDefinitionKey, String rawDeploymentId, String rawInteractionId, Interaction rawInteraction) throws StatusCodeError {
//        processService.updateInteraction(rawProcessDefinitionKey, rawDeploymentId, rawInteractionId, rawInteraction);

        ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
        return responseBuilder.build();
    }

    @Override
    public Response updateSection(String rawProcessDefinitionKey, String rawDeploymentId, String rawSectionId, Section rawSection) throws StatusCodeError {
//        processService.updateSection(rawProcessDefinitionKey, rawDeploymentId, rawSectionId, rawSection);

        ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
        return responseBuilder.build();
    }

    @Override
    public Response updateField(String rawProcessDefinitionKey, String rawDeploymentId, String rawInteractionId, String rawSectionId, String rawFieldId, Field rawField) throws StatusCodeError {
//        processService.updateField(rawProcessDefinitionKey, rawDeploymentId, rawSectionId, rawFieldId, rawField);

        ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
        return responseBuilder.build();
    }

    @Override
    public String getVersion() {
        return versions.getVersion1().getVersion();
    }

}
