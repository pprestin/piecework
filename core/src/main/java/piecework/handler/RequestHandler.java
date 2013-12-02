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
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.authorization.AuthorizationRole;
import piecework.model.RequestDetails;
import piecework.enumeration.ActionType;
import piecework.identity.IdentityHelper;
import piecework.exception.*;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.RequestRepository;
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
    TaskService taskService;

    public FormRequest create(RequestDetails requestDetails, Process process) throws StatusCodeError {
        return create(requestDetails, process, null, (Task)null, ActionType.CREATE, null);
    }

    public FormRequest create(RequestDetails requestDetails, Process process, ProcessInstance processInstance, Task task, ActionType actionType) throws StatusCodeError {
        return create(requestDetails, process, processInstance, task, actionType, null);
    }

    public FormRequest create(RequestDetails requestDetails, Process process, ProcessInstance processInstance, Task task, ActionType actionType, FormValidation validation) throws StatusCodeError {
        Activity activity = activity(process, processInstance, task);

        // Don't allow anyone to issue a create request for a task that's not open
        if (actionType == ActionType.CREATE && task != null && task.getTaskStatus() != null && !task.getTaskStatus().equals(Constants.TaskStatuses.OPEN))
            actionType = ActionType.VIEW;

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
        Task task = taskService.read(instance, taskId);
        verifyCurrentUserIsAuthorized(process, task);

        ActionType actionType = ActionType.CREATE;

        return create(requestDetails, process, instance, task, actionType);
    }

    public FormRequest handle(RequestDetails request, String requestId) throws StatusCodeError {
        FormRequest formRequest = requestRepository.findOne(requestId);

        if (formRequest == null) {
            return null;
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

            if (formRequest.getRequestDate() != null) {
                Hours hours = Hours.hoursBetween(new DateTime(formRequest.getRequestDate()), new DateTime());
                int h = hours.getHours();
                if (h > 1) {
                    throw new ForbiddenError(Constants.ExceptionCodes.request_expired);
                }
            }
        }

        ProcessInstance instance = formRequest.getInstance();

        if (instance == null && StringUtils.isNotEmpty(formRequest.getProcessDefinitionKey()) && StringUtils.isNotEmpty(formRequest.getProcessInstanceId()))
            instance = processInstanceService.read(formRequest.getProcessDefinitionKey(), formRequest.getProcessInstanceId(), false);

        FormRequest.Builder builder = new FormRequest.Builder(formRequest)
                .instance(instance)
                .task(taskService.read(instance, formRequest.getTaskId()));

        return builder.build();
    }


    /*
     * Helper methods
     */
    public static Activity activity(Process process, ProcessInstance instance, Task task) throws StatusCodeError {
        Activity activity = null;
        if (process.isAllowPerInstanceActivities() && task != null && task.getTaskDefinitionKey() != null && instance != null) {
            Map<String, Activity> activityMap = instance.getActivityMap();
            if (activityMap != null)
                activity = activityMap.get(task.getTaskDefinitionKey());

            if (activity != null)
                return activity;
        }

        ProcessDeployment deployment = process.getDeployment();
        if (deployment == null)
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);

        String activityKey = deployment.getStartActivityKey();
        if (task != null)
            activityKey = task.getTaskDefinitionKey();

        if (activityKey != null)
            activity = deployment.getActivity(activityKey);

        if (activity != null)
            return activity;

        throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
    }


    private void verifyCurrentUserIsAuthorized(Process process, Task task) throws ForbiddenError, BadRequestError {
        if (process == null)
            throw new BadRequestError(Constants.ExceptionCodes.process_does_not_exist);

        String taskId = task != null ? task.getTaskInstanceId() : null;

        Entity principal = identityHelper.getPrincipal();
        if (principal == null || StringUtils.isEmpty(principal.getEntityId())) {
            LOG.error("Forbidden: Unauthorized user or user with no userId (e.g. system user) attempting to create a request for task: " + taskId);
            throw new ForbiddenError();
        }

        if (!principal.hasRole(process, AuthorizationRole.OVERSEER)) {
            if (task != null && !task.isCandidateOrAssignee(principal)) {
                LOG.warn("Forbidden: Unauthorized principal " + principal.toString() + " attempting to access task " + taskId);
                throw new ForbiddenError();
            }
        }
    }
}
