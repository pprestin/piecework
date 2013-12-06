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
package piecework.validation;

import org.apache.commons.lang.StringUtils;
import piecework.Constants;
import piecework.enumeration.FieldSubmissionType;
import piecework.enumeration.FieldTag;
import piecework.model.Button;
import piecework.model.Field;
import piecework.model.Option;
import piecework.util.ManyMap;

import java.util.*;

/**
 * @author James Renfro
 */
public class SubmissionTemplate {

    private final Set<String> buttonNames;
    private final Map<String, Button> buttonValueMap;
    private final Set<String> userFields;
    private final Map<Field, List<ValidationRule>> fieldRuleMap;
    private final Map<String, Field> fieldMap;
    private final boolean isAttachmentAllowed;
    private final long maxAttachmentSize;

    private SubmissionTemplate() {
        this(new Builder());
    }

    private SubmissionTemplate(Builder builder) {
        this.buttonNames = Collections.unmodifiableSet(builder.buttonNames);
        this.buttonValueMap = Collections.unmodifiableMap(builder.buttonValueMap);
        this.fieldMap = Collections.unmodifiableMap(builder.fieldMap);
        this.userFields = Collections.unmodifiableSet(builder.userFields);
        this.isAttachmentAllowed = builder.isAttachmentAllowed;
        this.fieldRuleMap = Collections.unmodifiableMap(builder.fieldRuleMap);
        this.maxAttachmentSize = builder.maxAttachmentSize;
    }

    public FieldSubmissionType fieldSubmissionType(String name) {
        if (StringUtils.isNotEmpty(name)) {
            Field field = this.fieldMap.get(name);
            if (field != null) {
                if (field.isRestricted())
                    return FieldSubmissionType.RESTRICTED;
                return FieldSubmissionType.ACCEPTABLE;
            }

            if (buttonNames.contains(name))
                return FieldSubmissionType.BUTTON;

            int indexOf = name.indexOf("!description");
            if (indexOf != -1 && indexOf < name.length()) {
                String fieldName = name.substring(0, indexOf);
                if (this.fieldMap.containsKey(fieldName))
                    return FieldSubmissionType.DESCRIPTION;
            }

            if (isAttachmentAllowed)
                return FieldSubmissionType.ATTACHMENT;
        }

        return FieldSubmissionType.INVALID;
    }

    public Button getButton(String value) {
        return buttonValueMap.get(value);
    }

    public Field getField(String name) {
        return fieldMap != null && name != null ? fieldMap.get(name) : null;
    }

    public Map<String, Field> getFieldMap() {
        return fieldMap;
    }

    public Map<Field, List<ValidationRule>> getFieldRuleMap() {
        return fieldRuleMap;
    }

    public long getMaxAttachmentSize() {
        return maxAttachmentSize;
    }

    public boolean isUserField(String name) {
        Field field = fieldMap.get(name);
        return field != null && field.getType() != null && field.getType().equals(Constants.FieldTypes.PERSON);
    }

    public final static class Builder {
        private Set<String> acceptable;
        private Set<String> restricted;
        private Set<String> buttonNames;
        private Set<String> userFields;
        private Map<String, Button> buttonValueMap;
        private ManyMap<Field, ValidationRule> fieldRuleMap;
        private Map<String, Field> fieldMap;
        private boolean isAttachmentAllowed;
        private long maxAttachmentSize;

        public Builder() {
            this.acceptable = new HashSet<String>();
            this.buttonNames = new HashSet<String>();
            this.buttonValueMap = new HashMap<String, Button>();
            this.restricted = new HashSet<String>();
            this.userFields = new HashSet<String>();
            this.fieldRuleMap = new ManyMap<Field, ValidationRule>();
            this.fieldMap = new HashMap<String, Field>();
            this.maxAttachmentSize = 10485760l;
        }

        public SubmissionTemplate build() {
            return new SubmissionTemplate(this);
        }

        public Builder acceptable(String key) {
            this.acceptable.add(key);
            return this;
        }

        public Builder button(Button button) {
            this.buttonNames.add(button.getName());
            this.buttonValueMap.put(button.getValue(), button);
            return this;
        }

        public Builder field(Field field) {
            this.fieldMap.put(field.getName(), field);
//            if (field.getType() != null && field.getType().equals(Constants.FieldTypes.CHECKBOX)) {
//                List<Option> options = field.getOptions();
//                if (options != null) {
//                    for (Option option : options) {
//                        if (StringUtils.isNotEmpty(option.getName())) {
//                            this.fieldMap.put(option.getName(), field);
//                        }
//                    }
//                }
//            }

            return this;
        }

        public Builder restricted(String key) {
            this.restricted.add(key);
            return this;
        }

        public Builder rule(Field field, ValidationRule rule) {
            if (rule != null)
                this.fieldRuleMap.putOne(field, rule);
            return this;
        }

        public Builder rules(Field field, List<ValidationRule> rules) {
            if (rules != null && !rules.isEmpty())
                this.fieldRuleMap.put(field, rules);
            return this;
        }

        public Builder userField(String name) {
            this.userFields.add(name);
            return this;
        }

        public Builder allowAttachments() {
            this.isAttachmentAllowed = true;
            return this;
        }

        public Builder maxAttachmentSize(long maxAttachmentSize) {
            this.maxAttachmentSize = maxAttachmentSize;
            return this;
        }
    }

}
