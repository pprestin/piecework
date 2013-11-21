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

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.Versions;
import piecework.authorization.AuthorizationRole;
import piecework.common.ViewContext;
import piecework.enumeration.ActionType;
import piecework.exception.ForbiddenError;
import piecework.exception.StatusCodeError;
import piecework.model.*;
import piecework.model.Process;
import piecework.CommandExecutor;
import piecework.command.TaskCommand;
import piecework.identity.IdentityHelper;
import piecework.persistence.DeploymentRepository;
import piecework.persistence.ProcessInstanceRepository;
import piecework.process.ProcessInstanceSearchCriteria;
import piecework.security.DataFilterService;
import piecework.security.Sanitizer;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.task.TaskFactory;
import piecework.validation.FormValidation;

import javax.ws.rs.core.MultivaluedMap;
import java.util.*;

/**
 * @author James Renfro
 */
@Service
public class TaskService {

    private static final Logger LOG = Logger.getLogger(TaskService.class);

    @Autowired
    DataFilterService dataFilterService;

    @Autowired
    DeploymentRepository deploymentRepository;

    @Autowired
    IdentityHelper helper;

    @Autowired
    IdentityService identityService;

    @Autowired
    ProcessInstanceRepository processInstanceRepository;

    @Autowired
    ProcessService processService;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    CommandExecutor commandExecutor;

    @Autowired
    Versions versions;


    public Task read(Process process, String taskId, boolean limitToActive) throws StatusCodeError {
        ProcessInstance instance = processInstanceRepository.findByTaskId(process.getProcessDefinitionKey(), taskId);

        if (instance == null)
            return null;

        Entity principal = helper.getPrincipal();
        return findTask(process, instance, taskId, principal, limitToActive);
    }

    /*
     * Returns the first task for the passed instance that the user is allowed to access
     */
    public Task allowedTask(Process process, ProcessInstance instance, Entity principal, boolean limitToActive) throws StatusCodeError {
        return findTask(process, instance, null, principal, limitToActive);
    }

    private Task findTask(Process process, ProcessInstance instance, String taskId, Entity principal, boolean limitToActive) throws StatusCodeError {

        Set<Task> tasks = instance.getTasks();
        boolean hasOversight = principal.hasRole(process, AuthorizationRole.OVERSEER);
        ViewContext version1 = versions.getVersion1();

        for (Task task : tasks) {
            if (limitToActive && !task.isActive())
                continue;

            if (taskId != null && !task.getTaskInstanceId().equals(taskId))
                continue;

            if ( hasOversight || task.isCandidateOrAssignee(principal) ) {
                Map<String, User> userMap = identityService.findUsers(task.getAssigneeAndCandidateAssigneeIds());
                return TaskFactory.task(task, new PassthroughSanitizer(), userMap, version1);
            }
        }

        return null;
    }

    public void checkIsActiveIfTaskExists(Process process, Task task) throws StatusCodeError {
        String taskId = task != null ? task.getTaskInstanceId() : null;
        if (StringUtils.isNotEmpty(taskId)) {
            task = read(process, taskId, false);
            if (task == null || !task.isActive())
                throw new ForbiddenError();
        }
    }

    public void completeIfTaskExists(Process process, ProcessInstance instance, Task task, ActionType action, FormValidation validation) throws StatusCodeError {
        if (task != null) {
            TaskCommand complete = new TaskCommand(process, instance, task, action, validation);
            commandExecutor.execute(complete);
        }
    }

