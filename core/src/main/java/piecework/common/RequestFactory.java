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
package piecework.common;

import piecework.Constants;
import piecework.enumeration.ActionType;
import piecework.exception.MisconfiguredProcessException;
import piecework.model.*;
import piecework.model.Process;
import piecework.util.ActivityUtil;
import piecework.validation.Validation;

import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * @author James Renfro
 */
public class RequestFactory {

    public FormRequest request(RequestDetails requestDetails, Process process, ProcessInstance processInstance, Task task, ActionType actionType, Validation validation, Explanation explanation) throws MisconfiguredProcessException {
        Activity activity = ActivityUtil.activity(process, processInstance, task);

        // Don't allow anyone to issue a create request for a task that's not open
        if (actionType == ActionType.CREATE && task != null && task.getTaskStatus() != null && !task.getTaskStatus().equals(Constants.TaskStatuses.OPEN))
            actionType = ActionType.VIEW;

        FormRequest.Builder formRequestBuilder = new FormRequest.Builder()
                .processDefinitionKey(process.getProcessDefinitionKey())
                .instance(processInstance)
                .task(task)
                .activity(activity)
                .action(actionType)
                .explanation(explanation);

        if (requestDetails != null) {
            String contentType = requestDetails.getContentType() != null ? requestDetails.getContentType().toString() : null;

            formRequestBuilder.remoteAddr(requestDetails.getRemoteAddr())
                    .remoteHost(requestDetails.getRemoteHost())
                    .remotePort(requestDetails.getRemotePort())
                    .remoteUser(requestDetails.getRemoteUser())
                    .actAsUser(requestDetails.getActAsUser())
                    .certificateIssuer(requestDetails.getCertificateIssuer())
                    .certificateSubject(requestDetails.getCertificateSubject())
                    .contentType(contentType)
                    .referrer(requestDetails.getReferrer())
                    .userAgent(requestDetails.getUserAgent());

            List<MediaType> acceptableMediaTypes = requestDetails.getAcceptableMediaTypes();
            if (acceptableMediaTypes != null) {
                for (MediaType acceptableMediaType : acceptableMediaTypes) {
                    formRequestBuilder.acceptableMediaType(acceptableMediaType.toString());
                }
            }
        }

        if (validation != null) {
            formRequestBuilder.messages(validation.getResults());
        }
        return formRequestBuilder.build();
    }

}
