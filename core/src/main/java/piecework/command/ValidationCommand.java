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
import piecework.ServiceLocator;
import piecework.enumeration.ActionType;
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.service.TaskService;
import piecework.validation.ValidationFactory;
import piecework.submission.SubmissionHandler;
import piecework.submission.SubmissionHandlerRegistry;
import piecework.submission.SubmissionTemplate;
import piecework.submission.SubmissionTemplateFactory;
import piecework.validation.Validation;

import java.util.*;

/**
 * @author James Renfro
 */
public class ValidationCommand extends AbstractCommand<Validation> {

    private static final Set<ActionType> UNEXCEPTIONAL_ACTION_TYPES = Sets.newHashSet(ActionType.ASSIGN, ActionType.CLAIM, ActionType.SAVE, ActionType.REJECT);

    private static final Logger LOG = Logger.getLogger(ValidationCommand.class);
    private final ProcessDeployment deployment;
    private final Submission submission;
    private final Task task;
    private final FormRequest request;
    private final Object object;
    private final Class<?> type;
    private final Entity principal;
    private final String validationId;
    private final String fieldName;
    private final String version;
    private final boolean ignoreThrowException;

    ValidationCommand(CommandExecutor commandExecutor, Process process, ProcessDeployment deployment, FormRequest request, Object object, Class<?> type, Entity principal, String validationId, String fieldName, String version) {
        super(commandExecutor, principal, process, request.getInstance());
        this.deployment = deployment;
        this.task = request.getTask();
        this.request = request;
        this.object = object;
        this.submission = null;
        this.type = type;
        this.principal = principal;
        this.validationId = validationId;
        this.fieldName = fieldName;
        this.version = version;
        this.ignoreThrowException = false;
    }

    ValidationCommand(CommandExecutor commandExecutor, Process process, ProcessDeployment deployment, FormRequest request, Submission submission, Entity principal, String version) {
        super(commandExecutor, principal, process, request.getInstance());
        this.deployment = deployment;
        this.task = request.getTask();
        this.request = request;
        this.object = null;
        this.type = null;
        this.principal = principal;
        this.submission = submission;
        this.validationId = null;
        this.fieldName = null;
        this.version = version;
        this.ignoreThrowException = true;
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

        if (StringUtils.isEmpty(fieldName)) {
            template = submissionTemplateFactory.submissionTemplate(process, deployment, request, validationId);
        } else {
            Activity activity = request.getActivity();
            Map<String, Field> fieldMap = activity.getFieldKeyMap();

            Field field = fieldMap.get(fieldName);

            if (field == null && !activity.isAllowAny())
                throw new NotFoundError();

            template = submissionTemplateFactory.submissionTemplate(process, field, request, activity.isAllowAny());
        }

        Submission submission = this.submission == null ? handler.handle(object, template, principal) : this.submission;
        boolean throwException = !ignoreThrowException && submission.getAction() != null && !UNEXCEPTIONAL_ACTION_TYPES.contains(submission.getAction());

        taskService.checkIsActiveIfTaskExists(process, task);

        return validationFactory.validation(process, instance, task, template, submission, principal, version, throwException);
    }

}
