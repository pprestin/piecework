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

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.Registry;
import piecework.enumeration.ActionType;
import piecework.enumeration.FieldTag;
import piecework.exception.MisconfiguredProcessException;
import piecework.model.*;
import piecework.model.Process;
import piecework.util.ActivityUtil;
import piecework.form.OptionResolver;
import piecework.util.ProcessUtility;
import piecework.validation.ValidationRule;

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

    public SubmissionTemplate submissionTemplate(Process process, ProcessDeployment deployment, Task task, FormRequest formRequest) throws MisconfiguredProcessException {
        return submissionTemplate(process, deployment, task, formRequest, null);
    }

    public SubmissionTemplate submissionTemplate(Process process, ProcessDeployment deployment, Task task, FormRequest formRequest, String validationId) throws MisconfiguredProcessException {
        if (deployment == null)
            throw new MisconfiguredProcessException("Deployment not specified in submission");

        Activity activity = formRequest.getActivity();
        if (activity == null) {
            String taskDefinitionKey = task != null ? task.getTaskDefinitionKey() : deployment.getStartActivityKey();
            activity = deployment.getActivity(taskDefinitionKey);
        }
        return submissionTemplate(process, deployment, formRequest, activity, validationId);
    }

    public SubmissionTemplate submissionTemplate(Process process, Field field, FormRequest formRequest) {
        SubmissionTemplate.Builder builder = new SubmissionTemplate.Builder(process, process.getDeployment());
        if (formRequest != null)
            builder.requestId(formRequest.getRequestId()).taskId(formRequest.getTaskId()).actAsUser(formRequest.getActAsUser());

        addField(builder, field);
        return builder.build();
    }

    public SubmissionTemplate submissionTemplate(Process process, ProcessDeployment deployment, FormRequest formRequest) throws MisconfiguredProcessException {
        return submissionTemplate(process, deployment, formRequest, formRequest.getActivity(), null);
    }

    public SubmissionTemplate submissionTemplate(Process process, ProcessDeployment deployment, FormRequest formRequest, String validationId) throws MisconfiguredProcessException {
        return submissionTemplate(process, deployment, formRequest, formRequest.getActivity(), validationId);
    }

    /*
     * Takes an activity and generates the appropriate submission template for it,
     * limiting to a specific section id
     */
    private SubmissionTemplate submissionTemplate(Process process, ProcessDeployment deployment, FormRequest formRequest, Activity activity, String validationId) throws MisconfiguredProcessException {
        Set<Field> fields = null;

        SubmissionTemplate.Builder builder = new SubmissionTemplate.Builder(process, deployment);

        boolean includeFields = false;
        if (formRequest != null) {
            builder.requestId(formRequest.getRequestId()).taskId(formRequest.getTaskId()).actAsUser(formRequest.getActAsUser());
            ActionType actionType = formRequest.getAction();
            includeFields = actionType == ActionType.CREATE || actionType == ActionType.COMPLETE || actionType == ActionType.VALIDATE || actionType == ActionType.SAVE;
        }

        if (activity.isAllowAttachments()) {
            builder.allowAttachments();
            builder.maxAttachmentSize(activity.getMaxAttachmentSize());
        }

        if (includeFields) {
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
        }
        return builder.build();
    }

    private void addField(SubmissionTemplate.Builder builder, Field field) {
        if (!field.isDeleted() && field.isEditable()) {
            builder.rules(field, new ArrayList<ValidationRule>(validationRules(field)));
            builder.field(field);
        }
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
