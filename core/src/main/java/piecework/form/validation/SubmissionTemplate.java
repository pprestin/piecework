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
import piecework.model.Button;
import piecework.model.Field;

import java.util.*;

/**
 * @author James Renfro
 */
public class SubmissionTemplate {

    private final Set<String> acceptable;
    private final Set<String> restricted;
    private final List<ValidationRule> rules;
    private final boolean isAttachmentAllowed;

    private SubmissionTemplate() {
        this(new Builder());
    }

    private SubmissionTemplate(Builder builder) {
        this.acceptable = Collections.unmodifiableSet(builder.acceptable);
        this.restricted = Collections.unmodifiableSet(builder.restricted);
        this.isAttachmentAllowed = builder.isAttachmentAllowed;
        this.rules = Collections.unmodifiableList(builder.rules);
    }

    public Set<String> getAcceptable() {
        return acceptable;
    }

    public Set<String> getRestricted() {
        return restricted;
    }

    public List<ValidationRule> getRules() {
        return rules;
    }

    public boolean isAttachmentAllowed() {
        return isAttachmentAllowed;
    }

    public final static class Builder {
        private Set<String> acceptable;
        private Set<String> restricted;
        private List<ValidationRule> rules;
        private boolean isAttachmentAllowed;

        public Builder() {
            this.acceptable = new HashSet<String>();
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
