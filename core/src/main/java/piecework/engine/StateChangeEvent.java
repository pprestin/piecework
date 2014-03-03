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

import piecework.enumeration.StateChangeType;
import piecework.model.Task;
import piecework.persistence.ProcessInstanceProvider;

/**
 * @author James Renfro
 */
public class StateChangeEvent {

    private final StateChangeType type;
    private final ProcessInstanceProvider instanceProvider;
    private final Task task;
    private final EngineContext context;

    private StateChangeEvent() {
        this(new Builder(StateChangeType.NONE));
    }

    private StateChangeEvent(Builder builder) {
        this.type = builder.type;
        this.instanceProvider = builder.instanceProvider;
        this.task = builder.task;
        this.context = builder.context;
    }

    public StateChangeType getType() {
        return type;
    }

    public ProcessInstanceProvider getInstanceProvider() {
        return instanceProvider;
    }

    public Task getTask() {
        return task;
    }

    public EngineContext getContext() {
        return context;
    }

    public static final class Builder {

        private final StateChangeType type;
        private ProcessInstanceProvider instanceProvider;
        private Task task;
        private EngineContext context;

        public Builder(StateChangeType type) {
            this.type = type;
        }

        public StateChangeEvent build() {
            return new StateChangeEvent(this);
        }

        public Builder instanceProvider(ProcessInstanceProvider instanceProvider) {
            this.instanceProvider = instanceProvider;
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
