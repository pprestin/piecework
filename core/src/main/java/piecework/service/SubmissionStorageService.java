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
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.common.UuidGenerator;
import piecework.content.ContentResource;
import piecework.content.concrete.BasicContentResource;
import piecework.enumeration.ActionType;
import piecework.enumeration.FieldSubmissionType;
import piecework.exception.*;
import piecework.model.*;
import piecework.persistence.ContentProfileProvider;
import piecework.persistence.ProcessInstanceProvider;
import piecework.repository.ContentRepository;
import piecework.security.EncryptionService;
import piecework.security.MaxSizeInputStream;
import piecework.security.concrete.PassthroughEncryptionService;
import piecework.submission.Directive;
import piecework.submission.SubmissionTemplate;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author James Renfro
 */
@Service
public class SubmissionStorageService {

    private static final Logger LOG = Logger.getLogger(SubmissionStorageService.class);
    private DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTimeParser();

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

    public <P extends ContentProfileProvider> boolean store(P modelProvider, SubmissionTemplate template, Submission.Builder submissionBuilder, String name, String value, String actingAsId) throws PieceworkException {
        return store(modelProvider, template, submissionBuilder, name, value, actingAsId, null, MediaType.TEXT_PLAIN);
    }

    public <P extends ContentProfileProvider> boolean store(P modelProvider, SubmissionTemplate template, Submission.Builder submissionBuilder, String name, String value, String actingAsId, InputStream inputStream, String contentType) throws PieceworkException {
        FieldSubmissionType fieldSubmissionType = template.fieldSubmissionType(name);

        if (fieldSubmissionType == FieldSubmissionType.BUTTON) {
            // Note that submitting multiple button messages on a form will result in unpredictable behavior
            Button button = template.getButton(value);
            button(modelProvider, button, name, value, submissionBuilder);

            // set actionValue for current task
            String taskId = template.getTaskId();
            if ( taskId != null && modelProvider!= null && modelProvider instanceof ProcessInstanceProvider ) {
                ProcessInstanceProvider pirp = (ProcessInstanceProvider) modelProvider;
                ProcessInstance pi = pirp.instance();
                if ( pi != null ) {
                    Task task = pi.getTask(taskId);
                    if ( task != null ) {
                        submissionBuilder.formValue(task.getTaskDefinitionKey() + "_actionValue", value);
                    }
                }
            }

            return true;
        } else if (fieldSubmissionType != FieldSubmissionType.INVALID) {
            Field field = template.getField(name);
            File file = null;

            if (inputStream != null) {
                LOG.info("Found content from a stream");
                if (fieldSubmissionType == FieldSubmissionType.ATTACHMENT)
                    inputStream = new MaxSizeInputStream(inputStream, Long.valueOf(template.getMaxAttachmentSize()) * 1024l);
                else if (field != null && field.getMaxValueLength() > 0)
                    inputStream = new MaxSizeInputStream(inputStream, Long.valueOf(field.getMaxValueLength()).longValue() * 1024l);

                Map<String, String> metadata = new HashMap<String, String>();

                if (field != null && field.getMetadataTemplates() != null) {
                    // FIXME: Need to pass context to templates and compile them to provide instance metadata for content
                    metadata = new HashMap<String, String>(field.getMetadataTemplates());
                }

                String description = submissionBuilder.getDescription(name);
                if (StringUtils.isEmpty(description))
                    description = value;

                ContentResource contentResource = new BasicContentResource.Builder()
                        .contentType(contentType)
                        .name(name)
                        .description(description)
                        .filename(value)
                        .inputStream(inputStream)
                        .lastModified(new Date())
                        .lastModifiedBy(actingAsId)
                        .metadata(metadata)
                        .build();

                file = new File.Builder()
                        .name(value)
                        .description(description)
                        .contentResource(contentResource)
                        .contentType(contentType)
                        .build();

            } else if (field != null && field.getType().equals(Constants.FieldTypes.URL)) {
                // Don't bother to store empty urls
                if (StringUtils.isEmpty(value))
                    return false;

                String id = uuidGenerator.getNextId();
                String description = submissionBuilder.getDescription(name);
                if (StringUtils.isEmpty(description))
                    description = value;
                file = new File.Builder()
                        .id(id)
                        .name(value)
                        .description(description)
                        .contentType("text/url")
                        .build();
            }

            if (fieldSubmissionType == FieldSubmissionType.RESTRICTED) {
                LOG.info("Processing restricted field " + name);
                try {
                    submissionBuilder.formValue(name, encryptionService.encrypt(value));
                } catch (Exception e) {
                    LOG.error("Failed to correctly encrypt form value for " + name, e);
                    throw new InternalServerError(Constants.ExceptionCodes.encryption_error, name);
                }
            } else if (fieldSubmissionType == FieldSubmissionType.ACCEPTABLE) {
                LOG.info("Processing acceptable field " + name);
                if (file != null)
                    submissionBuilder.formValue(name, file);
                else if (template.isUserField(name))
                    submissionBuilder.formValue(name, identityService.getUser(value));
                else if (template.isDateField(name)) {
                    value = value.replaceAll("\"", "");
                    if (StringUtils.isNotEmpty(value)) {
                        DateTime dateTime = dateTimeFormatter.parseDateTime(value);
                        if (dateTime != null)
                            submissionBuilder.formValue(name, new DateValue(dateTime));
                    }
                } else
                    submissionBuilder.formValue(name, value);

            } else if (fieldSubmissionType == FieldSubmissionType.RANDOM) {
                LOG.info("Processing random field " + name);
                if (file != null)
                    submissionBuilder.formValue(name, file);
                else
                    submissionBuilder.formValue(name, value);
            } else if (fieldSubmissionType == FieldSubmissionType.ATTACHMENT) {
                LOG.info("Processing attachment " + name);
                if (file == null) {
                    file = new File.Builder()
                            .name(name)
                            .contentType(MediaType.TEXT_PLAIN)
                            .description(value)
                            .filerId(actingAsId)
                            .build();
                }
                submissionBuilder.attachment(file);
            }

            return true;
        } else if (fieldSubmissionType != FieldSubmissionType.DESCRIPTION) {
            submissionBuilder.description(name, value);
        }

        return false;
    }

    private void button(ContentProfileProvider modelProvider, Button button, String name, String value, Submission.Builder submissionBuilder) throws PieceworkException {
        if (button == null)
            throw new MisconfiguredProcessException("Button of this name (" + name + ") exists, but the button value (" + value + ") has not been configured");

        try {
            ActionType actionType = StringUtils.isNotEmpty(button.getAction()) ? ActionType.valueOf(button.getAction()) : null;

            if (actionType != null) {
                LOG.info("Button action type is " + actionType);
                submissionBuilder.actionType(actionType);

                // FIXME: Potential pipeline mechanism for sending data back to the Activiti Engine for routing - this would need to be added to the
                // FIXME: submission object and pass into the ProcessEngineFacade.completeTask() method.
                // FIXME: Requires some more thought about how additional routing variables could be set
                Directive directive = new Directive.Builder()
                        .taskVariable(name, value)
                        .instanceVariable(name, value)
                        .build();
            }
        } catch (IllegalArgumentException iae) {
            String processDefinitionKey = modelProvider.processDefinitionKey();
            throw new MisconfiguredProcessException("Could not determine correct action to take", processDefinitionKey, iae);
        }
    }

}
