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

import junit.framework.Assert;
import org.junit.Test;
import piecework.export.Pager;
import piecework.model.Field;
import piecework.model.ProcessInstance;
import piecework.util.ExportUtility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author James Renfro
 */
public class ExportAsCommaSeparatedValuesProviderTest {

    @Test
    public void test() throws IOException {
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

        Date startTime = new Date(1391725603939l);
        Date endTime = new Date(1391725704039l);

        ProcessInstance instance = new ProcessInstance.Builder()
                .processInstanceId("1234")
                .processInstanceLabel("A Simple Test Instance")
                .formValue("TestValue1", "Something")
                .formValue("TestValue2", "Another")
                .startTime(startTime)
                .endTime(endTime)
                .build();

        Pager<ProcessInstance> pager = new ProcessInstanceListPager(Collections.singletonList(instance));
        ExportAsCommaSeparatedValuesProvider provider = new ExportAsCommaSeparatedValuesProvider(headerMap, pager);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        provider.write(outputStream);

        String expected = "ID,Title,Test Header 1,Test Label 2,Submitted,Completed" + Character.toString((char)13) + Character.toString((char)10) +
                "1234,A Simple Test Instance,Something,Another,Thu Feb 06 14:26:43 PST 2014,Thu Feb 06 14:28:24 PST 2014" + Character.toString((char)13) + Character.toString((char)10);

        String actual = new String(outputStream.toByteArray());
        Assert.assertEquals(expected, actual);
    }


}
