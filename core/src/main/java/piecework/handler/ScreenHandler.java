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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.enumeration.ActionType;
import piecework.exception.ConflictError;
import piecework.exception.InternalServerError;
import piecework.exception.StatusCodeError;
import piecework.model.*;
import piecework.model.Process;
import piecework.identity.IdentityHelper;
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
    IdentityHelper identityHelper;

    public Screen firstScreen(Process process) throws StatusCodeError {
        ProcessDeployment deployment = process.getDeployment();
        if (deployment == null)
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);

        List<Interaction> interactions = deployment.getInteractions();

        if (interactions == null || interactions.isEmpty())
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);

        return firstScreen(interactions.iterator().next());
    }

    public Screen taskScreen(Process process, Task task) throws StatusCodeError {
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
                    return firstScreen(current);
                }
            }
        }
        throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
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
