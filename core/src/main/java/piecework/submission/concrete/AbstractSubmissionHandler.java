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
package piecework.submission.concrete;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import piecework.exception.MisconfiguredProcessException;
import piecework.exception.PieceworkException;
import piecework.exception.StatusCodeError;
import piecework.model.Entity;
import piecework.model.Submission;
import piecework.persistence.ActivityRepository;
import piecework.persistence.SubmissionRepository;
import piecework.security.Sanitizer;
import piecework.service.IdentityService;
import piecework.service.SubmissionStorageService;
import piecework.submission.SubmissionHandler;
import piecework.submission.SubmissionTemplate;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Date;

/**
 * @author James Renfro
 */
public abstract class AbstractSubmissionHandler<T> implements SubmissionHandler<T> {

    private static final Logger LOG = Logger.getLogger(AbstractSubmissionHandler.class);

    @Autowired
    ActivityRepository activityRepository;

    @Autowired
    IdentityService identityService;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    SubmissionRepository submissionRepository;

    @Autowired
    SubmissionStorageService submissionStorageService;

    @Override
    public Submission handle(T data, SubmissionTemplate template, Entity principal) throws PieceworkException {
        Submission submission = handleInternal(data, template, principal);
        return submissionRepository.save(submission);
    }

    protected abstract Submission handleInternal(T data, SubmissionTemplate template, Entity principal) throws PieceworkException;

    protected void handlePlaintext(SubmissionTemplate template, Submission.Builder submissionBuilder, org.apache.cxf.jaxrs.ext.multipart.Attachment attachment, String userId) throws MisconfiguredProcessException, StatusCodeError {
        String contentType = MediaType.TEXT_PLAIN;
        if (LOG.isDebugEnabled())
            LOG.debug("Processing multipart with content type " + contentType + " and content id " + attachment.getContentId());

        String name = sanitizer.sanitize(attachment.getDataHandler().getName());
        String value = sanitizer.sanitize(attachment.getObject(String.class));

        if (!submissionStorageService.store(template, submissionBuilder, name, value, userId)) {
            LOG.warn("Submission included field (" + name + ") that is not acceptable, and no attachments are allowed for this template");
        }
    }

    protected void handleAllContentTypes(SubmissionTemplate template, Submission.Builder submissionBuilder, org.apache.cxf.jaxrs.ext.multipart.Attachment attachment, String userId) throws MisconfiguredProcessException, StatusCodeError {
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
                    if (!submissionStorageService.store(template, submissionBuilder, name, filename, userId, attachment.getDataHandler().getInputStream(), contentType)) {
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

    protected Submission.Builder submissionBuilder(SubmissionTemplate template, Entity principal) {
        return submissionBuilder(template, principal, null);
    }

    protected Submission.Builder submissionBuilder(SubmissionTemplate template, Entity principal, Submission rawSubmission) {
        String principalId = principal != null ? principal.getEntityId() : "anonymous";

        String submitterId = principalId;
        if (principal.getEntityType() == Entity.EntityType.SYSTEM && StringUtils.isNotEmpty(template.getActAsUser()))
            submitterId = template.getActAsUser();
        else if (rawSubmission != null && StringUtils.isNotEmpty(rawSubmission.getSubmitterId()))
            submitterId = sanitizer.sanitize(rawSubmission.getSubmitterId());

        Submission.Builder submissionBuilder;

        if (rawSubmission != null)
            submissionBuilder = new Submission.Builder(rawSubmission, sanitizer, true);
        else
            submissionBuilder = new Submission.Builder();

        submissionBuilder.processDefinitionKey(template.getProcess().getProcessDefinitionKey())
                .requestId(template.getRequestId())
                .taskId(template.getTaskId())
                .submissionDate(new Date())
                .submitter(identityService.getUser(submitterId));

        return submissionBuilder;
    }


}
