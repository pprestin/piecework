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

import piecework.exception.PieceworkException;
import piecework.model.Entity;
import piecework.model.FormRequest;

/**
 * @author James Renfro
 */
public interface ModelProviderFactory {

    AllowedTaskProvider allowedTaskProvider(String processDefinitionKey, String processInstanceId, Entity principal);

    ContentProfileProvider systemContentProvider(Entity principal);

    ProcessProvider processProvider(String processDefinitionKey, Entity principal);

    ProcessDeploymentProvider deploymentProvider(String processDefinitionKey, Entity principal);

    ProcessDeploymentProvider deploymentProvider(String processDefinitionKey, String deploymentId, Entity principal);

    HistoryProvider historyProvider(String processDefinitionKey, String processInstanceId, Entity principal);

    ProcessInstanceProvider instanceProvider(String processInstanceId, Entity principal);

    ProcessInstanceProvider instanceProvider(String processDefinitionKey, String processInstanceId, Entity principal);

    TaskProvider taskProvider(String processDefinitionKey, String taskId, Entity principal);

    <P extends ProcessDeploymentProvider> P provider(FormRequest request, Entity principal);

    SearchProvider searchProvider(Entity principal);

}
