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
package piecework.test;

import piecework.Constants;
import piecework.content.ContentResource;
import piecework.content.concrete.BasicContentResource;
import piecework.enumeration.ActionType;
import piecework.model.*;
import piecework.model.Process;

import java.io.ByteArrayInputStream;
import java.util.UUID;

/**
 * @author James Renfro
 */
public class ExampleFactory {

    public static ContentResource exampleContent(String root) {
        return new BasicContentResource.Builder()
                .contentType("text/plain")
                .location(root + "/" + UUID.randomUUID().toString())
                .inputStream(new ByteArrayInputStream("This is a test".getBytes()))
                .build();
    }

    public static FormRequest exampleFormRequest(String formInstanceId) {
        return new FormRequest.Builder()
                .requestId(formInstanceId)
//                .screen(exampleContainer(Constants.ScreenTypes.WIZARD))
                .build();
    }


    public static Field employeeNameField() {
        return new Field.Builder()
                .fieldId("000100")
                .type(Constants.FieldTypes.TEXT)
                .label("Employee")
                .name("employeeName")
                .maxValueLength(40)
                .editable()
                .required()
                .build();
    }

    public static Field budgetNumberField() {
        return new Field.Builder()
                .fieldId("000101")
                .type(Constants.FieldTypes.NUMBER)
                .label("Budget number")
                .name("budgetNumber")
                .defaultValue("100000")
                .constraint(new Constraint.Builder().constraintId("c1").type(Constants.ConstraintTypes.IS_NUMERIC).build())
                .maxValueLength(20)
                .editable()
                .required()
                .build();
    }

    public static Field supervisorIdField() {
        return new Field.Builder()
                .fieldId("000102")
                .type(Constants.FieldTypes.TEXT)
                .label("Manager id")
                .name("supervisorId")
                .pattern("[a-z]{3}[0-9]{2}")
                .customValidity("Must be three lower-case letters, followed by two digits")
//                .constraint(new Constraint.Builder().constraintId("c2").type(Constants.ConstraintTypes.IS_VALID_USER).build())
                .constraint(new Constraint.Builder()
                        .constraintId("c3")
                        .type(Constants.ConstraintTypes.IS_ONLY_REQUIRED_WHEN)
                        .name("budgetNumber")
                        .value("^100000$")
                        .build())
                .constraint(new Constraint.Builder()
                        .constraintId("c4")
                        .type(Constants.ConstraintTypes.IS_ONLY_VISIBLE_WHEN)
                                .name("budgetNumber")
                                .value("^100001$")
                                .or(new Constraint.Builder()
                                        .constraintId("c5")
                                        .type(Constants.ConstraintTypes.IS_ONLY_VISIBLE_WHEN)
                                        .name("action")
                                        .value("^(bonus|promote)$")
                                        .build())
                        .build())
                .maxValueLength(40)
                .editable()
                .required()
                .build();
    }

    public static Field actionTypeField() {
        return new Field.Builder()
                .fieldId("000103")
                .type(Constants.FieldTypes.SELECT_MULTIPLE)
                .label("Action to take")
                .name("action")
                .option(new Option.Builder().optionId("o1").label("Grant bonus").value("bonus").build())
                .option(new Option.Builder().optionId("o2").label("Reprimand").value("reprimand").build())
                .option(new Option.Builder().optionId("o3").label("Promote").value("promote").build())
                .option(new Option.Builder().optionId("o4").label("Demote").value("demote").build())
                .defaultValue("bonus")
                .editable()
                .required()
                .build();
    }

    public static Field locationField() {
        return new Field.Builder()
                .fieldId("000104")
                .type(Constants.FieldTypes.SELECT_ONE)
                .label("Location")
                .name("location")
                .option(new Option.Builder().optionId("o5").label("In-state").value("in state").build())
                .option(new Option.Builder().optionId("o6").label("Out-of-state").value("out of state").selected().build())
                .option(new Option.Builder().optionId("o7").label("Waiver").value("waiver").build())
                .editable()
                .required()
                .build();
    }

