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
package piecework.form;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.exception.ForbiddenError;
import piecework.exception.InternalServerError;
import piecework.exception.StatusCodeError;
import piecework.model.FormRequest;
import piecework.model.Interaction;
import piecework.model.Screen;
import piecework.process.RequestRepository;

import javax.servlet.http.HttpServletRequest;
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
    RequestRepository requestRepository;

    public FormRequest create(HttpServletRequest request, String processDefinitionKey, String processInstanceId, Interaction interaction, Screen currentScreen) throws StatusCodeError {
        List<Screen> screens = interaction.getScreens();

        if (screens == null || screens.isEmpty())
            throw new InternalServerError();

        Screen nextScreen = null;
        Iterator<Screen> screenIterator = screens.iterator();

        String submissionType = Constants.SubmissionTypes.FINAL;

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

        // Generate a new uuid for this request
        String requestId = UUID.randomUUID().toString();

        FormRequest formRequest = new FormRequest.Builder()
                .requestId(requestId)
                .processDefinitionKey(processDefinitionKey)
                .processInstanceId(processInstanceId)
                .remoteAddr(request.getRemoteAddr())
                .remoteHost(request.getRemoteHost())
                .remotePort(request.getRemotePort())
                .remoteUser(request.getRemoteUser())
                .screen(nextScreen)
                .submissionType(submissionType)
                .build();

        return requestRepository.save(formRequest);
    }

    public FormRequest handle(HttpServletRequest request, String requestId) throws StatusCodeError {
        FormRequest formRequest = requestRepository.findOne(requestId);

        if (formRequest == null) {
            LOG.warn("Request being submitted for invalid/missing requestId " + requestId);
            throw new ForbiddenError(Constants.ExceptionCodes.request_does_not_match);
        }

        if (request.getRemoteUser() != null && formRequest.getRemoteUser() != null && !request.getRemoteUser().equals(formRequest.getRemoteUser())) {
            LOG.error("Wrong user submitting form: " + request.getRemoteUser() + " not " + formRequest.getRemoteUser());
            throw new ForbiddenError(Constants.ExceptionCodes.user_does_not_match);
        }

        if (request.getRemoteHost() != null && formRequest.getRemoteHost() != null && !request.getRemoteHost().equals(formRequest.getRemoteHost()))
            LOG.warn("This should not happen -- submission remote host (" + request.getRemoteHost() + ") does not match request (" + formRequest.getRemoteHost() + ")");

        if (request.getRemoteAddr() != null && formRequest.getRemoteAddr() != null && !request.getRemoteAddr().equals(formRequest.getRemoteAddr()))
            LOG.warn("This should not happen -- submission remote address (" + request.getRemoteAddr() + ") does not match request (" + formRequest.getRemoteAddr() + ")");

        if (request.getRemotePort() != formRequest.getRemotePort())
            LOG.warn("This should not happen -- submission remote port (" + request.getRemotePort() + ") does not match request (" + formRequest.getRemotePort() + ")");

        return formRequest;
    }


}
