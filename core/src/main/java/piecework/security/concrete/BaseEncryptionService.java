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
package piecework.security.concrete;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.InvalidCipherTextException;
import piecework.model.Secret;
import piecework.model.Value;
import piecework.security.EncryptionService;
import piecework.util.ManyMap;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
public abstract class BaseEncryptionService implements EncryptionService {

    private static final Logger LOG = Logger.getLogger(BaseEncryptionService.class);

    public ManyMap<String, Value> decrypt(Map<String, List<Value>> original) {
        ManyMap<String, Value> map = new ManyMap<String, Value>();

        if (original != null && !original.isEmpty()) {
            for (Map.Entry<String, List<Value>> entry : original.entrySet()) {
                String key = entry.getKey();
                try {
                    List<Value> decrypted = decrypt(entry.getValue());
                    map.put(key, decrypted);
                } catch (Exception e) {
                    LOG.error("Could not decrypt messages for " + key, e);
                }
            }
        }

        return map;
    }

    public List<Value> decrypt(List<? extends Value> values) throws UnsupportedEncodingException, GeneralSecurityException, InvalidCipherTextException {
        if (values.isEmpty())
            return Collections.emptyList();

        List<Value> list = new ArrayList<Value>(values.size());
        for (Value value : values) {
            if (value instanceof Secret) {
                Secret secret = Secret.class.cast(value);
                String plaintext = decrypt(secret);
                list.add(new Value(plaintext));
            } else {
                list.add(value);
            }
        }

        return list;
    }

}
