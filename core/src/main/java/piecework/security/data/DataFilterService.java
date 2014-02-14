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
package piecework.security.data;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.SystemUser;
import piecework.model.*;
import piecework.security.AccessTracker;
import piecework.security.DataFilter;
import piecework.security.EncryptionService;
import piecework.security.concrete.*;
import piecework.common.ManyMap;
import piecework.settings.UserInterfaceSettings;
import piecework.util.SecurityUtility;
import piecework.validation.Validation;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * @author James Renfro
 */
@Service
public class DataFilterService {

    private static final Logger LOG = Logger.getLogger(DataFilterService.class);

    @Autowired
    private AccessTracker accessTracker;

    @Autowired(required = false)
    private EncryptionService encryptionService;

    @Autowired
    private UserInterfaceSettings settings;

    private static final PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
    private static final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis();


    @PostConstruct
    public void init() {
        if (encryptionService == null)
            encryptionService = new PassthroughEncryptionService();
    }

    /**
     * Retrieves all instance data including restricted values, decrypting them all.
     *
     * @param instance containing the data
     * @param reason why this data is being retrieved in the code, e.g. to send it to some backend system
     * @return Map of decrypted instance data
     */
    public Map<String, List<Value>> allInstanceDataDecrypted(ProcessInstance instance, String reason) {
        LOG.info("Retrieving all instance data, decrypted, for the following reason: " + reason);
        Map<String, List<Value>> instanceData = instance != null ? instance.getData() : null;
        DataFilter decryptValuesFilter = new DecryptValuesFilter(instance, new SystemUser(), reason, accessTracker, encryptionService);
        return SecurityUtility.filter(instanceData, decryptValuesFilter);
    }

    /**
     * Retrieves all instance and validation data, including restricted values, decrypting them all.
     *
     * @param instance containing the data
     * @param reason why this data is being retrieved in the code, e.g. to send it to some backend system
     * @return Map of decrypted instance data
     */
    public Map<String, List<Value>> allInstanceAndValidationDataDecrypted(ProcessInstance instance, Validation validation, String reason) {
        LOG.info("Retrieving all instance and validation data, decrypted, for the following reason: " + reason);
        ManyMap<String, Value> combinedData = SecurityUtility.combinedData(instance, validation);
        DataFilter decryptValuesFilter = new DecryptValuesFilter(instance, new SystemUser(), reason, accessTracker, encryptionService);
        return SecurityUtility.filter(combinedData, decryptValuesFilter);
    }

    /**
     * Filters instance data to only values that belong to the passed fields,
     * and excludes values from any fields that are restricted.
     *
     *
     * @param instance containing the data
     * @param fields
     * @return Map of instance data that is not restricted
     */
    public Map<String, List<Value>> unrestrictedInstanceData(ProcessInstance instance, Set<Field> fields) {
        Map<String, List<Value>> instanceData = instance != null ? instance.getData() : null;
        DataFilter limitFieldsFilter = new LimitFieldsFilter(fields, false);
        return SecurityUtility.filter(instanceData, limitFieldsFilter);
    }

    /**
     * Retrieves all submission data, including restricted fields, which will be
     * decrypted
     *
     * @param instance to log access to
     * @param submission containing the data
     * @param principal entity that is accessing this data
     * @param reason why the principal is requesting the data
     * @return Map of validation data that
     */
    public Map<String, List<Value>> allSubmissionData(ProcessInstance instance, Submission submission, Entity principal, String reason) {
        Map<String, List<Value>> validationData = submission != null ? submission.getData() : null;
        DataFilter decryptValuesFilter = new DecryptValuesFilter(instance, principal, reason, accessTracker, encryptionService);
        return SecurityUtility.filter(validationData, decryptValuesFilter);
    }

    /**
     * Filters validation data to only values that belong to the passed fields,
     * but includes restricted values, since these need to be passed back to the user
     * whose validation is being evaluated -- NOTE: it's important not to use this method
     * in cases where the Validation could be retrieved by someone other than the person
     * who submitted the data.
     *
     *
     *
     * @param validation containing the data
     * @param fields
     * @param isAllowAny
     * @return Map of validation data that
     */
    public Map<String, List<Value>> allValidationData(Validation validation, Set<Field> fields, boolean isAllowAny) {
        Map<String, List<Value>> validationData = validation != null ? validation.getData() : null;
        if (isAllowAny)
            return validationData;

        // Need to include restricted fields since they are submitted by the requesting user
        DataFilter limitFieldsFilter = isAllowAny ? new NoOpFilter() : new LimitFieldsFilter(fields, true);
        return SecurityUtility.filter(validationData, limitFieldsFilter);
    }

