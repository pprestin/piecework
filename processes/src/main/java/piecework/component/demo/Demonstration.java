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
package piecework.component.demo;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.engine.ProcessDeploymentResource;
import piecework.engine.ProcessEngineFacade;
import piecework.engine.exception.ProcessEngineException;
import piecework.enumeration.ActionType;
import piecework.exception.StatusCodeError;
import piecework.model.*;
import piecework.model.Process;
import piecework.service.ProcessInstanceService;
import piecework.service.ProcessService;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
@Service("Demonstration")
public class Demonstration implements TaskListener {

    private static final Logger LOG = Logger.getLogger(Demonstration.class);

    private static String NAMESPACE = "activiti";
    private static String PROCESS_DEFINITION_KEY = "Demonstration";

    @Autowired
    ProcessService processService;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    ProcessEngineFacade facade;

    Section APPROVAL_FORM_MAIN_SECTION = approvalFormMainSection();
    Section APPROVED_MAIN_SECTION = approvedMainSection();
    Section CONFIRMATION_MAIN_SECTION = thankYouMainSectionWithConfirmationNumber();
    Section INITIAL_FORM_MAIN_SECTION = initialFormMainSection();
    Section REJECTED_MAIN_SECTION = rejectedMainSection();
    Section REVIEW_FORM_MAIN_SECTION = reviewFormMainSection();


    @Override
    public void notify(DelegateTask delegateTask) {
        String processBusinessKey = delegateTask.getExecution().getProcessBusinessKey();
        String processInstanceId = delegateTask.getProcessInstanceId();
        String taskDefinitionKey = delegateTask.getTaskDefinitionKey();

        ProcessInstance processInstance;
        try {
            processInstance = processInstanceService.read(PROCESS_DEFINITION_KEY, processBusinessKey, true);
        } catch (StatusCodeError e) {
            LOG.error("Could not retrieve process instance ", e);
            throw new RuntimeException(e);
        }

        Map<String, List<Value>> values = processInstance != null ? processInstance.getData() : null;

        if (values == null) {
            LOG.error("Process instance could not be found, or form messages is null " + NAMESPACE + ", " + PROCESS_DEFINITION_KEY + ", " + processInstanceId);
            return;
        }

        if (taskDefinitionKey == null) {
            LOG.error("Task definition key is null " + NAMESPACE + ", " + PROCESS_DEFINITION_KEY + ", " + processInstanceId);
            return;
        }

        List<Value> submitters = values.get("Submitter");
        User submitter = submitters != null ? (User) submitters.iterator().next() : null;

        delegateTask.setAssignee(submitter.getUserId());
    }

    @PostConstruct
    public void configure() throws IOException, ProcessEngineException, StatusCodeError {
        Process process = demoProcess();
        ProcessDeployment deployment = demoProcessDeployment();

        ClassPathResource classPathResource = new ClassPathResource("META-INF/demo/Demonstration.bpmn20.xml");
        ProcessDeploymentResource resource = new ProcessDeploymentResource.Builder()
                .contentType("application/xml")
                .name("Demonstration.bpmn20.xml")
                .inputStream(classPathResource.getInputStream())
                .build();

        try {
            processService.read(process.getProcessDefinitionKey());
            processService.updateAndPublishDeployment(process, deployment, resource, false);
        } catch (MappingException mappingException) {
            LOG.fatal("Could not create Demonstration process because of a spring mapping exception", mappingException);
        } catch (Exception e) {
            processService.create(process);
            processService.createAndPublishDeployment(process, deployment, resource, false);
        }
    }

    public void synchronize() throws IOException, ProcessEngineException, StatusCodeError {
        Process process = demoProcess();
        ProcessDeployment deployment = demoProcessDeployment();

        ClassPathResource classPathResource = new ClassPathResource("META-INF/demo/Demonstration.bpmn20.xml");
        ProcessDeploymentResource resource = new ProcessDeploymentResource.Builder()
                .contentType("application/xml")
                .name("Demonstration.bpmn20.xml")
                .inputStream(classPathResource.getInputStream())
                .build();

        processService.updateAndPublishDeployment(process, deployment, resource, false);
    }

