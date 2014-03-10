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
package piecework.validation;

import com.google.common.collect.Sets;
import com.mongodb.MongoException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.common.ViewContext;
import piecework.content.ContentResource;
import piecework.content.Version;
import piecework.exception.*;
import piecework.model.*;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.repository.ContentRepository;
import piecework.security.data.DataFilterService;
import piecework.settings.UserInterfaceSettings;
import piecework.submission.SubmissionTemplate;
import piecework.util.ModelUtility;
import piecework.util.ValidationUtility;

import java.io.IOException;
import java.util.*;

/**
 * @author James Renfro
 */
@Service
public class ValidationFactory {
	
	private static final Set<String> FREEFORM_INPUT_TYPES = Sets.newHashSet("text", "textarea", "person-lookup", "current-date", "current-user");
	private static final Logger LOG = Logger.getLogger(ValidationFactory.class);

    @Autowired
    ContentRepository contentRepository;

    @Autowired
    DataFilterService dataFilterService;

    @Autowired
    UserInterfaceSettings settings;

    public <P extends ProcessDeploymentProvider> Validation validation(P modelProvider, SubmissionTemplate template, Submission submission, String version, boolean onlyAcceptValidInputs, boolean ignoreException) throws PieceworkException {
        long time = 0;

        if (LOG.isDebugEnabled())
            time = System.currentTimeMillis();

        // Validate the submission
        Validation validation = validate(modelProvider, submission, template, version, onlyAcceptValidInputs);

        if (LOG.isDebugEnabled())
            LOG.debug("Validation took " + (System.currentTimeMillis() - time) + " ms");

        Map<String, List<Message>> results = validation.getResults();
        if (!ignoreException && results != null && !results.isEmpty()) {
            // Throw an exception if the submitter needs to adjust the data
            throw new BadRequestError(validation);
        }

        return validation;
    }

