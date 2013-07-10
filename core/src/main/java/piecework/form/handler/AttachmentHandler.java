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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import piecework.model.User;
import piecework.common.ViewContext;
import piecework.exception.StatusCodeError;
import piecework.identity.InternalUserDetailsService;
import piecework.model.*;
import piecework.persistence.ContentRepository;
import piecework.persistence.ProcessInstanceRepository;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.ui.StreamingAttachmentContent;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
@Service
public class AttachmentHandler {

    @Autowired
    ProcessInstanceRepository processInstanceRepository;

    @Autowired
    InternalUserDetailsService userDetailsService;

    @Autowired
    ContentRepository contentRepository;

    public Response handle(FormRequest formRequest, ViewContext viewContext, String attachmentId) throws StatusCodeError {
        ProcessInstance processInstance = processInstanceRepository.findOne(formRequest.getProcessInstanceId());

        Attachments.Builder attachmentsBuilder = new Attachments.Builder();
        Map<String, User> userMap = new HashMap<String, User>();
        List<Attachment> storedAttachments = processInstance.getAttachments();
        if (storedAttachments != null && !storedAttachments.isEmpty()) {
            PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
            for (Attachment storedAttachment : storedAttachments) {
                String userId = storedAttachment.getUserId();
                User user = userId != null ? userMap.get(userId) : null;
                if (user == null && userId != null) {
                    UserDetails userDetails = userDetailsService.loadUserByInternalId(userId);
                    user = new User.Builder(userDetails).build();

                    if (user != null)
                        userMap.put(user.getUserId(), user);
                }

                if (attachmentId == null)
                    attachmentsBuilder.attachment(new Attachment.Builder(storedAttachment, passthroughSanitizer).processDefinitionKey(processInstance.getProcessDefinitionKey()).requestId(formRequest.getRequestId()).user(user).build(viewContext));
                else if (attachmentId.equals(storedAttachment.getAttachmentId())) {
                    Content content = contentRepository.findByLocation(storedAttachment.getLocation());
                    String contentDisposition = new StringBuilder("attachment; filename=").append(storedAttachment.getDescription()).toString();
                    return Response.ok(new StreamingAttachmentContent(storedAttachment, content), storedAttachment.getContentType()).header("Content-Disposition", contentDisposition).build();
                }
            }
        }

        return Response.ok(attachmentsBuilder.build()).build();
    }

}
