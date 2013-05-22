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
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
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
import piecework.engine.ProcessExecution;
import piecework.engine.ProcessExecutionCriteria;
import piecework.security.Sanitizer;
import piecework.authorization.AuthorizationRole;
import piecework.common.Payload;
import piecework.common.view.SearchResults;
import piecework.common.view.ViewContext;
import piecework.engine.ProcessEngineRuntimeFacade;
import piecework.exception.BadRequestError;
import piecework.exception.ConflictError;
import piecework.exception.NotFoundError;
import piecework.exception.StatusCodeError;
import piecework.form.RequestHandler;
import piecework.form.SubmissionHandler;
import piecework.model.FormRequest;
import piecework.model.FormSubmission;
import piecework.model.Process;
import piecework.model.ProcessInstance;
import piecework.process.ProcessInstancePayload;
import piecework.process.ProcessInstanceRepository;
import piecework.process.ProcessInstanceResource;
import piecework.process.ProcessRepository;
import piecework.security.concrete.PassthroughSanitizer;
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
    RequestHandler requestHandler;

    @Autowired
    SubmissionHandler submissionHandler;
	
	@Autowired
	Sanitizer sanitizer;
	
	@Value("${base.application.uri}")
	String baseApplicationUri;
	
	@Value("${base.service.uri}")
	String baseServiceUri;
	
	@Override
	public Response create(HttpServletRequest request, String rawProcessDefinitionKey, ProcessInstance rawInstance) throws StatusCodeError {
        ProcessInstancePayload payload = new ProcessInstancePayload().processInstance(rawInstance);
        return create(request, rawProcessDefinitionKey, payload);
	}
	
	@Override
	public Response create(HttpServletRequest request, String rawProcessDefinitionKey, MultivaluedMap<String, String> formData) throws StatusCodeError {
        ProcessInstancePayload payload = new ProcessInstancePayload().formData(formData);
        return create(request, rawProcessDefinitionKey, payload);
	}

	@Override
	public Response createMultipart(HttpServletRequest request, String rawProcessDefinitionKey, MultipartBody body) throws StatusCodeError {
        ProcessInstancePayload payload = new ProcessInstancePayload().multipartBody(body);
        return create(request, rawProcessDefinitionKey, payload);
	}

	@Override
	public Response read(String rawProcessDefinitionKey, String rawProcessInstanceId) throws StatusCodeError {
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
		String processInstanceId = sanitizer.sanitize(rawProcessInstanceId);
		
		Process process = getProcess(processDefinitionKey);
        ProcessInstance instance = getProcessInstance(process, processInstanceId);

        // TODO: Use execution to decorate instance with useful information
		ProcessExecution execution = facade.findExecution(new ProcessExecutionCriteria.Builder().executionId(instance.getEngineProcessInstanceId()).build());

        ProcessInstance.Builder builder = new ProcessInstance.Builder(instance, new PassthroughSanitizer())
			.processDefinitionKey(processDefinitionKey)
			.processDefinitionLabel(process.getProcessDefinitionLabel());
	
		return Response.ok(builder.build(getViewContext())).build();
	}

	@Override
	public Response delete(String rawProcessDefinitionKey, String rawProcessInstanceId) throws StatusCodeError {
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
		String processInstanceId = sanitizer.sanitize(rawProcessInstanceId);
		
		Process process = getProcess(processDefinitionKey);
		ProcessInstance instance = getProcessInstance(process, processInstanceId);

		if (!facade.cancel(process, processInstanceId, null, null))
            throw new ConflictError();
		
		ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);		
		ViewContext context = getViewContext();
		String location = context != null ? context.getApplicationUri(instance.getProcessDefinitionKey(), instance.getProcessInstanceId()) : null;
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
			//resultsBuilder.items(facade.findInstances(process.getEngine(), process.getEngineProcessDefinitionKey(), queryParameters));
			// TODO: Add limiting/filtering by search results from form data
		}
		
		return resultsBuilder.build();
	}

	@Override
	public ViewContext getViewContext() {
		return new ViewContext(baseApplicationUri, baseServiceUri, "v1", "instance", "Instance");
	}

    private Response create(HttpServletRequest request, String rawProcessDefinitionKey, ProcessInstancePayload payload) throws StatusCodeError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        Process process = getProcess(processDefinitionKey);

        FormRequest formRequest = requestHandler.create(request, processDefinitionKey, null, null, null);

        ProcessInstance.Builder builder = payload.getType() == Payload.PayloadType.INSTANCE ? new ProcessInstance.Builder(payload.getInstance(), sanitizer) : new ProcessInstance.Builder();
        builder.processDefinitionKey(processDefinitionKey).processDefinitionLabel(process.getProcessDefinitionLabel());

        ProcessInstance instance = builder.build();

        FormSubmission submission = submissionHandler.handle(formRequest, payload);

        String engineProcessInstanceId = facade.start(process, instance.getAlias(), instance.getFormValueMap());
        builder.engineProcessInstanceId(engineProcessInstanceId);

        ProcessInstance persisted = processInstanceRepository.save(builder.build());

        return Response.ok(new ProcessInstance.Builder(persisted, new PassthroughSanitizer()).build(getViewContext())).build();
    }

	private Process getProcess(String processDefinitionKey) throws BadRequestError {
		Process record = repository.findOne(processDefinitionKey);
		if (record == null)
			throw new BadRequestError(Constants.ExceptionCodes.process_does_not_exist, processDefinitionKey);
		return record;
	}

    private ProcessInstance getProcessInstance(Process process, String processInstanceId) throws NotFoundError {
        ProcessInstance instance = processInstanceRepository.findOne(processInstanceId);

        if (instance == null)
            throw new NotFoundError();

        return instance;
    }

}
