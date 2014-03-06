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
package piecework.command;

import junit.framework.Assert;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import piecework.command.config.CommandConfiguration;
import piecework.common.ManyMap;
import piecework.enumeration.ActionType;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.TaskProvider;
import piecework.persistence.test.TaskProviderStub;
import piecework.validation.Validation;

import java.util.Map;

import static org.mockito.Matchers.eq;

/**
 * @author James Renfro
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={CommandConfiguration.class})
public class ValidationCommandTest {

    @Autowired
    CommandFactory commandFactory;

    @Test
    public void testMapValidationSuccess() throws PieceworkException {
        Activity mockActivity = Mockito.mock(Activity.class);
        Process mockProcess = Mockito.mock(Process.class);
        ProcessDeployment mockDeployment = Mockito.mock(ProcessDeployment.class);
        FormRequest mockRequest = Mockito.mock(FormRequest.class);
        Submission mockSubmission = Mockito.mock(Submission.class);
        User mockPrincipal = Mockito.mock(User.class);

        ManyMap<String, String> data = new ManyMap<String, String>();

        TaskProviderStub taskProvider = new TaskProviderStub(mockProcess, mockDeployment, null, null, mockPrincipal);
        taskProvider.setActivity(mockActivity);
        ValidationCommand<TaskProvider> validationCommand = commandFactory.validation(taskProvider, mockRequest, ActionType.VALIDATE, data, Map.class, "v1");
        Validation validation = validationCommand.execute();

        Assert.assertNotNull(validation);
    }

    @Test
    public void testMultipartBodyValidationSuccess() throws PieceworkException {
        Activity mockActivity = Mockito.mock(Activity.class);
        Process mockProcess = Mockito.mock(Process.class);
        ProcessDeployment mockDeployment = Mockito.mock(ProcessDeployment.class);
        FormRequest mockRequest = Mockito.mock(FormRequest.class);
        MultipartBody mockMultipartBody = Mockito.mock(MultipartBody.class);
        Submission mockSubmission = Mockito.mock(Submission.class);
        User mockPrincipal = Mockito.mock(User.class);

        TaskProviderStub taskProvider = new TaskProviderStub(mockProcess, mockDeployment, null, null, mockPrincipal);
        taskProvider.setActivity(mockActivity);
        ValidationCommand<TaskProvider> validationCommand = commandFactory.validation(taskProvider, mockRequest, ActionType.VALIDATE, mockMultipartBody, MultipartBody.class, "v1");
        Validation validation = validationCommand.execute();

        Assert.assertNotNull(validation);
    }

    @Test
    public void testSubmissionValidationSuccess() throws PieceworkException {
        Activity mockActivity = Mockito.mock(Activity.class);
        Process mockProcess = Mockito.mock(Process.class);
        ProcessDeployment mockDeployment = Mockito.mock(ProcessDeployment.class);
        FormRequest mockRequest = Mockito.mock(FormRequest.class);
        Submission mockSubmission = Mockito.mock(Submission.class);
        User mockPrincipal = Mockito.mock(User.class);

        TaskProviderStub taskProvider = new TaskProviderStub(mockProcess, mockDeployment, null, null, mockPrincipal);
        taskProvider.setActivity(mockActivity);
        SubmissionValidationCommand<TaskProvider> validationCommand = commandFactory.submissionValidation(taskProvider, mockRequest, ActionType.VALIDATE, mockSubmission, "v1", false);
        Validation validation = validationCommand.execute();

        Assert.assertNotNull(validation);
    }

}
