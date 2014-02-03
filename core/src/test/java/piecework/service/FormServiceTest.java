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

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import piecework.Constants;
import piecework.authorization.AuthorizationRole;
import piecework.command.CommandFactory;
import piecework.command.SubmissionCommandResponse;
import piecework.common.ManyMap;
import piecework.enumeration.ActionType;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.RequestRepository;
import piecework.submission.config.SubmissionConfiguration;
import piecework.test.config.IntegrationTestConfiguration;

import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;

/**
 * @author James Renfro
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={IntegrationTestConfiguration.class})
public class FormServiceTest {

    @Autowired
    FormService formService;

    @Autowired
    RequestRepository mockRequestRepository;

    private Process process;
    private RequestDetails requestDetails;
    private String requestId;
    private ManyMap<String, String> data;
    private User principal;

    @Before
    public void setup() {
        Activity activity = Mockito.mock(Activity.class);
        Mockito.doReturn(false)
                .when(activity).isAllowAny();

        process = Mockito.mock(Process.class);
        Mockito.doReturn("TEST")
                .when(process).getProcessDefinitionKey();
        ProcessDeployment deployment = Mockito.mock(ProcessDeployment.class);
        Mockito.doReturn(deployment)
                .when(process).getDeployment();
        Mockito.doReturn("123456")
                .when(deployment).getDeploymentId();
        Mockito.doReturn("start")
                .when(deployment).getStartActivityKey();
        Mockito.doReturn(activity)
                .when(deployment).getActivity(eq("start"));

        ProcessInstance instance = Mockito.mock(ProcessInstance.class);
        Mockito.doReturn("123456")
                .when(instance).getDeploymentId();

        requestDetails = Mockito.mock(RequestDetails.class);
        data = new ManyMap<String, String>();
        requestId = "123";
        principal = Mockito.mock(User.class);
        Mockito.doReturn(Boolean.TRUE)
                .when(principal).hasRole(eq(process), eq(AuthorizationRole.INITIATOR));

        FormRequest request = new FormRequest.Builder()
                .action(ActionType.CREATE)
                .activity(activity)
                .instance(instance)
                .build();

        Mockito.doReturn(request)
                .when(mockRequestRepository).findOne(anyString());
    }

    @Test
    public void onValidSave() throws PieceworkException {
        SubmissionCommandResponse response = formService.save(process, requestDetails, requestId, data, Map.class, principal);
        Assert.assertNotNull(response);
        Submission submission = response.getSubmission();
        Assert.assertEquals("TEST", submission.getProcessDefinitionKey());
        Assert.assertEquals(ActionType.COMPLETE, submission.getAction());
        FormRequest nextRequest = response.getNextRequest();
        Assert.assertEquals(ActionType.COMPLETE, nextRequest.getAction());
    }

    @Test
    public void onValidSubmit() throws PieceworkException {
        SubmissionCommandResponse response = formService.submit(process, requestDetails, requestId, data, Map.class, principal);
        Assert.assertNotNull(response);
        Submission submission = response.getSubmission();
        Assert.assertEquals("TEST", submission.getProcessDefinitionKey());
        Assert.assertEquals(ActionType.COMPLETE, submission.getAction());
        FormRequest nextRequest = response.getNextRequest();
        Assert.assertEquals(ActionType.COMPLETE, nextRequest.getAction());
    }

    @Test
    public void onValidValidate() throws PieceworkException {
        formService.validate(process, requestDetails, requestId, data, Map.class, null, principal);
    }

}
