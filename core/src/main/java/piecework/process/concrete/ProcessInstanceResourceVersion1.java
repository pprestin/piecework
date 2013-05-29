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

import java.util.*;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import piecework.Constants;
import piecework.common.RequestDetails;
import piecework.engine.ProcessExecution;
import piecework.engine.ProcessExecutionCriteria;
import piecework.engine.ProcessExecutionResults;
import piecework.engine.exception.ProcessEngineException;
import piecework.exception.*;
import piecework.model.*;
import piecework.model.Process;
import piecework.process.*;
import piecework.security.Sanitizer;
import piecework.authorization.AuthorizationRole;
import piecework.common.Payload;
import piecework.common.view.SearchResults;
import piecework.common.view.ViewContext;
import piecework.engine.ProcessEngineRuntimeFacade;
import piecework.form.handler.RequestHandler;
import piecework.form.handler.SubmissionHandler;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.util.ManyMap;

/**
 * @author James Renfro
 */
@Service
public class ProcessInstanceResourceVersion1 implements ProcessInstanceResource {

    private static final Logger LOG = Logger.getLogger(ProcessInstanceResourceVersion1.class);

	@Autowired
	ProcessRepository processRepository;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    ProcessInstanceRepository processInstanceRepository;
	
	@Autowired
	ResourceHelper helper;
	
	@Autowired
	ProcessEngineRuntimeFacade facade;

    @Autowired
    RequestHandler requestHandler;
	
	@Autowired
	Sanitizer sanitizer;
	
	@Value("${base.application.uri}")
	String baseApplicationUri;
	
	@Value("${base.service.uri}")
	String baseServiceUri;

    @Value("${certificate.issuer.header}")
    String certificateIssuerHeader;

    @Value("${certificate.subject.header}")
    String certificateSubjectHeader;

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

