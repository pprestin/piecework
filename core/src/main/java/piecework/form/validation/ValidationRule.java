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

import org.apache.commons.lang.StringUtils;
import piecework.exception.ValidationRuleException;
import piecework.model.Constraint;
import piecework.model.FormValue;
import piecework.model.Option;
import piecework.util.ConstraintUtil;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author James Renfro
 */
public class ValidationRule {

    public enum ValidationRuleType {
        CONSTRAINED,
        EMAIL,
        LIMITED_OPTIONS,
        NUMBER_OF_INPUTS,
        NUMERIC,
        PATTERN,
        REQUIRED,
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
    }

    public void evaluate(Map<String, FormValue> formValueMap) throws ValidationRuleException {
        switch(type) {
        case CONSTRAINED:
            evaluateConstraint(formValueMap);
            break;
        case EMAIL:
            evaluateEmail(formValueMap);
            break;
        case LIMITED_OPTIONS:
            evaluateOptions(formValueMap);
            break;
        case NUMBER_OF_INPUTS:
            evaluateNumberOfInputs(formValueMap);
            break;
        case NUMERIC:
            evaluateNumeric(formValueMap);
            break;
        case PATTERN:
            evaluatePattern(formValueMap);
            break;
        case REQUIRED:
            evaluateRequired(formValueMap);
            break;
        case VALID_USER:
            evaluateValidUser(formValueMap);
            break;
        case VALUE_LENGTH:
            evaluateValueLength(formValueMap);
            break;
        case VALUES_MATCH:
            evaluateValuesMatch(formValueMap);
            break;
        }
    }

    public String getName() {
        return name;
    }

    private void evaluateConstraint(Map<String, FormValue> formValueMap) throws ValidationRuleException {
        if (!ConstraintUtil.evaluate(null, formValueMap, constraint))
            throw new ValidationRuleException("Field is required");
    }

    private void evaluateEmail(Map<String, FormValue> formValueMap) throws ValidationRuleException {

    }

    private void evaluateOptions(Map<String, FormValue> formValueMap) throws ValidationRuleException {
        if (options == null || options.isEmpty())
            throw new ValidationRuleException("No valid options for this field");

        FormValue formValue = safeFormValue(name, formValueMap);
        if (formValue != null) {
            List<String> values = formValue.getValues();
            if (values != null && !values.isEmpty()) {
                for (String value : values) {
                    if (value != null && !options.contains(value))
                        throw new ValidationRuleException("Not a valid option for this field");
                }
            }
        }
    }

    private void evaluateNumberOfInputs(Map<String, FormValue> formValueMap) throws ValidationRuleException {
        int numberOfInputs = 0;
        FormValue formValue = safeFormValue(name, formValueMap);
        if (formValue != null) {
            List<String> values = formValue.getValues();
            if (values != null && !values.isEmpty()) {
                for (String value : values) {
                    if (StringUtils.isNotEmpty(value))
                        numberOfInputs++;
                }
            }
        }

        if (maxInputs < numberOfInputs)
            throw new ValidationRuleException("No more than " + maxInputs + " values are allowed");
        else if (minInputs > numberOfInputs)
            throw new ValidationRuleException("At least " + minInputs + " values are required");
    }

    private void evaluateNumeric(Map<String, FormValue> formValueMap) throws ValidationRuleException {
        FormValue formValue = safeFormValue(name, formValueMap);
        if (formValue != null) {
            List<String> values = formValue.getValues();
            if (values != null && !values.isEmpty()) {
                for (String value : values) {
                    if (value != null && !value.matches("^[0-9]+$"))
                        throw new ValidationRuleException("Must be a number");
                }
            }
        }
    }

    private void evaluatePattern(Map<String, FormValue> formValueMap) throws ValidationRuleException {
        FormValue formValue = safeFormValue(name, formValueMap);
        if (formValue != null) {
            List<String> values = formValue.getValues();
            if (values != null && !values.isEmpty()) {
                for (String value : values) {
                    if (value != null && pattern != null && !pattern.matcher(value).matches()) {
                        String examplePattern = mask;
                        if (examplePattern == null)
                            examplePattern = pattern.toString();

                        throw new ValidationRuleException("Does not match required pattern: " + examplePattern);
                    }
                }
            }
        }
    }

    private void evaluateRequired(Map<String, FormValue> formValueMap) throws ValidationRuleException {
        boolean hasAtLeastOneValue = false;
        FormValue formValue = safeFormValue(name, formValueMap);
        if (formValue != null) {
            List<String> values = formValue.getValues();
            if (values != null && !values.isEmpty()) {
                for (String value : values) {
                    if (StringUtils.isNotEmpty(value))
                        hasAtLeastOneValue = true;
                }
            }
        }

        if (!hasAtLeastOneValue)
            throw new ValidationRuleException("Field is required");
    }

    private void evaluateValidUser(Map<String, FormValue> formValueMap) throws ValidationRuleException {

    }

    private void evaluateValueLength(Map<String, FormValue> formValueMap) throws ValidationRuleException {
        FormValue formValue = safeFormValue(name, formValueMap);
        if (formValue != null) {
            List<String> values = formValue.getValues();
            if (values != null && !values.isEmpty()) {
                for (String value : values) {
                    if (value != null) {
                        if (value.length() > maxValueLength)
                            throw new ValidationRuleException("Cannot be more than " + maxValueLength + " characters");
                        else if (value.length() < minValueLength)
                            throw new ValidationRuleException("Cannot be less than " + minValueLength + " characters");
                    }
                }
            }
        }
    }

    private void evaluateValuesMatch(Map<String, FormValue> formValueMap) throws ValidationRuleException {
        FormValue formValue = safeFormValue(name, formValueMap);
        if (formValue != null) {
            List<String> values = formValue.getValues();
            if (values != null && !values.isEmpty()) {
                String lastValue = null;
                for (String value : values) {
                    if (lastValue != null && !lastValue.equals(value))
                        throw new ValidationRuleException("Values do not match");
                }
            }
        }
    }

    private FormValue safeFormValue(String name, Map<String, FormValue> formValueMap) {
        return name != null ? formValueMap.get(name) : null;
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
    }

}
