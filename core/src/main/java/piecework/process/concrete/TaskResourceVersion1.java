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

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.authorization.AuthorizationRole;
import piecework.common.RequestDetails;
import piecework.common.view.SearchResults;
import piecework.common.view.ViewContext;
import piecework.engine.ProcessEngineRuntimeFacade;
import piecework.engine.TaskCriteria;
import piecework.engine.TaskResults;
import piecework.engine.exception.ProcessEngineException;
import piecework.exception.BadRequestError;
import piecework.exception.InternalServerError;
import piecework.exception.NotFoundError;
import piecework.exception.StatusCodeError;
import piecework.form.handler.RequestHandler;
import piecework.form.handler.ResponseHandler;
import piecework.form.handler.SubmissionHandler;
import piecework.form.validation.FormValidation;
import piecework.form.validation.ValidationService;
import piecework.model.*;
import piecework.model.Process;
import piecework.process.ProcessInstancePayload;
import piecework.process.ProcessInstanceRepository;
import piecework.process.ProcessRepository;
import piecework.process.TaskResource;
import piecework.security.Sanitizer;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.util.ManyMap;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
@Service
public class TaskResourceVersion1 implements TaskResource {

    private static final Logger LOG = Logger.getLogger(TaskResourceVersion1.class);

    @Autowired
    ProcessEngineRuntimeFacade facade;

    @Autowired
    ResourceHelper helper;

    @Autowired
    ProcessRepository processRepository;

    @Autowired
    ProcessInstanceRepository processInstanceRepository;

    @Autowired
    RequestHandler requestHandler;

    @Autowired
    SubmissionHandler submissionHandler;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    ValidationService validationService;

    @Value("${base.application.uri}")
    String baseApplicationUri;

    @Value("${base.service.uri}")
    String baseServiceUri;

    @Value("${certificate.issuer.header}")
    String certificateIssuerHeader;

    @Value("${certificate.subject.header}")
    String certificateSubjectHeader;

    @Override
    public Response complete(@PathParam("processDefinitionKey") String rawProcessDefinitionKey, @PathParam("taskId") String rawTaskId, @PathParam("action") String rawAction, @Context HttpServletRequest request, MultipartBody body) throws StatusCodeError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        String taskId = sanitizer.sanitize(rawTaskId);
        String action = sanitizer.sanitize(rawAction);

        piecework.model.Process process = processRepository.findOne(processDefinitionKey);

        if (process == null)
            throw new NotFoundError(Constants.ExceptionCodes.process_does_not_exist);

        RequestDetails requestDetails = new RequestDetails.Builder(request, certificateIssuerHeader, certificateSubjectHeader).build();
        FormRequest formRequest = requestHandler.create(requestDetails, processDefinitionKey, null);

        ProcessInstancePayload payload = new ProcessInstancePayload().multipartBody(body);
        FormSubmission submission = submissionHandler.handle(formRequest, payload);

        ProcessInstance instance = null;
        if (formRequest.getProcessInstanceId() != null)
            instance = processInstanceRepository.findOne(formRequest.getProcessInstanceId());

        FormValidation validation = validationService.validate(submission, instance, formRequest.getScreen(), null);

        List<ValidationResult> results = validation.getResults();
        if (results != null && !results.isEmpty()) {
            throw new BadRequestError(new ValidationResultList(results));
        }

        ProcessInstance previous = formRequest.getProcessInstanceId() != null ? processInstanceRepository.findOne(formRequest.getProcessInstanceId()) : null;

        ProcessInstance.Builder instanceBuilder;

        if (previous != null) {
            instanceBuilder = new ProcessInstance.Builder(previous, new PassthroughSanitizer());

        } else {
            try {
                String engineInstanceId = facade.start(process, null, validation.getFormValueMap());

                instanceBuilder = new ProcessInstance.Builder()
                        .processDefinitionKey(process.getProcessDefinitionKey())
                        .processDefinitionLabel(process.getProcessDefinitionLabel())
                        .processInstanceLabel(validation.getTitle())
                        .engineProcessInstanceId(engineInstanceId);

            } catch (ProcessEngineException e) {
                LOG.error("Process engine unable to start instance ", e);
                throw new InternalServerError();
            }
        }

        instanceBuilder.formValueMap(validation.getFormValueMap())
                .restrictedValueMap(validation.getRestrictedValueMap())
                .submission(submission);

        ProcessInstance stored = processInstanceRepository.save(instanceBuilder.build());


