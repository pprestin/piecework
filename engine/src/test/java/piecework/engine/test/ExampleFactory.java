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
package piecework.engine.test;

import piecework.Constants;
import piecework.enumeration.ActionType;
import piecework.model.*;
import piecework.model.Process;

/**
 * @author James Renfro
 */
public class ExampleFactory {

    public static Field employeeNameField() {
        return new Field.Builder()
                .type(Constants.FieldTypes.TEXT)
                .name("employeeName")
                .maxValueLength(40)
                .editable()
                .required()
                .build();
    }

    public static Field budgetNumberField() {
        return new Field.Builder()
                .type(Constants.FieldTypes.TEXT)
                .name("budgetNumber")
                .constraint(new Constraint.Builder().type(Constants.ConstraintTypes.IS_NUMERIC).build())
                .maxValueLength(20)
                .editable()
                .required()
                .build();
    }

    public static Field supervisorIdField() {
        return new Field.Builder()
                .type(Constants.FieldTypes.PERSON)
                .name("supervisorId")
//                .constraint(new Constraint.Builder().type(Constants.ConstraintTypes.IS_VALID_USER).build())
                .maxValueLength(40)
                .editable()
                .required()
                .build();
    }

    public static Field confirmationField() {
        return new Field.Builder()
                .type(Constants.FieldTypes.TEXT)
                .name("ConfirmationNumber")
                .constraint(new Constraint.Builder().type(Constants.ConstraintTypes.IS_CONFIRMATION_NUMBER).build())
                .maxValueLength(40)
                .build();
    }

    public static ProcessDeployment exampleProcessDeployment() {
        return new ProcessDeployment.Builder()
                .engineProcessDefinitionLocation("META-INF/example.bpmn20.xml")
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


}
