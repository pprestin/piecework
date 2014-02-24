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
package piecework.form;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import piecework.Versions;
import piecework.common.ViewContext;
import piecework.enumeration.ActionType;
import piecework.exception.FormBuildingException;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.persistence.ProcessInstanceProvider;
import piecework.persistence.test.ProcessDeploymentProviderStub;
import piecework.persistence.test.ProcessInstanceProviderStub;
import piecework.security.AccessTracker;
import piecework.security.data.DataFilterService;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class FormFactoryTest {

    @InjectMocks
    FormFactory formFactory;

    @Mock
    AccessTracker accessTracker;

    @Mock
    DataFilterService dataFilterService;

    @Mock
    Process process;

    @Mock
    ProcessInstance instance;

    @Mock
    ProcessDeployment deployment;

    @Mock
    Versions versions;

    @Mock
    Entity user;

    @Test
    public void testFormInitial() throws PieceworkException {
        Mockito.when(process.getProcessDefinitionKey())
               .thenReturn("TESTPROCESS1");
        Mockito.when(instance.getProcessDefinitionKey())
                .thenReturn("TESTPROCESS1");
        Mockito.when(instance.getProcessInstanceId())
                .thenReturn("987");
        Mockito.when(versions.getVersion1())
                .thenReturn(new ViewContext("http://localhost", "/static", "/api/v1", "/public", "v1"));

        FormRequest request = new FormRequest.Builder()
                .requestId("123")
                .processDefinitionKey("TESTPROCESS1")
                .processInstanceId("987").build();

        ProcessInstanceProvider deploymentProvider = new ProcessInstanceProviderStub(process, deployment, instance, user);

        Form form = formFactory.form(deploymentProvider, request, ActionType.CREATE, null, null, false, false, "v1");

        Assert.assertNotNull(form);
        Assert.assertEquals("123", form.getFormInstanceId());
        Assert.assertEquals("TESTPROCESS1", form.getProcess().getProcessDefinitionKey());
        Assert.assertEquals("987", form.getProcessInstanceId());
    }

}
