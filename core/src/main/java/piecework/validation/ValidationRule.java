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
import org.apache.commons.validator.routines.EmailValidator;
import piecework.exception.ValidationRuleException;
import piecework.model.*;
import piecework.util.ConstraintUtil;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author James Renfro
 */
public class ValidationRule {

    public enum ValidationRuleType {
        CONSTRAINED,
        CONSTRAINED_REQUIRED,
        CONSTRAINED_RULE,
        EMAIL,
        LIMITED_OPTIONS,
        NUMBER_OF_INPUTS,
        NUMERIC,
        PATTERN,
        REQUIRED,
        REQUIRED_IF_NO_PREVIOUS,
        VALID_USER,
        VALUE_LENGTH,
        VALUES_MATCH
    };

    private final ValidationRuleType type;
    private final String name;
    private final Constraint constraint;
    private final Set<String> options;
    private final String mask;
    private final Pattern pattern;
    private final int maxInputs;
    private final int minInputs;
    private final int maxValueLength;
    private final int minValueLength;
    private final boolean required;

    private ValidationRule() {
        this(new Builder(ValidationRuleType.REQUIRED));
    }

    private ValidationRule(Builder builder) {
        this.type = builder.type;
        this.name = builder.name;
        this.constraint = builder.constraint;
        this.options = builder.options;
        this.mask = builder.mask;
        this.pattern = builder.pattern;
        this.maxInputs = builder.maxInputs;
        this.minInputs = builder.minInputs;
        this.maxValueLength = builder.maxValueLength;
        this.minValueLength = builder.minValueLength;
        this.required = builder.required;
    }

    public ValidationRuleType getType() {
        return type;
    }

    public String toString() {
        return new StringBuilder("{ rule: \"")
                .append(type.toString())
                .append("\" name: \"")
                .append(name)
                .append("\"}")
                .toString();
    }

    public void evaluate(Map<String, List<Value>> submissionData, Map<String, List<Value>> instanceData, boolean onlyAcceptValidInputs) throws ValidationRuleException {
        switch(type) {
        case CONSTRAINED:
            evaluateConstraint(submissionData);
            break;
        case CONSTRAINED_REQUIRED:
            evaluateConstraintRequired(submissionData);
            break;
        case EMAIL:
            evaluateEmail(submissionData);
            break;
        case LIMITED_OPTIONS:
            evaluateOptions(submissionData);
            break;
        case NUMBER_OF_INPUTS:
            evaluateNumberOfInputs(submissionData, onlyAcceptValidInputs);
            break;
        case NUMERIC:
            evaluateNumeric(submissionData);
            break;
        case PATTERN:
            evaluatePattern(submissionData);
            break;
        case REQUIRED:
            evaluateRequired(submissionData);
            break;
        case REQUIRED_IF_NO_PREVIOUS:
            evaluateRequiredIfNoPrevious(submissionData, instanceData);
            break;
        case VALID_USER:
            evaluateValidUser(submissionData);
            break;
        case VALUE_LENGTH:
            evaluateValueLength(submissionData);
            break;
        case VALUES_MATCH:
            evaluateValuesMatch(submissionData);
            break;
        }
    }

    public String getName() {
        return name;
    }

    private void evaluateConstraint(Map<String, List<Value>> submissionData) throws ValidationRuleException {
        if (!ConstraintUtil.evaluate(null, submissionData, constraint))
            throw new ValidationRuleException(this, "Not a valid input for this field");
    }

    private void evaluateConstraintRequired(Map<String, List<Value>> submissionData) throws ValidationRuleException {
        if (ConstraintUtil.evaluate(null, submissionData, constraint))
            evaluateRequired(submissionData);
    }

    private void evaluateEmail(Map<String, List<Value>> submissionData) throws ValidationRuleException {
        List<? extends Value> values = safeValues(name, submissionData);
        for (Value value : values) {
            if (value != null && StringUtils.isNotEmpty(value.getValue()) && !EmailValidator.getInstance().isValid(value.getValue()))
                throw new ValidationRuleException(this, "Must be a valid email address");
        }
    }

    private void evaluateOptions(Map<String, List<Value>> submissionData) throws ValidationRuleException {
        if (options == null || options.isEmpty())
            throw new ValidationRuleException(this, "No valid options for this field");

        List<? extends Value> values = safeValues(name, submissionData);
        for (Value value : values) {
            if (value != null && StringUtils.isNotEmpty(value.getValue()) && !options.contains(value.getValue()))
                throw new ValidationRuleException(this, "Not a valid option for this field");
        }
    }

    private void evaluateNumberOfInputs(Map<String, List<Value>> submissionData, boolean onlyAcceptValidInputs) throws ValidationRuleException {
        if (constraint != null && !ConstraintUtil.evaluate(null, submissionData, constraint))
            return;

        int numberOfInputs = 0;
        List<? extends Value> values = safeValues(name, submissionData);
        for (Value value : values) {
            if (value == null)
                continue;

            if (value instanceof File) {
                File file = File.class.cast(value);
                if (StringUtils.isNotEmpty(file.getName()))
                    numberOfInputs++;
            } else if (StringUtils.isNotEmpty(value.getValue()))
                numberOfInputs++;
        }

        if (maxInputs < numberOfInputs)
            throw new ValidationRuleException(this, "No more than " + maxInputs + " are allowed");
        else if (required && minInputs > numberOfInputs && onlyAcceptValidInputs)
            throw new ValidationRuleException(this, "At least " + minInputs + " are required");
    }

    private void evaluateNumeric(Map<String, List<Value>> submissionData) throws ValidationRuleException {
        List<? extends Value> values = safeValues(name, submissionData);
        for (Value value : values) {
            if (value != null && StringUtils.isNotEmpty(value.getValue()) && !value.getValue().matches("^[0-9]+$"))
                throw new ValidationRuleException(this, "Must be a number");
        }
    }

