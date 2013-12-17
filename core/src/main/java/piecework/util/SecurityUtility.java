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
package piecework.util;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import piecework.Constants;
import piecework.authorization.AuthorizationRole;
import piecework.exception.BadRequestError;
import piecework.exception.ForbiddenError;
import piecework.model.*;

/**
 * @author James Renfro
 */
public class SecurityUtility {

    private static final Logger LOG = Logger.getLogger(SecurityUtility.class);

    public static void verifyEntityIsAuthorized(piecework.model.Process process, Task task, Entity principal) throws ForbiddenError, BadRequestError {
        if (process == null)
            throw new BadRequestError(Constants.ExceptionCodes.process_does_not_exist);

        String taskId = task != null ? task.getTaskInstanceId() : null;

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

    public static void verifyRequestIntegrity(FormRequest formRequest, RequestDetails request) throws ForbiddenError {
        if (request == null)
            return;

        if (StringUtils.isNotEmpty(formRequest.getRemoteUser())) {
            // If this form request belongs to a specific user (is not anonymous), then don't show it to an anonymous user
            if (StringUtils.isEmpty(request.getRemoteUser())) {
                LOG.error("Anonymous user attempting to view or submit a request belonging to " + formRequest.getRemoteUser());
                throw new ForbiddenError(Constants.ExceptionCodes.user_does_not_match);
            }
            if (!request.getRemoteUser().equals(formRequest.getRemoteUser())) {
                LOG.error("Wrong user viewing or submitting form: " + request.getRemoteUser() + " not " + formRequest.getRemoteUser());
                throw new ForbiddenError(Constants.ExceptionCodes.user_does_not_match);
            }
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
            if (h > 24) {
                throw new ForbiddenError(Constants.ExceptionCodes.request_expired);
            }
        }
    }

}
