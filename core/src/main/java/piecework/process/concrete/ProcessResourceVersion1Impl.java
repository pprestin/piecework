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

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import piecework.Constants;
import piecework.Sanitizer;
import piecework.authorization.AuthorizationRole;
import piecework.common.view.SearchResults;
import piecework.common.view.ViewContext;
import piecework.exception.ForbiddenError;
import piecework.exception.NotFoundError;
import piecework.exception.StatusCodeError;
import piecework.process.ProcessResource;
import piecework.process.ProcessService;
import piecework.process.exception.ProcessNotFoundException;
import piecework.process.model.view.ProcessView;
import piecework.security.PassthroughSanitizer;

/**
 * @author James Renfro
 */
@Service
public class ProcessResourceVersion1Impl implements ProcessResource {

	@Autowired
	ProcessService service;
	
	@Autowired
	Sanitizer sanitizer;
	
	@Value("${base.application.uri}")
	String baseApplicationUri;
	
	@Value("${base.service.uri}")
	String baseServiceUri;
	
	@Override
	public Response create(ProcessView process) throws StatusCodeError {
		// ProcessService handles sanitizing of fields
		piecework.process.model.Process result = service.storeProcess(process);
		
		// Can use PassthroughSanitizer on response, since data is coming from storage 
		ProcessView view = new ProcessView.Builder(result, new PassthroughSanitizer()).build(getViewContext());

		ResponseBuilder responseBuilder = Response.status(Status.CREATED);
		URI location = toURI(view.getUri());
		if (location != null)
			responseBuilder.location(location);
		
		return responseBuilder.build();
	}
	
	@Override
	public Response delete(String rawProcessDefinitionKey) throws StatusCodeError {
		// Sanitize all user input
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);

		piecework.process.model.Process result;
		
		try {
			result = service.deleteProcess(processDefinitionKey);
		} catch (ProcessNotFoundException nested) {
			// If the current process does not exist, then return 404
			throw new NotFoundError(Constants.ExceptionCodes.process_does_not_exist, processDefinitionKey);
		}	
		
		// Can use PassthroughSanitizer on response, since data is coming from storage 
		ProcessView view = new ProcessView.Builder(result, new PassthroughSanitizer()).build(getViewContext());
		
		ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
		URI location = toURI(view.getUri());
		if (location != null)
			responseBuilder.location(location);
		
		return responseBuilder.build();
	}

	@Override
	public Response update(String rawProcessDefinitionKey, ProcessView process) throws StatusCodeError {
		// Sanitize all user input
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
		String includedKey = sanitizer.sanitize(process.getProcessDefinitionKey());	
		
		// If the path param key is not the same as the one that's included in the process, then this put is a rename
		// of the key -- this means we delete the old one and create a new one, assuming that the new one doesn't conflict
		// with an existing key
		if (!processDefinitionKey.equals(includedKey)) {
			try {
				// Check for a process with the new key
				service.getProcess(includedKey);
				// If the exception wasn't thrown then it means that a process with that key already exists
				throw new ForbiddenError(Constants.ExceptionCodes.process_change_key_duplicate, processDefinitionKey, includedKey);
			} catch (ProcessNotFoundException exception) {
				try {
					service.deleteProcess(processDefinitionKey);
				} catch (ProcessNotFoundException nested) {
					// If the current process does not exist, then return 404
					throw new NotFoundError(Constants.ExceptionCodes.process_does_not_exist, processDefinitionKey);
				}				
			}
		}
		
		// ProcessService handles sanitizing of fields
		piecework.process.model.Process result = service.storeProcess(process);
				
		// Can use PassthroughSanitizer on response, since data is coming from storage 
		ProcessView view = new ProcessView.Builder(result, new PassthroughSanitizer()).build(getViewContext());
		
		ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
		URI location = toURI(view.getUri());
		if (location != null)
			responseBuilder.location(location);
		
		return responseBuilder.build();
	}

	@Override
	public ProcessView read(String rawProcessDefinitionKey) throws StatusCodeError {
		// Sanitize all user input
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
				
		piecework.process.model.Process result;
		try {
			result = service.getProcess(processDefinitionKey);
		} catch (ProcessNotFoundException e) {
			throw new NotFoundError();
		}
				
		// Can use PassthroughSanitizer on response, since data is coming from storage 
		return new ProcessView.Builder(result, new PassthroughSanitizer()).build();
	}
	
	@Override
	public SearchResults search(UriInfo uriInfo) throws StatusCodeError {	
		SearchResults.Builder resultsBuilder = new SearchResults.Builder();
		List<piecework.process.model.Process> processes = service.findProcesses(AuthorizationRole.OWNER, AuthorizationRole.CREATOR);
		for (piecework.process.model.Process process : processes) {
			resultsBuilder.item(new ProcessView.Builder(process, sanitizer).build(getViewContext()));
		}
		
		return resultsBuilder.build();
	}

	@Override
	public String getPageName() {
		return "Process";
	}
	
	private ViewContext getViewContext() {
		return new ViewContext(baseApplicationUri, baseServiceUri, "v1", "process");
	}
	
	private URI toURI(String uri) {
		if (uri == null)
			return null;
		
		return UriBuilder.fromPath(uri).build();
	}
	
}
