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
import org.springframework.data.querydsl.QueryDslUtils;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.common.RequestDetails;
import piecework.engine.ProcessEngineRuntimeFacade;
import piecework.engine.exception.ProcessEngineException;
import piecework.exception.*;
import piecework.form.handler.RequestHandler;
import piecework.form.handler.ResponseHandler;
import piecework.form.handler.SubmissionHandler;
import piecework.form.validation.FormValidation;
import piecework.form.validation.ValidationService;
import piecework.model.*;
import piecework.model.Process;
import piecework.security.Sanitizer;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.util.ManyMap;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author James Renfro
 */
@Service
public class ProcessInstanceService {

    private static final Logger LOG = Logger.getLogger(ProcessInstanceService.class);

    @Autowired
    ProcessEngineRuntimeFacade facade;

    @Autowired
    ProcessInstanceRepository processInstanceRepository;

    @Autowired
    SubmissionHandler submissionHandler;

    @Autowired
    ValidationService validationService;

    public ProcessInstance findOne(Process process, String processInstanceId) throws NotFoundError {
        ProcessInstance instance = processInstanceRepository.findOne(processInstanceId);

        if (instance == null || !instance.getProcessDefinitionKey().equals(process.getProcessDefinitionKey()))
            throw new NotFoundError(Constants.ExceptionCodes.instance_does_not_exist);

        return instance;
    }

    public Set<ProcessInstance> findByCriteria(List<Process> processes, ManyMap<String, String> criteria) {
        if (criteria.containsKey("keyword")) {
            String keyword = criteria.getOne("keyword");
            return processInstanceRepository.findByKeywordsRegex(keyword);
        }
        return Collections.emptySet();
    }

    public ProcessInstance store(Process process, FormSubmission submission, FormValidation validation, ProcessInstance previous) throws StatusCodeError {
        ProcessInstance.Builder instanceBuilder;

        if (previous != null) {
            instanceBuilder = new ProcessInstance.Builder(previous, new PassthroughSanitizer());
        } else {
            try {
                String processInstanceId = UUID.randomUUID().toString();
                String engineInstanceId = facade.start(process, processInstanceId, validation.getFormValueMap());

                instanceBuilder = new ProcessInstance.Builder()
                        .processDefinitionKey(process.getProcessDefinitionKey())
                        .processDefinitionLabel(process.getProcessDefinitionLabel())
                        .processInstanceId(processInstanceId)
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

    public ProcessInstance submit(Process process, Screen screen, ProcessInstancePayload payload) throws StatusCodeError {

        // Validate the submission
        FormValidation validation = validate(process, screen, payload);
        FormSubmission submission = validation.getSubmission();
        ProcessInstance previous = validation.getInstance();

        return store(process, submission, validation, previous);
    }

    public FormValidation validate(Process process, Screen screen, ProcessInstancePayload payload) throws StatusCodeError {

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

        return validation;
    }

}
