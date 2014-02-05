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
package piecework.persistence.concrete;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;
import piecework.Constants;
import piecework.enumeration.ActionType;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.IteratingDataProvider;
import piecework.persistence.ProcessInstanceRepository;
import piecework.process.ProcessInstanceQueryBuilder;
import piecework.process.ProcessInstanceSearchCriteria;
import piecework.util.ActivityUtil;

import java.io.BufferedWriter;
import java.util.*;

/**
 * @author James Renfro
 */
public class ExportInstanceProvider implements IteratingDataProvider<String> {

    private static final int PAGE_SIZE = 200;

    private final Map<String, String> headerMap;
    private final String[] headerKeys;
    private final Query query;
    private final ProcessInstanceRepository repository;
    private final Sort sort;
    private final boolean isCSV;

    private Page<ProcessInstance> page;
    private Pageable request;
    private Workbook wb;

    public ExportInstanceProvider(Process process, ProcessInstanceSearchCriteria criteria, ProcessInstanceRepository repository, Sort sort, boolean isCSV) {
        this.headerMap = new LinkedHashMap<String, String>();
        this.query = new ProcessInstanceQueryBuilder(criteria).build();
        this.repository = repository;
        this.request = new PageRequest(0, PAGE_SIZE, sort);
        this.sort = sort;
        this.isCSV = isCSV;

        if (!isCSV) {
            Workbook wb = new XSSFWorkbook();
        }

        this.headerMap.put("__processInstanceId", "\"ID\"");
        this.headerMap.put("__title", "\"Title\"");
        String[] headerKeys = null;
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
            this.headerMap.put("__submitted", "\"Submitted\"");
            this.headerMap.put("__completed", "\"Completed\"");

            if (!headerMap.isEmpty())
                headerKeys = headerMap.keySet().toArray(new String[headerMap.size()]);
        }
        this.headerKeys = headerKeys;
    }

    @Override
    public String getHeader() {
        if (isCSV) {
            List<String> quotedValues = new ArrayList<String>();
            if (!headerMap.isEmpty()) {
                for (String header : headerMap.values()) {
                    quotedValues.add(new StringBuilder("\"").append(header).append("\"").toString());
                }
            }
            return StringUtils.join(quotedValues, ", ");
        } else {
            return null;
        }

    }

    @Override
    public List<String> next() {
        Query query = this.query.with(this.request);
        this.page = repository.findByQuery(query, this.request);
        this.request = page.nextPageable();

        List<String> rows = page.hasContent() ? new ArrayList<String>(page.getNumberOfElements()) : Collections.<String>emptyList();

        if (page.hasContent()) {
            List<ProcessInstance> instances = page.getContent();
            for (ProcessInstance instance : instances) {
                String row = convert(instance);
                if (row != null)
                    rows.add(row);
            }
        }

        return rows;
    }

    @Override
    public boolean hasNext() {
        return this.page == null || !this.page.isLastPage();
    }

    @Override
    public void reset() {
        this.request = new PageRequest(0, PAGE_SIZE, sort);
    }

    private String convert(ProcessInstance instance) {
        StringBuilder builder = new StringBuilder();
        Map<String, List<Value>> data = instance.getData();
        if (headerKeys != null) {
            int length = headerKeys.length - 1;
            for (int i=0;i<=length;i++) {
                builder.append("\"");
                String headerKey = headerKeys[i];
                if (i < 2) {
                    if (headerKey.equals("__processInstanceId"))
                        builder.append(instance.getProcessInstanceId());
                    else if (headerKey.equals("__title"))
                        builder.append(instance.getProcessInstanceLabel());
                } else if (i > length - 2) {
                    if (headerKey.equals("__submitted") && instance.getStartTime() != null)
                        builder.append(instance.getStartTime());
                    else if (headerKey.equals("__completed") && instance.getEndTime() != null)
                        builder.append(instance.getEndTime());
                } else {
                    List<Value> values = data.get(headerKey);
                    if (values != null && !values.isEmpty()) {
                        int lastValue = values.size() - 1;
                        for (int j=0;j<=lastValue;j++) {
                            Value value = values.get(j);
                            String text = value.toString();
                            if (text != null)
                                builder.append(text);
                            if (j != lastValue)
                                builder.append(", ");
                        }
                    }
                }
                builder.append("\"");
                if (i != length)
                    builder.append(", ");
            }
        }
        String crLf = Character.toString((char)13) + Character.toString((char)10);
        return builder.append(crLf).toString();
    }

}