        try {
            facade.completeTask(process, taskId);
            return Response.noContent().build();
        } catch (ProcessEngineException e) {
            LOG.error("Process engine unable to complete task ", e);
            throw new InternalServerError();
        }
    }

    public Response read(String rawProcessDefinitionKey, String rawTaskId) throws StatusCodeError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        String taskId = sanitizer.sanitize(rawTaskId);

        piecework.model.Process process = processRepository.findOne(processDefinitionKey);

        if (process == null)
            throw new NotFoundError(Constants.ExceptionCodes.process_does_not_exist);

        TaskCriteria criteria = new TaskCriteria.Builder()
                .engine(process.getEngine())
                .engineProcessDefinitionKey(process.getEngineProcessDefinitionKey())
                .taskId(taskId)
                .build();

        try {
            Task task = facade.findTask(criteria);

            return Response.ok(new Task.Builder(task, new PassthroughSanitizer()).build(getViewContext())).build();
        } catch (ProcessEngineException e) {
            LOG.error("Process engine unable to find task ", e);
            throw new InternalServerError();
        }
    }

    @Override
    public Response update(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("taskId") String taskId, @Context HttpServletRequest request, MultipartBody body) throws StatusCodeError {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SearchResults search(@Context UriInfo uriInfo) throws StatusCodeError {
        MultivaluedMap<String, String> rawQueryParameters = uriInfo != null ? uriInfo.getQueryParameters() : null;
        ManyMap<String, String> queryParameters = new ManyMap<String, String>();

        DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis();
        TaskCriteria.Builder criteriaBuilder = new TaskCriteria.Builder();
        for (Map.Entry<String, List<String>> rawQueryParameterEntry : rawQueryParameters.entrySet()) {
            String key = sanitizer.sanitize(rawQueryParameterEntry.getKey());
            List<String> rawValues = rawQueryParameterEntry.getValue();
            if (rawValues != null && !rawValues.isEmpty()) {
                for (String rawValue : rawValues) {
                    String value = sanitizer.sanitize(rawValue);
                    queryParameters.putOne(key, value);

                    try {
                        if (key.equals("taskId"))
                            criteriaBuilder.taskId(value);
                        else if (key.equals("engine"))
                            criteriaBuilder.engine(value);
                        else if (key.equals("active"))
                            criteriaBuilder.active(Boolean.valueOf(value));
                        else if (key.equals("assignee"))
                            criteriaBuilder.assigneeId(value);
                        else if (key.equals("candidateAssignee"))
                            criteriaBuilder.candidateAssigneeId(value);
                        else if (key.equals("complete"))
                            criteriaBuilder.complete(Boolean.valueOf(value));
                        else if (key.equals("createdAfter"))
                            criteriaBuilder.createdAfter(dateTimeFormatter.parseDateTime(value).toDate());
                        else if (key.equals("createdBefore"))
                            criteriaBuilder.createdBefore(dateTimeFormatter.parseDateTime(value).toDate());
                        else if (key.equals("dueBefore"))
                            criteriaBuilder.dueBefore(dateTimeFormatter.parseDateTime(value).toDate());
                        else if (key.equals("dueAfter"))
                            criteriaBuilder.dueAfter(dateTimeFormatter.parseDateTime(value).toDate());
                        else if (key.equals("alias"))
                            criteriaBuilder.businessKey(value);
                        else if (key.equals("processInstanceId"))
                            criteriaBuilder.executionId(value);
                        else if (key.equals("maxPriority"))
                            criteriaBuilder.maxPriority(Integer.valueOf(value));
                        else if (key.equals("minPriority"))
                            criteriaBuilder.minPriority(Integer.valueOf(value));

                    } catch (NumberFormatException e) {
                        LOG.warn("Unable to parse query parameter key: " + key + " value: " + value, e);
                    } catch (IllegalArgumentException e) {
                        LOG.warn("Unable to parse query parameter key: " + key + " value: " + value, e);
                    }
                }
            }
        }

        SearchResults.Builder resultsBuilder = new SearchResults.Builder().resourceName(ProcessInstance.Constants.ROOT_ELEMENT_NAME);
//        List<Process> processes = helper.findProcesses(AuthorizationRole.OVERSEER);
//        for (Process process : processes) {
//
//        }

        try {
            TaskResults results = facade.findTasks(criteriaBuilder.build());
            resultsBuilder.items(results.getTasks());
        } catch (ProcessEngineException e) {
            throw new InternalServerError();
        }
        return resultsBuilder.build();
    }

    public ViewContext getViewContext() {
        return new ViewContext(baseApplicationUri, baseServiceUri, "v1", "task", "Task");
    }

}
