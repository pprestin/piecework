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
import piecework.engine.ProcessEngineFacade;
import piecework.engine.exception.ProcessEngineException;
import piecework.enumeration.ActionType;
import piecework.exception.InternalServerError;
import piecework.exception.StatusCodeError;
import piecework.model.*;
import piecework.model.Process;
import piecework.CommandExecutor;

/**
 * @author James Renfro
 */
public class TaskCommand implements Command<Boolean> {

    private static final Logger LOG = Logger.getLogger(TaskCommand.class);

    private final Process process;
    private final Task task;
    private final ActionType action;

    public TaskCommand(piecework.model.Process process, Task task, ActionType action) {
        this.process = process;
        this.task = task;
        this.action = action;
    }

    @Override
    public Boolean execute(CommandExecutor commandExecutor) throws StatusCodeError {
        ProcessEngineFacade facade = commandExecutor.getFacade();

        boolean result = false;
        String taskId = task != null ? task.getTaskInstanceId() : null;
        if (StringUtils.isNotEmpty(taskId)) {
            try {
                result = facade.completeTask(process, taskId, action);
            } catch (ProcessEngineException e) {
                LOG.error(e);
                throw new InternalServerError(e);
            }
        }

        return Boolean.valueOf(result);
    }

}
