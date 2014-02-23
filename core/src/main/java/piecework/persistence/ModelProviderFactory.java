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
package piecework.persistence;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;
import piecework.enumeration.CacheName;
import piecework.exception.GoneError;
import piecework.exception.NotFoundError;
import piecework.exception.StatusCodeError;
import piecework.model.*;
import piecework.model.Process;
import piecework.repository.ActivityRepository;
import piecework.repository.DeploymentRepository;
import piecework.repository.ProcessInstanceRepository;
import piecework.repository.ProcessRepository;
import piecework.security.Sanitizer;
import piecework.service.CacheService;

/**
 * @author James Renfro
 */
public interface ModelProviderFactory {

    AllowedTaskProvider allowedTaskProvider(String processDefinitionKey, String processInstanceId, Entity principal);

    ProcessProvider processProvider(String processDefinitionKey, Entity principal);

    ProcessDeploymentProvider deploymentProvider(String processDefinitionKey, Entity principal);

    HistoryProvider historyProvider(String processDefinitionKey, String processInstanceId, Entity principal);

    ProcessInstanceProvider instanceProvider(String processInstanceId, Entity principal);

    ProcessInstanceProvider instanceProvider(String processDefinitionKey, String processInstanceId, Entity principal);

    TaskProvider taskProvider(String processDefinitionKey, String taskId, Entity principal);

    <P extends ProcessDeploymentProvider> P provider(FormRequest request, Entity principal);

}
