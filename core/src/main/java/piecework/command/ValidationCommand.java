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
import piecework.exception.*;
import piecework.model.*;
import piecework.persistence.ProcessDeploymentProvider;
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
public class ValidationCommand<P extends ProcessDeploymentProvider> extends AbstractValidationCommand<P> {

    ValidationCommand(CommandExecutor commandExecutor, P modelProvider, FormRequest request, ActionType actionType, Object object, Class<?> type, String version) {
        super(commandExecutor, modelProvider, request, actionType, type, object, null, version, false);
    }

    Validation execute(SubmissionHandlerRegistry submissionHandlerRegistry, SubmissionTemplateFactory submissionTemplateFactory, ValidationFactory validationFactory) throws PieceworkException {
        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(modelProvider, request, actionType, null);
        return validation(submissionHandlerRegistry, validationFactory, template);
    }

}
