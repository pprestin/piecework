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
import piecework.exception.PieceworkException;
import piecework.model.FormRequest;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.submission.SubmissionHandlerRegistry;
import piecework.submission.SubmissionTemplate;
import piecework.submission.SubmissionTemplateFactory;
import piecework.validation.Validation;
import piecework.validation.ValidationFactory;

/**
 * @author James Renfro
 */
public class ContainerValidationCommand<P extends ProcessDeploymentProvider> extends AbstractValidationCommand<P> {

    private final String validationId;

    ContainerValidationCommand(CommandExecutor commandExecutor, P modelProvider, FormRequest request, ActionType actionType, Object object, Class<?> type, String validationId, String version) {
        super(commandExecutor, modelProvider, request, actionType, type, object, null, version, false);
        this.validationId = validationId;
    }

    Validation execute(SubmissionHandlerRegistry submissionHandlerRegistry, SubmissionTemplateFactory submissionTemplateFactory, ValidationFactory validationFactory) throws PieceworkException {
        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(modelProvider, request, actionType, validationId);
        return validation(submissionHandlerRegistry, validationFactory, template);
    }

}
