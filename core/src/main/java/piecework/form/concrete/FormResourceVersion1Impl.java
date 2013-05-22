/*
 * Copyright 2012 University of Washington
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
package piecework.form.concrete;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import piecework.Constants;
import piecework.Sanitizer;
import piecework.common.view.ViewContext;
import piecework.engine.ProcessEngineRuntimeFacade;
import piecework.exception.*;
import piecework.form.*;
import piecework.form.validation.FormValidation;
import piecework.form.validation.ValidationService;
import piecework.model.*;
import piecework.model.Process;
import piecework.process.*;
import piecework.security.PassthroughSanitizer;

/**
 * @author James Renfro
 */
@Service
public class FormResourceVersion1Impl implements FormResource {

    private static final Logger LOG = Logger.getLogger(FormResourceVersion1Impl.class);

    @Autowired
    ProcessEngineRuntimeFacade facade;

    @Autowired
    ProcessRepository processRepository;

    @Autowired
    ProcessInstanceRepository processInstanceRepository;

    @Autowired
    RequestHandler requestHandler;

    @Autowired
    ResponseHandler responseHandler;

    @Autowired
    SubmissionHandler submissionHandler;
	
	@Autowired
	ProcessEngineRuntimeFacade runtime;
	
	@Autowired
    Sanitizer sanitizer;

    @Autowired
    ValidationService validationService;

    @Value("${base.application.uri}")
    String baseApplicationUri;

    @Value("${base.service.uri}")
    String baseServiceUri;

	public Response read(final String rawProcessDefinitionKey, HttpServletRequest request) throws StatusCodeError {
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);

        Process process = processRepository.findOne(processDefinitionKey);

        if (process == null)
            throw new NotFoundError(Constants.ExceptionCodes.process_does_not_exist);

        List<Interaction> interactions = process.getInteractions();

        if (interactions == null || interactions.isEmpty())
            throw new InternalServerError();

        // Pick the first interaction and the first screen
        Interaction interaction = interactions.iterator().next();

        FormRequest formRequest = requestHandler.create(request, processDefinitionKey, null, interaction, null);

