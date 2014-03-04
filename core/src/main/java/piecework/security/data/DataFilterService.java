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
import piecework.common.ManyMap;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.persistence.ProcessInstanceProvider;
import piecework.persistence.ProcessProvider;
import piecework.security.AccessTracker;
import piecework.security.DataFilter;
import piecework.security.EncryptionService;
import piecework.security.concrete.PassthroughEncryptionService;
import piecework.settings.UserInterfaceSettings;
import piecework.util.ModelUtility;
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
     * @param modelProvider of model data objects
     * @param reason why this data is being retrieved in the code, e.g. to send it to some backend system
     * @return Map of decrypted instance data
     */
    public <P extends ProcessInstanceProvider> Map<String, List<Value>> allInstanceDataDecrypted(P modelProvider, String reason) throws PieceworkException {
        LOG.info("Retrieving all instance data, decrypted, for the following reason: " + reason);
        ProcessInstance instance = modelProvider.instance();
        Map<String, List<Value>> instanceData = instance != null ? instance.getData() : null;
        DataFilter decryptValuesFilter = new DecryptValuesFilter(modelProvider, reason, accessTracker, encryptionService, false);
        return SecurityUtility.filter(Collections.<Field>emptySet(), instanceData, decryptValuesFilter);
    }

    /**
     * Retrieves all instance and validation data, including restricted values, decrypting them all.
     *
     * @param modelProvider of model data objects
     * @param reason why this data is being retrieved in the code, e.g. to send it to some backend system
     * @return Map of decrypted instance data
     */
    public <P extends ProcessInstanceProvider> Map<String, List<Value>> allInstanceAndValidationDataDecrypted(P modelProvider, Validation validation, String reason) throws PieceworkException {
        if (LOG.isDebugEnabled())
            LOG.debug("Retrieving all instance and validation data, decrypted, for the following reason: " + reason);
        ProcessInstance instance = modelProvider.instance();
        ManyMap<String, Value> combinedData = SecurityUtility.combinedData(instance, validation);
        DataFilter decryptValuesFilter = new DecryptValuesFilter(modelProvider, reason, accessTracker, encryptionService, false);
        return SecurityUtility.filter(Collections.<Field>emptySet(), combinedData, decryptValuesFilter);
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
        return SecurityUtility.filter(fields, instanceData, limitFieldsFilter);
    }

    /**
     * Retrieves all submission data, including restricted fields, which will be
     * decrypted
     *
     *
     * @param provider of model data objects
     * @param submission containing the data
     * @param reason why the principal is requesting the data
     * @return Map of validation data that
     */
    public <P extends ProcessProvider> Map<String, List<Value>> allSubmissionData(P provider, Submission submission, String reason) throws PieceworkException {
        Map<String, List<Value>> validationData = submission != null ? submission.getData() : null;
        Process process = provider.process();
        ProcessInstance instance = ModelUtility.instance(provider);
        boolean isAnonymousDecryptAllowed = instance == null && process.isAnonymousSubmissionAllowed();
        DataFilter decryptValuesFilter = new DecryptValuesFilter(provider, reason, accessTracker, encryptionService, isAnonymousDecryptAllowed);
        return SecurityUtility.filter(Collections.<Field>emptySet(), validationData, decryptValuesFilter);
    }

    /**
     * Filters validation data to only values that belong to the passed fields,
     * but includes restricted values, since these need to be passed back to the user
     * whose validation is being evaluated -- NOTE: it's important not to use this method
     * in cases where the Validation could be retrieved by someone other than the person
     * who submitted the data.
     *
     * @param validation containing the data
     * @param fields
     * @param isAllowAny
     * @param principal
     * @return Map of validation data that
     */
    public Map<String, List<Value>> allValidationData(Validation validation, Set<Field> fields, boolean isAllowAny, Entity principal) {
        Map<String, List<Value>> validationData = validation != null ? validation.getData() : null;
        if (isAllowAny)
            return validationData;

        // Need to include restricted fields since they are submitted by the requesting user
        DataFilter limitFieldsFilter = isAllowAny ? new NoOpFilter() : new LimitFieldsFilter(fields, true);
        DataFilter decorateValuesFilter = new DecorateValuesFilter(null, null, fields, settings, principal, null);
        return SecurityUtility.filter(fields, validationData, limitFieldsFilter, decorateValuesFilter);
    }

    public <P extends ProcessDeploymentProvider> Map<String, List<Value>> authorizedInstanceData(P modelProvider, Set<Field> fields, String version, String reason, boolean isAllowAny) throws PieceworkException {
        ProcessInstance instance = ModelUtility.instance(modelProvider);
        Map<String, List<Value>> instanceData = instance != null ? instance.getData() : null;

        DataFilter decryptValuesFilter;
        Task task = ModelUtility.task(modelProvider);
        Entity principal = modelProvider.principal();

        if (task != null && task.isAssignee(principal) && task.isActive())
            decryptValuesFilter = new DecryptValuesFilter(modelProvider, reason, accessTracker, encryptionService, false);
        else
            decryptValuesFilter = new MaskRestrictedValuesFilter(modelProvider, encryptionService);

        DataFilter limitFieldsFilter = isAllowAny ? new NoOpFilter() : new LimitFieldsFilter(fields, true);
        DataFilter decorateValuesFilter = new DecorateValuesFilter(instance, task, fields, settings, principal, version);
        return SecurityUtility.filter(fields, instanceData, limitFieldsFilter, decryptValuesFilter, decorateValuesFilter);
    }

    /**
     * Filters instance and validation data to the
     *
     * @param modelProvider
     * @param validation
     * @param fields
     * @param isAllowAny
     */
    public <P extends ProcessDeploymentProvider> Map<String, List<Value>> authorizedInstanceAndValidationData(P modelProvider, Validation validation, Set<Field> fields, String version, String reason, boolean isAllowAny) throws PieceworkException {
        ProcessInstance instance = ModelUtility.instance(modelProvider);
        ManyMap<String, Value> combinedData = SecurityUtility.combinedData(instance, validation);

        DataFilter decryptValuesFilter;
        Task task = ModelUtility.task(modelProvider);
        Entity principal = modelProvider.principal();

        if (task != null && task.isAssignee(principal) && task.isActive())
            decryptValuesFilter = new DecryptValuesFilter(modelProvider, reason, accessTracker, encryptionService, false);
        else
            decryptValuesFilter = new MaskRestrictedValuesFilter(modelProvider, encryptionService);

        DataFilter limitFieldsFilter = isAllowAny ? new NoOpFilter() : new LimitFieldsFilter(fields, true);
        DataFilter decorateValuesFilter = new DecorateValuesFilter(instance, task, fields, settings, principal, version);
        return SecurityUtility.filter(fields, combinedData, limitFieldsFilter, decryptValuesFilter, decorateValuesFilter);
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
