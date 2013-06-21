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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import piecework.Constants;
import piecework.common.RequestDetails;
import piecework.process.ProcessInstanceSearchCriteria;
import piecework.model.ProcessExecution;
import piecework.engine.exception.ProcessEngineException;
import piecework.exception.*;
import piecework.model.*;
import piecework.model.Process;
import piecework.process.*;
import piecework.security.Sanitizer;
import piecework.common.view.SearchResults;
import piecework.common.view.ViewContext;
import piecework.engine.ProcessEngineRuntimeFacade;
import piecework.form.handler.RequestHandler;
import piecework.security.concrete.PassthroughSanitizer;

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

        ProcessInstance.Builder builder = new ProcessInstance.Builder(instance, new PassthroughSanitizer())
                .processDefinitionKey(processDefinitionKey)
                .processDefinitionLabel(process.getProcessDefinitionLabel());

        try {
            ProcessExecution execution = facade.findExecution(new ProcessInstanceSearchCriteria.Builder().executionId(instance.getEngineProcessInstanceId()).build());

            if (execution != null) {
                builder.startTime(execution.getStartTime());
                builder.endTime(execution.getEndTime());
                builder.initiatorId(execution.getInitiatorId());
            }

        } catch (ProcessEngineException e) {
            LOG.error("Process engine unable to find execution ", e);
        }

        return Response.ok(builder.build(getViewContext())).build();
	}

    @Override
    public Response update(@PathParam("processDefinitionKey") String rawProcessDefinitionKey, @PathParam("processInstanceId") String rawProcessInstanceId, ProcessInstance instance) throws StatusCodeError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        String processInstanceId = sanitizer.sanitize(rawProcessInstanceId);

        Process process = getProcess(processDefinitionKey);
        ProcessInstance persisted = getProcessInstance(process, processInstanceId);
        ProcessInstance sanitized = new ProcessInstance.Builder(instance, sanitizer).build();

        String applicationStatus = sanitized.getApplicationStatus();
        String applicationStatusExplanation = sanitized.getApplicationStatusExplanation();

        if (applicationStatus != null && applicationStatus.equalsIgnoreCase(Constants.ProcessStatuses.SUSPENDED)) {
            try {
                if (!facade.suspend(process, instance, applicationStatusExplanation))
                    throw new ConflictError();

                ProcessInstance.Builder modified = new ProcessInstance.Builder(persisted, new PassthroughSanitizer())
                        .applicationStatus(applicationStatus)
                        .applicationStatusExplanation(applicationStatusExplanation)
                        .processStatus(Constants.ProcessStatuses.SUSPENDED);

                processInstanceRepository.save(modified.build());

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

        throw new BadRequestError(Constants.ExceptionCodes.instance_cannot_be_modified);
    }

    @Override
	public Response delete(String rawProcessDefinitionKey, String rawProcessInstanceId, String rawReason) throws StatusCodeError {
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
		String processInstanceId = sanitizer.sanitize(rawProcessInstanceId);
        String reason = sanitizer.sanitize(rawReason);
		
		Process process = getProcess(processDefinitionKey);
		ProcessInstance instance = getProcessInstance(process, processInstanceId);

        try {
            if (!facade.cancel(process, instance, reason))
                throw new ConflictError();

            ProcessInstance.Builder modified = new ProcessInstance.Builder(instance, new PassthroughSanitizer())
                    .applicationStatus(process.getCancellationStatus())
                    .applicationStatusExplanation(reason)
                    .processStatus(Constants.ProcessStatuses.CANCELLED);

            processInstanceRepository.save(modified.build());

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
		return processInstanceService.search(rawQueryParameters, getViewContext());
	}

	@Override
	public ViewContext getViewContext() {
		return new ViewContext(baseApplicationUri, baseServiceUri, getVersion(), "instance", "Instance");
	}

    @Override
    public String getVersion() {
        return "v1";
    }

    private Response create(HttpServletRequest request, String rawProcessDefinitionKey, ProcessInstancePayload payload) throws StatusCodeError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        Process process = getProcess(processDefinitionKey);

        RequestDetails requestDetails = new RequestDetails.Builder(request, certificateIssuerHeader, certificateSubjectHeader).build();
        FormRequest formRequest = requestHandler.create(requestDetails, process);
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
