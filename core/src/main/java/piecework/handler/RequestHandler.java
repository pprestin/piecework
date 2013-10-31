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
package piecework.handler;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.authorization.AuthorizationRole;
import piecework.common.RequestDetails;
import piecework.enumeration.ActionType;
import piecework.form.FormFactory;
import piecework.identity.IdentityHelper;
import piecework.exception.*;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.RequestRepository;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.service.ProcessInstanceService;
import piecework.service.TaskService;
import piecework.validation.FormValidation;

import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 * @author James Renfro
 */
@Service
public class RequestHandler {

    private static final Logger LOG = Logger.getLogger(RequestHandler.class);

    @Autowired
    RequestRepository requestRepository;

    @Autowired
    IdentityHelper identityHelper;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    ScreenHandler screenHandler;

    @Autowired
    TaskService taskService;

    public FormRequest create(RequestDetails requestDetails, Process process) throws StatusCodeError {
        return create(requestDetails, process, null, (Task)null, ActionType.CREATE, null);
    }

//    public FormRequest create(RequestDetails requestDetails, Process process, ProcessInstance processInstance, Task task) throws StatusCodeError {
//        return create(requestDetails, process, processInstance, task, null, null);
//    }

    public FormRequest create(RequestDetails requestDetails, Process process, ProcessInstance processInstance, Task task, ActionType actionType) throws StatusCodeError {
        return create(requestDetails, process, processInstance, task, actionType, null);
    }

    public FormRequest create(RequestDetails requestDetails, Process process, ProcessInstance processInstance, Task task, ActionType actionType, FormValidation validation) throws StatusCodeError {
        verifyCurrentUserIsAuthorized(process, task);

        Activity activity = FormFactory.activity(process, processInstance, task);

        FormRequest.Builder formRequestBuilder = new FormRequest.Builder()
                .processDefinitionKey(process.getProcessDefinitionKey())
                .instance(processInstance)
                .task(task)
                .activity(activity)
                .action(actionType);

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

        return requestRepository.save(formRequestBuilder.build());
    }

    public FormRequest create(RequestDetails requestDetails, Process process, String taskId, FormRequest previousFormRequest) throws StatusCodeError {
        ProcessInstance instance = processInstanceService.findByTaskId(process, taskId);
        if (instance == null) {
            LOG.warn("Forbidden: No instance found for the task id passed " + process.getProcessDefinitionKey() + " task: " + taskId);
            throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);
        }
        Task task = taskService.task(instance, taskId);
        verifyCurrentUserIsAuthorized(process, task);
        return create(requestDetails, process, instance, task, null);
    }

    public FormRequest handle(RequestDetails request, String requestId) throws StatusCodeError {
        FormRequest formRequest = requestRepository.findOne(requestId);

        if (formRequest == null) {
            return null;
//            LOG.warn("Request being viewed or submitted for invalid/missing requestId " + requestId);
//            throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);
        }

        if (request != null) {
            if (request.getRemoteUser() != null && formRequest.getRemoteUser() != null && !request.getRemoteUser().equals(formRequest.getRemoteUser())) {
                LOG.error("Wrong user viewing or submitting form: " + request.getRemoteUser() + " not " + formRequest.getRemoteUser());
                throw new ForbiddenError(Constants.ExceptionCodes.user_does_not_match);
            }

            if (request.getRemoteHost() != null && formRequest.getRemoteHost() != null && !request.getRemoteHost().equals(formRequest.getRemoteHost()))
                LOG.warn("This should not happen -- submission remote host (" + request.getRemoteHost() + ") does not match request (" + formRequest.getRemoteHost() + ")");

            if (request.getRemoteAddr() != null && formRequest.getRemoteAddr() != null && !request.getRemoteAddr().equals(formRequest.getRemoteAddr()))
                LOG.warn("This should not happen -- submission remote address (" + request.getRemoteAddr() + ") does not match request (" + formRequest.getRemoteAddr() + ")");

            if (formRequest.getCertificateIssuer() != null && formRequest.getCertificateSubject() != null) {
                String certificateIssuer = request.getCertificateIssuer();
                String certificateSubject = request.getCertificateSubject();

                if (StringUtils.isEmpty(certificateIssuer) || StringUtils.isEmpty(certificateSubject) ||
                        !certificateIssuer.equals(formRequest.getCertificateIssuer()) ||
                        !certificateSubject.equals(formRequest.getCertificateSubject())) {
                    LOG.error("Wrong certificate submitting form: " + certificateIssuer + ":" + certificateSubject + " not " + formRequest.getCertificateIssuer() + ":" + formRequest.getCertificateSubject());
                    throw new ForbiddenError(Constants.ExceptionCodes.certificate_does_not_match);

                }
            }
        }

        ProcessInstance instance = formRequest.getInstance();

        if (instance == null && StringUtils.isNotEmpty(formRequest.getProcessDefinitionKey()) && StringUtils.isNotEmpty(formRequest.getProcessInstanceId()))
            instance = processInstanceService.read(formRequest.getProcessDefinitionKey(), formRequest.getProcessInstanceId(), false);

        FormRequest.Builder builder = new FormRequest.Builder(formRequest)
                .instance(instance)
                .task(taskService.task(instance, formRequest.getTaskId()));

        return builder.build();
    }


    /*
     * Helper methods
     */

    private void verifyCurrentUserIsAuthorized(Process process, Task task) throws ForbiddenError, BadRequestError {
        if (process == null)
            throw new BadRequestError(Constants.ExceptionCodes.process_does_not_exist);

        if (!identityHelper.hasRole(process, AuthorizationRole.OVERSEER)) {
            String taskId = task != null ? task.getTaskInstanceId() : null;
            // If the user does not have 'overseer' role then she or he needs to be an assignee or at least a candidate assignee
            User currentUser = identityHelper.getCurrentUser();
            if (currentUser == null || StringUtils.isEmpty(currentUser.getUserId())) {
                LOG.error("Forbidden: Unauthorized user or user with no userId (e.g. system user) attempting to create a request for task: " + taskId);
                String systemId = identityHelper.getAuthenticatedSystemOrUserId();
                if (StringUtils.isNotEmpty(systemId))
                    LOG.error("System id is " + systemId + " -- needs overseer access in order to use form functionality");

                throw new ForbiddenError();
            }

            if (task != null && (task.getAssigneeId() == null || !task.getAssigneeId().equals(currentUser.getUserId()))) {
                // If the user is not the assignee then she or he needs to be a candidate assignee
                Set<String> candidateAssigneeIds = task.getCandidateAssigneeIds();
                if (candidateAssigneeIds == null || !candidateAssigneeIds.contains(currentUser.getUserId())) {
                    LOG.warn("Forbidden: Unauthorized user " + currentUser.getDisplayName() + " (" + currentUser.getUserId() + ") attempting to access task " + taskId);
                    throw new ForbiddenError();
                }
            }
        }
    }
}
