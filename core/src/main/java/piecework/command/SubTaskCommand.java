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
import piecework.exception.PieceworkException;
import piecework.exception.StatusCodeError;
import piecework.manager.StorageManager;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.DeploymentRepository;
import piecework.persistence.ProcessInstanceRepository;
import piecework.task.TaskCriteria;
import piecework.validation.Validation;
import piecework.validation.Validation;

public class SubTaskCommand extends AbstractEngineStorageCommand<ProcessInstance> {

    private static final Logger LOG = Logger.getLogger(SubTaskCommand.class);

    private final Process process;
    private final ProcessInstance instance;
    private final String parentTaskId;
    private final Validation validation;
    private final CommandExecutor executor;
    private final ProcessDeployment deployment;
    private final Entity principal;

    public SubTaskCommand(CommandExecutor executor, Entity principal, Process process, ProcessInstance instance, ProcessDeployment deployment, String parentTaskId, Validation validation) {
        super(executor, principal, validation.getProcess());
        this.principal = principal;
        this.executor = executor;
        this.process = process;
        this.instance = instance;
        this.parentTaskId = parentTaskId;
        this.validation = validation;
        this.deployment = deployment;
    }

    @Override
    ProcessInstance execute(final ProcessEngineFacade processEngineFacade, final StorageManager storageManager) throws PieceworkException {

        Task result = null;
        ProcessInstance iresult = null;
        if (StringUtils.isNotEmpty(parentTaskId)) {
            try {
                result = processEngineFacade.createSubTask(process, deployment, parentTaskId, instance, validation);

                ProcessInstance.Builder builder = new ProcessInstance.Builder(instance);
                builder.task(result);

                iresult = storageManager.store(builder.build());

                if (result == null || iresult == null) {
                    throw new InternalServerError(Constants.ExceptionCodes.subtask_create_invalid);
                }
            } catch (ProcessEngineException e) {
                LOG.error(e);
                throw new InternalServerError(e);
            }
        }

        return instance;

    }

    public String getProcessDefinitionKey() {
        return process != null ? process.getProcessDefinitionKey() : null;
    }
}
