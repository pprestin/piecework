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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.Versions;
import piecework.authorization.AuthorizationRole;
import piecework.common.ViewContext;
import piecework.engine.ProcessEngineFacade;
import piecework.engine.exception.ProcessEngineException;
import piecework.enumeration.ActionType;
import piecework.exception.ForbiddenError;
import piecework.exception.InternalServerError;
import piecework.exception.StatusCodeError;
import piecework.identity.IdentityDetails;
import piecework.model.*;
import piecework.model.Process;
import piecework.CommandExecutor;
import piecework.command.TaskCommand;
import piecework.identity.IdentityHelper;
import piecework.process.ProcessInstanceQueryBuilder;
import piecework.process.ProcessInstanceSearchCriteria;
import piecework.security.Sanitizer;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.task.TaskCriteria;
import piecework.task.TaskResults;

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
    MongoTemplate mongoOperations;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    CommandExecutor commandExecutor;

    @Autowired
    Versions versions;


    public Task allowedTask(Process process, String taskId, boolean limitToActive) throws StatusCodeError {
        Set<Process> overseerProcesses = helper.findProcesses(AuthorizationRole.OVERSEER);
        Set<Process> userProcesses = Sets.difference(helper.findProcesses(AuthorizationRole.USER), overseerProcesses);

        try {
            TaskCriteria.Builder criteriaBuilder = new TaskCriteria.Builder().taskId(taskId);
            if (overseerProcesses.contains(process)) {
                criteriaBuilder.process(process);
            } else if (userProcesses.contains(process)) {
                criteriaBuilder.process(process).participantId(helper.getAuthenticatedSystemOrUserId());
            }

            if (!limitToActive)
                criteriaBuilder.processStatus(Constants.ProcessStatuses.ALL);

            return facade.findTask(criteriaBuilder.build());
        } catch (ProcessEngineException e) {
            LOG.error(e);
            throw new InternalServerError();
        }
    }

    public Task allowedTask(Process process, ProcessInstance processInstance, boolean limitToActive) throws StatusCodeError {
        TaskCriteria.Builder taskCriteria = new TaskCriteria.Builder()
                .process(process)
                .processInstanceId(processInstance.getProcessInstanceId())
                .orderBy(TaskCriteria.OrderBy.CREATED_TIME_ASC);

        if (! helper.isAuthenticatedSystem() && !helper.hasRole(process, AuthorizationRole.OVERSEER)) {
            // If the call is not being made by an authenticated system, then the principal is a user and must have an active task
            // on this instance
            IdentityDetails user = helper.getAuthenticatedPrincipal();

            if (user == null)
                throw new ForbiddenError();

            taskCriteria.participantId(user.getInternalId());
        }

        if (!limitToActive)
            taskCriteria.processStatus(Constants.ProcessStatuses.ALL);
        else
            taskCriteria.active(Boolean.TRUE);

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

    public SearchResults allowedTasks(MultivaluedMap<String, String> rawQueryParameters) throws StatusCodeError {
        long time = 0;
        if (LOG.isDebugEnabled())
            time = System.currentTimeMillis();

        Set<Process> overseerProcesses = helper.findProcesses(AuthorizationRole.OVERSEER);
        Set<Process> userProcesses = Sets.difference(helper.findProcesses(AuthorizationRole.USER), overseerProcesses);
        Set<Process> allowedProcesses = Sets.union(overseerProcesses, userProcesses);

        ViewContext taskViewContext = versions.getVersion1();

        SearchResults.Builder resultsBuilder = new SearchResults.Builder()
                .resourceLabel("Tasks")
                .resourceName(Form.Constants.ROOT_ELEMENT_NAME)
                .link(taskViewContext.getApplicationUri(Form.Constants.ROOT_ELEMENT_NAME));

        if (!allowedProcesses.isEmpty()) {
            for (Process allowedProcess : allowedProcesses) {
                if (StringUtils.isEmpty(allowedProcess.getProcessDefinitionKey()))
                    continue;
                resultsBuilder.definition(allowedProcess);
            }
        }
        try {
            TaskResults results = facade.findTasks(overseerCriteria(overseerProcesses, rawQueryParameters), userCriteria(userProcesses, rawQueryParameters));
            List<Task> tasks = results.getTasks();
            if (tasks != null && !tasks.isEmpty()) {
                PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
                for (Task task : tasks) {
                    resultsBuilder.item(new Task.Builder(task, passthroughSanitizer)
                        .build(taskViewContext));
                }
            }

            resultsBuilder.firstResult(results.getFirstResult());
            resultsBuilder.maxResults(results.getMaxResults());
            resultsBuilder.total(Long.valueOf(results.getTotal()));
        } catch (ProcessEngineException e) {
            LOG.error("Could not find tasks", e);
            throw new InternalServerError();
        }

        if (LOG.isDebugEnabled())
            LOG.debug("Retrieving tasks took " + (System.currentTimeMillis() - time) + " ms");

        return resultsBuilder.build();
    }

    public void checkIsActiveIfTaskExists(Process process, Task task) throws StatusCodeError {
        String taskId = task != null ? task.getTaskInstanceId() : null;
        if (StringUtils.isNotEmpty(taskId)) {
            task = allowedTask(process, taskId, false);
            if (task == null || !task.isActive())
                throw new ForbiddenError();
        }
    }

    public void completeIfTaskExists(Process process, Task task, ActionType action) throws StatusCodeError {
        TaskCommand complete = new TaskCommand(process, task, action);
        commandExecutor.execute(complete);
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

            ViewContext version1 = versions.getVersion1();
            PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
            if (tasks != null && !tasks.isEmpty()) {
                convertedTasks = new ArrayList<Task>(tasks.size());
                for (Task task : tasks) {
                    Task.Builder builder = new Task.Builder(task, passthroughSanitizer);

                    if (task.getAssignee() != null && StringUtils.isNotEmpty(task.getAssignee().getUserId())) {
                        builder.assignee(identityService.getUser(task.getAssignee().getUserId()));
                    }

                    if (task.getCandidateAssignees() != null && !task.getCandidateAssignees().isEmpty()) {
                        builder.clearCandidateAssignees();
                        for (User candidateAssignee : task.getCandidateAssignees()) {
                            builder.candidateAssignee(identityService.getUser(candidateAssignee.getUserId()));
                        }
                    }

                    convertedTasks.add(builder.build(version1));
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
                executionCriteriaBuilder.processDefinitionKey(allowedProcess.getProcessDefinitionKey())
                        .engineProcessDefinitionKey(allowedProcess.getEngineProcessDefinitionKey())
                        .engine(allowedProcess.getEngine());

                resultsBuilder.definition(new Process.Builder(allowedProcess, passthroughSanitizer, false)
                        .interactions(null).build(version1));
            }
            ProcessInstanceSearchCriteria executionCriteria = executionCriteriaBuilder.build();
            resultsBuilder.parameters(executionCriteria.getSanitizedParameters());

            // Otherwise, look up all instances that match the query
            Query query = new ProcessInstanceQueryBuilder(executionCriteria).build();
            // Don't include form data in the result
            org.springframework.data.mongodb.core.query.Field field = query.fields();
            field.exclude("data");

            List<ProcessInstance> processInstances = mongoOperations.find(query, ProcessInstance.class);
            if (processInstances != null && !processInstances.isEmpty()) {
                int count = 0;

                String processStatus = executionCriteria.getProcessStatus() != null ? executionCriteria.getProcessStatus() : Constants.ProcessStatuses.OPEN;
                String taskStatus = executionCriteria.getTaskStatus() != null ? executionCriteria.getTaskStatus() : Constants.TaskStatuses.ALL;

                for (ProcessInstance processInstance : processInstances) {
                    Set<Task> tasks = processInstance.getTasks();
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

                if (count == 0 && environment.getProperty("synchronize.tasks", Boolean.class, Boolean.FALSE)) {
                    SearchResults internalTasks = allowedTasks(rawQueryParameters);
                    if (internalTasks.getTotal() != null && internalTasks.getTotal().longValue() > 0) {
                        resultsBuilder.items(internalTasks.getList());
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
