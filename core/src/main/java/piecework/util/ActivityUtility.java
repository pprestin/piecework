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

import org.apache.commons.lang.StringUtils;
import piecework.enumeration.ActionType;
import piecework.enumeration.ActivityUsageType;
import piecework.exception.MisconfiguredProcessException;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
public class ActivityUtility {

    public static Activity activity(Process process, ProcessDeployment deployment, ProcessInstance instance, Task task) throws PieceworkException {
        if (process == null)
            throw new MisconfiguredProcessException("No process found");

        Activity activity = null;
        if (process.isAllowPerInstanceActivities() && task != null && StringUtils.isNotEmpty(task.getTaskDefinitionKey()) && instance != null) {
            Map<String, Activity> activityMap = instance.getActivityMap();
            if (activityMap != null)
                activity = activityMap.get(task.getTaskDefinitionKey());

            if (activity != null)
                return activity;
        }

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

    public static List<String> fieldIds(Container container, Container parentContainer) {
        List<String> fieldIds = new ArrayList<String>();

        // These fieldIds ultimately determine which fields will be validated
        int reviewChildIndex = parentContainer.getReviewChildIndex();
        if (reviewChildIndex > -1 && reviewChildIndex == container.getOrdinal()) {
            // If we're at a review step then we need to gather the fields of all
            // previous containers owned by the parent
            List<Container> children = parentContainer.getChildren();
            for (Container child : children) {
                if (child.getOrdinal() <= reviewChildIndex)
                    ActivityUtility.gatherFieldIds(child, fieldIds);
            }
        } else {
            // Otherwise we only need to gather the fieldIds from the container that is being validated
            ActivityUtility.gatherFieldIds(container, fieldIds);
        }
        return fieldIds;
    }

    public static void gatherFieldIds(Container container, List<String> allFieldIds) {
        List<String> fieldIds = container.getFieldIds();
        if (fieldIds != null) {
            allFieldIds.addAll(fieldIds);
        }

        if (container.getChildren() != null && !container.getChildren().isEmpty()) {
            for (Container child : container.getChildren()) {
                gatherFieldIds(child, allFieldIds);
            }
        }
    }

}
