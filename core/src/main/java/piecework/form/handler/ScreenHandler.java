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
package piecework.form.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.authorization.AuthorizationRole;
import piecework.enumeration.ActionType;
import piecework.exception.InternalServerError;
import piecework.exception.NotFoundError;
import piecework.exception.StatusCodeError;
import piecework.model.*;
import piecework.model.Process;
import piecework.process.concrete.ResourceHelper;
import piecework.util.ConstraintUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
@Service
public class ScreenHandler {

    @Autowired
    ResourceHelper resourceHelper;

    public Screen firstScreen(Process process) throws StatusCodeError {
//        // Ensure that this user has the right to initiate processes of this type
//        if (!resourceHelper.hasRole(process, AuthorizationRole.INITIATOR))
//            throw new NotFoundError();

        List<Interaction> interactions = process.getInteractions();

        if (interactions == null || interactions.isEmpty())
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);

        return firstScreen(interactions.iterator().next());
    }

    public Screen taskScreen(Process process, Task task) throws StatusCodeError {
        List<Interaction> interactions = process.getInteractions();

        if (interactions == null || interactions.isEmpty())
            throw new InternalServerError();

        Iterator<Interaction> interactionIterator = interactions.iterator();

        if (task != null) {
            String taskDefinitionKey = task.getTaskDefinitionKey();
            while (interactionIterator.hasNext()) {
                Interaction current = interactionIterator.next();
                if (current.getTaskDefinitionKeys() != null && current.getTaskDefinitionKeys().contains(taskDefinitionKey)) {
                    return firstScreen(current);
                }
            }
        }
        throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
    }

    public Screen nextScreen(Interaction interaction, Screen currentScreen, ProcessInstance processInstance, ActionType action) throws StatusCodeError {
        if (interaction == null ||  interaction.getScreens() == null || interaction.getScreens().isEmpty())
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);

        Map<String, FormValue> formValueMap = processInstance != null ? processInstance.getFormValueMap() : null;

        Iterator<Screen> screenIterator = interaction.getScreens().iterator();

        Screen nextScreen = null;
        boolean isFound = false;
        while (screenIterator.hasNext() && nextScreen == null) {
            Screen cursor = screenIterator.next();

            if (currentScreen == null) {
                currentScreen = cursor;
                isFound = true;
            }

            if (isFound) {
                // Once we've reached the current screen then we can start looking for the next screen
                if (satisfiesScreenConstraints(cursor, formValueMap, action))
                    nextScreen = cursor;
            } else if (cursor.getScreenId().equals(currentScreen.getScreenId()))
                isFound = true;
        }

//        if (screenIterator.hasNext())
//            submissionType = Constants.SubmissionTypes.INTERIM;

        return nextScreen;
    }

    private Screen firstScreen(Interaction interaction) throws StatusCodeError {
        if (interaction == null ||  interaction.getScreens() == null || interaction.getScreens().isEmpty())
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);

        return interaction.getScreens().iterator().next();
    }

    private boolean satisfiesScreenConstraints(Screen screen, Map<String, FormValue> formValueMap, ActionType action) {
        Constraint constraint = ConstraintUtil.getConstraint(Constants.ConstraintTypes.SCREEN_IS_DISPLAYED_WHEN_ACTION_TYPE, screen.getConstraints());
        if (constraint != null) {
            ActionType expected = ActionType.valueOf(constraint.getValue());
            return expected == action;
        }
        return true;
    }
}
