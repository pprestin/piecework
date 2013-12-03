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
package piecework.command;

import piecework.Command;
import piecework.CommandExecutor;
import piecework.Constants;
import piecework.exception.StatusCodeError;
import piecework.model.ProcessDeployment;
import piecework.model.ProcessInstance;
import piecework.model.Value;
import piecework.persistence.DeploymentRepository;
import piecework.persistence.ProcessInstanceRepository;
import piecework.security.DataFilterService;

import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
public class CompletionCommand implements Command<ProcessInstance> {

    private final ProcessInstance instance;

    public CompletionCommand(ProcessInstance instance) {
        this.instance = instance;
    }

    @Override
    public ProcessInstance execute(CommandExecutor commandExecutor) throws StatusCodeError {
        if (instance != null) {
            DataFilterService dataFilterService = commandExecutor.getDataFilterService();
            DeploymentRepository deploymentRepository = commandExecutor.getDeploymentRepository();
            ProcessInstanceRepository processInstanceRepository = commandExecutor.getProcessInstanceRepository();

            String deploymentId = instance.getDeploymentId();
            ProcessDeployment deployment = deploymentRepository.findOne(deploymentId);
            String completionStatus = null;
            if (deployment != null) {
                completionStatus = deployment.getCompletionStatus();
            }
            // Exclude all restricted data from final storage - i.e. all restricted data is purged from the data store when a process completes
            Map<String, List<Value>> data = dataFilterService.exclude(instance.getData());
            return processInstanceRepository.update(instance.getProcessInstanceId(), Constants.ProcessStatuses.COMPLETE, completionStatus, data);
        }
        return null;
    }

    @Override
    public String getProcessDefinitionKey() {
        return instance.getProcessDefinitionKey();
    }
}
