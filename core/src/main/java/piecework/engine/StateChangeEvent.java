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
package piecework.engine;

import piecework.enumeration.ActionType;
import piecework.enumeration.StateChangeType;
import piecework.model.Process;
import piecework.model.ProcessInstance;
import piecework.model.Task;

/**
 * @author James Renfro
 */
public class StateChangeEvent {

    private final StateChangeType type;
    private final Process process;
    private final ProcessInstance instance;
    private final Task task;
    private final ActionType actionType;
    private final EngineContext context;

    private StateChangeEvent() {
        this(new Builder(StateChangeType.NONE));
    }

    private StateChangeEvent(Builder builder) {
        this.type = builder.type;
        this.process = builder.process;
        this.instance = builder.instance;
        this.task = builder.task;
        this.actionType = builder.actionType;
        this.context = builder.context;
    }

    public StateChangeType getType() {
        return type;
    }

    public Process getProcess() {
        return process;
    }

    public ProcessInstance getInstance() {
        return instance;
    }

    public Task getTask() {
        return task;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public EngineContext getContext() {
        return context;
    }

    public static final class Builder {

        private final StateChangeType type;
        private Process process;
        private ProcessInstance instance;
        private Task task;
        private ActionType actionType;
        private EngineContext context;

        public Builder(StateChangeType type) {
            this.type = type;
        }

        public StateChangeEvent build() {
            return new StateChangeEvent(this);
        }

        public Builder process(Process process) {
            this.process = process;
            return this;
        }

        public Builder instance(ProcessInstance instance) {
            this.instance = instance;
            return this;
        }

        public Builder task(Task task) {
            this.task = task;
            return this;
        }

        public Builder context(EngineContext context) {
            this.context = context;
            return this;
        }

    }


}
