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

import org.apache.log4j.Logger;
import piecework.common.OperationResult;
import piecework.engine.ProcessEngineFacade;
import piecework.engine.exception.ProcessEngineException;
import piecework.enumeration.OperationType;
import piecework.exception.PieceworkException;
import piecework.exception.StatusCodeError;
import piecework.manager.StorageManager;
import piecework.model.Process;
import piecework.model.*;

/**
 * @author James Renfro
 */
public abstract class AbstractOperationCommand extends AbstractEngineStorageCommand<ProcessInstance> {

    private static final Logger LOG = Logger.getLogger(AbstractOperationCommand.class);

    protected final OperationType operation;
    protected String applicationStatus;
    protected String applicationStatusExplanation;
    protected Task task;

    AbstractOperationCommand(CommandExecutor commandExecutor, Entity principal, Process process, ProcessInstance instance, Task task, OperationType operation, String applicationStatus, String applicationStatusExplanation) {
        super(commandExecutor, principal, process, instance);
        this.task = task;
        this.operation = operation;
        this.applicationStatus = applicationStatus;
        this.applicationStatusExplanation = applicationStatusExplanation;
    }

    /*
     * This method does all the work for the AbstractOperationCommand, but it's access is set to prevent developers
     * from calling it directly outside of this package -- only should be called directly in tests
     */
    ProcessInstance execute(ProcessEngineFacade processEngineFacade, StorageManager storageManager) throws PieceworkException {
        if (LOG.isDebugEnabled())
            LOG.debug("Executing instance state command " + this.toString());

        ProcessInstance updated = null;
        try {
            OperationResult result = operation(processEngineFacade);
            updated = storageManager.store(operation, result, instance, principal);

            if (LOG.isDebugEnabled())
                LOG.debug("Executed instance state command " + this.toString());

        } catch (ProcessEngineException e) {
            LOG.error("Process engine unable to cancel execution ", e);
            throw new PieceworkException("Unable to cancel execution");
        }

        return updated;
    }

    public String toString() {
        String processDefinitionKey = process != null ? process.getProcessDefinitionKey() : "";
        String processInstanceId = instance != null ? instance.getProcessInstanceId() : "";

        return "{ processDefinitionKey: \"" + processDefinitionKey + "\", processInstanceId: \"" + processInstanceId + "\", operation: \"" + operation + "\" }";
    }

    protected abstract OperationResult operation(ProcessEngineFacade facade) throws StatusCodeError, ProcessEngineException;

}
