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
import org.springframework.stereotype.Service;
import piecework.Command;
import piecework.Constants;
import piecework.Versions;
import piecework.authorization.AuthorizationRole;
import piecework.command.*;
import piecework.common.ViewContext;
import piecework.enumeration.ActionType;
import piecework.exception.*;
import piecework.model.*;
import piecework.model.Process;
import piecework.identity.IdentityHelper;
import piecework.persistence.ProcessInstanceRepository;
import piecework.process.ProcessInstanceSearchCriteria;
import piecework.security.DataFilterService;
import piecework.security.Sanitizer;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.task.TaskFactory;
import piecework.task.TaskFilter;
import piecework.task.TaskPageHandler;
import piecework.util.TaskUtility;
import piecework.validation.Validation;

import javax.ws.rs.core.MultivaluedMap;
import java.util.*;

/**
 * @author James Renfro
 */
@Service
public class TaskService {

    private static final Logger LOG = Logger.getLogger(TaskService.class);

    @Autowired
    private CommandFactory commandFactory;

    @Autowired
    DataFilterService dataFilterService;

    @Autowired
    DeploymentService deploymentService;

    @Autowired
    IdentityHelper helper;

    @Autowired
    IdentityService identityService;

    @Autowired
    ProcessInstanceRepository processInstanceRepository;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    ProcessService processService;

    @Autowired
    RequestService requestService;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    Versions versions;


    public void complete(final String rawProcessDefinitionKey, final String rawTaskId, final String rawAction, final Submission rawSubmission, final RequestDetails requestDetails, final Entity principal) throws PieceworkException {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        String taskId = sanitizer.sanitize(rawTaskId);
        String action = sanitizer.sanitize(rawAction);

        ActionType actionType = ActionType.COMPLETE;
        if (StringUtils.isNotEmpty(action)) {
            try {
                actionType = ActionType.valueOf(action.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestError(Constants.ExceptionCodes.task_action_invalid);
            }
        }
        if (rawSubmission != null && rawSubmission.getAction() != null)
            actionType = rawSubmission.getAction();
        String assigneeId = sanitizer.sanitize(rawSubmission.getAssignee());
        String actingUser = null;
        if (principal != null) {
            if (principal.getEntityType() == Entity.EntityType.SYSTEM && StringUtils.isNotEmpty(requestDetails.getActAsUser()))
                actingUser = requestDetails.getActAsUser();
            else
                actingUser = principal.getEntityId();
        }

        Process process = processService.read(processDefinitionKey);
        Task task = taskId != null ? read(process, taskId, true) : null;
        if (task == null)
            throw new NotFoundError();

        // [jira wws-154] simply returns for identical assignee
        if ( StringUtils.isEmpty(assigneeId) ) {
            assigneeId = null; // to be consistent with task.getAssigneeId() to facilitate later comparison
        }
        if (    actionType == ActionType.ASSIGN && StringUtils.equals(assigneeId, task.getAssigneeId()) 
             || actionType == ActionType.CLAIM && StringUtils.equals(actingUser, task.getAssigneeId()) ) {
            return; // no change in assignee
        }

        ProcessInstance instance = processInstanceService.read(process, task.getProcessInstanceId(), false);
        ProcessDeployment deployment = deploymentService.read(process, instance);

        FormRequest request;
        Validation validation;

        Command<?> command;
        switch (actionType) {
            case ASSIGN:
                User assignee = assigneeId != null ? identityService.getUserWithAccessAuthority(assigneeId) : null;
                command = commandFactory.assignment(principal, process, deployment, instance, task, assignee);
                break;
            case CLAIM:
                User actingAs = actingUser != null ? identityService.getUserWithAccessAuthority(actingUser) : null;
                command = commandFactory.assignment(principal, process, deployment, instance, task, actingAs);
                break;
            case ATTACH:
                request = requestService.create(requestDetails, process, instance, task, actionType);
                validation = commandFactory.validation(process, deployment, request, rawSubmission, Submission.class, principal).execute();
                command = commandFactory.attachment(principal, deployment, validation);
                break;
            default:
                request = requestService.create(requestDetails, process, instance, task, actionType);
                validation = commandFactory.validation(process, deployment, request, rawSubmission, Submission.class, principal).execute();
                command = commandFactory.completeTask(principal, deployment, validation, actionType);
                break;
        }

        command.execute();
    }

    public Task read(Process process, String taskId, boolean limitToActive) throws StatusCodeError {
        ProcessInstance instance = processInstanceRepository.findByTaskId(process.getProcessDefinitionKey(), taskId);

        if (instance == null)
            return null;

        Entity principal = helper.getPrincipal();
        return TaskUtility.findTask(identityService, process, instance, taskId, principal, limitToActive, versions.getVersion1());
    }

    /*
     * Returns the first task for the passed instance that the user is allowed to access
     */
    public Task allowedTask(Process process, ProcessInstance instance, Entity principal, boolean limitToActive) throws StatusCodeError {
        return TaskUtility.findTask(identityService, process, instance, null, principal, limitToActive, versions.getVersion1());
    }

    public void checkIsActiveIfTaskExists(Process process, Task task) throws StatusCodeError {
        String taskId = task != null ? task.getTaskInstanceId() : null;
        if (StringUtils.isNotEmpty(taskId)) {
            task = read(process, taskId, false);
            if (task == null || !task.isActive())
                throw new ForbiddenError();
        }
    }

    public SearchResults search(MultivaluedMap<String, String> rawQueryParameters, Entity principal, boolean wrapWithForm, boolean includeData) throws StatusCodeError {
        long time = 0;
        if (LOG.isDebugEnabled())
            time = System.currentTimeMillis();

        Set<String> overseerProcessDefinitionKeys = principal.getProcessDefinitionKeys(AuthorizationRole.OVERSEER);
        Set<String> userProcessDefinitionKeys = principal.getProcessDefinitionKeys(AuthorizationRole.USER);

        Set<String> allProcessDefinitionKeys = Sets.union(overseerProcessDefinitionKeys, userProcessDefinitionKeys);
        Set<Process> allowedProcesses = processService.findProcesses(allProcessDefinitionKeys);

        ViewContext version = versions.getVersion1();

        TaskFilter taskFilter = new TaskFilter(dataFilterService, principal, overseerProcessDefinitionKeys, wrapWithForm, includeData);
        TaskPageHandler pageHandler = new TaskPageHandler(rawQueryParameters, taskFilter, sanitizer, version){

            protected Map<String, ProcessDeployment> getDeploymentMap(Set<String> deploymentIds) {
                return deploymentService.getDeploymentMap(deploymentIds);
            }

            @Override
            protected Map<String, User> getUserMap(Set<String> userIds) {
                return identityService.findUsers(userIds);
            }

        };

        ProcessInstanceSearchCriteria executionCriteria = pageHandler.criteria(allowedProcesses);

        int firstResult = executionCriteria.getFirstResult() != null ? executionCriteria.getFirstResult() : 0;
        int maxResult = executionCriteria.getMaxResults() != null ? executionCriteria.getMaxResults() : 1000;

        Pageable pageable = new PageRequest(firstResult, maxResult, executionCriteria.getSort());
        Page<ProcessInstance> page = processInstanceRepository.findByCriteria(executionCriteria, pageable);

        SearchResults results = pageHandler.handle(page);

        if (LOG.isDebugEnabled())
            LOG.debug("Retrieved tasks in " + (System.currentTimeMillis() - time) + " ms");

        return results;
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
