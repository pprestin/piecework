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
import piecework.command.AbstractOperationCommand;
import piecework.command.CommandFactory;
import piecework.command.UpdateDataCommand;
import piecework.common.ViewContext;
import piecework.engine.ProcessEngineFacade;
import piecework.exception.*;
import piecework.export.IteratingDataProvider;
import piecework.export.concrete.ExportAsCommaSeparatedValuesProvider;
import piecework.export.concrete.ExportAsExcelWorkbookProvider;
import piecework.export.concrete.ProcessInstanceQueryPager;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ModelProviderFactory;
import piecework.persistence.ProcessInstanceProvider;
import piecework.process.ProcessInstanceQueryBuilder;
import piecework.process.ProcessInstanceSearchCriteria;
import piecework.repository.AttachmentRepository;
import piecework.repository.ProcessInstanceRepository;
import piecework.security.Sanitizer;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.security.data.DataFilterService;
import piecework.settings.UserInterfaceSettings;
import piecework.util.ExportUtility;
import piecework.validation.ValidationFactory;

import javax.ws.rs.core.MultivaluedMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author James Renfro
 */
@Service
public class ProcessInstanceService {

    private static final Logger LOG = Logger.getLogger(ProcessInstanceService.class);
    private static final String VERSION = "v1";

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
    ModelProviderFactory modelProviderFactory;

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


    public void assign(String processDefinitionKey, String processInstanceId, Entity principal, Task task, String assigneeId) throws PieceworkException {
        ProcessInstanceProvider instanceProvider = modelProviderFactory.instanceProvider(processDefinitionKey, processInstanceId, principal);
        User assignee = assigneeId != null ? identityService.getUserWithAccessAuthority(assigneeId) : null;
        commandFactory.assignment(instanceProvider, task, assignee).execute();
    }

    public ProcessInstance complete(String processInstanceId, Entity principal) {
        try {
            ProcessInstanceProvider instanceProvider = modelProviderFactory.instanceProvider(processInstanceId, principal);
            return commandFactory.completion(instanceProvider).execute();
        } catch (PieceworkException error) {
            LOG.error("Unable to mark instance as complete: " + processInstanceId, error);
        }
        return null;
    }

    public ProcessInstance findByTaskId(Process process, String rawTaskId) throws StatusCodeError {
        if (process == null)
            throw new BadRequestError(Constants.ExceptionCodes.process_does_not_exist);
        String taskId = sanitizer.sanitize(rawTaskId);
        String processDefinitionKey = process.getProcessDefinitionKey();

        ProcessInstance instance = processInstanceRepository.findByTaskId(processDefinitionKey, taskId);
        return instance;
    }

    public ProcessInstance update(Entity principal, String rawProcessDefinitionKey, String rawProcessInstanceId, ProcessInstance rawInstance) throws BadRequestError, PieceworkException {
        ProcessInstanceProvider instanceProvider = modelProviderFactory.instanceProvider(rawProcessDefinitionKey, rawProcessInstanceId, principal);

        String processStatus = sanitizer.sanitize(rawInstance.getProcessStatus());
        String applicationStatus = sanitizer.sanitize(rawInstance.getApplicationStatus());
        String applicationStatusExplanation = sanitizer.sanitize(rawInstance.getApplicationStatusExplanation());

        if (StringUtils.isNotEmpty(processStatus) || StringUtils.isEmpty(applicationStatus)) {
            AbstractOperationCommand command = null;
            ProcessInstance instance = instanceProvider.instance();
            if (processStatus != null && !processStatus.equalsIgnoreCase(instance.getProcessStatus())) {
                if (processStatus.equals(Constants.ProcessStatuses.OPEN))
                    command = commandFactory.activation(instanceProvider, applicationStatusExplanation);
                else if (processStatus.equals(Constants.ProcessStatuses.CANCELLED))
                    command = commandFactory.cancellation(instanceProvider, applicationStatusExplanation);
                else if (processStatus.equals(Constants.ProcessStatuses.SUSPENDED))
                    command = commandFactory.suspension(instanceProvider, applicationStatusExplanation);
            }
            if (command == null)
                command = commandFactory.updateStatus(instanceProvider, applicationStatus, applicationStatusExplanation);

            return command.execute();
        } else {
            throw new BadRequestError(Constants.ExceptionCodes.instance_cannot_be_modified);
        }
    }

    public IteratingDataProvider<?> exportProvider(MultivaluedMap<String, String> rawQueryParameters, Entity principal, boolean isCSV) throws PieceworkException {
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
            Process process = modelProviderFactory.processProvider(processDefinitionKey, principal).process();

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

    public ProcessInstance updateData(Entity principal, String rawProcessDefinitionKey, String rawProcessInstanceId, Map<String, List<Value>> data, Map<String, List<Message>> messages, String applicationStatusExplanation) throws PieceworkException {
        ProcessInstanceProvider instanceProvider = modelProviderFactory.allowedTaskProvider(rawProcessDefinitionKey, rawProcessInstanceId, principal);

        UpdateDataCommand<ProcessInstanceProvider> command = commandFactory.updateData(instanceProvider, data, messages, applicationStatusExplanation);
        return command.execute();
    }

}
