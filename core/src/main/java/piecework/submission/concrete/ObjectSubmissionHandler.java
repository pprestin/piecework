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
import piecework.exception.MisconfiguredProcessException;
import piecework.exception.PieceworkException;
import piecework.exception.StatusCodeError;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessProvider;
import piecework.submission.SubmissionTemplate;
import piecework.util.ModelUtility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
@Service
public class ObjectSubmissionHandler extends AbstractSubmissionHandler<Submission> {

    private static final Logger LOG = Logger.getLogger(FormValueSubmissionHandler.class);

    @Override
    protected <P extends ProcessProvider> Submission handleInternal(P modelProvider, Submission rawSubmission, SubmissionTemplate template) throws PieceworkException {
        Entity principal = modelProvider.principal();
        ProcessInstance instance = ModelUtility.instance(modelProvider);

        String principalId = principal != null ? principal.getEntityId() : "anonymous";
        Submission.Builder submissionBuilder = submissionBuilder(instance, template, principal, rawSubmission);
        if (rawSubmission != null && rawSubmission.getData() != null) {
            for (Map.Entry<String, List<Value>> entry : rawSubmission.getData().entrySet()) {
                String name = sanitizer.sanitize(entry.getKey());
                List<? extends Value> values = entry.getValue();

                for (Value value : values) {
                    if (value == null)
                        continue;

                    String actualValue = sanitizer.sanitize(value.getValue());
                    if (!submissionStorageService.store(instance, template, submissionBuilder, name, actualValue, principalId, principal)) {
                        LOG.warn("Submission included field (" + name + ") that is not acceptable, and no attachments are allowed for this template");
                    }
                }
            }
        }
        Process process = template.getProcess();
        if (process.isAllowPerInstanceActivities() && rawSubmission != null && rawSubmission.getActivityMap() != null) {
            Map<String, Activity> rawActivityMap = rawSubmission.getActivityMap();

            HashMap<String, Activity> activityMap = new HashMap<String, Activity>();
            for (Map.Entry<String, Activity> entry : rawActivityMap.entrySet()) {
                String key = sanitizer.sanitize(entry.getKey());
                if (key == null)
                    continue;
                if (entry.getValue() == null)
                    continue;

                Activity activity = activityRepository.save(new Activity.Builder(entry.getValue(), sanitizer).build());
                activityMap.put(key, activity);
            }
        }

        return submissionBuilder.build();
    }

    @Override
    public Class<?> getType() {
        return Submission.class;
    }
}
