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

import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.IteratingDataProvider;
import piecework.persistence.ProcessInstanceRepository;
import piecework.process.ProcessInstanceQueryBuilder;
import piecework.process.ProcessInstanceSearchCriteria;

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

    private Page<ProcessInstance> page;
    private Pageable request;

    public ExportInstanceProvider(Process process, ProcessInstanceSearchCriteria criteria, ProcessInstanceRepository repository, Sort sort) {
        this.headerMap = new LinkedHashMap<String, String>();
        this.query = new ProcessInstanceQueryBuilder(criteria).build();
        this.repository = repository;
        this.request = new PageRequest(0, PAGE_SIZE, sort);
        this.sort = sort;

        String[] headerKeys = null;
        ProcessDeployment deployment = process.getDeployment();
        if (deployment != null) {
            Collection<Activity> activities = deployment.getActivityMap().values();
            for (Activity activity : activities) {
                Set<Field> fields = activity.getFields();
                if (fields != null) {
                    for (Field field : fields) {
                        if (field.isRestricted())
                            continue;

                        String fieldName = field.getName();
                        String fieldLabel = field.getLabel();
                        String fieldHeader = field.getHeader();

                        if (StringUtils.isNotEmpty(fieldHeader))
                            headerMap.put(fieldName, "\"" + fieldHeader + "\"");
                        else
                            headerMap.put(fieldName, "\"" + fieldLabel+ "\"");
                    }
                }
            }
            if (!headerMap.isEmpty())
                headerKeys = headerMap.keySet().toArray(new String[headerMap.size()]);
        }
        this.headerKeys = headerKeys;
    }

    @Override
    public String getHeader() {
        return StringUtils.join(headerMap.values(), ", ");
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
                List<Value> values = data.get(headerKeys[i]);
                builder.append("\"");
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
                builder.append("\"");
                if (i != length)
                    builder.append(", ");
            }
        }
        return builder.toString();
    }

}
