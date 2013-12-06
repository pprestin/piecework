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

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.Registry;
import piecework.enumeration.ActionType;
import piecework.enumeration.FieldTag;
import piecework.exception.InternalServerError;
import piecework.exception.StatusCodeError;
import piecework.model.*;
import piecework.model.Process;
import piecework.util.ActivityUtil;
import piecework.util.ConstraintUtil;
import piecework.util.OptionResolver;
import piecework.util.ProcessUtility;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author James Renfro
 */
@Service
public class SubmissionTemplateFactory {

    private static final Set<FieldTag> FREEFORM_INPUT_TYPES = Sets.newHashSet(FieldTag.FILE, FieldTag.EMAIL, FieldTag.NUMBER, FieldTag.TEXT, FieldTag.TEXTAREA);

    @Autowired(required=false)
    Registry registry;

    /*
     * Generated an "attachments only submission template", where all fields will be stored as attachments
     */
    public SubmissionTemplate submissionTemplate(Process process, Task task) throws StatusCodeError {
        ProcessDeployment deployment = process.getDeployment();
        if (deployment == null)
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);

        SubmissionTemplate.Builder builder = new SubmissionTemplate.Builder();

        String taskDefinitionKey = task != null ? task.getTaskDefinitionKey() : deployment.getStartActivityKey();

        Activity activity = deployment.getActivity(taskDefinitionKey);
        return submissionTemplate(process, activity, null);
    }

    public SubmissionTemplate submissionTemplate(Field field) {
        SubmissionTemplate.Builder builder = new SubmissionTemplate.Builder();
        addField(builder, field);
        return builder.build();
    }

    private void gatherFieldIds(Container container, List<String> allFieldIds) {
        List<String> fieldIds = container.getFieldIds();
        if (fieldIds != null) {
            allFieldIds.addAll(fieldIds);
        }

        if (container.getChildren() != null && !container.getChildren().isEmpty()) {
            for (Container child : container.getChildren()) {
                gatherFieldIds(child, allFieldIds);
            }
        }
    }

    /*
     * Takes an activity and generates the appropriate submission template for it,
     * limiting to a specific section id
     */
    public SubmissionTemplate submissionTemplate(Process process, Activity activity, String validationId) throws StatusCodeError {
        ProcessDeployment deployment = process.getDeployment();
        if (deployment == null)
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);

        Set<Field> fields = null;

        SubmissionTemplate.Builder builder = new SubmissionTemplate.Builder();

        if (activity.isAllowAttachments()) {
            builder.allowAttachments();
            builder.maxAttachmentSize(activity.getMaxAttachmentSize());
        }

        Container parentContainer = ActivityUtil.parent(activity, ActionType.CREATE);
        Container container = ActivityUtil.child(activity, ActionType.CREATE, parentContainer);
        if (container != null) {
            if (StringUtils.isNotEmpty(validationId))
                container = ProcessUtility.container(container, validationId);

            if (container != null) {
                Map<String, Field> fieldMap = activity.getFieldMap();
                List<String> fieldIds = new ArrayList<String>();

                // These fieldIds ultimately determine which fields will be validated
                int reviewChildIndex = parentContainer.getReviewChildIndex();
                if (reviewChildIndex > -1 && reviewChildIndex == container.getOrdinal()) {
                    // If we're at a review step then we need to gather the fields of all
                    // previous containers owned by the parent
                    List<Container> children = parentContainer.getChildren();
                    for (Container child : children) {
                        if (child.getOrdinal() <= reviewChildIndex)
                            gatherFieldIds(child, fieldIds);
                    }
                } else {
                    // Otherwise we only need to gather the fieldIds from the container that is being validated
                    gatherFieldIds(container, fieldIds);
                }

                if (fieldIds != null) {
                    fields = new TreeSet<Field>();
                    for (String fieldId : fieldIds) {
                        Field field = fieldMap.get(fieldId);
                        if (field != null)
                            fields.add(field);
                    }
                }

                // Only add buttons to the validation from the top-level container, or from
                // the particular validation container that is selected
                List<Button> buttons = parentContainer.getButtons();
                if (buttons != null) {
                    for (Button button : buttons) {
                        if (button == null)
                            continue;
                        builder.button(button);
                    }
                }
            }
        }

        // If we're not validating a single container, or if we weren't able to find the container to validate,
        // then simply validate all fields for this activity
        if (fields == null)
            fields = activity.getFields();

        if (fields != null) {
            for (Field field : fields) {
                addField(builder, field);
            }
        }

        return builder.build();
    }

    /*
     * Takes a screen and generates the appropriate submission template for it,
     * limiting to a specific section id
     */
//    public SubmissionTemplate submissionTemplate(Process process, Screen screen, String validationId) throws StatusCodeError {
//        List<Grouping> groupings = screen.getGroupings();
//        ProcessDeployment deployment = process.getDeployment();
//        if (deployment == null)
//            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
//
//        Map<String, Section> sectionMap = deployment.getSectionMap();
//
//        SubmissionTemplate.Builder builder = new SubmissionTemplate.Builder();
//        if (groupings != null && sectionMap != null && !sectionMap.isEmpty()) {
//            for (Grouping grouping : groupings) {
//                if (grouping == null)
//                    continue;
//
//                String groupingId = grouping.getGroupingId();
//                if (groupingId != null && validationId != null && !groupingId.equals(validationId))
//                    continue;
//
//                List<Button> buttons = grouping.getButtons();
//                if (buttons != null) {
//                    for (Button button : buttons) {
//                        if (button == null)
//                            continue;
//                        builder.button(button);
//                    }
//                }
//
//                List<String> sectionsIds = grouping.getSectionIds();
//                if (sectionsIds == null)
//                    continue;
//
//                for (String sectionId : sectionsIds) {
//                    Section section = sectionMap.get(sectionId);
//                    if (section == null)
//                        continue;
//
//                    List<Field> fields = section.getFields();
//                    if (fields == null || fields.isEmpty())
//                        continue;
//
//                    for (Field field : fields) {
//                        addField(builder, field);
//                    }
//                }
//            }
//        }
//
//        return builder.build();
//    }

    private void addField(SubmissionTemplate.Builder builder, Field field) {
        if (!field.isDeleted() && field.isEditable()) {
            builder.rules(field, new ArrayList<ValidationRule>(validationRules(field)));
            builder.field(field);

//            String fieldName = field.getName();
//            if (fieldName != null) {
//                if (field.isRestricted())
//                    builder.restricted(fieldName);
//                else
//                    builder.acceptable(fieldName);
//
//                if (field.getType().equals(Constants.FieldTypes.PERSON))
//                    builder.userField(fieldName);
//
//                builder.field(field);
//            } else if (field.getType() != null && field.getType().equals(FieldTag.CHECKBOX.getTagName())) {
//                List<Option> options = field.getOptions();
//                if (options != null) {
//                    for (Option option : options) {
//                        if (field.isRestricted())
//                            builder.restricted(option.getName());
//                        else
//                            builder.acceptable(option.getName());
//                    }
//                }
//            }
        }
    }

    private Set<ValidationRule> validationRules(Field field) {
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
