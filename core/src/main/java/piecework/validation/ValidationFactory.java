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
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.exception.BadRequestError;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.security.data.DataFilterService;
import piecework.submission.SubmissionTemplate;
import piecework.util.ModelUtility;
import piecework.util.ValidationUtility;

import java.util.*;

/**
 * @author James Renfro
 */
@Service
public class ValidationFactory {
	
	private static final Set<String> FREEFORM_INPUT_TYPES = Sets.newHashSet("text", "textarea", "person-lookup", "current-date", "current-user");
	private static final Logger LOG = Logger.getLogger(ValidationFactory.class);

    @Autowired
    DataFilterService dataFilterService;

    public <P extends ProcessDeploymentProvider> Validation validation(P modelProvider, SubmissionTemplate template, Submission submission, String version, boolean throwException) throws PieceworkException {
        long time = 0;

        if (LOG.isDebugEnabled())
            time = System.currentTimeMillis();

        // Validate the submission
        Validation validation = validate(modelProvider, submission, template, version, throwException);

        if (LOG.isDebugEnabled())
            LOG.debug("Validation took " + (System.currentTimeMillis() - time) + " ms");

        Map<String, List<Message>> results = validation.getResults();
        if (throwException && results != null && !results.isEmpty()) {
            // Throw an exception if the submitter needs to adjust the data
            throw new BadRequestError(validation);
        }

        return validation;
    }

    public <P extends ProcessDeploymentProvider> Validation validate(P modelProvider,
                                Submission submission, SubmissionTemplate template, String version, boolean onlyAcceptValidInputs) throws PieceworkException {

        Map<Field, List<ValidationRule>> fieldRuleMap = template.getFieldRuleMap();
        Set<String> allFieldNames = Collections.unmodifiableSet(new HashSet<String>(template.getFieldMap().keySet()));
        Set<String> fieldNames = new HashSet<String>(template.getFieldMap().keySet());

        ProcessInstance instance = ModelUtility.instance(modelProvider);
        Map<String, List<Value>> instanceData = instance != null ? instance.getData() : null;
        Map<String, List<Value>> submissionData = submission.getData();

        Validation.Builder validationBuilder = new Validation.Builder().submission(submission);
        if (fieldRuleMap != null) {
            Set<Field> fields = fieldRuleMap.keySet();
            boolean isAllowAny = template.isAnyFieldAllowed();
            String reason = "User is submitting data that needs to be validated";
            Map<String, List<Value>> decryptedSubmissionData = dataFilterService.allSubmissionData(modelProvider, submission, reason);
            Map<String, List<Value>> decryptedInstanceData = null;

            Task task = ModelUtility.task(modelProvider);
            if (task != null)
                decryptedInstanceData = dataFilterService.authorizedInstanceData(modelProvider, fields, version, reason, isAllowAny);

            for (Map.Entry<Field, List<ValidationRule>> entry : fieldRuleMap.entrySet()) {
                Field field = entry.getKey();
                List<ValidationRule> rules = entry.getValue();
                ValidationUtility.validateField(validationBuilder, field, rules, fieldNames, submissionData, instanceData, decryptedSubmissionData, decryptedInstanceData, onlyAcceptValidInputs);
            }
        }

        if (template.isAnyFieldAllowed()) {
            if (!submissionData.isEmpty()) {
                for (Map.Entry<String, List<Value>> entry : submissionData.entrySet()) {
                    String fieldName = entry.getKey();

                    if (!allFieldNames.contains(fieldName)) {
                        List<? extends Value> values = entry.getValue();
                        List<? extends Value> previousValues = instanceData != null ? instanceData.get(fieldName) : null;

                        if (isFile(values, previousValues))
                            values = ValidationUtility.append(values, previousValues);

                        validationBuilder.formValue(fieldName, values);
                    }
                }
            }
        }

        return validationBuilder.build();
    }

    private static boolean isFile(List<? extends Value> values, List<? extends Value> previousValues) {
        return isFile(values) || isFile(previousValues);
    }

    private static boolean isFile(List<? extends Value> values) {
        if (values != null && !values.isEmpty()) {
            Value value = values.iterator().next();
            return value instanceof File;
        }
        return false;
    }

}
