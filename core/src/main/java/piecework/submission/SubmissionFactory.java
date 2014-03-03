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

import piecework.enumeration.ActionType;
import piecework.model.Attachment;
import piecework.model.Entity;
import piecework.model.Submission;
import piecework.model.Value;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Sometimes need to be able to create a submission without first handling a template -- for example,
 * during internal update data operations. This factory produces new submission objects for this
 * purpose.
 *
 * @author James Renfro
 */
public class SubmissionFactory {

    public static Submission submission(ActionType actionType, String processDefinitionKey, String taskId, String requestId, Map<String, List<Value>> data, Collection<Attachment> attachments, Entity principal) {
        String principalId = principal != null ? principal.getEntityId() : "anonymous";
        Submission.Builder submissionBuilder = new Submission.Builder();

        submissionBuilder.processDefinitionKey(processDefinitionKey)
                .requestId(requestId)
                .taskId(taskId)
                .submissionDate(new Date())
                .data(data)
                .attachments(attachments)
                .submissionDate(new Date())
                .submitterId(principalId)
                .actionType(actionType);

        return submissionBuilder.build();
    }


}
