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

import java.util.*;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import piecework.exception.BadRequestError;
import piecework.exception.StatusCodeError;
import piecework.model.*;
import piecework.model.Process;

import com.google.common.collect.Sets;
import piecework.security.data.DataFilterService;
import piecework.submission.SubmissionTemplate;
import piecework.util.ValidationUtility;

/**
 * @author James Renfro
 */
@Service
public class ValidationFactory {
	
	private static final Set<String> FREEFORM_INPUT_TYPES = Sets.newHashSet("text", "textarea", "person-lookup", "current-date", "current-user");
	private static final Logger LOG = Logger.getLogger(ValidationFactory.class);

    @Autowired
    DataFilterService dataFilterService;

    public Validation validation(Process process, ProcessInstance instance, Task task, SubmissionTemplate template, Submission submission, Entity principal, String version, boolean throwException) throws StatusCodeError {
        long time = 0;

        if (LOG.isDebugEnabled())
            time = System.currentTimeMillis();

        Map<String, List<Value>> instanceData = instance != null ? instance.getData() : Collections.<String, List<Value>>emptyMap();

        // Validate the submission
        Validation validation = validate(process, instance, task, submission, template, principal, version, throwException);

        if (LOG.isDebugEnabled())
            LOG.debug("Validation took " + (System.currentTimeMillis() - time) + " ms");

        Map<String, List<Message>> results = validation.getResults();
        if (throwException && results != null && !results.isEmpty()) {
            // Throw an exception if the submitter needs to adjust the data
            throw new BadRequestError(validation);
        }

        return validation;
    }

    public Validation validate(Process process, ProcessInstance instance, Task task, Submission submission,
                               SubmissionTemplate template,
                               Entity principal, String version,
                               boolean onlyAcceptValidInputs) {

        Map<Field, List<ValidationRule>> fieldRuleMap = template.getFieldRuleMap();
        Set<String> allFieldNames = Collections.unmodifiableSet(new HashSet<String>(template.getFieldMap().keySet()));
        Set<String> fieldNames = new HashSet<String>(template.getFieldMap().keySet());

        Map<String, List<Value>> instanceData = instance != null ? instance.getData() : null;
        Map<String, List<Value>> submissionData = submission.getData();

        Validation.Builder validationBuilder = new Validation.Builder().process(process).instance(instance).submission(submission).task(task);
        if (fieldRuleMap != null) {
            Set<Field> fields = fieldRuleMap.keySet();
            boolean isAllowAny = template.isAnyFieldAllowed();
            String reason = "User is submitting data that needs to be validated";
            Map<String, List<Value>> decryptedSubmissionData = dataFilterService.allSubmissionData(instance, submission, principal, reason);
            Map<String, List<Value>> decryptedInstanceData = null;

            if (task != null)
                decryptedInstanceData = dataFilterService.authorizedInstanceData(instance, task, fields, principal, version, reason, isAllowAny);

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
                        validationBuilder.formValue(fieldName, entry.getValue());
                    }
                }
            }
        }

        return validationBuilder.build();
    }

}
