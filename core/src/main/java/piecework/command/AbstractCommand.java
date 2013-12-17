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
import piecework.ServiceLocator;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;

/**
 * @author James Renfro
 */
public abstract class AbstractCommand<T> implements Command<T> {

    private final CommandExecutor commandExecutor;
    private final String processDefinitionKey;
    protected final piecework.model.Process process;
    protected final ProcessInstance instance;
    protected final Entity principal;

    AbstractCommand(CommandExecutor commandExecutor, ProcessInstance instance) {
        this.commandExecutor = commandExecutor;
        this.principal = null;
        this.processDefinitionKey = instance != null ? instance.getProcessDefinitionKey() : null;
        this.process = null;
        this.instance = instance;
    }

    AbstractCommand(CommandExecutor commandExecutor, Entity principal, Process process) {
        this(commandExecutor, principal, process, null);
    }

    AbstractCommand(CommandExecutor commandExecutor, Entity principal, Process process, ProcessInstance instance) {
        this.commandExecutor = commandExecutor;
        this.processDefinitionKey = process != null ? process.getProcessDefinitionKey() : null;
        this.principal = principal;
        this.process = process;
        this.instance = instance;
    }

    @Override
    public T execute() throws PieceworkException {
        return commandExecutor.execute(this);
    }

    // Package-access abstract method signature to all CommandFactory to pass in ServiceLocator
    abstract <T> T execute(ServiceLocator serviceLocator) throws PieceworkException;

    public Process getProcess() {
        return process;
    }

    public ProcessInstance getInstance() {
        return instance;
    }

    public Entity getPrincipal() {
        return principal;
    }

    @Override
    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

}
