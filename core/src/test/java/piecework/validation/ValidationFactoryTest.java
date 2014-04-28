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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import piecework.common.ManyMap;
import piecework.enumeration.ActionType;
import piecework.exception.BadRequestError;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.persistence.test.ProcessDeploymentProviderStub;
import piecework.submission.SubmissionTemplate;
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

    @Test
    public void testValidateValidRequiredField() {
        ProcessDeploymentProvider modelProvider = new ProcessDeploymentProviderStub();

        String fieldName = "TestField";

        Field field = new Field.Builder()
                .name(fieldName)
                .editable()
                .required()
                .build();
        Validation.Builder builder = new Validation.Builder();
        List<ValidationRule> rules = new ArrayList<ValidationRule>(ValidationUtility.validationRules(field, null));
        Set<String> fieldNames = new HashSet<String>();
        fieldNames.add(fieldName);
        ManyMap<String, Value> submissionData = new ManyMap<String, Value>();
        ManyMap<String, Value> instanceData = new ManyMap<String, Value>();
        submissionData.putOne(fieldName, new Value("TestValue"));

        validationFactory.validateField(modelProvider, builder, field, rules, fieldNames,
                submissionData, instanceData, submissionData, instanceData, true, false);

        Validation validation = builder.build();
        Assert.assertEquals(submissionData, validation.getData());
        Assert.assertEquals(0, validation.getResults().size());
    }

    @Test
    public void testValidateMissingRequiredField() {
        ProcessDeploymentProvider modelProvider = new ProcessDeploymentProviderStub();

        String fieldName = "TestField";

        Field field = new Field.Builder()
                .name(fieldName)
                .editable()
                .required()
                .build();
        Validation.Builder builder = new Validation.Builder();
        List<ValidationRule> rules = new ArrayList<ValidationRule>(ValidationUtility.validationRules(field, null));
        Set<String> fieldNames = new HashSet<String>();
        fieldNames.add(fieldName);
        ManyMap<String, Value> submissionData = new ManyMap<String, Value>();
        ManyMap<String, Value> instanceData = new ManyMap<String, Value>();

        validationFactory.validateField(modelProvider, builder, field, rules, fieldNames,
                submissionData, instanceData, submissionData, instanceData, true, false);

        Validation validation = builder.build();
        List<Message> messages = validation.getResults().get(fieldName);
        Assert.assertEquals(1, messages.size());
        Assert.assertEquals("Field is required", messages.get(0).getText());
        Assert.assertTrue(validation.getData().isEmpty());
    }

    @Test
    public void testValidateEmptyField() {

        String fieldName1 = "TestField1";
        String fieldName2 = "TestField2";

        Field field1 = new Field.Builder()
                .name(fieldName1)
                .editable()
                .required()
                .build();
        Field field2 = new Field.Builder()
                .name(fieldName2)
                .editable()
                .build();

        Submission submission = new Submission.Builder()
                .formValue(fieldName1, "")
                .attachment(new File.Builder()
                        .description("Test")
                        .build())
                .actionType(ActionType.ATTACH)
                .build();

        ProcessDeployment deployment = new ProcessDeployment.Builder()
                .deploymentId("1234")
                .build();

        Process process = new Process.Builder()
                .processDefinitionKey("TEST")
                .deploy(new ProcessDeploymentVersion(deployment), deployment)
                .build();

        SubmissionTemplate template = new SubmissionTemplate.Builder(process, deployment)
                .field(field1)
                .field(field2)
                .build();

        Entity principal = Mockito.mock(User.class);

        ProcessDeploymentProvider modelProvider = new ProcessDeploymentProviderStub(process, deployment, principal);

        BadRequestError badRequestError = null;
        Validation validation = null;

        try {
            validation = validationFactory.validate(modelProvider, submission, template, "v1", true);


        } catch (BadRequestError bre) {
            badRequestError = bre;
        } catch (PieceworkException e) {

        }

        List<Value> values = validation.getData().get(fieldName1);
        Assert.assertEquals(1, values.size());

        values = validation.getData().get(fieldName2);
        Assert.assertNull(values);
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
