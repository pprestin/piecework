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
package piecework.form;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import piecework.Constants;
import piecework.form.legacy.AttributeValidation;
import piecework.form.validation.ValidationService;
import piecework.model.Constraint;
import piecework.model.Field;
import piecework.model.FormSubmission;
import piecework.model.ProcessInstance;
import piecework.model.Screen;
import piecework.model.Section;

/**
 * @author James Renfro
 */
public class ValidationServiceTest {

	Screen testScreen;
	ValidationService validationService;
	
	@Before
	public void setUp() {
		this.validationService = new ValidationService();
		Field employeeNameField = new Field.Builder()
			.type(Constants.FieldTypes.TEXT)
			.maxValueLength(40)
			.build();
		
		Field budgetNumberField = new Field.Builder()
			.type(Constants.FieldTypes.TEXT)
			.maxValueLength(20)
			.constraint(new Constraint.Builder().type(Constants.ConstraintTypes.IS_NUMERIC).build())
			.build();
		
		Section step1 = new Section.Builder()
			.tagId("basic")
			.field(employeeNameField)
			.field(budgetNumberField)
			.ordinal(1)
			.build();
		
		Field supervisorIdField = new Field.Builder()
			.type(Constants.FieldTypes.TEXT)
			.constraint(new Constraint.Builder().type(Constants.ConstraintTypes.IS_VALID_USER).build())
			.maxValueLength(40)
			.build();
		
		Section step2 = new Section.Builder()
			.tagId("supplemental")
			.field(supervisorIdField)
			.ordinal(2)
			.build();
		
		this.testScreen = new Screen.Builder()
			.section(step1)
			.section(step2)
			.attachmentAllowed(false)
			.build();
	}
	
	
	@Test
	public void testValidateFirstOfTwoSections() {
		FormSubmission submission = new FormSubmission.Builder()
			.formValue("employeeName", "John Test")
			.formValue("budgetNumber", "123456")
			.build();

		ProcessInstance instance = null;
		
	
		List<AttributeValidation> validations = validationService.validate(submission, instance, testScreen, "basic");
		
		
	}

}
