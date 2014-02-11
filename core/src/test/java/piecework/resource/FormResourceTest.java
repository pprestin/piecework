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
package piecework.resource;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import piecework.enumeration.ActionType;
import piecework.exception.ForbiddenError;
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.identity.IdentityHelper;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessInstanceRepository;
import piecework.persistence.ProcessRepository;
import piecework.test.config.IntegrationTestConfiguration;

import javax.ws.rs.core.Response;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;

/**
 * @author James Renfro
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={IntegrationTestConfiguration.class})
public class FormResourceTest {

    @Autowired
    FormResource formResource;

    @Autowired
    IdentityHelper mockIdentityHelper;

    @Autowired
    ProcessRepository mockProcessRepository;

    @Autowired
    ProcessInstanceRepository mockProcessInstanceRepository;

    private Action createAction;

    @Before
    public void setup() {
        String deploymentId = "992340";

        User principal = Mockito.mock(User.class);
        Mockito.doReturn(principal)
               .when(mockIdentityHelper).getPrincipal();
        Mockito.doReturn("09234")
               .when(principal).getEntityId();
        Mockito.doReturn("09234")
                .when(principal).getUserId();

        createAction = Mockito.mock(Action.class);

        Activity startActivity = Mockito.mock(Activity.class);
        Mockito.doReturn(createAction)
               .when(startActivity).action(eq(ActionType.CREATE));

        ProcessDeployment deployment = Mockito.mock(ProcessDeployment.class);
        Mockito.doReturn(deploymentId)
                .when(deployment).getDeploymentId();
        Mockito.doReturn(startActivity)
               .when(deployment).getActivity(eq("start"));
        Mockito.doReturn("start")
               .when(deployment).getStartActivityKey();

        Process process = Mockito.mock(Process.class);
        Mockito.doReturn("TEST")
               .when(process).getProcessDefinitionKey();
        Mockito.doReturn(deployment)
               .when(process).getDeployment();
        Mockito.doReturn(process)
               .when(mockProcessRepository).findOne(eq("TEST"));
        ProcessInstance instance = Mockito.mock(ProcessInstance.class);
        Mockito.doReturn(instance)
               .when(mockProcessInstanceRepository).findByTaskId(eq("TEST"), eq("1234"));
        Mockito.doReturn(deploymentId)
               .when(instance).getDeploymentId();
    }

    @Test(expected = NotFoundError.class)
    public void readByTaskIdNoProcess() throws PieceworkException {
        formResource.read(null, "INVALID_PROCESS_KEY", "INVALID_PROCESS_INSTANCE_ID", null, null, null);
    }

    @Test(expected = ForbiddenError.class)
    public void readByTaskIdNoInstance() throws PieceworkException {
        formResource.read(null, "TEST", "INVALID_PROCESS_INSTANCE_ID", null, null, null);
    }

//    @Test
//    public void readByTaskId() throws PieceworkException {
//        Response response = formResource.read(null, "TEST", "1234", null, null, "1");
//
//        Assert.assertEquals(200, response.getStatus());
//        Form form = Form.class.cast(response.getEntity());
//        Assert.assertNotNull(form);
//    }

}
