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
package piecework.task.concrete;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.authorization.AuthorizationRole;
import piecework.common.RequestDetails;
import piecework.engine.ProcessEngineFacade;
import piecework.enumeration.ActionType;
import piecework.form.handler.SubmissionHandler;
import piecework.form.validation.SubmissionTemplate;
import piecework.form.validation.SubmissionTemplateFactory;
import piecework.model.SearchResults;
import piecework.common.ViewContext;
import piecework.exception.*;
import piecework.identity.InternalUserDetails;
import piecework.identity.InternalUserDetailsService;
import piecework.persistence.ProcessInstanceRepository;
import piecework.task.TaskCriteria;
import piecework.task.TaskResults;
import piecework.engine.exception.ProcessEngineException;
import piecework.form.handler.RequestHandler;
import piecework.model.*;
import piecework.process.*;
import piecework.process.concrete.ResourceHelper;
import piecework.security.Sanitizer;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.task.TaskResource;
import piecework.util.ManyMap;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
@Service
public class TaskResourceVersion1 implements TaskResource {

    private static final Logger LOG = Logger.getLogger(TaskResourceVersion1.class);

    @Autowired
    Environment environment;

    @Autowired
    ProcessEngineFacade facade;

    @Autowired
    ResourceHelper helper;

    @Autowired
    InternalUserDetailsService userDetailsService;

    @Autowired
    ProcessInstanceRepository processInstanceRepository;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    RequestHandler requestHandler;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    SubmissionHandler submissionHandler;

    @Autowired
    SubmissionTemplateFactory submissionTemplateFactory;

    @Override
    public Response complete(String rawProcessDefinitionKey, String rawTaskId, String rawAction, HttpServletRequest request, Submission rawSubmission) throws StatusCodeError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        String taskId = sanitizer.sanitize(rawTaskId);
        String action = sanitizer.sanitize(rawAction);

        piecework.model.Process process = processInstanceService.getProcess(processDefinitionKey);

        RequestDetails requestDetails = requestDetails(request);

        FormRequest formRequest = requestHandler.create(requestDetails, process, null, taskId, null);
        Task task = formRequest.getTaskId() != null ? processInstanceService.userTask(process, formRequest.getTaskId()) : null;
        ProcessInstance instance = null;

