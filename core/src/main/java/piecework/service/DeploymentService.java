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
import org.springframework.stereotype.Service;
import piecework.exception.MisconfiguredProcessException;
import piecework.model.Process;
import piecework.model.ProcessDeployment;
import piecework.model.ProcessInstance;
import piecework.persistence.DeploymentRepository;

/**
 * @author James Renfro
 */
@Service
public class DeploymentService {

    private static final Logger LOG = Logger.getLogger(DeploymentService.class);

    @Autowired
    DeploymentRepository deploymentRepository;

    public ProcessDeployment read(Process process, ProcessInstance instance) throws MisconfiguredProcessException {
        // Make sure we have a process
        if (process == null)
            throw new MisconfiguredProcessException("No process available, cannot look for deployment");

        // Always use the current deployment when starting a new execution, but for executions
        // that are already running (that have an instance) it may be necessary to retrieve them
        // on an ad-hoc basis
        ProcessDeployment deployment = process.getDeployment();

        // Various other sanity checks
        if (deployment == null || StringUtils.isEmpty(deployment.getDeploymentId()))
            throw new MisconfiguredProcessException("No deployment or deployment id is empty", process.getProcessDefinitionKey());

        // It's okay not to have an instance here, but if we have one, it needs to have a deployment id
        if (instance != null && StringUtils.isEmpty(instance.getDeploymentId()))
            throw new MisconfiguredProcessException("Instance deployment id is empty", process.getProcessDefinitionKey());

        if (!useCurrentDeployment(instance, deployment)) {
            String deploymentId = instance.getDeploymentId();
            return deploymentRepository.findOne(deploymentId);
        }
        return deployment;
    }

    private boolean useCurrentDeployment(ProcessInstance instance, ProcessDeployment deployment) {
        // If instance is null then we have no choice
        if (instance == null)
            return true;

        // If the instance has a deployment id and it's the same as the one passed then it means this instance is on the current deployment
        String deploymentId = instance.getDeploymentId();
        return deploymentId.equals(deployment.getDeploymentId());
    }

}
