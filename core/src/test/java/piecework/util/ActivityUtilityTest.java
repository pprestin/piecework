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

import org.junit.Assert;
import org.junit.Test;
import piecework.enumeration.ActionType;
import piecework.enumeration.FlowElementType;
import piecework.exception.MisconfiguredProcessException;
import piecework.model.*;
import piecework.model.Process;

import java.util.HashMap;
import java.util.Map;

/**
 * @author James Renfro
 */
public class ActivityUtilityTest {

    @Test(expected = MisconfiguredProcessException.class)
    public void verifyActivityNullProcess() throws Exception {
        ActivityUtility.activity(null, null, null, null);
    }

    @Test(expected = MisconfiguredProcessException.class)
    public void verifyActivityNullDeployment() throws Exception {
        Process process = new Process.Builder()
                .build();
        ActivityUtility.activity(process, null, null, null);
    }

    @Test(expected = MisconfiguredProcessException.class)
    public void verifyActivityNoActivityKeyOrMap() throws Exception {
        Process process = new Process.Builder()
                .build();
        ProcessDeployment deployment = new ProcessDeployment.Builder()
                .build();
        ActivityUtility.activity(process, deployment, null, null);
    }

    @Test(expected = MisconfiguredProcessException.class)
    public void verifyActivityNoActivityKey() throws Exception {
        Process process = new Process.Builder()
                .build();
        ProcessDeployment deployment = new ProcessDeployment.Builder()
                .activity("start",
                        new Activity.Builder()
                                .action(ActionType.CREATE, new Action())
                                .build())
                .build();
        ActivityUtility.activity(process, deployment, null, null);
    }

    @Test
    public void verifyActivityAllowAny() throws Exception {
        Process process = new Process.Builder()
                .build();
        ProcessDeployment deployment = new ProcessDeployment.Builder()
                .startActivityKey("start")
                .activity("start",
                        new Activity.Builder()
                                .action(ActionType.CREATE, new Action())
                                .allowAny()
                                .build())
                .build();
        Activity activity = ActivityUtility.activity(process, deployment, null, null);
        Assert.assertTrue(activity.isAllowAny());
    }

    @Test
    public void verifyActivityByTaskDefinitionKey() throws Exception {
        Process process = new Process.Builder()
                .build();
        ProcessDeployment deployment = new ProcessDeployment.Builder()
                .startActivityKey("start")
                .activity("start",
                        new Activity.Builder()
                                .action(ActionType.CREATE, new Action())
                                .allowAny()
                                .build())
                .activity("review",
                        new Activity.Builder()
                                .action(ActionType.CREATE, new Action())
                                .build())
                .build();
        Task task = new Task.Builder()
                .taskDefinitionKey("review")
                .build();
        Activity activity = ActivityUtility.activity(process, deployment, null, task);
        Assert.assertFalse(activity.isAllowAny());
    }

    @Test
    public void verifyActivityByTaskDefinitionKeyOffInstanceMap() throws Exception {
        Process process = new Process.Builder()
                .allowPerInstanceActivities()
                .build();
        ProcessDeployment deployment = new ProcessDeployment.Builder()
                .startActivityKey("start")
                .activity("start",
                        new Activity.Builder()
                                .elementType(FlowElementType.START_EVENT)
                                .action(ActionType.CREATE, new Action())
                                .allowAny()
                                .build())
                .activity("review",
                        new Activity.Builder()
                                .elementType(FlowElementType.USER_TASK)
                                .action(ActionType.CREATE, new Action())
                                .build())
                .build();
        Map<String, Activity> activityMap = new HashMap<String, Activity>();
        activityMap.put("review", new Activity.Builder()
                .elementType(FlowElementType.SERVICE_TASK)
                .action(ActionType.CREATE, new Action())
                .build());
        ProcessInstance instance = new ProcessInstance.Builder()
                .activityMap(activityMap)
                .build();
        Task task = new Task.Builder()
                .taskDefinitionKey("review")
                .build();
        Activity activity = ActivityUtility.activity(process, deployment, instance, task);
        Assert.assertEquals(FlowElementType.SERVICE_TASK, activity.getElementType());
    }

    @Test
    public void verifyActivityByTaskDefinitionKeyNotOffInstanceMap() throws Exception {
        // This process does not allow per instance activities
        Process process = new Process.Builder()
                .build();
        ProcessDeployment deployment = new ProcessDeployment.Builder()
                .startActivityKey("start")
                .activity("start",
                        new Activity.Builder()
                                .elementType(FlowElementType.START_EVENT)
                                .action(ActionType.CREATE, new Action())
                                .allowAny()
                                .build())
                .activity("review",
                        new Activity.Builder()
                                .elementType(FlowElementType.USER_TASK)
                                .action(ActionType.CREATE, new Action())
                                .build())
                .build();
        Map<String, Activity> activityMap = new HashMap<String, Activity>();
        activityMap.put("review", new Activity.Builder()
                .elementType(FlowElementType.SERVICE_TASK)
                .action(ActionType.CREATE, new Action())
                .build());
        ProcessInstance instance = new ProcessInstance.Builder()
                .activityMap(activityMap)
                .build();
        Task task = new Task.Builder()
                .taskDefinitionKey("review")
                .build();
        Activity activity = ActivityUtility.activity(process, deployment, instance, task);
        Assert.assertEquals(FlowElementType.USER_TASK, activity.getElementType());
    }

}
