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

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import piecework.common.ManyMap;
import piecework.model.Field;
import piecework.model.Message;
import piecework.model.Value;
import piecework.test.config.IntegrationTestConfiguration;
import piecework.util.ValidationUtility;
import piecework.validation.config.ValidationConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author James Renfro
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={ValidationConfiguration.class})
public class ValidationFactoryTest {

    @Autowired
    ValidationFactory validationFactory;


    @Test
    public void verifyApplicationContext() {
        Assert.assertTrue(true);
    }

//    @Test
//	public void testValidateFirstOfTwoSections() throws StatusCodeError {
//        Process process = ExampleFactory.exampleProcess();
//        ProcessInstance instance = null;
//		Submission submission = new Submission.Builder()
//			.formValue("employeeName", "John Test")
//			.formValue("budgetNumber", "123456")
//			.build();
//
//        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, ExampleFactory.exampleContainer(Constants.ScreenTypes.WIZARD), "A");
//		Validation validation = validationFactory.validate(process, instance, null, template, submission, true);
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
//        Validation validation = validationFactory.validate(process, instance, null, template, submission, false);
//
//        Map<String, List<Message>> results = validation.getResults();
//        Assert.assertNotNull(results);
//        Assert.assertEquals(1, results.size());
//    }
//
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
//        Validation validation = validationFactory.validate(process, instance, null, template, submission, true);
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
//        Validation validation = validationFactory.validate(process, instance, null, template, submission, false);
//
//        Map<String, List<Message>> results = validation.getResults();
//        Assert.assertNotNull(results);
//        Assert.assertEquals(1, results.size());
//    }


}
