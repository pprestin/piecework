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

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import piecework.Registry;
import piecework.engine.*;
import piecework.engine.exception.ProcessEngineException;
import piecework.model.*;
import piecework.model.Process;

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
    public boolean cancel(Process process, String processInstanceId, String alias, String reason) throws ProcessEngineException {
        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, process.getEngine());
        return proxy.cancel(process, processInstanceId, alias, reason);
    }

    @Override
    public ProcessExecution findExecution(ProcessExecutionCriteria criteria) throws ProcessEngineException {
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
    public ProcessExecutionResults findExecutions(ProcessExecutionCriteria criteria) throws ProcessEngineException {
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
        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, criteria.getEngine());
        return proxy.findTask(criteria);
    }

    @Override
    public TaskResults findTasks(TaskCriteria criteria) throws ProcessEngineException {
        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, criteria.getEngine());
        return proxy.findTasks(criteria);
    }

    @Override
    public boolean completeTask(Process process, String taskId) throws ProcessEngineException {
        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, process.getEngine());
        return proxy.completeTask(process, taskId);
    }

}
