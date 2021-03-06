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
package piecework.util;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import piecework.common.ViewContext;
import piecework.enumeration.ActionType;
import piecework.enumeration.DataInjectionStrategy;
import piecework.form.FormDisposition;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.persistence.test.ProcessDeploymentProviderStub;

import java.net.URI;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class FormUtilityTest {

    @Mock
    Process process;

    @Mock
    ProcessDeployment deployment;

    @Test
    public void testDispositionNoFormBuilderNoTaskNoMediaType() throws Exception {
        Mockito.doReturn("TEST")
               .when(process).getProcessDefinitionKey();
        Mockito.doReturn("http://piecework.test")
               .when(deployment).getRemoteHost();

        String location = "/something.html";
        Action action = new Action(null, location, DataInjectionStrategy.REMOTE);
        Form.Builder builder = null;
        Task task = null;
        Activity activity = new Activity.Builder()
                .action(ActionType.CREATE, action)
                .build();

        ViewContext context = new ViewContext("http://localhost", "/piecework/ui", "/piecework/api", "/piecework/public", "v2");

        ProcessDeploymentProvider deploymentProvider = new ProcessDeploymentProviderStub(process, deployment, null);
        FormDisposition disposition = FormUtility.disposition(deploymentProvider, activity, ActionType.CREATE, context, builder);
        Assert.assertEquals(action, disposition.getAction());
        Assert.assertEquals(URI.create("http://piecework.test/something.html"), disposition.getPageUri());
    }


}