        try {
            ProcessExecution execution = facade.findExecution(new ProcessExecutionCriteria.Builder().executionId(instance.getEngineProcessInstanceId()).build());

            ProcessInstance.Builder builder = new ProcessInstance.Builder(instance, new PassthroughSanitizer())
                .processDefinitionKey(processDefinitionKey)
                .processDefinitionLabel(process.getProcessDefinitionLabel())
                .execution(execution);

            return Response.ok(builder.build(getViewContext())).build();
        } catch (ProcessEngineException e) {
            LOG.error("Process engine unable to find execution ", e);
            throw new InternalServerError();
        }
	}

	@Override
	public Response delete(String rawProcessDefinitionKey, String rawProcessInstanceId) throws StatusCodeError {
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
		String processInstanceId = sanitizer.sanitize(rawProcessInstanceId);
		
		Process process = getProcess(processDefinitionKey);
		ProcessInstance instance = getProcessInstance(process, processInstanceId);

        try {
            if (!facade.cancel(process, processInstanceId, null, null))
                throw new ConflictError();

            ResponseBuilder responseBuilder = Response.status(Status.NO_CONTENT);
            ViewContext context = getViewContext();
            String location = context != null ? context.getApplicationUri(instance.getProcessDefinitionKey(), instance.getProcessInstanceId()) : null;
            if (location != null)
                responseBuilder.location(UriBuilder.fromPath(location).build());
            return responseBuilder.build();

        } catch (ProcessEngineException e) {
            LOG.error("Process engine unable to cancel execution ", e);
            throw new InternalServerError();
        }
	}

	@Override
	public SearchResults search(UriInfo uriInfo) throws StatusCodeError {
		MultivaluedMap<String, String> rawQueryParameters = uriInfo != null ? uriInfo.getQueryParameters() : null;
		ManyMap<String, String> queryParameters = new ManyMap<String, String>();

        ProcessExecutionCriteria.Builder criteria = new ProcessExecutionCriteria.Builder();
        String limitToProcessDefinitionKey = null;
        String limitToProcessInstanceId = null;

        DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis();

        for (Entry<String, List<String>> rawQueryParameterEntry : rawQueryParameters.entrySet()) {
			String key = sanitizer.sanitize(rawQueryParameterEntry.getKey());
			List<String> rawValues = rawQueryParameterEntry.getValue();
			if (rawValues != null && !rawValues.isEmpty()) {
				for (String rawValue : rawValues) {
					String value = sanitizer.sanitize(rawValue);
					queryParameters.putOne(key, value);

                    try {
                        if (key.equals("processDefinitionKey"))
                            limitToProcessDefinitionKey = value;
                        else if (key.equals("processInstanceId"))
                            limitToProcessInstanceId = value;
                        else if (key.equals("complete"))
                            criteria.complete(Boolean.valueOf(value));
                        else if (key.equals("initiatedBy"))
                            criteria.initiatedBy(value);
                        else if (key.equals("completedAfter"))
                            criteria.completedAfter(dateTimeFormatter.parseDateTime(value).toDate());
                        else if (key.equals("completedBefore"))
                            criteria.completedBefore(dateTimeFormatter.parseDateTime(value).toDate());
                        else if (key.equals("startedAfter"))
                            criteria.startedAfter(dateTimeFormatter.parseDateTime(value).toDate());
                        else if (key.equals("startedBefore"))
                            criteria.startedBefore(dateTimeFormatter.parseDateTime(value).toDate());
                        else if (key.equals("maxResults"))
                            criteria.maxResults(Integer.valueOf(value));
                        else if (key.equals("firstResult"))
                            criteria.firstResult(Integer.valueOf(value));

                    } catch (NumberFormatException e) {
                        LOG.warn("Unable to parse query parameter key: " + key + " value: " + value, e);
                    } catch (IllegalArgumentException e) {
                        LOG.warn("Unable to parse query parameter key: " + key + " value: " + value, e);
                    }
				}
			}
		}

        boolean noResults = false;

        ProcessInstance single = null;
        if (limitToProcessInstanceId != null) {
            single = processInstanceRepository.findOne(limitToProcessInstanceId);

            if (single != null)
                criteria.executionId(single.getEngineProcessInstanceId());
            else
                noResults = true;
        }

		SearchResults.Builder resultsBuilder = new SearchResults.Builder()
			.resourceName(ProcessInstance.Constants.ROOT_ELEMENT_NAME);

        if (! noResults) {
            List<Process> processes = helper.findProcesses(AuthorizationRole.OVERSEER);
            for (Process process : processes) {
                if (limitToProcessDefinitionKey == null || limitToProcessDefinitionKey.equals(process.getProcessDefinitionKey()))
                    criteria.engineProcessDefinitionKey(process.getEngineProcessDefinitionKey());
            }

            try {
                List<String> processInstanceIds = new ArrayList<String>();

                ProcessExecutionResults results = facade.findExecutions(criteria.build());

                long total = results.getTotal();
                long firstResult = results.getFirstResult();
                long maxResults = results.getMaxResults();

                if (results.getExecutions() != null) {
                    Map<String, ProcessExecution> executionMap = new HashMap<String, ProcessExecution>();
                    for (ProcessExecution execution : results.getExecutions()) {
                        processInstanceIds.add(execution.getBusinessKey());
                        executionMap.put(execution.getBusinessKey(), execution);
                    }

                    Iterable<ProcessInstance> instances = processInstanceRepository.findAll(processInstanceIds);
                    PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
                    for (ProcessInstance instance : instances) {
                        resultsBuilder.item(new ProcessInstance.Builder(instance, passthroughSanitizer).build(getViewContext()));
                    }
                }

            } catch (ProcessEngineException e) {
                LOG.error("Process engine unable to find executions ", e);
                throw new InternalServerError();
            }
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

        RequestDetails requestDetails = new RequestDetails.Builder(request, certificateIssuerHeader, certificateSubjectHeader).build();
        FormRequest formRequest = requestHandler.create(requestDetails, processDefinitionKey, null, null, null);
        Screen screen = formRequest.getScreen();

        ProcessInstance instance = processInstanceService.submit(process, screen, payload);

        return Response.ok(new ProcessInstance.Builder(instance, new PassthroughSanitizer()).build(getViewContext())).build();
    }

	private Process getProcess(String processDefinitionKey) throws BadRequestError {
		Process record = processRepository.findOne(processDefinitionKey);
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