    public static Field descriptionField() {
        return new Field.Builder()
                .fieldId("000105")
                .type(Constants.FieldTypes.TEXTAREA)
                .label("Description")
                .name("Description")
                .maxValueLength(4000)
                .build();
    }

    public static Field confirmationField() {
        return new Field.Builder()
                .fieldId("000106")
                .type(Constants.FieldTypes.TEXT)
                .name("ConfirmationNumber")
                .constraint(new Constraint.Builder().constraintId("c6").type(Constants.ConstraintTypes.IS_CONFIRMATION_NUMBER).build())
                .maxValueLength(40)
                .build();
    }

    public static Field allowedField() {
        return new Field.Builder()
                .fieldId("000107")
                .type(Constants.FieldTypes.CHECKBOX)
                .label("Is this employee allowed to go on vacation?")
                .name("Allowed")
                .option(new Option.Builder().optionId("o8").value("Yes").label("Yes").build())
                .editable()
                .build();
    }

    public static Field applicableField() {
        return new Field.Builder()
                .fieldId("000108")
                .type(Constants.FieldTypes.RADIO)
                .label("Is this form applicable to them?")
                .name("Applicable")
                .option(new Option.Builder().optionId("o9").value("Yes").label("Yes").build())
                .option(new Option.Builder().optionId("o10").value("No").label("No").build())
                .editable()
                .build();
    }

    public static Section exampleSectionWithTwoFields() {
        return new Section.Builder()
                .sectionId("00001")
                .tagId("basic")
                .field(employeeNameField())
                .field(budgetNumberField())
                .ordinal(1)
                .build();
    }

    public static Section exampleSectionWithOneField() {
        return new Section.Builder()
                .sectionId("00002")
                .tagId("supplemental")
                .field(supervisorIdField())
                .field(actionTypeField())
                .field(locationField())
                .field(descriptionField())
                .field(allowedField())
                .field(applicableField())
                .ordinal(2)
                .build();
    }

    public static Section exampleSectionWithConfirmationNumber() {
        return new Section.Builder()
                .tagId("confirmation")
                .field(confirmationField())
                .ordinal(1)
                .build();
    }

//    public static Container exampleContainer(String type) {
//
//        return new Container.Builder()
//                .title("First screen")
//                .grouping(new Grouping.Builder()
//                        .groupingId("A")
//                        .button(new Button.Builder().buttonId("b1").label("Next").type("button-link").value("next").tooltip("Go to next step").build())
//                        .sectionId(exampleSectionWithTwoFields().getSectionId())
//                        .build())
//                .attachmentAllowed(false)
//                .location("/test/example1.html")
//                .build();
//    }

//    public static Screen exampleThankYouScreen() {
//        return new Screen.Builder()
//                .title("Second screen")
//                .grouping(new Grouping.Builder()
//                        .groupingId("B")
//                        .button(new Button.Builder().buttonId("b2").name("next").label("Next").type("button").value("submit").tooltip("Complete").build())
//                        .sectionId(exampleSectionWithConfirmationNumber().getSectionId())
//                        .build())
//                .attachmentAllowed(false)
//                .build();
//    }
//
//    public static Screen exampleScreenForReview(String type) {
//
//        return new Screen.Builder()
//                .title("Review screen")
//                .type(type)
//                .grouping(new Grouping.Builder()
//                        .groupingId("A")
//                        .button(new Button.Builder().buttonId("b1").name("next").label("Next").type("button-link").value("next").tooltip("Go to next step").build())
//                        .sectionId(exampleSectionWithTwoFields().getSectionId())
//                        .build())
//                .attachmentAllowed(false)
//                .build();
//    }
//
//    public static Interaction exampleInteractionWithTwoScreens() {
//        return new Interaction.Builder()
//                .label("Example Interaction")
//                .screen(exampleContainer(Constants.ScreenTypes.WIZARD))
//                .screen(ActionType.COMPLETE, exampleThankYouScreen())
//                .build();
//    }
//
//    public static Interaction exampleInteractionForTaskReview() {
//        return new Interaction.Builder()
//                .label("Example Interaction for Review")
//                .screen(exampleScreenForReview(Constants.ScreenTypes.WIZARD))
//                .screen(ActionType.COMPLETE, exampleThankYouScreen())
//                .taskDefinitionKey("Review")
//                .build();
//    }

//    public static Form exampleForm() {
//        Process process = exampleProcess();
//        return new Form.Builder()
//                .processDefinitionKey(process.getProcessDefinitionKey())
//                .submissionType(Constants.SubmissionTypes.INTERIM)
//                .formInstanceId("12345")
//                .variable(employeeNameField().getName(), Collections.singletonList(new Value("Joe Testington")))
//                .container(exampleContainer(Constants.ScreenTypes.STANDARD))
//                .build();
//    }
//
//    public static Form exampleFormWithWizardTemplate() {
//        Process process = exampleProcess();
//        return new Form.Builder()
//                .processDefinitionKey(process.getProcessDefinitionKey())
//                .submissionType(Constants.SubmissionTypes.INTERIM)
//                .formInstanceId("12345")
//                .variable(employeeNameField().getName(), Collections.singletonList(new Value("Joe Testington")))
//                .screen(exampleContainer(Constants.ScreenTypes.WIZARD_TEMPLATE))
//                .build();
//    }