    public <P extends ProcessDeploymentProvider> Validation validate(P modelProvider,
                                Submission submission, SubmissionTemplate template, String version, boolean onlyAcceptValidInputs) throws PieceworkException {

        Map<Field, List<ValidationRule>> fieldRuleMap = template.getFieldRuleMap();
        Set<String> allFieldNames = Collections.unmodifiableSet(new HashSet<String>(template.getFieldMap().keySet()));
        Set<String> fieldNames = new HashSet<String>(template.getFieldMap().keySet());

        ProcessInstance instance = ModelUtility.instance(modelProvider);
        Map<String, List<Value>> instanceData = instance != null ? instance.getData() : null;
        Map<String, List<Value>> submissionData = submission.getData();

        Map<String, List<Value>> decryptedInstanceData = null;
        Map<String, List<Value>> decryptedSubmissionData = null;
        String reason = "User is submitting data that needs to be validated";

        Task task = null;
        Validation.Builder validationBuilder = new Validation.Builder().submission(submission);

        List<File> attachments = submission.getAttachments();
        if (template.isAttachmentAllowed() && attachments != null) {
            Map<String, String> existingAttachmentLocations = new HashMap<String, String>();

            List<Attachment> instanceAttachments = ModelUtility.attachments(modelProvider, new ViewContext(settings, version));
            if (instanceAttachments != null && !instanceAttachments.isEmpty()) {
                for (Attachment attachment : instanceAttachments) {
                    if (StringUtils.isNotEmpty(attachment.getName()) && StringUtils.isNotEmpty(attachment.getLocation()))
                        existingAttachmentLocations.put(attachment.getName(), attachment.getLocation());
                }
            }

            for (File file : attachments) {
                try {
                    // Handle comments
                    if (file.getContentResource() == null) {
                        Attachment attachment = new Attachment.Builder()
                                .contentType(file.getContentType())
                                .processDefinitionKey(modelProvider.processDefinitionKey())
                                .description(file.getDescription())
                                .userId(file.getFilerId())
                                .name(file.getName())
                                .build();

                        validationBuilder.attachment(attachment);
                        continue;
                    }

                    boolean isReplace = StringUtils.isNotEmpty(file.getName()) && existingAttachmentLocations.containsKey(file.getName());

                    ContentResource contentResource = saveOrReplace(modelProvider, file, existingAttachmentLocations);

                    if (!isReplace) {
                        Attachment attachment = new Attachment.Builder()
                                .contentType(contentResource.contentType())
                                .location(contentResource.getLocation())
                                .processDefinitionKey(modelProvider.processDefinitionKey())
                                .description(contentResource.getDescription())
                                .userId(contentResource.lastModifiedBy())
                                .name(file.getName())
                                .build();

                        validationBuilder.attachment(attachment);

                        if (StringUtils.isNotEmpty(attachment.getName()) && StringUtils.isNotEmpty(attachment.getLocation()))
                            existingAttachmentLocations.put(attachment.getName(), attachment.getLocation());
                    }

                } catch (Exception e) {
                    LOG.error("Unable to store content", e);
                }
            }
        }

        boolean isAllowAny = template.isAnyFieldAllowed();

        if (fieldRuleMap != null) {
            Set<Field> fields = fieldRuleMap.keySet();
            decryptedSubmissionData = dataFilterService.allSubmissionData(modelProvider, submission, reason);

            task = ModelUtility.task(modelProvider);
            if (task != null)
                decryptedInstanceData = dataFilterService.authorizedInstanceData(modelProvider, fields, version, reason, isAllowAny);

            for (Map.Entry<Field, List<ValidationRule>> entry : fieldRuleMap.entrySet()) {
                Field field = entry.getKey();
                List<ValidationRule> rules = entry.getValue();
                validateField(modelProvider, validationBuilder, field, rules, fieldNames, submissionData, instanceData, decryptedSubmissionData, decryptedInstanceData, onlyAcceptValidInputs, false);
            }
        }

        if (isAllowAny) {
            if (!submissionData.isEmpty()) {
                if (task == null)
                    task = ModelUtility.task(modelProvider);
                if (task != null)
                    decryptedInstanceData = dataFilterService.authorizedInstanceData(modelProvider, Collections.<Field>emptySet(), version, reason, isAllowAny);

                if (decryptedSubmissionData == null)
                    decryptedSubmissionData = dataFilterService.allSubmissionData(modelProvider, submission, reason);

                for (Map.Entry<String, List<Value>> entry : submissionData.entrySet()) {
                    String fieldName = entry.getKey();

                    if (!allFieldNames.contains(fieldName)) {
//                        List<? extends Value> values = entry.getValue();
//                        List<? extends Value> previousValues = instanceData != null ? instanceData.get(fieldName) : null;
//
//                        if (isFile(values, previousValues))
//                            values = ValidationUtility.append(values, previousValues);
//
//                        validationBuilder.formValue(fieldName, values);
                        Field field = new Field.Builder()
                                .name(fieldName)
                                .type(null)
                                .maxInputs(10)
                                .build();

                        validateField(modelProvider, validationBuilder, field, Collections.<ValidationRule>emptyList(), fieldNames, submissionData, instanceData, decryptedSubmissionData, decryptedInstanceData, onlyAcceptValidInputs, true);
                    }
                }
            }
        }

        return validationBuilder.build();
    }

