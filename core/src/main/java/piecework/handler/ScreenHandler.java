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
package piecework.handler;

import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.enumeration.ActionType;
import piecework.exception.InternalServerError;
import piecework.model.*;
import piecework.model.Process;
import piecework.util.ConstraintUtil;

import java.util.Iterator;
import java.util.List;

/**
 * @author James Renfro
 */
@Service
public class ScreenHandler {

    public Screen currentScreen(Process process, Task task) throws InternalServerError {
        if (task != null)
            return taskScreen(process, task);
        return startScreen(process);
    }

    public Screen nextScreen(Process process, Task task, ActionType action) throws InternalServerError {
        Interaction interaction = null;

        if (task != null)
            interaction = taskInteraction(process, task);
        else
            interaction = startInteraction(process);

        Screen currentScreen = firstScreen(interaction);

        if (currentScreen == null)
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);

        return nextScreen(process, interaction, currentScreen, action);
    }

    private Screen firstScreen(Interaction interaction) throws InternalServerError {
        if (interaction == null ||  interaction.getScreens() == null || !interaction.getScreens().containsKey(ActionType.CREATE))
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);

        return interaction.getScreens().get(ActionType.CREATE);
    }

    private Screen nextScreen(Process process, Interaction interaction, Screen currentScreen, ActionType action) throws InternalServerError {
        if (interaction != null && interaction.getScreens().isEmpty())
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);

        Screen nextScreen = interaction.getScreens().get(action);

        if (nextScreen == null)
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);

//        Iterator<Screen> screenIterator = interaction.getScreens().iterator();
//
//        boolean isFound = false;
//        while (screenIterator.hasNext() && nextScreen == null) {
//            Screen cursor = screenIterator.next();
//
//            if (currentScreen == null) {
//                currentScreen = cursor;
//                isFound = true;
//            }
//
//            if (isFound) {
//                // Once we've reached the current screen then we can start looking for the next screen
//                if (satisfiesScreenConstraints(cursor, action))
//                    nextScreen = cursor;
//            } else if (cursor.getScreenId().equals(currentScreen.getScreenId()))
//                isFound = true;
//        }

        return nextScreen;
    }

    private boolean satisfiesScreenConstraints(Screen screen, ActionType action) {
        Constraint constraint = ConstraintUtil.getConstraint(Constants.ConstraintTypes.SCREEN_IS_DISPLAYED_WHEN_ACTION_TYPE, screen.getConstraints());
        if (constraint != null) {
            ActionType expected = ActionType.valueOf(constraint.getValue());
            return expected == action;
        }
        return true;
    }

    private Interaction startInteraction(Process process) throws InternalServerError {
        ProcessDeployment deployment = process.getDeployment();
        if (deployment == null)
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);

        List<Interaction> interactions = deployment.getInteractions();

        if (interactions == null || interactions.isEmpty())
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);

        return interactions.iterator().next();
    }

    private Screen startScreen(Process process) throws InternalServerError {
        Interaction interaction = startInteraction(process);
        return firstScreen(interaction);
    }

    private Interaction taskInteraction(Process process, Task task) throws InternalServerError {
        ProcessDeployment deployment = process.getDeployment();
        if (deployment == null)
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);

        List<Interaction> interactions = deployment.getInteractions();

        if (interactions == null || interactions.isEmpty())
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);

        Iterator<Interaction> interactionIterator = interactions.iterator();

        if (task != null) {
            String taskDefinitionKey = task.getTaskDefinitionKey();
            while (interactionIterator.hasNext()) {
                Interaction current = interactionIterator.next();
                if (current.getTaskDefinitionKeys() != null && current.getTaskDefinitionKeys().contains(taskDefinitionKey)) {
                    return current;
                }
            }
        }
        throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
    }

    private Screen taskScreen(Process process, Task task) throws InternalServerError {
        Interaction interaction = taskInteraction(process, task);
        return firstScreen(interaction);
    }
}
