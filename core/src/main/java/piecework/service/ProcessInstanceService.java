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
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.authorization.AuthorizationRole;
import piecework.command.*;
import piecework.engine.ProcessDeploymentResource;
import piecework.engine.ProcessEngineFacade;
import piecework.engine.exception.ProcessEngineException;
import piecework.enumeration.ActionType;
import piecework.export.IteratingDataProvider;
import piecework.export.concrete.ExportAsCommaSeparatedValuesProvider;
import piecework.export.concrete.ExportAsExcelWorkbookProvider;
import piecework.export.concrete.ProcessInstanceQueryPager;
import piecework.process.ProcessInstanceQueryBuilder;
import piecework.process.ProcessInstanceSearchCriteria;
import piecework.model.SearchResults;
import piecework.common.ViewContext;
import piecework.exception.*;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.AttachmentRepository;
import piecework.persistence.ProcessInstanceRepository;
import piecework.security.DataFilterService;
import piecework.security.Sanitizer;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.settings.UserInterfaceSettings;
import piecework.submission.SubmissionTemplate;
import piecework.ui.Streamable;
import piecework.util.ExportUtility;
import piecework.util.ProcessUtility;
import piecework.validation.Validation;
import piecework.validation.ValidationFactory;

import javax.ws.rs.core.MultivaluedMap;
import java.util.*;

/**
 * @author James Renfro
 */
@Service
public class ProcessInstanceService {

    private static final String VERSION = "v1";
    private static final Logger LOG = Logger.getLogger(ProcessInstanceService.class);

    @Autowired
    AttachmentRepository attachmentRepository;

    @Autowired
    CommandFactory commandFactory;

    @Autowired
    DataFilterService dataFilterService;

    @Autowired
    DeploymentService deploymentService;

    @Autowired
    IdentityService identityService;

    @Autowired
    ProcessEngineFacade facade;

    @Autowired
    ProcessService processService;

    @Autowired
    ProcessInstanceRepository processInstanceRepository;

    @Autowired
    RequestService requestService;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    TaskService taskService;

    @Autowired
    ValidationFactory validationFactory;

    @Autowired
    UserInterfaceSettings settings;


    public void activate(Entity principal, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawReason) throws BadRequestError, PieceworkException {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = read(process, rawProcessInstanceId, false);
        ProcessDeployment deployment = deploymentService.read(process, instance);
        String reason = sanitizer.sanitize(rawReason);
        commandFactory.activation(principal, process, deployment, instance, reason).execute();
    }

    public void assign(Entity principal, Process process, ProcessDeployment deployment, ProcessInstance instance, Task task, String assigneeId) throws BadRequestError, PieceworkException {
        User assignee = assigneeId != null ? identityService.getUserWithAccessAuthority(assigneeId) : null;
        commandFactory.assignment(principal, process, deployment, instance, task, assignee).execute();
    }

    public <T> ProcessInstance attach(Entity principal, RequestDetails requestDetails, String rawProcessDefinitionKey, String rawProcessInstanceId, T data, Class<T> type) throws PieceworkException {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = read(process, rawProcessInstanceId, false);
        ProcessDeployment deployment = deploymentService.read(process, instance);

        Task task = taskService.allowedTask(process, instance, principal, true);
        if (task == null)
            throw new ForbiddenError(Constants.ExceptionCodes.task_required);

        FormRequest request = requestService.create(requestDetails, process, instance, task, ActionType.ATTACH);
        Validation validation = commandFactory.validation(process, deployment, request, data, type, principal).execute();

        return commandFactory.attachment(principal, deployment, validation).execute();
    }

    public ProcessInstance cancel(Entity principal, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawReason) throws BadRequestError, PieceworkException {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = read(process, rawProcessInstanceId, false);
        ProcessDeployment deployment = deploymentService.read(process, instance);
        String reason = sanitizer.sanitize(rawReason);

        return commandFactory.cancellation(principal, process, deployment, instance, reason).execute();
    }

    public ProcessInstance complete(String processInstanceId) {
        try {
            ProcessInstance instance = processInstanceRepository.findOne(processInstanceId);
            if (instance != null) {
                Map<String, List<Value>> data = instance.getData();
                return commandFactory.completion(instance, data).execute();
            }
        } catch (PieceworkException error) {
            LOG.error("Unable to mark instance as complete: " + processInstanceId, error);
        }
        return null;
    }

    public <T> ProcessInstance create(Entity principal, RequestDetails requestDetails, String rawProcessDefinitionKey, T data, Class<T> type) throws PieceworkException {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessDeployment deployment = deploymentService.read(process, (ProcessInstance)null);
        FormRequest request = requestService.create(requestDetails, process);
        Validation validation = commandFactory.validation(process, deployment, request, data, type, principal).execute();

        return commandFactory.createInstance(principal, validation).execute();
    }

