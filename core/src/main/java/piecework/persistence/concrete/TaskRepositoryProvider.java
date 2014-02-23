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

import piecework.common.ViewContext;
import piecework.engine.ProcessEngineFacade;
import piecework.exception.PieceworkException;
import piecework.model.Process;
import piecework.model.ProcessInstance;
import piecework.model.Task;
import piecework.persistence.ProcessProvider;
import piecework.persistence.TaskProvider;
import piecework.repository.AttachmentRepository;
import piecework.repository.ContentRepository;
import piecework.repository.DeploymentRepository;
import piecework.repository.ProcessInstanceRepository;
import piecework.service.IdentityService;
import piecework.util.TaskUtility;

/**
 * @author James Renfro
 */
public class TaskRepositoryProvider extends ProcessInstanceRepositoryProvider implements TaskProvider {

    private final IdentityService identityService;
    private final String taskId;

    TaskRepositoryProvider(ProcessProvider processProvider, ProcessInstanceRepository processInstanceRepository,
                           ProcessEngineFacade facade, AttachmentRepository attachmentRepository, ContentRepository contentRepository, DeploymentRepository deploymentRepository,
                           IdentityService identityService, String taskId) {

        super(processProvider, processInstanceRepository, facade, attachmentRepository, contentRepository, deploymentRepository, null);
        this.identityService = identityService;
        this.taskId = taskId;
    }

    @Override
    public Task task() throws PieceworkException {
        return task(null, false);
    }

    @Override
    public synchronized ProcessInstance instance(ViewContext context) throws PieceworkException {
        Process process = process();
        ProcessInstance instance = processInstanceRepository.findByTaskId(process.getProcessDefinitionKey(), taskId);

        if (instance == null)
            return null;

        setInstance(instance);
        return instance;
    }

    @Override
    public synchronized Task task(ViewContext context, boolean limitToActive) throws PieceworkException {
        return TaskUtility.findTask(identityService, process(), instance(), taskId, principal(), limitToActive, context);
    }

}