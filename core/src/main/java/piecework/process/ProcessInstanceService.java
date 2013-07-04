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
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.authorization.AuthorizationRole;
import piecework.common.view.SearchResults;
import piecework.common.view.ViewContext;
import piecework.engine.ProcessEngineRuntimeFacade;
import piecework.engine.exception.ProcessEngineException;
import piecework.exception.*;
import piecework.form.handler.SubmissionHandler;
import piecework.form.validation.FormValidation;
import piecework.form.validation.ValidationService;
import piecework.identity.InternalUserDetails;
import piecework.model.*;
import piecework.model.Process;
import piecework.process.concrete.ResourceHelper;
import piecework.security.Sanitizer;
import piecework.security.concrete.PassthroughSanitizer;

import javax.ws.rs.core.MultivaluedMap;
import java.io.StringReader;
import java.io.StringWriter;
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
    ProcessResource processResource;

    @Autowired
    ProcessInstanceResource processInstanceResource;

    @Autowired
    ProcessEngineRuntimeFacade facade;

    @Autowired
    ResourceHelper helper;

    @Autowired
    MongoOperations mongoOperations;

    @Autowired
    ProcessInstanceRepository processInstanceRepository;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    SubmissionHandler submissionHandler;

    @Autowired
    ValidationService validationService;

    public ProcessInstance findOne(Process process, String processInstanceId) throws NotFoundError {
        ProcessInstance instance = processInstanceRepository.findOne(processInstanceId);

        if (instance == null || !instance.getProcessDefinitionKey().equals(process.getProcessDefinitionKey()))
            throw new NotFoundError(Constants.ExceptionCodes.instance_does_not_exist);

        return instance;
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

                resultsBuilder.definition(new Process.Builder(allowedProcess, new PassthroughSanitizer()).interactions(null).build(processResource.getViewContext()));
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

            for (Map.Entry<String, List<String>> entry : validation.getFormValueMap().entrySet()) {
                List<String> values = entry.getValue();
                if (values != null && !values.isEmpty())
                    scopes.put(entry.getKey(), values.iterator().next());
            }

            StringWriter writer = new StringWriter();
            MustacheFactory mf = new DefaultMustacheFactory();
            Mustache mustache = mf.compile(new StringReader(processInstanceLabel), "processInstanceLabel");
            mustache.execute(writer, scopes);

            processInstanceLabel = writer.toString();
        }

        if (previous != null) {
            instanceBuilder = new ProcessInstance.Builder(previous, new PassthroughSanitizer())
                    .submission(submission)
                    .attachments(validation.getAttachments());

            if (!isAttachment) {
                instanceBuilder.formValueMap(validation.getFormValueMap())
                        .processInstanceLabel(processInstanceLabel)
                        .restrictedValueMap(validation.getRestrictedValueMap());
            }
        } else {
            if (isAttachment)
                throw new ForbiddenError();

            try {
                String processInstanceId = UUID.randomUUID().toString();

                Map<String, String> variables = new HashMap<String, String>();
                variables.put("PIECEWORK_PROCESS_DEFINITION_KEY", process.getProcessDefinitionKey());
                variables.put("PIECEWORK_PROCESS_INSTANCE_ID", processInstanceId);
                variables.put("PIECEWORK_PROCESS_INSTANCE_LABEL", processInstanceLabel);

                InternalUserDetails user = helper.getAuthenticatedPrincipal();
                String initiatorId = user != null ? user.getInternalId() : null;
                String initiationStatus = process.getInitiationStatus();
                instanceBuilder = new ProcessInstance.Builder()
                        .processDefinitionKey(process.getProcessDefinitionKey())
                        .processDefinitionLabel(process.getProcessDefinitionLabel())
                        .processInstanceId(processInstanceId)
                        .processInstanceLabel(processInstanceLabel)
                        .formValueMap(validation.getFormValueMap())
                        .restrictedValueMap(validation.getRestrictedValueMap())
                        .submission(submission)
                        .startTime(new Date())
                        .initiatorId(initiatorId)
                        .processStatus(Constants.ProcessStatuses.OPEN)
                        .attachments(validation.getAttachments())
                        .applicationStatus(initiationStatus);

                // Save it before routing, then save again with the engine instance id
                ProcessInstance stored = processInstanceRepository.save(instanceBuilder.build());

                String engineInstanceId = facade.start(process, stored.getProcessInstanceId(), variables);

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

    public ProcessInstance attach(Process process, Screen screen, ProcessInstancePayload payload) throws StatusCodeError {

        // Validate the submission
        FormValidation validation = validate(process, screen, payload, false);
        FormSubmission submission = validation.getSubmission();
        ProcessInstance previous = validation.getInstance();

        return store(process, submission, validation, previous, true);
    }

    public ProcessInstance submit(Process process, Screen screen, ProcessInstancePayload payload) throws StatusCodeError {

        // Validate the submission
        FormValidation validation = validate(process, screen, payload, true);
        FormSubmission submission = validation.getSubmission();
        ProcessInstance previous = validation.getInstance();

        return store(process, submission, validation, previous, false);
    }

    public FormValidation validate(Process process, Screen screen, ProcessInstancePayload payload, boolean throwException) throws StatusCodeError {

        boolean isAttachmentAllowed = screen == null || screen.isAttachmentAllowed();

        // Create a new submission to store the data submitted
        FormSubmission submission = submissionHandler.handle(payload, isAttachmentAllowed);

        // If an instance already exists then get it from the repository
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

}