    public SearchResults search(MultivaluedMap<String, String> rawQueryParameters, Entity principal, boolean wrapWithForm, boolean includeData) throws StatusCodeError {
        long time = 0;
        if (LOG.isDebugEnabled())
            time = System.currentTimeMillis();

        Set<String> overseerProcessDefinitionKeys = principal.getProcessDefinitionKeys(AuthorizationRole.OVERSEER);
        Set<String> userProcessDefinitionKeys = principal.getProcessDefinitionKeys(AuthorizationRole.USER);

        Set<Process> overseerProcesses = processService.findProcesses(overseerProcessDefinitionKeys);
        Set<Process> userProcesses = Sets.difference(processService.findProcesses(userProcessDefinitionKeys), overseerProcesses);
        Set<Process> allowedProcesses = Sets.union(overseerProcesses, userProcesses);

        ViewContext taskViewContext = versions.getVersion1();

        SearchResults.Builder resultsBuilder = new SearchResults.Builder()
                .resourceLabel("Tasks")
                .resourceName(Form.Constants.ROOT_ELEMENT_NAME)
                .link(taskViewContext.getApplicationUri());

        ViewContext version1 = versions.getVersion1();

        if (wrapWithForm)
            resultsBuilder.resourceName(Form.Constants.ROOT_ELEMENT_NAME)
                    .link(version1.getApplicationUri(Form.Constants.ROOT_ELEMENT_NAME));

        ProcessInstanceSearchCriteria.Builder executionCriteriaBuilder =
                new ProcessInstanceSearchCriteria.Builder(rawQueryParameters, sanitizer);

        if (!allowedProcesses.isEmpty()) {
            PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();

            Map<String, ProcessDeployment> deploymentMap = new HashMap<String, ProcessDeployment>();
            for (Process allowedProcess : allowedProcesses) {
                if (allowedProcess.getProcessDefinitionKey() != null) {
                    executionCriteriaBuilder.processDefinitionKey(allowedProcess.getProcessDefinitionKey());

                    Process definition = new Process.Builder(allowedProcess, passthroughSanitizer).build(version1);

                    if (wrapWithForm) {
                        resultsBuilder.definition(new Form.Builder().processDefinitionKey(definition.getProcessDefinitionKey()).task(new Task.Builder().processDefinitionKey(definition.getProcessDefinitionKey()).processDefinitionLabel(definition.getProcessDefinitionLabel()).build(version1)).build(version1));
                    } else {
                        resultsBuilder.definition(definition);
                    }
                }
            }
            ProcessInstanceSearchCriteria executionCriteria = executionCriteriaBuilder.build();
            resultsBuilder.parameters(executionCriteria.getSanitizedParameters());

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

                String processStatus = executionCriteria.getProcessStatus() != null ? executionCriteria.getProcessStatus() : Constants.ProcessStatuses.OPEN;
                String taskStatus = executionCriteria.getTaskStatus() != null ? executionCriteria.getTaskStatus() : Constants.TaskStatuses.ALL;
                int count = 0;
                if (wrapWithForm) {
                    Set<String> deploymentIds = new HashSet<String>();
                    for (ProcessInstance instance : page.getContent()) {
                        if (instance.getDeploymentId() != null)
                            deploymentIds.add(instance.getDeploymentId());
                    }
                    Iterable<ProcessDeployment> deployments = deploymentRepository.findAll(deploymentIds);
                    if (deployments != null) {
                        for (ProcessDeployment deployment : deployments) {
                            deploymentMap.put(deployment.getDeploymentId(), deployment);
                        }
                    }
                }

                for (ProcessInstance instance : page.getContent()) {
                    ProcessDeployment processDeployment = null;
                    if (wrapWithForm)
                        processDeployment = deploymentMap.get(instance.getDeploymentId());

                    Set<Task> tasks = instance.getTasks();
                    if (tasks != null && !tasks.isEmpty()) {
                        for (Task task : tasks) {
                            if (!processStatus.equals(Constants.ProcessStatuses.ALL) &&
                                    !processStatus.equalsIgnoreCase(task.getTaskStatus()))
                                continue;

                            if (!taskStatus.equals(Constants.TaskStatuses.ALL) &&
                                    !taskStatus.equalsIgnoreCase(task.getTaskStatus()))
                                continue;

                            if (overseerProcessDefinitionKeys.contains(task.getProcessDefinitionKey())
                                || task.isCandidateOrAssignee(principal) ) {
                                Map<String, User> userMap = identityService.findUsers(task.getAssigneeAndCandidateAssigneeIds());
                                Task rebuilt = TaskFactory.task(task, new PassthroughSanitizer(), userMap, version1);

                                if (wrapWithForm) {
                                    if (processDeployment != null) {
                                        Activity activity = processDeployment != null ? processDeployment.getActivity(task.getTaskDefinitionKey()) : null;
                                        Action createAction = activity != null ? activity.action(ActionType.CREATE) : null;

                                        boolean external = createAction != null && StringUtils.isNotEmpty(createAction.getLocation());

                                        Map<String, List<Value>> data = null;

                                        if (includeData)
                                            data = dataFilterService.filter(activity.getFieldMap(), instance, null, principal, false);

                                        resultsBuilder.item(new Form.Builder()
                                                .formInstanceId(rebuilt.getTaskInstanceId())
                                                .taskSubresources(rebuilt.getProcessDefinitionKey(), rebuilt, version1)
                                                .processDefinitionKey(rebuilt.getProcessDefinitionKey())
                                                .instanceSubresources(rebuilt.getProcessDefinitionKey(),
                                                        rebuilt.getProcessInstanceId(), null, 0, version1)
                                                .data(data)
                                                .external(external)
                                                .build(version1));
                                    }
                                } else {
                                    resultsBuilder.item(rebuilt);
                                }
                                count++;
                            }
                        }
                    }
                }

                if (executionCriteria.getMaxResults() != null || executionCriteria.getFirstResult() != null) {
                    if (executionCriteria.getFirstResult() != null)
                        resultsBuilder.firstResult(executionCriteria.getFirstResult());
                    else
                        resultsBuilder.firstResult(1);

                    if (executionCriteria.getMaxResults() != null)
                        resultsBuilder.maxResults(executionCriteria.getMaxResults());
                    else
                        resultsBuilder.maxResults(count);

                    resultsBuilder.total(Long.valueOf(count));
                } else {
                    resultsBuilder.firstResult(1);
                    resultsBuilder.maxResults(count);
                    resultsBuilder.total(Long.valueOf(count));
                }
            }
        }

        if (LOG.isDebugEnabled())
            LOG.debug("Retrieved tasks in " + (System.currentTimeMillis() - time) + " ms");

        return resultsBuilder.build();
    }

    public boolean hasAllowedTask(Process process, ProcessInstance processInstance, Entity principal, boolean limitToActive) throws StatusCodeError {

        if (allowedTask(process, processInstance, principal, limitToActive) != null)
            return true;

        return false;
    }

    public Task read(ProcessInstance instance, String taskId) {
        if (instance != null && StringUtils.isNotEmpty(taskId)) {
            if (instance != null) {
                Set<Task> tasks = instance.getTasks();
                if (tasks != null) {
                    for (Task task : tasks) {
                        if (task.getTaskInstanceId() != null && task.getTaskInstanceId().equals(taskId)) {
                            Map<String, User> userMap = identityService.findUsers(task.getAssigneeAndCandidateAssigneeIds());
                            return TaskFactory.task(task, new PassthroughSanitizer(), userMap, versions.getVersion1());
                        }
                    }
                }
            }
        }

        return null;
    }

    public boolean update(String processInstanceId, Task task) {
        return processInstanceRepository.update(processInstanceId, task);
    }

}