    public static Field approvedField() {
        return new Field.Builder()
                .type(Constants.FieldTypes.HTML)
                .defaultValue("<h4>Form has been approved. </h4>")
                .name("ApprovedText")
                .build();
    }

    public static Section approvedMainSection() {
        return new Section.Builder()
                .field(approvedField())
                .ordinal(1)
                .build();
    }

    public static Field rejectedField() {
        return new Field.Builder()
                .type(Constants.FieldTypes.HTML)
                .defaultValue("<h4>Form has been returned to reviewer. </h4>")
                .name("RejectedText")
                .build();
    }

    public static Section rejectedMainSection() {
        return new Section.Builder()
                .field(rejectedField())
                .ordinal(1)
                .build();
    }

    public Screen approvedScreen() {
        return new Screen.Builder()
                .title("Approval")
                .grouping(new Grouping.Builder()
                        .sectionId(APPROVED_MAIN_SECTION.getSectionId())
                        .ordinal(1)
                        .build())
                .attachmentAllowed(false)
                .build();
    }

    public Screen rejectedScreen() {
        return new Screen.Builder()
                .title("Rejection")
                .grouping(new Grouping.Builder()
                        .sectionId(REJECTED_MAIN_SECTION.getSectionId())
                        .ordinal(1)
                        .build())
                .attachmentAllowed(false)
                .build();
    }


    public static Section reviewFormMainSection() {
        return new Section.Builder()
                .tagId("mainSection")
                .title("Submitter Details")
                .field(new Field.Builder()
                        .type(Constants.FieldTypes.FILE)
                        .label("Profile photo")
                        .name("variableImage")
                        .accept("image/*")
                        .build())
                .field(new Field.Builder()
                        .type(Constants.FieldTypes.PERSON)
                        .label("Submitter")
                        .name("Submitter")
                        .defaultValue("rod")
//                        .constraint(new Constraint.Builder().type(Constants.ConstraintTypes.IS_VALID_USER).build())
                        .displayValueLength(40)
                        .maxValueLength(40)
                        .build())
                .field(new Field.Builder()
                        .type(Constants.FieldTypes.TEXT)
                        .label("What's your favorite band?")
                        .name("FavoriteBand")
                        .displayValueLength(40)
                        .maxValueLength(200)
                        .editable()
                        .build())
                .field(new Field.Builder()
                        .type(Constants.FieldTypes.SELECT_ONE)
                        .label("What's your favorite color")
                        .name("FavoriteColor")
                        .option(new Option.Builder().value("red").label("Red").build())
                        .option(new Option.Builder().value("orange").label("Orange").build())
                        .option(new Option.Builder().value("yellow").label("Yellow").build())
                        .option(new Option.Builder().value("green").label("Green").build())
                        .option(new Option.Builder().value("blue").label("Blue").build())
                        .option(new Option.Builder().value("indigo").label("Indigo").build())
                        .option(new Option.Builder().value("violet").label("Violet").build())
                        .editable()
                        .build())
                .field(new Field.Builder()
                        .type(Constants.FieldTypes.RADIO)
                        .label("Do you like dancing?")
                        .name("LikesDancing")
                        .option(new Option.Builder().value("yes").label("Yes").build())
                        .option(new Option.Builder().value("no").label("No").build())
                        .build())
                .field(new Field.Builder()
                        .type(Constants.FieldTypes.CHECKBOX)
                        .label("Can you whistle?")
                        .name("CanWhistle")
                        .option(new Option.Builder().value("yes").label("Yes").build())
                        .build())
                .field(new Field.Builder()
                        .type(Constants.FieldTypes.TEXTAREA)
                        .label("Any other comments")
                        .name("Comments")
                        .maxValueLength(500)
                        .build())
                .field(new Field.Builder()
                        .type(Constants.FieldTypes.FILE)
                        .label("Feel free to attach examples of your work")
                        .name("AdditionalFiles")
                        .accept("*/*")
                        .minInputs(0)
                        .maxInputs(30)
                        .editable()
                        .build())
                .ordinal(1)
                .build();
    }

