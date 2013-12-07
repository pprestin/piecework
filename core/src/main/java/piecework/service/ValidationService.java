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
package piecework.service;

import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import piecework.Constants;
import piecework.exception.BadRequestError;
import piecework.exception.StatusCodeError;
import piecework.exception.ValidationRuleException;
import piecework.model.*;
import piecework.model.Process;

import com.google.common.collect.Sets;
import piecework.security.DataFilterService;
import piecework.util.ManyMap;
import piecework.validation.FormValidation;
import piecework.submission.SubmissionTemplate;
import piecework.validation.ValidationRule;

/**
 * @author James Renfro
 */
@Service
public class ValidationService {
	
	private static final Set<String> FREEFORM_INPUT_TYPES = Sets.newHashSet("text", "textarea", "person-lookup", "current-date", "current-user");
	private static final Logger LOG = Logger.getLogger(ValidationService.class);

    @Autowired
    DataFilterService dataFilterService;

    @Autowired
    IdentityService identityService;

    @Autowired
    TaskService taskService;

    public FormValidation validate(Process process, ProcessInstance instance, Task task, SubmissionTemplate template, Submission submission, boolean throwException) throws StatusCodeError {
        long time = 0;

        if (LOG.isDebugEnabled())
            time = System.currentTimeMillis();

        taskService.checkIsActiveIfTaskExists(process, task);

        // Validate the submission
        FormValidation validation = validate(instance, template, submission, throwException);

        if (LOG.isDebugEnabled())
            LOG.debug("Validation took " + (System.currentTimeMillis() - time) + " ms");

        Map<String, List<Message>> results = validation.getResults();
        if (throwException && results != null && !results.isEmpty()) {
            // Throw an exception if the submitter needs to adjust the data
            throw new BadRequestError(validation);
        }

        return validation;
    }

    private FormValidation validate(ProcessInstance instance, SubmissionTemplate template, Submission submission, boolean onlyAcceptValidInputs) {

        FormValidation.Builder validationBuilder = new FormValidation.Builder().instance(instance).submission(submission);

        Map<Field, List<ValidationRule>> fieldRuleMap = template.getFieldRuleMap();

        if (fieldRuleMap != null) {
            Set<String> fieldNames = new HashSet<String>(template.getFieldMap().keySet());
            Map<String, List<Value>> submissionData = submission.getData();
            Map<String, List<Value>> instanceData = instance != null ? instance.getData() : Collections.<String, List<Value>>emptyMap();

            ManyMap<String, Value> decryptedSubmissionData = dataFilterService.decrypt(submissionData);
            ManyMap<String, Value> decryptedInstanceData = dataFilterService.decrypt(instanceData);

            for (Map.Entry<Field, List<ValidationRule>> entry : fieldRuleMap.entrySet()) {
                Field field = entry.getKey();
                List<ValidationRule> rules = entry.getValue();
                if (rules != null) {
                    for (ValidationRule rule : rules) {
                        try {
                            rule.evaluate(decryptedSubmissionData, decryptedInstanceData);
                        } catch (ValidationRuleException e) {
                            LOG.warn("Invalid input: " + e.getMessage() + " " + e.getRule());

                            validationBuilder.error(rule.getName(), e.getMessage());
                            if (onlyAcceptValidInputs) {
                                fieldNames.remove(rule.getName());
                            }
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
                    Map<String, String> optionMap = field.getOptionMap();

                    continue;
                }

                if (fieldNames.contains(fieldName)) {
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

    public List<? extends Value> users(List<? extends Value> values) {
        if (values.isEmpty())
            return Collections.emptyList();

        List<User> list = new ArrayList<User>(values.size());
        for (Value value : values) {
            if (value instanceof User) {
                list.add(User.class.cast(value));
            } else {
                User user = identityService.getUser(value.getValue());
                if (user != null)
                    list.add(user);
            }
        }

        return list;
    }

}
