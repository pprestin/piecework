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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.SystemUser;
import piecework.model.*;
import piecework.model.Process;
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


    @PostConstruct
    public void init() {
        if (encryptionService == null)
            encryptionService = new PassthroughEncryptionService();
    }

    /**
     * Retrieves all instance data including restricted values, decrypting them all.
     *
     *
     * @param process
     * @param instance containing the data
     * @param reason why this data is being retrieved in the code, e.g. to send it to some backend system
     * @return Map of decrypted instance data
     */
    public Map<String, List<Value>> allInstanceDataDecrypted(Process process, ProcessInstance instance, String reason) {
        LOG.info("Retrieving all instance data, decrypted, for the following reason: " + reason);
        Map<String, List<Value>> instanceData = instance != null ? instance.getData() : null;
        DataFilter decryptValuesFilter = new DecryptValuesFilter(process, instance, new SystemUser(), reason, accessTracker, encryptionService, false);
        return SecurityUtility.filter(instanceData, decryptValuesFilter);
    }

    /**
     * Retrieves all instance and validation data, including restricted values, decrypting them all.
     *
     *
     * @param process
     * @param instance containing the data
     * @param reason why this data is being retrieved in the code, e.g. to send it to some backend system
     * @return Map of decrypted instance data
     */
    public Map<String, List<Value>> allInstanceAndValidationDataDecrypted(Process process, ProcessInstance instance, Validation validation, String reason) {
        LOG.info("Retrieving all instance and validation data, decrypted, for the following reason: " + reason);
        ManyMap<String, Value> combinedData = SecurityUtility.combinedData(instance, validation);
        DataFilter decryptValuesFilter = new DecryptValuesFilter(process, instance, new SystemUser(), reason, accessTracker, encryptionService, false);
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
     *
     * @param process
     * @param instance to log access to
     * @param submission containing the data
     * @param principal entity that is accessing this data
     * @param reason why the principal is requesting the data
     * @return Map of validation data that
     */
    public Map<String, List<Value>> allSubmissionData(Process process, ProcessInstance instance, Submission submission, Entity principal, String reason) {
        Map<String, List<Value>> validationData = submission != null ? submission.getData() : null;
        boolean isAnonymousDecryptAllowed = instance == null && process.isAnonymousSubmissionAllowed();
        DataFilter decryptValuesFilter = new DecryptValuesFilter(process, instance, principal, reason, accessTracker, encryptionService, isAnonymousDecryptAllowed);
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

    public Map<String, List<Value>> authorizedInstanceData(Process process, ProcessInstance instance, Task task, Set<Field> fields, Entity principal, String version, String reason, boolean isAllowAny) {
        Map<String, List<Value>> instanceData = instance != null ? instance.getData() : null;

        DataFilter decryptValuesFilter;
        if (task.isAssignee(principal))
            decryptValuesFilter = new DecryptValuesFilter(process, instance, principal, reason, accessTracker, encryptionService, false);
        else
            decryptValuesFilter = new MaskRestrictedValuesFilter(instance, principal, encryptionService);

        DataFilter limitFieldsFilter = isAllowAny ? new NoOpFilter() : new LimitFieldsFilter(fields, true);
        DataFilter decorateValuesFilter = new DecorateValuesFilter(instance, task, fields, settings, principal, version);
        return SecurityUtility.filter(instanceData, limitFieldsFilter, decryptValuesFilter, decorateValuesFilter);
    }

    /**
     * Filters instance and validation data to the
     *
     *
     * @param process
     * @param instance
     * @param validation
     * @param task
     * @param fields
     * @param principal  @return
     * @param isAllowAny
     */
    public Map<String, List<Value>> authorizedInstanceAndValidationData(Process process, ProcessInstance instance, Validation validation, Task task, Set<Field> fields, Entity principal, String version, String reason, boolean isAllowAny) {
        ManyMap<String, Value> combinedData = SecurityUtility.combinedData(instance, validation);

        DataFilter decryptValuesFilter;
        if (task.isAssignee(principal))
            decryptValuesFilter = new DecryptValuesFilter(process, instance, principal, reason, accessTracker, encryptionService, false);
        else
            decryptValuesFilter = new MaskRestrictedValuesFilter(instance, principal, encryptionService);

        DataFilter limitFieldsFilter = isAllowAny ? new NoOpFilter() : new LimitFieldsFilter(fields, true);
        DataFilter decorateValuesFilter = new DecorateValuesFilter(instance, task, fields, settings, principal, version);
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
