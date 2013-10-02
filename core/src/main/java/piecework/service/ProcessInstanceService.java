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
package piecework.service;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.CommandExecutor;
import piecework.Versions;
import piecework.authorization.AuthorizationRole;
import piecework.command.*;
import piecework.enumeration.OperationType;
import piecework.process.ProcessInstanceSearchCriteria;
import piecework.validation.SubmissionTemplate;
import piecework.model.SearchResults;
import piecework.common.ViewContext;
import piecework.exception.*;
import piecework.validation.FormValidation;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.AttachmentRepository;
import piecework.persistence.ProcessInstanceRepository;
import piecework.identity.IdentityHelper;
import piecework.security.Sanitizer;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.util.ProcessInstanceUtility;

import javax.ws.rs.core.MultivaluedMap;
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
    ProcessService processService;

    @Autowired
    IdentityHelper helper;

    @Autowired
    ProcessInstanceRepository processInstanceRepository;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    TaskService taskService;

    @Autowired
    CommandExecutor commandExecutor;

    @Autowired
    ValidationService validationService;

    @Autowired
    Versions versions;

    public void activate(Process process, ProcessInstance instance, String reason) throws StatusCodeError {
        InstanceStateCommand activation = new InstanceStateCommand(process, instance, OperationType.ACTIVATION);
        activation.reason(reason);
        commandExecutor.execute(activation);
    }

    public void assign(Process process, ProcessInstance instance, Task task, String assignee) throws StatusCodeError {
        AssignmentCommand assignment = new AssignmentCommand(process, instance, task, assignee);
        commandExecutor.execute(assignment);
    }

    public void cancel(Process process, ProcessInstance instance, String reason) throws StatusCodeError {
        InstanceStateCommand cancellation = new InstanceStateCommand(process, instance, OperationType.CANCELLATION);
        cancellation.reason(reason);
        commandExecutor.execute(cancellation);
    }

    public ProcessInstance read(String processDefinitionKey, String processInstanceId, boolean full) throws StatusCodeError {
        Process process = processService.read(processDefinitionKey);
        return read(process, processInstanceId, full);
    }

    public ProcessInstance read(Process process, String rawProcessInstanceId, boolean full) throws StatusCodeError {
        String processInstanceId = sanitizer.sanitize(rawProcessInstanceId);
        long start = 0;
        if (LOG.isDebugEnabled()) {
            start = System.currentTimeMillis();
            LOG.debug("Retrieving instance for " + processInstanceId);
        }

        ProcessInstance instance = processInstanceRepository.findOne(processInstanceId);

        if (instance == null || !instance.getProcessDefinitionKey().equals(process.getProcessDefinitionKey()))
            throw new NotFoundError(Constants.ExceptionCodes.instance_does_not_exist);

        if (instance.isDeleted())
            throw new GoneError(Constants.ExceptionCodes.instance_does_not_exist);


        if (full) {
            ProcessInstance.Builder builder = new ProcessInstance.Builder(instance);
            ViewContext viewContext = versions.getVersion1();

            Set<String> attachmentIds = instance.getAttachmentIds();
            if (attachmentIds != null && !attachmentIds.isEmpty()) {
                if (LOG.isDebugEnabled())
                    LOG.debug("Retrieving all attachments for instance " + processInstanceId);
                Iterable<Attachment> attachments = attachmentRepository.findAll(attachmentIds);

                for (Attachment attachment : attachments) {
                    builder.attachment(new Attachment.Builder(attachment).build(viewContext));
                }
            }

            if (LOG.isDebugEnabled())
                LOG.debug("Retrieved instance and attachments for " + processInstanceId + " in " + (System.currentTimeMillis() - start) + " ms");

            return builder.build(viewContext);
        }

        if (LOG.isDebugEnabled())
            LOG.debug("Retrieved instance for " + processInstanceId + " in " + (System.currentTimeMillis() - start) + " ms");

        return instance;
    }

    public void suspend(Process process, ProcessInstance instance, String reason) throws StatusCodeError {
        InstanceStateCommand suspension = new InstanceStateCommand(process, instance, OperationType.SUSPENSION);
        suspension.reason(reason);
        commandExecutor.execute(suspension);
    }

    public void update(String processDefinitionKey, String processInstanceId, ProcessInstance processInstance) throws StatusCodeError {
        Process process = processService.read(processDefinitionKey);
        ProcessInstance persisted = read(process, processInstanceId, false);
        ProcessInstance sanitized = new ProcessInstance.Builder(processInstance).build();

        String processStatus = sanitized.getProcessStatus();
        String applicationStatus = sanitized.getApplicationStatus();
        String applicationStatusExplanation = sanitized.getApplicationStatusExplanation();

        if (StringUtils.isNotEmpty(processStatus) || StringUtils.isEmpty(applicationStatus)) {
            OperationType operationType = OperationType.UPDATE;
            if (processStatus != null && !processStatus.equalsIgnoreCase(persisted.getProcessStatus())) {
                if (processStatus.equals(Constants.ProcessStatuses.OPEN))
                    operationType = OperationType.ACTIVATION;
                else if (processStatus.equals(Constants.ProcessStatuses.CANCELLED))
                    operationType = OperationType.CANCELLATION;
                else if (processStatus.equals(Constants.ProcessStatuses.SUSPENDED))
                    operationType = OperationType.SUSPENSION;
            }
            InstanceStateCommand command = new InstanceStateCommand(process, persisted, operationType);
            command.applicationStatus(applicationStatus);
            command.reason(applicationStatusExplanation);
            commandExecutor.execute(command);
        } else {
            throw new BadRequestError(Constants.ExceptionCodes.instance_cannot_be_modified);
        }
    }

    public SearchResults search(MultivaluedMap<String, String> rawQueryParameters) throws StatusCodeError {
        ProcessInstanceSearchCriteria.Builder executionCriteriaBuilder =
                new ProcessInstanceSearchCriteria.Builder(rawQueryParameters, sanitizer);

        ViewContext version1 = versions.getVersion1();

        SearchResults.Builder resultsBuilder = new SearchResults.Builder()
                .resourceLabel("Workflows")
                .resourceName(ProcessInstance.Constants.ROOT_ELEMENT_NAME)
                .link(version1.getApplicationUri());

        Set<Process> allowedProcesses = helper.findProcesses(AuthorizationRole.OVERSEER);
        if (!allowedProcesses.isEmpty()) {
            for (Process allowedProcess : allowedProcesses) {
                String allowedProcessDefinitionKey = allowedProcess.getProcessDefinitionKey();
                ProcessDeployment deployment = allowedProcess.getDeployment();

                if (deployment != null) {
                    executionCriteriaBuilder.processDefinitionKey(allowedProcessDefinitionKey)
                        .engineProcessDefinitionKey(deployment.getEngineProcessDefinitionKey())
                        .engine(deployment.getEngine());

                    resultsBuilder.definition(new Process.Builder(allowedProcess, new PassthroughSanitizer(), false).build(version1));
                }
            }
            ProcessInstanceSearchCriteria executionCriteria = executionCriteriaBuilder.build();

            if (executionCriteria.getSanitizedParameters() != null) {
                for (Map.Entry<String, List<String>> entry : executionCriteria.getSanitizedParameters().entrySet()) {
                    resultsBuilder.parameter(entry.getKey(), entry.getValue());
                }
            }

            int firstResult = executionCriteria.getFirstResult() != null ? executionCriteria.getFirstResult() : 0;
            int maxResult = executionCriteria.getMaxResults() != null ? executionCriteria.getMaxResults() : 1000;

            Sort.Direction direction = Sort.Direction.DESC;
            String sortProperty = "startTime";
            switch (executionCriteria.getOrderBy()) {
            case START_TIME_ASC:
                direction = Sort.Direction.ASC;
                sortProperty = "startTime";
                break;
            case START_TIME_DESC:
                direction = Sort.Direction.DESC;
                sortProperty = "startTime";
                break;
            case END_TIME_ASC:
                direction = Sort.Direction.ASC;
                sortProperty = "endTime";
                break;
            case END_TIME_DESC:
                direction = Sort.Direction.DESC;
                sortProperty = "endTime";
                break;
            }

            Pageable pageable = new PageRequest(firstResult, maxResult, new Sort(direction, sortProperty));
            Page<ProcessInstance> page = processInstanceRepository.findByCriteria(executionCriteria, pageable);

            if (page.hasContent()) {
                for (ProcessInstance instance : page.getContent()) {
                    resultsBuilder.item(new ProcessInstance.Builder(instance).build(version1));
                }
            }

            resultsBuilder.page(page, pageable);

//            if (executionCriteria.getProcessInstanceIds().size() == 1) {
//                // If the user provided an actual instance id, then we can look it up directly and ignore the other parameters
//                String processInstanceId = executionCriteria.getProcessInstanceIds().iterator().next();
//                resultsBuilder.parameter("processInstanceId", processInstanceId);
//
//                if (StringUtils.isNotEmpty(processInstanceId)) {
//                    ProcessInstance single = processInstanceRepository.findOne(processInstanceId);
//
//                    // Verify that the user is allowed to see processes like this instance
//                    if (single != null && single.getProcessDefinitionKey() != null && allowedProcesses.contains(single.getProcessDefinitionKey())) {
//                        resultsBuilder.item(single);
//                        resultsBuilder.total(Long.valueOf(1));
//                        resultsBuilder.firstResult(1);
//                        resultsBuilder.maxResults(1);
//                    }
//                }
//            } else {
//
//                SearchResults interimResults = processInstanceRepository.findByCriteria(executionCriteria);
//
//                resultsBuilder.items(interimResults.getList());
//                resultsBuilder.firstResult(interimResults.getFirstResult());
//                resultsBuilder.maxResults(interimResults.getMaxResults());
//                resultsBuilder.total(interimResults.getTotal());
//                resultsBuilder.parameters(interimResults.getParameters());
//
////                // Otherwise, look up all instances that match the query
////                Query query = new ProcessInstanceQueryBuilder(executionCriteria).build();
////                // Don't include form data in the result
////                org.springframework.data.mongodb.core.query.Field field = query.fields();
////                field.exclude("data");
////
//////                query.fields().exclude("attachments");
////
////                List<ProcessInstance> processInstances = mongoOperations.find(query, ProcessInstance.class);
////                if (processInstances != null && !processInstances.isEmpty()) {
////                    for (ProcessInstance processInstance : processInstances) {
////                        resultsBuilder.item(new ProcessInstance.Builder(processInstance).build(version1));
////                    }
////
////                    int size = processInstances.size();
////                    if (executionCriteria.getMaxResults() != null || executionCriteria.getFirstResult() != null) {
////                        long total = mongoOperations.count(query, ProcessInstance.class);
////
////                        if (executionCriteria.getFirstResult() != null)
////                            resultsBuilder.firstResult(executionCriteria.getFirstResult());
////                        else
////                            resultsBuilder.firstResult(1);
////
////                        if (executionCriteria.getMaxResults() != null)
////                            resultsBuilder.maxResults(executionCriteria.getMaxResults());
////                        else
////                            resultsBuilder.maxResults(size);
////
////                        resultsBuilder.total(total);
////                    } else {
////                        resultsBuilder.firstResult(1);
////                        resultsBuilder.maxResults(size);
////                        resultsBuilder.total(Long.valueOf(size));
////                    }
////                }
//            }
        }
        return resultsBuilder.build();
    }

    public ProcessInstance reject(Process process, ProcessInstance instance, Task task, SubmissionTemplate template, Submission submission) throws StatusCodeError {
        FormValidation validation = validationService.validate(process, instance, task, template, submission, false);
        taskService.completeIfTaskExists(process, task, submission.getAction(), validation);
        return store(process, submission, validation, instance, false);
    }

    public ProcessInstance save(Process process, ProcessInstance instance, Task task, SubmissionTemplate template, Submission submission) throws StatusCodeError {
        FormValidation validation = validationService.validate(process, instance, task, template, submission, false);
        return store(process, submission, validation, instance, false);
    }

    public ProcessInstance submit(Process process, ProcessInstance instance, Task task, SubmissionTemplate template, Submission submission) throws StatusCodeError {
        FormValidation validation = validationService.validate(process, instance, task, template, submission, true);
        taskService.completeIfTaskExists(process, task, submission.getAction(), validation);
        return store(process, submission, validation, instance, false);
    }

    public ProcessInstance store(Process process, Submission submission, FormValidation validation, ProcessInstance previous, boolean isAttachment) throws StatusCodeError {
        long time = 0;
        if (LOG.isDebugEnabled())
            time = System.currentTimeMillis();

        List<Attachment> attachments = validation.getAttachments();
        if (attachments != null && !attachments.isEmpty()) {
            if (LOG.isDebugEnabled())
                LOG.debug("Persisting " + attachments.size() + " attachments");
            attachments = attachmentRepository.save(attachments);
        }

        Map<String, List<Value>> data = isAttachment ? null : validation.getData();

        InstanceCommand persist = previous == null ? new StartInstanceCommand(process) : new UpdateInstanceCommand(process, previous);

        persist.label(ProcessInstanceUtility.processInstanceLabel(process, previous, validation, submission.getProcessInstanceLabel()))
               .attachments(attachments)
               .data(data)
               .submission(submission);

        ProcessInstance instance = commandExecutor.execute(persist);

        if (LOG.isDebugEnabled())
            LOG.debug("Storage took " + (System.currentTimeMillis() - time) + " ms");

        return instance;
    }
}
