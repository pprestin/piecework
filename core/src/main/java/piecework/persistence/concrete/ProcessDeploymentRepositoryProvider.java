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
import piecework.common.ViewContext;
import piecework.exception.InternalServerError;
import piecework.exception.MisconfiguredProcessException;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.persistence.ProcessProvider;
import piecework.repository.DeploymentRepository;

/**
 * @author James Renfro
 */
public class ProcessDeploymentRepositoryProvider implements ProcessDeploymentProvider {

    private final DeploymentRepository deploymentRepository;
    private final ProcessProvider processProvider;
    private final String deploymentId;

    ProcessDeploymentRepositoryProvider(final ProcessProvider processProvider) {
        this(null, processProvider, null);
    }

    public ProcessDeploymentRepositoryProvider(DeploymentRepository deploymentRepository, final ProcessProvider processProvider, final String deploymentId) {
        this.deploymentRepository = deploymentRepository;
        this.processProvider = processProvider;
        this.deploymentId = deploymentId;
    }

    @Override
    public Activity activity() throws PieceworkException {
        ProcessDeployment deployment = deployment();

        Activity activity = null;
        if (deployment == null)
            throw new MisconfiguredProcessException("No deployment found");

        String activityKey = deployment.getStartActivityKey();
        if (activityKey != null)
            activity = deployment.getActivity(activityKey);

        if (activity != null)
            return activity;

        throw new MisconfiguredProcessException("Unable to build activity for process");
    }

    @Override
    public ContentProfile contentProfile() throws PieceworkException {
        ProcessDeployment deployment = deployment();
        ContentProfile contentProfile;
        if (deployment != null && deployment.getContentProfile() != null)
            contentProfile = deployment.getContentProfile();
        else
            contentProfile = new ContentProfile.Builder().build();

        return contentProfile;
    }

    @Override
    public Process process() throws PieceworkException {
        return processProvider.process();
    }

    @Override
    public Process process(ViewContext context) throws PieceworkException {
        return processProvider.process(context);
    }

    @Override
    public ProcessDeployment deployment() throws PieceworkException {
        if (StringUtils.isNotEmpty(deploymentId)) {
            if (deploymentRepository == null)
                throw new InternalServerError();

            return deploymentRepository.findOne(deploymentId);
        }

        Process process = process();
        ProcessDeployment deployment = process.getDeployment();

//        if (deployment == null)
//           throw new MisconfiguredProcessException("No deployment can be found for this process: " + process.getProcessDefinitionKey());

        return deployment;
    }

    @Override
    public Entity principal() {
        return processProvider.principal();
    }

    @Override
    public String processDefinitionKey() {
        return processProvider.processDefinitionKey();
    }

}
