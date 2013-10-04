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
import piecework.common.RequestDetails;
import piecework.enumeration.ActionType;
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

    /*
     * Constructor for testing -- normally not used
     */
//    public RequestHandler(RequestRepository requestRepository, IdentityHelper identityHelper, TaskService taskService) {
//        this.requestRepository = requestRepository;
//        this.identityHelper = identityHelper;
//        this.taskService = taskService;
//    }

    public FormRequest create(RequestDetails requestDetails, Process process) throws StatusCodeError {
        return create(requestDetails, process, null, (Task)null, null, null);
    }

    public FormRequest create(RequestDetails requestDetails, Process process, ProcessInstance processInstance, Task task) throws StatusCodeError {
        return create(requestDetails, process, processInstance, task, null, null);
    }

    public FormRequest create(RequestDetails requestDetails, Process process, ProcessInstance processInstance, Task task, ActionType action) throws StatusCodeError {
        return create(requestDetails, process, processInstance, task, action, null);
    }

    public FormRequest create(RequestDetails requestDetails, Process process, ProcessInstance processInstance, Task task, ActionType action, FormValidation validation) throws StatusCodeError {
        Screen nextScreen = null;
        String submissionType = Constants.SubmissionTypes.FINAL;

        if (process == null)
            throw new BadRequestError(Constants.ExceptionCodes.process_does_not_exist);

        String processInstanceId = processInstance != null ? processInstance.getProcessInstanceId() : null;

        //if (validation != null && !validation.getResults().isEmpty()) {
        if (action == null) {
            // If validation is provided then it's an error and we should return the original screen
            nextScreen = screenHandler.currentScreen(process, task);
        } else {
            nextScreen = screenHandler.nextScreen(process, task, action);
        }

        FormRequest.Builder formRequestBuilder = new FormRequest.Builder()
                .processDefinitionKey(process.getProcessDefinitionKey())
                .processInstanceId(processInstanceId)
                .instance(processInstance)
                .task(task)
                .screen(nextScreen);

        if (requestDetails != null) {
            String contentType = requestDetails.getContentType() != null ? requestDetails.getContentType().toString() : null;

            formRequestBuilder.remoteAddr(requestDetails.getRemoteAddr())
                    .remoteHost(requestDetails.getRemoteHost())
                    .remotePort(requestDetails.getRemotePort())
                    .remoteUser(requestDetails.getRemoteUser())
                    .actAsUser(requestDetails.getActAsUser())
                    .certificateIssuer(requestDetails.getCertificateIssuer())
                    .certificateSubject(requestDetails.getCertificateSubject())
                    .contentType(contentType);

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
        Task task = null;
        if (StringUtils.isNotEmpty(taskId)) {
            task = taskService.allowedTask(process, taskId, false);

            if (task == null)
                throw new NotFoundError();
        }
        ProcessInstance processInstance = null;
        if (task != null) {
            processInstance = processInstanceService.read(process, task.getProcessInstanceId(), false);
        }

        return create(requestDetails, process, processInstance, task, null);
    }

    public FormRequest handle(RequestDetails request, String requestId) throws StatusCodeError {
        FormRequest formRequest = requestRepository.findOne(requestId);

        if (formRequest == null) {
            LOG.warn("Request being viewed or submitted for invalid/missing requestId " + requestId);
//            throw new NotFoundError(Constants.ExceptionCodes.request_does_not_match);
            throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);
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

//            if (request.getRemotePort() != formRequest.getRemotePort())
//                LOG.warn("This should not happen -- submission remote port (" + request.getRemotePort() + ") does not match request (" + formRequest.getRemotePort() + ")");

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

        FormRequest.Builder builder = new FormRequest.Builder(formRequest, new PassthroughSanitizer())
                .instance(instance);

        return builder.build();
    }

}
