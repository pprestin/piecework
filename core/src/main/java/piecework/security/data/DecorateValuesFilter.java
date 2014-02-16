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
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import piecework.common.ViewContext;
import piecework.model.*;
import piecework.security.DataFilter;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.settings.UserInterfaceSettings;

import java.util.*;

/**
 * Data filter that decorates default values and rebuilds file values to include
 * a link that can be used to access the file contents.
 *
 * @author James Renfro
 */
public class DecorateValuesFilter implements DataFilter {
    private static final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis();

    private final Map<String, Value> defaultValueMap;
    private final PassthroughSanitizer passthroughSanitizer;
    private final ProcessInstance instance;
    private final ViewContext context;

    public DecorateValuesFilter(ProcessInstance instance, Task task, Set<Field> fields, UserInterfaceSettings settings, Entity principal, String version) {
        this.defaultValueMap = new HashMap<String, Value>();
        this.passthroughSanitizer = new PassthroughSanitizer();
        this.instance = instance;
        this.context = new ViewContext(settings, version);
        if (fields != null && !fields.isEmpty()) {
            for (Field field : fields) {
                String defaultValueString = field.getDefaultValue();
                Value defaultValue = null;
                if (StringUtils.isNotEmpty(defaultValueString)) {
                    if (defaultValueString.equals("{{CurrentUser}}") && (task == null || task.isAssignee(principal)))
                        defaultValue = principal != null ? principal.getActingAs() : null; //principal != null ? principal.getActingAs() : "";
                    else if (defaultValueString.equals("{{CurrentDate}}"))
                        defaultValue = new Value(dateTimeFormatter.print(new Date().getTime()));
                    else if (defaultValueString.contains("{{ConfirmationNumber}}") && instance != null)
                        defaultValue = new Value(defaultValueString.replaceAll("\\{\\{ConfirmationNumber\\}\\}", instance.getProcessInstanceId()));
                    else
                        defaultValue = new Value(defaultValueString);

                    defaultValueMap.put(field.getName(), defaultValue);
                }
            }
        }
    }

    @Override
    public List<Value> filter(String key, List<Value> values) {
        if (isEmpty(values)) {
            Value defaultValue = defaultValueMap.get(key);
            if (defaultValue != null)
                return Collections.singletonList(defaultValue);

            return Collections.emptyList();
        }

        List<Value> list = new ArrayList<Value>(values.size());
        for (Value value : values) {
            if (value instanceof File) {
                File file = File.class.cast(value);

                list.add(new File.Builder(file, passthroughSanitizer)
                        .processDefinitionKey(instance.getProcessDefinitionKey())
                        .processInstanceId(instance.getProcessInstanceId())
                        .fieldName(key)
                        .build(context));
            } else {
                list.add(value);
            }
        }

        return list;
    }

    private static boolean isEmpty(List<Value> values) {
        if (values == null || values.isEmpty())
            return true;

        boolean isEmpty = true;
        for (Value value : values) {
            if (value != null && !value.isEmpty()) {
                isEmpty = false;
                break;
            }
        }
        return isEmpty;
    }

}