    public static Section approvalFormMainSection() {
        return new Section.Builder()
                .tagId("mainSection")
                .title("Submitter Details")
                .field(new Field.Builder()
                        .type(Constants.FieldTypes.FILE)
                        .label("Profile photo")
                        .name("variableImage")
                        .accept("image/*")
                        .editable()
                        .build())
                .field(new Field.Builder()
                        .type(Constants.FieldTypes.PERSON)
                        .label("Submitter")
                        .name("Submitter")
                        .defaultValue("rod")
//                        .constraint(new Constraint.Builder().type(Constants.ConstraintTypes.IS_VALID_USER).build())
                        .displayValueLength(40)
                        .maxValueLength(40)
                        .build())
                .field(new Field.Builder()
                        .type(Constants.FieldTypes.TEXT)
                        .label("What's your favorite band?")
                        .name("FavoriteBand")
                        .displayValueLength(40)
                        .maxValueLength(200)
                        .editable()
                        .build())
                .field(new Field.Builder()
                        .type(Constants.FieldTypes.SELECT_ONE)
                        .label("What's your favorite color")
                        .name("FavoriteColor")
                        .option(new Option.Builder().value("red").label("Red").build())
                        .option(new Option.Builder().value("orange").label("Orange").build())
                        .option(new Option.Builder().value("yellow").label("Yellow").build())
                        .option(new Option.Builder().value("green").label("Green").build())
                        .option(new Option.Builder().value("blue").label("Blue").build())
                        .option(new Option.Builder().value("indigo").label("Indigo").build())
                        .option(new Option.Builder().value("violet").label("Violet").build())
                        .build())
                .field(new Field.Builder()
                        .type(Constants.FieldTypes.RADIO)
                        .label("Do you like dancing?")
                        .name("LikesDancing")
                        .option(new Option.Builder().value("yes").label("Yes").build())
                        .option(new Option.Builder().value("no").label("No").build())
                        .editable()
                        .required()
                        .build())
                .field(new Field.Builder()
                        .type(Constants.FieldTypes.CHECKBOX)
                        .label("Can you whistle?")
                        .name("CanWhistle")
                        .option(new Option.Builder().value("yes").label("Yes").build())
                        .editable()
                        .build())
                .field(new Field.Builder()
                        .type(Constants.FieldTypes.TEXTAREA)
                        .label("Any other comments")
                        .name("Comments")
                        .maxValueLength(500)
                        .editable()
                        .build())
                .field(new Field.Builder()
                        .type(Constants.FieldTypes.FILE)
                        .label("Feel free to attach examples of your work")
                        .name("AdditionalFiles")
                        .accept("*/*")
                        .minInputs(0)
                        .maxInputs(30)
                        .editable()
                        .build())
                .ordinal(1)
                .build();
    }

    public Screen reviewFormScreen() {
        return new Screen.Builder()
                .title("Review")
                .type(Constants.ScreenTypes.STANDARD)
                .grouping(new Grouping.Builder()
                        .breadcrumb("Initial")
                        .button(new Button.Builder().type(Constants.ButtonTypes.SUBMIT).name("actionButton").label("Save").value("save").action(ActionType.SAVE.name()).ordinal(1).build())
                        .button(new Button.Builder().type(Constants.ButtonTypes.SUBMIT).name("actionButton").label("Submit").value("submit").action(ActionType.COMPLETE.name()).ordinal(1).build())
                        .sectionId(REVIEW_FORM_MAIN_SECTION.getSectionId())
                        .ordinal(1)
                        .build())
                .attachmentAllowed(true)
                .maxActiveGroupingIndex(-1)
                .build();
    }

