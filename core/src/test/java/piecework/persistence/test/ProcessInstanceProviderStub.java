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
import piecework.content.ContentResource;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.persistence.ProcessInstanceProvider;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.ui.Streamable;

/**
 * @author James Renfro
 */
public class ProcessInstanceProviderStub extends ProcessDeploymentProviderStub implements ProcessInstanceProvider {

    private ProcessInstance instance;

    public ProcessInstanceProviderStub() {

    }

    public ProcessInstanceProviderStub(piecework.model.Process process, ProcessDeployment deployment, ProcessInstance instance, Entity principal) {
        super(process, deployment, principal);
        this.instance = instance;
    }

    @Override
    public ContentResource diagram() throws PieceworkException {
        return null;
    }

    @Override
    public ProcessInstance instance() throws PieceworkException {
        return instance;
    }

    @Override
    public ProcessInstance instance(ViewContext context) throws PieceworkException {
        return new ProcessInstance.Builder(instance).build(context);
    }

    public ProcessInstanceProviderStub instance(ProcessInstance instance) {
        this.instance = instance;
        return this;
    }

}
