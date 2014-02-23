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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import piecework.Versions;
import piecework.exception.StatusCodeError;
import piecework.identity.IdentityHelper;
import piecework.repository.AttachmentRepository;
import piecework.process.AttachmentQueryParameters;
import piecework.validation.ValidationFactory;
import piecework.model.*;
import piecework.model.Process;
import piecework.repository.ContentRepository;
import piecework.security.Sanitizer;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.ui.streaming.StreamingAttachmentContent;

import java.util.*;

/**
 * @author James Renfro
 */
@Service
public class AttachmentService {

    private static final Logger LOG = Logger.getLogger(AttachmentService.class);

    @Autowired
    AttachmentRepository attachmentRepository;

    @Autowired
    ContentRepository contentRepository;

    @Autowired
    Environment environment;

    @Autowired
    IdentityHelper helper;

    @Autowired
    IdentityService identityService;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    ValidationFactory validationFactory;

    @Autowired
    Versions versions;

    public StreamingAttachmentContent content(Process process, ProcessInstance processInstance, String attachmentId, Entity principal) {

        Set<Attachment> storedAttachments = processInstance.getAttachments();
        if (storedAttachments != null && !storedAttachments.isEmpty()) {
            for (Attachment storedAttachment : storedAttachments) {
                if (StringUtils.isEmpty(attachmentId) || StringUtils.isEmpty(storedAttachment.getAttachmentId()) || !attachmentId.equals(storedAttachment.getAttachmentId()))
                    continue;

                Content content = contentRepository.findByLocation(process, storedAttachment.getLocation(), principal);
                if (content != null)
                    return new StreamingAttachmentContent(storedAttachment, content);
            }
        }

        return null;
    }


}
