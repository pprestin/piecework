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

import com.google.common.collect.Sets;
import piecework.Constants;
import piecework.enumeration.ActionType;
import piecework.enumeration.ActivityUsageType;
import piecework.enumeration.DataInjectionStrategy;
import piecework.enumeration.FlowElementType;
import piecework.model.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author James Renfro
 */
public class ProcessFactory {

    private static final String CUSTOM_WEB_PAGE = "/path/to/CustomWebPage.html";


    public static piecework.model.Process process(String processDefinitionKey, String label, ProcessDeployment deployment) {
        return new piecework.model.Process.Builder()
                .processDefinitionKey(processDefinitionKey)
                .processDefinitionLabel(label)
                .deploy(new ProcessDeploymentVersion(deployment), deployment)
                .build();
    }

    public static ProcessDeployment remoteStrategyProcessDeployment(String deploymentId) {
        ProcessDeployment current = new ProcessDeployment.Builder()
                .deploymentId(deploymentId)
                .deploymentLabel("First revision")
                .processInstanceLabelTemplate("{{BudgetNumber}} for {{ReportPeriod}}")
                .engine("activiti")
                .engineProcessDefinitionKey("SomeEngineKey.v2")
                .engineProcessDefinitionLocation("classpath:META-INF/some/path/SomeEngineKey.v2.bpmn20.xml")
                .base("classpath:META-INF/org/institution")
                .remoteHost("https://some.institution.org")
                .startActivityKey("start")
                .activity("start", remoteActivity(Start.INPUT_CONTAINER, CUSTOM_WEB_PAGE))
                .activity("preliminary", remoteActivity(Start.INPUT_CONTAINER, CUSTOM_WEB_PAGE))
                .activity("preReconciliation", remoteActivity(PreReconciliation.INPUT_CONTAINER, CUSTOM_WEB_PAGE))
                .activity("prep", remoteActivity(Prep.INPUT_CONTAINER, CUSTOM_WEB_PAGE))
                .activity("analysis", remoteActivity(Analysis.INPUT_CONTAINER, CUSTOM_WEB_PAGE))
                .activity("managerReview", remoteActivity(ManagerReview.INPUT_CONTAINER, CUSTOM_WEB_PAGE))
                .activity("submission", remoteActivity(Submission.INPUT_CONTAINER, CUSTOM_WEB_PAGE))
                .build();
        return current;
    }

    public static ProcessDeployment multistepProcessDeployment(String deploymentId) {
        ProcessDeployment current = new ProcessDeployment.Builder()
                .deploymentId(deploymentId)
                .deploymentLabel("Fifteen revision")
                .engine("activiti")
                .engineProcessDefinitionKey("SomeOtherEngineKey.v2")
                .engineProcessDefinitionLocation("classpath:META-INF/some/path/SomeOtherEngineKey.v2.bpmn20.xml")
                .base("classpath:META-INF/org/institution")
                .remoteHost("https://some.institution.org")
                .startActivityKey("start")
                .activity("start", multistepActivity())
                .build();
        return current;
    }

    public static Activity multistepActivity() {
        Container.Builder parentContainerBuilder = new Container.Builder()
                .children(MULTISTEP_INPUT_CONTAINERS)
                .activeChildIndex(1);

        parentContainerBuilder
                    .title("Multistep Form")
                    .button(new Button.Builder().type(Constants.ButtonTypes.SUBMIT).name("actionButton").label("Submit").value("submit").ordinal(1).build());

        Activity.Builder builder = new Activity.Builder()
                .elementType(FlowElementType.START_EVENT)
                .usageType(ActivityUsageType.MULTI_STEP)
                .action(ActionType.CREATE, new Action(parentContainerBuilder.build(), null, DataInjectionStrategy.NONE))
                .action(ActionType.COMPLETE, new Action(Start.CONF_CONTAINER, null, DataInjectionStrategy.NONE))
                .action(ActionType.VIEW, new Action(parentContainerBuilder.build(), null, DataInjectionStrategy.NONE))
                .allowAttachments()
                .allowAny();

        for (Container inputContainer : MULTISTEP_INPUT_CONTAINERS) {
            builder.appendFields(inputContainer.getFields());
        }

        return builder.build();
    }