    <P extends ProcessDeploymentProvider> void validateField(P modelProvider,
                                     Validation.Builder validationBuilder, Field field,
                                     List<ValidationRule> rules, Set<String> fieldNames,
                                     Map<String, List<Value>> submissionData,
                                     Map<String, List<Value>> instanceData,
                                     Map<String, List<Value>> decryptedSubmissionData,
                                     Map<String, List<Value>> decryptedInstanceData,
                                     boolean onlyAcceptValidInputs,
                                     boolean anyFieldAllowed) {

        String fieldName = ValidationUtility.fieldName(field, submissionData);

        if (fieldName == null) {
            LOG.warn("Field is missing name " + field.getFieldId());
            return;
        }

        if (rules != null) {
            for (ValidationRule rule : rules) {
                try {
                    if (!onlyAcceptValidInputs) {
                        if (rule.getType() == ValidationRule.ValidationRuleType.REQUIRED)
                            continue;
                        if (rule.getType() == ValidationRule.ValidationRuleType.REQUIRED_IF_NO_PREVIOUS)
                            continue;
                    }
                    rule.evaluate(decryptedSubmissionData, decryptedInstanceData);
                } catch (ValidationRuleException e) {
                    LOG.warn("Invalid input: " + e.getMessage() + " " + e.getRule());

                    validationBuilder.error(rule.getName(), e.getMessage());
                    if (onlyAcceptValidInputs) {
                        fieldNames.remove(rule.getName());
                    }
                }
            }
        }

        if (anyFieldAllowed || fieldNames.contains(fieldName)) {

            List<? extends Value> values = submissionData.get(fieldName);
            List<? extends Value> previousValues = instanceData != null ? instanceData.get(fieldName) : Collections.<Value>emptyList();

            boolean isFileField = false;

            if (field.getType() == null && anyFieldAllowed)
                isFileField = isFile(values, previousValues);
            else if (field.getType() != null)
                isFileField = (field.getType().equals(Constants.FieldTypes.FILE) || field.getType().equals(Constants.FieldTypes.URL));

            if (isFileField) {
                // Need to track how many files have been added in order to respect the max inputs constraint
                int numberOfFiles = 0;
                Map<String, String> existingFileLocations = new HashMap<String, String>();
                Map<String, List<Version>> previousVersionMap = new HashMap<String, List<Version>>();
                // File fields are special -- instead of replacing the values, it's necessary to append them to the end of the previous values
                if (previousValues != null && !previousValues.isEmpty()) {
                    // Obviously these won't have a ContentResource attached, and shouldn't be re-stored to the repository, but then need to be
                    // added to the validation list unless we want them to disappear
                    for (Value previousValue : previousValues) {
                        File file = File.class.cast(previousValue);
                        validationBuilder.formFileValue(fieldName, file);

                        // Track the existing file names so we can treat files of the same name as a new version rather than a new file entirely
                        if (StringUtils.isNotEmpty(file.getName()) && StringUtils.isNotEmpty(file.getLocation())) {
                            existingFileLocations.put(file.getName(), file.getLocation());
                            previousVersionMap.put(file.getName(), file.getVersions());
                        }

                        // Update the number of files, but let's assume that we don't need to check that we're within the max inputs during this
                        // loop since that should have been covered when these files were uploaded
                        numberOfFiles++;
                    }
                }

                if (values != null && !values.isEmpty()) {
                    for (Value value : values) {
                        try {
                            File file = File.class.cast(value);

                            // Don't bother to attach a file if it's a null stream
                            if (file.getContentResource() == null || file.getContentResource().getInputStream() == null)
                                continue;

//                            boolean isReplace = StringUtils.isNotEmpty(file.getName()) && existingFileLocations.containsKey(file.getName());
                            ContentResource contentResource = saveOrReplace(modelProvider, file, existingFileLocations, validationBuilder, field, numberOfFiles);

                            // Need to track these variables, since they're how we find the content resource in the repository again -- only location
                            // really matters for getting the stream, but the id is used when we delete a file
                            if (contentResource != null) {
                                String id = contentResource.getContentId();
                                String location = contentResource.getLocation();

                                List<Version> versions = contentResource.versions();
                                // If the content repository doesn't keep track of versions, then we can
                                // at least track the versions uploaded here
                                if (versions == null || versions.isEmpty()) {
                                    versions = new ArrayList<Version>();
                                    int count = 1;
                                    List<Version> previousVersions = previousVersionMap.get(file.getName());
                                    if (previousVersions != null && !previousVersions.isEmpty()) {
                                        versions.addAll(previousVersions);
                                        count = previousVersions.size() + 1;
                                    }

                                    versions.add(new Version("v" + count, modelProvider.principal().getEntityId(), new Date().getTime(), id, location));
                                }

                                File persisted = new File.Builder(file)
                                        .id(id)
                                        .location(location)
                                        .versions(versions)
                                        .build();

                                validationBuilder.formFileValue(fieldName, persisted);

                                // Update the number of files
                                numberOfFiles++;

                                // Track the existing file names so we can treat files of the same name as a new version rather than a new file entirely
                                // (same idea as in the previous values loop)
                                if (StringUtils.isNotEmpty(file.getName()) && StringUtils.isNotEmpty(file.getLocation()))
                                    existingFileLocations.put(file.getName(), file.getLocation());
                            }

                        } catch (MongoException mongoException) {
                            Throwable cause = mongoException.getCause();
                            if (cause instanceof MaxSizeExceededException) {
                                MaxSizeExceededException sizeExceededException = MaxSizeExceededException.class.cast(cause);
                                LOG.warn("User attempted to upload a stream larger than the max size allowed: " + sizeExceededException.getMaxSize());
                                validationBuilder.error(fieldName, "File must be less than " + sizeExceededException.getMaxSize() + "KB");
                            } else {
                                LOG.error("Failed to store file to mongo", mongoException);
                                validationBuilder.error(fieldName, "Unable to store file. The data store may be down or not responding");
                            }
                            return;
                        } catch (IOException ioe) {
                            LOG.error("Unable to store file. The content management system may be down or not responding", ioe);
                            validationBuilder.error(fieldName, "Unable to store file. The content management system may be down or not responding");
                            return;
                        } catch (BadRequestError be) {
                            LOG.error("Unable to store file. Invalid.", be);
                            validationBuilder.error(fieldName, be.getMessage());
                            return;
                        } catch (PieceworkException pe) {
                            LOG.error("Unable to store file. The content management system may be down or not responding", pe);
                            validationBuilder.error(fieldName, "Unable to store file. The content management system may be down or not responding");
                            return;
                        }
                    }
                }
            } else {
                // If it's not a file, then null values should be set to an empty list
                if (values == null)
                    values = Collections.emptyList();

                validationBuilder.formValue(fieldName, values.toArray(new Value[values.size()]));
            }
        }
    }

