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
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.Versions;
import piecework.authorization.AuthorizationRole;
import piecework.common.ViewContext;
import piecework.engine.ProcessEngineFacade;
import piecework.enumeration.ActionType;
import piecework.exception.ForbiddenError;
import piecework.exception.StatusCodeError;
import piecework.model.*;
import piecework.model.Process;
import piecework.CommandExecutor;
import piecework.command.TaskCommand;
import piecework.identity.IdentityHelper;
import piecework.persistence.ProcessInstanceRepository;
import piecework.process.ProcessInstanceSearchCriteria;
import piecework.security.Sanitizer;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.task.TaskCriteria;
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
    Environment environment;

    @Autowired
    IdentityHelper helper;

    @Autowired
    ProcessEngineFacade facade;

    @Autowired
    IdentityService identityService;

    @Autowired
    ProcessInstanceRepository processInstanceRepository;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    CommandExecutor commandExecutor;

    @Autowired
    Versions versions;


    public Task allowedTask(Process process, String taskId, boolean limitToActive) throws StatusCodeError {

        ProcessInstance instance = processInstanceRepository.findByTaskId(process.getProcessDefinitionKey(), taskId);

        if (instance == null)
            return null;

        return allowedTask(process, instance, limitToActive);
    }

    /*
     * Returns the first task for the passed instance that the user is allowed to access
     */
    public Task allowedTask(Process process, ProcessInstance processInstance, boolean limitToActive) throws StatusCodeError {

        Set<Task> tasks = processInstance.getTasks();

        boolean hasOversight = helper.hasRole(process, AuthorizationRole.OVERSEER);

        String currentUserId = !hasOversight ? helper.getAuthenticatedSystemOrUserId() : null;

        for (Task task : tasks) {
            if (limitToActive && !task.isActive())
                continue;

            if (!hasOversight) {
                if (currentUserId == null)
                    break;

                boolean isCandidate = !task.getCandidateAssigneeIds().isEmpty() && task.getCandidateAssigneeIds().contains(currentUserId);
                if (isCandidate)
                    return task;

                boolean isAssignee = task.getAssigneeId() != null && task.getAssigneeId().equals(currentUserId);
                if (isAssignee)
                    return task;
            } else {
                return task;
            }
        }

        return null;
    }

    public void checkIsActiveIfTaskExists(Process process, Task task) throws StatusCodeError {
        String taskId = task != null ? task.getTaskInstanceId() : null;
        if (StringUtils.isNotEmpty(taskId)) {
            task = allowedTask(process, taskId, false);
            if (task == null || !task.isActive())
                throw new ForbiddenError();
        }
    }

    public void completeIfTaskExists(Process process, Task task, ActionType action, FormValidation validation) throws StatusCodeError {
        TaskCommand complete = new TaskCommand(process, task, action, validation);
        commandExecutor.execute(complete);
    }

    private Set<String> processDefinitionKeys(Set<Process> processes) {
        Set<String> set = new HashSet<String>();
        if (processes != null) {
            for (Process process : processes) {
                set.add(process.getProcessDefinitionKey());
            }
        }

        return set;
    }

    public SearchResults allowedTasksDirect(MultivaluedMap<String, String> rawQueryParameters) throws StatusCodeError {
        long time = 0;
        if (LOG.isDebugEnabled())
            time = System.currentTimeMillis();

        Set<Process> overseerProcesses = helper.findProcesses(AuthorizationRole.OVERSEER);
        Set<Process> userProcesses = Sets.difference(helper.findProcesses(AuthorizationRole.USER), overseerProcesses);
        Set<Process> allowedProcesses = Sets.union(overseerProcesses, userProcesses);

        Set<String> overseerProcessDefinitionKeys = processDefinitionKeys(overseerProcesses);
        Set<String> userProcessDefinitionKeys = processDefinitionKeys(userProcesses);

        String currentUserId = helper.getAuthenticatedSystemOrUserId();

        ViewContext taskViewContext = versions.getVersion1();

        SearchResults.Builder resultsBuilder = new SearchResults.Builder()
                .resourceLabel("Tasks")
                .resourceName(Form.Constants.ROOT_ELEMENT_NAME)
                .link(taskViewContext.getApplicationUri());

        ProcessInstanceSearchCriteria.Builder executionCriteriaBuilder =
                new ProcessInstanceSearchCriteria.Builder(rawQueryParameters, sanitizer);
        ViewContext version1 = versions.getVersion1();

        if (!allowedProcesses.isEmpty()) {
            PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();

            for (Process allowedProcess : allowedProcesses) {
//                ProcessDeployment deployment = allowedProcess.getDeployment();
                if (allowedProcess.getProcessDefinitionKey() != null) {
                    executionCriteriaBuilder.processDefinitionKey(allowedProcess.getProcessDefinitionKey());
//                            .engineProcessDefinitionKey(deployment.getEngineProcessDefinitionKey())
//                            .engine(deployment.getEngine());

                    resultsBuilder.definition(new Process.Builder(allowedProcess, passthroughSanitizer).build(version1));
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
                for (ProcessInstance instance : page.getContent()) {
                    Set<Task> tasks = instance.getTasks();
                    if (tasks != null && !tasks.isEmpty()) {
                        for (Task task : tasks) {
                            if (!processStatus.equals(Constants.ProcessStatuses.ALL) &&
                                    !processStatus.equalsIgnoreCase(task.getTaskStatus()))
                                continue;

                            if (!taskStatus.equals(Constants.TaskStatuses.ALL) &&
                                    !taskStatus.equalsIgnoreCase(task.getTaskStatus()))
                                continue;

                            if (!overseerProcessDefinitionKeys.contains(task.getProcessDefinitionKey())) {
                                if (!task.getCandidateAssignees().contains(currentUserId) && !task.getAssigneeId().equals(currentUserId))
                                    continue;
                            }

                            resultsBuilder.item(rebuildTask(task, passthroughSanitizer));
                            count++;
                        }
                    }
                }

//                if (count == 0 && environment.getProperty("synchronize.tasks", Boolean.class, Boolean.FALSE)) {
//                    SearchResults internalTasks = allowedTasks(rawQueryParameters);
//                    if (internalTasks.getTotal() != null && internalTasks.getTotal().longValue() > 0) {
//                        resultsBuilder.items(internalTasks.getList());
//                    }
//                }

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

    public boolean hasAllowedTask(Process process, ProcessInstance processInstance, boolean limitToActive) throws StatusCodeError {

        if (allowedTask(process, processInstance, limitToActive) != null)
            return true;

        return false;
    }

    public Task rebuildTask(Task task, Sanitizer sanitizer) {
        Task.Builder builder = new Task.Builder(task, sanitizer);

        if (StringUtils.isNotEmpty(task.getAssigneeId())) {
            builder.assignee(identityService.getUser(task.getAssigneeId()));
        }
        Set<String> candidateAssigneeIds = task.getCandidateAssigneeIds();
        if (candidateAssigneeIds != null && !candidateAssigneeIds.isEmpty()) {
            for (String candidateAssigneeId : candidateAssigneeIds) {
                builder.candidateAssignee(identityService.getUser(candidateAssigneeId));
            }
        }

        return builder.build(versions.getVersion1());
    }

    private TaskCriteria overseerCriteria(Set<Process> allowedProcesses, MultivaluedMap<String, String> rawQueryParameters) {
        return new TaskCriteria.Builder(allowedProcesses, rawQueryParameters, sanitizer).build();
    }

    private TaskCriteria overseerCriteria(Set<Process> allowedProcesses, String taskId) {
        return new TaskCriteria.Builder().processes(allowedProcesses).taskId(taskId).build();
    }

    private TaskCriteria userCriteria(Set<Process> allowedProcesses, MultivaluedMap<String, String> rawQueryParameters) {
        return new TaskCriteria.Builder(allowedProcesses, rawQueryParameters, sanitizer).participantId(helper.getAuthenticatedSystemOrUserId()).build();
    }

    private TaskCriteria userCriteria(Set<Process> allowedProcesses, String taskId) {
        return new TaskCriteria.Builder().processes(allowedProcesses).taskId(taskId).participantId(helper.getAuthenticatedSystemOrUserId()).build();
    }
}