    public static Activity remoteActivity(Container container, String page) {
        Container.Builder parentContainerBuilder = new Container.Builder()
                .children(INPUT_CONTAINERS)
                .activeChildIndex(container.getOrdinal());

        if (container.getOrdinal() == 1) {
            parentContainerBuilder
                    .title("FSR")
                    .button(new Button.Builder().type(Constants.ButtonTypes.SUBMIT).name("actionButton").label("Create").value("submit").ordinal(1).build());
        } else {
            parentContainerBuilder.title("{{BudgetNumber}} for {{ReportPeriod}}");

            if (container.getOrdinal() > 1)
                parentContainerBuilder.button(new Button.Builder().type(Constants.ButtonTypes.SUBMIT).name("actionButton").label("Return to Previous Step").value("reject").action(ActionType.REJECT.name()).ordinal(1).build());

            parentContainerBuilder
                    .button(new Button.Builder().type(Constants.ButtonTypes.SUBMIT).name("actionButton").label("Save").value("save").action(ActionType.SAVE.name()).ordinal(2).build())
                    .button(new Button.Builder().type(Constants.ButtonTypes.SUBMIT).name("actionButton").label("Submit").value("approve").action(ActionType.COMPLETE.name()).primary().ordinal(3).build());
        }

        Activity.Builder builder = new Activity.Builder()
                .elementType(FlowElementType.START_EVENT)
                .usageType(ActivityUsageType.MULTI_PAGE)
                .action(ActionType.CREATE, new Action(parentContainerBuilder.build(), page, DataInjectionStrategy.REMOTE))
                .action(ActionType.COMPLETE, new Action(Start.CONF_CONTAINER, page, DataInjectionStrategy.REMOTE))
                .action(ActionType.VIEW, new Action(parentContainerBuilder.build(), page, DataInjectionStrategy.REMOTE))
                .allowAttachments()
                .allowAny();

        for (int i=0;i<container.getOrdinal();i++) {
            Container inputContainer = INPUT_CONTAINERS.get(i);
            builder.appendFields(inputContainer.getFields());
        }

        return builder.build();
    }

    public static final class GeneralInformation {
        public static Field NAME = new Field.Builder()
                .type(Constants.FieldTypes.TEXT)
                .label("Name")
                .header("Entity name")
                .name("Name")
                .maxValueLength(75)
                .editable()
                .required()
                .ordinal(1)
                .build();

        public static Field STREET1 = new Field.Builder()
                .type(Constants.FieldTypes.TEXT)
                .label("Street Address")
                .header("Street 1")
                .name("Street1")
                .maxValueLength(40)
                .editable()
                .required()
                .ordinal(3)
                .build();

        public static Field STREET2 = new Field.Builder()
                .type(Constants.FieldTypes.TEXT)
                .header("Street 2")
                .name("Street2")
                .maxValueLength(40)
                .editable()
                .ordinal(4)
                .build();

        public static Field CITY = new Field.Builder()
                .type(Constants.FieldTypes.TEXT)
                .label("City")
                .name("City")
                .displayValueLength(29)
                .maxValueLength(29)
                .editable()
                .required()
                .ordinal(5)
                .build();

        public static Field STATE = new Field.Builder()
                .type(Constants.FieldTypes.SELECT_ONE)
                .label("State")
                .name("State")
                .constraint(new Constraint.Builder().type(Constants.ConstraintTypes.IS_STATE).build())
                .maxValueLength(2)
                .editable()
                .required()
                .ordinal(6)
                .build();

        public static Field POSTAL_CODE = new Field.Builder()
                .type(Constants.FieldTypes.TEXT)
                .label("Postal Code")
                .name("PostalCode")
                .mask("99999[-9999]")
                .displayValueLength(10)
                .maxValueLength(10)
                .editable()
                .required()
                .ordinal(7)
                .build();


        public static Set<Field> INPUT_FIELDS = Sets.newHashSet(NAME, STREET1, STREET2, CITY, STATE, POSTAL_CODE);

        public static Container INPUT_CONTAINER = new Container.Builder()
                .title("General Information")
                .breadcrumb("general")
                .fields(INPUT_FIELDS)
                .ordinal(1)
                .build();
    }

