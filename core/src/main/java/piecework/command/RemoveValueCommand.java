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
import piecework.Constants;
import piecework.authorization.AuthorizationRole;
import piecework.common.ManyMap;
import piecework.engine.ProcessEngineFacade;
import piecework.enumeration.ActionType;
import piecework.exception.ForbiddenError;
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.manager.StorageManager;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.AllowedTaskProvider;
import piecework.submission.SubmissionFactory;
import piecework.util.Base64Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Removes a particular value (probably a file) from the process instance
 * data.
 *
 * @author James Renfro
 */
public class RemoveValueCommand extends AbstractEngineStorageCommand<ProcessInstance, AllowedTaskProvider> {

    private final String fieldName;
    private final String valueId;

    RemoveValueCommand(CommandExecutor commandExecutor, AllowedTaskProvider allowedTaskProvider, String fieldName, String valueId) {
        super(commandExecutor, allowedTaskProvider);
        this.fieldName = fieldName;
        this.valueId = valueId;
    }

    @Override
    ProcessInstance execute(ProcessEngineFacade processEngineFacade, StorageManager storageManager) throws PieceworkException {
        Entity principal = modelProvider.principal();
        // This is an operation that anonymous users should never be able to cause
        if (principal == null)
            throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);

        ActionType actionType = ActionType.REMOVE;

        // Users are only allowed to remove values if they have been assigned a task, and
        // only process overseers or superusers are allowed to remove values otherwise
        Process process = modelProvider.process();
        if (!principal.hasRole(process, AuthorizationRole.OVERSEER, AuthorizationRole.SUPERUSER)) {
            Task task = modelProvider.allowedTask(true);
            if (task == null || !task.isCandidateOrAssignee(principal) || !principal.hasRole(process, AuthorizationRole.USER))
                throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);
        }

        ProcessInstance instance = modelProvider.instance();
        Map<String, List<Value>> data = instance.getData();
        List<? extends Value> values = fieldName != null ? data.get(fieldName) : null;

        if (values == null || values.isEmpty() || StringUtils.isEmpty(valueId))
            throw new NotFoundError();

        List<Value> remainingValues = new ArrayList<Value>();
        for (Value value : values) {
            if (value == null)
                continue;

            if (value instanceof File) {
                File file = File.class.cast(value);

                if (StringUtils.isEmpty(file.getId()))
                    continue;

                if (file.getId().equals(valueId))
                    storageManager.expire(modelProvider, file.getLocation());
                else
                    remainingValues.add(value);

            } else {
                String link = value.getValue();
                String id = Base64Utility.safeBase64(link);
                if (id == null || !id.equals(valueId))
                    remainingValues.add(value);
            }
        }
        ManyMap<String, Value> updatedData = new ManyMap<String, Value>();
        updatedData.put(fieldName, remainingValues);

        Submission submission = SubmissionFactory.submission(actionType, process.getProcessDefinitionKey(), null, null, updatedData, null, principal);
        return storageManager.store(null, instance, updatedData, submission);
    }


}
