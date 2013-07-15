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
package piecework.process;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.authorization.AuthorizationRole;
import piecework.common.Payload;
import piecework.engine.ProcessEngineFacade;
import piecework.identity.InternalUserDetailsService;
import piecework.model.SearchResults;
import piecework.common.ViewContext;
import piecework.engine.exception.ProcessEngineException;
import piecework.exception.*;
import piecework.form.handler.SubmissionHandler;
import piecework.form.validation.FormValidation;
import piecework.form.validation.ValidationService;
import piecework.identity.InternalUserDetails;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.AttachmentRepository;
import piecework.persistence.ContentRepository;
import piecework.persistence.ProcessInstanceRepository;
import piecework.persistence.ProcessRepository;
import piecework.process.concrete.ResourceHelper;
import piecework.security.Sanitizer;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.task.TaskCriteria;
import piecework.ui.StreamingAttachmentContent;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.*;

/**
 * @author James Renfro
 */
@Service
public class ProcessInstanceService {

    private static final Logger LOG = Logger.getLogger(ProcessInstanceService.class);

    @Autowired
    AttachmentRepository attachmentRepository;

    @Autowired
    ContentRepository contentRepository;

    @Autowired
    Environment environment;

    @Autowired
    ProcessService processService;

    @Autowired
    ProcessInstanceResource processInstanceResource;

    @Autowired
    ProcessEngineFacade facade;

    @Autowired
    ResourceHelper helper;

    @Autowired
    MongoOperations mongoOperations;

    @Autowired
    ProcessRepository processRepository;

    @Autowired
    ProcessInstanceRepository processInstanceRepository;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    SubmissionHandler submissionHandler;

    @Autowired
    InternalUserDetailsService userDetailsService;

    @Autowired
    ValidationService validationService;

    public void delete(String processDefinitionKey, String processInstanceId, String reason) throws StatusCodeError {
        Process process = getProcess(processDefinitionKey);
        ProcessInstance instance = findOne(process, processInstanceId);

        try {
            if (!facade.cancel(process, instance, reason))
                throw new ConflictError();

            ProcessInstance.Builder modified = new ProcessInstance.Builder(instance, new PassthroughSanitizer())
                    .applicationStatus(process.getCancellationStatus())
                    .applicationStatusExplanation(reason)
                    .processStatus(Constants.ProcessStatuses.CANCELLED);

            processInstanceRepository.save(modified.build());

        } catch (ProcessEngineException e) {
            LOG.error("Process engine unable to cancel execution ", e);
            throw new InternalServerError();
        }
    }

    public SearchResults findAttachments(String rawProcessDefinitionKey, String rawProcessInstanceId, AttachmentQueryParameters queryParameters) throws StatusCodeError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        String processInstanceId = sanitizer.sanitize(rawProcessInstanceId);
        String paramName = sanitizer.sanitize(queryParameters.getName());
        String paramContentType = sanitizer.sanitize(queryParameters.getContentType());
        String paramUserId = sanitizer.sanitize(queryParameters.getUserId());

        Process process = getProcess(processDefinitionKey);
        ProcessInstance processInstance = findOne(process, processInstanceId);

        SearchResults.Builder searchResultsBuilder = new SearchResults.Builder();

        int count = 0;
        Map<String, User> userMap = new HashMap<String, User>();
        List<Attachment> storedAttachments = processInstance.getAttachments();
        if (storedAttachments != null && !storedAttachments.isEmpty()) {
            PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
            for (Attachment storedAttachment : storedAttachments) {
                String userId = storedAttachment.getUserId();

                if (StringUtils.isNotEmpty(paramName) && (StringUtils.isEmpty(storedAttachment.getName()) || !paramName.equals(storedAttachment.getName())))
                    continue;

                if (StringUtils.isNotEmpty(paramContentType) && (StringUtils.isEmpty(storedAttachment.getContentType()) || !paramContentType.equals(storedAttachment.getContentType())))
                    continue;

                if (StringUtils.isNotEmpty(paramUserId) && (StringUtils.isEmpty(userId) || !paramUserId.equals(userId)))
                    continue;

                User user = userId != null ? userMap.get(userId) : null;
                if (user == null && userId != null) {
                    UserDetails userDetails = userDetailsService.loadUserByInternalId(userId);
                    user = new User.Builder(userDetails).build();

                    if (user != null)
                        userMap.put(user.getUserId(), user);
                }

                searchResultsBuilder.item(new Attachment.Builder(storedAttachment, passthroughSanitizer)
                            .processDefinitionKey(processInstance.getProcessDefinitionKey())
                            .processInstanceId(processInstanceId)
                            .user(user)
                            .build(getInstanceViewContext()));

                count++;
            }
        }

        searchResultsBuilder.firstResult(0);
        searchResultsBuilder.maxResults(count);
        searchResultsBuilder.total(Long.valueOf(count));

