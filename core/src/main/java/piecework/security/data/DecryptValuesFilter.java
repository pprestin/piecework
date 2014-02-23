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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import piecework.enumeration.AlarmSeverity;
import piecework.model.Entity;
import piecework.model.Process;
import piecework.model.ProcessInstance;
import piecework.model.Secret;
import piecework.model.Value;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.persistence.ProcessProvider;
import piecework.security.AccessTracker;
import piecework.security.DataFilter;
import piecework.security.EncryptionService;
import piecework.util.ModelUtility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data filter that decrypts values passed, and calls AccessTracker to log the fact
 * that restricted data is being decrypted, including a reason why and who has
 * requested access to it
 *
 * @author James Renfro
 */
public class DecryptValuesFilter<P extends ProcessProvider> implements DataFilter {

    private static final Logger LOG = Logger.getLogger(DecryptValuesFilter.class);
    private static final String ANONYMOUS_RESTRICTED_MESSAGE = "Anonymous principals should never have access to restricted data. System will not decrypt this value ";

    private final P modelProvider;
    private final String reason;
    private final AccessTracker accessTracker;
    private final EncryptionService encryptionService;
    private final boolean isAnonymousDecryptAllowed;

    private final String processInstanceId;
    private final String entityId;

    private final String coreMessage;

    public DecryptValuesFilter(P modelProvider, String reason, AccessTracker accessTracker, EncryptionService encryptionService, boolean isAnonymousDecryptAllowed) {
        this.modelProvider = modelProvider;
        this.reason = reason;
        this.accessTracker = accessTracker;
        this.encryptionService = encryptionService;
        this.isAnonymousDecryptAllowed = isAnonymousDecryptAllowed;
        this.processInstanceId = ModelUtility.instanceId(modelProvider);
        this.entityId = modelProvider.principal() != null ? modelProvider.principal().getEntityId() : null;
        this.coreMessage = message(modelProvider.processDefinitionKey(), processInstanceId, entityId, isAnonymousDecryptAllowed);
    }

    @Override
    public List<Value> filter(String key, List<Value> values) {
        if (values == null || values.isEmpty())
            return Collections.emptyList();

        List<Value> list = new ArrayList<Value>(values.size());
        for (Value value : values) {
            if (value instanceof Secret) {
                // The only time we should ever anonymously decrypt anything is for submission data on a process that is allowed to submit anonymous
                // submission data, when no instance yet exists...
                if (StringUtils.isEmpty(entityId) && !isAnonymousDecryptAllowed) {
                    accessTracker.alarm(AlarmSeverity.URGENT, new StringBuilder(ANONYMOUS_RESTRICTED_MESSAGE).append(key).append(coreMessage).toString());
                    continue;
                }
                Secret secret = Secret.class.cast(value);
                try {
                    accessTracker.track(modelProvider, secret.getId(), key, reason, isAnonymousDecryptAllowed);
                    String plaintext = encryptionService.decrypt(secret);
                    list.add(new Value(plaintext));
                    if (LOG.isInfoEnabled())
                        LOG.info(new StringBuilder("Decrypting ").append(key).append(coreMessage).toString());
                } catch (Exception exception) {
                    LOG.error(new StringBuilder("Failed to decrypt ").append(key).append(coreMessage).toString(), exception);
                }
            } else {
                list.add(value);
            }
        }

        return list;
    }

    private static String message(String processDefinitionKey, String processInstanceId, String entityId, boolean isAnonymousDecryptAllowed) {
        StringBuilder message = new StringBuilder(" restricted field for process ").append(processDefinitionKey);

        if (StringUtils.isNotEmpty(processInstanceId))
            message.append(" and instance ").append(processInstanceId);

        message.append(" on behalf of ");
        if (entityId == null && isAnonymousDecryptAllowed)
            message.append("anonymous submitter");
        else
            message.append(entityId);

        return message.toString();
    }

}
