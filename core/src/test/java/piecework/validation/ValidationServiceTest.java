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
package piecework.validation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import piecework.Constants;
import piecework.Registry;
import piecework.exception.StatusCodeError;
import piecework.identity.IdentityHelper;
import piecework.service.IdentityService;
import piecework.security.EncryptionService;
import piecework.service.TaskService;
import piecework.service.ValidationService;
import piecework.model.*;
import piecework.model.Process;
import piecework.test.ExampleFactory;
import piecework.util.ManyMap;

import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
@Ignore
@RunWith(MockitoJUnitRunner.class)
public class ValidationServiceTest {

    @InjectMocks
    SubmissionTemplateFactory submissionTemplateFactory;

    @InjectMocks
	ValidationService validationService;

    @Mock
    Registry registry;

    @Mock
    IdentityHelper helper;

    @Mock
    EncryptionService encryptionService;

    @Mock
    IdentityService identityService;

    @Mock
    TaskService taskService;

	@Before
	public void setUp() {
        Mockito.when(encryptionService.decrypt(Mockito.any(Map.class))).then(new Answer<Map<String, List<String>>>() {
            @Override
            public Map<String, List<String>> answer(InvocationOnMock invocation) throws Throwable {
                Map<String, List<String>> map = (Map<String, List<String>>) invocation.getArguments()[0];
                return new ManyMap<String, String>(map);
            }
        });
	}

//	@Test
//	public void testValidateFirstOfTwoSections() throws StatusCodeError {
//        Process process = ExampleFactory.exampleProcess();
//        ProcessInstance instance = null;
//		Submission submission = new Submission.Builder()
//			.formValue("employeeName", "John Test")
//			.formValue("budgetNumber", "123456")
//			.build();
//
//        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, ExampleFactory.exampleContainer(Constants.ScreenTypes.WIZARD), "A");
//		FormValidation validation = validationService.validate(process, instance, null, template, submission, true);
//
//        Map<String, List<Message>> results = validation.getResults();
//		Assert.assertTrue(results.isEmpty());
//	}
//
//    @Test
//    public void testValidateFirstOfTwoSectionsFailed() throws StatusCodeError {
//        Process process = ExampleFactory.exampleProcess();
//        ProcessInstance instance = null;
//        Submission submission = new Submission.Builder()
//                .formValue("employeeName", "John Test")
//                .build();
//
//        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, ExampleFactory.exampleContainer(Constants.ScreenTypes.WIZARD), "A");
//        FormValidation validation = validationService.validate(process, instance, null, template, submission, false);
//
//        Map<String, List<Message>> results = validation.getResults();
//        Assert.assertNotNull(results);
//        Assert.assertEquals(1, results.size());
//    }

//    @Test
//    public void testValidateBothOfTwoSections() throws StatusCodeError {
//        Process process = ExampleFactory.exampleProcess();
//        ProcessInstance instance = null;
//        Submission submission = new Submission.Builder()
//                .formValue("employeeName", "John Test")
//                .formValue("budgetNumber", "123456")
//                .formValue("supervisorId", "sup1234")
//                .build();
//
//        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, ExampleFactory.exampleContainer(Constants.ScreenTypes.WIZARD));
//        FormValidation validation = validationService.validate(process, instance, null, template, submission, true);
//
//        Map<String, List<Message>> results = validation.getResults();
//        Assert.assertTrue(results.isEmpty());
//    }
//
//    @Test
//    public void testValidateBothOfTwoSectionsFailed() throws StatusCodeError {
//        Process process = ExampleFactory.exampleProcess();
//        ProcessInstance instance = null;
//        Submission submission = new Submission.Builder()
//                .formValue("budgetNumber", "123456")
//                .formValue("supervisorId", "sup1234")
//                .build();
//
//        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, ExampleFactory.exampleContainer(Constants.ScreenTypes.WIZARD));
//        FormValidation validation = validationService.validate(process, instance, null, template, submission, false);
//
//        Map<String, List<Message>> results = validation.getResults();
//        Assert.assertNotNull(results);
//        Assert.assertEquals(1, results.size());
//    }


}
