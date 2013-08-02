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

import com.mongodb.DBRef;
import com.mongodb.WriteResult;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.authorization.AuthorizationRole;
import piecework.engine.ProcessEngineFacade;
import piecework.enumeration.ActionType;
import piecework.enumeration.OperationType;
import piecework.form.validation.SubmissionTemplate;
import piecework.identity.InternalUserDetailsService;
import piecework.model.SearchResults;
import piecework.common.ViewContext;
import piecework.engine.exception.ProcessEngineException;
import piecework.exception.*;
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
import piecework.task.TaskResults;
import piecework.ui.StreamingAttachmentContent;
import piecework.util.ManyMap;
import piecework.util.ProcessInstanceUtility;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.*;

import static org.springframework.data.mongodb.core.query.Criteria.where;

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
    ProcessEngineFacade facade;

    @Autowired
    ResourceHelper helper;

    @Autowired
    MongoTemplate mongoOperations;

    @Autowired
    ProcessRepository processRepository;

    @Autowired
    ProcessInstanceRepository processInstanceRepository;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    InternalUserDetailsService userDetailsService;

    @Autowired
    ValidationService validationService;

    public ProcessInstance attach(Process process, ProcessInstance instance, Task task, SubmissionTemplate template, Submission submission) throws StatusCodeError {
        FormValidation validation = validate(process, instance, task, template, submission, false);
        return store(process, submission, validation, instance, true);
    }

    public SearchResults findAttachments(Process process, ProcessInstance processInstance, AttachmentQueryParameters queryParameters) throws StatusCodeError {
        String paramName = sanitizer.sanitize(queryParameters.getName());
        String paramContentType = sanitizer.sanitize(queryParameters.getContentType());
        String paramUserId = sanitizer.sanitize(queryParameters.getUserId());

        SearchResults.Builder searchResultsBuilder = new SearchResults.Builder();

        int count = 0;
        Map<String, User> userMap = new HashMap<String, User>();
        List<Attachment> storedAttachments = processInstance.getAttachments();
        if (storedAttachments != null && !storedAttachments.isEmpty()) {
            PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
            for (Attachment storedAttachment : storedAttachments) {
                if (storedAttachment.isFieldAttachment())
                    continue;

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
                            .processInstanceId(processInstance.getProcessInstanceId())
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

    public void operate(OperationType operationType, Process process, ProcessInstance instance, String applicationStatus, String reason) throws ConflictError, InternalServerError {
        try {
            ProcessInstance.Builder modified = new ProcessInstance.Builder(instance);
            String processStatus = instance.getProcessStatus();

            if (operationType != OperationType.UPDATE) {
                String defaultApplicationStatus;
                switch(operationType) {
                    case ACTIVATION:
                        if (!facade.activate(process, instance))
                            throw new ConflictError(Constants.ExceptionCodes.invalid_process_status);
                        defaultApplicationStatus = instance.getPreviousApplicationStatus();
                        processStatus = Constants.ProcessStatuses.OPEN;
                        break;
                    case CANCELLATION:
                        if (!facade.cancel(process, instance))
                            throw new ConflictError(Constants.ExceptionCodes.invalid_process_status);
                        defaultApplicationStatus = process.getCancellationStatus();
                        processStatus = Constants.ProcessStatuses.CANCELLED;
                        break;
                    case SUSPENSION:
                        if (!facade.suspend(process, instance))
                            throw new ConflictError(Constants.ExceptionCodes.invalid_process_status);
                        defaultApplicationStatus = process.getSuspensionStatus();
                        modified.previousApplicationStatus(instance.getApplicationStatus());
                        processStatus = Constants.ProcessStatuses.SUSPENDED;
                        break;
                    default:
                        throw new ConflictError(Constants.ExceptionCodes.invalid_process_status);
                }

                if (StringUtils.isNotEmpty(applicationStatus))
                    applicationStatus = defaultApplicationStatus;
            }

            String userId = helper.getAuthenticatedSystemOrUserId();

            boolean skipOptimization = environment.getProperty(Constants.Settings.OPTIMIZATIONS_OFF, Boolean.class, Boolean.FALSE);

            if (skipOptimization) {
                modified.applicationStatus(applicationStatus)
                        .applicationStatusExplanation(reason)
                        .processStatus(processStatus)
                        .operation(operationType, reason, new Date(), userId);

                processInstanceRepository.save(modified.build());
            } else {
                WriteResult result = mongoOperations.updateFirst(new Query(where("_id").is(instance.getProcessInstanceId())),
                        new Update()
                                .set("applicationStatus", applicationStatus)
                                .set("applicationStatusExplanation", reason)
                                .set("processStatus", processStatus)
                                .push("operations", new Operation(operationType, reason, new Date(), userId)),
                        ProcessInstance.class);

                String error = result.getError();
                if (StringUtils.isNotEmpty(error))
                    LOG.error("Unable to correctly save applicationStatus " + applicationStatus + ", processStatus " + processStatus + ", and reason " + reason + " for " + instance.getProcessInstanceId() + ": " + error);
            }

        } catch (ProcessEngineException e) {
            LOG.error("Process engine unable to cancel execution ", e);
            throw new InternalServerError();
        }
    }

    public ProcessInstance read(String processDefinitionKey, String processInstanceId) throws StatusCodeError {
        Process process = getProcess(processDefinitionKey);
        return read(process, processInstanceId);
    }

    public ProcessInstance read(Process process, String rawProcessInstanceId) throws StatusCodeError {
        String processInstanceId = sanitizer.sanitize(rawProcessInstanceId);
        ProcessInstance instance = processInstanceRepository.findOne(processInstanceId);

        if (instance == null || !instance.getProcessDefinitionKey().equals(process.getProcessDefinitionKey()))
            throw new NotFoundError(Constants.ExceptionCodes.instance_does_not_exist);

        if (instance.isDeleted())
            throw new GoneError(Constants.ExceptionCodes.instance_does_not_exist);

        return instance;
    }

    public Response readValue(Process process, ProcessInstance instance, String fieldName, String fileId) throws StatusCodeError {
        Map<String, List<Value>> data = instance.getData();
        List<? extends Value> values = fieldName != null ? data.get(fieldName) : null;

        if (values == null || values.isEmpty() || StringUtils.isEmpty(fileId))
            throw new NotFoundError();

        for (Value value : values) {
            if (value == null)
                continue;

            if (value instanceof File) {
                File file = File.class.cast(value);

                if (StringUtils.isEmpty(file.getId()))
                    continue;

                if (file.getId().equals(fileId)) {
                    Content content = contentRepository.findByLocation(file.getLocation());
                    if (content != null) {
                        StreamingAttachmentContent streamingAttachmentContent = new StreamingAttachmentContent(null, content);
                        String contentDisposition = new StringBuilder("attachment; filename=").append(content.getFilename()).toString();
                        return Response.ok(streamingAttachmentContent, streamingAttachmentContent.getContent().getContentType()).header("Content-Disposition", contentDisposition).build();
                    }
                }
            }
        }

        throw new NotFoundError();
    }

    public void removeAttachment(Process process, ProcessInstance instance, String attachmentId) throws StatusCodeError {
        if (instance == null)
            throw new InternalServerError();

        boolean skipOptimization = environment.getProperty(Constants.Settings.OPTIMIZATIONS_OFF, Boolean.class, Boolean.FALSE);

        if (skipOptimization) {
            PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
            ProcessInstance.Builder builder = new ProcessInstance.Builder(instance);
            builder.removeAttachment(attachmentId);

            processInstanceRepository.save(builder.build());
        } else {
            Query query = new Query(where("_id").is(instance.getProcessInstanceId()));
            Update update = new Update();

            if (attachmentId != null)
                update.pull("attachments", attachmentId);

            mongoOperations.updateFirst(query, update, ProcessInstance.class);
        }
    }

    public void removeValue(Process process, ProcessInstance instance, String fieldName, String fileId) throws StatusCodeError {
        Map<String, List<Value>> data = instance.getData();
        List<? extends Value> values = fieldName != null ? data.get(fieldName) : null;

        if (values == null || values.isEmpty() || StringUtils.isEmpty(fileId))
            throw new NotFoundError();

        List<Value> remainingValues = new ArrayList<Value>();
        for (Value value : values) {
            if (value == null)
                continue;

            if (value instanceof File) {
                File file = File.class.cast(value);

                if (StringUtils.isEmpty(file.getId()))
                    continue;

                if (!file.getId().equals(fileId))
                    remainingValues.add(value);
            }
        }

        ManyMap<String, Value> update = new ManyMap<String, Value>();
        update.put(fieldName, remainingValues);

        updateProcessInstance(update, null, null, null, instance);
    }

    public List<File> searchValues(Process process, ProcessInstance instance, String fieldName) throws StatusCodeError {
        Map<String, List<Value>> data = instance.getData();
        List<? extends Value> values = fieldName != null ? data.get(fieldName) : null;

        List<File> files = new ArrayList<File>();

        if (values != null && !values.isEmpty()) {
            for (Value value : values) {
                if (value == null)
                    continue;

                if (value instanceof File) {
                    File file = File.class.cast(value);
                    files.add(new File.Builder().processDefinitionKey(process.getProcessDefinitionKey()).processInstanceId(instance.getProcessInstanceId()).fieldName(fieldName).name(file.getName()).id(file.getId()).contentType(file.getContentType()).build(getInstanceViewContext()));
                }
            }
        }

        return files;
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

    public History getHistory(String rawProcessDefinitionKey, String rawProcessInstanceId) throws StatusCodeError {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = read(process, rawProcessInstanceId);

        List<Operation> operations = instance.getOperations();
        List<Task> tasks = findAllTasks(process, instance);

        InternalUserDetails initiatorUserDetails = userDetailsService.loadUserByAnyId(instance.getInitiatorId());

        User initiator = initiatorUserDetails != null ?  new User.Builder(initiatorUserDetails).build() : null;

        History.Builder history = new History.Builder()
            .processDefinitionKey(process.getProcessDefinitionKey())
            .processDefinitionLabel(process.getProcessDefinitionLabel())
            .processInstanceId(instance.getProcessInstanceId())
            .processInstanceLabel(instance.getProcessInstanceLabel())
            .startTime(instance.getStartTime())
            .endTime(instance.getEndTime())
            .initiator(initiator);

        if (operations != null) {
            for (Operation operation : operations) {
                String userId = operation.getUserId();
                User user = userDetailsService.getUser(userId);
                history.operation(operation, user);
            }
        }

        if (tasks != null) {
            for (Task task : tasks) {
                history.task(task);
            }
        }

        return history.build(getInstanceViewContext());
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
        ProcessInstance persisted = read(process, processInstanceId);
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

            operate(operationType, process, persisted, applicationStatus, applicationStatusExplanation);
        } else {
            throw new BadRequestError(Constants.ExceptionCodes.instance_cannot_be_modified);
        }
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

                resultsBuilder.definition(new Process.Builder(allowedProcess, new PassthroughSanitizer(), false)
                        .interactions(null).build(processService.getProcessViewContext()));
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
                        resultsBuilder.item(new ProcessInstance.Builder(processInstance).build(getInstanceViewContext()));
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

    public ProcessInstance store(Process process, Submission submission, FormValidation validation, ProcessInstance previous, boolean isAttachment) throws StatusCodeError {
        long time = 0;
        if (LOG.isDebugEnabled())
            time = System.currentTimeMillis();

        String processInstanceLabel = ProcessInstanceUtility.processInstanceLabel(process, previous, validation, submission.getProcessInstanceLabel());
        List<Attachment> attachments = validation.getAttachments();
        if (attachments != null && !attachments.isEmpty())
            attachments = attachmentRepository.save(attachments);

        Map<String, List<Value>> data = isAttachment ? null : validation.getData();

        ProcessInstance instance;
        if (previous != null)
            instance = updateProcessInstance(data, attachments, submission, processInstanceLabel, previous);
        else
            instance = startProcessInstance(data, attachments, submission, processInstanceLabel, process);

        if (LOG.isDebugEnabled())
            LOG.debug("Storage took " + (System.currentTimeMillis() - time) + " ms");

        return instance;
    }

    public ProcessInstance reject(Process process, ProcessInstance instance, Task task, SubmissionTemplate template, Submission submission) throws StatusCodeError {
        FormValidation validation = validate(process, instance, task, template, submission, false);
        completeIfTaskExists(process, task, submission.getAction());
        return store(process, submission, validation, instance, false);
    }

    public ProcessInstance save(Process process, ProcessInstance instance, Task task, SubmissionTemplate template, Submission submission) throws StatusCodeError {
        FormValidation validation = validate(process, instance, task, template, submission, false);
        return store(process, submission, validation, instance, false);
    }

    public ProcessInstance submit(Process process, ProcessInstance instance, Task task, SubmissionTemplate template, Submission submission) throws StatusCodeError {
        FormValidation validation = validate(process, instance, task, template, submission, true);
        completeIfTaskExists(process, task, submission.getAction());
        return store(process, submission, validation, instance, false);
    }

    public List<Task> findAllTasks(Process process, ProcessInstance instance) throws StatusCodeError {
        TaskCriteria taskCriteria = new TaskCriteria.Builder()
                .process(process)
                .executionId(instance.getEngineProcessInstanceId())
                .processStatus(Constants.ProcessStatuses.ALL)
                .orderBy(TaskCriteria.OrderBy.CREATED_TIME_ASC)
                .build();
        try {
            TaskResults taskResults = facade.findTasks(taskCriteria);

            List<Task> tasks = taskResults.getTasks();
            List<Task> convertedTasks;

            PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
            if (tasks != null && !tasks.isEmpty()) {
                convertedTasks = new ArrayList<Task>(tasks.size());
                for (Task task : tasks) {
                    Task.Builder builder = new Task.Builder(task, passthroughSanitizer);

                    if (task.getAssignee() != null && StringUtils.isNotEmpty(task.getAssignee().getUserId())) {
                        builder.assignee(userDetailsService.getUser(task.getAssignee().getUserId()));
                    }

                    if (task.getCandidateAssignees() != null && !task.getCandidateAssignees().isEmpty()) {
                        builder.clearCandidateAssignees();
                        for (User candidateAssignee : task.getCandidateAssignees()) {
                            builder.candidateAssignee(userDetailsService.getUser(candidateAssignee.getUserId()));
                        }
                    }

                    convertedTasks.add(builder.build(getTaskViewContext()));
                }

            } else {
                convertedTasks = Collections.emptyList();
            }

            return convertedTasks;

        } catch (ProcessEngineException e) {
            LOG.error(e);
            throw new InternalServerError();
        }
    }

    public Task userTask(Process process, String taskId) throws StatusCodeError {
        TaskCriteria.Builder taskCriteria = new TaskCriteria.Builder()
                .process(process)
                .taskId(taskId)
                .orderBy(TaskCriteria.OrderBy.CREATED_TIME_ASC);

        if (! helper.isAuthenticatedSystem()) {
            // If the call is not being made by an authenticated system, then the principal is a user and must have an active task
            // on this instance
            InternalUserDetails user = helper.getAuthenticatedPrincipal();

            if (user == null)
                throw new ForbiddenError();

            taskCriteria.participantId(user.getInternalId());
        }

        try {
            return facade.findTask(taskCriteria.build());
        } catch (ProcessEngineException e) {
            LOG.error(e);
            throw new InternalServerError();
        }
    }

    public Task userTask(Process process, ProcessInstance processInstance, boolean limitToActive) throws StatusCodeError {
        TaskCriteria.Builder taskCriteria = new TaskCriteria.Builder()
                .process(process)
                .executionId(processInstance.getEngineProcessInstanceId())
                .orderBy(TaskCriteria.OrderBy.CREATED_TIME_ASC);

        if (! helper.isAuthenticatedSystem()) {
            // If the call is not being made by an authenticated system, then the principal is a user and must have an active task
            // on this instance
            InternalUserDetails user = helper.getAuthenticatedPrincipal();

            if (user == null)
                throw new ForbiddenError();

            taskCriteria.participantId(user.getInternalId());

            if (limitToActive)
                taskCriteria.active(Boolean.TRUE);
        }

        try {
            TaskResults taskResults = facade.findTasks(taskCriteria.build());

            if (taskResults != null && taskResults.getTasks() != null && !taskResults.getTasks().isEmpty())
                return taskResults.getTasks().iterator().next();

            return null;
        } catch (ProcessEngineException e) {
            LOG.error(e);
            throw new InternalServerError();
        }
    }

    public boolean userHasTask(Process process, ProcessInstance processInstance, boolean limitToActive) throws StatusCodeError {

        if (userTask(process, processInstance, limitToActive) != null)
            return true;

        return false;
    }

    public FormValidation validate(Process process, ProcessInstance instance, Task task, SubmissionTemplate template, Submission submission, boolean throwException) throws StatusCodeError {
        long time = 0;

        if (LOG.isDebugEnabled())
            time = System.currentTimeMillis();

        checkIsActiveIfTaskExists(process, task);

        // Validate the submission
        FormValidation validation = validationService.validate(instance, template, submission, throwException);

        if (LOG.isDebugEnabled())
            LOG.debug("Validation took " + (System.currentTimeMillis() - time) + " ms");

        Map<String, List<Message>> results = validation.getResults();
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

    public ViewContext getTaskViewContext() {
        String baseApplicationUri = environment.getProperty("base.application.uri");
        String baseServiceUri = environment.getProperty("base.service.uri");
        return new ViewContext(baseApplicationUri, baseServiceUri, getVersion(), Task.Constants.ROOT_ELEMENT_NAME, Task.Constants.RESOURCE_LABEL);
    }

    public String getVersion() {
        return "v1";
    }

    private void checkIsActiveIfTaskExists(Process process, Task task) throws StatusCodeError {
        String taskId = task != null ? task.getTaskInstanceId() : null;
        if (StringUtils.isNotEmpty(taskId)) {
            try {
                InternalUserDetails user = helper.getAuthenticatedPrincipal();
                task = facade.findTask(new TaskCriteria.Builder().process(process).participantId(user.getInternalId()).taskId(taskId).build());
                if (task == null || !task.isActive())
                    throw new ForbiddenError();

            } catch (ProcessEngineException e) {
                throw new InternalServerError();
            }
        }
    }

    private void completeIfTaskExists(Process process, Task task, ActionType action) throws StatusCodeError {
        String taskId = task != null ? task.getTaskInstanceId() : null;
        if (StringUtils.isNotEmpty(taskId)) {
            try {
                facade.completeTask(process, taskId, action);
            } catch (ProcessEngineException e) {
                LOG.error(e);
                throw new InternalServerError(e);
            }
        }
    }

    private ProcessInstance startProcessInstance(Map<String, List<Value>> data, List<Attachment> attachments, Submission submission, String processInstanceLabel, Process process) throws StatusCodeError {
        ProcessInstance.Builder builder;
        try {
            InternalUserDetails user = helper.getAuthenticatedPrincipal();
            String initiatorId = user != null ? user.getInternalId() : null;
            String initiationStatus = process.getInitiationStatus();
            builder = new ProcessInstance.Builder()
                    .processDefinitionKey(process.getProcessDefinitionKey())
                    .processDefinitionLabel(process.getProcessDefinitionLabel())
                    .processInstanceLabel(processInstanceLabel)
                    .data(data)
                    .submission(submission)
                    .startTime(new Date())
                    .initiatorId(initiatorId)
                    .processStatus(Constants.ProcessStatuses.OPEN)
                    .attachments(attachments)
                    .applicationStatus(initiationStatus);

            // Save it before routing, then save again with the engine instance id
            ProcessInstance stored = processInstanceRepository.save(builder.build());

            Map<String, String> variables = new HashMap<String, String>();
            variables.put("PIECEWORK_PROCESS_DEFINITION_KEY", process.getProcessDefinitionKey());
            variables.put("PIECEWORK_PROCESS_INSTANCE_ID", stored.getProcessInstanceId());
            variables.put("PIECEWORK_PROCESS_INSTANCE_LABEL", processInstanceLabel);

            String engineInstanceId = facade.start(process, stored.getProcessInstanceId(), variables);

            builder.processInstanceId(stored.getProcessInstanceId());
            builder.engineProcessInstanceId(engineInstanceId);

            WriteResult result = mongoOperations.updateFirst(new Query(where("_id").is(stored.getProcessInstanceId())),
                    new Update().set("engineProcessInstanceId", engineInstanceId),
                    ProcessInstance.class);

            String error = result.getError();
            if (StringUtils.isNotEmpty(error))
                LOG.error("Unable to correctly save engine instance id " + engineInstanceId + " for " + stored.getProcessInstanceId() + ": " + error);

        } catch (ProcessEngineException e) {
            LOG.error("Process engine unable to start instance ", e);
            throw new InternalServerError();
        }

        return builder.build();
    }

    private ProcessInstance updateProcessInstance(Map<String, List<Value>> data, List<Attachment> attachments, Submission submission, String processInstanceLabel, ProcessInstance instance) throws StatusCodeError {
        if (instance == null)
            throw new InternalServerError();

        boolean skipOptimization = environment.getProperty(Constants.Settings.OPTIMIZATIONS_OFF, Boolean.class, Boolean.FALSE);

        if (skipOptimization) {
            ProcessInstance.Builder builder = new ProcessInstance.Builder(instance)
                    .attachments(attachments)
                    .data(data)
                    .submission(submission);

            if (StringUtils.isNotEmpty(processInstanceLabel))
                builder.processInstanceLabel(processInstanceLabel);

            return processInstanceRepository.save(builder.build());
        } else {
            Query query = new Query(where("_id").is(instance.getProcessInstanceId()));
            Update update = new Update();

            if (attachments != null && !attachments.isEmpty()) {
                DBRef[] attachmentRefs = new DBRef[attachments.size()];
                for (int i=0;i<attachments.size();i++) {
                    attachmentRefs[i] = new DBRef(mongoOperations.getDb(), mongoOperations.getCollectionName(Attachment.class), new ObjectId(attachments.get(i).getAttachmentId()));
                }
                update.pushAll("attachments", attachmentRefs);
            }
            if (submission != null)
                update.push("submissions", new DBRef(mongoOperations.getDb(), mongoOperations.getCollectionName(Submission.class), new ObjectId(submission.getSubmissionId())));
            if (StringUtils.isNotEmpty(processInstanceLabel))
                update.set("processInstanceLabel", processInstanceLabel);
            if (data != null && !data.isEmpty()) {
                for (Map.Entry<String, List<Value>> entry : data.entrySet()) {
                    String key = "data." + entry.getKey();
                    List<Value> values = entry.getValue();

                    List<File> files = null;
                    List<User> users = null;
                    if (values != null) {
                        for (Value value : values) {
                            if (value instanceof File) {
                                File file = File.class.cast(value);

                                if (StringUtils.isNotEmpty(file.getName())) {
                                    update.addToSet("keywords", file.getName().toLowerCase());
                                    if (files == null)
                                        files = new ArrayList<File>(values.size());
                                    files.add(file);
                                }
                            } else if (value instanceof User) {
                                User user = User.class.cast(value);
                                if (user != null) {
                                    if (user.getDisplayName() != null)
                                        update.addToSet("keywords", user.getDisplayName());
                                    if (user.getVisibleId() != null)
                                        update.addToSet("keywords", user.getVisibleId());
                                    if (user.getUserId() != null)
                                        update.addToSet("keywords", user.getUserId());
                                    if (user.getEmailAddress() != null)
                                        update.addToSet("keywords", user.getEmailAddress());

                                    if (users == null)
                                        users = new ArrayList<User>(values.size());
                                    users.add(user);
                                }
                            } else if (! (value instanceof Secret)) {
                                if (StringUtils.isNotEmpty(value.getValue()))
                                    update.addToSet("keywords", value.getValue().toLowerCase());
                            }
                        }
                    }
                    if (files != null)
                        update.set(key, files);
                    else if (users != null)
                        update.set(key, users);
                    else
                        update.set(key, values);
                }
            }

            return mongoOperations.findAndModify(query, update, ProcessInstance.class);
        }
    }
}
