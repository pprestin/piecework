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
package piecework.command;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import piecework.Command;
import piecework.Constants;
import piecework.engine.ProcessEngineFacade;
import piecework.engine.exception.ProcessEngineException;
import piecework.enumeration.ActionType;
import piecework.exception.ConflictError;
import piecework.exception.InternalServerError;
import piecework.exception.StatusCodeError;
import piecework.model.*;
import piecework.model.Process;
import piecework.CommandExecutor;
import piecework.persistence.DeploymentRepository;
import piecework.persistence.ProcessInstanceRepository;
import piecework.task.TaskCriteria;
import piecework.validation.FormValidation;

/**
 * @author James Renfro
 */
public class TaskCommand implements Command<Boolean> {

    private static final Logger LOG = Logger.getLogger(TaskCommand.class);

    private final Process process;
    private final ProcessInstance instance;
    private final Task task;
    private final ActionType action;
    private final FormValidation validation;

    public TaskCommand(Process process, ProcessInstance instance, Task task, ActionType action, FormValidation validation) {
        this.process = process;
        this.instance = instance;
        this.task = task;
        this.action = action;
        this.validation = validation;
    }

    @Override
    public Boolean execute(CommandExecutor commandExecutor) throws StatusCodeError {
        ProcessEngineFacade facade = commandExecutor.getFacade();
        DeploymentRepository deploymentRepository = commandExecutor.getDeploymentRepository();

        ProcessDeployment deployment = process.getDeployment();
        if (instance.getDeploymentId() != null && !instance.getDeploymentId().equals(process.getDeploymentId()))
            deployment = deploymentRepository.findOne(instance.getDeploymentId());

        if (deployment == null)
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);

        boolean result = false;
        String taskId = task != null ? task.getTaskInstanceId() : null;
        if (StringUtils.isNotEmpty(taskId)) {
            try {
                result = facade.completeTask(process, deployment, taskId, action, validation);

                if (result == false) {
                    // It's possible that the system has gotten out of sync -- grab a current instance of the task from the engine
                    // and overwrite the one that's stored in mongo
                    Task task = facade.findTask(process, deployment, taskId, false);
                    if (task != null) {
                        ProcessInstanceRepository processInstanceRepository = commandExecutor.getProcessInstanceRepository();
                        processInstanceRepository.update(instance.getProcessInstanceId(), task);
                    }
                    throw new ConflictError(Constants.ExceptionCodes.active_task_required);
                }
            } catch (ProcessEngineException e) {
                LOG.error(e);
                throw new InternalServerError(e);
            }
        }

        return Boolean.valueOf(result);
    }

    public String getProcessDefinitionKey() {
        return process != null ? process.getProcessDefinitionKey() : null;
    }

}
