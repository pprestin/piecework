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
package piecework.command;

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import piecework.Constants;
import piecework.ServiceLocator;
import piecework.enumeration.ActionType;
import piecework.exception.BadRequestError;
import piecework.exception.ForbiddenError;
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.persistence.ProcessInstanceProvider;
import piecework.service.TaskService;
import piecework.submission.SubmissionHandler;
import piecework.submission.SubmissionHandlerRegistry;
import piecework.submission.SubmissionTemplate;
import piecework.submission.SubmissionTemplateFactory;
import piecework.util.ModelUtility;
import piecework.validation.Validation;
import piecework.validation.ValidationFactory;

import java.util.Map;
import java.util.Set;

/**
 * @author James Renfro
 */
public class FieldValidationCommand<P extends ProcessDeploymentProvider> extends AbstractCommand<Validation, P> {

    private static final Set<ActionType> UNEXCEPTIONAL_ACTION_TYPES = Sets.newHashSet(ActionType.ASSIGN, ActionType.CLAIM, ActionType.SAVE, ActionType.REJECT);

    private static final Logger LOG = Logger.getLogger(ValidationCommand.class);
    private final Submission submission;
    private final FormRequest request;
    private final Object object;
    private final Class<?> type;
    private final String fieldName;
    private final String version;
    private final boolean ignoreThrowException;

    FieldValidationCommand(CommandExecutor commandExecutor, P modelProvider, FormRequest request, Object object, Class<?> type, String fieldName, String version) {
        super(commandExecutor, modelProvider);
        this.request = request;
        this.object = object;
        this.submission = null;
        this.type = type;
        this.fieldName = fieldName;
        this.version = version;
        this.ignoreThrowException = false;
    }

    @Override
    Validation execute(ServiceLocator serviceLocator) throws PieceworkException {
        SubmissionHandlerRegistry submissionHandlerRegistry = serviceLocator.getService(SubmissionHandlerRegistry.class);
        SubmissionTemplateFactory submissionTemplateFactory = serviceLocator.getService(SubmissionTemplateFactory.class);
        TaskService taskService = serviceLocator.getService(TaskService.class);
        ValidationFactory validationFactory = serviceLocator.getService(ValidationFactory.class);

        return execute(submissionHandlerRegistry, submissionTemplateFactory, taskService, validationFactory);
    }

    Validation execute(SubmissionHandlerRegistry submissionHandlerRegistry, SubmissionTemplateFactory submissionTemplateFactory, TaskService taskService, ValidationFactory validationFactory) throws PieceworkException {
        SubmissionHandler handler = type != null ? submissionHandlerRegistry.handler(type) : null;
        SubmissionTemplate template;

        Task task = ModelUtility.allowedTask(modelProvider);
        if (task != null) {
            if (!task.isActive())
                throw new BadRequestError(Constants.ExceptionCodes.active_task_required);
            if (!task.isAssignee(modelProvider.principal()))
                throw new ForbiddenError(Constants.ExceptionCodes.active_task_required);
        }

        Activity activity = request.getActivity();
        Map<String, Field> fieldMap = activity.getFieldKeyMap();

        Field field = fieldMap.get(fieldName);

        if (field == null && !activity.isAllowAny())
            throw new NotFoundError();

        template = submissionTemplateFactory.submissionTemplate(modelProvider, field, request, activity.isAllowAny());

        Submission submission = this.submission == null ? handler.handle(modelProvider, object, template) : this.submission;
        boolean throwException = !ignoreThrowException && submission.getAction() != null && !UNEXCEPTIONAL_ACTION_TYPES.contains(submission.getAction());

        return validationFactory.validation(modelProvider, template, submission, version, throwException);
    }
}
