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
package piecework.process;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.common.RequestDetails;
import piecework.engine.ProcessEngineRuntimeFacade;
import piecework.engine.exception.ProcessEngineException;
import piecework.exception.BadRequestError;
import piecework.exception.ForbiddenError;
import piecework.exception.InternalServerError;
import piecework.exception.StatusCodeError;
import piecework.form.handler.RequestHandler;
import piecework.form.handler.ResponseHandler;
import piecework.form.handler.SubmissionHandler;
import piecework.form.validation.FormValidation;
import piecework.form.validation.ValidationService;
import piecework.model.*;
import piecework.model.Process;
import piecework.security.Sanitizer;
import piecework.security.concrete.PassthroughSanitizer;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author James Renfro
 */
@Service
public class ProcessInstanceService {

    private static final Logger LOG = Logger.getLogger(ProcessInstanceService.class);

    @Autowired
    ProcessEngineRuntimeFacade facade;

    @Autowired
    ProcessRepository processRepository;

    @Autowired
    ProcessInstanceRepository processInstanceRepository;

    @Autowired
    RequestHandler requestHandler;

    @Autowired
    SubmissionHandler submissionHandler;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    ValidationService validationService;


    public ProcessInstance submit(Process process, Screen screen, ProcessInstancePayload payload) throws StatusCodeError {

        boolean isAttachmentAllowed = screen == null || screen.isAttachmentAllowed();

        // Create a new submission to store the data submitted
        FormSubmission submission = submissionHandler.handle(payload, isAttachmentAllowed);

        // If an instance already exists then get it from the repository
        ProcessInstance previous = null;
        if (StringUtils.isNotBlank(payload.getProcessInstanceId()))
            previous = processInstanceRepository.findOne(payload.getProcessInstanceId());

        // Validate the submission
        FormValidation validation = validationService.validate(submission, previous, screen, payload.getValidationId());

        List<ValidationResult> results = validation.getResults();
        if (results != null && !results.isEmpty()) {
            // Throw an exception if the submitter needs to adjust the data
            throw new BadRequestError(new ValidationResultList(results));
        }

        return store(process, submission, validation, previous);
    }

    public void validate(Process process, Screen screen, ProcessInstancePayload payload) throws StatusCodeError {

        boolean isAttachmentAllowed = screen == null || screen.isAttachmentAllowed();

        // Create a new submission to store the data submitted
        FormSubmission submission = submissionHandler.handle(payload, isAttachmentAllowed);

        // If an instance already exists then get it from the repository
        ProcessInstance previous = null;
        if (StringUtils.isNotBlank(payload.getProcessInstanceId()))
            previous = processInstanceRepository.findOne(payload.getProcessInstanceId());

        // Validate the submission
        FormValidation validation = validationService.validate(submission, previous, screen, payload.getValidationId());

        List<ValidationResult> results = validation.getResults();
        if (results != null && !results.isEmpty()) {
            // Throw an exception if the submitter needs to adjust the data
            throw new BadRequestError(new ValidationResultList(results));
        }
    }


    private ProcessInstance store(Process process, FormSubmission submission, FormValidation validation, ProcessInstance previous) throws StatusCodeError {
        ProcessInstance.Builder instanceBuilder;

        if (previous != null) {
            instanceBuilder = new ProcessInstance.Builder(previous, new PassthroughSanitizer());
        } else {
            try {
                String engineInstanceId = facade.start(process, null, validation.getFormValueMap());

                instanceBuilder = new ProcessInstance.Builder()
                        .processDefinitionKey(process.getProcessDefinitionKey())
                        .processDefinitionLabel(process.getProcessDefinitionLabel())
                        .processInstanceLabel(validation.getTitle())
                        .engineProcessInstanceId(engineInstanceId);

            } catch (ProcessEngineException e) {
                LOG.error("Process engine unable to start instance ", e);
                throw new InternalServerError();
            }
        }

        instanceBuilder.formValueMap(validation.getFormValueMap())
                .restrictedValueMap(validation.getRestrictedValueMap())
                .submission(submission);

        return processInstanceRepository.save(instanceBuilder.build());
    }

//    public FormRequest submit(String processDefinitionKey, String requestId, ProcessInstancePayload payload) throws StatusCodeError {
//
//        piecework.model.Process process = processRepository.findOne(processDefinitionKey);
//
//        if (process == null)
//            throw new ForbiddenError(Constants.ExceptionCodes.process_does_not_exist);
//
//        RequestDetails requestDetails = payload.getRequestDetails();
//
//        FormRequest formRequest = requestHandler.handle(requestDetails, requestId);
//
//        FormSubmission submission = submissionHandler.handle(formRequest, payload);
//
//        ProcessInstance instance = null;
//        if (formRequest.getProcessInstanceId() != null)
//            instance = processInstanceRepository.findOne(formRequest.getProcessInstanceId());
//
//        FormValidation validation = validationService.validate(submission, instance, formRequest.getScreen(), null);
//
//        List<ValidationResult> results = validation.getResults();
//        if (results != null && !results.isEmpty()) {
//            throw new BadRequestError(new ValidationResultList(results));
//        }
//
//        ProcessInstance previous = formRequest.getProcessInstanceId() != null ? processInstanceRepository.findOne(formRequest.getProcessInstanceId()) : null;
//
//        ProcessInstance.Builder instanceBuilder;
//
//        if (previous != null) {
//            instanceBuilder = new ProcessInstance.Builder(previous, new PassthroughSanitizer());
//
//        } else {
//            try {
//                String engineInstanceId = facade.start(process, null, validation.getFormValueMap());
//
//                instanceBuilder = new ProcessInstance.Builder()
//                        .processDefinitionKey(process.getProcessDefinitionKey())
//                        .processDefinitionLabel(process.getProcessDefinitionLabel())
//                        .processInstanceLabel(validation.getTitle())
//                        .engineProcessInstanceId(engineInstanceId);
//
//            } catch (ProcessEngineException e) {
//                LOG.error("Process engine unable to start instance ", e);
//                throw new InternalServerError();
//            }
//        }
//
//        instanceBuilder.formValueMap(validation.getFormValueMap())
//                .restrictedValueMap(validation.getRestrictedValueMap())
//                .submission(submission);
//
//        ProcessInstance stored = processInstanceRepository.save(instanceBuilder.build());
//
//        List<Interaction> interactions = process.getInteractions();
//
//        Interaction interaction = formRequest.getInteraction();
//
//        if (interaction == null)
//            throw new InternalServerError();
//
//        List<Screen> screens = interaction.getScreens();
//
//        if (screens == null || screens.isEmpty())
//            throw new InternalServerError();
//
//        FormRequest nextFormRequest = null;
//
//        if (!formRequest.getSubmissionType().equals(Constants.SubmissionTypes.FINAL))
//            nextFormRequest = requestHandler.create(requestDetails, processDefinitionKey, interaction, formRequest.getScreen(), stored.getProcessInstanceId());
//
//        return nextFormRequest;
//    }



}
