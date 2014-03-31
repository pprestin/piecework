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
import piecework.engine.ProcessEngineFacade;
import piecework.exception.PieceworkException;
import piecework.manager.StorageManager;
import piecework.model.ProcessInstance;
import piecework.model.Value;
import piecework.persistence.ProcessInstanceProvider;

import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
public class CompletionCommand extends AbstractEngineStorageCommand<ProcessInstance, ProcessInstanceProvider> {

    CompletionCommand(CommandExecutor commandExecutor, ProcessInstanceProvider instanceProvider) {
        super(commandExecutor, instanceProvider);
    }

    ProcessInstance execute(ProcessEngineFacade processEngineFacade, StorageManager storageManager) throws PieceworkException {
        // This is obviously a trivial implementation but in case we have additional logic on
        // instance completion this is available
        ProcessInstance instance = modelProvider.instance();

        if (instance == null)
            return null;

        Map<String, List<Value>> data = instance.getData();
        return storageManager.archive(instance, data, Constants.ProcessStatuses.COMPLETE);
    }

}
