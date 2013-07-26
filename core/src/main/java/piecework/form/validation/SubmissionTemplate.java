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
package piecework.form.validation;

import com.google.common.collect.Sets;
import piecework.form.handler.ValueHandler;
import piecework.model.Button;
import piecework.model.Field;

import java.util.*;

/**
 * @author James Renfro
 */
public class SubmissionTemplate {

    private final Set<String> acceptable;
    private final Map<String, Button> buttonMap;
    private final Map<String, ValueHandler> handlerMap;
    private final Set<String> restricted;
    private final List<ValidationRule> rules;
    private final boolean isAttachmentAllowed;

    private SubmissionTemplate() {
        this(new Builder());
    }

    private SubmissionTemplate(Builder builder) {
        this.acceptable = Collections.unmodifiableSet(builder.acceptable);
        this.buttonMap = Collections.unmodifiableMap(builder.buttonMap);
        this.handlerMap = Collections.unmodifiableMap(builder.handlerMap);
        this.restricted = Collections.unmodifiableSet(builder.restricted);
        this.isAttachmentAllowed = builder.isAttachmentAllowed;
        this.rules = Collections.unmodifiableList(builder.rules);
    }

    public Set<String> getAcceptable() {
        return acceptable;
    }

    public Collection<Button> getButtons() {
        return buttonMap.values();
    }

    public Map<String, ValueHandler> getHandlerMap() {
        return handlerMap;
    }

    public Set<String> getRestricted() {
        return restricted;
    }

    public List<ValidationRule> getRules() {
        return rules;
    }

    public boolean isAcceptable(String name) {
        return acceptable.contains(name);
    }

    public boolean isButton(String name) {
        return buttonMap.containsKey(name);
    }

    public boolean isAttachmentAllowed() {
        return isAttachmentAllowed;
    }

    public boolean isRestricted(String name) {
        return restricted.contains(name);
    }

    public final static class Builder {
        private Set<String> acceptable;
        private Set<String> restricted;
        private Map<String, Button> buttonMap;
        private Map<String, ValueHandler> handlerMap;
        private List<ValidationRule> rules;
        private boolean isAttachmentAllowed;

        public Builder() {
            this.acceptable = new HashSet<String>();
            this.buttonMap = new HashMap<String, Button>();
            this.handlerMap = new HashMap<String, ValueHandler>();
            this.restricted = new HashSet<String>();
            this.rules = new ArrayList<ValidationRule>();
        }

        public SubmissionTemplate build() {
            return new SubmissionTemplate(this);
        }

        public Builder acceptable(String key) {
            this.acceptable.add(key);
            return this;
        }

        public Builder acceptables(Collection<String> keys) {
            this.acceptable.addAll(keys);
            return this;
        }

        public Builder button(Button button) {
            this.buttonMap.put(button.getName(), button);
            return this;
        }

        public Builder handler(String key, ValueHandler handler) {
            this.handlerMap.put(key, handler);
            return this;
        }

        public Builder restricted(String key) {
            this.restricted.add(key);
            return this;
        }

        public Builder rule(ValidationRule rule) {
            this.rules.add(rule);
            return this;
        }

        public Builder rules(Collection<ValidationRule> rules) {
            if (rules != null && !rules.isEmpty())
                this.rules.addAll(rules);
            return this;
        }

        public Builder allowAttachments() {
            this.isAttachmentAllowed = true;
            return this;
        }
    }

}