    private void evaluatePattern(Map<String, List<Value>> submissionData) throws ValidationRuleException {
        List<? extends Value> values = safeValues(name, submissionData);
        for (Value value : values) {
            if (value != null && StringUtils.isNotEmpty(value.getValue()) && pattern != null && !pattern.matcher(value.getValue()).matches()) {
                String examplePattern = mask;
                if (examplePattern == null)
                    examplePattern = pattern.toString();

                throw new ValidationRuleException(this, "Does not match required pattern: " + examplePattern);
            }
        }
    }

    private void evaluateRequired(Map<String, List<Value>> submissionData) throws ValidationRuleException {
        boolean hasAtLeastOneValue = false;
        List<? extends Value> values = safeValues(name, submissionData);
        for (Value value : values) {
            if (value == null)
                continue;

            if (value instanceof File) {
                File file = File.class.cast(value);
                if (StringUtils.isNotEmpty(file.getName()))
                    hasAtLeastOneValue = true;
            } else if (value instanceof User) {
                User user = User.class.cast(value);

                if (user != null && user.getUserId() != null)
                    hasAtLeastOneValue = true;
            } else if (StringUtils.isNotEmpty(value.getValue()))
                hasAtLeastOneValue = true;
        }

        if (!hasAtLeastOneValue)
            throw new ValidationRuleException(this, "Field is required");
    }

    private void evaluateRequiredIfNoPrevious(Map<String, List<Value>> submissionData, Map<String, List<Value>> instanceData) throws ValidationRuleException {
        boolean hasAtLeastOneValue = false;
        List<? extends Value> values = safeValues(name, submissionData);
        for (Value value : values) {
            if (value instanceof File) {
                File file = File.class.cast(value);
                if (StringUtils.isNotEmpty(file.getName()))
                    hasAtLeastOneValue = true;
            } else if (StringUtils.isNotEmpty(value.getValue()))
                hasAtLeastOneValue = true;
        }
        if (!hasAtLeastOneValue) {
            List<? extends Value> previousValues = safeValues(name, instanceData);
            for (Value value : previousValues) {
                if (value instanceof File) {
                    File file = File.class.cast(value);
                    if (StringUtils.isNotEmpty(file.getName()))
                        hasAtLeastOneValue = true;
                } else if (StringUtils.isNotEmpty(value.getValue()))
                    hasAtLeastOneValue = true;
            }
        }
        if (!hasAtLeastOneValue)
            throw new ValidationRuleException(this, "Field is required");
    }

    private void evaluateValidUser(Map<String, List<Value>> submissionData) throws ValidationRuleException {
        List<? extends Value> values = safeValues(name, submissionData);
        for (Value value : values) {
            if (value != null) {
                if (!(value instanceof User))
                    throw new ValidationRuleException(this, "Must be a valid user");
            }
        }
    }

    private void evaluateValueLength(Map<String, List<Value>> submissionData) throws ValidationRuleException {
        List<? extends Value> values = safeValues(name, submissionData);
        for (Value value : values) {
            if (value != null && value.getValue() != null) {
                if (value.getValue().length() > maxValueLength)
                    throw new ValidationRuleException(this, "Cannot be more than " + maxValueLength + " characters");
                else if (value.getValue().length() < minValueLength)
                    throw new ValidationRuleException(this, "Cannot be less than " + minValueLength + " characters");
            }
        }
    }

    private void evaluateValuesMatch(Map<String, List<Value>> submissionData) throws ValidationRuleException {
        if (constraint != null && !ConstraintUtil.evaluate(null, submissionData, constraint))
            return;

        List<? extends Value> values = safeValues(name, submissionData);
        String lastValue = null;
        for (Value value : values) {
            if (value != null && value.getValue() != null) {
                if (lastValue != null && !lastValue.equals(value.getValue()))
                    throw new ValidationRuleException(this, "Values do not match");
                lastValue = value.getValue();
            }
        }
    }

    private List<? extends Value> safeValues(String name, Map<String, List<Value>> submissionData) {
        List<? extends Value> values = null;

        if (name != null && submissionData != null)
            values = submissionData.get(name);

        if (values == null)
            values = Collections.<Value>emptyList();

        return values;
    }

    public final static class Builder {

        private final ValidationRuleType type;
        private String name;
        private Constraint constraint;
        private Set<String> options;
        private String mask;
        private Pattern pattern;
        private int maxInputs;
        private int minInputs;
        private int maxValueLength;
        private int minValueLength;
        private boolean required;

        public Builder(ValidationRuleType type) {
            this.type = type;
        }

        public ValidationRule build() {
            return new ValidationRule(this);
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder constraint(Constraint constraint) {
            this.constraint = constraint;
            return this;
        }

        public Builder options(Collection<Option> options) {
            this.options = new HashSet<String>();
            if (options != null) {
                for (Option option : options) {
                    this.options.add(option.getValue());
                }
            }

            return this;
        }

        public Builder mask(String mask) {
            this.mask = mask;
            return this;
        }

        public Builder numberOfInputs(int maxInputs, int minInputs) {
            this.maxInputs = maxInputs;
            this.minInputs = minInputs;
            return this;
        }

        public Builder pattern(Pattern pattern) {
            this.pattern = pattern;
            return this;
        }

        public Builder valueLength(int maxValueLength, int minValueLength) {
            this.maxValueLength = maxValueLength;
            this.minValueLength = minValueLength;
            return this;
        }

        public Builder required(boolean required) {
            this.required = required;
            return this;
        }
    }

}