    private <P extends ProcessDeploymentProvider> ContentResource saveOrReplace(P modelProvider, File file, Map<String, String> existingFileLocations) throws PieceworkException, IOException {
        return saveOrReplace(modelProvider, file, existingFileLocations, null, null, 0);
    }

    private <P extends ProcessDeploymentProvider> ContentResource saveOrReplace(P modelProvider, File file, Map<String, String> existingFileLocations, Validation.Builder validationBuilder, Field field, int numberOfFiles) throws PieceworkException, IOException {

        boolean isReplace = StringUtils.isNotEmpty(file.getName()) && existingFileLocations.containsKey(file.getName());

        if (isReplace) {
            String location = existingFileLocations.get(file.getName());
            // Can't actually replace if there isn't a location, so let it fall through to become a new file instead,
            // seems like the best option if this ever does happen
            if (StringUtils.isNotEmpty(location)) {
                ContentResource contentResource = contentRepository.replace(modelProvider, file.getContentResource(), location);
                // If it worked, then don't execute the code below, because we've already replaced the file in the repository
                if (contentResource != null)
                    return contentResource;
            }
        }

        // Only need to check the max inputs if this is a new file (not replacing an existing file)
        if (field != null && numberOfFiles >= field.getMaxInputs()) {
            if (LOG.isInfoEnabled())
                LOG.info("Number of files being attached to " + field.getName() + " is greater than " + field.getMaxInputs() + " - no more files will be accepted");
            if (validationBuilder != null)
                validationBuilder.error(field.getName(), "Number of files being uploaded is greater than " + field.getMaxInputs() + " - no more files will be accepted");
            return null;
        }

        return contentRepository.save(modelProvider, file.getContentResource());
    }

    private static boolean isFile(List<? extends Value> values, List<? extends Value> previousValues) {
        return isFile(values) || isFile(previousValues);
    }

    private static boolean isFile(List<? extends Value> values) {
        if (values != null && !values.isEmpty()) {
            Value value = values.iterator().next();
            return value instanceof File;
        }
        return false;
    }

}
