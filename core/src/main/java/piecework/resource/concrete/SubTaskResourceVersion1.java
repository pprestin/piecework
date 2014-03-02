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
package piecework.resource.concrete;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.Versions;
import piecework.command.CommandFactory;
import piecework.command.SubmissionValidationCommand;
import piecework.command.ValidationCommand;
import piecework.persistence.ModelProviderFactory;
import piecework.persistence.TaskProvider;
import piecework.resource.SubTaskResource;
import piecework.service.*;
import piecework.enumeration.ActionType;
import piecework.exception.*;
import piecework.identity.IdentityHelper;
import piecework.settings.SecuritySettings;
import piecework.model.*;
import piecework.submission.SubmissionHandler;
import piecework.submission.SubmissionHandlerRegistry;
import piecework.submission.SubmissionTemplate;
import piecework.submission.SubmissionTemplateFactory;
import piecework.validation.Validation;
import piecework.validation.ValidationFactory;

import javax.ws.rs.core.Response;

@Service
public class SubTaskResourceVersion1 implements SubTaskResource {

    private static final Logger LOG = Logger.getLogger(SubTaskResourceVersion1.class);
    protected static final String VERSION = "v1";


    @Autowired
    CommandFactory commandFactory;

    @Autowired
    IdentityHelper helper;

    @Autowired
    ModelProviderFactory modelProviderFactory;

    @Autowired
    RequestService requestService;

    @Autowired
    SecuritySettings securitySettings;

    @Autowired
    SubmissionHandlerRegistry submissionHandlerRegistry;

    @Autowired
    SubmissionTemplateFactory submissionTemplateFactory;

    @Autowired
    ValidationFactory validationFactory;

    @Autowired
    Versions versions;

    @Override
    public Response create(MessageContext context, String rawProcessDefinitionKey, String rawTaskId, Submission rawSubmission) throws StatusCodeError {
        TaskProvider taskProvider = modelProviderFactory.taskProvider(rawProcessDefinitionKey, rawTaskId, helper.getPrincipal());
        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();

        SubmissionHandler handler = submissionHandlerRegistry.handler(Submission.class);
        ActionType actionType = ActionType.SUBCREATE;
        try{
            FormRequest formRequest = requestService.create(requestDetails, taskProvider, actionType);
            SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(taskProvider, formRequest, actionType);
            Submission submission = handler.handle(taskProvider, rawSubmission, template);
            SubmissionValidationCommand<TaskProvider> validationCommand = commandFactory.submissionValidation(taskProvider, formRequest, actionType, submission, VERSION);
            Validation validation = validationCommand.execute();

            commandFactory.createSubTask(taskProvider, validation).execute();

            return Response.noContent().build();
        } catch (Exception mpe) {
            LOG.error("Unable to create subtask", mpe);
            throw new InternalServerError(Constants.ExceptionCodes.subtask_create_invalid);
        }
    }

    @Override
    public String getVersion() {
        return versions.getVersion1().getVersion();
    }

}
