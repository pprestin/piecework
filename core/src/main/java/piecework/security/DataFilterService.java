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
package piecework.security;

import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Versions;
import piecework.model.*;
import piecework.security.concrete.PassthroughEncryptionService;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.util.ManyMap;
import piecework.validation.FormValidation;

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

    @Autowired(required = false)
    private EncryptionService encryptionService;

    @Autowired
    private Versions versions;

    private static final PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
    private static final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis();

    @PostConstruct
    public void init() {
        if (encryptionService == null)
            encryptionService = new PassthroughEncryptionService();
    }

    public Map<String, List<Value>> filter(Map<String, Field> fieldMap, ProcessInstance instance, Task task, Entity principal, FormValidation validation) {
        Map<String, List<Value>> data = filter(fieldMap, instance, task, principal, false);
        if (validation != null) {
            Map<String, List<Value>> validationData = validation.getData();
            if (validationData != null) {
                for (Map.Entry<String, List<Value>> entry : validationData.entrySet()) {
                    data.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return data;
    }

    public Map<String, List<Value>> filter(Map<String, Field> fieldMap, ProcessInstance instance, Task task, Entity principal, boolean includeRestrictedData) {
        Map<String, List<Value>> data = instance != null ? instance.getData() : new ManyMap<String, Value>();
        Map<String, List<Value>> filtered = new ManyMap<String, Value>();
        if (fieldMap != null) {
            for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {
                Field field = entry.getValue();
                String fieldName = field.getName();
                if (fieldName == null)
                    continue;

                String defaultValue = field.getDefaultValue();
                List<Value> values = values(instance, fieldName, data.get(fieldName), defaultValue, principal);
                filtered.put(fieldName, values);
            }
        }

        if (includeRestrictedData && task != null) {
            if (task.isAssignee(principal)) {
                return decrypt(filtered);
            }
            return mask(filtered);
        }
        return exclude(filtered);
    }

    private List<Value> values(ProcessInstance instance, String fieldName, List<? extends Value> values, String defaultValue, Entity principal) {
        if (values == null || values.isEmpty()) {
            if (StringUtils.isNotEmpty(defaultValue)) {
                if (defaultValue.equals("{{CurrentUser}}") && principal != null)
                    return Collections.singletonList((Value) principal.getActingAs());
                if (defaultValue.equals("{{CurrentDate}}")) {
                    Value currentDateValue = new Value(dateTimeFormatter.print(new Date().getTime()));
                    return Collections.singletonList(currentDateValue);
                }
                return Collections.singletonList(new Value(defaultValue));
            }

            return Collections.emptyList();
        }

        List<Value> list = new ArrayList<Value>(values.size());
        for (Value value : values) {
            if (value instanceof File) {
                File file = File.class.cast(value);

                list.add(new File.Builder(file, passthroughSanitizer)
                        .processDefinitionKey(instance.getProcessDefinitionKey())
                        .processInstanceId(instance.getProcessInstanceId())
                        .fieldName(fieldName)
                        .build(versions.getVersion1()));
            } else {
                list.add(value);
            }
        }

        return list;
    }

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

    public List<Value> encrypt(List<? extends Value> values) throws UnsupportedEncodingException, GeneralSecurityException, InvalidCipherTextException {
        if (values.isEmpty())
            return Collections.emptyList();

        List<Value> list = new ArrayList<Value>(values.size());
        for (Value value : values) {
            if (value instanceof Value) {
                String plaintext = value != null ? value.getValue() : null;
                if (StringUtils.isNotEmpty(plaintext)) {
                    list.add(encryptionService.encrypt(plaintext));
                }
            } else {
                list.add(value);
            }
        }

        return list;
    }

    private List<Value> decrypt(List<? extends Value> values) throws UnsupportedEncodingException, GeneralSecurityException, InvalidCipherTextException {
        if (values.isEmpty())
            return Collections.emptyList();

        List<Value> list = new ArrayList<Value>(values.size());
        for (Value value : values) {
            if (value instanceof Secret) {
                Secret secret = Secret.class.cast(value);
                String plaintext = encryptionService.decrypt(secret);
                list.add(new Value(plaintext));
            } else {
                list.add(value);
            }
        }

        return list;
    }

    private ManyMap<String, Value> mask(Map<String, List<Value>> original) {
        ManyMap<String, Value> map = new ManyMap<String, Value>();

        if (original != null && !original.isEmpty()) {
            for (Map.Entry<String, List<Value>> entry : original.entrySet()) {
                String key = entry.getKey();
                try {
                    List<Value> masked = mask(entry.getValue());
                    map.put(key, masked);
                } catch (Exception e) {
                    LOG.error("Could not decrypt messages for " + key, e);
                }
            }
        }

        return map;
    }

    private List<Value> mask(List<? extends Value> values) throws UnsupportedEncodingException, GeneralSecurityException, InvalidCipherTextException {
        if (values.isEmpty())
            return Collections.emptyList();

        List<Value> list = new ArrayList<Value>(values.size());
        for (Value value : values) {
            if (value instanceof Secret) {
                Secret secret = Secret.class.cast(value);
                String plaintext = encryptionService.decrypt(secret);
                list.add(new Value(Strings.repeat("*", plaintext.length())));
            } else {
                list.add(value);
            }
        }

        return list;
    }

    private ManyMap<String, Value> exclude(Map<String, List<Value>> original) {
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

    private List<Value> exclude(List<? extends Value> values) throws UnsupportedEncodingException, GeneralSecurityException, InvalidCipherTextException {
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
