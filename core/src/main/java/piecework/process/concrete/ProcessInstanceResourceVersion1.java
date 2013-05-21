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
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import piecework.Constants;
import piecework.Sanitizer;
import piecework.authorization.AuthorizationRole;
import piecework.common.view.SearchResults;
import piecework.common.view.ViewContext;
import piecework.engine.ProcessEngineRuntimeFacade;
import piecework.exception.BadRequestError;
import piecework.exception.StatusCodeError;
import piecework.model.Process;
import piecework.model.ProcessInstance;
import piecework.process.ProcessInstanceRepository;
import piecework.process.ProcessInstanceResource;
import piecework.process.ProcessRepository;
import piecework.security.PassthroughSanitizer;
import piecework.util.ManyMap;

/**
 * @author James Renfro
 */
@Service
public class ProcessInstanceResourceVersion1 implements ProcessInstanceResource {

	@Autowired
	ProcessRepository repository;

    @Autowired
    ProcessInstanceRepository processInstanceRepository;
	
	@Autowired
	ResourceHelper helper;
	
	@Autowired
	ProcessEngineRuntimeFacade facade;
	
	@Autowired
	Sanitizer sanitizer;
	
	@Value("${base.application.uri}")
	String baseApplicationUri;
	
	@Value("${base.service.uri}")
	String baseServiceUri;
	
	@Override
	public Response create(String rawProcessDefinitionKey, ProcessInstance instance) throws StatusCodeError {
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
		Process process = getProcess(processDefinitionKey);
		Map<String, Object> data = null;
		String engineProcessInstanceId = facade.start(process.getEngine(), process.getEngineProcessDefinitionKey(), instance.getAlias(), data);
		
		ProcessInstance.Builder builder = new ProcessInstance.Builder(instance, sanitizer)
			.processDefinitionKey(processDefinitionKey)
			.processDefinitionLabel(process.getProcessDefinitionLabel())
            .engineProcessInstanceId(engineProcessInstanceId);

        processInstanceRepository.save(builder.build());
		
		return Response.ok(builder.build(getViewContext())).build();
	}
	
	@Override
	public Response create(String processDefinitionKey, MultivaluedMap<String, String> formData) throws StatusCodeError {
		return null;
	}

	@Override
	public Response createMultipart(String processDefinitionKey, MultipartBody body) throws StatusCodeError {
		return null;
	}

	@Override
	public Response read(String rawProcessDefinitionKey, String rawProcessInstanceId) throws StatusCodeError {
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
		String processInstanceId = sanitizer.sanitize(rawProcessInstanceId);
		
		Process process = getProcess(processDefinitionKey);
		
		ProcessInstance result = facade.findInstance(process.getEngine(), process.getEngineProcessDefinitionKey(), processInstanceId, null);
		
		ProcessInstance.Builder builder = new ProcessInstance.Builder(result, sanitizer)
			.processDefinitionKey(processDefinitionKey)
			.processDefinitionLabel(process.getProcessDefinitionLabel());
	
		return Response.ok(builder.build(getViewContext())).build();
	}

	@Override
	public Response delete(String rawProcessDefinitionKey, String rawProcessInstanceId) throws StatusCodeError {
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
		String processInstanceId = sanitizer.sanitize(rawProcessInstanceId);
		
		Process process = getProcess(processDefinitionKey);
		
		ProcessInstance result = facade.cancel(process.getEngine(), process.getEngineProcessDefinitionKey(), processInstanceId, null);
		
		ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);		
		ViewContext context = getViewContext();
		String location = context != null ? context.getApplicationUri(result.getProcessDefinitionKey(), result.getProcessInstanceId()) : null;
		if (location != null)
			responseBuilder.location(UriBuilder.fromPath(location).build());	
		return responseBuilder.build();
	}

	@Override
	public SearchResults search(UriInfo uriInfo) throws StatusCodeError {
		MultivaluedMap<String, String> rawQueryParameters = uriInfo != null ? uriInfo.getQueryParameters() : null;
		ManyMap<String, String> queryParameters = new ManyMap<String, String>();
		
		
		for (Entry<String, List<String>> rawQueryParameterEntry : rawQueryParameters.entrySet()) {
			String key = sanitizer.sanitize(rawQueryParameterEntry.getKey());
			List<String> rawValues = rawQueryParameterEntry.getValue();
			if (rawValues != null && !rawValues.isEmpty()) {
				for (String rawValue : rawValues) {
					String value = sanitizer.sanitize(rawValue);
					queryParameters.putOne(key, value);
				}
			}
		}
		
		SearchResults.Builder resultsBuilder = new SearchResults.Builder()
			.resourceName(ProcessInstance.Constants.ROOT_ELEMENT_NAME);
		List<Process> processes = helper.findProcesses(AuthorizationRole.OVERSEER);
		for (Process process : processes) {			
			resultsBuilder.items(facade.findInstances(process.getEngine(), process.getEngineProcessDefinitionKey(), queryParameters));
			// TODO: Add limiting/filtering by search results from form data
		}
		
		return resultsBuilder.build();
	}

	@Override
	public ViewContext getViewContext() {
		return new ViewContext(baseApplicationUri, baseServiceUri, "v1", "instance", "Instance");
	}
	
	private Process getProcess(String processDefinitionKey) throws BadRequestError {
		Process record = repository.findOne(processDefinitionKey);
		if (record == null)
			throw new BadRequestError(Constants.ExceptionCodes.process_does_not_exist, processDefinitionKey);
		return record;
	}
}
