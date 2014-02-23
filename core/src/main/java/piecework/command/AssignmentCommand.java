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

import piecework.Constants;
import piecework.common.OperationResult;
import piecework.engine.ProcessEngineFacade;
import piecework.engine.exception.ProcessEngineException;
import piecework.enumeration.OperationType;
import piecework.exception.ForbiddenError;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessInstanceProvider;

/**
 * Assigns a task to a particular user or unassigns it (if a null assignee is passed)
 *
 * @author James Renfro
 */
public class AssignmentCommand extends AbstractOperationCommand {

    private final User assignee;

    AssignmentCommand(CommandExecutor commandExecutor, ProcessInstanceProvider instanceProvider, Task task, User assignee) {
        super(commandExecutor, instanceProvider, task, OperationType.ASSIGNMENT, null, null);
        this.task = task;
        this.assignee = assignee;
    }

    protected OperationResult operation(ProcessEngineFacade facade) throws PieceworkException, ProcessEngineException {
        Process process = modelProvider.process();
        boolean isAssignmentRestricted = process.isAssignmentRestrictedToCandidates();
        if (isAssignmentRestricted && assignee != null) {
//            Set<String> candidateAssigneeIds = task.getCandidateAssigneeIds();
//            if (!candidateAssigneeIds.contains(assignee.getUserId()) && (assignee.getAccessAuthority() == null || !assignee.getAccessAuthority().hasGroup(candidateAssigneeIds)))
            if (!task.isCandidateOrAssignee(assignee))
                throw new ForbiddenError(Constants.ExceptionCodes.invalid_assignment);
        }
        ProcessDeployment deployment = modelProvider.deployment();
        if (!task.isDisconnected() && !facade.assign(process, deployment, task.getTaskInstanceId(), assignee)) {
            throw new ForbiddenError(Constants.ExceptionCodes.invalid_assignment);
        }

        String displayName = assignee != null ? assignee.getDisplayName() : null;
        return new OperationResult(displayName, null, null, null, null);
    }

}
