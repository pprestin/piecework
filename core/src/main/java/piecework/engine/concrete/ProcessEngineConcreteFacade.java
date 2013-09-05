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

import java.util.*;

import com.google.common.collect.Sets;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import piecework.Constants;
import piecework.Registry;
import piecework.engine.*;
import piecework.engine.exception.ProcessEngineException;
import piecework.enumeration.ActionType;
import piecework.exception.NotFoundError;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessInstanceRepository;
import piecework.process.ProcessInstanceSearchCriteria;
import piecework.process.ProcessInstanceService;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.task.TaskCriteria;
import piecework.task.TaskResults;
import piecework.util.ManyMap;

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
    public String start(piecework.model.Process process, String alias, Map<String, ?> data) throws ProcessEngineException {
        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, process.getEngine());
        return proxy.start(process, alias, data);
    }

    @Override
    public boolean activate(Process process, ProcessInstance instance) throws ProcessEngineException {
        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, process.getEngine());
        return proxy.activate(process, instance);
    }

    @Override
    public boolean assign(Process process, String taskId, User user) throws ProcessEngineException {
        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, process.getEngine());
        return proxy.assign(process, taskId, user);
    }

    @Override
    public boolean cancel(Process process, ProcessInstance instance) throws ProcessEngineException {
        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, process.getEngine());
        return proxy.cancel(process, instance);
    }

    @Override
    public boolean suspend(Process process, ProcessInstance instance) throws ProcessEngineException {
        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, process.getEngine());
        return proxy.suspend(process, instance);
    }

    @Override
    public ProcessExecution findExecution(ProcessInstanceSearchCriteria criteria) throws ProcessEngineException {
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
    public ProcessExecutionResults findExecutions(ProcessInstanceSearchCriteria criteria) throws ProcessEngineException {
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
    public Task findTask(TaskCriteria ... criterias) throws ProcessEngineException {
        if (criterias != null && criterias.length > 0) {
            for (TaskCriteria criteria : criterias) {
                if (criteria.getProcesses() != null && !criteria.getProcesses().isEmpty()) {
                    Set<String> engineSet = new HashSet<String>();
                    for (Process process : criteria.getProcesses()) {
                        if (process.getEngine() == null || engineSet.contains(process.getEngine()))
                            continue;
                        engineSet.add(process.getEngine());
                        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, process.getEngine());
                        Task task = proxy.findTask(criteria);

                        if (task != null) {
                            ProcessInstance instance = processInstanceRepository.findByProcessDefinitionKeyAndEngineProcessInstanceId(process.getProcessDefinitionKey(), task.getEngineProcessInstanceId());
                            if (instance == null)
                                continue;

                            return new Task.Builder(task, new PassthroughSanitizer())
                                    .processInstanceId(instance.getProcessInstanceId())
                                    .processInstanceAlias(instance.getAlias())
                                    .processInstanceLabel(instance.getProcessInstanceLabel())
                                    .build();
                        }
                    }
                }
            }
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
                        allowedProcessDefinitionKeys.add(process.getProcessDefinitionKey());
                        if (process.getEngine() == null || engineSet.contains(process.getEngine()))
                            continue;
                        engineSet.add(process.getEngine());
                        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, process.getEngine());

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
    public boolean completeTask(Process process, String taskId, ActionType action) throws ProcessEngineException {
        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, process.getEngine());
        if (proxy == null)
            throw new ProcessEngineException("Not found");
        return proxy.completeTask(process, taskId, action);
    }

    @Override
    public void deploy(Process process, String name, ProcessModelResource... resources) throws ProcessEngineException {
        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, process.getEngine());
        if (proxy == null)
            throw new ProcessEngineException("Not found");
        proxy.deploy(process, name, resources);
    }
}
