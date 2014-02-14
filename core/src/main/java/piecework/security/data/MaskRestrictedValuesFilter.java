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

import com.google.common.base.Strings;
import org.apache.log4j.Logger;
import piecework.model.Entity;
import piecework.model.ProcessInstance;
import piecework.model.Secret;
import piecework.model.Value;
import piecework.security.DataFilter;
import piecework.security.EncryptionService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data filter that decrypts and then masks all restricted values to their
 * appropriate plaintext length. The encrypted values are not returned from the
 * filter method; only the masked values are returned.
 *
 * @author James Renfro
 */
public class MaskRestrictedValuesFilter implements DataFilter {

    private static final Logger LOG = Logger.getLogger(MaskRestrictedValuesFilter.class);

    private final EncryptionService encryptionService;
    private final ProcessInstance instance;
    private final Entity principal;

    public MaskRestrictedValuesFilter(ProcessInstance instance, Entity principal, EncryptionService encryptionService) {
        this.instance = instance;
        this.principal = principal;
        this.encryptionService = encryptionService;
    }

    @Override
    public List<Value> filter(String key, List<Value> values) {
        if (values == null || values.isEmpty())
            return Collections.emptyList();

        String entityId = principal != null ? principal.getEntityId() : "ANONYMOUS";
        List<Value> list = new ArrayList<Value>(values.size());
        for (Value value : values) {
            if (value instanceof Secret) {
                Secret secret = Secret.class.cast(value);
                try {
                    String plaintext = encryptionService.decrypt(secret);
                    list.add(new Value(Strings.repeat("*", plaintext.length())));
                    if (LOG.isInfoEnabled())
                        LOG.info("Masking value of restricted field " + key + " of process " + instance.getProcessDefinitionKey() + " and instance " + instance.getProcessInstanceId() + " on behalf of " + entityId);
                } catch (Exception exception) {
                    LOG.error("Failed to mask value of restricted field " + key + " of process " + instance.getProcessDefinitionKey() + " and instance " + instance.getProcessInstanceId() + " on behalf of " + entityId, exception);
                }
            } else {
                list.add(value);
            }
        }

        return list;
    }
}