    public Map<String, List<Value>> authorizedInstanceData(ProcessInstance instance, Task task, Set<Field> fields, Entity principal, String version, String reason, boolean isAllowAny) {
        Map<String, List<Value>> instanceData = instance != null ? instance.getData() : null;

        DataFilter decryptValuesFilter;
        if (task.isAssignee(principal))
            decryptValuesFilter = new DecryptValuesFilter(instance, principal, reason, accessTracker, encryptionService);
        else
            decryptValuesFilter = new MaskRestrictedValuesFilter(instance, principal, encryptionService);

        DataFilter limitFieldsFilter = isAllowAny ? new NoOpFilter() : new LimitFieldsFilter(fields, true);
        DataFilter decorateValuesFilter = new DecorateValuesFilter(instance, fields, settings, principal, version);
        return SecurityUtility.filter(instanceData, limitFieldsFilter, decryptValuesFilter, decorateValuesFilter);
    }

    /**
     * Filters instance and validation data to the
     *
     *
     * @param instance
     * @param validation
     * @param task
     * @param fields
     * @param principal  @return
     * @param isAllowAny
     */
    public Map<String, List<Value>> authorizedInstanceAndValidationData(ProcessInstance instance, Validation validation, Task task, Set<Field> fields, Entity principal, String version, String reason, boolean isAllowAny) {
        ManyMap<String, Value> combinedData = SecurityUtility.combinedData(instance, validation);

        DataFilter decryptValuesFilter;
        if (task.isAssignee(principal))
            decryptValuesFilter = new DecryptValuesFilter(instance, principal, reason, accessTracker, encryptionService);
        else
            decryptValuesFilter = new MaskRestrictedValuesFilter(instance, principal, encryptionService);

        DataFilter limitFieldsFilter = isAllowAny ? new NoOpFilter() : new LimitFieldsFilter(fields, true);
        DataFilter decorateValuesFilter = new DecorateValuesFilter(instance, fields, settings, principal, version);
        return SecurityUtility.filter(combinedData, limitFieldsFilter, decryptValuesFilter, decorateValuesFilter);
    }


