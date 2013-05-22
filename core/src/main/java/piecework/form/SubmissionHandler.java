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
package piecework.form;

import com.mongodb.BasicDBObject;
import com.mongodb.gridfs.GridFSFile;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import piecework.Sanitizer;
import piecework.exception.InternalServerError;
import piecework.exception.StatusCodeError;
import piecework.model.FormRequest;
import piecework.model.FormSubmission;
import piecework.model.Screen;
import piecework.process.ProcessInstancePayload;
import piecework.process.SubmissionRepository;
import piecework.security.UserInputSanitizer;
import piecework.util.ManyMap;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author James Renfro
 */
@Service
public class SubmissionHandler {

    private static final Logger LOG = Logger.getLogger(SubmissionHandler.class);

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    SubmissionRepository submissionRepository;

    public FormSubmission handle(FormRequest formRequest, ProcessInstancePayload payload) throws StatusCodeError {
        String requestId = formRequest.getRequestId();
        Screen screen = formRequest.getScreen();
        boolean isAttachmentAllowed = screen == null || screen.isAttachmentAllowed();

        FormSubmission.Builder submissionBuilder = new FormSubmission.Builder()
                .requestId(requestId)
                .submissionDate(new Date())
                .submissionType(formRequest.getSubmissionType());

        switch (payload.getType()) {
            case INSTANCE:
                submissionBuilder.formData(payload.getInstance().getFormValueMap());
                break;
            case FORMDATA:
                submissionBuilder.formData(payload.getFormData());
                break;
            case MULTIPART:
                multipart(submissionBuilder, payload.getMultipartBody(), isAttachmentAllowed);
                break;
        }

        FormSubmission result = submissionBuilder.build();
        return submissionRepository.save(result);
    }

    private void multipart(FormSubmission.Builder submissionBuilder, MultipartBody body, boolean isAttachmentAllowed) {
        List<org.apache.cxf.jaxrs.ext.multipart.Attachment> attachments = body.getAllAttachments();
        if (attachments != null && !attachments.isEmpty()) {
            for (org.apache.cxf.jaxrs.ext.multipart.Attachment attachment : attachments) {
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
                if (contentDisposition != null && isAttachmentAllowed) {
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
    }

}