    public void deleteAttachment(Entity principal, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawAttachmentId) throws BadRequestError, PieceworkException {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = read(process, rawProcessInstanceId, false);
        String attachmentId = sanitizer.sanitize(rawAttachmentId);
        Task task = taskService.allowedTask(process, instance, principal, true);

        commandFactory.detachment(principal, process, instance, task, attachmentId).execute();
    }

    public void createSubTask(Entity principal, Process process, ProcessInstance instance, Task task, String parentTaskId, SubmissionTemplate template, Submission submission) throws PieceworkException {
        Validation validation = validationFactory.validation(process, instance, task, template, submission, principal, true);
        ProcessDeployment deployment = deploymentService.read(process, (ProcessInstance)null);
        commandFactory.createsubtask(principal, process, instance, deployment, parentTaskId, validation).execute();
    }

    public ProcessInstance findByTaskId(Process process, String rawTaskId) throws StatusCodeError {
        if (process == null)
            throw new BadRequestError(Constants.ExceptionCodes.process_does_not_exist);
        String taskId = sanitizer.sanitize(rawTaskId);
        String processDefinitionKey = process.getProcessDefinitionKey();

        ProcessInstance instance = processInstanceRepository.findByTaskId(processDefinitionKey, taskId);
        return instance;
    }

