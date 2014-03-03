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

import piecework.enumeration.ActionType;
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.model.Activity;
import piecework.model.Field;
import piecework.model.FormRequest;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.submission.SubmissionHandlerRegistry;
import piecework.submission.SubmissionTemplate;
import piecework.submission.SubmissionTemplateFactory;
import piecework.validation.Validation;
import piecework.validation.ValidationFactory;

import java.util.Map;

/**
 * @author James Renfro
 */
public class FieldValidationCommand<P extends ProcessDeploymentProvider> extends AbstractValidationCommand<P> {

    private final String fieldName;

    FieldValidationCommand(CommandExecutor commandExecutor, P modelProvider, FormRequest request, Object object, Class<?> type, String fieldName, String version) {
        super(commandExecutor, modelProvider, request, ActionType.SAVE, type, object, null, version, false);
        this.fieldName = fieldName;
    }

    @Override
    Validation execute(SubmissionHandlerRegistry submissionHandlerRegistry, SubmissionTemplateFactory submissionTemplateFactory, ValidationFactory validationFactory) throws PieceworkException {
        Activity activity = modelProvider.activity();
        Map<String, Field> fieldMap = activity.getFieldKeyMap();
        Field field = fieldMap.get(fieldName);

        if (field == null && !activity.isAllowAny())
            throw new NotFoundError();

        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(modelProvider, field, request, activity.isAllowAny());
        return validation(submissionHandlerRegistry, validationFactory, template);
    }
}
