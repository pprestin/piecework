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

import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.persistence.ProcessDeploymentProvider;

/**
 * @author James Renfro
 */
public class ProcessDeploymentProviderStub extends ProcessProviderStub implements ProcessDeploymentProvider {

    private ProcessDeployment deployment;
    private ContentProfile contentProfile;
    private Activity activity;

    public ProcessDeploymentProviderStub() {

    }

    public ProcessDeploymentProviderStub(ContentProfile contentProfile) {
        this.contentProfile = contentProfile;
    }

    public ProcessDeploymentProviderStub(piecework.model.Process process, ProcessDeployment deployment, Entity principal) {
        super(process, principal);
        this.deployment = deployment;
    }

    public ProcessDeploymentProviderStub(piecework.model.Process process, ProcessDeployment deployment, ContentProfile contentProfile, Entity principal) {
        super(process, principal);
        this.deployment = deployment;
        this.contentProfile = contentProfile;
    }


    @Override
    public Activity activity() throws PieceworkException {
        return activity;
    }

    @Override
    public ContentProfile contentProfile() throws PieceworkException {
        if (this.contentProfile != null)
            return contentProfile;
        return deployment != null ? deployment.getContentProfile() : null;
    }

    @Override
    public ProcessDeployment deployment() throws PieceworkException {
        return deployment;
    }

    public ProcessDeploymentProviderStub deployment(ProcessDeployment deployment) {
        this.deployment = deployment;
        return this;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }
}