    public static ProcessDeployment exampleProcessDeployment() {
        return new ProcessDeployment.Builder()
//                .interaction(exampleInteractionWithTwoScreens())
//                .interaction(exampleInteractionForTaskReview())
//                .section(exampleSectionWithTwoFields())
//                .section(exampleSectionWithConfirmationNumber())
//                .section(exampleSectionWithOneField())
                .engine("activiti")
                .engineProcessDefinitionKey("example")
                .build();
    }

    public static Process exampleProcess() {
        ProcessDeployment deployment = exampleProcessDeployment();
        return new Process.Builder()
                .processDefinitionKey("Demonstration")
                .processDefinitionLabel("This is a demonstration process")
                .deploy(new ProcessDeploymentVersion(deployment), deployment)
                .build();
    }

    public static ProcessInstance exampleProcessInstance() {
        Process process = exampleProcess();
        return new ProcessInstance.Builder()
                .processDefinitionKey(process.getProcessDefinitionKey())
                .processDefinitionLabel(process.getProcessDefinitionLabel())
                .engineProcessInstanceId(UUID.randomUUID().toString())
                .build();
    }

    public static Process simpleThreeInteractionProcess() {

        Screen startScreen = new Screen.Builder()
                .screenId("1")
                .title("Start Screen")
                .build();

        Screen startConfirmationScreen = new Screen.Builder()
                .screenId("2")
                .title("Start Confirmation Screen")
                .build();

        Interaction startInteraction = new Interaction.Builder()
                .screen(startScreen)
                .screen(ActionType.COMPLETE, startConfirmationScreen)
                .build();

        Screen task1Screen = new Screen.Builder()
                .screenId("3")
                .title("Task 1 Screen")
                .build();

        Screen task1ConfirmationScreen = new Screen.Builder()
                .screenId("4")
                .title("Task 1 Confirmation Screen")
                .build();

        Screen task1RejectionScreen = new Screen.Builder()
                .screenId("5")
                .title("Task 1 Rejection Screen")
                .build();

        Interaction task1Interaction = new Interaction.Builder()
                .taskDefinitionKey("TASK1")
                .screen(task1Screen)
                .screen(ActionType.COMPLETE, task1ConfirmationScreen)
                .screen(ActionType.REJECT, task1RejectionScreen)
                .build();

        Interaction task2Interaction = new Interaction.Builder()
                .taskDefinitionKey("TASK2")
                .build();

        ProcessDeployment deployment = new ProcessDeployment.Builder()
//                .interaction(startInteraction)
//                .interaction(task1Interaction)
//                .interaction(task2Interaction)
                .build();

        ProcessDeploymentVersion version = new ProcessDeploymentVersion(deployment);
        return new Process.Builder()
                .version(version)
                .deploy(version, deployment)
                .build();

    }

}
