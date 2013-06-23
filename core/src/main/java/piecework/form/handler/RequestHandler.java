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
package piecework.form.handler;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.common.RequestDetails;
import piecework.engine.TaskCriteria;
import piecework.engine.concrete.ProcessEngineRuntimeConcreteFacade;
import piecework.engine.exception.ProcessEngineException;
import piecework.exception.*;
import piecework.model.*;
import piecework.model.Process;
import piecework.process.ProcessRepository;
import piecework.process.RequestRepository;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * @author James Renfro
 */
@Service
public class RequestHandler {

    private static final Logger LOG = Logger.getLogger(RequestHandler.class);

    @Autowired
    ProcessEngineRuntimeConcreteFacade facade;

    @Autowired
    ProcessRepository processRepository;

    @Autowired
    RequestRepository requestRepository;

    public FormRequest create(RequestDetails requestDetails, Process process) throws StatusCodeError {
        return create(requestDetails, process, null, null, null);
    }

    public FormRequest create(RequestDetails requestDetails, Process process, String processInstanceId, String taskId, FormRequest previousFormRequest) throws StatusCodeError {
        Interaction interaction = null;
        Screen nextScreen = null;
        String submissionType = Constants.SubmissionTypes.FINAL;

        if (process == null)
            throw new BadRequestError(Constants.ExceptionCodes.process_does_not_exist);

        if (previousFormRequest != null) {
            interaction = previousFormRequest.getInteraction();
            Screen currentScreen = previousFormRequest.getScreen();

            if (interaction == null)
                throw new InternalServerError();

            List<Screen> screens = interaction.getScreens();

            if (screens == null || screens.isEmpty())
                throw new InternalServerError();

            Iterator<Screen> screenIterator = screens.iterator();

            while (screenIterator.hasNext()) {
                Screen cursor = screenIterator.next();

                if (currentScreen == null) {
                    nextScreen = cursor;
                    break;
                } else if (cursor.getScreenId().equals(currentScreen.getScreenId())) {
                    if (screenIterator.hasNext())
                        nextScreen = screenIterator.next();
                    // Either way, break out, since we found the right place in the list
                    break;
                }
            }

            // If there is no next screen, then we're done
            if (nextScreen == null)
                return null;

            if (screenIterator.hasNext())
                submissionType = Constants.SubmissionTypes.INTERIM;
        } else {
            List<Interaction> interactions = process.getInteractions();

            if (interactions == null || interactions.isEmpty())
                throw new InternalServerError();

            Iterator<Interaction> interactionIterator = interactions.iterator();

            if (StringUtils.isNotEmpty(taskId)) {
                try {
                    Task task = facade.findTask(new TaskCriteria.Builder().engineProcessDefinitionKey(process.getEngineProcessDefinitionKey()).taskId(taskId).build());

                    if (task != null) {
                        String taskDefinitionKey = task.getTaskDefinitionKey();
                        while (interactionIterator.hasNext()) {
                            Interaction current = interactionIterator.next();
                            if (current.getTaskDefinitionKeys() != null && current.getTaskDefinitionKeys().contains(taskDefinitionKey)) {
                                interaction = current;
                                break;
                            }
                        }
                    }

                } catch (ProcessEngineException e) {
                    LOG.error("Could not find task ", e);
                    throw new InternalServerError();
                }
            } else {
                // Pick the first interaction and the first screen
                interaction = interactionIterator.next();
            }

            if (interaction != null && !interaction.getScreens().isEmpty()) {
                Iterator<Screen> screenIterator = interaction.getScreens().iterator();
                nextScreen = screenIterator.next();
                if (screenIterator.hasNext())
                    submissionType = Constants.SubmissionTypes.INTERIM;
            }
        }

        // Generate a new uuid for this request
        String requestId = UUID.randomUUID().toString();

        FormRequest.Builder formRequestBuilder = new FormRequest.Builder()
                .requestId(requestId)
                .processDefinitionKey(process.getProcessDefinitionKey())
                .processInstanceId(processInstanceId)
                .taskId(taskId)
                .interaction(interaction)
                .screen(nextScreen)
                .submissionType(submissionType);

        if (requestDetails != null) {
            formRequestBuilder.remoteAddr(requestDetails.getRemoteAddr())
                    .remoteHost(requestDetails.getRemoteHost())
                    .remotePort(requestDetails.getRemotePort())
                    .remoteUser(requestDetails.getRemoteUser())
                    .certificateIssuer(requestDetails.getCertificateIssuer())
                    .certificateSubject(requestDetails.getCertificateSubject());
        }

        return requestRepository.save(formRequestBuilder.build());
    }

    public FormRequest handle(RequestDetails request, String requestType, String requestId) throws StatusCodeError {
        FormRequest formRequest = requestRepository.findOne(requestId);

        if (formRequest == null) {
            LOG.warn("Request being viewed or submitted for invalid/missing requestId " + requestId);
            throw new ForbiddenError(Constants.ExceptionCodes.request_does_not_match);
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

            if (request.getRemotePort() != formRequest.getRemotePort())
                LOG.warn("This should not happen -- submission remote port (" + request.getRemotePort() + ") does not match request (" + formRequest.getRemotePort() + ")");

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

        return formRequest;
    }

}
