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
package piecework.submission.concrete;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import piecework.exception.PieceworkException;
import piecework.model.Entity;
import piecework.model.ProcessInstance;
import piecework.model.Submission;
import piecework.submission.SubmissionTemplate;

import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
@Service
public class FormValueSubmissionHandler extends AbstractSubmissionHandler<Map<String, List<String>>> {

    private static final Logger LOG = Logger.getLogger(FormValueSubmissionHandler.class);

    @Override
    protected Submission handleInternal(ProcessInstance instance, Map<String, List<String>> data, SubmissionTemplate template, Entity principal) throws PieceworkException {
        String principalId = principal != null ? principal.getEntityId() : "anonymous";
        String actingAsId = principal != null ? principal.getActingAsId() : "anonymous";
        Submission.Builder submissionBuilder = submissionBuilder(template, principal);

        if (data != null && !data.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : data.entrySet()) {
                String name = sanitizer.sanitize(entry.getKey());
                List<String> rawValues = entry.getValue();

                if (rawValues != null) {
                    for (String rawValue : rawValues) {
                        String value = sanitizer.sanitize(rawValue);
                        if (!submissionStorageService.store(instance, template, submissionBuilder, name, value, actingAsId, principal)) {
                            LOG.warn("Submission included field (" + name + ") that is not acceptable, and no attachments are allowed for this template");
                        }
                    }
                }
            }
        }
        return submissionBuilder.build();
    }

    @Override
    public Class<?> getType() {
        return Map.class;
    }
}
