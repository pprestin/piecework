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
import piecework.model.Entity;
import piecework.model.Process;
import piecework.model.ProcessInstance;
import piecework.model.Secret;
import piecework.model.Value;
import piecework.security.AccessTracker;
import piecework.security.DataFilter;
import piecework.security.EncryptionService;

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
public class DecryptValuesFilter implements DataFilter {

    private static final Logger LOG = Logger.getLogger(DecryptValuesFilter.class);

    private final Process process;
    private final ProcessInstance instance;
    private final Entity principal;
    private final String reason;
    private final AccessTracker accessTracker;
    private final EncryptionService encryptionService;
    private final boolean isAnonymousDecryptAllowed;
    private final String processDefinitionKey;
    private final String processInstanceId;
    private final String entityId;

    public DecryptValuesFilter(Process process, ProcessInstance instance, Entity principal, String reason, AccessTracker accessTracker, EncryptionService encryptionService, boolean isAnonymousDecryptAllowed) {
        this.process = process;
        this.instance = instance;
        this.principal = principal;
        this.reason = reason;
        this.accessTracker = accessTracker;
        this.encryptionService = encryptionService;
        this.isAnonymousDecryptAllowed = isAnonymousDecryptAllowed;
        this.processDefinitionKey = process != null ? process.getProcessDefinitionKey() : null;
        this.processInstanceId = instance != null ? instance.getProcessInstanceId() : null;
        this.entityId = principal != null ? principal.getEntityId() : null;
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
                if (principal == null && !isAnonymousDecryptAllowed) {
                    LOG.error("Anonymous principals should never have access to restricted data. System will not decrypt this value");
                    continue;
                }
                Secret secret = Secret.class.cast(value);
                try {
                    accessTracker.track(process, instance, secret.getId(), key, reason, principal, isAnonymousDecryptAllowed);
                    String plaintext = encryptionService.decrypt(secret);
                    list.add(new Value(plaintext));
                    if (LOG.isInfoEnabled()) {
                        StringBuilder message = new StringBuilder("Decrypting value of restricted field ")
                                .append(key).append(" of process ")
                                .append(processDefinitionKey)
                                .append(" and instance ")
                                .append(processInstanceId)
                                .append(" on behalf of ");
                        if (entityId == null && isAnonymousDecryptAllowed)
                            message.append("anonymous submitter");
                        else
                            message.append(entityId);

                        LOG.info(message.toString());
                    }
                } catch (Exception exception) {
                    LOG.error("Failed to decrypt value of restricted field " + key + " of process " + processDefinitionKey + " and instance " + processInstanceId + " on behalf of " + entityId, exception);
                }
            } else {
                list.add(value);
            }
        }

        return list;
    }

}