    public static final class ContactInformation {
        public static Field CONTACT_NAME = new Field.Builder()
                .type(Constants.FieldTypes.TEXT)
                .label("Contact Name")
                .name("ContactName")
                .displayValueLength(50)
                .maxValueLength(50)
                .editable()
                .required()
                .ordinal(1)
                .build();

        public static Field CONTACT_EMAIL = new Field.Builder()
                .type(Constants.FieldTypes.EMAIL)
                .label("Email address")
                .header("Contact email")
                .name("ContactEmail")
                .constraint(new Constraint.Builder().type(Constants.ConstraintTypes.IS_EMAIL_ADDRESS).build())
                .constraint(new Constraint.Builder().type(Constants.ConstraintTypes.IS_ALL_VALUES_MATCH).build())
                .displayValueLength(50)
                .maxValueLength(50)
                .minInputs(2)
                .maxInputs(2)
                .editable()
                .required()
                .ordinal(2)
                .build();

        public static Field CONTACT_PHONE = new Field.Builder()
                .type(Constants.FieldTypes.TEXT)
                .label("Phone")
                .header("Contact phone")
                .name("ContactPhone")
                .mask("(999) 999-9999 [99999]")
                .pattern("^\\(\\d{3}\\) \\d{3}-\\d{4}( \\d{0,5})?")
                .displayValueLength(20)
                .maxValueLength(20)
                .editable()
                .required()
                .ordinal(3)
                .build();

        public static Set<Field> INPUT_FIELDS = Sets.newHashSet(CONTACT_NAME, CONTACT_EMAIL, CONTACT_PHONE);

        public static Container INPUT_CONTAINER = new Container.Builder()
                .title("Contact Information")
                .fields(INPUT_FIELDS)
                .breadcrumb("contact")
                .ordinal(2)
                .build();
    }


    public static final class Start {

        public static Field confirmationField() {
            return new Field.Builder()
                    .type(Constants.FieldTypes.HTML)
                    .defaultValue("<h4>Thank you for completing this step</h4>")
                    .name("ConfirmationText")
                    .constraint(new Constraint.Builder().type(Constants.ConstraintTypes.IS_CONFIRMATION_NUMBER).build())
                    .build();
        }

        public static Container INPUT_CONTAINER = new Container.Builder()
                .title("Preliminary Information")
                .breadcrumb("Preliminary Information")
                .ordinal(1)
                .build();

        public static Container CONF_CONTAINER = new Container.Builder()
                .title("Task Complete")
                .fields(confirmationField())
                .ordinal(1)
                .build();
    }

    public static final class PreReconciliation {

        public static Container INPUT_CONTAINER = new Container.Builder()
                .title("Pre-reconciliation")
                .breadcrumb("Pre-reconciliation")
                .ordinal(2)
                .build();

    }

    public static final class Prep {

        public static Container INPUT_CONTAINER = new Container.Builder()
                .title("Prep")
                .breadcrumb("Prep")
                .ordinal(3)
                .build();
    }

    public static final class Analysis {

        public static Container INPUT_CONTAINER = new Container.Builder()
                .title("Analysis/Data Entry")
                .breadcrumb("Analysis/Data Entry")
                .ordinal(4)
                .build();
    }

    public static final class ManagerReview {

        public static Container INPUT_CONTAINER = new Container.Builder()
                .title("Manager Review")
                .breadcrumb("Manager Review")
                .ordinal(5)
                .build();

    }


    public static final class Submission {

        public static Container INPUT_CONTAINER = new Container.Builder()
                .title("Submission")
                .breadcrumb("Submission")
                .ordinal(6)
                .build();

    }

    public static List<Container> INPUT_CONTAINERS = Arrays.asList(Start.INPUT_CONTAINER, PreReconciliation.INPUT_CONTAINER, Prep.INPUT_CONTAINER, Analysis.INPUT_CONTAINER,
            ManagerReview.INPUT_CONTAINER, Submission.INPUT_CONTAINER);

    public static List<Container> MULTISTEP_INPUT_CONTAINERS = Arrays.asList(GeneralInformation.INPUT_CONTAINER, ContactInformation.INPUT_CONTAINER);
}
