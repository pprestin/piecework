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

import piecework.Command;
import piecework.common.ServiceLocator;
import piecework.exception.PieceworkException;
import piecework.model.Entity;
import piecework.persistence.ProcessProvider;

/**
 * @author James Renfro
 */
public abstract class AbstractCommand<T, P extends ProcessProvider> implements Command<T> {

    private final CommandExecutor commandExecutor;
    protected final P modelProvider;

    AbstractCommand(CommandExecutor commandExecutor, P modelProvider) {
        this.commandExecutor = commandExecutor;
        this.modelProvider = modelProvider;
    }

    @Override
    public T execute() throws PieceworkException {
        return commandExecutor.execute(this);
    }

    // Package-access abstract method signature to all CommandFactory to pass in ServiceLocator
    abstract <T> T execute(ServiceLocator serviceLocator) throws PieceworkException;


    public P getProvider() {
        return modelProvider;
    }

    @Override
    public String getProcessDefinitionKey() {
        return modelProvider.processDefinitionKey();
    }

    @Override
    public Entity getPrincipal() {
        return modelProvider.principal();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