    public Screen approvalFormScreen() {
        return new Screen.Builder()
                .title("Approve")
                .type(Constants.ScreenTypes.STANDARD)
                .grouping(new Grouping.Builder()
                        .breadcrumb("Initial")
                        .button(new Button.Builder().type(Constants.ButtonTypes.SUBMIT).name("actionButton").label("Save").value("save").action(ActionType.SAVE.name()).ordinal(1).build())
                        .button(new Button.Builder().type(Constants.ButtonTypes.SUBMIT).name("actionButton").label("Return").value("reject").action(ActionType.REJECT.name()).ordinal(1).build())
                        .button(new Button.Builder().type(Constants.ButtonTypes.SUBMIT).name("actionButton").label("Approve").value("approve").action(ActionType.COMPLETE.name()).ordinal(2).build())
                        .sectionId(APPROVAL_FORM_MAIN_SECTION.getSectionId())
                        .ordinal(1)
                        .build())
                .attachmentAllowed(true)
                .maxActiveGroupingIndex(-1)
                .build();
    }

    public static Section initialFormMainSection() {
        return new Section.Builder()
                .tagId("mainSection")
                .title("Enter Details")
                .field(new Field.Builder()
                        .type(Constants.FieldTypes.FILE)
                        .label("Profile photo")
                        .name("variableImage")
                        .accept("image/*")
                        .editable()
                        .build())
                .field(new Field.Builder()
                        .type(Constants.FieldTypes.PERSON)
                        .label("Submitter")
                        .name("Submitter")
                        .defaultValue("rod")
//                        .constraint(new Constraint.Builder().type(Constants.ConstraintTypes.IS_VALID_USER).build())
                        .displayValueLength(40)
                        .maxValueLength(40)
                        .editable()
                        .required()
                        .build())
                .field(new Field.Builder()
                        .type(Constants.FieldTypes.TEXT)
                        .label("What's your favorite band?")
                        .name("FavoriteBand")
                        .displayValueLength(40)
                        .maxValueLength(200)
                        .editable()
                        .build())
                .field(new Field.Builder()
                        .type(Constants.FieldTypes.SELECT_ONE)
                        .label("What's your favorite color")
                        .name("FavoriteColor")
                        .option(new Option.Builder().value("red").label("Red").build())
                        .option(new Option.Builder().value("orange").label("Orange").build())
                        .option(new Option.Builder().value("yellow").label("Yellow").build())
                        .option(new Option.Builder().value("green").label("Green").build())
                        .option(new Option.Builder().value("blue").label("Blue").build())
                        .option(new Option.Builder().value("indigo").label("Indigo").build())
                        .option(new Option.Builder().value("violet").label("Violet").build())
                        .editable()
                        .required()
                        .build())
                .field(new Field.Builder()
                        .type(Constants.FieldTypes.RADIO)
                        .label("Do you like dancing?")
                        .name("LikesDancing")
                        .option(new Option.Builder().value("yes").label("Yes").build())
                        .option(new Option.Builder().value("no").label("No").build())
                        .editable()
                        .required()
                        .build())
                .field(new Field.Builder()
                        .type(Constants.FieldTypes.CHECKBOX)
                        .label("Can you whistle?")
                        .name("CanWhistle")
                        .option(new Option.Builder().value("yes").label("Yes").build())
                        .editable()
                        .build())
                .field(new Field.Builder()
                        .type(Constants.FieldTypes.TEXTAREA)
                        .label("Any other comments")
                        .name("Comments")
                        .maxValueLength(500)
                        .editable()
                        .build())
                .field(new Field.Builder()
                        .type(Constants.FieldTypes.FILE)
                        .label("Feel free to attach examples of your work")
                        .name("AdditionalFiles")
                        .accept("*/*")
                        .minInputs(0)
                        .maxInputs(30)
                        .editable()
                        .build())
                .ordinal(1)
                .build();
    }

