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
import piecework.common.ManyMap;
import piecework.exception.BadRequestError;
import piecework.exception.ForbiddenError;
import piecework.model.*;
import piecework.security.AccessTracker;
import piecework.security.DataFilter;
import piecework.validation.Validation;

import java.util.*;

/**
 * @author James Renfro
 */
public class SecurityUtility {

    private static final Logger LOG = Logger.getLogger(SecurityUtility.class);

    public static ManyMap<String, Value> combinedData(ProcessInstance instance, Validation validation) {
        ManyMap<String, Value> combinedData = new ManyMap<String, Value>();
        if (instance != null) {
            Map<String, List<Value>> instanceData = instance.getData();
            if (instanceData != null && !instanceData.isEmpty())
                combinedData.putAll(instanceData);
        }
        if (validation != null) {
            Map<String, List<Value>> validationData = validation.getData();
            if (validationData != null && !validationData.isEmpty())
                combinedData.putAll(validationData);
        }
        return combinedData;
    }

    public static Set<Field> fields(Activity activity, Action action) {
        Map<String, Field> fieldMap = activity.getFieldMap();
        Container container = action != null ? action.getContainer() : null;
        Set<Field> fields = new HashSet<Field>();
        if (container != null) {
            fields(fields, fieldMap, container, container.getActiveChildIndex());
        }
        return fields;
//        return fieldMap.isEmpty() ? Collections.<Field>emptySet() : new HashSet<Field>(fieldMap.values());
    }

    private static void fields(Set<Field> fields, Map<String, Field> fieldMap, Container container, int activeChildIndex) {
        List<String> fieldIds = container.getFieldIds();
        if (fieldIds != null && !fieldIds.isEmpty()) {
            for (String fieldId : fieldIds) {
                Field field = fieldMap.get(fieldId);
                if (field == null)
                    continue;
                fields.add(field);
            }
        }
        if (container.getChildren() != null && !container.getChildren().isEmpty()) {
            for (Container child : container.getChildren()) {
                fields(fields, fieldMap, child, activeChildIndex);
            }
        }
    }

    public static Set<Field> restrictedFields(Set<Field> allFields) {
        Set<Field> restrictedFields = new HashSet<Field>();
        if (allFields != null && !allFields.isEmpty()) {
            for (Field field : allFields) {
                if (field.isRestricted())
                    restrictedFields.add(field);
            }
        }
        return restrictedFields;
    }

    public static ManyMap<String, Value> filter(Map<String, List<Value>> original, DataFilter... dataFilters) {
        ManyMap<String, Value> map = new ManyMap<String, Value>();

        if (original != null && !original.isEmpty()) {
            for (Map.Entry<String, List<Value>> entry : original.entrySet()) {
                String key = entry.getKey();
                List<Value> values = entry.getValue();
                if (dataFilters != null) {
                    for (DataFilter dataFilter : dataFilters) {
                        values = dataFilter.filter(key, values);
                    }
                }
                if (!values.isEmpty())
                    map.put(key, values);
            }
        }

        return map;
    }

//    public static boolean isAuthorizedForRestrictedData(Task task, Entity principal) {
//        // If this is an application
//        if (principal instanceof Application && StringUtils.isNotEmpty(principal.getEntityId()) && principal.getEntityId().equals("piecework"))
//            return true;
//        return task.isAssignee(principal);
//    }

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

    public static void verifyRequestIntegrity(AccessTracker accessTracker, String processDefinitionKey, FormRequest formRequest, RequestDetails request) throws ForbiddenError {
        if (request == null)
            return;

        if (StringUtils.isEmpty(processDefinitionKey)) {
            LOG.error("Attempting to verify request integrity with empty processDefinitionKey -- this is FORBIDDEN and SUSPICIOUS");
            throw new ForbiddenError(Constants.ExceptionCodes.process_key_required);
        }

        if (StringUtils.isEmpty(formRequest.getProcessDefinitionKey())) {
            LOG.error("Attempting to verify request integrity when the request itself has an empty processDefinitionKey -- this shouldn't happen and indicates that something is seriously wrong in the data store");
            throw new ForbiddenError(Constants.ExceptionCodes.process_is_misconfigured);
        }

        if (!processDefinitionKey.equals(formRequest.getProcessDefinitionKey())) {
            LOG.error("Attempting to verify request integrity and the processDefinitionKey doesn't match the request -- this is FORBIDDEN and SUSPICIOUS");
            throw new ForbiddenError(Constants.ExceptionCodes.process_is_misconfigured);
        }

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

        if (formRequest.getCertificateIssuer() != null || formRequest.getCertificateSubject() != null) {
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
