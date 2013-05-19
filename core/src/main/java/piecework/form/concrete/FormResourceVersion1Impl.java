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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import piecework.Constants;
import piecework.common.view.ViewContext;
import piecework.engine.ProcessEngineRuntimeFacade;
import piecework.exception.NotFoundError;
import piecework.exception.StatusCodeError;
import piecework.form.FormResource;
import piecework.form.PageRepository;
import piecework.form.model.view.FormView;
import piecework.model.*;
import piecework.model.Process;
import piecework.model.FormSubmission;
import piecework.process.ProcessInstanceRepository;
import piecework.process.ProcessRepository;
import piecework.process.RequestRepository;
import piecework.process.SubmissionRepository;
import piecework.security.UserInputSanitizer;

/**
 * @author James Renfro
 */
@Service
public class FormResourceVersion1Impl implements FormResource {

    @Autowired
    ProcessRepository processRepository;

    @Autowired
    ProcessInstanceRepository processInstanceRepository;

    @Autowired
    PageRepository pageRepository;

    @Autowired
    RequestRepository requestRepository;

    @Autowired
    SubmissionRepository submissionRepository;
	
	@Autowired
	ProcessEngineRuntimeFacade runtime;
	
	@Autowired
	UserInputSanitizer sanitizer;

    @Value("${base.application.uri}")
    String baseApplicationUri;

    @Value("${base.service.uri}")
    String baseServiceUri;

	public Response read(final String rawProcessDefinitionKey, HttpServletRequest request) throws StatusCodeError {
		// Sanitize all user input
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);

        Process process = processRepository.findOne(processDefinitionKey);

        if (process == null)
            throw new NotFoundError();

        List<Interaction> interactions = process.getInteractions();

        if (interactions == null || interactions.isEmpty())
            throw new NotFoundError();

        // Pick the first interaction and the first screen
        Interaction interaction = interactions.iterator().next();

        List<Screen> screens = interaction.getScreens();

        if (screens == null || screens.isEmpty())
            throw new NotFoundError();

        Screen screen = screens.iterator().next();
        String location = screen.getLocation();

        // Generate a new uuid for this request
        String requestId = UUID.randomUUID().toString();

        FormRequest formRequest = new FormRequest.Builder()
                .requestId(requestId)
                .remoteAddress(request.getRemoteAddr())
                .remoteHost(request.getRemoteHost())
                .remotePort(request.getRemotePort())
                .remoteUser(request.getRemoteUser())
                .build();

        formRequest = requestRepository.save(formRequest);

        Form form = new Form.Builder()
                .formInstanceId(UUID.randomUUID().toString())
                .processDefinitionKey(processDefinitionKey)
                .submissionType(Constants.SubmissionTypes.START)
                .requestId(formRequest.getRequestId())
                .screen(screen)
                .build();

        if (StringUtils.isNotEmpty(location)) {
            // If the location is not blank then delegate to the
            return pageRepository.getPageResponse(form, location);
        }

        return Response.ok(form).build();

//		try {
//			Form form = service.getForm(processDefinitionKey, FormPosition.START_REQUEST, null);
//
//			if (form == null)
//				throw new NotFoundError();
//
//			return new FormView.Builder(form).build(getViewContext());
//		} catch (RecordNotFoundException e) {
//			throw new BadRequestError();
//		} catch (RecordDeletedException e) {
//			throw new BadRequestError();
//		}
		
//		SecurityContext context = SecurityContextHolder.getContext();
//		Collection<? extends GrantedAuthority> authorities = context.getAuthentication().getAuthorities();
//		
//		if (authorities != null && !authorities.isEmpty()) {
//			GrantedAuthority authority = authorities.iterator().next();
//			
//			if (authority instanceof ResourceAuthority) {
//				ResourceAuthority resourceAuthority = ResourceAuthority.class.cast(authority);
//				form.setName(resourceAuthority.toString());
//			}
//		}
	}
	
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
	public FormView submit(final String rawProcessDefinitionKey, final MultivaluedMap<String, String> rawFormData) throws StatusCodeError {
		return this.submit(rawProcessDefinitionKey, null, rawFormData);
	}
	
	@Override
	public FormView submit(final String rawProcessDefinitionKey, final String rawProcessBusinessKey, final MultivaluedMap<String, String> rawFormData) throws StatusCodeError {
		// Sanitize all user input
		String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
		String processBusinessKey = sanitizer.sanitize(rawProcessBusinessKey);
		Map<String, List<String>> formData = sanitizer.sanitize(rawFormData);
	
		String submissionId = getSubmissionId(formData);
		
//		// Ensure that we always set a process business key
//		if (processBusinessKey == null)
//			processBusinessKey = UUID.randomUUID().toString();
//
//		// FIXME: Add in description for why this error is returned
//		if (processBusinessKey.length() > 140)
//			throw new BadRequestError();
//
//		// FIXME: Add logic to handle attachments in the case where the previous validation failed
////		if (submissionId != null)
////			storeAttachments(processDefinitionKey, processBusinessKey, submissionId);
//
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
	}
	
	@Override
	public ViewContext getViewContext() {
		return new ViewContext(baseApplicationUri, baseServiceUri, "v1", "form", "Form");
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
