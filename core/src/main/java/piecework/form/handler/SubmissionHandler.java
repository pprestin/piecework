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
import piecework.model.Content;
import piecework.persistence.ContentRepository;
import piecework.common.Payload;
import piecework.security.Sanitizer;
import piecework.exception.StatusCodeError;
import piecework.model.FormSubmission;
import piecework.persistence.SubmissionRepository;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author James Renfro
 */
@Service
public class SubmissionHandler {

    private static final Logger LOG = Logger.getLogger(SubmissionHandler.class);

    @Autowired
    ContentRepository contentRepository;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    SubmissionRepository submissionRepository;

    @Autowired
    UuidGenerator uuidGenerator;

    public FormSubmission handle(Payload payload, boolean isAttachmentAllowed) throws StatusCodeError {
        String requestId = payload.getRequestId();

        FormSubmission.Builder submissionBuilder = new FormSubmission.Builder()
                .requestId(requestId)
                .submissionDate(new Date());
//                .submissionType(submissionType);

        switch (payload.getType()) {
            case INSTANCE:
                submissionBuilder.formData(payload.getInstance().getFormValueContentMap());
                submissionBuilder.attachments(payload.getInstance().getAttachments());
                break;
            case FORMDATA:
                submissionBuilder.formData(payload.getFormData());
                break;
            case FORMVALUEMAP:
                submissionBuilder.formValueMap(payload.getFormValueMap());
                break;
            case MULTIPART:
                multipart(submissionBuilder, payload.getMultipartBody(), isAttachmentAllowed);
                break;
        }

        FormSubmission result = submissionBuilder.build();
        return submissionRepository.save(result);
    }

    private void multipart(FormSubmission.Builder submissionBuilder, MultipartBody body, boolean isAttachmentAllowed) {
        List<org.apache.cxf.jaxrs.ext.multipart.Attachment> attachments = body != null ? body.getAllAttachments() : null;
        if (attachments != null && !attachments.isEmpty()) {
            for (org.apache.cxf.jaxrs.ext.multipart.Attachment attachment : attachments) {
                ContentDisposition contentDisposition = attachment.getContentDisposition();
                MediaType contentType = attachment.getContentType();

                // Don't process if there's no content type
                if (contentType == null)
                    continue;

                String contentId = sanitizer.sanitize(attachment.getContentId());
                String name = sanitizer.sanitize(attachment.getDataHandler().getName());

                if (contentType.equals(MediaType.TEXT_PLAIN_TYPE)) {
                    LOG.info("Processing multipart with content type " + contentType.toString() + " and content id " + attachment.getContentId());
                    // Treat as a String form value

                    String value = sanitizer.sanitize(attachment.getObject(String.class));
                    submissionBuilder.formValue(name, value);
                } else if (contentDisposition != null && isAttachmentAllowed) {
                    String variableName = contentDisposition.getParameter("name");
                    String filename = sanitizer.sanitize(contentDisposition.getParameter("filename"));
                    if (StringUtils.isNotEmpty(filename)) {
                        LOG.info("Processing multipart with content type " + contentType.toString() + " content id " + attachment.getContentId() + " and filename " + filename);
                        try {
                            String location = "/submissions/" + uuidGenerator.getNextId();

                            Content content = new Content.Builder()
                                    .contentType(contentType.toString())
                                    .filename(filename)
                                    .location(location)
                                    .inputStream(attachment.getDataHandler().getInputStream())
                                    .build();

                            content = contentRepository.save(content);

                            submissionBuilder.formContent(contentType.toString(), variableName, filename, content.getLocation());

                        } catch (IOException e) {
                            LOG.error("Unable to save this attachment with filename: " + filename);
                        }
                    }
                }
            }
        }
    }

}