    public Screen initialScreen() {
        return new Screen.Builder()
                .title("Initial Screen")
                .type(Constants.ScreenTypes.STANDARD)
                .grouping(new Grouping.Builder()
                        .breadcrumb("Initial")
                        .button(new Button.Builder().type(Constants.ButtonTypes.SUBMIT).name("actionButton").label("Submit").value("submit").ordinal(2).build())
                        .sectionId(INITIAL_FORM_MAIN_SECTION.getSectionId())
                        .ordinal(1)
                        .build())
                .attachmentAllowed(true)
                .maxActiveGroupingIndex(-1)
                .build();
    }

    public static Field confirmationField() {
        return new Field.Builder()
                .type(Constants.FieldTypes.HTML)
                .defaultValue("<h4>\n" +
                        "  Thank you for completing your task. \n" +
                        "  Your confirmation number is <b>{ConfirmationNumber}</b>.</h4> \n")
                .name("ConfirmationText")
                .constraint(new Constraint.Builder().type(Constants.ConstraintTypes.IS_CONFIRMATION_NUMBER).build())
                .build();
    }

    public static Section thankYouMainSectionWithConfirmationNumber() {
        return new Section.Builder()
                .tagId("confirmation")
                .field(confirmationField())
                .ordinal(1)
                .build();
    }

    public Screen genericTaskCompletedScreen() {
        return new Screen.Builder()
                .title("Task complete")
                .grouping(new Grouping.Builder()
                        .sectionId(CONFIRMATION_MAIN_SECTION.getSectionId())
                        .ordinal(1)
                        .build())
                .attachmentAllowed(false)
                .build();
    }

    public Interaction initialInteraction() {
        return new Interaction.Builder()
                .label("Initial Data Submission")
                .screen(initialScreen())
                .screen(ActionType.COMPLETE, genericTaskCompletedScreen())
                .build();
    }

    public Interaction reviewInteraction() {
        return new Interaction.Builder()
                .label("Review")
                .screen(reviewFormScreen())
                .screen(ActionType.COMPLETE, genericTaskCompletedScreen())
                .taskDefinitionKey("demonstrationReviewTask")
                .build();
    }

    public Interaction approvalInteraction() {
        return new Interaction.Builder()
                .label("Approval")
                .screen(approvalFormScreen())
                .screen(ActionType.COMPLETE, approvedScreen())
                .screen(ActionType.REJECT, rejectedScreen())
                .taskDefinitionKey("demonstrationApprovalTask")
                .build();
    }

    public ProcessDeployment demoProcessDeployment() {
        ProcessDeployment current = new ProcessDeployment.Builder()
                .processInstanceLabelTemplate("{{Submitter.DisplayName}} likes {{FavoriteColor}}")
                .engine("activiti")
                .engineProcessDefinitionKey("DemonstrationProcess")
                .engineProcessDefinitionLocation("classpath:META-INF/demo/Demonstration.bpmn20.xml")
                .base("classpath:META-INF/demo")
//                .interaction(initialInteraction())
//                .interaction(reviewInteraction())
//                .interaction(approvalInteraction())
//                .section(INITIAL_FORM_MAIN_SECTION)
//                .section(REVIEW_FORM_MAIN_SECTION)
//                .section(APPROVAL_FORM_MAIN_SECTION)
//                .section(CONFIRMATION_MAIN_SECTION)
//                .section(APPROVED_MAIN_SECTION)
//                .section(REJECTED_MAIN_SECTION)
                .build();
        return current;
    }

    public static Process demoProcess() {
        return new Process.Builder()
                .processDefinitionKey("Demonstration")
                .processDefinitionLabel("Demonstration Process")
                .allowAnonymousSubmission(true)
                .build();
    }

}
