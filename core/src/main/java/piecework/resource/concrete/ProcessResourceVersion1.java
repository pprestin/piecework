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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import piecework.Versions;
import piecework.model.ProcessDeployment;
import piecework.model.ProcessDeploymentVersion;
import piecework.service.ProcessService;
import piecework.model.SearchResults;
import piecework.common.ViewContext;
import piecework.exception.StatusCodeError;
import piecework.model.Process;
import piecework.resource.ProcessResource;
import piecework.security.concrete.PassthroughSanitizer;

import java.util.List;

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
		ResponseBuilder responseBuilder = Response.ok(new Process.Builder(result, new PassthroughSanitizer(), true).build(versions.getVersion1()));
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
				
		ResponseBuilder responseBuilder = Response.ok(new Process.Builder(result, new PassthroughSanitizer(), true).build(versions.getVersion1()));
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
    public String getVersion() {
        return versions.getVersion1().getVersion();
    }

}
