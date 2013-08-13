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

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import piecework.Constants;
import piecework.Registry;
import piecework.exception.ValidationRuleException;
import piecework.identity.InternalUserDetailsService;
import piecework.model.*;
import piecework.process.concrete.ResourceHelper;

import com.google.common.collect.Sets;
import piecework.security.EncryptionService;
import piecework.util.ConstraintUtil;
import piecework.util.ManyMap;

/**
 * @author James Renfro
 */
@Service
public class ValidationService {
	
	private static final Set<String> FREEFORM_INPUT_TYPES = Sets.newHashSet("text", "textarea", "person-lookup", "current-date", "current-user");
	private static final Logger LOG = Logger.getLogger(ValidationService.class);

	@Autowired(required=false)
	Registry registry;

    @Autowired
    ResourceHelper helper;

    @Autowired
    EncryptionService encryptionService;

    @Autowired
    InternalUserDetailsService userDetailsService;


    public FormValidation validate(ProcessInstance instance, SubmissionTemplate template, Submission submission, boolean onlyAcceptValidInputs) {


        FormValidation.Builder validationBuilder = new FormValidation.Builder().instance(instance).submission(submission);

        Map<Field, List<ValidationRule>> fieldRuleMap = template.getFieldRuleMap();

        if (fieldRuleMap != null) {
            Set<String> acceptableFieldNames = new HashSet<String>(template.getAcceptable());
            Map<String, List<Value>> submissionData = submission.getData();
            Map<String, List<Value>> instanceData = instance != null ? instance.getData() : Collections.<String, List<Value>>emptyMap();

            ManyMap<String, Value> decryptedSubmissionData = decrypted(submissionData);
            ManyMap<String, Value> decryptedInstanceData = decrypted(instanceData);

            for (Map.Entry<Field, List<ValidationRule>> entry : fieldRuleMap.entrySet()) {
                Field field = entry.getKey();
                List<ValidationRule> rules = entry.getValue();
                if (rules != null) {
                    for (ValidationRule rule : rules) {
                        try {
                            rule.evaluate(decryptedSubmissionData, decryptedInstanceData);
                        } catch (ValidationRuleException e) {
                            validationBuilder.error(rule.getName(), e.getMessage());
                            if (onlyAcceptValidInputs)
                                acceptableFieldNames.remove(rule.getName());
                        }
                    }
                }
                String fieldName = field.getName();

                if (fieldName == null) {
                    if (field.getType() != null && field.getType().equalsIgnoreCase(Constants.FieldTypes.CHECKBOX)) {
                        List<Option> options = field.getOptions();
                        if (options != null) {
                            for (Option option : options) {
                                if (StringUtils.isNotEmpty(option.getName()) && submissionData.containsKey(option.getName()))
                                    fieldName = option.getName();
                            }
                        }
                    }
                }

                if (fieldName == null) {
                    LOG.warn("Field is missing name " + field.getFieldId());
                    continue;
                }

                if (acceptableFieldNames.contains(fieldName)) {
                    List<? extends Value> values = submissionData.get(fieldName);
                    List<? extends Value> previousValues = instanceData.get(fieldName);

                    boolean isFileField = field.getType() != null && field.getType().equals(Constants.FieldTypes.FILE);
                    if (values == null) {
                        // Files are a special case, in that we don't want to wipe them out if they aren't resubmitted
                        // on every request
                        if (isFileField)
                            values = previousValues;

                    } else if (isFileField && field.getMaxInputs() > 1) {
                        // With file fields that accept multiple files, we want to append each submission
                        values = append(values, previousValues);
                    }

                    if (values == null)
                        values = Collections.emptyList();

//                    if (ConstraintUtil.hasConstraint(Constants.ConstraintTypes.IS_VALID_USER, field.getConstraints()))
//                        messages = users(messages);

                    validationBuilder.formValue(fieldName, values.toArray(new Value[values.size()]));
                }
            }
        }

        return validationBuilder.build();
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

    public ManyMap<String, Value> decrypted(Map<String, List<Value>> original) {
        ManyMap<String, Value> map = new ManyMap<String, Value>();

        if (original != null && !original.isEmpty()) {
            for (Map.Entry<String, List<Value>> entry : original.entrySet()) {
                String key = entry.getKey();
                try {
                    List<Value> decrypted = decrypted(entry.getValue());
                    map.put(key, decrypted);
                } catch (Exception e) {
                    LOG.error("Could not decrypt messages for " + key, e);
                }
            }
        }

        return map;
    }

    public List<Value> decrypted(List<? extends Value> values) throws UnsupportedEncodingException, GeneralSecurityException, InvalidCipherTextException {
        if (values.isEmpty())
            return Collections.emptyList();

        List<Value> list = new ArrayList<Value>(values.size());
        for (Value value : values) {
            if (value instanceof Secret) {
                Secret secret = Secret.class.cast(value);
                String plaintext = encryptionService.decrypt(secret);
                list.add(new Value(plaintext));
            } else {
                list.add(value);
            }
        }

        return list;
    }

    public List<? extends Value> users(List<? extends Value> values) {
        if (values.isEmpty())
            return Collections.emptyList();

        List<User> list = new ArrayList<User>(values.size());
        for (Value value : values) {
            if (value instanceof User) {
                list.add(User.class.cast(value));
            } else {
                User user = userDetailsService.getUserByAnyId(value.getValue());
                if (user != null)
                    list.add(user);
            }
        }

        return list;
    }


    /*public FormValidation validate(Submission submission, ProcessInstance instance, Screen screen) {
        return validate(submission, instance, screen, null);
    }
	
	public FormValidation validate(Submission submission, ProcessInstance instance, Screen screen, String validationId) {
		
		long start = System.currentTimeMillis();
		
		FormValidation.Builder validationBuilder = new FormValidation.Builder().instance(instance).submission(submission);

        boolean isAttachmentAllowed = screen == null || screen.isAttachmentAllowed();

		// Only include attachments in the validation result if attachments are allowed
//		if (isAttachmentAllowed)
//			validationBuilder.attachments(submission.getAttachments());

		Map<String, FormValue> submissionValueMap = submission.getFormValueMap();
//        ManyMap<String, Attachment> submissionAttachmentMap = submission.getAttachmentMap();
        Set<String> unvalidatedFieldNames = new HashSet<String>(submissionValueMap.keySet());
        ManyMap<String, Attachment> instanceAttachmentMap = instance != null ? instance.getAttachmentMap() : new ManyMap<String, Attachment>();
        Map<String, FormValue> instanceValueMap = instance != null ? instance.getFormValueMap() : new HashMap<String, FormValue>();

//        String title = submissionValueMap.get("title");
//        validationBuilder.title(title);

        if (screen != null) {
            List<Section> sections = screen.getSections();
            List<Grouping> groupings = screen.getGroupings();

            if (groupings != null) {
                for (Grouping grouping : groupings) {
                    String groupingId = grouping.getGroupingId();

                    List<Button> buttons = grouping.getButtons();
                    if (buttons != null && !buttons.isEmpty()) {
                        for (Button button : buttons) {
                            if (StringUtils.isEmpty(button.getName()) || StringUtils.isEmpty(button.getValue()))
                                continue;

                            unvalidatedFieldNames.remove(button.getName());

                            FormValue variable = submissionValueMap.get(button.getName());
                            List<String> messages = variable != null ? variable.getAllValues() : null;

                            if (messages != null && !messages.isEmpty()) {
                                for (String value : messages) {
                                    if (StringUtils.isEmpty(value))
                                        continue;

                                    if (value.equals(button.getValue()))
                                        validationBuilder.variable(variable);
                                }
                            }
                        }
                    }

                    // If a validation id is passed, then limit the validation to the grouping that matches
                    if (groupingId != null && validationId != null && groupingId.equals(validationId)) {
                        Set<String> sectionIdSet = new HashSet<String>(grouping.getSectionIds());
                        List<Section> filteredSections = new ArrayList<Section>();
                        for (Section section : sections) {
                            if (section.getSectionId() != null && sectionIdSet.contains(section.getSectionId()))
                                filteredSections.add(section);
                        }
                        sections = filteredSections;
                    }

                }

            }

            if (sections != null) {
                Map<String, Field> fieldMap = new HashMap<String, Field>();
                for (Section section : sections) {
                    if (section == null)
                        continue;

                    List<Field> fields = section.getFields();
                    if (fields == null || fields.isEmpty())
                        continue;


                    for (Field field : fields) {
                        String fieldName = field.getName();

                        if (fieldName == null)
                            continue;

                        fieldMap.put(field.getName(), field);
                    }

                }
                for (Section section : sections) {
                    if (section == null)
                        continue;

                    List<Field> fields = section.getFields();
                    if (fields == null || fields.isEmpty())
                        continue;

                    for (Field field : fields) {
                        String fieldName = field.getName();

                        if (fieldName == null) {
                            if (field.getType() != null && field.getType().equalsIgnoreCase(Constants.FieldTypes.CHECKBOX)) {
                                List<Option> options = field.getOptions();
                                if (options != null) {
                                    for (Option option : options) {
                                        if (unvalidatedFieldNames.contains(option.getValue())) {
                                            fieldName = option.getValue();
                                            unvalidatedFieldNames.remove(fieldName);
                                            validateField(validationId, field, fieldMap, submissionValueMap, submissionAttachmentMap, instanceValueMap, instanceAttachmentMap, validationBuilder);
                                        }
                                    }
                                }
                            } else {
                                LOG.warn("Field is missing name " + field.getFieldId());
                                continue;
                            }
                        } else {
                            // Remove any form messages from the attachment map, since this has a field already
                            unvalidatedFieldNames.remove(fieldName);
                            validateField(validationId, field, fieldMap, submissionValueMap, submissionAttachmentMap, instanceValueMap, instanceAttachmentMap, validationBuilder);
                        }
                    }
                }
            }
        }

        if (isAttachmentAllowed) {

            if (submissionAttachmentMap != null && !submissionAttachmentMap.isEmpty()) {
                for (List<Attachment> attachments : submissionAttachmentMap.messages()) {
                    if (attachments == null || attachments.isEmpty())
                        continue;
                    for (Attachment attachment : attachments) {
                        validationBuilder.attachment(attachment);
                    }
                }
            }

//            List<FormValue> formValues = submission.getFormData();
//            if (formValues != null && !formValues.isEmpty()) {
//                String userId = null;
//                InternalUserDetails userDetails = helper.getAuthenticatedPrincipal();
//                if (userDetails != null)
//                    userId = userDetails.getInternalId();
//
//                for (FormValue variable : formValues) {
//                    if (unvalidatedFieldNames.contains(variable.getName())) {
//                        List<String> messages = variable.getAllValues();
//
//                        if (messages != null && !messages.isEmpty()) {
//                            for (String value : messages) {
//                                Attachment.Builder attachmentBuilder = new Attachment.Builder()
//                                        .name(variable.getName())
//                                        .description(value)
//                                        .lastModified(new Date())
//                                        .userId(userId);
//
//                                if (variable.getContentType() != null) {
//                                    attachmentBuilder
//                                        .contentType(variable.getContentType())
//                                        .location(variable.getLocation());
//                                } else {
//                                    attachmentBuilder.contentType("text/plain");
//                                }
//                                validationBuilder.attachment(attachmentBuilder.build());
//                            }
//                        }
//                    }
//                }
//            }
        }
		
		if (LOG.isDebugEnabled()) {
			long end = System.currentTimeMillis();
			LOG.debug("Validation took " + (end-start) + " milliseconds");
		}

		return validationBuilder.build();
	}


    private void validateField(String validationId, Field field, Map<String, Field> fieldMap, Map<String, FormValue> submissionValueMap, ManyMap<String, Attachment> submissionAttachmentMap,
                               Map<String, FormValue> instanceValueMap, ManyMap<String, Attachment> instanceAttachmentMap, FormValidation.Builder validationBuilder) {
        boolean hasErrorResult = false;
        String fieldName = field.getName();

        FormValue variable = submissionValueMap.get(fieldName);
        List<String> messages = variable != null ? variable.getMessages() : null;
        FormValue previousFormValue = instanceValueMap != null ? instanceValueMap.get(fieldName) : null;
        List<String> previousValues = previousFormValue != null ? previousFormValue.getMessages() : null;
        String inputType = field.getType();

        boolean isEmailAddress = false;
        boolean isNumeric = false;
        boolean isPersonLookup = false;
        boolean isAllValuesMatch = false;
        boolean isOnlyRequiredWhen = false;
        boolean isText = inputType == null || FREEFORM_INPUT_TYPES.contains(inputType);
        boolean isFile = inputType != null && inputType.equals(Constants.FieldTypes.FILE);
        boolean isFieldSpecificUpdate = validationId != null && validationId.equals(fieldName);
        boolean hasValues = !isFullyEmpty(messages);
        boolean hasPreviousValues = !isFullyEmpty(previousValues);
        boolean hasAtLeastEmptyValue = messages != null && !messages.isEmpty();

        OptionResolver optionResolver = null;

        List<Constraint> onlyRequiredWhenConstraints = null;

        List<Constraint> constraints = field.getConstraints();
        if (constraints != null && !constraints.isEmpty()) {
            for (Constraint constraint : constraints) {
                String type = constraint.getType();
                if (type == null)
                    continue;

                isOnlyRequiredWhen = type.equals(Constants.ConstraintTypes.IS_ONLY_REQUIRED_WHEN);

                if (isOnlyRequiredWhen) {
                    if (onlyRequiredWhenConstraints == null)
                        onlyRequiredWhenConstraints = new ArrayList<Constraint>();
                    onlyRequiredWhenConstraints.add(constraint);
                }

                if (type.equals(Constants.ConstraintTypes.IS_VALID_USER))
                    isPersonLookup = true;
                else if (type.equals(Constants.ConstraintTypes.IS_NUMERIC))
                    isNumeric = true;
                else if (type.equals(Constants.ConstraintTypes.IS_EMAIL_ADDRESS))
                    isEmailAddress = true;
                else if (type.equals(Constants.ConstraintTypes.IS_ALL_VALUES_MATCH))
                    isAllValuesMatch = true;
                else if (type.equals(Constants.ConstraintTypes.IS_LIMITED_TO)) {
                    optionResolver = registry.retrieve(Option.class, constraint.getName());
                }
            }
        }

        boolean isUnchanged = false;

        if (hasValues) {

            if (!field.isEditable()) {
                hasErrorResult = true;
                validationBuilder.error(fieldName, "Not permitted to set a value for this property");
                return;
            }

            // Check to make sure that we're not resubmitting a value that already exists in the database for this property
            if (previousValues != null && !previousValues.isEmpty()) {
                isUnchanged = true;
                for (int i=0;i<messages.size();i++) {
                    String previousValue = previousValues.size() > i ? previousValues.get(i) : null;
                    String currentValue = messages.get(i);

                    if (previousValue == null || !previousValue.equals(currentValue)) {
                        isUnchanged = false;
                        break;
                    }
                }
                validationBuilder.unchangedField(fieldName);
            }

            // Ensure that messages for non-text properties all match valid options
            if (!isText) {
                List<Option> options = field.getOptions();

                if (optionResolver != null)
                    options = optionResolver.getOptions();

                // If no options are stored, then assume that any option is valid
                if (options != null && !options.isEmpty()) {

                    // FIXME: This is not efficient when there is only a single value (the majority of the time)
                    Set<String> optionSet = new HashSet<String>();
                    for (Option option : options) {
                        optionSet.add(option.getValue());
                    }

                    if (messages != null) {
                        for (String value : messages) {
                            if (value != null) {
                                if (!optionSet.contains(value)) {
                                    hasErrorResult = true;
                                    validationBuilder.error(fieldName, "Not a valid option for this field");
                                }
                            }
                        }
                    }
                }
            } else {

                int counter = 0;

                String lastValue = null;

                Pattern pattern = field.getPattern() != null ? Pattern.compile(field.getPattern()) : null;

                if (field != null) {
                    if (isPersonLookup) {
                        FormValue.Builder displayNameBuilder = new FormValue.Builder().name(fieldName + "__displayName");
                        FormValue.Builder visibleIdBuilder = new FormValue.Builder().name(fieldName + "__visibleId");
                        for (String value : messages) {
                            User user = userDetailsService.getUserByAnyId(value);
                            if (user != null) {
                                displayNameBuilder.value(user.getDisplayName());
                                visibleIdBuilder.value(user.getVisibleId());
                            }
                        }
                        validationBuilder.variable(displayNameBuilder.build());
                        validationBuilder.variable(visibleIdBuilder.build());
                    }
                }

                for (String value : messages) {

                    if (value.length() > field.getMaxValueLength()) {
                        hasErrorResult = true;
                        validationBuilder.error(fieldName, "Cannot be more than " + field.getMaxValueLength() + " characters");
                    } else if (value.length() < field.getMinValueLength()) {
                        hasErrorResult = true;
                        validationBuilder.error(fieldName, "Cannot be less than " + field.getMinValueLength() + " characters");
                    }

                    if (isNumeric && !value.matches("^[0-9]+$")) {
                        hasErrorResult = true;
                        validationBuilder.error(fieldName, "Must be a number");
                    }

                    if (isEmailAddress && !EmailValidator.getInstance().isValid(value)) {
                        hasErrorResult = true;
                        validationBuilder.error(fieldName, "Is not a valid email address");
                    }

                    if (pattern != null && !pattern.matcher(value).matches()) {
                        hasErrorResult = true;
                        String examplePattern = field.getMask();
                        if (examplePattern == null)
                            examplePattern = field.getPattern();
                        validationBuilder.error(fieldName, "Does not match required pattern: " + examplePattern);
                    }

                    if (isAllValuesMatch) {
                        if (lastValue != null && !lastValue.equals(value)) {
                            hasErrorResult = true;
                            validationBuilder.error(fieldName, "Values do not match");
                            break;
                        }

                        lastValue = value;
                    }

                    if (isPersonLookup) {
                        InternalUserDetails userDetails = userDetailsService.loadUserByAnyId(value);

                        if (userDetails == null) {
                            hasErrorResult = true;
                            validationBuilder.error(fieldName, "This identifier is not recognized as a person in the system " + value);
                        }
                    }

                    counter++;
                }

                if (field.getMaxInputs() < counter) {
                    hasErrorResult = true;
                    validationBuilder.error(fieldName, "No more than " + field.getMaxInputs() + " messages are allowed");
                } else if (field.getMinInputs() > counter) {
                    hasErrorResult = true;
                    validationBuilder.error(fieldName, "At least " + field.getMinInputs() + " messages are required");
                }
            }
        } else if (isFile) {
            List<Attachment> attachments = submissionAttachmentMap.remove(fieldName);
            List<Attachment> previousAttachments = instanceAttachmentMap.get(fieldName);

            boolean hasAttachments = attachments != null && !attachments.isEmpty();
            boolean hasPreviousAttachments = previousAttachments != null && !previousAttachments.isEmpty();

            if (!hasAttachments && !hasPreviousAttachments && field.isRequired()) {
                validationBuilder.error(fieldName, "Field is required");
            } else if (hasAttachments) {
                PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
                for (Attachment attachment : attachments) {
                    validationBuilder.attachment(new Attachment.Builder(attachment, passthroughSanitizer).fieldAttachment().build());
                }
            }

        } else if (hasPreviousValues) {
            // File submission is a special case, since we don't want to overwrite an existing file
            if (isFile && previousFormValue != null) {
                validationBuilder.variable(previousFormValue);
            }
        } else {

            // No value was passed and this is a property that cannot be created or edited at this time,
            // so just bail out so we don't erroneously create a SUCCESS validation for it
            if (!field.isEditable())
                return;

            boolean isRequired = field.isRequired();

            String requiredText = "Field is required";
            if (inputType != null && inputType.equalsIgnoreCase(Constants.FieldTypes.CHECKBOX))
                requiredText = "Please check this box if you want to proceed";

            if (ConstraintUtil.hasConstraint(Constants.ConstraintTypes.IS_ONLY_REQUIRED_WHEN, constraints)) {
                if (ConstraintUtil.checkAll(Constants.ConstraintTypes.IS_ONLY_REQUIRED_WHEN, fieldMap, submissionValueMap, constraints)) {
                    hasErrorResult = true;
                    validationBuilder.error(fieldName, requiredText);
                }
            } else if (isRequired) {
                if (isFieldSpecificUpdate) {
                    validationBuilder.warning(fieldName, requiredText);
                    return;
                } else {
                    if (hasPreviousValues)
                        return;

                    hasErrorResult = true;
                    validationBuilder.error(fieldName, requiredText);
                    return;
                }
            }

            // Handle special case for checkboxes that have a single option -- they're either on or off, and when they're off nothing
            // is passed in... this is valid generally, unless the checkbox is 'required', in which case it has to be checked and the
            // code directly above this will send back an error
            if (inputType != null && inputType.equalsIgnoreCase(Constants.FieldTypes.CHECKBOX)) {

                // So basically what this means is that checkboxes that send nothing at all, not even an empty string, will be treated
                // exactly as if they had sent an empty string, assuming they have only one option
                if (field.getOptions() != null && field.getOptions().size() == 1) {
                    hasAtLeastEmptyValue = true;
                }
            }

        }

        if (!hasErrorResult && hasAtLeastEmptyValue) {
            if (messages != null && !messages.isEmpty()) {
                // Ensure that we save the form value
                if (field.isRestricted())
                    validationBuilder.restrictedValue(variable);
                else
                    validationBuilder.variable(variable);
            }
        }
    }   */

	private static boolean isFullyEmpty(List<String> list) {
        if (list == null || list.size() == 0) {
            return true;
        }
        for (String item : list) {
        	if (!StringUtils.isEmpty(item))
        		return false;
        }
        return true;
    }
	
}
