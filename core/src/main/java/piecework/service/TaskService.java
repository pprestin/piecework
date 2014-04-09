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
package piecework.service;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Command;
import piecework.Constants;
import piecework.command.CommandFactory;
import piecework.command.ValidationCommand;
import piecework.common.ViewContext;
import piecework.enumeration.ActionType;
import piecework.exception.BadRequestError;
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.identity.IdentityHelper;
import piecework.model.*;
import piecework.persistence.ModelProviderFactory;
import piecework.persistence.TaskProvider;
import piecework.repository.ProcessInstanceRepository;
import piecework.security.Sanitizer;
import piecework.security.data.DataFilterService;
import piecework.settings.UserInterfaceSettings;
import piecework.validation.Validation;

/**
 * @author James Renfro
 */
@Service
public class TaskService {

    private static final Logger LOG = Logger.getLogger(TaskService.class);
    private static final String VERSION = "v1";

    @Autowired
    private CommandFactory commandFactory;

    @Autowired
    IdentityHelper helper;

    @Autowired
    IdentityService identityService;

    @Autowired
    ModelProviderFactory modelProviderFactory;

    @Autowired
    ProcessInstanceRepository processInstanceRepository;

    @Autowired
    RequestService requestService;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    UserInterfaceSettings settings;


    public TaskProvider complete(final String rawProcessDefinitionKey, final String rawTaskId, final String rawAction, final Submission rawSubmission, final RequestDetails requestDetails, final Entity principal) throws PieceworkException {
        TaskProvider taskProvider = modelProviderFactory.taskProvider(rawProcessDefinitionKey, rawTaskId, principal);

        String action = sanitizer.sanitize(rawAction);

        ActionType actionType = ActionType.COMPLETE;
        if (StringUtils.isNotEmpty(action)) {
            try {
                actionType = ActionType.valueOf(action.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestError(Constants.ExceptionCodes.task_action_invalid);
            }
        }
        if (rawSubmission != null && rawSubmission.getAction() != null)
            actionType = rawSubmission.getAction();
        String assigneeId = sanitizer.sanitize(rawSubmission.getAssignee());
        String actingUser = null;
        if (principal != null) {
            if (principal.getEntityType() == Entity.EntityType.SYSTEM && StringUtils.isNotEmpty(requestDetails.getActAsUser()))
                actingUser = requestDetails.getActAsUser();
            else
                actingUser = principal.getEntityId();
        }

        ViewContext context = new ViewContext(settings, VERSION);
        Task task = taskProvider.task(context, true);
        if (task == null)
            throw new NotFoundError();

        // [jira wws-154] simply returns for identical assignee
        if ( StringUtils.isEmpty(assigneeId) ) {
            assigneeId = null; // to be consistent with task.getAssigneeId() to facilitate later comparison
        }
        if (actionType == ActionType.ASSIGN && StringUtils.equals(assigneeId, task.getAssigneeId())
             || actionType == ActionType.CLAIM && StringUtils.equals(actingUser, task.getAssigneeId()) ) {
            return taskProvider; // no change in assignee
        }

        FormRequest request;
        Validation validation;

        Command<?> command;
        switch (actionType) {
            case ASSIGN:
                User assignee = assigneeId != null ? identityService.getUserWithAccessAuthority(assigneeId) : null;
                command = commandFactory.assignment(taskProvider, task, assignee);
                break;
            case CLAIM:
                User actingAs = actingUser != null ? identityService.getUserWithAccessAuthority(actingUser) : null;
                command = commandFactory.assignment(taskProvider, task, actingAs);
                break;
            case ATTACH:
                request = requestService.create(requestDetails, taskProvider, actionType);
                ValidationCommand<TaskProvider> validationCommand = commandFactory.validation(taskProvider, request, actionType, rawSubmission, Submission.class, VERSION);
                validation = validationCommand.execute();
                command = commandFactory.attachment(taskProvider, validation);
                break;
            default:
                request = requestService.create(requestDetails, taskProvider, actionType);
                validationCommand = commandFactory.validation(taskProvider, request, actionType, rawSubmission, Submission.class, VERSION);
                validation = validationCommand.execute();
                command = commandFactory.completeTask(taskProvider, validation, actionType);
                break;
        }

        command.execute();
        return taskProvider;
    }

    public Task read(String rawProcessDefinitionKey, String rawTaskId, Entity principal, boolean limitToActive) throws PieceworkException {
        TaskProvider taskProvider = modelProviderFactory.taskProvider(rawProcessDefinitionKey, rawTaskId, principal);
        return taskProvider.task(new ViewContext(settings, VERSION), limitToActive);
    }

    public boolean update(String processInstanceId, Task task) {
        return processInstanceRepository.update(processInstanceId, task);
    }

}
