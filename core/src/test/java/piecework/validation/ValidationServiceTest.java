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
import org.junit.Test;

import piecework.Constants;
import piecework.exception.StatusCodeError;
import piecework.service.ValidationService;
import piecework.model.*;
import piecework.model.Process;
import piecework.test.ExampleFactory;

import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
public class ValidationServiceTest {

    SubmissionTemplateFactory submissionTemplateFactory;
	ValidationService validationService;

	@Before
	public void setUp() {
		this.validationService = new ValidationService();
        this.submissionTemplateFactory = new SubmissionTemplateFactory();
	}

	@Test
	public void testValidateFirstOfTwoSections() throws StatusCodeError {
        Process process = ExampleFactory.exampleProcess();
        ProcessInstance instance = null;
		Submission submission = new Submission.Builder()
			.formValue("employeeName", "John Test")
			.formValue("budgetNumber", "123456")
			.build();

        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, ExampleFactory.exampleScreenWithTwoSections(Constants.ScreenTypes.WIZARD), "A");
		FormValidation validation = validationService.validate(process, instance, null, template, submission, true);

        Map<String, List<Message>> results = validation.getResults();
		Assert.assertNull(results);
	}

    @Test
    public void testValidateFirstOfTwoSectionsFailed() throws StatusCodeError {
        Process process = ExampleFactory.exampleProcess();
        ProcessInstance instance = null;
        Submission submission = new Submission.Builder()
                .formValue("employeeName", "John Test")
                .build();

        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, ExampleFactory.exampleScreenWithTwoSections(Constants.ScreenTypes.WIZARD), "A");
        FormValidation validation = validationService.validate(process, instance, null, template, submission, true);

        Map<String, List<Message>> results = validation.getResults();
        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.size());
    }

    @Test
    public void testValidateBothOfTwoSections() throws StatusCodeError {
        Process process = ExampleFactory.exampleProcess();
        ProcessInstance instance = null;
        Submission submission = new Submission.Builder()
                .formValue("employeeName", "John Test")
                .formValue("budgetNumber", "123456")
                .formValue("supervisorId", "sup1234")
                .build();

        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, ExampleFactory.exampleScreenWithTwoSections(Constants.ScreenTypes.WIZARD));
        FormValidation validation = validationService.validate(process, instance, null, template, submission, true);

        Map<String, List<Message>> results = validation.getResults();
        Assert.assertNull(results);
    }

    @Test
    public void testValidateBothOfTwoSectionsFailed() throws StatusCodeError {
        Process process = ExampleFactory.exampleProcess();
        ProcessInstance instance = null;
        Submission submission = new Submission.Builder()
                .formValue("budgetNumber", "123456")
                .formValue("supervisorId", "sup1234")
                .build();

        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, ExampleFactory.exampleScreenWithTwoSections(Constants.ScreenTypes.WIZARD));
        FormValidation validation = validationService.validate(process, instance, null, template, submission, true);

        Map<String, List<Message>> results = validation.getResults();
        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.size());
    }


}
