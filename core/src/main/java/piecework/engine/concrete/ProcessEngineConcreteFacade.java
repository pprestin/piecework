/*
 * Copyright 2012 University of Washington
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
package piecework.engine.concrete;

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.common.Registry;
import piecework.content.ContentResource;
import piecework.engine.ProcessEngineFacade;
import piecework.engine.ProcessEngineProxy;
import piecework.engine.ProcessExecutionResults;
import piecework.engine.exception.ProcessEngineException;
import piecework.enumeration.ActionType;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.TaskProvider;
import piecework.common.SearchCriteria;
import piecework.repository.ProcessInstanceRepository;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.task.TaskCriteria;
import piecework.task.TaskResults;
import piecework.validation.Validation;

import java.util.*;

/**
 * @author James Renfro
 */
@Service
public class ProcessEngineConcreteFacade implements ProcessEngineFacade {

    private static final Logger LOG = Logger.getLogger(ProcessEngineConcreteFacade.class);

	@Autowired
    Registry registry;

    @Autowired
    ProcessInstanceRepository processInstanceRepository;

    @Override
    public String start(Process process, ProcessDeployment deployment, ProcessInstance instance) throws ProcessEngineException {
        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, deployment.getEngine());
        return proxy.start(process, deployment, instance);
    }

    @Override
    public boolean activate(Process process, ProcessDeployment deployment, ProcessInstance instance) throws ProcessEngineException {
        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, deployment.getEngine());
        return proxy.activate(process, deployment, instance);
    }

    @Override
    public boolean assign(Process process, ProcessDeployment deployment, String taskId, User user) throws ProcessEngineException {
        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, deployment.getEngine());
        return proxy.assign(process, deployment, taskId, user);
    }

    @Override
    public boolean cancel(Process process, ProcessDeployment deployment, ProcessInstance instance) throws ProcessEngineException {
        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, deployment.getEngine());
        return proxy.cancel(process, deployment, instance);
    }

    @Override
    public boolean suspend(Process process, ProcessDeployment deployment, ProcessInstance instance) throws ProcessEngineException {
        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, deployment.getEngine());
        return proxy.suspend(process, deployment, instance);
    }

    @Override
    public ProcessExecution findExecution(SearchCriteria criteria) throws ProcessEngineException {
        ProcessExecution execution = null;
        if (criteria.getEngines() != null && !criteria.getEngines().isEmpty()) {
            for (String engine : criteria.getEngines()) {
                ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, engine);
                execution = proxy.findExecution(criteria);

                if (execution != null)
                    break;
            }

        }
        return execution;
    }

    @Override
    public ProcessExecutionResults findExecutions(SearchCriteria criteria) throws ProcessEngineException {
        ProcessExecutionResults.Builder builder = null;
        if (criteria.getEngines() != null && !criteria.getEngines().isEmpty()) {
            for (String engine : criteria.getEngines()) {
                ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, engine);
                ProcessExecutionResults localResults = proxy.findExecutions(criteria);
                if (localResults == null)
                    continue;
                if (builder == null)
                    builder = new ProcessExecutionResults.Builder(localResults);
                else {
                    builder.executions(localResults.getExecutions());
                    builder.addToTotal(localResults.getTotal());
                }
            }

        } else {
            builder = new ProcessExecutionResults.Builder();
        }
        return builder.build();
    }

    @Override
    public Task findTask(Process process, ProcessDeployment deployment, String taskId, boolean limitToActive) throws ProcessEngineException {

        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, deployment.getEngine());
        Task task = proxy.findTask(process, deployment, taskId, limitToActive);

        if (task != null) {
            ProcessInstance instance = processInstanceRepository.findByProcessDefinitionKeyAndEngineProcessInstanceId(process.getProcessDefinitionKey(), task.getEngineProcessInstanceId());
            if (instance != null)
                return new Task.Builder(task, new PassthroughSanitizer())
                        .processInstanceId(instance.getProcessInstanceId())
                        .processInstanceAlias(instance.getAlias())
                        .processInstanceLabel(instance.getProcessInstanceLabel())
                        .build();
        }

        return null;
    }

    @Override
    public TaskResults findTasks(TaskCriteria ... criterias) throws ProcessEngineException {
        String keyword = null;
        TaskResults.Builder builder = null;
        Set<String> allowedProcessDefinitionKeys = new HashSet<String>();
        Set<String> engineProcessInstanceIds = new HashSet<String>();

        if (criterias != null && criterias.length > 0) {
            for (TaskCriteria criteria : criterias) {
                if (StringUtils.isNotEmpty(criteria.getKeyword()))
                    keyword = criteria.getKeyword();

                if (criteria.getProcesses() != null && !criteria.getProcesses().isEmpty()) {
                    Set<String> engineSet = new HashSet<String>();
                    for (Process process : criteria.getProcesses()) {
                        ProcessDeployment deployment = processDeployment(process);
                        allowedProcessDefinitionKeys.add(process.getProcessDefinitionKey());
                        if (deployment.getEngine() == null || engineSet.contains(deployment.getEngine()))
                            continue;
                        engineSet.add(deployment.getEngine());
                        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, deployment.getEngine());

                        TaskResults localResults = proxy.findTasks(criteria);
                        if (localResults == null)
                            continue;

                        engineProcessInstanceIds.addAll(localResults.getEngineProcessInstanceIds());

                        if (builder == null)
                            builder = new TaskResults.Builder(localResults);
                        else {
                            builder.tasks(localResults.getTasks());
                            builder.addToTotal(localResults.getTotal());
                        }
                    }
                }
            }
        } else {
            builder = new TaskResults.Builder();
        }

        long time = 0;
        if (LOG.isDebugEnabled())
            time = System.currentTimeMillis();

        TaskResults.Builder resultsBuilder = new TaskResults.Builder();

        List<Task> taskInstances = builder != null ? builder.build().getTasks() : null;
        List<Task> tasks;
        int count = 0;

        if (taskInstances != null && !taskInstances.isEmpty()) {
            tasks = new ArrayList<Task>(taskInstances.size());

            List<ProcessInstance> processInstances;

            if (StringUtils.isNotEmpty(keyword))
                processInstances = processInstanceRepository.findByProcessDefinitionKeyInAndEngineProcessInstanceIdInAndKeyword(allowedProcessDefinitionKeys, engineProcessInstanceIds, keyword);
            else
                processInstances = processInstanceRepository.findByProcessDefinitionKeyInAndEngineProcessInstanceIdIn(allowedProcessDefinitionKeys, engineProcessInstanceIds);

            if (processInstances != null && !processInstances.isEmpty()) {

                MultiKeyMap processInstanceMap = new MultiKeyMap();
                for (ProcessInstance processInstance : processInstances) {
                    if (processInstance == null)
                        continue;
                    if (org.apache.cxf.common.util.StringUtils.isEmpty(processInstance.getProcessDefinitionKey()))
                        continue;
                    if (org.apache.cxf.common.util.StringUtils.isEmpty(processInstance.getEngineProcessInstanceId()))
                        continue;

                    processInstanceMap.put(processInstance.getProcessDefinitionKey(), processInstance.getEngineProcessInstanceId(), processInstance);
                }
                for (Task taskInstance : taskInstances) {
                    ProcessInstance instance = ProcessInstance.class.cast(processInstanceMap.get(taskInstance.getProcessDefinitionKey(), taskInstance.getEngineProcessInstanceId()));
                    if (instance == null)
                        continue;

                    tasks.add(new Task.Builder(taskInstance, new PassthroughSanitizer())
                            .processInstanceId(instance.getProcessInstanceId())
                            .processInstanceAlias(instance.getAlias())
                            .processInstanceLabel(instance.getProcessInstanceLabel())
                            .build());
                    count++;
                }
            }
        } else {
            tasks = Collections.emptyList();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Searching for process instances took " + (System.currentTimeMillis() - time) + " ms");
        }

        resultsBuilder.firstResult(1);
        resultsBuilder.maxResults(count);
        resultsBuilder.total(count);
        resultsBuilder.tasks(tasks);

        return resultsBuilder.build();
    }

    @Override
    public boolean completeTask(Process process, ProcessDeployment deployment, String taskId, ActionType action, Validation validation, Entity principal) throws ProcessEngineException {
        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, deployment.getEngine());
        if (proxy == null)
            throw new ProcessEngineException("Not found");
        return proxy.completeTask(process, deployment, taskId, action, validation, principal);
    }

    @Override
    public Task createSubTask(TaskProvider taskProvider, Validation validation) throws PieceworkException {
        ProcessDeployment deployment  = taskProvider.deployment();
        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, deployment.getEngine());
        if(proxy == null)
            throw new ProcessEngineException("Not Found");

        return proxy.createSubTask(taskProvider, validation);
    }

    @Override
    public ProcessDeployment deploy(Process process, ProcessDeployment deployment, ContentResource contentResource) throws ProcessEngineException {
        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, deployment.getEngine());
        if (proxy == null)
            throw new ProcessEngineException("Not found");
        return proxy.deploy(process, deployment, contentResource);
    }

    @Override
    public ContentResource resource(Process process, ProcessDeployment deployment, String contentType) throws ProcessEngineException {
        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, deployment.getEngine());
        if (proxy == null)
            throw new ProcessEngineException("Not found");
        return proxy.resource(process, deployment, contentType);
    }

    @Override
    public ContentResource resource(Process process, ProcessDeployment deployment, ProcessInstance instance, String contentType) throws ProcessEngineException {
        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, deployment.getEngine());
        if (proxy == null)
            throw new ProcessEngineException("Not found");
        return proxy.resource(process, deployment, instance, contentType);
    }

    private ProcessDeployment processDeployment(Process process) throws ProcessEngineException {
        ProcessDeployment deployment = process.getDeployment();
        if (deployment == null)
            throw new ProcessEngineException("Unable to retrieve deployment");
        return deployment;
    }
}
