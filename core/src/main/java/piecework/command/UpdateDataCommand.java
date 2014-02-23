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
import piecework.authorization.AuthorizationRole;
import piecework.engine.ProcessEngineFacade;
import piecework.enumeration.ActionType;
import piecework.exception.ForbiddenError;
import piecework.exception.PieceworkException;
import piecework.manager.StorageManager;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessProvider;
import piecework.submission.*;
import piecework.util.ModelUtility;

import java.util.List;
import java.util.Map;

/**
 * Internal command for updating a process instance directly.
 *
 * @author James Renfro
 */
public class UpdateDataCommand<P extends ProcessProvider> extends AbstractEngineStorageCommand<ProcessInstance, P> {

    private final Map<String, List<Value>> data;
    private final Map<String, List<Message>> messages;
    private final String applicationStatusExplanation;

    UpdateDataCommand(CommandExecutor commandExecutor, P modelProvider, Map<String, List<Value>> data, Map<String, List<Message>> messages, String applicationStatusExplanation) {
        super(commandExecutor, modelProvider);
        this.data = data;
        this.messages = messages;
        this.applicationStatusExplanation = applicationStatusExplanation;
    }

    @Override
    ProcessInstance execute(final ProcessEngineFacade processEngineFacade, final StorageManager storageManager) throws PieceworkException {
        // This is an operation that anonymous users should never be able to cause
        Entity principal = modelProvider.principal();
        if (principal == null)
            throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);

        ActionType actionType = ActionType.UPDATE;

        // Users are only allowed to complete tasks if they have been assigned a task, and
        // only process overseers or superusers are allowed to complete tasks otherwise
        Process process = modelProvider.process();
        if (!principal.hasRole(process, AuthorizationRole.OVERSEER, AuthorizationRole.SUPERUSER)) {
            Task task = ModelUtility.allowedTask(modelProvider);
            if (task == null || !task.isCandidateOrAssignee(principal) || !principal.hasRole(process, AuthorizationRole.USER))
                throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);
        }

        Submission submission = SubmissionFactory.submission(actionType, process.getProcessDefinitionKey(), null, null, data, null, principal);
        ProcessInstance instance = ModelUtility.instance(modelProvider);
        return storageManager.store(instance, data, messages, submission, applicationStatusExplanation);
    }

}
