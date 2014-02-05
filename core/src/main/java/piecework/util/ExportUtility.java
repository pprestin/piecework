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
package piecework.util;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import piecework.Constants;
import piecework.enumeration.ActionType;
import piecework.export.Escaper;
import piecework.model.*;
import piecework.model.Process;

import java.util.*;

/**
 * @author James Renfro
 */
public class ExportUtility {

    public static Map<String, String> headerMap(Process process) {
        Map<String, String> headerMap = new LinkedHashMap<String, String>();

        headerMap.put("__processInstanceId", "ID");
        headerMap.put("__title", "Title");
        ProcessDeployment deployment = process.getDeployment();
        if (deployment != null) {
            Collection<Activity> activities = deployment.getActivityMap().values();
            for (Activity activity : activities) {
                Action action = activity.action(ActionType.CREATE);
                if (action != null) {
                    Container parentContainer = ActivityUtil.parent(activity, ActionType.CREATE);
                    Container container = ActivityUtil.child(activity, ActionType.CREATE, parentContainer);

                    Map<String, Field> fieldMap = activity.getFieldMap();
                    List<String> fieldIds = ActivityUtil.fieldIds(container, parentContainer);
                    List<Field> fields = new ArrayList<Field>();
                    if (fieldIds != null) {
                        for (String fieldId : fieldIds) {
                            Field field = fieldMap.get(fieldId);
                            if (field != null)
                                fields.add(field);
                        }
                    }
                    if (fields != null) {
                        for (Field field : fields) {
                            if (field.isRestricted())
                                continue;
                            if (field.getType().equals(Constants.FieldTypes.HTML))
                                continue;

                            String fieldName = field.getName();
                            String fieldLabel = field.getLabel();
                            String fieldHeader = field.getHeader();

                            if (StringUtils.isEmpty(fieldName))
                                continue;

                            if (StringUtils.isNotEmpty(fieldHeader))
                                headerMap.put(fieldName, StringEscapeUtils.unescapeXml(fieldHeader));
                            else if (StringUtils.isNotEmpty(fieldLabel))
                                headerMap.put(fieldName, StringEscapeUtils.unescapeXml(fieldLabel));
                            else
                                headerMap.put(fieldName, "");
                        }
                    }
                }
            }
            headerMap.put("__submitted", "Submitted");
            headerMap.put("__completed", "Completed");
        }
        return headerMap;
    }

    public static String[] dataColumns(ProcessInstance instance, String[] headerKeys, Escaper escaper) {
        Map<String, List<Value>> data = instance.getData();
        int length = headerKeys.length;
        int endTimeIndex = length - 1;
        int startTimeIndex = length - 2;
        String[] columns = new String[headerKeys.length];
        columns[0] = escaper.escape(instance.getProcessInstanceId());
        columns[1] = escaper.escape(instance.getProcessInstanceLabel());
        columns[startTimeIndex] = instance.getStartTime() != null ? escaper.escape(instance.getStartTime().toString()) : null;
        columns[endTimeIndex] = instance.getEndTime() != null ? escaper.escape(instance.getEndTime().toString()) : null;
        for (int i=2;i<startTimeIndex;i++) {
            String headerKey = headerKeys[i];
            String columnValue = null;
            List<Value> values = data.get(headerKey);
            if (values != null && !values.isEmpty()) {
                StringBuilder valueBuilder = new StringBuilder();
                int lastValue = values.size() - 1;
                for (int j=0;j<=lastValue;j++) {
                    Value value = values.get(j);
                    String text = value.toString();
                    if (StringUtils.isNotEmpty(text)) {
                        valueBuilder.append(text);
                        if (j != lastValue)
                            valueBuilder.append(", ");
                    }
                }
                columns[i] = escaper.escape(valueBuilder.toString());
            }
        }
        return columns;
    }

}
