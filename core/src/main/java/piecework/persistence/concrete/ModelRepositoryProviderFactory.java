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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.engine.ProcessEngineFacade;
import piecework.model.Entity;
import piecework.model.FormRequest;
import piecework.model.ProcessInstance;
import piecework.persistence.*;
import piecework.repository.*;
import piecework.security.Sanitizer;
import piecework.service.CacheService;
import piecework.service.IdentityService;
import piecework.settings.ContentSettings;

/**
 * Implementation of the ModelProviderFactory that uses injected repositories to retrieve
 * persisted data from the data store.
 *
 * @author James Renfro
 */
@Service
public class ModelRepositoryProviderFactory implements ModelProviderFactory {

    private static final Logger LOG = Logger.getLogger(ModelRepositoryProviderFactory.class);

    @Autowired
    ActivityRepository activityRepository;

    @Autowired
    AttachmentRepository attachmentRepository;

    @Autowired
    CacheService cacheService;

    @Autowired
    ContentRepository contentRepository;

    @Autowired
    ContentSettings contentSettings;

    @Autowired
    DeploymentRepository deploymentRepository;

    @Autowired
    IdentityService identityService;

    @Autowired
    ProcessEngineFacade facade;

    @Autowired
    ProcessRepository processRepository;

    @Autowired
    ProcessInstanceRepository processInstanceRepository;

    @Autowired
    Sanitizer sanitizer;

    @Override
    public AllowedTaskProvider allowedTaskProvider(String rawProcessDefinitionKey, String rawProcessInstanceId, Entity principal) {
        String processInstanceId = sanitizer.sanitize(rawProcessInstanceId);

        AllowedTaskProvider taskProvider =
                new AllowedTaskRepositoryProvider(processProvider(rawProcessDefinitionKey, principal),
                        processInstanceRepository, facade, attachmentRepository, contentRepository,
                        deploymentRepository, identityService, sanitizer, processInstanceId);
        return taskProvider;
    }

    @Override
    public ProcessProvider processProvider(String rawProcessDefinitionKey, Entity principal) {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        ProcessProvider processProvider = new ProcessRepositoryProvider(processRepository, processDefinitionKey, principal);
        return new CachingProcessProvider(cacheService, processProvider);
    }

    @Override
    public ProcessDeploymentProvider deploymentProvider(String rawProcessDefinitionKey, Entity principal) {
        ProcessDeploymentProvider deploymentProvider = new ProcessDeploymentRepositoryProvider(processProvider(rawProcessDefinitionKey, principal));
        return deploymentProvider;
    }

    @Override
    public ProcessDeploymentProvider deploymentProvider(String rawProcessDefinitionKey, String rawDeploymentId, Entity principal) {
        String deploymentId = sanitizer.sanitize(rawDeploymentId);
        ProcessDeploymentProvider deploymentProvider = new ProcessDeploymentRepositoryProvider(deploymentRepository, processProvider(rawProcessDefinitionKey, principal), deploymentId);
        return deploymentProvider;
    }

    @Override
    public <P extends ProcessDeploymentProvider> P provider(FormRequest request, Entity principal) {
        String processDefinitionKey = request.getProcessDefinitionKey();
        String processInstanceId = request.getProcessInstanceId();
        String taskId = request.getTaskId();

        if (StringUtils.isNotEmpty(taskId))
            return (P)taskProvider(processDefinitionKey, taskId, principal);

        if (StringUtils.isNotEmpty(processInstanceId))
            return (P)instanceProvider(processDefinitionKey, processInstanceId, principal);

        return (P)deploymentProvider(processDefinitionKey, principal);
    }

    @Override
    public HistoryProvider historyProvider(String rawProcessDefinitionKey, String rawProcessInstanceId, Entity principal) {
        String processInstanceId = sanitizer.sanitize(rawProcessInstanceId);

        HistoryProvider historyProvider =
                new HistoryRepositoryProvider(processProvider(rawProcessDefinitionKey, principal),
                        processInstanceRepository, facade, attachmentRepository, contentRepository,
                        deploymentRepository, identityService, processInstanceId);
        return historyProvider;
    }

    public ProcessInstanceProvider instanceProvider(String rawProcessInstanceId, Entity principal) {
        String processInstanceId = sanitizer.sanitize(rawProcessInstanceId);
        ProcessInstance instance = processInstanceRepository.findOne(processInstanceId);
        ProcessProvider processProvider = new ProcessRepositoryProvider(processRepository, instance.getProcessDefinitionKey(), principal);
        ProcessInstanceProvider instanceProvider =
                new ProcessInstanceRepositoryProvider(processProvider,
                        processInstanceRepository, facade, attachmentRepository, contentRepository, deploymentRepository,
                        processInstanceId);
        return instanceProvider;
    }

    @Override
    public ProcessInstanceProvider instanceProvider(String rawProcessDefinitionKey, String rawProcessInstanceId, Entity principal) {
        String processInstanceId = sanitizer.sanitize(rawProcessInstanceId);
        ProcessInstanceProvider instanceProvider =
                new ProcessInstanceRepositoryProvider(processProvider(rawProcessDefinitionKey, principal),
                        processInstanceRepository, facade, attachmentRepository, contentRepository, deploymentRepository,
                        processInstanceId);
        return instanceProvider;
    }

    @Override
    public ContentProfileProvider systemContentProvider(Entity principal) {
        return new SystemContentProfileProvider(contentSettings, principal);
    }

    @Override
    public TaskProvider taskProvider(String rawProcessDefinitionKey, String rawTaskId, Entity principal) {
        String taskId = sanitizer.sanitize(rawTaskId);

        TaskProvider instanceProvider =
                new TaskRepositoryProvider(processProvider(rawProcessDefinitionKey, principal),
                        processInstanceRepository, facade, attachmentRepository, contentRepository,
                        deploymentRepository, identityService, taskId);
        return instanceProvider;
    }

}
