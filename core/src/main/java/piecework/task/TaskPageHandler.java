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

import org.apache.cxf.common.util.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import piecework.Constants;
import piecework.common.PageHandler;
import piecework.common.ViewContext;
import piecework.model.*;
import piecework.model.Process;
import piecework.common.SearchCriteria;
import piecework.security.Sanitizer;
import piecework.security.concrete.PassthroughSanitizer;

import java.util.*;

/**
 * @author James Renfro
 */
public abstract class TaskPageHandler implements PageHandler<ProcessInstance> {

//    private final MultivaluedMap<String, String> rawQueryParameters;
//    private final SearchQueryParameters queryParameters;
    private final String processStatus;
    private final String taskStatus;
    private final TaskFilter taskFilter;
    private final PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
    private final Sanitizer sanitizer;
    private final ViewContext version;

//    private SearchCriteria executionCriteria;
//    private SearchResults.Builder resultsBuilder;

    public TaskPageHandler(SearchCriteria criteria, TaskFilter taskFilter, Sanitizer sanitizer, ViewContext version) {
//        this.rawQueryParameters = rawQueryParameters;
        this.processStatus = criteria.getProcessStatus() != null ? sanitizer.sanitize(criteria.getProcessStatus()) : Constants.ProcessStatuses.OPEN;
        this.taskStatus = criteria.getTaskStatus() != null ? sanitizer.sanitize(criteria.getTaskStatus()) : Constants.TaskStatuses.ALL;

        this.taskFilter = taskFilter;
        this.sanitizer = sanitizer;
        this.version = version;
    }

//    public SearchCriteria criteria(Set<Process> allowedProcesses) {
//        resultsBuilder = new SearchResults.Builder()
//                .resourceLabel("Tasks")
//                .resourceName(Form.Constants.ROOT_ELEMENT_NAME)
//                .link(version.getApplicationUri());
//
//        if (taskFilter.isWrapWithForm())
//            resultsBuilder.resourceName(Form.Constants.ROOT_ELEMENT_NAME)
//                    .link(version.getApplicationUri(Form.Constants.ROOT_ELEMENT_NAME));
//
//        SearchCriteria.Builder executionCriteriaBuilder =
//                new SearchCriteria.Builder(rawQueryParameters, sanitizer);
//
//        addDefinitions(executionCriteriaBuilder, resultsBuilder, allowedProcesses);
//
//        this.executionCriteria = executionCriteriaBuilder.build();
//        resultsBuilder.parameters(executionCriteria.getSanitizedParameters());
//        return this.executionCriteria;
//    }

