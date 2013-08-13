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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.Registry;
import piecework.enumeration.FieldTag;
import piecework.model.*;
import piecework.model.Process;
import piecework.util.ConstraintUtil;
import piecework.util.OptionResolver;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author James Renfro
 */
@Service
public class SubmissionTemplateFactory {

    private static final Set<FieldTag> FREEFORM_INPUT_TYPES = Sets.newHashSet(FieldTag.TEXT, FieldTag.TEXTAREA);

    @Autowired(required=false)
    Registry registry;

    /*
     * Generated an "attachments only submission template", where all fields will be stored as attachments
     */
    public SubmissionTemplate submissionTemplate(Process process) {
        return new SubmissionTemplate.Builder().allowAttachments().build();
    }

    public SubmissionTemplate submissionTemplate(Field field) {
        SubmissionTemplate.Builder builder = new SubmissionTemplate.Builder();
        addField(builder, field);
        return builder.build();
    }

    /*
     * Takes a screen and generates the appropriate submission template for all
     * sections and fields used by all groupings
     */
    public SubmissionTemplate submissionTemplate(Process process, Screen screen) {
        return submissionTemplate(process, screen, null);
    }

    /*
     * Takes a screen and generates the appropriate submission template for it,
     * limiting to a specific section id
     */
    public SubmissionTemplate submissionTemplate(Process process, Screen screen, String validationId) {
        List<Grouping> groupings = screen.getGroupings();
        Map<String, Section> sectionMap = process.getSectionMap();

        SubmissionTemplate.Builder builder = new SubmissionTemplate.Builder();
        if (groupings != null && sectionMap != null && !sectionMap.isEmpty()) {
            for (Grouping grouping : groupings) {
                if (grouping == null)
                    continue;

                String groupingId = grouping.getGroupingId();
                if (groupingId != null && validationId != null && !groupingId.equals(validationId))
                    continue;

                List<Button> buttons = grouping.getButtons();
                if (buttons != null) {
                    for (Button button : buttons) {
                        if (button == null)
                            continue;
                        builder.button(button);
                    }
                }

                List<String> sectionsIds = grouping.getSectionIds();
                if (sectionsIds == null)
                    continue;

                for (String sectionId : sectionsIds) {
                    Section section = sectionMap.get(sectionId);
                    if (section == null)
                        continue;

                    List<Field> fields = section.getFields();
                    if (fields == null || fields.isEmpty())
                        continue;

                    for (Field field : fields) {
                        addField(builder, field);
                    }
                }
            }
        }

        return builder.build();
    }

    private void addField(SubmissionTemplate.Builder builder, Field field) {
        builder.rules(field, new ArrayList<ValidationRule>(validationRules(field)));
        if (!field.isDeleted() && field.isEditable()) {
            String fieldName = field.getName();
            if (fieldName != null) {
                builder.acceptable(fieldName);

                if (field.isRestricted())
                    builder.restricted(fieldName);

                if (ConstraintUtil.hasConstraint(Constants.ConstraintTypes.IS_VALID_USER, field.getConstraints()))
                    builder.userField(fieldName);
            }
        }
    }

    private Set<ValidationRule> validationRules(Field field) {
        FieldTag fieldTag = FieldTag.getInstance(field.getType());

        String fieldName = field.getName();
        Set<ValidationRule> rules = new HashSet<ValidationRule>();

        if (!field.isEditable())
            return rules;

        if (field.isRequired()) {
            if (fieldTag == FieldTag.FILE)
                rules.add(new ValidationRule.Builder(ValidationRule.ValidationRuleType.REQUIRED_IF_NO_PREVIOUS).name(fieldName).build());
            else
                rules.add(new ValidationRule.Builder(ValidationRule.ValidationRuleType.REQUIRED).name(fieldName).build());
        }

        OptionResolver optionResolver = null;
        List<Constraint> constraints = field.getConstraints();
        if (constraints != null && !constraints.isEmpty()) {
            for (Constraint constraint : constraints) {
                String type = constraint.getType();
                if (type == null)
                    continue;

                if (type.equals(Constants.ConstraintTypes.IS_ONLY_REQUIRED_WHEN))
                    rules.add(new ValidationRule.Builder(ValidationRule.ValidationRuleType.CONSTRAINED)
                            .name(fieldName)
                            .constraint(constraint)
                            .build());

                if (type.equals(Constants.ConstraintTypes.IS_VALID_USER))
                    rules.add(new ValidationRule.Builder(ValidationRule.ValidationRuleType.VALID_USER).name(fieldName).build());
                else if (type.equals(Constants.ConstraintTypes.IS_NUMERIC))
                    rules.add(new ValidationRule.Builder(ValidationRule.ValidationRuleType.NUMERIC).name(fieldName).build());
                else if (type.equals(Constants.ConstraintTypes.IS_EMAIL_ADDRESS))
                    rules.add(new ValidationRule.Builder(ValidationRule.ValidationRuleType.EMAIL).name(fieldName).build());
                else if (type.equals(Constants.ConstraintTypes.IS_ALL_VALUES_MATCH))
                    rules.add(new ValidationRule.Builder(ValidationRule.ValidationRuleType.VALUES_MATCH).name(fieldName).build());
                else if (type.equals(Constants.ConstraintTypes.IS_LIMITED_TO))
                    optionResolver = registry.retrieve(Option.class, constraint.getName());
            }
        }

        if (!FREEFORM_INPUT_TYPES.contains(fieldTag)) {
            List<Option> options = field.getOptions();

            if (optionResolver != null)
                options = optionResolver.getOptions();

            // If no options are stored, then assume that any option is valid
            if (options != null && !options.isEmpty()) {
                rules.add(new ValidationRule.Builder(ValidationRule.ValidationRuleType.CONSTRAINED)
                        .name(fieldName)
                        .options(options).build());
            }
        } else {
            Pattern pattern = field.getPattern() != null ? Pattern.compile(field.getPattern()) : null;
            if (pattern != null)
                rules.add(new ValidationRule.Builder(ValidationRule.ValidationRuleType.PATTERN).name(fieldName).pattern(pattern).build());

            rules.add(new ValidationRule.Builder(ValidationRule.ValidationRuleType.VALUE_LENGTH).name(fieldName).valueLength(field.getMaxValueLength(), field.getMinValueLength()).build());
        }

        if (field.getMaxInputs() > 1 || field.getMinInputs() > 1)
            rules.add(new ValidationRule.Builder(ValidationRule.ValidationRuleType.NUMBER_OF_INPUTS).name(fieldName).numberOfInputs(field.getMaxInputs(), field.getMinInputs()).build());

        return rules;
    }

}
