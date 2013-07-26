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
package piecework.form.handler;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.common.UuidGenerator;
import piecework.form.validation.SubmissionTemplate;
import piecework.identity.InternalUserDetails;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.AttachmentRepository;
import piecework.persistence.ContentRepository;
import piecework.process.concrete.ResourceHelper;
import piecework.security.Sanitizer;
import piecework.exception.StatusCodeError;
import piecework.persistence.SubmissionRepository;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author James Renfro
 */
@Service
public class SubmissionHandler {

    private static final Logger LOG = Logger.getLogger(SubmissionHandler.class);

    @Autowired
    ContentRepository contentRepository;

    @Autowired
    ResourceHelper helper;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    SubmissionRepository submissionRepository;

    @Autowired
    UuidGenerator uuidGenerator;

    public Submission handle(Process process, SubmissionTemplate template, Submission rawSubmission) {
        Submission.Builder submissionBuilder = new Submission.Builder(rawSubmission, sanitizer)
                .processDefinitionKey(process.getProcessDefinitionKey())
                .submissionDate(new Date())
                .submitterId(helper.getAuthenticatedSystemOrUserId());

        return submissionRepository.save(submissionBuilder.build());
    }

    public Submission handle(Process process, SubmissionTemplate template, Map<String, List<String>> formValueContentMap) {
        return handle(process, template, formValueContentMap, null);
    }

    public Submission handle(Process process, SubmissionTemplate template, Map<String, List<String>> formValueContentMap, FormRequest formRequest) {
        Submission.Builder submissionBuilder = new Submission.Builder()
                .processDefinitionKey(process.getProcessDefinitionKey())
                .requestId(formRequest != null ? formRequest.getRequestId() : null)
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

    public Submission handle(Process process, SubmissionTemplate template, MultipartBody body) {
        return handle(process, template, body, null);
    }

    public Submission handle(Process process, SubmissionTemplate template, MultipartBody body, FormRequest formRequest) {
        Submission.Builder submissionBuilder = new Submission.Builder()
                .processDefinitionKey(process.getProcessDefinitionKey())
                .requestId(formRequest != null ? formRequest.getRequestId() : null)
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

                if (mediaType.equals(MediaType.TEXT_PLAIN_TYPE))
                    handlePlaintext(template, submissionBuilder, attachment, userId);
                else
                    handleOtherContentTypes(template, submissionBuilder, attachment, userId);
            }
        }
        return submissionRepository.save(submissionBuilder.build());
    }

    private void handlePlaintext(SubmissionTemplate template, Submission.Builder submissionBuilder, org.apache.cxf.jaxrs.ext.multipart.Attachment attachment, String userId) {
        String contentType = MediaType.TEXT_PLAIN;
        if (LOG.isDebugEnabled())
            LOG.debug("Processing multipart with content type " + contentType + " and content id " + attachment.getContentId());

        String name = sanitizer.sanitize(attachment.getDataHandler().getName());
        String value = sanitizer.sanitize(attachment.getObject(String.class));

        if (!handleStorage(template, submissionBuilder, name, value, userId)) {
            LOG.warn("Submission included field (" + name + ") that is not acceptable, and no attachments are allowed for this template");
        }
    }

    private void handleOtherContentTypes(SubmissionTemplate template, Submission.Builder submissionBuilder, org.apache.cxf.jaxrs.ext.multipart.Attachment attachment, String userId) {
        ContentDisposition contentDisposition = attachment.getContentDisposition();
        if (contentDisposition != null) {
            MediaType mediaType = attachment.getContentType();
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
            }
        }
    }

    private boolean handleStorage(SubmissionTemplate template, Submission.Builder submissionBuilder, String name, String value, String userId) {
        return handleStorage(template, submissionBuilder, name, value, userId, null, MediaType.TEXT_PLAIN);
    }

    private boolean handleStorage(SubmissionTemplate template, Submission.Builder submissionBuilder, String name, String value, String userId, InputStream inputStream, String contentType) {
        boolean isAcceptable = template.isAcceptable(name);
        boolean isButton = template.isButton(name);
        boolean isRestricted = !isAcceptable && template.isRestricted(name);
        boolean isAttachment = !isAcceptable && !isRestricted && template.isAttachmentAllowed();

        if (isButton) {
            submissionBuilder.buttonValue(value);
            return true;
        } else if (isAcceptable || isRestricted || isAttachment) {
            String location = null;
            FormValueDetail detail = null;

            if (inputStream != null) {
                String directory = StringUtils.isNotEmpty(submissionBuilder.getProcessDefinitionKey()) ? submissionBuilder.getProcessDefinitionKey() : "submissions";
                location = "/" + directory + "/" + uuidGenerator.getNextId();

                Content content = new Content.Builder()
                        .contentType(contentType)
                        .filename(value)
                        .location(location)
                        .inputStream(inputStream)
                        .build();

                content = contentRepository.save(content);
                location = content.getLocation();
                detail = new FormValueDetail.Builder()
                        .location(location)
                        .contentType(contentType)
                        .build();
            }

            if (isAcceptable) {
                submissionBuilder.formValueAndDetail(name, value, detail);
            } else if (isRestricted) {
                submissionBuilder.restrictedValueAndDetail(name, value, detail);
            } else if (isAttachment) {
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
