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
package piecework.handler;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.common.UuidGenerator;
import piecework.enumeration.ActionType;
import piecework.exception.InternalServerError;
import piecework.validation.SubmissionTemplate;
import piecework.identity.IdentityService;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ContentRepository;
import piecework.identity.IdentityHelper;
import piecework.security.EncryptionService;
import piecework.security.Sanitizer;
import piecework.persistence.SubmissionRepository;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author James Renfro
 */
@Service
public class SubmissionHandler {

    private static final Logger LOG = Logger.getLogger(SubmissionHandler.class);

    @Autowired
    ContentRepository contentRepository;

    @Autowired
    EncryptionService encryptionService;

    @Autowired
    IdentityHelper helper;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    SubmissionRepository submissionRepository;

    @Autowired
    IdentityService userDetailsService;

    @Autowired
    UuidGenerator uuidGenerator;

//    public Submission handle(Process process, SubmissionTemplate template, Submission rawSubmission) throws InternalServerError {
//        return handle(process, template, rawSubmission);
//    }

    public Submission handle(Process process, SubmissionTemplate template, Submission rawSubmission, FormRequest formRequest, ActionType action) throws InternalServerError {
        String submitterId = helper.getAuthenticatedSystemOrUserId();

        if (helper.isAuthenticatedSystem() && StringUtils.isNotEmpty(formRequest.getActAsUser()))
            submitterId = formRequest.getActAsUser();
        else if (rawSubmission != null && StringUtils.isNotEmpty(rawSubmission.getSubmitterId()))
            submitterId = sanitizer.sanitize(rawSubmission.getSubmitterId());

        Submission.Builder submissionBuilder;

        if (rawSubmission != null)
            submissionBuilder = new Submission.Builder(rawSubmission, sanitizer, true);
        else
            submissionBuilder = new Submission.Builder();

        submissionBuilder.processDefinitionKey(process.getProcessDefinitionKey())
                .requestId(formRequest != null ? formRequest.getRequestId() : null)
                .taskId(formRequest != null ? formRequest.getTaskId() : null)
                .submissionDate(new Date())
                .submitterId(submitterId)
                .action(action.name());

        if (rawSubmission != null && rawSubmission.getData() != null) {
            for (Map.Entry<String, List<Value>> entry : rawSubmission.getData().entrySet()) {
                String name = sanitizer.sanitize(entry.getKey());
                List<? extends Value> values = entry.getValue();

                for (Value value : values) {
                    String actualValue = value.getValue();
                    if (!handleStorage(template, submissionBuilder, name, actualValue, submitterId)) {
                        LOG.warn("Submission included field (" + name + ") that is not acceptable, and no attachments are allowed for this template");
                    }
                }
            }
        }

        return submissionRepository.save(submissionBuilder.build());
    }

    public Submission handle(Process process, SubmissionTemplate template, Map<String, List<String>> formValueContentMap) throws InternalServerError {
        return handle(process, template, formValueContentMap, null);
    }

    public Submission handle(Process process, SubmissionTemplate template, Map<String, List<String>> formValueContentMap, FormRequest formRequest) throws InternalServerError {
        Submission.Builder submissionBuilder = new Submission.Builder()
                .processDefinitionKey(process.getProcessDefinitionKey())
                .requestId(formRequest != null ? formRequest.getRequestId() : null)
                .taskId(formRequest != null ? formRequest.getTaskId() : null)
                .submissionDate(new Date())
                .submitterId(helper.getAuthenticatedSystemOrUserId());

        if (formValueContentMap != null && !formValueContentMap.isEmpty()) {
            String userId = helper.getAuthenticatedSystemOrUserId();

            for (Map.Entry<String, List<String>> entry : formValueContentMap.entrySet()) {
                String name = sanitizer.sanitize(entry.getKey());
                List<String> rawValues = entry.getValue();

                if (rawValues != null) {
                    for (String rawValue : rawValues) {
                        String value = sanitizer.sanitize(rawValue);
                        if (!handleStorage(template, submissionBuilder, name, value, userId)) {
                            LOG.warn("Submission included field (" + name + ") that is not acceptable, and no attachments are allowed for this template");
                        }
                    }
                }
            }
        }

        return submissionRepository.save(submissionBuilder.build());
    }

    public Submission handle(Process process, SubmissionTemplate template, MultipartBody body) throws InternalServerError {
        return handle(process, template, body, null);
    }

    public Submission handle(Process process, SubmissionTemplate template, MultipartBody body, FormRequest formRequest) throws InternalServerError {
        Submission.Builder submissionBuilder = new Submission.Builder()
                .processDefinitionKey(process.getProcessDefinitionKey())
                .requestId(formRequest != null ? formRequest.getRequestId() : null)
                .taskId(formRequest != null ? formRequest.getTaskId() : null)
                .submissionDate(new Date())
                .submitterId(helper.getAuthenticatedSystemOrUserId());

        String userId = helper.getAuthenticatedSystemOrUserId();
        List<org.apache.cxf.jaxrs.ext.multipart.Attachment> attachments = body != null ? body.getAllAttachments() : null;
        if (attachments != null && !attachments.isEmpty()) {
            for (org.apache.cxf.jaxrs.ext.multipart.Attachment attachment : attachments) {
                MediaType mediaType = attachment.getContentType();

                // Don't process if there's no content type
                if (mediaType == null)
                    continue;

                handleAllContentTypes(template, submissionBuilder, attachment, userId);
            }
        }
        return submissionRepository.save(submissionBuilder.build());
    }

