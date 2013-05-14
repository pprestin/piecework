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
package piecework.process.concrete;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import piecework.Sanitizer;
import piecework.common.view.SearchResults;
import piecework.common.view.ViewContext;
import piecework.exception.GoneError;
import piecework.exception.NotFoundError;
import piecework.exception.StatusCodeError;
import piecework.process.InteractionRepository;
import piecework.process.InteractionResource;
import piecework.process.ProcessResource;
import piecework.process.ProcessService;
import piecework.process.model.Interaction;
import piecework.security.PassthroughSanitizer;

/**
 * @author James Renfro
 */
@Service
public class InteractionResourceVersion1Impl implements InteractionResource {
	
	@Autowired
	ProcessService service;
	
	@Autowired
	ProcessResource processResource;
	
	@Autowired
	InteractionRepository repository;
	
	@Autowired
	Sanitizer sanitizer;
	
	@Value("${base.application.uri}")
	String baseApplicationUri;
	
	@Value("${base.service.uri}")
	String baseServiceUri;
	
	@Override
	public Response create(String rawProcessDefinitionKey, Interaction interaction)
			throws StatusCodeError {
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);

		Interaction.Builder builder = new Interaction.Builder(interaction, sanitizer);
		Interaction record = builder.build();
		
		// Save and return the result, no need to sanitize, since it's coming from storage and
		// was already sanitized before being saved
		Interaction result = new Interaction
				.Builder(repository.save(record), new PassthroughSanitizer())
				.processDefinitionKey(processDefinitionKey)
				.build(getViewContext());
		
		return Response.ok(result).build();
	}

	@Override
	public Response read(String rawProcessDefinitionKey, String interactionId) throws StatusCodeError {
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);

		Interaction result = getInteraction(interactionId);

		ResponseBuilder responseBuilder = Response.ok(result);
		return responseBuilder.build();
	}

	@Override
	public Response update(String rawProcessDefinitionKey, String interactionId, Interaction interaction) throws StatusCodeError {
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
 
		Interaction.Builder builder = new Interaction.Builder(interaction, sanitizer);
		Interaction record = builder.build();
		Interaction result = repository.save(record);
		
		ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
		ViewContext context = getViewContext();
		String location = context != null ? context.getApplicationUri(result.getId()) : null;
		if (location != null)
			responseBuilder.location(UriBuilder.fromPath(location).build());	
		
		return responseBuilder.build();
	}

	@Override
	public Response delete(String rawProcessDefinitionKey, String interactionId) throws StatusCodeError {
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
		
//		Interaction record = repository.findOne(interactionId);
//		if (record == null)
//			throw new ProcessNotFoundException(processDefinitionKey);
//		
//		record.setDeleted(true);
//		return repository.save(record);
		
		return null;
	}

	@Override
	public SearchResults searchInteractions(String rawProcessDefinitionKey, UriInfo uriInfo) throws StatusCodeError {
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
		
		return null;
	}
	
	@Override
	public ViewContext getViewContext() {
		return new ViewContext(baseApplicationUri, baseServiceUri, "v1", "interaction", "Interaction");
	}
	
//	@Override
//	public ViewContext getViewContext() {
//		return new ChildViewContext(processResource.getViewContext(), "interaction", "Interaction");
//	}
	
	private Interaction getInteraction(String id) throws StatusCodeError {
		Interaction record = repository.findOne(id);
	
		if (record == null)
			throw new NotFoundError();
		if (record.isDeleted())
			throw new GoneError();
		
		return record;
	}

}
