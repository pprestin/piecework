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

import piecework.ServiceLocator;
import piecework.engine.ProcessEngineFacade;
import piecework.exception.PieceworkException;
import piecework.manager.StorageManager;
import piecework.model.*;
import piecework.model.Process;

/**
 * The majority of the commands implement the package-level execute(ServiceLocator) to
 * inject the ProcessEngineFacade and StorageManager -- to manipulate process engine
 * state and to store something to a repository. This class abstracts that method to
 * avoid a lot of boilerplate
 *
 * @author James Renfro
 */
public abstract class AbstractEngineStorageCommand<T> extends AbstractCommand<T> {

    protected AbstractEngineStorageCommand(CommandExecutor commandExecutor, ProcessInstance instance) {
        super(commandExecutor, instance);
    }

    protected AbstractEngineStorageCommand(CommandExecutor commandExecutor, Entity principal, Process process) {
        super(commandExecutor, principal, process);
    }

    protected AbstractEngineStorageCommand(CommandExecutor commandExecutor, Entity principal, Process process, ProcessInstance instance) {
        super(commandExecutor, principal, process, instance);
    }

    /*
         * Injects necessary services into the other package-access execute method, which then does all the work
         */
    @Override
    ProcessInstance execute(ServiceLocator serviceLocator) throws PieceworkException {
        ProcessEngineFacade processEngineFacade = serviceLocator.getService(ProcessEngineFacade.class);
        StorageManager storageManager = serviceLocator.getService(StorageManager.class);
        return execute(processEngineFacade, storageManager);
    }

    /*
     * This method is where the actual work of the command takes place
     */
    abstract ProcessInstance execute(ProcessEngineFacade processEngineFacade, StorageManager storageManager) throws PieceworkException;

}
