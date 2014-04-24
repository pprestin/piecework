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
package piecework.engine;

import org.apache.commons.lang.StringUtils;
import piecework.common.SearchCriteria;
import piecework.content.ContentResource;
import piecework.engine.exception.ProcessEngineException;
import piecework.enumeration.ActionType;
import piecework.enumeration.FlowElementType;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.TaskProvider;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.task.TaskCriteria;
import piecework.task.TaskResults;
import piecework.validation.Validation;

import java.util.Collection;
import java.util.Map;

/**
 * @author James Renfro
 */
public class ProcessEngineFacadeStub implements ProcessEngineFacade {

    @Override
    public String start(piecework.model.Process process, ProcessDeployment deployment, ProcessInstance instance) throws ProcessEngineException {
        return null;
    }

    @Override
    public boolean activate(Process process, ProcessDeployment deployment, ProcessInstance instance) throws ProcessEngineException {
        return false;
    }

    @Override
    public boolean assign(Process process, ProcessDeployment deployment, String taskId, User user) throws ProcessEngineException {
        return false;
    }

    @Override
    public boolean cancel(Process process, ProcessDeployment deployment, ProcessInstance instance) throws ProcessEngineException {
        return false;
    }

    @Override
    public boolean suspend(Process process, ProcessDeployment deployment, ProcessInstance instance) throws ProcessEngineException {
        return false;
    }

    @Override
    public ProcessExecution findExecution(SearchCriteria criteria) throws ProcessEngineException {
        return null;
    }

    @Override
    public ProcessExecutionResults findExecutions(SearchCriteria criteria) throws ProcessEngineException {
        return null;
    }

    @Override
    public Task findTask(Process process, ProcessDeployment deployment, String taskId, boolean limitToActive) throws ProcessEngineException {
        return null;
    }

    @Override
    public TaskResults findTasks(TaskCriteria... criterias) throws ProcessEngineException {
        return null;
    }

    @Override
    public boolean completeTask(Process process, ProcessDeployment deployment, String taskId, ActionType action, Validation validation, Entity principal) throws ProcessEngineException {
        return false;
    }

    @Override
    public Task createSubTask(TaskProvider taskProvider, Validation validation) throws PieceworkException {
        return null;
    }

    @Override
    public ProcessDeployment deploy(Process process, ProcessDeployment deployment, ContentResource contentResource) throws ProcessEngineException {
        ProcessDeployment.Builder updated = new ProcessDeployment.Builder(deployment, new PassthroughSanitizer(), true);

        updated.deploy();

        return updated.build();
    }

    @Override
    public ContentResource resource(Process process, ProcessDeployment deployment, String contentType) throws ProcessEngineException {
        return null;
    }

    @Override
    public ContentResource resource(Process process, ProcessDeployment deployment, ProcessInstance instance, String contentType) throws ProcessEngineException {
        return null;
    }
}
