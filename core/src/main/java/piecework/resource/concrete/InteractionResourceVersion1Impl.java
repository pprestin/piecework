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

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import piecework.Constants;
import piecework.Versions;
import piecework.enumeration.ActionType;
import piecework.exception.*;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.DeploymentRepository;
import piecework.security.Sanitizer;
import piecework.common.ViewContext;
import piecework.persistence.InteractionRepository;
import piecework.resource.InteractionResource;
import piecework.persistence.ProcessRepository;
import piecework.persistence.ScreenRepository;
import piecework.security.concrete.PassthroughSanitizer;

/**
 * @author James Renfro
 */
@Service
public class InteractionResourceVersion1Impl implements InteractionResource {

    @Autowired
    DeploymentRepository deploymentRepository;

	@Autowired
	ProcessRepository processRepository;
	
	@Autowired
	InteractionRepository interactionRepository;
	
	@Autowired
	ScreenRepository screenRepository;
	
	@Autowired
	Sanitizer sanitizer;

    @Autowired
    Versions versions;

	@Override
	public Response create(String rawProcessDefinitionKey, Interaction interaction)
			throws StatusCodeError {
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);

		// Process must already exist
		Process process = processRepository.findOne(processDefinitionKey);
		if (process == null)
			throw new BadRequestError(Constants.ExceptionCodes.process_does_not_exist, processDefinitionKey);
		
		// If the interaction has an id already then something is wrong -- probably should be a PUT
		if (interaction.getId() != null) 
			throw new BadRequestError(Constants.ExceptionCodes.interaction_id_invalid);
		
		// Builder will sanitize all input (including the screens)
		Interaction.Builder builder = new Interaction.Builder(interaction, sanitizer);
		
		// Need to save screens to screen repository because Spring Data doesn't auto cascade saves
        Map<ActionType, Screen> screens = interaction.getScreens();
        if (screens != null && !screens.isEmpty()) {
            for (Map.Entry<ActionType, Screen> entry : screens.entrySet()) {
                Screen persistedScreen = screenRepository.save(entry.getValue());
                builder.screen(entry.getKey(), persistedScreen);
            }
        }
		
		Interaction record = builder.build();
		
		PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
		
		// Save and return the result, no need to sanitize, since it's coming from storage and
		// was already sanitized before being saved
		Interaction result = new Interaction
				.Builder(interactionRepository.save(record), passthroughSanitizer)
				.processDefinitionKey(processDefinitionKey)
				.build(getViewContext());
		
		// Ensure that a reference to the interaction is added on the process
		Process.Builder processBuilder = new Process.Builder(process, passthroughSanitizer);
//		processBuilder.interaction(result);
		processRepository.save(processBuilder.build());
		
