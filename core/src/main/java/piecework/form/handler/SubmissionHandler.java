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
            for (Map.Entry<String, List<String>> entry : formValueContentMap.entrySet()) {
                String key = sanitizer.sanitize(entry.getKey());
                List<String> rawValues = entry.getValue();
                if (rawValues != null) {
                    for (String rawValue : rawValues) {
                        submissionBuilder.formValue(key, sanitizer.sanitize(rawValue));
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
        Set<String> acceptableFieldNames = template.getAcceptable();
        boolean isAttachedAllowed = template.isAttachmentAllowed();

        String contentType = MediaType.TEXT_PLAIN;
        if (LOG.isDebugEnabled())
            LOG.debug("Processing multipart with content type " + contentType + " and content id " + attachment.getContentId());

        String name = sanitizer.sanitize(attachment.getDataHandler().getName());
        String value = sanitizer.sanitize(attachment.getObject(String.class));

        if (acceptableFieldNames.contains(name)) {
            submissionBuilder.formValue(name, value);
        } else if (isAttachedAllowed) {
            Attachment attachmentDetails = new Attachment.Builder()
                    .contentType(contentType)
                    .processDefinitionKey(submissionBuilder.getProcessDefinitionKey())
                    .description(value)
                    .userId(userId)
                    .name(name)
                    .build();
            submissionBuilder.attachment(attachmentDetails);
        } else {
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
                    Set<String> acceptableFieldNames = template.getAcceptable();
                    boolean isAttachedAllowed = template.isAttachmentAllowed();

                    if (acceptableFieldNames.contains(name) || isAttachedAllowed) {
                        String directory = StringUtils.isNotEmpty(submissionBuilder.getProcessDefinitionKey()) ? submissionBuilder.getProcessDefinitionKey() : "submissions";
                        String location = "/" + directory + "/" + uuidGenerator.getNextId();

                        Content content = new Content.Builder()
                                .contentType(contentType)
                                .filename(filename)
                                .location(location)
                                .inputStream(attachment.getDataHandler().getInputStream())
                                .build();

                        content = contentRepository.save(content);

                        if (acceptableFieldNames.contains(name)) {
                            FormValueDetail detail = new FormValueDetail.Builder()
                                    .location(content.getLocation())
                                    .contentType(contentType)
                                    .build();

                            submissionBuilder.formValueAndDetail(name, filename, detail);
                        } else if (isAttachedAllowed) {
                            Attachment attachmentDetails = new Attachment.Builder()
                                    .contentType(contentType)
                                    .location(content.getLocation())
                                    .processDefinitionKey(submissionBuilder.getProcessDefinitionKey())
                                    .description(filename)
                                    .userId(userId)
                                    .name(name)
                                    .build();
                            submissionBuilder.attachment(attachmentDetails);
                        }
                    } else {
                       LOG.warn("Submission included field (" + name + ") that is not acceptable, and no attachments are allowed for this template");
                    }
                } catch (IOException e) {
                    LOG.error("Unable to save this attachment with filename: " + filename, e);
                }
            }
        }
    }

//    public Submission handle(Payload payload, boolean isAttachmentAllowed) throws StatusCodeError {
//        String requestId = payload.getRequestId();
//
//        Submission.Builder submissionBuilder = new Submission.Builder()
//                .requestId(requestId)
//                .submissionDate(new Date())
//                .submitterId(helper.getAuthenticatedSystemOrUserId());
////                .submissionType(submissionType);
//
//        switch (payload.getType()) {
//            case INSTANCE:
//                submissionBuilder.formData(payload.getInstance().getFormValueContentMap());
//                submissionBuilder.attachments(payload.getInstance().getAttachments());
//                break;
//            case FORMDATA:
//                submissionBuilder.formData(payload.getFormData());
//                break;
//            case FORMVALUEMAP:
//                submissionBuilder.formValueMap(payload.getFormValueMap());
//                break;
//            case MULTIPART:
//                multipart(payload.getProcessDefinitionKey(), submissionBuilder, payload.getMultipartBody(), isAttachmentAllowed);
//                break;
//        }
//
//        Submission result = submissionBuilder.build();
//        return submissionRepository.save(result);
//    }

}
