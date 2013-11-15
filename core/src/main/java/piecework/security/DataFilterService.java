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

import org.apache.commons.lang.StringUtils;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Versions;
import piecework.model.*;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.util.ManyMap;

import java.util.*;

/**
 * @author James Renfro
 */
@Service
public class DataFilterService {

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private Versions versions;

    private static final PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
    private static final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis();

    public Map<String, List<Value>> filter(Map<String, Field> fieldMap, ProcessInstance instance, Task task, Entity principal) {
        Map<String, List<Value>> data = instance != null ? instance.getData() : new ManyMap<String, Value>();
        Map<String, List<Value>> filtered = new ManyMap<String, Value>();
        if (fieldMap != null) {
            for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {
                Field field = entry.getValue();
                String fieldName = field.getName();
                String defaultValue = field.getDefaultValue();
                List<Value> values = values(instance, fieldName, data.get(fieldName), defaultValue, principal);
                filtered.put(fieldName, values);
            }
        }

        if (task.isAssignee(principal)) {
            return encryptionService.decrypt(filtered);
        }
        return encryptionService.mask(filtered);
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

}
