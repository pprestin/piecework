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
package piecework.persistence.concrete;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import piecework.Constants;
import piecework.common.ViewContext;
import piecework.engine.ProcessEngineFacade;
import piecework.exception.ForbiddenError;
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.AllowedTaskProvider;
import piecework.persistence.ProcessProvider;
import piecework.process.AttachmentQueryParameters;
import piecework.repository.AttachmentRepository;
import piecework.repository.ContentRepository;
import piecework.repository.DeploymentRepository;
import piecework.repository.ProcessInstanceRepository;
import piecework.security.Sanitizer;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.service.IdentityService;
import piecework.ui.streaming.StreamingAttachmentContent;
import piecework.util.Base64Utility;
import piecework.util.ProcessInstanceUtility;
import piecework.util.TaskUtility;

import java.util.*;

/**
 * @author James Renfro
 */
public class AllowedTaskRepositoryProvider extends ProcessInstanceRepositoryProvider implements AllowedTaskProvider {

    private static final Logger LOG = Logger.getLogger(AllowedTaskRepositoryProvider.class);

    private final IdentityService identityService;
    private final Sanitizer sanitizer;

    AllowedTaskRepositoryProvider(ProcessProvider processProvider, ProcessInstanceRepository processInstanceRepository,
                                  ProcessEngineFacade facade, AttachmentRepository attachmentRepository, ContentRepository contentRepository, DeploymentRepository deploymentRepository,
                                  IdentityService identityService, Sanitizer sanitizer, String processInstanceId) {

        super(processProvider, processInstanceRepository, facade, attachmentRepository, contentRepository, deploymentRepository, processInstanceId);
        this.identityService = identityService;
        this.sanitizer = sanitizer;
    }

    @Override
    public Task allowedTask(boolean limitToActive) throws PieceworkException {
        return TaskUtility.findTask(identityService, process(), instance(), null, principal(), limitToActive, null);
    }

    @Override
    public Task allowedTask(ViewContext context, boolean limitToActive) throws PieceworkException {
        return TaskUtility.findTask(identityService, process(), instance(), null, principal(), limitToActive, context);
    }

    @Override
    public StreamingAttachmentContent attachment(String attachmentId) throws PieceworkException {
        Task allowedTask = allowedTask(false);
        if (allowedTask == null)
            throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);

        Process process = process();
        ProcessInstance instance = instance(new ViewContext());

        Set<Attachment> storedAttachments = instance.getAttachments();
        if (storedAttachments != null && !storedAttachments.isEmpty()) {
            for (Attachment storedAttachment : storedAttachments) {
                if (StringUtils.isEmpty(attachmentId) || StringUtils.isEmpty(storedAttachment.getAttachmentId()) || !attachmentId.equals(storedAttachment.getAttachmentId()))
                    continue;

                Content content = contentRepository.findByLocation(process, storedAttachment.getLocation(), principal());
                if (content != null)
                    return new StreamingAttachmentContent(storedAttachment, content);
            }
        }

        return null;
    }

    @Override
    public SearchResults attachments(AttachmentQueryParameters queryParameters, ViewContext context) throws PieceworkException {
        Task allowedTask = allowedTask(false);
        if (allowedTask == null)
            throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);

        ProcessInstance processInstance = instance();

        String paramName = sanitizer.sanitize(queryParameters.getName());
        String paramContentType = sanitizer.sanitize(queryParameters.getContentType());
        String paramUserId = sanitizer.sanitize(queryParameters.getUserId());

        SearchResults.Builder searchResultsBuilder = new SearchResults.Builder();

        int count = 0;
        Map<String, User> userMap = new HashMap<String, User>();
        Set<Attachment> storedAttachments = processInstance.getAttachments();
        Set<String> attachmentIds = processInstance.getAttachmentIds();
        if ((attachmentIds != null || !attachmentIds.isEmpty()) && (storedAttachments == null || storedAttachments.isEmpty())) {
            storedAttachments = new TreeSet<Attachment>();
            if (attachmentIds != null && !attachmentIds.isEmpty()) {
                if (LOG.isDebugEnabled())
                    LOG.debug("Retrieving all attachments for instance " + processInstance.getProcessInstanceId());

                Iterable<Attachment> attachments = attachmentRepository.findAll(attachmentIds);

                for (Attachment attachment : attachments) {
                    storedAttachments.add(new Attachment.Builder(attachment).build(context));
                }
            }
        }

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
                        .build(context));

                count++;
            }
        }

        searchResultsBuilder.firstResult(0);
        searchResultsBuilder.maxResults(count);
        searchResultsBuilder.total(Long.valueOf(count));

        return searchResultsBuilder.build();
    }

    @Override
    public StreamingAttachmentContent value(String fieldName, String fileId) throws PieceworkException {
        Task allowedTask = allowedTask(false);
        if (allowedTask == null)
            throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);

        Process process = process();
        ProcessInstance instance = instance();

        Map<String, List<Value>> data = instance.getData();
        Value value = ProcessInstanceUtility.firstMatchingFileOrLink(fieldName, data, fileId);

        if (value != null) {
            if (value instanceof File) {
                File file = File.class.cast(value);
                Content content = contentRepository.findByLocation(process, file.getLocation(), principal());
                if (content != null)
                    return new StreamingAttachmentContent(null, content);
            }
        }

        throw new NotFoundError();
    }

    @Override
    public SearchResults values(String fieldName, ViewContext context) throws PieceworkException {
        Task allowedTask = allowedTask(false);
        if (allowedTask == null)
            throw new ForbiddenError(Constants.ExceptionCodes.insufficient_permission);

        ProcessInstance instance = instance();
        Map<String, List<Value>> data = instance.getData();
        List<? extends Value> values = fieldName != null ? data.get(fieldName) : null;

        List<Value> files = new ArrayList<Value>();
        if (values != null && !values.isEmpty()) {
            for (Value value : values) {
                if (value == null)
                    continue;

                if (value instanceof File) {
                    File file = File.class.cast(value);
                    files.add(new File.Builder().processDefinitionKey(processDefinitionKey()).processInstanceId(instance.getProcessInstanceId()).fieldName(fieldName).name(file.getName()).id(file.getId()).contentType(file.getContentType()).description(file.getDescription()).build(context));
                } else {
                    String link = value.getValue();
                    String id = Base64Utility.safeBase64(link);
                    if (link != null)
                        files.add(new File.Builder().processDefinitionKey(processDefinitionKey()).processInstanceId(instance.getProcessInstanceId()).fieldName(fieldName).name(link).id(id).contentType("text/url").build(context));
                }
            }
        }
        return new SearchResults.Builder()
                .items(files)
                .build();
    }
}
