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

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import piecework.exception.PieceworkException;
import piecework.model.Entity;
import piecework.model.ProcessInstance;
import piecework.model.Submission;
import piecework.persistence.ContentProfileProvider;
import piecework.persistence.ProcessProvider;
import piecework.submission.SubmissionTemplate;
import piecework.util.ModelUtility;

import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * @author James Renfro
 */
@Service
public class MultipartSubmissionHandler extends AbstractSubmissionHandler<MultipartBody> {

    private static final Logger LOG = Logger.getLogger(MultipartSubmissionHandler.class);

    @Override
    protected Submission handleInternal(ContentProfileProvider modelProvider, MultipartBody body, SubmissionTemplate template) throws PieceworkException {
        Entity principal = modelProvider.principal();
        ProcessInstance instance = ModelUtility.instance(modelProvider);

        String actingAsId = principal != null ? principal.getActingAsId() : "anonymous";
        Submission.Builder submissionBuilder = submissionBuilder(instance, template, principal);
        List<Attachment> attachments = body != null ? body.getAllAttachments() : null;
        if (attachments != null && !attachments.isEmpty()) {
            if (LOG.isDebugEnabled())
                LOG.debug("Handling " + attachments.size() + " multipart attachments");
            for (org.apache.cxf.jaxrs.ext.multipart.Attachment attachment : attachments) {
                MediaType mediaType = attachment.getContentType();

                // Don't process if there's no content type
                if (mediaType == null)
                    continue;

                handleAllContentTypes(modelProvider, template, submissionBuilder, attachment, actingAsId);
            }
        }
        return submissionBuilder.build();
    }

    @Override
    public Class<MultipartBody> getType() {
        return MultipartBody.class;
    }

}
