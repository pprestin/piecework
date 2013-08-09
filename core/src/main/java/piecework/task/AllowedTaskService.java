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
package piecework.task;

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.authorization.AuthorizationRole;
import piecework.common.ViewContext;
import piecework.engine.ProcessEngineFacade;
import piecework.engine.exception.ProcessEngineException;
import piecework.exception.ForbiddenError;
import piecework.exception.InternalServerError;
import piecework.exception.StatusCodeError;
import piecework.identity.InternalUserDetails;
import piecework.model.*;
import piecework.model.Process;
import piecework.process.concrete.ResourceHelper;
import piecework.security.Sanitizer;
import piecework.security.concrete.PassthroughSanitizer;

import javax.ws.rs.core.MultivaluedMap;
import java.util.List;
import java.util.Set;

/**
 * @author James Renfro
 */
@Service
public class AllowedTaskService {

    private static final Logger LOG = Logger.getLogger(AllowedTaskService.class);

    @Autowired
    Environment environment;

    @Autowired
    ResourceHelper helper;

    @Autowired
    ProcessEngineFacade facade;

    @Autowired
    Sanitizer sanitizer;

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

            return facade.findTask(criteriaBuilder.build());
        } catch (ProcessEngineException e) {
            LOG.error(e);
            throw new InternalServerError();
        }
    }

    public Task allowedTask(Process process, ProcessInstance processInstance, boolean limitToActive) throws StatusCodeError {
        TaskCriteria.Builder taskCriteria = new TaskCriteria.Builder()
                .process(process)
                .executionId(processInstance.getEngineProcessInstanceId())
                .orderBy(TaskCriteria.OrderBy.CREATED_TIME_ASC);

        if (! helper.isAuthenticatedSystem() && !helper.hasRole(process, AuthorizationRole.OVERSEER)) {
            // If the call is not being made by an authenticated system, then the principal is a user and must have an active task
            // on this instance
            InternalUserDetails user = helper.getAuthenticatedPrincipal();

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
        Set<Process> overseerProcesses = helper.findProcesses(AuthorizationRole.OVERSEER);
        Set<Process> userProcesses = Sets.difference(helper.findProcesses(AuthorizationRole.USER), overseerProcesses);
        Set<Process> allowedProcesses = Sets.union(overseerProcesses, userProcesses);

        ViewContext taskViewContext = getTaskViewContext();

        SearchResults.Builder resultsBuilder = new SearchResults.Builder()
                .resourceLabel("Tasks")
                .resourceName(Form.Constants.ROOT_ELEMENT_NAME)
                .link(taskViewContext.getApplicationUri());

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

        return resultsBuilder.build();
    }

    public boolean hasAllowedTask(Process process, ProcessInstance processInstance, boolean limitToActive) throws StatusCodeError {

        if (allowedTask(process, processInstance, limitToActive) != null)
            return true;

        return false;
    }

    public ViewContext getTaskViewContext() {
        String baseApplicationUri = environment.getProperty("base.application.uri");
        String baseServiceUri = environment.getProperty("base.service.uri");
        return new ViewContext(baseApplicationUri, baseServiceUri, getVersion(), Task.Constants.ROOT_ELEMENT_NAME, Task.Constants.RESOURCE_LABEL);
    }

    public String getVersion() {
        return "v1";
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
