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
import piecework.model.ProcessInstance;
import piecework.model.Task;

import java.util.Date;

/**
 * @author James Renfro
 */
public class ProcessInstanceUtilityTest {

    @Test
    public void verifyCompletedByInitiator() {
        String expected = "testuser-1";
        ProcessInstance instance = new ProcessInstance.Builder()
                .initiatorId(expected)
                .build();

        String actual = ProcessInstanceUtility.completedBy(instance);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void verifyCompletedByOnlyCompletedTask() {
        String expected = "testuser-2";
        ProcessInstance instance = new ProcessInstance.Builder()
                .initiatorId("testuser-1")
                .task(new Task.Builder()
                        .taskInstanceId("1231")
                        .assigneeId(expected)
                        .startTime(new Date())
                        .endTime(new Date())
                        .build())
                .build();

        String actual = ProcessInstanceUtility.completedBy(instance);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void verifyCompletedByLastCompletedTask() {
        String expected = "testuser-3";
        ProcessInstance instance = new ProcessInstance.Builder()
                .initiatorId("testuser-1")
                .task(new Task.Builder()
                        .taskInstanceId("1231")
                        .assigneeId("testuser-2")
                        .startTime(new Date())
                        .endTime(new Date(1000))
                        .build())
                .task(new Task.Builder()
                        .taskInstanceId("1232")
                        .assigneeId(expected)
                        .startTime(new Date())
                        .endTime(new Date(2000))
                        .build())
                .task(new Task.Builder()
                        .taskInstanceId("1233")
                        .assigneeId("testuser-4")
                        .startTime(new Date())
                        .endTime(new Date(1001))
                        .build())
                .build();

        String actual = ProcessInstanceUtility.completedBy(instance);
        Assert.assertEquals(expected, actual);
    }

}
