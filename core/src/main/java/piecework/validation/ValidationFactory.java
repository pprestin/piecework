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
import piecework.service.TaskService;
import piecework.common.ManyMap;
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

    public Validation validation(Process process, ProcessInstance instance, Task task, SubmissionTemplate template, Submission submission, boolean throwException) throws StatusCodeError {
        long time = 0;

        if (LOG.isDebugEnabled())
            time = System.currentTimeMillis();

        Map<String, List<Value>> submissionData = submission.getData();
        Map<String, List<Value>> instanceData = instance != null ? instance.getData() : Collections.<String, List<Value>>emptyMap();

        ManyMap<String, Value> decryptedSubmissionData = dataFilterService.decrypt(submissionData);
        ManyMap<String, Value> decryptedInstanceData = dataFilterService.decrypt(instanceData);

        // Validate the submission
        Validation validation = ValidationUtility.validate(process, instance, task, submission, template, decryptedSubmissionData, decryptedInstanceData, throwException);

        if (LOG.isDebugEnabled())
            LOG.debug("Validation took " + (System.currentTimeMillis() - time) + " ms");

        Map<String, List<Message>> results = validation.getResults();
        if (throwException && results != null && !results.isEmpty()) {
            // Throw an exception if the submitter needs to adjust the data
            throw new BadRequestError(validation);
        }

        return validation;
    }

}