    public Streamable getDiagram(String rawProcessDefinitionKey, String rawProcessInstanceId) throws StatusCodeError {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = read(process, rawProcessInstanceId, false);

        ProcessDeploymentVersion selectedDeploymentVersion = ProcessUtility.deploymentVersion(process, instance.getDeploymentId());
        if (selectedDeploymentVersion == null)
            throw new NotFoundError();

        ProcessDeployment deployment = deploymentService.read(process, instance.getDeploymentId());

        try {
            ProcessDeploymentResource resource = facade.resource(process, deployment, "image/png");
            return resource;
        } catch (ProcessEngineException e) {
            LOG.error("Could not generate diagram", e);
            throw new InternalServerError(e);
        }
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
            ViewContext viewContext = new ViewContext(settings, VERSION);

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

    public void restart(Entity principal, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawReason) throws BadRequestError, PieceworkException {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = read(process, rawProcessInstanceId, false);
        ProcessDeployment deployment = deploymentService.read(process, instance);
        String reason = sanitizer.sanitize(rawReason);
        commandFactory.restart(principal, process, deployment, instance, reason).execute();
    }

    public void suspend(Entity principal, String rawProcessDefinitionKey, String rawProcessInstanceId, String rawReason) throws BadRequestError, PieceworkException {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = read(process, rawProcessInstanceId, false);
        ProcessDeployment deployment = deploymentService.read(process, instance);
        String reason = sanitizer.sanitize(rawReason);

        commandFactory.suspension(principal, process, deployment, instance, reason).execute();
    }

    public ProcessInstance update(Entity principal, String rawProcessDefinitionKey, String rawProcessInstanceId, ProcessInstance rawInstance) throws BadRequestError, PieceworkException {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = read(process, rawProcessInstanceId, false);
        ProcessDeployment deployment = deploymentService.read(process, instance);

        String processStatus = sanitizer.sanitize(rawInstance.getProcessStatus());
        String applicationStatus = sanitizer.sanitize(rawInstance.getApplicationStatus());
        String applicationStatusExplanation = sanitizer.sanitize(rawInstance.getApplicationStatusExplanation());

        if (StringUtils.isNotEmpty(processStatus) || StringUtils.isEmpty(applicationStatus)) {
            AbstractOperationCommand command = null;
            if (processStatus != null && !processStatus.equalsIgnoreCase(instance.getProcessStatus())) {
                if (processStatus.equals(Constants.ProcessStatuses.OPEN))
                    command = commandFactory.activation(principal, process, deployment, instance, applicationStatusExplanation);
                else if (processStatus.equals(Constants.ProcessStatuses.CANCELLED))
                    command = commandFactory.cancellation(principal, process, deployment, instance, applicationStatusExplanation);
                else if (processStatus.equals(Constants.ProcessStatuses.SUSPENDED))
                    command = commandFactory.suspension(principal, process, deployment, instance, applicationStatusExplanation);
            }
            if (command == null)
                command = commandFactory.updateStatus(principal, process, instance, applicationStatus, applicationStatusExplanation);

            return command.execute();
        } else {
            throw new BadRequestError(Constants.ExceptionCodes.instance_cannot_be_modified);
        }
    }

    public IteratingDataProvider<?> exportProvider(MultivaluedMap<String, String> rawQueryParameters, Entity principal, boolean isCSV) throws StatusCodeError {
        ProcessInstanceSearchCriteria.Builder executionCriteriaBuilder =
                new ProcessInstanceSearchCriteria.Builder(rawQueryParameters, sanitizer);

        executionCriteriaBuilder.processStatus(Constants.ProcessStatuses.ALL);
        executionCriteriaBuilder.maxResults(50000);

        Set<String> processDefinitionKeys = principal.getProcessDefinitionKeys(AuthorizationRole.OVERSEER);
        Set<Process> allowedProcesses = processService.findProcesses(processDefinitionKeys);
        if (!allowedProcesses.isEmpty()) {
            Map<String, Process> allowedProcessMap = new HashMap<String, Process>();
            for (Process allowedProcess : allowedProcesses) {
                String allowedProcessDefinitionKey = allowedProcess.getProcessDefinitionKey();

                if (allowedProcessDefinitionKey != null) {
                    allowedProcessMap.put(allowedProcessDefinitionKey, allowedProcess);
                    executionCriteriaBuilder.processDefinitionKey(allowedProcessDefinitionKey);
                }
            }
            ProcessInstanceSearchCriteria executionCriteria = executionCriteriaBuilder.build();
            processDefinitionKeys = executionCriteria.getProcessDefinitionKeys();
            if (processDefinitionKeys.size() != 1)
                throw new BadRequestError();

            String processDefinitionKey = processDefinitionKeys.iterator().next();
            Process process = processService.read(processDefinitionKey);

            Query query = new ProcessInstanceQueryBuilder(executionCriteria).build();
            ProcessInstanceQueryPager pager = new ProcessInstanceQueryPager(query, processInstanceRepository, executionCriteria.getSort());

            List<Field> fields = ExportUtility.exportFields(process.getDeployment());
            Map<String, String> headerMap = ExportUtility.headerMap(fields);
            if (isCSV)
                return new ExportAsCommaSeparatedValuesProvider(headerMap, pager);
            return new ExportAsExcelWorkbookProvider(process.getProcessDefinitionLabel(), headerMap, pager);
        }
        return null;
    }

    public SearchResults search(MultivaluedMap<String, String> rawQueryParameters, Entity principal) throws StatusCodeError {
        ProcessInstanceSearchCriteria.Builder executionCriteriaBuilder =
                new ProcessInstanceSearchCriteria.Builder(rawQueryParameters, sanitizer);

        ViewContext context = new ViewContext(settings, VERSION);

        SearchResults.Builder resultsBuilder = new SearchResults.Builder()
                .resourceLabel("Workflows")
                .resourceName(ProcessInstance.Constants.ROOT_ELEMENT_NAME)
                .link(context.getApplicationUri());

        Set<String> processDefinitionKeys = principal.getProcessDefinitionKeys(AuthorizationRole.OVERSEER);
        Set<Process> allowedProcesses = processService.findProcesses(processDefinitionKeys);
        if (!allowedProcesses.isEmpty()) {
            for (Process allowedProcess : allowedProcesses) {
                String allowedProcessDefinitionKey = allowedProcess.getProcessDefinitionKey();

                if (allowedProcessDefinitionKey != null) {
                    executionCriteriaBuilder.processDefinitionKey(allowedProcessDefinitionKey);
                    resultsBuilder.definition(new Process.Builder(allowedProcess, new PassthroughSanitizer()).build(context));
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

            Pageable pageable = new PageRequest(firstResult, maxResult, executionCriteria.getSort());
            Page<ProcessInstance> page = processInstanceRepository.findByCriteria(executionCriteria, pageable);

            if (page.hasContent()) {
                for (ProcessInstance instance : page.getContent()) {
                    resultsBuilder.item(new ProcessInstance.Builder(instance).build(context));
                }
            }

            resultsBuilder.page(page, pageable);
        }
        return resultsBuilder.build();
    }

    public ProcessInstance updateField(RequestDetails requestDetails, String rawProcessDefinitionKey, String rawProcessInstanceId, String fieldName, Object object, Class<?> type, Entity principal) throws PieceworkException {
        Process process = processService.read(rawProcessDefinitionKey);
        ProcessInstance instance = read(process, rawProcessInstanceId, true);
        ProcessDeployment deployment = deploymentService.read(process, instance);
        Task task = taskService.allowedTask(process, instance, principal, true);
        FormRequest request = requestService.create(requestDetails, process, instance, task, ActionType.UPDATE);
        Validation validation = commandFactory.validation(process, deployment, request, object, type, principal, null, fieldName).execute();

        return commandFactory.updateValue(principal, task, validation).execute();
    }

    public ProcessInstance updateData(Entity principal, String processDefinitionKey, String processInstanceId, Map<String, List<Value>> data, Map<String, List<Message>> messages, String applicationStatusExplanation) throws PieceworkException {
        Process process = processService.read(processDefinitionKey);
        ProcessInstance instance = read(process, processInstanceId, true);
        Task task = taskService.allowedTask(process, instance, principal, true);
        return commandFactory.updateData(principal, process, instance, task, data, messages, applicationStatusExplanation).execute();
    }

}
