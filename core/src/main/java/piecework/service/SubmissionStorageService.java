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
package piecework.service;

import com.mongodb.MongoException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.common.UuidGenerator;
import piecework.enumeration.FieldSubmissionType;
import piecework.exception.*;
import piecework.model.*;
import piecework.persistence.ContentRepository;
import piecework.security.EncryptionService;
import piecework.security.MaxSizeInputStream;
import piecework.security.concrete.PassthroughEncryptionService;
import piecework.submission.SubmissionTemplate;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author James Renfro
 */
@Service
public class SubmissionStorageService {

    private static final Logger LOG = Logger.getLogger(SubmissionStorageService.class);

    @Autowired
    ContentRepository contentRepository;

    @Autowired(required = false)
    EncryptionService encryptionService;

    @Autowired
    IdentityService identityService;

    @Autowired
    UuidGenerator uuidGenerator;

    @PostConstruct
    public void init() {
        if (encryptionService == null)
            encryptionService = new PassthroughEncryptionService();
    }

    public boolean store(SubmissionTemplate template, Submission.Builder submissionBuilder, String name, String value, String actingAsId, Entity principal) throws MisconfiguredProcessException, StatusCodeError {
        return store(template, submissionBuilder, name, value, actingAsId, null, MediaType.TEXT_PLAIN, principal);
    }

    public boolean store(SubmissionTemplate template, Submission.Builder submissionBuilder, String name, String value, String actingAsId, InputStream inputStream, String contentType, Entity principal) throws MisconfiguredProcessException, StatusCodeError {
        FieldSubmissionType fieldSubmissionType = template.fieldSubmissionType(name);

        if (fieldSubmissionType == FieldSubmissionType.BUTTON) {
            // Note that submitting multiple button messages on a form will result in unpredictable behavior
            Button button = template.getButton(value);
            button(button, name, value, submissionBuilder);
            return true;
        } else if (fieldSubmissionType != FieldSubmissionType.INVALID) {
            Field field = template.getField(name);
            String location = null;
            File file = null;

            if (inputStream != null) {
                String directory = StringUtils.isNotEmpty(submissionBuilder.getProcessDefinitionKey()) ? submissionBuilder.getProcessDefinitionKey() : "submissions";
                String id = uuidGenerator.getNextId();
                location = "/" + directory + "/" + id;

                if (fieldSubmissionType == FieldSubmissionType.ATTACHMENT)
                    inputStream = new MaxSizeInputStream(inputStream, Long.valueOf(template.getMaxAttachmentSize()) * 1024l);
                else if (field != null && field.getMaxValueLength() > 0)
                    inputStream = new MaxSizeInputStream(inputStream, Long.valueOf(field.getMaxValueLength()).longValue() * 1024l);

                Map<String, String> metadata = new HashMap<String, String>();

                if (field != null && field.getMetadataTemplates() != null) {
                    // FIXME: Need to pass context to templates and compile them to provide instance metadata for content
                    metadata = new HashMap<String, String>(field.getMetadataTemplates());
                }

                Content content = new Content.Builder()
                        .contentType(contentType)
                        .filename(value)
                        .location(location)
                        .inputStream(inputStream)
                        .metadata(metadata)
                        .build();

                try {
                    content = contentRepository.save(template.getProcess(), content, principal);

                } catch (MongoException mongoException) {
                    Throwable cause = mongoException.getCause();
                    if (cause instanceof MaxSizeExceededException) {
                        MaxSizeExceededException sizeExceededException = MaxSizeExceededException.class.cast(cause);
                        throw new BadRequestError(Constants.ExceptionCodes.attachment_is_too_large, Long.valueOf(sizeExceededException.getMaxSize()));
                    } else {
                        LOG.error("Failed to store file to mongo", mongoException);
                        throw new InternalServerError();
                    }
                } catch (IOException ioe) {
                    LOG.error(ioe);
                    String body = ioe.getMessage();
                    throw new InternalServerError(Constants.ExceptionCodes.content_cannot_be_stored, body);
                }

                location = content.getLocation();

                String description = submissionBuilder.getDescription(name);
                file = new File.Builder()
                        .id(id)
                        .name(value)
                        .description(description)
                        .location(location)
                        .contentType(contentType)
                        .build();
            } else if (field != null && field.getType().equals(Constants.FieldTypes.URL)) {
                // Don't bother to store empty urls
                if (StringUtils.isEmpty(value))
                    return false;

                String id = uuidGenerator.getNextId();
                String description = submissionBuilder.getDescription(name);
                file = new File.Builder()
                        .id(id)
                        .name(value)
                        .description(description)
                        .contentType("text/url")
                        .build();
            }

            if (fieldSubmissionType == FieldSubmissionType.RESTRICTED) {
                try {
                    submissionBuilder.formValue(name, encryptionService.encrypt(value));
                } catch (Exception e) {
                    LOG.error("Failed to correctly encrypt form value for " + name, e);
                    throw new InternalServerError(Constants.ExceptionCodes.encryption_error, name);
                }
            } else if (fieldSubmissionType == FieldSubmissionType.ACCEPTABLE) {
                if (file != null)
                    submissionBuilder.formValue(name, file);
                else if (template.isUserField(name))
                    submissionBuilder.formValue(name, identityService.getUser(value));
                else
                    submissionBuilder.formValue(name, value);
            } else if (fieldSubmissionType == FieldSubmissionType.RANDOM) {
                if (file != null)
                    submissionBuilder.formValue(name, file);
                else
                    submissionBuilder.formValue(name, value);
            } else if (fieldSubmissionType == FieldSubmissionType.ATTACHMENT) {
                if (file != null) {
                    contentType = file.getContentType();
                    location = file.getLocation();
                } else {
                    contentType = MediaType.TEXT_PLAIN;
                    location = null;
                    // Don't bother to save 'notes' if they're empty
                    if (StringUtils.isEmpty(value))
                        return false;
                }
                Attachment attachmentDetails = new Attachment.Builder()
                        .contentType(contentType)
                        .location(location)
                        .processDefinitionKey(submissionBuilder.getProcessDefinitionKey())
                        .description(value)
                        .userId(actingAsId)
                        .name(name)
                        .build();
                submissionBuilder.attachment(attachmentDetails);
            }

            return true;
        } else if (fieldSubmissionType != FieldSubmissionType.DESCRIPTION) {
            submissionBuilder.description(name, value);
        }

        return false;
    }

    private void button(Button button, String name, String value, Submission.Builder submissionBuilder) throws MisconfiguredProcessException {
        if (button == null)
            throw new MisconfiguredProcessException("Button of this name (" + name + ") exists, but the button value (" + value + ") has not been configured");

        submissionBuilder.actionType(button.getAction());
    }
}
