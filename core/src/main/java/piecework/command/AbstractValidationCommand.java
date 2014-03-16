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
import org.apache.log4j.Logger;
import piecework.Constants;
import piecework.ServiceLocator;
import piecework.enumeration.ActionType;
import piecework.exception.BadRequestError;
import piecework.exception.ForbiddenError;
import piecework.exception.InternalServerError;
import piecework.exception.PieceworkException;
import piecework.model.FormRequest;
import piecework.model.Submission;
import piecework.model.Task;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.submission.SubmissionHandler;
import piecework.submission.SubmissionHandlerRegistry;
import piecework.submission.SubmissionTemplate;
import piecework.submission.SubmissionTemplateFactory;
import piecework.util.ModelUtility;
import piecework.validation.Validation;
import piecework.validation.ValidationFactory;

import java.util.Set;

/**
 * @author James Renfro
 */
public abstract class AbstractValidationCommand<P extends ProcessDeploymentProvider> extends AbstractCommand<Validation, P> {

    private static final Logger LOG = Logger.getLogger(AbstractValidationCommand.class);
    private static final Set<ActionType> UNEXCEPTIONAL_ACTION_TYPES = Sets.newHashSet(ActionType.ASSIGN, ActionType.CLAIM, ActionType.SAVE, ActionType.REJECT);

    protected final FormRequest request;
    protected final ActionType actionType;
    private final Class<?> type;
    private final Object object;
    private final Submission submission;
    private final String version;
    private final boolean ignoreThrowException;

    AbstractValidationCommand(CommandExecutor commandExecutor, P modelProvider, FormRequest request, ActionType actionType, Class<?> type, Object object, Submission submission, String version, boolean ignoreThrowException) {
        super(commandExecutor, modelProvider);
        this.request = request;
        this.actionType = actionType;
        this.type = type;
        this.object = object;
        this.submission = submission;
        this.version = version;
        this.ignoreThrowException = ignoreThrowException;
    }

    @Override
    Validation execute(ServiceLocator serviceLocator) throws PieceworkException {
        SubmissionHandlerRegistry submissionHandlerRegistry = serviceLocator.getService(SubmissionHandlerRegistry.class);
        SubmissionTemplateFactory submissionTemplateFactory = serviceLocator.getService(SubmissionTemplateFactory.class);
        ValidationFactory validationFactory = serviceLocator.getService(ValidationFactory.class);

        return execute(submissionHandlerRegistry, submissionTemplateFactory, validationFactory);
    }

    abstract Validation execute(SubmissionHandlerRegistry submissionHandlerRegistry, SubmissionTemplateFactory submissionTemplateFactory, ValidationFactory validationFactory) throws PieceworkException;

    protected Validation validation(SubmissionHandlerRegistry submissionHandlerRegistry, ValidationFactory validationFactory, SubmissionTemplate template) throws PieceworkException {
        if (actionType == null || actionType != ActionType.ATTACH)
            verifyTask();

        SubmissionHandler handler = type != null ? submissionHandlerRegistry.handler(type) : null;
        Submission submission = this.submission == null ? handler.handle(modelProvider, object, template) : this.submission;

        ActionType validatedActionType;
        if (actionType == ActionType.UNDEFINED)
            validatedActionType = submission.getAction();
        else
            validatedActionType = actionType;

        if (validatedActionType == null) {
            LOG.fatal("For some reason there is no action type defined in the validation");
            throw new InternalServerError(Constants.ExceptionCodes.system_misconfigured, "No action type provided");
        }

//        boolean throwException = !ignoreThrowException && !UNEXCEPTIONAL_ACTION_TYPES.contains(validatedActionType);

        return validationFactory.validation(modelProvider, template, submission, version, !UNEXCEPTIONAL_ACTION_TYPES.contains(validatedActionType), ignoreThrowException);
    }

    Task verifyTask() throws PieceworkException {
        Task task = ModelUtility.allowedTask(modelProvider);
        if (task != null) {
            if (!task.isActive())
                throw new BadRequestError(Constants.ExceptionCodes.active_task_required);
            if (!task.isAssignee(modelProvider.principal()))
                throw new ForbiddenError(Constants.ExceptionCodes.active_task_required);
        }
        return task;
    }

}
