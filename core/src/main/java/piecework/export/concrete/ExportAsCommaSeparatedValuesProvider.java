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
package piecework.export.concrete;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;
import piecework.export.IteratingDataProvider;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessInstanceRepository;
import piecework.process.ProcessInstanceQueryBuilder;
import piecework.process.ProcessInstanceSearchCriteria;
import piecework.util.ExportUtility;

import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author James Renfro
 */
public class ExportAsCommaSeparatedValuesProvider implements IteratingDataProvider<String> {

    private static final Logger LOG = Logger.getLogger(ExportAsCommaSeparatedValuesProvider.class);

    private static final int PAGE_SIZE = 200;

    private final Map<String, String> headerMap;
    private final String[] headerKeys;
    private final Query query;
    private final ProcessInstanceRepository repository;
    private final Sort sort;

    private Page<ProcessInstance> page;
    private Pageable request;

    public ExportAsCommaSeparatedValuesProvider(Process process, Map<String, String> headerMap, ProcessInstanceSearchCriteria criteria, ProcessInstanceRepository repository, Sort sort) {
        this.headerMap = headerMap;
        this.query = new ProcessInstanceQueryBuilder(criteria).build();
        this.repository = repository;
        this.request = new PageRequest(0, PAGE_SIZE, sort);
        this.sort = sort;

        this.headerKeys = !headerMap.isEmpty() ? headerMap.keySet().toArray(new String[headerMap.size()]) : new String[0];
    }

    @Override
    public void write(OutputStream output) throws IOException, WebApplicationException {
        PrintWriter writer = null;

        try {
            writer = new PrintWriter(output, true);
            writer.println(getHeader());
            while (hasNext()) {
                List<String> rows = next();
                for (String row : rows) {
                    writer.print(row);
                }
            }
        } catch (Exception e) {
            LOG.error(e);
            throw new WebApplicationException(e);
        } finally {
            if (writer != null)
                writer.close();
        }

    }

    @Override
    public String getHeader() {
        List<String> quotedValues = new ArrayList<String>();
        if (!headerMap.isEmpty()) {
            for (String header : headerMap.values()) {
                quotedValues.add(StringEscapeUtils.escapeCsv(header));
            }
        }
        return StringUtils.join(quotedValues, ", ");
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
        String[] columns = ExportUtility.dataColumns(instance, headerKeys, new CsvEscaper());
        String crLf = Character.toString((char)13) + Character.toString((char)10);

        StringBuilder builder = new StringBuilder(StringUtils.join(columns, ","));
        return builder.append(crLf).toString();
    }

}
