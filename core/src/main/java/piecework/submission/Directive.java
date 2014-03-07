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
package piecework.submission;

import java.util.HashMap;
import java.util.Map;

/**
 * @author James Renfro
 */
public class Directive {

    private final Map<String, ? extends Object> taskVariables;
    private final Map<String, ? extends Object> instanceVariables;

    private Directive() {
        this(new Builder());
    }

    private Directive(Builder builder) {
        this.instanceVariables = builder.instanceVariables;
        this.taskVariables = builder.taskVariables;
    }

    public Map<String, ? extends Object> getTaskVariables() {
        return taskVariables;
    }

    public Map<String, ? extends Object> getInstanceVariables() {
        return instanceVariables;
    }

    public static final class Builder {
        private Map<String, Object> taskVariables;
        private Map<String, Object> instanceVariables;

        public Builder() {
            this.taskVariables = new HashMap<String, Object>();
            this.instanceVariables = new HashMap<String, Object>();
        }

        public Directive build() {
            return new Directive(this);
        }

        public Builder taskVariable(String name, Object value) {
            this.taskVariables.put(name, value);
            return this;
        }

        public Builder instanceVariable(String name, Object value) {
            this.instanceVariables.put(name, value);
            return this;
        }

    }

}
