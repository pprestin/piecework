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
package piecework.persistence.test;

import piecework.common.ViewContext;
import piecework.content.ContentResource;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.persistence.AllowedTaskProvider;
import piecework.process.AttachmentQueryParameters;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.ui.streaming.StreamingResource;

/**
 * @author James Renfro
 */
public class AllowedTaskProviderStub extends ProcessInstanceProviderStub implements AllowedTaskProvider {


    private Task allowedTask;
    private ContentResource attachment;

    public AllowedTaskProviderStub(piecework.model.Process process, ProcessDeployment deployment,
                                   ProcessInstance instance, Task allowedTask, Entity principal) {
        super(process, deployment, instance, principal);
        this.allowedTask = allowedTask;
    }

    @Override
    public Task allowedTask(boolean limitToActive) throws PieceworkException {
        return allowedTask;
    }

    @Override
    public Task allowedTask(ViewContext context, boolean limitToActive) throws PieceworkException {
        return new Task.Builder(allowedTask, new PassthroughSanitizer()).build(context);
    }

    @Override
    public ContentResource attachment(String attachmentId) throws PieceworkException {
        return attachment;
    }

    @Override
    public SearchResults attachments(AttachmentQueryParameters queryParameters, ViewContext context) throws PieceworkException {
        return null;
    }

    @Override
    public ContentResource value(String fieldName, String fileId, long millis) throws PieceworkException {
        return null;
    }

    @Override
    public SearchResults values(String fieldName, ViewContext context) throws PieceworkException {
        return null;
    }

    public AllowedTaskProviderStub allowedTask(Task allowedTask) {
        this.allowedTask = allowedTask;
        return this;
    }

    public AllowedTaskProviderStub attachment(ContentResource attachment) {
        this.attachment = attachment;
        return this;
    }
}
