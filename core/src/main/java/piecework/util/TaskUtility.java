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

import piecework.authorization.AuthorizationRole;
import piecework.common.ViewContext;
import piecework.exception.StatusCodeError;
import piecework.model.Process;
import piecework.model.*;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.service.IdentityService;
import piecework.task.TaskFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author James Renfro
 */
public class TaskUtility {

    public static Task findTask(IdentityService identityService, Process process, ProcessInstance instance, String taskId, Entity principal, boolean limitToActive, ViewContext viewContext) throws StatusCodeError {

        Set<Task> tasks = instance.getTasks();
        boolean hasOversight = principal.hasRole(process, AuthorizationRole.OVERSEER);

        if (tasks == null)
            return null;

        for (Task task : tasks) {
            if (limitToActive && !task.isActive())
                continue;

            if (taskId != null && !task.getTaskInstanceId().equals(taskId))
                continue;

            if (hasOversight || task.isCandidateOrAssignee(principal) ) {
                Map<String, User> userMap = identityService != null ? identityService.findUsers(task.getAssigneeAndCandidateAssigneeIds()) : Collections.<String, User>emptyMap();
                return TaskFactory.task(task, new PassthroughSanitizer(), userMap, viewContext);
            }
        }

        return null;
    }

}
