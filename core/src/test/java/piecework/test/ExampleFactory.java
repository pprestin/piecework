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

    public static ProcessDeployment exampleProcessDeployment() {
        return new ProcessDeployment.Builder()
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

}
