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
import piecework.enumeration.ActionType;
import piecework.enumeration.ActivityUsageType;
import piecework.enumeration.DataInjectionStrategy;
import piecework.enumeration.FlowElementType;
import piecework.model.*;

import java.util.Arrays;
import java.util.List;

/**
 * @author James Renfro
 */
public class ProcessFactory {

    private static final String CUSTOM_WEB_PAGE = "/path/to/CustomWebPage.html";


    public static piecework.model.Process process(String processDefinitionKey, String label) {
        return new piecework.model.Process.Builder()
                .processDefinitionKey(processDefinitionKey)
                .processDefinitionLabel(label)
                .build();
    }

    public static ProcessDeployment remoteStrategyProcessDeployment() {
        ProcessDeployment current = new ProcessDeployment.Builder()
                .deploymentLabel("First revision")
                .processInstanceLabelTemplate("{{BudgetNumber}} for {{ReportPeriod}}")
                .engine("activiti")
                .engineProcessDefinitionKey("SomeEngineKey.v2")
                .engineProcessDefinitionLocation("classpath:META-INF/some/path/SomeEngineKey.v2.bpmn20.xml")
                .base(" classpath:META-INF/uw/gca")
                .remoteHost("https://some.institution.org")
                .startActivityKey("start")
                .activity("start", activity(Start.INPUT_CONTAINER, CUSTOM_WEB_PAGE))
                .activity("preliminary", activity(Start.INPUT_CONTAINER, CUSTOM_WEB_PAGE))
                .activity("preReconciliation", activity(PreReconciliation.INPUT_CONTAINER, CUSTOM_WEB_PAGE))
                .activity("prep", activity(Prep.INPUT_CONTAINER, CUSTOM_WEB_PAGE))
                .activity("analysis", activity(Analysis.INPUT_CONTAINER, CUSTOM_WEB_PAGE))
                .activity("managerReview", activity(ManagerReview.INPUT_CONTAINER, CUSTOM_WEB_PAGE))
                .activity("submission", activity(Submission.INPUT_CONTAINER, CUSTOM_WEB_PAGE))
                .build();
        return current;
    }

    public static Activity activity(Container container, String page) {
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

}
