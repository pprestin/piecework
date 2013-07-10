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
package piecework.designer.concrete;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import piecework.Constants;
import piecework.security.Sanitizer;
import piecework.model.SearchResults;
import piecework.common.ViewContext;
import piecework.exception.BadRequestError;
import piecework.exception.GoneError;
import piecework.exception.NotFoundError;
import piecework.exception.StatusCodeError;
import piecework.model.Interaction;
import piecework.model.Screen;
import piecework.designer.InteractionRepository;
import piecework.process.ProcessRepository;
import piecework.designer.ScreenRepository;
import piecework.designer.ScreenResource;
import piecework.security.concrete.PassthroughSanitizer;

/**
 * @author James Renfro
 */
@Service
public class ScreenResourceVersion1Impl implements ScreenResource {

	@Autowired
	ProcessRepository processRepository;
	
	@Autowired
	InteractionRepository interactionRepository;
	
	@Autowired
	ScreenRepository screenRepository;
	
	@Autowired
	Sanitizer sanitizer;
	
	@Value("${base.application.uri}")
	String baseApplicationUri;
	
	@Value("${base.service.uri}")
	String baseServiceUri;
	

	@Override
	public Response create(String rawProcessDefinitionKey, String rawInteractionId,
			Screen screen) throws StatusCodeError {
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
		String interactionId = sanitizer.sanitize(rawInteractionId);
		
		piecework.model.Process process = processRepository.findOne(processDefinitionKey);
		
		if (process == null)
			throw new BadRequestError(Constants.ExceptionCodes.process_does_not_exist, processDefinitionKey);
		
		// TODO: Check for existing interactions on this process and ensure that the interactionId is valid
		
		Interaction interaction = interactionRepository.findOne(interactionId);
		
		if (interaction == null)
			throw new BadRequestError(Constants.ExceptionCodes.interaction_invalid, interactionId);
		
		if (screen.getScreenId() != null) 
			throw new BadRequestError(Constants.ExceptionCodes.screen_id_invalid);

		Screen.Builder screenBuilder = new Screen.Builder(screen, sanitizer);
		Screen record = screenBuilder.build();
	
		PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
		
		
		// Save the result, no need to sanitize, since it's coming from storage and
		// was already sanitized before being saved
		Screen result = new Screen
				.Builder(screenRepository.save(record), passthroughSanitizer)
				.processDefinitionKey(processDefinitionKey)
				.interactionId(interactionId)
				.build(getViewContext());
		
		// Save the reference to the screen in the interaction
		interactionRepository.save(new Interaction.Builder(interaction, passthroughSanitizer).screen(result).build());
		
		return Response.ok(result).build();
	}

	@Override
	public Response read(String rawProcessDefinitionKey, String rawInteractionId,
			String screenId) throws StatusCodeError {
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
		String interactionId = sanitizer.sanitize(rawInteractionId);
		
		Screen result = getScreen(interactionId);

		ResponseBuilder responseBuilder = Response.ok(result);
		return responseBuilder.build();
	}

	@Override
	public Response update(String rawProcessDefinitionKey, String rawInteractionId,
			String screenId, Screen screen) throws StatusCodeError {
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
		String interactionId = sanitizer.sanitize(rawInteractionId);
		
		piecework.model.Process process = processRepository.findOne(processDefinitionKey);
		
		if (process == null)
			throw new BadRequestError(Constants.ExceptionCodes.process_does_not_exist, processDefinitionKey);
		
		// TODO: Check for existing interactions on this process and ensure that the interactionId is valid
		
		Interaction interaction = interactionRepository.findOne(interactionId);
		
		if (interaction == null)
			throw new BadRequestError(Constants.ExceptionCodes.interaction_invalid, interactionId);
		
		Screen.Builder builder = new Screen.Builder(screen, sanitizer);
		Screen record = builder.build();
		Screen result = screenRepository.save(record);
		
		ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
		ViewContext context = getViewContext();
		String location = context != null ? context.getApplicationUri(result.getScreenId()) : null;
		if (location != null)
			responseBuilder.location(UriBuilder.fromPath(location).build());	
		
		return responseBuilder.build();
	}

	@Override
	public Response delete(String rawProcessDefinitionKey, String rawInteractionId,
			String screenId) throws StatusCodeError {
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
		String interactionId = sanitizer.sanitize(rawInteractionId);
		
		Screen result = getScreen(interactionId);
		
		
		return null;
	}

	@Override
	public SearchResults searchInteractions(String rawProcessDefinitionKey,
			String rawInteractionId, UriInfo uriInfo) throws StatusCodeError {
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
		String interactionId = sanitizer.sanitize(rawInteractionId);
		
		return null;
	}
	
	@Override
	public ViewContext getViewContext() {
		return new ViewContext(baseApplicationUri, baseServiceUri, "v1", "screen", "Screen");
	}

	private Screen getScreen(String id) throws StatusCodeError {
		Screen record = screenRepository.findOne(id);
	
		if (record == null)
			throw new NotFoundError();
		if (record.isDeleted())
			throw new GoneError();
		
		return record;
	}
	
}
