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
import piecework.repository.ProcessInstanceRepository;
import piecework.repository.ProcessRepository;
import piecework.test.ProcessFactory;
import piecework.test.config.IntegrationTestConfiguration;

import java.util.Collections;

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

        ProcessDeployment deployment = ProcessFactory.remoteStrategyProcessDeployment(deploymentId);
        Process process = ProcessFactory.process("TEST", "Some Test Process", deployment);

        Mockito.doReturn(process)
               .when(mockProcessRepository).findOne(eq("TEST"));
        ProcessInstance instance = Mockito.mock(ProcessInstance.class);
        Mockito.doReturn(instance)
               .when(mockProcessInstanceRepository).findByTaskId(eq("TEST"), eq("1234"));
        Mockito.doReturn(deploymentId)
               .when(instance).getDeploymentId();

        Task task = Mockito.mock(Task.class);
        Mockito.doReturn(Collections.singleton(task))
                .when(instance).getTasks();
        Mockito.doReturn("1234")
                .when(task).getTaskInstanceId();

        Mockito.doReturn(Collections.singleton("testuser"))
                .when(task).getAssigneeAndCandidateAssigneeIds();

        Mockito.doReturn(Boolean.TRUE)
                .when(task).isCandidateOrAssignee(eq(principal));
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
//    public void readByTaskIdHtml() throws PieceworkException {
//        Response response = formResource.read(null, "TEST", "1234", null, null, "1");
//
//        Assert.assertEquals(303, response.getStatus());
//        String location = response.getHeaderString(HttpHeaders.LOCATION);
//        Assert.assertEquals("", location);
//    }
//
//    @Test
//    public void readByTaskIdJson() throws PieceworkException {
//        MessageContext context = Mockito.mock(MessageContext.class);
//        HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);
//
//        Mockito.doReturn(Collections.singletonList(MediaType.APPLICATION_JSON_TYPE))
//               .when(httpHeaders.getAcceptableMediaTypes());
//
//        Mockito.doReturn(httpHeaders)
//               .when(context.getHttpHeaders());
//
//        Response response = formResource.read(context, "TEST", "1234", null, null, "1");
//
//        Assert.assertEquals(200, response.getStatus());
//        Form form = Form.class.cast(response.getEntity());
//        Assert.assertNotNull(form);
//    }

}