        if (task != null && task.getProcessInstanceId() != null)
            instance = processInstanceService.read(process, task.getProcessInstanceId());

        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, formRequest.getScreen());
        Submission submission = submissionHandler.handle(process, template, rawSubmission, formRequest);

        ActionType validatedAction = ActionType.COMPLETE;
        if (StringUtils.isNotEmpty(action)) {
            try {
                validatedAction = ActionType.valueOf(action);
            } catch (IllegalArgumentException e) {
                throw new BadRequestError(Constants.ExceptionCodes.task_action_invalid);
            }
        }
        switch (validatedAction) {
            case COMPLETE:
                processInstanceService.submit(process, instance, task, template, submission);
                break;
            case SAVE:
                processInstanceService.save(process, instance, task, template, submission);
                break;
            case REJECT:
                processInstanceService.reject(process, instance, task, template, submission);
                break;
            case VALIDATE:
                processInstanceService.validate(process, instance, task, template, submission, true);
                break;
        }
        return Response.noContent().build();
    }

    public Response read(String rawProcessDefinitionKey, String rawTaskId) throws StatusCodeError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        String taskId = sanitizer.sanitize(rawTaskId);

        piecework.model.Process process = processInstanceService.getProcess(processDefinitionKey);

        TaskCriteria criteria = new TaskCriteria.Builder()
                .process(process)
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
    public Response update(String processDefinitionKey, String taskId, HttpServletRequest request, Task task) throws StatusCodeError {
        throw new NotImplementedException();
    }

    @Override
    public SearchResults search(UriInfo uriInfo) throws StatusCodeError {
        MultivaluedMap<String, String> rawQueryParameters = uriInfo != null ? uriInfo.getQueryParameters() : null;

        return search(rawQueryParameters);
    }

    @Override
    public SearchResults search(MultivaluedMap<String, String> rawQueryParameters) throws StatusCodeError {
        SearchResults.Builder resultsBuilder = new SearchResults.Builder().resourceName(Task.Constants.ROOT_ELEMENT_NAME)
                .resourceLabel("Tasks")
                .link(processInstanceService.getTaskViewContext().getApplicationUri())
                .uri(processInstanceService.getTaskViewContext().getServiceUri());

        ManyMap<String, String> queryParameters = new ManyMap<String, String>();
        DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis();
        TaskCriteria.Builder criteriaBuilder = new TaskCriteria.Builder().processes(helper.findProcesses(AuthorizationRole.OVERSEER, AuthorizationRole.USER));

        if (!helper.isAuthenticatedSystem()) {
            InternalUserDetails user = helper.getAuthenticatedPrincipal();
            if (user != null)
                criteriaBuilder.participantId(user.getInternalId());
            else {
                return resultsBuilder.build();
            }
        }

        ManyMap<String, String> contentQueryParameters = new ManyMap<String, String>();
        for (Map.Entry<String, List<String>> rawQueryParameterEntry : rawQueryParameters.entrySet()) {
            String key = sanitizer.sanitize(rawQueryParameterEntry.getKey());
            List<String> rawValues = rawQueryParameterEntry.getValue();
            if (rawValues != null && !rawValues.isEmpty()) {
                for (String rawValue : rawValues) {
                    String value = sanitizer.sanitize(rawValue);
                    queryParameters.putOne(key, value);

                    try {
                        boolean isEngineParameter = true;
                        if (key.equals("taskId"))
                            criteriaBuilder.taskId(value);
                        else if (key.equals("active"))
                            criteriaBuilder.active(Boolean.valueOf(value));
                        else if (key.equals("assignee"))
                            criteriaBuilder.assigneeId(toInternalId(value));
                        else if (key.equals("candidateAssignee"))
                            criteriaBuilder.candidateAssigneeId(toInternalId(value));
                        else if (key.equals("participantId"))
                            criteriaBuilder.participantId(toInternalId(value));
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
                        else if (key.equals("processInstanceId")) {
                            ProcessInstance processInstance = processInstanceRepository.findOne(value);
                            if (processInstance != null)
                                criteriaBuilder.executionId(processInstance.getEngineProcessInstanceId());
                        } else if (key.equals("maxPriority"))
                            criteriaBuilder.maxPriority(Integer.valueOf(value));
                        else if (key.equals("minPriority"))
                            criteriaBuilder.minPriority(Integer.valueOf(value));
                        else if (key.equals("maxResults"))
                            criteriaBuilder.maxResults(Integer.valueOf(value));
                        else if (key.equals("firstResult"))
                            criteriaBuilder.firstResult(Integer.valueOf(value));
                        else {
                            contentQueryParameters.putOne(key, value);
                            isEngineParameter = false;
                        }

                        if (isEngineParameter)
                            resultsBuilder.parameter(key, value);

                    } catch (NumberFormatException e) {
                        LOG.warn("Unable to parse query parameter key: " + key + " value: " + value, e);
                    } catch (IllegalArgumentException e) {
                        LOG.warn("Unable to parse query parameter key: " + key + " value: " + value, e);
                    }
                }
            }
        }

        try {
            TaskResults results = facade.findTasks(criteriaBuilder.build());
            if (results.getTasks() != null && !results.getTasks().isEmpty()) {
                PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
                List<Task> tasks = new ArrayList<Task>(results.getTasks().size());
                for (Task task : results.getTasks()) {
                    tasks.add(new Task.Builder(task, passthroughSanitizer).build(processInstanceService.getTaskViewContext()));
                }
                resultsBuilder.items(tasks);
            }

            resultsBuilder.total(results.getTotal());
            resultsBuilder.firstResult(results.getFirstResult());
            resultsBuilder.maxResults(results.getMaxResults());
        } catch (ProcessEngineException e) {
            throw new InternalServerError();
        }
        return resultsBuilder.build();
    }

    public ViewContext getViewContext() {
        String baseApplicationUri = environment.getProperty("base.application.uri");
        String baseServiceUri = environment.getProperty("base.service.uri");
        return new ViewContext(baseApplicationUri, baseServiceUri, getVersion(), "task", "Task");
    }

    @Override
    public String getVersion() {
        return "v1";
    }

    private RequestDetails requestDetails(HttpServletRequest request) {
        String certificateIssuerHeader = environment.getProperty(Constants.Settings.CERTIFICATE_ISSUER_HEADER);
        String certificateSubjectHeader = environment.getProperty(Constants.Settings.CERTIFICATE_SUBJECT_HEADER);

        return new RequestDetails.Builder(request, certificateIssuerHeader, certificateSubjectHeader).build();
    }

    private String toInternalId(String userId) {
        InternalUserDetails userDetails = userDetailsService.loadUserByAnyId(userId);
        if (userDetails != null)
            return userDetails.getInternalId();
        return null;
    }

}
