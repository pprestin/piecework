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

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;
import piecework.export.IteratingDataProvider;
import piecework.model.*;
import piecework.persistence.ProcessInstanceRepository;
import piecework.process.ProcessInstanceQueryBuilder;
import piecework.process.ProcessInstanceSearchCriteria;
import piecework.util.ExportUtility;

import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * @author James Renfro
 */
public class ExportAsExcelWorkbookProvider implements IteratingDataProvider<Row> {

    private static final Logger LOG = Logger.getLogger(ExportAsExcelWorkbookProvider.class);
    private static final int PAGE_SIZE = 200;

    private final Map<String, String> headerMap;
    private final String[] headerKeys;
    private final Query query;
    private final ProcessInstanceRepository repository;
    private final Sort sort;

    private Page<ProcessInstance> page;
    private Pageable request;
    private Workbook wb;
    private Sheet sheet;
    private int rowCount = 0;

    public ExportAsExcelWorkbookProvider(piecework.model.Process process, Map<String, String> headerMap, ProcessInstanceSearchCriteria criteria, ProcessInstanceRepository repository, Sort sort) {
        this.headerMap = headerMap;
        this.query = new ProcessInstanceQueryBuilder(criteria).build();
        this.repository = repository;
        this.request = new PageRequest(0, PAGE_SIZE, sort);
        this.sort = sort;

        this.wb = new XSSFWorkbook();
        Date now = new Date();
        this.sheet = wb.createSheet(WorkbookUtil.createSafeSheetName(process.getProcessDefinitionLabel() + " Export - " + now.toString()));

        this.headerKeys = !headerMap.isEmpty() ? headerMap.keySet().toArray(new String[headerMap.size()]) : new String[0];
    }

    @Override
    public void write(OutputStream output) throws IOException, WebApplicationException {
        try {
            getHeader();
            while (hasNext()) {
                next();
            }
            wb.write(output);
        } catch (Exception e) {
            LOG.error(e);
            throw new WebApplicationException(e);
        } finally {
            IOUtils.closeQuietly(output);
        }
    }

    @Override
    public Row getHeader() {
        Row headerRow = sheet.createRow(rowCount++);

        if (headerKeys != null && headerKeys.length > 0) {
            int count = 0;
            for (String headerKey : headerKeys) {
                String header = headerMap.get(headerKey);
                Cell cell = headerRow.createCell(count);
                if (header != null)
                    cell.setCellValue(header);
                count++;
            }
        }

        return headerRow;
    }

    @Override
    public List<Row> next() {
        Query query = this.query.with(this.request);
        this.page = repository.findByQuery(query, this.request);
        this.request = page.nextPageable();

        List<Row> rows = page.hasContent() ? new ArrayList<Row>(page.getNumberOfElements()) : Collections.<Row>emptyList();

        if (page.hasContent()) {
            List<ProcessInstance> instances = page.getContent();
            for (ProcessInstance instance : instances) {
                rows.add(convert(instance));
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

    private Row convert(ProcessInstance instance) {
        Row row = sheet.createRow(rowCount++);
        String[] columns = ExportUtility.dataColumns(instance, headerKeys, new ExcelEscaper());
        if (columns != null) {
            int j = 0;
            for (String column : columns) {
                Cell cell = row.createCell(j);
                cell.setCellValue(column);
                j++;
            }
        }

        return row;
    }

}
