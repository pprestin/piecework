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
package piecework.submission.concrete;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import piecework.common.ManyMap;
import piecework.enumeration.FieldSubmissionType;
import piecework.exception.PieceworkException;
import piecework.model.Submission;
import piecework.model.User;
import piecework.submission.SubmissionTemplate;
import piecework.submission.config.SubmissionConfiguration;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

/**
 * @author James Renfro
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={SubmissionConfiguration.class})
public class FormValueSubmissionHandlerTest {

    @Autowired
    FormValueSubmissionHandler submissionHandler;

    @Test
    public void testHandleSuccess() throws PieceworkException {
        piecework.model.Process mockProcess = Mockito.mock(piecework.model.Process.class);
        SubmissionTemplate mockTemplate = Mockito.mock(SubmissionTemplate.class);
        User mockUser = Mockito.mock(User.class);

        Mockito.doReturn(FieldSubmissionType.ACCEPTABLE)
               .when(mockTemplate).fieldSubmissionType(eq("TestField1"));
        Mockito.doReturn(mockProcess)
               .when(mockTemplate).getProcess();
        Mockito.doReturn("TESTPROCESSKEY1")
               .when(mockProcess).getProcessDefinitionKey();

        ManyMap<String, String> data = new ManyMap<String, String>();
        data.putOne("TestField1", "Some data");

        Submission submission = submissionHandler.handle(data, mockTemplate, mockUser);
        String fieldData = submission.getData().get("TestField1").iterator().next().getValue();
        Assert.assertEquals("Some data", fieldData);
    }


}
