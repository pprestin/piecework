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
import piecework.Constants;
import piecework.Versions;
import piecework.authorization.AuthorizationRole;
import piecework.common.PageHandler;
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
import piecework.task.TaskFilter;
import piecework.task.TaskPageHandler;
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
    DeploymentService deploymentService;

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

        Set<String> allProcessDefinitionKeys = Sets.union(overseerProcessDefinitionKeys, userProcessDefinitionKeys);
        Set<Process> allowedProcesses = processService.findProcesses(allProcessDefinitionKeys);

//        Set<Process> overseerProcesses = processService.findProcesses(overseerProcessDefinitionKeys);
//        Set<Process> userProcesses = Sets.difference(processService.findProcesses(userProcessDefinitionKeys), overseerProcesses);
//        Set<Process> allowedProcesses = Sets.union(overseerProcesses, userProcesses);

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

}