    public ManyMap<String, Value> exclude(Map<String, List<Value>> original) {
        ManyMap<String, Value> map = new ManyMap<String, Value>();

        if (original != null && !original.isEmpty()) {
            for (Map.Entry<String, List<Value>> entry : original.entrySet()) {
                String key = entry.getKey();
                try {
                    List<Value> filtered = exclude(entry.getValue());
                    if (!filtered.isEmpty())
                        map.put(key, filtered);
                } catch (Exception e) {
                    LOG.error("Could not decrypt messages for " + key, e);
                }
            }
        }

        return map;
    }

//    public Map<String, List<Value>> filter(Map<String, Field> fieldMap, ProcessInstance instance, Task task, Entity principal, Validation validation, boolean includeRestrictedData, boolean includeInstanceData, boolean allowAny) {
//        Map<String, List<Value>> data = filter(fieldMap, instance, task, principal, includeRestrictedData, includeInstanceData, allowAny);
//        if (validation != null) {
//            Map<String, List<Value>> validationData = filter(validation.getData(), task, principal, includeRestrictedData);
//            if (validationData != null) {
//                for (Map.Entry<String, List<Value>> entry : validationData.entrySet()) {
//                    String fieldName = entry.getKey();
//                    List<Value> values = values(instance, fieldName, entry.getValue(), null, principal);
//                    data.put(fieldName, values);
//                }
//            }
//        }
//        return data;
//    }
//
//    public Map<String, List<Value>> filter(Map<String, Field> fieldMap, ProcessInstance instance, Task task, Entity principal, boolean includeRestrictedData, boolean includeInstanceData, boolean allowAny) {
//        Map<String, List<Value>> data = includeInstanceData && instance != null ? instance.getData() : new ManyMap<String, Value>();
//        Map<String, List<Value>> filtered = new ManyMap<String, Value>();
//        if (allowAny) {
//            for (Map.Entry<String, List<Value>> entry : data.entrySet()) {
//                String fieldName = entry.getKey();
//                List<Value> values = values(instance, fieldName, entry.getValue(), null, principal);
//                filtered.put(fieldName, values);
//            }
//        }
//
//        if (fieldMap != null) {
//            for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {
//                Field field = entry.getValue();
//                String fieldName = field.getName();
//                if (fieldName == null)
//                    continue;
//
//                String defaultValue = field.getDefaultValue();
//                List<Value> values = values(instance, fieldName, data.get(fieldName), defaultValue, principal);
//                filtered.put(fieldName, values);
//            }
//        }
//
//        return filter(filtered, task, principal, includeRestrictedData);
//    }

//    private List<Value> values(ProcessInstance instance, String fieldName, List<? extends Value> values, String defaultValue, Entity principal) {
//        if (values == null || values.isEmpty()) {
//            if (StringUtils.isNotEmpty(defaultValue)) {
//                if (defaultValue.equals("{{CurrentUser}}") && principal != null)
//                    return Collections.singletonList((Value) principal.getActingAs());
//                if (defaultValue.equals("{{CurrentDate}}")) {
//                    Value currentDateValue = new Value(dateTimeFormatter.print(new Date().getTime()));
//                    return Collections.singletonList(currentDateValue);
//                }
//                if (instance != null && defaultValue.contains("{{ConfirmationNumber}}"))
//                    defaultValue = defaultValue.replaceAll("\\{\\{ConfirmationNumber\\}\\}", instance.getProcessInstanceId());
//
//                return Collections.singletonList(new Value(defaultValue));
//            }
//
//            return Collections.emptyList();
//        }
//
//        List<Value> list = new ArrayList<Value>(values.size());
//        for (Value value : values) {
//            if (value instanceof File) {
//                File file = File.class.cast(value);
//
//                list.add(new File.Builder(file, passthroughSanitizer)
//                        .processDefinitionKey(instance.getProcessDefinitionKey())
//                        .processInstanceId(instance.getProcessInstanceId())
//                        .fieldName(fieldName)
//                        .build(version));
//            } else {
//                list.add(value);
//            }
//        }
//
//        return list;
//    }

//    public ManyMap<String, Value> filter(Map<String, List<Value>> filtered, Task task, Entity principal, boolean includeRestrictedData) {
//        if (includeRestrictedData && task != null) {
//            if (SecurityUtility.isAuthorizedForRestrictedData(task, principal)) {
//                return decrypt(filtered, principal);
//            }
//            return mask(filtered);
//        }
//        return exclude(filtered);
//    }

    /*
     * Takes a passed set of fields that are currently being validated and decrypts any corresponding values so they can be validated
     */
//    public ManyMap<String, Value> decryptAnyRestrictedFieldsForValidation(Set<Field> fields, Map<String, List<Value>> data, Task task, Entity principal) {
//        ManyMap<String, Value> decryptedMap = new ManyMap<String, Value>();
//
//        // If the fields set is null or empty, just return the data as passed
//        if (fields == null || fields.isEmpty())
//            return data != null && !data.isEmpty() ? new ManyMap<String, Value>(data) : decryptedMap;
//
//        for (Field field : fields) {
//            String fieldName = ValidationUtility.fieldName(field, data);
//            List<Value> values = data.get(fieldName);
//
//            if (field.isRestricted()) {
//                if (SecurityUtility.isAuthorizedForRestrictedData(task, principal)) {
//                    try {
//                        List<Value> decrypted = decrypt(fieldName, values, principal);
//                        decryptedMap.put(fieldName, decrypted);
//                    } catch (Exception e) {
//                        LOG.error("Unable to decrypt restricted data for " + fieldName + " as requested by " + principal.getEntityId());
//                    }
//                }
//            } else {
//                if (values == null)
//                    values = Collections.emptyList();
//
//                decryptedMap.put(fieldName, values);
//            }
//        }
//        return decryptedMap;
//    }


    private static List<Value> exclude(List<? extends Value> values) throws UnsupportedEncodingException, GeneralSecurityException, InvalidCipherTextException {
        if (values.isEmpty())
            return Collections.emptyList();

        List<Value> list = new ArrayList<Value>(values.size());
        for (Value value : values) {
            if (! (value instanceof Secret)) {
                list.add(value);
            }
        }

        return list;
    }

}
