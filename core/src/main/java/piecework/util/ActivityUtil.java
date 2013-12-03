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

import piecework.Constants;
import piecework.enumeration.ActionType;
import piecework.enumeration.ActivityUsageType;
import piecework.exception.InternalServerError;
import piecework.exception.MisconfiguredProcessException;
import piecework.exception.StatusCodeError;
import piecework.model.*;

import java.util.Map;

/**
 * @author James Renfro
 */
public class ActivityUtil {

    public static Activity activity(piecework.model.Process process, ProcessInstance instance, Task task) throws MisconfiguredProcessException {
        Activity activity = null;
        if (process.isAllowPerInstanceActivities() && task != null && task.getTaskDefinitionKey() != null && instance != null) {
            Map<String, Activity> activityMap = instance.getActivityMap();
            if (activityMap != null)
                activity = activityMap.get(task.getTaskDefinitionKey());

            if (activity != null)
                return activity;
        }

        ProcessDeployment deployment = process.getDeployment();
        if (deployment == null)
            throw new MisconfiguredProcessException("No deployment found");

        String activityKey = deployment.getStartActivityKey();
        if (task != null)
            activityKey = task.getTaskDefinitionKey();

        if (activityKey != null)
            activity = deployment.getActivity(activityKey);

        if (activity != null)
            return activity;

        throw new MisconfiguredProcessException("Unable to build activity for process");
    }

    public static Container parent(Activity activity, ActionType actionType) {
        Action action = activity.action(actionType != null ? actionType : ActionType.CREATE);
        if (action == null)
            return null;

        Container container = action.getContainer();
        return container;
    }

    public static Container child(Activity activity, ActionType actionType, Container parent) {

        if (parent == null)
            parent = parent(activity, actionType);

        if (activity.getUsageType() == ActivityUsageType.MULTI_PAGE) {
            int activeChildIndex = parent.getActiveChildIndex();

            if (activeChildIndex != -1 && parent.getChildren() != null && parent.getChildren().size() >= activeChildIndex) {
                return parent.getChildren().get(activeChildIndex - 1);
            }
        }

        return parent;
    }

}