        return responseHandler.handle(formRequest);
	}

    @Override
    public Response read(String rawProcessDefinitionKey, String rawTaskId, HttpServletRequest request) throws StatusCodeError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        String taskId = sanitizer.sanitize(rawTaskId);

        Process process = processRepository.findOne(processDefinitionKey);

        if (process == null)
            throw new NotFoundError(Constants.ExceptionCodes.process_does_not_exist);

        Task task = facade.findTask(process.getEngine(), process.getEngineProcessDefinitionKey(), taskId);


        return null;
    }

    @Override
    public Response submit(@PathParam("processDefinitionKey") String rawProcessDefinitionKey, String rawRequestId, HttpServletRequest request, MultipartBody body) throws StatusCodeError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        String requestId = sanitizer.sanitize(rawRequestId);

        Process process = processRepository.findOne(processDefinitionKey);

        if (process == null)
            throw new ForbiddenError(Constants.ExceptionCodes.process_does_not_exist);

        FormRequest formRequest = requestHandler.handle(request, requestId);

        FormSubmission submission = submissionHandler.handle(formRequest, body);

        FormValidation validation = validationService.validate(submission, null, formRequest.getScreen(), null);

        List<ValidationResult> results = validation.getResults();
        if (results != null && !results.isEmpty()) {
            throw new BadRequestError(new ValidationResultList(results));
        }

        ProcessInstance previous = formRequest.getProcessInstanceId() != null ? processInstanceRepository.findOne(formRequest.getProcessInstanceId()) : null;

        ProcessInstance.Builder instanceBuilder;

        if (previous != null) {
            instanceBuilder = new ProcessInstance.Builder(previous, new PassthroughSanitizer());



        } else {
            String engineInstanceId = facade.start(process, null, validation.getFormValueMap());

            instanceBuilder = new ProcessInstance.Builder()
                    .processDefinitionKey(process.getProcessDefinitionKey())
                    .processDefinitionLabel(process.getProcessDefinitionLabel())
                    .processInstanceLabel(validation.getTitle())
                    .engineProcessInstanceId(engineInstanceId);
        }

        instanceBuilder.formValueMap(validation.getFormValueMap())
                .restrictedValueMap(validation.getRestrictedValueMap())
                .submission(submission);


        ProcessInstance stored = processInstanceRepository.save(instanceBuilder.build());

        List<Interaction> interactions = process.getInteractions();

        // Pick the first interaction
        Interaction interaction = interactions.iterator().next();

        // Pick the next screen

        List<Screen> screens = interaction.getScreens();

        if (screens == null || screens.isEmpty())
            throw new InternalServerError();

        FormRequest nextFormRequest = null;

        if (!formRequest.getSubmissionType().equals(Constants.SubmissionTypes.FINAL))
            nextFormRequest = requestHandler.create(request, processDefinitionKey, stored.getProcessInstanceId(), interaction, formRequest.getScreen());

        // If the request handler doesn't have another request to process, then
        // provide the generic thank you page back to the user
        if (nextFormRequest == null) {

            Response.ok();
        }

        return responseHandler.handle(nextFormRequest);
    }

    @Override
    public Response validate(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("requestId") String requestId, @PathParam("validationId") String validationId, @Context HttpServletRequest request, MultipartBody body) throws StatusCodeError {
        return null;
    }

    /*
	@Override
	public FormView read(final String rawProcessDefinitionKey, final String rawProcessInstanceId)
			throws StatusCodeError {
		// Sanitize all user input
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
		String processInstanceId = sanitizer.sanitize(rawProcessInstanceId);
				
//		try {
//			Form form = service.getForm(processDefinitionKey, FormPosition.START_RESPONSE, null);
//
//			if (form == null)
//				throw new NotFoundError();
//
//			FormView.Builder builder = new FormView.Builder(form);
//			builder.processInstanceId(processInstanceId);
//			return builder.build(getViewContext());
//		} catch (RecordNotFoundException e) {
//			throw new BadRequestError();
//		} catch (RecordDeletedException e) {
//			throw new BadRequestError();
//		}
        return null;
	}

	@Override
	public Response submit(final String rawProcessDefinitionKey, final MultivaluedMap<String, String> rawFormData) throws StatusCodeError {
		return this.submit(rawProcessDefinitionKey, null, rawFormData);
	}
	
	@Override
	public Response submit(final String rawProcessDefinitionKey, final String rawProcessBusinessKey, final MultivaluedMap<String, String> rawFormData) throws StatusCodeError {
		// Sanitize all user input
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
		String processBusinessKey = sanitizer.sanitize(rawProcessBusinessKey);
		Map<String, List<String>> formData = sanitizer.sanitize(rawFormData);
	
		String submissionId = getSubmissionId(formData);
		
		// Ensure that we always set a process business key
		if (processBusinessKey == null)
			processBusinessKey = UUID.randomUUID().toString();

		// FIXME: Add in description for why this error is returned
		if (processBusinessKey.length() > 140)
			throw new BadRequestError(Constants.ExceptionCodes.process_business_key_limit);

		// FIXME: Add logic to handle attachments in the case where the previous validation failed
//		if (submissionId != null)
//			storeAttachments(processDefinitionKey, processBusinessKey, submissionId);




//		PropertyValueReader reader = new PropertyValueReader(formData);
//
//		try {
//			try {
//				// Use sanitized form data to validate that all constraints are met before creating a new
//				service.validate(processDefinitionKey, null, null, reader);
//				service.storeValues(processDefinitionKey, null, processBusinessKey, null, reader, true);
//
//				ProcessInstance instance = runtime.start(null, processDefinitionKey, processBusinessKey, formData);
//
//				String processInstanceId = instance.getProcessInstanceId();
//
//			} catch (ValidationException validationException) {
//				List<AttributeValidation> validations = validationException.getValidations();
//
//				Form form = service.getForm(processDefinitionKey, FormPosition.START_REQUEST, null, validations);
//
//				if (form == null)
//					throw new NotFoundError();
//
//				FormView.Builder builder = new FormView.Builder(form);
//				builder.submissionId(submissionId);
//
//				return builder.build(getViewContext());
//			}
//		} catch (RecordNotFoundException e) {
//			throw new ForbiddenError();
//		} catch (RecordDeletedException e) {
//			throw new BadRequestError();
//		}
		
		return null;
	}  */
	
	@Override
	public ViewContext getViewContext() {
		return new ViewContext(baseApplicationUri, baseServiceUri, null, "form", "Form");
	}
	
	private void storeAttachments(String processDefinitionKey, String processInstanceId, String submissionId) throws StatusCodeError {
//		List<AttachmentReference> attachments = listAttachments(processDefinitionKey, submissionId);
//		if (attachments != null && !attachments.isEmpty()) {
//			for (AttachmentReference attachment : attachments) {
//				String label = attachment.getLabel();
//				String description = attachment.getDescription();
//				String contentType = attachment.getContentType();				
//				String taskId = null;
//				String url = attachment.getLocation();
//				
//				capability.attach().storeAttachment(namespace, processDefinitionKey, processInstanceId, taskId, label, description, contentType, userId, url);
//			}
//		}
	}
	
	private String getSubmissionId(Map<String, List<String>> parameters) {
		List<String> submissionIds = parameters.get("_submissionId");
		if (submissionIds != null && !submissionIds.isEmpty()) {
			String submissionId = submissionIds.get(0);
			return submissionId;
		}
		return null;
	}
}
