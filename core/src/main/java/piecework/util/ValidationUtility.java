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
package piecework.util;

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import piecework.Constants;
import piecework.common.Registry;
import piecework.enumeration.FieldTag;
import piecework.form.OptionResolver;
import piecework.model.Constraint;
import piecework.model.Field;
import piecework.model.Option;
import piecework.model.Value;
import piecework.validation.ValidationRule;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author James Renfro
 */
public class ValidationUtility {

    private static final Set<FieldTag> FREEFORM_INPUT_TYPES = Sets.newHashSet(FieldTag.FILE, FieldTag.EMAIL, FieldTag.NUMBER, FieldTag.TEXT, FieldTag.TEXTAREA);
    private static final Logger LOG = Logger.getLogger(ValidationUtility.class);

    public static String fieldName(Field field, Map<String, List<Value>> data) {
        String fieldName = field.getName();

        if (fieldName == null) {
            if (field.getType() != null && field.getType().equalsIgnoreCase(Constants.FieldTypes.CHECKBOX)) {
                List<Option> options = field.getOptions();
                if (options != null) {
                    for (Option option : options) {
                        if (StringUtils.isNotEmpty(option.getName()) && data.containsKey(option.getName()))
                            fieldName = option.getName();
                    }
                }
            }
        }
        return fieldName;
    }

    public static List<? extends Value> append(List<? extends Value> values, List<? extends Value> previousValues) {
        if (values == null)
            return previousValues;

        List<Value> combined = new ArrayList<Value>();
        if (values != null)
            combined.addAll(values);
        if (previousValues != null)
            combined.addAll(previousValues);

        return combined;
    }

    public static OptionResolver resolveConstraints(Field field, FieldTag fieldTag, Set<ValidationRule> rules, Registry registry) {
        String fieldName = field.getName();
        OptionResolver optionResolver = null;
        List<Constraint> constraints = field.getConstraints();
        Constraint onlyRequiredWhenConstraint = null;
        if (constraints != null && !constraints.isEmpty()) {
            for (Constraint constraint : constraints) {
                String type = constraint.getType();
                if (type == null)
                    continue;

                if (type.equals(Constants.ConstraintTypes.IS_ONLY_REQUIRED_WHEN)) {
                    rules.add(new ValidationRule.Builder(ValidationRule.ValidationRuleType.CONSTRAINED_REQUIRED)
                            .name(fieldName)
                            .constraint(constraint)
                            .build());
                    onlyRequiredWhenConstraint = constraint;
                } else if (type.equals(Constants.ConstraintTypes.IS_NUMERIC))
                    rules.add(new ValidationRule.Builder(ValidationRule.ValidationRuleType.NUMERIC).name(fieldName).build());
                else if (type.equals(Constants.ConstraintTypes.IS_EMAIL_ADDRESS))
                    rules.add(new ValidationRule.Builder(ValidationRule.ValidationRuleType.EMAIL).name(fieldName).build());
                else if (type.equals(Constants.ConstraintTypes.IS_ALL_VALUES_MATCH))
                    rules.add(new ValidationRule.Builder(ValidationRule.ValidationRuleType.VALUES_MATCH).name(fieldName).constraint(onlyRequiredWhenConstraint).build());
                else if (type.equals(Constants.ConstraintTypes.IS_LIMITED_TO))
                    optionResolver = registry.retrieve(Option.class, constraint.getName());
            }
        }

        if (onlyRequiredWhenConstraint == null && field.isRequired()) {
            if (fieldTag == FieldTag.FILE)
                rules.add(new ValidationRule.Builder(ValidationRule.ValidationRuleType.REQUIRED_IF_NO_PREVIOUS).name(fieldName).build());
            else
                rules.add(new ValidationRule.Builder(ValidationRule.ValidationRuleType.REQUIRED).name(fieldName).build());
        }

        if (field.getMaxInputs() > 1 || field.getMinInputs() > 1)
            rules.add(new ValidationRule.Builder(ValidationRule.ValidationRuleType.NUMBER_OF_INPUTS).name(fieldName).numberOfInputs(field.getMaxInputs(), field.getMinInputs()).constraint(onlyRequiredWhenConstraint).required(field.isRequired()).build());

        return optionResolver;
    }

    public static Set<ValidationRule> validationRules(Field field, Registry registry) {
        FieldTag fieldTag = FieldTag.getInstance(field.getType());

        String fieldName = field.getName();
        Set<ValidationRule> rules = new HashSet<ValidationRule>();

        if (!field.isEditable())
            return rules;

        OptionResolver optionResolver = resolveConstraints(field, fieldTag, rules, registry);

        if (!FREEFORM_INPUT_TYPES.contains(fieldTag)) {
            List<Option> options = field.getOptions();

            if (optionResolver != null)
                options = optionResolver.getOptions();

            // If no options are stored, then assume that any option is valid
            if (options != null && !options.isEmpty()) {
                rules.add(new ValidationRule.Builder(ValidationRule.ValidationRuleType.LIMITED_OPTIONS)
                        .name(fieldName)
                        .options(options).build());
            }
        } else if (fieldTag == FieldTag.PERSON) {
            rules.add(new ValidationRule.Builder(ValidationRule.ValidationRuleType.VALID_USER).name(fieldName).build());
        } else {
            Pattern pattern = field.getPattern() != null ? Pattern.compile(field.getPattern()) : null;
            if (pattern != null)
                rules.add(new ValidationRule.Builder(ValidationRule.ValidationRuleType.PATTERN).name(fieldName).pattern(pattern).build());

            rules.add(new ValidationRule.Builder(ValidationRule.ValidationRuleType.VALUE_LENGTH).name(fieldName).valueLength(field.getMaxValueLength(), field.getMinValueLength()).build());
        }

        return rules;
    }

}
