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

import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSFile;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import piecework.Constants;
import piecework.common.view.ViewContext;
import piecework.engine.ProcessEngineRuntimeFacade;
import piecework.exception.*;
import piecework.form.FormResource;
import piecework.form.PageRepository;
import piecework.form.validation.FormValidator;
import piecework.model.*;
import piecework.model.Process;
import piecework.process.*;
import piecework.security.UserInputSanitizer;
import piecework.util.ManyMap;

/**
 * @author James Renfro
 */
@Service
public class FormResourceVersion1Impl implements FormResource {

    private static final Logger LOG = Logger.getLogger(FormResourceVersion1Impl.class);

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    ProcessRepository processRepository;

    @Autowired
    ProcessInstanceRepository processInstanceRepository;

    @Autowired
    PageRepository pageRepository;

    @Autowired
    RequestRepository requestRepository;

    @Autowired
    ScreenRepository screenRepository;

    @Autowired
    SubmissionRepository submissionRepository;
	
	@Autowired
	ProcessEngineRuntimeFacade runtime;
	
	@Autowired
	UserInputSanitizer sanitizer;

    @Autowired
    FormValidator validator;

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

        List<Screen> screens = interaction.getScreens();

        if (screens == null || screens.isEmpty())
            throw new InternalServerError();

        Screen screen = screens.iterator().next();
        String location = screen.getLocation();

        // Generate a new uuid for this request
        String requestId = UUID.randomUUID().toString();

        FormRequest formRequest = new FormRequest.Builder()
                .requestId(requestId)
                .processDefinitionKey(processDefinitionKey)
                .remoteAddr(request.getRemoteAddr())
                .remoteHost(request.getRemoteHost())
                .remotePort(request.getRemotePort())
                .remoteUser(request.getRemoteUser())
                .screen(screen)
                .submissionType(Constants.SubmissionTypes.START)
                .build();

        formRequest = requestRepository.save(formRequest);

        Form form = new Form.Builder()
                .formInstanceId(formRequest.getRequestId())
                .processDefinitionKey(processDefinitionKey)
                .submissionType(Constants.SubmissionTypes.START)
                .screen(screen)
                .build();

        if (StringUtils.isNotEmpty(location)) {
            // If the location is not blank then delegate to the
            return pageRepository.getPageResponse(form, location);
        }

        return Response.ok(form).build();
	}

    @Override
    public Response submit(@PathParam("processDefinitionKey") String rawProcessDefinitionKey, String rawRequestId, HttpServletRequest request, MultipartBody body) throws StatusCodeError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        String requestId = sanitizer.sanitize(rawRequestId);

        Process process = processRepository.findOne(processDefinitionKey);

        if (process == null)
            throw new ForbiddenError(Constants.ExceptionCodes.process_does_not_exist);

        FormRequest formRequest = requestRepository.findOne(requestId);

        if (formRequest == null) {
            LOG.warn("Request being submitted for invalid/missing requestId " + requestId);
            throw new ForbiddenError(Constants.ExceptionCodes.request_does_not_match);
        }

        if (request.getRemoteUser() != null && formRequest.getRemoteUser() != null && !request.getRemoteUser().equals(formRequest.getRemoteUser())) {
            LOG.error("Wrong user submitting form: " + request.getRemoteUser() + " not " + formRequest.getRemoteUser());
            throw new ForbiddenError(Constants.ExceptionCodes.user_does_not_match);
        }

        if (request.getRemoteHost() != null && formRequest.getRemoteHost() != null && !request.getRemoteHost().equals(formRequest.getRemoteHost()))
            LOG.warn("This should not happen -- submission remote host (" + request.getRemoteHost() + ") does not match request (" + formRequest.getRemoteHost() + ")");

        if (request.getRemoteAddr() != null && formRequest.getRemoteAddr() != null && !request.getRemoteAddr().equals(formRequest.getRemoteAddr()))
            LOG.warn("This should not happen -- submission remote address (" + request.getRemoteAddr() + ") does not match request (" + formRequest.getRemoteAddr() + ")");

        if (request.getRemotePort() != formRequest.getRemotePort())
            LOG.warn("This should not happen -- submission remote port (" + request.getRemotePort() + ") does not match request (" + formRequest.getRemotePort() + ")");

        Screen screen = formRequest.getScreen();

        if (screen == null) {
            LOG.error("No screen configured for request " + requestId);
            throw new InternalServerError();
        }

        FormSubmission.Builder submissionBuilder = new FormSubmission.Builder()
                .requestId(requestId)
                .submissionDate(new Date())
                .submissionType(formRequest.getSubmissionType());

        ManyMap<String, String> formData = new ManyMap<String, String>();

        List<Attachment> attachments = body.getAllAttachments();
        if (attachments != null && !attachments.isEmpty()) {
            for (Attachment attachment : attachments) {
                ContentDisposition contentDisposition = attachment.getContentDisposition();
                MediaType contentType = attachment.getContentType();

                // Don't process if there's no content type
                if (contentType == null)
                    continue;

                if (contentType.equals("text/plain")) {
                    // Treat as a String form value
                    String key = sanitizer.sanitize(attachment.getContentId());
                    String value = sanitizer.sanitize(attachment.getObject(String.class));

                    submissionBuilder.formValue(key, value);
                }
                if (contentDisposition != null && screen.isAttachmentAllowed()) {
                    String filename = sanitizer.sanitize(contentDisposition.getParameter("filename"));
                    try {
                        BasicDBObject metadata = new BasicDBObject();
                        metadata.append("Content-Type", contentType);
                        String uuid = UUID.randomUUID().toString();
                        GridFSFile file = gridFsTemplate.store(attachment.getDataHandler().getInputStream(), uuid, metadata);

                        submissionBuilder.formValue(filename, uuid);
                    } catch (IOException e) {
                        LOG.error("Unable to save this attachment with filename: " + filename);
                    }
                }
            }
        }

        FormSubmission submission = submissionBuilder.build();

        if (request.getRemoteUser() != null && formRequest.getRemoteUser() != null) {
            // If this is not an anonymous submission, then the data will be saved before actually validating or modifying the process instance data
            // so the user can come back to it later if she wants to


//            String submissionDisposition = body.getAttachmentObject(Constants.SubmissionDirectives.SUBMISSION_DISPOSITION, String.class);
//
//            if (submissionDisposition != null) {
//                if (submissionDisposition.equals(Constants.SubmissionDirectiveDispositionValues.SAVE)) {
//
//
//                }
//            }
        	
        }


        String submissionId = getSubmissionId(formData);

//        // Ensure that we always set a process business key
//        if (processBusinessKey == null)
//            processBusinessKey = UUID.randomUUID().toString();
//
//        // FIXME: Add in description for why this error is returned
//        if (processBusinessKey.length() > 140)
//            throw new BadRequestError(Constants.ExceptionCodes.process_business_key_limit);


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
