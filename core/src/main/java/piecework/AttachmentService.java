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
package piecework;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import piecework.exception.InternalServerError;
import piecework.exception.StatusCodeError;
import piecework.process.AttachmentQueryParameters;
import piecework.service.ProcessInstanceService;
import piecework.service.ValidationService;
import piecework.validation.FormValidation;
import piecework.validation.SubmissionTemplate;
import piecework.service.IdentityService;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ContentRepository;
import piecework.persistence.ProcessInstanceRepository;
import piecework.security.Sanitizer;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.ui.StreamingAttachmentContent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author James Renfro
 */
@Service
public class AttachmentService {

    @Autowired
    ContentRepository contentRepository;

    @Autowired
    Environment environment;

    @Autowired
    IdentityService identityService;

    @Autowired
    MongoTemplate mongoOperations;

    @Autowired
    ProcessInstanceRepository processInstanceRepository;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    ValidationService validationService;

    @Autowired
    Versions versions;

    public ProcessInstance attach(Process process, ProcessInstance instance, Task task, SubmissionTemplate template, Submission submission) throws StatusCodeError {
        FormValidation validation = validationService.validate(process, instance, task, template, submission, false);

        return processInstanceService.store(process, submission, validation, instance, true);
    }

    public StreamingAttachmentContent content(Process process, ProcessInstance processInstance, String attachmentId) {

        Set<Attachment> storedAttachments = processInstance.getAttachments();
        if (storedAttachments != null && !storedAttachments.isEmpty()) {
            for (Attachment storedAttachment : storedAttachments) {
                if (StringUtils.isEmpty(attachmentId) || StringUtils.isEmpty(storedAttachment.getAttachmentId()) || !attachmentId.equals(storedAttachment.getAttachmentId()))
                    continue;

                Content content = contentRepository.findByLocation(storedAttachment.getLocation());
                if (content != null)
                    return new StreamingAttachmentContent(storedAttachment, content);
            }
        }

        return null;
    }

    public void delete(Process process, ProcessInstance instance, String attachmentId) throws StatusCodeError {
        if (instance == null)
            throw new InternalServerError();

        boolean skipOptimization = environment.getProperty(Constants.Settings.OPTIMIZATIONS_OFF, Boolean.class, Boolean.FALSE);

        if (skipOptimization) {
            ProcessInstance.Builder builder = new ProcessInstance.Builder(instance);
            builder.removeAttachment(attachmentId);

            processInstanceRepository.save(builder.build());
        } else {
            Query query = new Query(where("_id").is(instance.getProcessInstanceId()));
            Update update = new Update();

            if (attachmentId != null)
                update.pull("attachments", attachmentId);

            mongoOperations.updateFirst(query, update, ProcessInstance.class);
        }
    }

    public SearchResults search(piecework.model.Process process, ProcessInstance processInstance, AttachmentQueryParameters queryParameters) throws StatusCodeError {
        String paramName = sanitizer.sanitize(queryParameters.getName());
        String paramContentType = sanitizer.sanitize(queryParameters.getContentType());
        String paramUserId = sanitizer.sanitize(queryParameters.getUserId());

        SearchResults.Builder searchResultsBuilder = new SearchResults.Builder();

        int count = 0;
        Map<String, User> userMap = new HashMap<String, User>();
        Set<Attachment> storedAttachments = processInstance.getAttachments();
        if (storedAttachments != null && !storedAttachments.isEmpty()) {
            PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
            for (Attachment storedAttachment : storedAttachments) {
                if (storedAttachment.isFieldAttachment())
                    continue;

                String userId = storedAttachment.getUserId();

                if (StringUtils.isNotEmpty(paramName) && (StringUtils.isEmpty(storedAttachment.getName()) || !paramName.equals(storedAttachment.getName())))
                    continue;

                if (StringUtils.isNotEmpty(paramContentType) && (StringUtils.isEmpty(storedAttachment.getContentType()) || !paramContentType.equals(storedAttachment.getContentType())))
                    continue;

                if (StringUtils.isNotEmpty(paramUserId) && (StringUtils.isEmpty(userId) || !paramUserId.equals(userId)))
                    continue;

                User user = userId != null ? userMap.get(userId) : null;
                if (user == null && userId != null) {
                    user = identityService.getUser(userId);

                    if (user != null)
                        userMap.put(user.getUserId(), user);
                }

                searchResultsBuilder.item(new Attachment.Builder(storedAttachment, passthroughSanitizer)
                        .processDefinitionKey(processInstance.getProcessDefinitionKey())
                        .processInstanceId(processInstance.getProcessInstanceId())
                        .user(user)
                        .build(versions.getVersion1()));

                count++;
            }
        }

        searchResultsBuilder.firstResult(0);
        searchResultsBuilder.maxResults(count);
        searchResultsBuilder.total(Long.valueOf(count));

        return searchResultsBuilder.build();
    }
}
