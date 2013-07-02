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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import piecework.Registry;
import piecework.engine.*;
import piecework.engine.exception.ProcessEngineException;
import piecework.model.*;
import piecework.model.Process;
import piecework.process.ProcessInstanceSearchCriteria;
import piecework.task.TaskCriteria;
import piecework.task.TaskResults;

/**
 * @author James Renfro
 */
@Service
public class ProcessEngineRuntimeConcreteFacade implements ProcessEngineRuntimeFacade {

	@Autowired
    Registry registry;

    @Override
    public String start(piecework.model.Process process, String alias, Map<String, ?> data) throws ProcessEngineException {
        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, process.getEngine());
        return proxy.start(process, alias, data);
    }

    @Override
    public boolean cancel(Process process, ProcessInstance instance, String reason) throws ProcessEngineException {
        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, process.getEngine());
        return proxy.cancel(process, instance, reason);
    }

    @Override
    public boolean suspend(Process process, ProcessInstance instance, String reason) throws ProcessEngineException {
        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, process.getEngine());
        return proxy.suspend(process, instance, reason);
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
    public Task findTask(TaskCriteria criteria) throws ProcessEngineException {
        Task task = null;
        if (criteria.getProcesses() != null && !criteria.getProcesses().isEmpty()) {
            Set<String> engineSet = new HashSet<String>();
            for (Process process : criteria.getProcesses()) {
                if (process.getEngine() == null || engineSet.contains(process.getEngine()))
                    continue;
                engineSet.add(process.getEngine());
                ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, process.getEngine());
                task = proxy.findTask(criteria);

                if (task != null)
                    break;
            }

        }
        return task;
    }

    @Override
    public TaskResults findTasks(TaskCriteria criteria) throws ProcessEngineException {
        TaskResults.Builder builder = null;
        if (criteria.getProcesses() != null && !criteria.getProcesses().isEmpty()) {
            Set<String> engineSet = new HashSet<String>();
            for (Process process : criteria.getProcesses()) {
                if (process.getEngine() == null || engineSet.contains(process.getEngine()))
                    continue;
                engineSet.add(process.getEngine());
                ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, process.getEngine());
                TaskResults localResults = proxy.findTasks(criteria);
                if (localResults == null)
                    continue;
                if (builder == null)
                    builder = new TaskResults.Builder(localResults);
                else {
                    builder.tasks(localResults.getTasks());
                    builder.addToTotal(localResults.getTotal());
                }
            }
        } else {
            builder = new TaskResults.Builder();
        }
        return builder.build();
    }

    @Override
    public boolean completeTask(Process process, String taskId) throws ProcessEngineException {
        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, process.getEngine());
        if (proxy == null)
            throw new ProcessEngineException("Not found");
        return proxy.completeTask(process, taskId);
    }

    @Override
    public void deploy(Process process, String name, ProcessModelResource... resources) throws ProcessEngineException {
        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, process.getEngine());
        if (proxy == null)
            throw new ProcessEngineException("Not found");
        proxy.deploy(process, name, resources);
    }
}