    private void handlePlaintext(SubmissionTemplate template, Submission.Builder submissionBuilder, org.apache.cxf.jaxrs.ext.multipart.Attachment attachment, String userId) throws InternalServerError {
        String contentType = MediaType.TEXT_PLAIN;
        if (LOG.isDebugEnabled())
            LOG.debug("Processing multipart with content type " + contentType + " and content id " + attachment.getContentId());

        String name = sanitizer.sanitize(attachment.getDataHandler().getName());
        String value = sanitizer.sanitize(attachment.getObject(String.class));

        if (!handleStorage(template, submissionBuilder, name, value, userId)) {
            LOG.warn("Submission included field (" + name + ") that is not acceptable, and no attachments are allowed for this template");
        }
    }

    private void handleAllContentTypes(SubmissionTemplate template, Submission.Builder submissionBuilder, org.apache.cxf.jaxrs.ext.multipart.Attachment attachment, String userId) throws InternalServerError {
        ContentDisposition contentDisposition = attachment.getContentDisposition();
        MediaType mediaType = attachment.getContentType();

        if (contentDisposition != null) {
            String contentType = mediaType.toString();
            String name = sanitizer.sanitize(contentDisposition.getParameter("name"));
            String filename = sanitizer.sanitize(contentDisposition.getParameter("filename"));
            if (StringUtils.isNotEmpty(filename)) {
                if (LOG.isDebugEnabled())
                    LOG.debug("Processing multipart with content type " + contentType + " content id " + attachment.getContentId() + " and filename " + filename);
                    try {
                        if (!handleStorage(template, submissionBuilder, name, filename, userId, attachment.getDataHandler().getInputStream(), contentType)) {
                           LOG.warn("Submission included field (" + name + ") that is not acceptable, and no attachments are allowed for this template");
                        }
                    } catch (IOException e) {
                        LOG.warn("Unable to store file with content type " + contentType + " and filename " + filename);
                    }
            } else if (mediaType.equals(MediaType.TEXT_PLAIN_TYPE)) {
                handlePlaintext(template, submissionBuilder, attachment, userId);
            }
        }
    }

    private boolean handleStorage(SubmissionTemplate template, Submission.Builder submissionBuilder, String name, String value, String userId) throws InternalServerError {
        return handleStorage(template, submissionBuilder, name, value, userId, null, MediaType.TEXT_PLAIN);
    }

    private boolean handleStorage(SubmissionTemplate template, Submission.Builder submissionBuilder, String name, String value, String userId, InputStream inputStream, String contentType) throws InternalServerError {
        boolean isAcceptable = template.isAcceptable(name);
        boolean isButton = template.isButton(name);
        boolean isRestricted = !isAcceptable && template.isRestricted(name);
        boolean isAttachment = !isAcceptable && !isRestricted && template.isAttachmentAllowed();
        boolean isUserField = template.isUserField(name);

        if (isButton) {
            // Note that submitting multiple button messages on a form will result in unpredictable behavior
            Button button = template.getButton(value);
            if (button == null) {
                LOG.error("Button of this name (" + name + ") exists, but the button value (" + value + ") has not been configured");
                throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
            }
            submissionBuilder.action(button.getAction());
            return true;
        } else if (isAcceptable || isRestricted || isAttachment) {
            String location = null;
            File file = null;

            if (inputStream != null) {
                String directory = StringUtils.isNotEmpty(submissionBuilder.getProcessDefinitionKey()) ? submissionBuilder.getProcessDefinitionKey() : "submissions";
                String id = uuidGenerator.getNextId();
                location = "/" + directory + "/" + id;

                Content content = new Content.Builder()
                        .contentType(contentType)
                        .filename(value)
                        .location(location)
                        .inputStream(inputStream)
                        .build();

                content = contentRepository.save(content);
                location = content.getLocation();
                file = new File.Builder()
                        .id(id)
                        .name(value)
                        .location(location)
                        .contentType(contentType)
                        .build();
            }

            if (isRestricted) {
                try {
                    submissionBuilder.formValue(name, encryptionService.encrypt(value));
                } catch (Exception e) {
                    LOG.error("Failed to correctly encrypt form value for " + name, e);
                    throw new InternalServerError(Constants.ExceptionCodes.encryption_error, name);
                }
            } else if (isAcceptable) {
                if (file != null)
                    submissionBuilder.formValue(name, file);
                else if (isUserField)
                    submissionBuilder.formValue(name, userDetailsService.getUserByAnyId(value));
                else
                    submissionBuilder.formValue(name, value);

            } else if (isAttachment) {
                if (file != null) {
                    contentType = file.getContentType();
                    location = file.getLocation();
                } else {
                    contentType = MediaType.TEXT_PLAIN;
                    location = null;
                }
                Attachment attachmentDetails = new Attachment.Builder()
                        .contentType(contentType)
                        .location(location)
                        .processDefinitionKey(submissionBuilder.getProcessDefinitionKey())
                        .description(value)
                        .userId(userId)
                        .name(name)
                        .build();
                submissionBuilder.attachment(attachmentDetails);
            }

            return true;
        }

        return false;
    }

}
