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
import piecework.Constants;
import piecework.Registry;
import piecework.enumeration.FieldTag;
import piecework.form.OptionResolver;
import piecework.model.Constraint;
import piecework.model.Field;
import piecework.model.Option;
import piecework.validation.ValidationRule;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author James Renfro
 */
public class ValidationUtility {

    private static final Set<FieldTag> FREEFORM_INPUT_TYPES = Sets.newHashSet(FieldTag.FILE, FieldTag.EMAIL, FieldTag.NUMBER, FieldTag.TEXT, FieldTag.TEXTAREA);


    public static Set<ValidationRule> validationRules(Field field, Registry registry) {
        FieldTag fieldTag = FieldTag.getInstance(field.getType());

        String fieldName = field.getName();
        Set<ValidationRule> rules = new HashSet<ValidationRule>();

        if (!field.isEditable())
            return rules;

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

        if (field.getMaxInputs() > 1 || field.getMinInputs() > 1)
            rules.add(new ValidationRule.Builder(ValidationRule.ValidationRuleType.NUMBER_OF_INPUTS).name(fieldName).numberOfInputs(field.getMaxInputs(), field.getMinInputs()).constraint(onlyRequiredWhenConstraint).required(field.isRequired()).build());

        return rules;
    }

}
