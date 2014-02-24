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
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.persistence.AllowedTaskProvider;
import piecework.persistence.TaskProvider;
import piecework.process.AttachmentQueryParameters;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.ui.streaming.StreamingAttachmentContent;

/**
 * @author James Renfro
 */
public class TaskProviderStub extends ProcessInstanceProviderStub implements TaskProvider {


    private Task task;

    public TaskProviderStub(piecework.model.Process process, ProcessDeployment deployment,
                                   ProcessInstance instance, Task task, Entity principal) {
        super(process, deployment, instance, principal);
        this.task = task;
    }

    @Override
    public Task task() throws PieceworkException {
        return task;
    }

    @Override
    public Task task(ViewContext context, boolean limitToActive) throws PieceworkException {
        return new Task.Builder(task, new PassthroughSanitizer()).build(context);
    }

    public TaskProviderStub task(Task task) {
        this.task = task;
        return this;
    }

}
