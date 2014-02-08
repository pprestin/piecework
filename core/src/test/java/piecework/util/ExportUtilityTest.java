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

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import piecework.export.concrete.ExcelEscaper;
import piecework.model.Field;
import piecework.model.ProcessInstance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
public class ExportUtilityTest {

    @Test
    public void testHeaderMap() {
        List<Field> fields = new ArrayList<Field>();
        fields.add(new Field.Builder()
            .name("TestValue1")
            .label("Test Label 1")
            .header("Test Header 1")
            .build());
        fields.add(new Field.Builder()
                .name("TestValue2")
                .label("Test Label 2")
                .build());

        Map<String, String> headerMap = ExportUtility.headerMap(fields);
        Assert.assertEquals(6, headerMap.size());
        Assert.assertEquals("Test Header 1", headerMap.get("TestValue1"));
        Assert.assertEquals("Test Label 2", headerMap.get("TestValue2"));
    }

    @Test
    public void testDataColumns() {
        Date startTime = new Date();
        Date endTime = new Date();
        String[] headerKeys = {"ID", "Title", "TestValue1", "TestValue2", "Submitted", "Completed"};
        ProcessInstance instance = new ProcessInstance.Builder()
                .processInstanceId("1234")
                .processInstanceLabel("A Simple Test Instance")
                .formValue("TestValue1", "1,2,3")
                .startTime(startTime)
                .endTime(endTime)
                .build();

        String[] columns = ExportUtility.dataColumns(instance, headerKeys, new ExcelEscaper());
        Assert.assertEquals(6, columns.length);
        Assert.assertEquals("1234", columns[0]);
        Assert.assertEquals("A Simple Test Instance", columns[1]);
        Assert.assertEquals("1,2,3", columns[2]);
        Assert.assertNull(columns[3]);
        Assert.assertEquals(startTime.toString(), columns[4]);
        Assert.assertEquals(endTime.toString(), columns[5]);
    }

}
