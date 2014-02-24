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

    public ProcessDeploymentProviderStub() {

    }

    public ProcessDeploymentProviderStub(piecework.model.Process process, ProcessDeployment deployment, Entity principal) {
        super(process, principal);
        this.deployment = deployment;
    }

    @Override
    public ProcessDeployment deployment() throws PieceworkException {
        return deployment;
    }

    public ProcessDeploymentProviderStub deployment(ProcessDeployment deployment) {
        this.deployment = deployment;
        return this;
    }

}
