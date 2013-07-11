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

import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import piecework.Constants;
import piecework.persistence.InteractionRepository;
import piecework.persistence.ScreenRepository;
import piecework.model.Interaction;
import piecework.model.Screen;
import piecework.process.ProcessService;
import piecework.security.Sanitizer;
import piecework.authorization.AuthorizationRole;
import piecework.model.SearchResults;
import piecework.common.ViewContext;
import piecework.exception.ForbiddenError;
import piecework.exception.GoneError;
import piecework.exception.NotFoundError;
import piecework.exception.StatusCodeError;
import piecework.model.Process;
import piecework.persistence.ProcessRepository;
import piecework.process.ProcessResource;
import piecework.security.concrete.PassthroughSanitizer;

/**
 * @author James Renfro
 */
@Service
public class ProcessResourceVersion1 implements ProcessResource {

	@Autowired
    ProcessService processService;

	@Override
	public Response create(Process rawProcess) throws StatusCodeError {
		Process result = processService.create(rawProcess);
		ResponseBuilder responseBuilder = Response.ok(new Process.Builder(result, new PassthroughSanitizer()).build(processService.getProcessViewContext()));
		return responseBuilder.build();
	}
	
	@Override
	public Response delete(String rawProcessDefinitionKey) throws StatusCodeError {
        Process result = processService.delete(rawProcessDefinitionKey);

		ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);		
		ViewContext context = processService.getProcessViewContext();
		String location = context != null ? context.getApplicationUri(result.getProcessDefinitionKey()) : null;
		if (location != null)
			responseBuilder.location(UriBuilder.fromPath(location).build());	
		return responseBuilder.build();
	}

	@Override
	public Response update(String rawProcessDefinitionKey, Process rawProcess) throws StatusCodeError {
        Process result = processService.update(rawProcessDefinitionKey, rawProcess);
		
		ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
		ViewContext context = processService.getProcessViewContext();
		String location = context != null ? context.getApplicationUri(result.getProcessDefinitionKey()) : null;
		if (location != null)
			responseBuilder.location(UriBuilder.fromPath(location).build());	
		
		return responseBuilder.build();
	}

	@Override
	public Response read(String rawProcessDefinitionKey) throws StatusCodeError {
        Process result = processService.read(rawProcessDefinitionKey);
				
		ResponseBuilder responseBuilder = Response.ok(new Process.Builder(result, new PassthroughSanitizer()).build(processService.getProcessViewContext()));
		return responseBuilder.build();
	}
	
	@Override
	public SearchResults search(UriInfo uriInfo) throws StatusCodeError {
		return processService.search(uriInfo.getQueryParameters());
	}

    @Override
    public String getVersion() {
        return processService.getVersion();
    }

}