		ResponseBuilder responseBuilder = Response.ok(new Interaction.Builder(result, passthroughSanitizer).processDefinitionKey(processDefinitionKey).build(getViewContext()));
		return responseBuilder.build();
	}

	@Override
	public Response read(String rawProcessDefinitionKey, String rawInteractionId) throws StatusCodeError {
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
		String interactionId = sanitizer.sanitize(rawInteractionId);

		verifyProcessOwnsInteraction(processDefinitionKey, interactionId);
		
		Interaction result = getInteraction(interactionId);

		ResponseBuilder responseBuilder = Response.ok(new Interaction.Builder(result, new PassthroughSanitizer()).processDefinitionKey(processDefinitionKey).build(getViewContext()));
		return responseBuilder.build();
	}

	@Override
	public Response update(String rawProcessDefinitionKey, String rawInteractionId, Interaction interaction) throws StatusCodeError {
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
		String interactionId = sanitizer.sanitize(rawInteractionId);
 
		// Process must already exist
		Process process = processRepository.findOne(processDefinitionKey);
		if (process == null)
			throw new BadRequestError(Constants.ExceptionCodes.process_does_not_exist, processDefinitionKey);

		verifyProcessOwnsInteraction(processDefinitionKey, interactionId);
		
		Interaction.Builder builder = new Interaction.Builder(interaction, sanitizer);
		
		// Interaction must have same id as path param
		if (builder.getId() == null || !builder.getId().equals(interactionId))
			throw new BadRequestError(Constants.ExceptionCodes.interaction_invalid, processDefinitionKey);
		
		// Need to save screens to screen repository because Spring Data doesn't auto cascade saves
        Map<ActionType, Screen> screens = interaction.getScreens();
        if (screens != null && !screens.isEmpty()) {
            for (Map.Entry<ActionType, Screen> entry : screens.entrySet()) {
                Screen persistedScreen = screenRepository.save(entry.getValue());
                builder.screen(entry.getKey(), persistedScreen);
            }
        }

		Interaction record = builder.build();
		Interaction result = interactionRepository.save(record);
		
		ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
		ViewContext context = getViewContext();
		String location = context != null ? context.getApplicationUri(result.getId()) : null;
		if (location != null)
			responseBuilder.location(UriBuilder.fromPath(location).build());	
		
		return responseBuilder.build();
	}

	@Override
	public Response delete(String rawProcessDefinitionKey, String rawInteractionId) throws StatusCodeError {
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
		String interactionId = sanitizer.sanitize(rawInteractionId);
		
		verifyProcessOwnsInteraction(processDefinitionKey, interactionId);
		
		Interaction record = interactionRepository.findOne(interactionId);
		if (record == null)
			throw new NotFoundError();
		
		Interaction.Builder builder = new Interaction.Builder(record, new PassthroughSanitizer());
		builder.delete();
		
		Interaction result = interactionRepository.save(builder.build());
		
		ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);		
		ViewContext context = getViewContext();
		String location = context != null ? context.getApplicationUri(result.getId()) : null;
		if (location != null)
			responseBuilder.location(UriBuilder.fromPath(location).build());	
		return responseBuilder.build();
	}

	@Override
	public SearchResults searchInteractions(String rawProcessDefinitionKey, UriInfo uriInfo) throws StatusCodeError {
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
		
		Process process = getProcess(processDefinitionKey);
        ProcessDeployment deployment = process.getDeployment();
        if (deployment == null)
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);

		SearchResults.Builder builder = new SearchResults.Builder();
		List<Interaction> existingInteractions = deployment.getInteractions();
		if (existingInteractions != null && !existingInteractions.isEmpty()) {
			for (Interaction existingInteraction : existingInteractions) {
				builder.item(new Interaction.Builder(existingInteraction, new PassthroughSanitizer()).processDefinitionKey(processDefinitionKey).build(getViewContext()));
			}
		}
		
		return builder.build();
	}

	public ViewContext getViewContext() {
        return versions.getVersion1();
	}
	
	private Interaction getInteraction(String id) throws StatusCodeError {
		Interaction record = interactionRepository.findOne(id);
	
		if (record == null)
			throw new NotFoundError();
		if (record.isDeleted())
			throw new GoneError();
		
		return record;
	}
	
	private Process getProcess(String processDefinitionKey) throws BadRequestError {
		Process process = processRepository.findOne(processDefinitionKey);
		if (process == null)
			throw new BadRequestError(Constants.ExceptionCodes.process_does_not_exist, processDefinitionKey);
		return process;
	}
	
	private Process verifyProcessOwnsInteraction(String processDefinitionKey, String interactionId) throws StatusCodeError {
		Process process = getProcess(processDefinitionKey);
        ProcessDeployment deployment = process.getDeployment();
        if (deployment == null)
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);

		boolean hasInteraction = false;
		List<Interaction> existingInteractions = deployment.getInteractions();
		if (interactionId != null && existingInteractions != null && !existingInteractions.isEmpty()) {
			for (Interaction existingInteraction : existingInteractions) {
				if (existingInteraction.getId().equals(interactionId)) {
					hasInteraction = true;
					break;
				}
			}
		}
		
		if (!hasInteraction) 
			throw new BadRequestError(Constants.ExceptionCodes.interaction_invalid, processDefinitionKey);
		
		return process;
	}

}
