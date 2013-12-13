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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import piecework.ServiceLocator;
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
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

    private static final Logger LOG = Logger.getLogger(ValidationCommand.class);
    private final ProcessDeployment deployment;
    private final Task task;
    private final FormRequest request;
    private final Object object;
    private final Class<?> type;
    private final String fieldName;

    ValidationCommand(CommandExecutor commandExecutor, Process process, ProcessDeployment deployment, FormRequest request, Object object, Class<?> type, Entity principal, String fieldName) {
        super(commandExecutor, principal, process, request.getInstance());
        this.deployment = deployment;
        this.task = request.getTask();
        this.request = request;
        this.object = object;
        this.type = type;
        this.fieldName = fieldName;
    }

    @Override
    Validation execute(ServiceLocator serviceLocator) throws PieceworkException {
        SubmissionHandlerRegistry submissionHandlerRegistry = serviceLocator.getService(SubmissionHandlerRegistry.class);
        SubmissionTemplateFactory submissionTemplateFactory = serviceLocator.getService(SubmissionTemplateFactory.class);
        ValidationFactory validationFactory = serviceLocator.getService(ValidationFactory.class);

        return execute(submissionHandlerRegistry, submissionTemplateFactory, validationFactory);
    }

    Validation execute(SubmissionHandlerRegistry submissionHandlerRegistry, SubmissionTemplateFactory submissionTemplateFactory, ValidationFactory validationFactory) throws PieceworkException {
        SubmissionHandler handler = submissionHandlerRegistry.handler(type);
        SubmissionTemplate template;

        if (StringUtils.isEmpty(fieldName)) {
            template = submissionTemplateFactory.submissionTemplate(process, deployment, request);
        } else {
            Activity activity = request.getActivity();
            Map<String, Field> fieldMap = activity.getFieldKeyMap();

            Field field = fieldMap.get(fieldName);
            if (field == null)
                throw new NotFoundError();

            template = submissionTemplateFactory.submissionTemplate(process, field, request);
        }

        Submission submission = handler.handle(object, template, principal);

        return validationFactory.validation(process, instance, task, template, submission, true);
    }

}