        return searchResultsBuilder.build();
    }

    public ProcessInstance findOne(Process process, String processInstanceId) throws StatusCodeError {
        ProcessInstance instance = processInstanceRepository.findOne(processInstanceId);

        if (instance == null || !instance.getProcessDefinitionKey().equals(process.getProcessDefinitionKey()))
            throw new NotFoundError(Constants.ExceptionCodes.instance_does_not_exist);

        if (instance.isDeleted())
            throw new GoneError(Constants.ExceptionCodes.instance_does_not_exist);

        return instance;
    }

    public StreamingAttachmentContent getAttachmentContent(Process process, ProcessInstance processInstance, String attachmentId) {

        List<Attachment> storedAttachments = processInstance.getAttachments();
        if (storedAttachments != null && !storedAttachments.isEmpty()) {
            for (Attachment storedAttachment : storedAttachments) {
                if (StringUtils.isEmpty(attachmentId) || StringUtils.isEmpty(storedAttachment.getAttachmentId()) || !attachmentId.equals(storedAttachment.getAttachmentId()))
                    continue;

                Content content = contentRepository.findByLocation(storedAttachment.getLocation());
                if (content != null)
                    return new StreamingAttachmentContent(storedAttachment, content);
            }
        }

        return null;
    }

    public Process getProcess(String processDefinitionKey) throws StatusCodeError {
        Process record = processRepository.findOne(processDefinitionKey);
        if (record == null)
            throw new BadRequestError(Constants.ExceptionCodes.process_does_not_exist, processDefinitionKey);
        if (record.isDeleted())
            throw new GoneError(Constants.ExceptionCodes.process_does_not_exist, processDefinitionKey);

        return record;
    }

    public void update(String processDefinitionKey, String processInstanceId, ProcessInstance processInstance) throws StatusCodeError {
        Process process = getProcess(processDefinitionKey);
        ProcessInstance persisted = findOne(process, processInstanceId);
        ProcessInstance sanitized = new ProcessInstance.Builder(processInstance, sanitizer).build();

        String applicationStatus = sanitized.getApplicationStatus();
        String applicationStatusExplanation = sanitized.getApplicationStatusExplanation();

        if (applicationStatus != null && applicationStatus.equalsIgnoreCase(Constants.ProcessStatuses.SUSPENDED)) {
            try {
                if (!facade.suspend(process, persisted, applicationStatusExplanation))
                    throw new ConflictError();

                ProcessInstance.Builder modified = new ProcessInstance.Builder(persisted, new PassthroughSanitizer())
                        .applicationStatus(applicationStatus)
                        .applicationStatusExplanation(applicationStatusExplanation)
                        .processStatus(Constants.ProcessStatuses.SUSPENDED);

                processInstanceRepository.save(modified.build());
                return;

            } catch (ProcessEngineException e) {
                LOG.error("Process engine unable to cancel execution ", e);
                throw new InternalServerError();
            }
        }

        throw new BadRequestError(Constants.ExceptionCodes.instance_cannot_be_modified);

    }

    public SearchResults search(MultivaluedMap<String, String> rawQueryParameters, ViewContext viewContext) throws StatusCodeError {
        ProcessInstanceSearchCriteria.Builder executionCriteriaBuilder =
                new ProcessInstanceSearchCriteria.Builder(rawQueryParameters, sanitizer);

        SearchResults.Builder resultsBuilder = new SearchResults.Builder()
                .resourceLabel("Workflows")
                .resourceName(ProcessInstance.Constants.ROOT_ELEMENT_NAME)
                .link(viewContext.getApplicationUri());

        Set<Process> allowedProcesses = helper.findProcesses(AuthorizationRole.OVERSEER);
        if (!allowedProcesses.isEmpty()) {
            for (Process allowedProcess : allowedProcesses) {
                executionCriteriaBuilder.processDefinitionKey(allowedProcess.getProcessDefinitionKey())
                    .engineProcessDefinitionKey(allowedProcess.getEngineProcessDefinitionKey())
                    .engine(allowedProcess.getEngine());

                resultsBuilder.definition(new Process.Builder(allowedProcess, new PassthroughSanitizer()).interactions(null).build(processService.getProcessViewContext()));
            }
            ProcessInstanceSearchCriteria executionCriteria = executionCriteriaBuilder.build();

            if (executionCriteria.getSanitizedParameters() != null) {
                for (Map.Entry<String, List<String>> entry : executionCriteria.getSanitizedParameters().entrySet()) {
                    resultsBuilder.parameter(entry.getKey(), entry.getValue());
                }
            }

            if (executionCriteria.getProcessInstanceIds().size() == 1) {
                // If the user provided an actual instance id, then we can look it up directly and ignore the other parameters
                String processInstanceId = executionCriteria.getProcessInstanceIds().iterator().next();
                resultsBuilder.parameter("processInstanceId", processInstanceId);

                if (StringUtils.isNotEmpty(processInstanceId)) {
                    ProcessInstance single = processInstanceRepository.findOne(processInstanceId);

                    // Verify that the user is allowed to see processes like this instance
                    if (single != null && single.getProcessDefinitionKey() != null && allowedProcesses.contains(single.getProcessDefinitionKey())) {
                        resultsBuilder.item(single);
                        resultsBuilder.total(Long.valueOf(1));
                        resultsBuilder.firstResult(1);
                        resultsBuilder.maxResults(1);
                    }
                }
            } else {
                // Otherwise, look up all instances that match the query
                Query query = new ProcessInstanceQueryBuilder(executionCriteria).build();
                // Don't include form data in the result
                query.fields().exclude("formData");

                List<ProcessInstance> processInstances = mongoOperations.find(query, ProcessInstance.class);
                if (processInstances != null && !processInstances.isEmpty()) {
                    for (ProcessInstance processInstance : processInstances) {
                        resultsBuilder.item(new ProcessInstance.Builder(processInstance, new PassthroughSanitizer()).build(processInstanceResource.getViewContext()));
                    }

                    int size = processInstances.size();
                    if (executionCriteria.getMaxResults() != null || executionCriteria.getFirstResult() != null) {
                        long total = mongoOperations.count(query, ProcessInstance.class);

                        if (executionCriteria.getFirstResult() != null)
                            resultsBuilder.firstResult(executionCriteria.getFirstResult());
                        else
                            resultsBuilder.firstResult(1);

                        if (executionCriteria.getMaxResults() != null)
                            resultsBuilder.maxResults(executionCriteria.getMaxResults());
                        else
                            resultsBuilder.maxResults(size);

                        resultsBuilder.total(total);
                    } else {
                        resultsBuilder.firstResult(1);
                        resultsBuilder.maxResults(size);
                        resultsBuilder.total(Long.valueOf(size));
                    }
                }
            }
        }
        return resultsBuilder.build();
    }

    public ProcessInstance store(Process process, FormSubmission submission, FormValidation validation, ProcessInstance previous, boolean isAttachment) throws StatusCodeError {
        ProcessInstance.Builder instanceBuilder;

        String processInstanceLabel = process.getProcessInstanceLabelTemplate();

        if (!isAttachment && processInstanceLabel != null && processInstanceLabel.indexOf('{') != -1) {
            Map<String, String> scopes = new HashMap<String, String>();

            Map<String, List<String>> formValueMap = validation.getFormValueMap();
            if (formValueMap != null) {
                for (Map.Entry<String, List<String>> entry : formValueMap.entrySet()) {
                    List<String> values = entry.getValue();
                    if (values != null && !values.isEmpty())
                        scopes.put(entry.getKey(), values.iterator().next());
                }
            }

            StringWriter writer = new StringWriter();
            MustacheFactory mf = new DefaultMustacheFactory();
            Mustache mustache = mf.compile(new StringReader(processInstanceLabel), "processInstanceLabel");
            mustache.execute(writer, scopes);

            processInstanceLabel = writer.toString();

            if (StringUtils.isEmpty(processInstanceLabel))
                processInstanceLabel = "Submission " + DateFormat.getDateTimeInstance(
                        DateFormat.SHORT,
                        DateFormat.SHORT,
                        Locale.getDefault());
        }

        PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();

        if (previous != null) {
            instanceBuilder = new ProcessInstance.Builder(previous, new PassthroughSanitizer())
                    .submission(submission)
                    .attachments(validation.getAttachments(), passthroughSanitizer);

            if (!isAttachment) {
                instanceBuilder.formValueMap(validation.getFormValueMap())
                        .processInstanceLabel(processInstanceLabel)
                        .restrictedValueMap(validation.getRestrictedValueMap());
            }
        } else {
            if (isAttachment)
                throw new ForbiddenError();

            try {
                InternalUserDetails user = helper.getAuthenticatedPrincipal();
                String initiatorId = user != null ? user.getInternalId() : null;
                String initiationStatus = process.getInitiationStatus();
                instanceBuilder = new ProcessInstance.Builder()
                        .processDefinitionKey(process.getProcessDefinitionKey())
                        .processDefinitionLabel(process.getProcessDefinitionLabel())
//                        .processInstanceId(processInstanceId)
                        .processInstanceLabel(processInstanceLabel)
                        .formValueMap(validation.getFormValueMap())
                        .restrictedValueMap(validation.getRestrictedValueMap())
                        .submission(submission)
                        .startTime(new Date())
                        .initiatorId(initiatorId)
                        .processStatus(Constants.ProcessStatuses.OPEN)
                        .attachments(validation.getAttachments(), passthroughSanitizer)
                        .applicationStatus(initiationStatus);

                // Save it before routing, then save again with the engine instance id
                ProcessInstance stored = processInstanceRepository.save(instanceBuilder.build());

                Map<String, String> variables = new HashMap<String, String>();
                variables.put("PIECEWORK_PROCESS_DEFINITION_KEY", process.getProcessDefinitionKey());
                variables.put("PIECEWORK_PROCESS_INSTANCE_ID", stored.getProcessInstanceId());
                variables.put("PIECEWORK_PROCESS_INSTANCE_LABEL", processInstanceLabel);

                String engineInstanceId = facade.start(process, stored.getProcessInstanceId(), variables);

                instanceBuilder.processInstanceId(stored.getProcessInstanceId());
                instanceBuilder.engineProcessInstanceId(engineInstanceId);

            } catch (ProcessEngineException e) {
                LOG.error("Process engine unable to start instance ", e);
                throw new InternalServerError();
            }
        }

        ProcessInstance instance = instanceBuilder.build();

        List<Attachment> attachments = instance.getAttachments();
        if (attachments != null && !attachments.isEmpty()) {
            for (Attachment attachment : attachments) {
                attachmentRepository.save(attachment);
            }
        }

        return processInstanceRepository.save(instance);
    }

    public ProcessInstance attach(Process process, Screen screen, Payload payload) throws StatusCodeError {

        // Validate the submission
        FormValidation validation = validate(process, screen, payload, false);
        FormSubmission submission = validation.getSubmission();
        ProcessInstance previous = validation.getInstance();

        return store(process, submission, validation, previous, true);
    }

    public ProcessInstance submit(Process process, Screen screen, Payload payload) throws StatusCodeError {

        // Validate the submission
        FormValidation validation = validate(process, screen, payload, true);
        FormSubmission submission = validation.getSubmission();
        ProcessInstance previous = validation.getInstance();

        completeIfTaskExists(process, payload, validation);

        return store(process, submission, validation, previous, false);
    }

    public FormValidation validate(Process process, Screen screen, Payload payload, boolean throwException) throws StatusCodeError {

        checkIsActiveIfTaskExists(process, payload);

        boolean isAttachmentAllowed = screen == null || screen.isAttachmentAllowed();

        // Create a new submission to store the data submitted
        FormSubmission submission = submissionHandler.handle(payload, isAttachmentAllowed);

        // If an instance already exists then get it from the processRepository
        ProcessInstance previous = null;
        if (StringUtils.isNotBlank(payload.getProcessInstanceId()))
            previous = processInstanceRepository.findOne(payload.getProcessInstanceId());

        // Validate the submission
        FormValidation validation = validationService.validate(submission, previous, screen, payload.getValidationId());

        List<ValidationResult> results = validation.getResults();
        if (throwException && results != null && !results.isEmpty()) {
            // Throw an exception if the submitter needs to adjust the data
            throw new BadRequestError(validation);
        }

        return validation;
    }

    public ViewContext getInstanceViewContext() {
        String baseApplicationUri = environment.getProperty("base.application.uri");
        String baseServiceUri = environment.getProperty("base.service.uri");
        return new ViewContext(baseApplicationUri, baseServiceUri, getVersion(), "instance", "Instance");
    }

    public String getVersion() {
        return "v1";
    }

    private void checkIsActiveIfTaskExists(Process process, Payload payload) throws StatusCodeError {
        String taskId = payload.getTaskId();
        if (StringUtils.isNotEmpty(taskId)) {
            try {
                InternalUserDetails user = helper.getAuthenticatedPrincipal();
                Task task = facade.findTask(new TaskCriteria.Builder().process(process).participantId(user.getInternalId()).taskId(taskId).build());
                if (task == null || !task.isActive())
                    throw new ForbiddenError();

            } catch (ProcessEngineException e) {
                throw new InternalServerError();
            }
        }
    }

    private void completeIfTaskExists(Process process, Payload payload, FormValidation validation) throws StatusCodeError {
        String taskId = payload.getTaskId();
        if (StringUtils.isNotEmpty(taskId)) {
            try {
                String actionValue = null;
                if (validation.getFormValueMap() != null) {
                    List<String> actionValues = validation.getFormValueMap().get("actionButton");
                    actionValue = actionValues != null && !actionValues.isEmpty() ? actionValues.get(0) : null;
                }

                facade.completeTask(process, taskId, actionValue);
            } catch (ProcessEngineException e) {
                throw new InternalServerError();
            }
        }
    }



//    private ProcessInstance getProcessInstance(Process process, String processInstanceId) throws NotFoundError {
//        ProcessInstance instance = processInstanceRepository.findOne(processInstanceId);
//
//        if (instance == null)
//            throw new NotFoundError();
//
//        return instance;
//    }
}
