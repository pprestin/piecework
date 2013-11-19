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

import piecework.enumeration.ActionType;
import piecework.enumeration.ActivityUsageType;
import piecework.model.Action;
import piecework.model.Activity;
import piecework.model.Container;

/**
 * @author James Renfro
 */
public class ActivityUtil {

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
