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
import piecework.exception.*;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.persistence.ProcessInstanceProvider;
import piecework.service.TaskService;
import piecework.util.ModelUtility;
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
public class ValidationCommand<P extends ProcessDeploymentProvider> extends AbstractCommand<Validation, P> {

    private static final Set<ActionType> UNEXCEPTIONAL_ACTION_TYPES = Sets.newHashSet(ActionType.ASSIGN, ActionType.CLAIM, ActionType.SAVE, ActionType.REJECT);

    private static final Logger LOG = Logger.getLogger(ValidationCommand.class);
    private final Submission submission;
    private final FormRequest request;
    private final Object object;
    private final Class<?> type;
    private final String validationId;
    private final String version;
    private final boolean ignoreThrowException;

    ValidationCommand(CommandExecutor commandExecutor, P modelProvider, FormRequest request, Submission submission, String version) {
        super(commandExecutor, modelProvider);
        this.request = request;
        this.object = null;
        this.type = null;
        this.submission = submission;
        this.validationId = null;
        this.version = version;
        this.ignoreThrowException = true;
    }

    ValidationCommand(CommandExecutor commandExecutor, P modelProvider, FormRequest request, Object object, Class<?> type, String validationId, String version) {
        super(commandExecutor, modelProvider);
        this.request = request;
        this.object = object;
        this.type = type;
        this.submission = null;
        this.validationId = validationId;
        this.version = version;
        this.ignoreThrowException = true;
    }

    @Override
    Validation execute(ServiceLocator serviceLocator) throws PieceworkException {
        SubmissionHandlerRegistry submissionHandlerRegistry = serviceLocator.getService(SubmissionHandlerRegistry.class);
        SubmissionTemplateFactory submissionTemplateFactory = serviceLocator.getService(SubmissionTemplateFactory.class);
        ValidationFactory validationFactory = serviceLocator.getService(ValidationFactory.class);

        return execute(submissionHandlerRegistry, submissionTemplateFactory, validationFactory);
    }

    Validation execute(SubmissionHandlerRegistry submissionHandlerRegistry, SubmissionTemplateFactory submissionTemplateFactory, ValidationFactory validationFactory) throws PieceworkException {
        SubmissionHandler handler = type != null ? submissionHandlerRegistry.handler(type) : null;
        SubmissionTemplate template;

        Task task = ModelUtility.task(modelProvider);
        if (task != null) {
            if (!task.isActive())
                throw new BadRequestError(Constants.ExceptionCodes.active_task_required);
            if (!task.isAssignee(modelProvider.principal()))
                throw new ForbiddenError(Constants.ExceptionCodes.active_task_required);
        }

        template = submissionTemplateFactory.submissionTemplate(modelProvider, request, validationId);

        Submission submission = this.submission == null ? handler.handle(modelProvider, object, template) : this.submission;
        boolean throwException = !ignoreThrowException && submission.getAction() != null && !UNEXCEPTIONAL_ACTION_TYPES.contains(submission.getAction());

        return validationFactory.validation(modelProvider, template, submission, version, throwException);
    }

}