    public SearchResults handle(Page<ProcessInstance> page, Pageable pageable, Set<Process> allowedProcesses) {
        SearchResults.Builder resultsBuilder = new SearchResults.Builder()
                .resourceLabel("Tasks")
                .resourceName(Form.Constants.ROOT_ELEMENT_NAME)
                .link(version.getApplicationUri());

        if (allowedProcesses == null || allowedProcesses.isEmpty())
            return resultsBuilder.build(version);

        List<Process> alphabetical = new ArrayList<Process>(allowedProcesses);
        Collections.sort(alphabetical, new Comparator<Process>() {
            @Override
            public int compare(Process o1, Process o2) {
                if (org.apache.commons.lang.StringUtils.isEmpty(o1.getProcessDefinitionLabel()))
                    return 0;
                if (org.apache.commons.lang.StringUtils.isEmpty(o2.getProcessDefinitionLabel()))
                    return 1;
                return o1.getProcessDefinitionLabel().compareTo(o2.getProcessDefinitionLabel());
            }
        });
        for (Process allowedProcess : alphabetical) {
            if (allowedProcess.getProcessDefinitionKey() != null) {
                Process definition = new Process.Builder(allowedProcess, sanitizer).build(version);
                if (taskFilter.isWrapWithForm()) {
                    resultsBuilder.definition(new Form.Builder().processDefinitionKey(definition.getProcessDefinitionKey()).task(new Task.Builder().processDefinitionKey(definition.getProcessDefinitionKey()).processDefinitionLabel(definition.getProcessDefinitionLabel()).build(version)).build(version));
                } else {
                    resultsBuilder.definition(definition);
                }
            }
        }

        if (page.hasContent()) {
            int count = 0;

            // Loop once through list to get the deployment ids
            Set<String> deploymentIds = taskFilter.getDeploymentIds(page.getContent());

            // Retrieve a map of deployment objects from Mongo
//            Map<String, ProcessDeployment> deploymentMap = getDeploymentMap(deploymentIds);

            List<TaskDeployment> rawTasks = new ArrayList<TaskDeployment>();
            Set<String> userIds = new HashSet<String>();

            // Loop again through the list to get all user ids and build the intermediate object including
            // task, instance, and deployment
            for (ProcessInstance instance : page.getContent()) {
                ProcessDeployment processDeployment = null;
//                if (taskFilter.isWrapWithForm())
//                    processDeployment = deploymentMap.get(instance.getDeploymentId());

                Set<Task> tasks = instance.getTasks();
                if (tasks != null && !tasks.isEmpty()) {
                    for (Task task : tasks) {
                        if (taskFilter.include(task, processStatus, taskStatus)) {
                            rawTasks.add(new TaskDeployment(0, processDeployment, instance, task));
                            userIds.addAll(task.getAssigneeAndCandidateAssigneeIds());
                        }
                    }
                }
            }

            Map<String, User> userMap = getUserMap(userIds);

            for (TaskDeployment rawTask : rawTasks) {
                resultsBuilder.item(taskFilter.result(rawTask, userMap, version));
                count++;
            }

            resultsBuilder.firstResult(pageable.getOffset());
            resultsBuilder.maxResults(pageable.getPageSize());
            resultsBuilder.total(Long.valueOf(count));
        }
        if (taskFilter.getPrincipal() != null && taskFilter.getPrincipal() instanceof User)
            resultsBuilder.currentUser(User.class.cast(taskFilter.getPrincipal()));

        return resultsBuilder.build(version);
    }

//    protected abstract Map<String, ProcessDeployment> getDeploymentMap(Set<String> deploymentIds);

    protected abstract Map<String, User> getUserMap(Set<String> userIds);

    private void addDefinitions(SearchCriteria.Builder executionCriteriaBuilder, SearchResults.Builder resultsBuilder, Set<Process> allowedProcesses) {
        if (allowedProcesses == null || allowedProcesses.isEmpty())
            return;

        List<Process> alphabetical = new ArrayList<Process>(allowedProcesses);
        Collections.sort(alphabetical, new Comparator<Process>() {
            @Override
            public int compare(Process o1, Process o2) {
                if (StringUtils.isEmpty(o1.getProcessDefinitionLabel()))
                    return 0;
                if (StringUtils.isEmpty(o2.getProcessDefinitionLabel()))
                    return 1;
                return o1.getProcessDefinitionLabel().compareTo(o2.getProcessDefinitionLabel());
            }
        });
        for (Process allowedProcess : alphabetical) {
            if (allowedProcess.getProcessDefinitionKey() != null) {
                executionCriteriaBuilder.processDefinitionKey(allowedProcess.getProcessDefinitionKey());
                Process definition = new Process.Builder(allowedProcess, passthroughSanitizer).build(version);
                if (taskFilter.isWrapWithForm()) {
                    resultsBuilder.definition(new Form.Builder().processDefinitionKey(definition.getProcessDefinitionKey()).task(new Task.Builder().processDefinitionKey(definition.getProcessDefinitionKey()).processDefinitionLabel(definition.getProcessDefinitionLabel()).build(version)).build(version));
                } else {
                    resultsBuilder.definition(definition);
                }
            }
        }
    }
}
