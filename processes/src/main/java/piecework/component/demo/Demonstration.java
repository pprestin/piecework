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

import com.google.common.collect.Sets;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.SystemUser;
import piecework.content.ContentResource;
import piecework.content.concrete.BasicContentResource;
import piecework.engine.ProcessEngineFacade;
import piecework.engine.exception.ProcessEngineException;
import piecework.enumeration.ActionType;
import piecework.enumeration.ActivityUsageType;
import piecework.enumeration.DataInjectionStrategy;
import piecework.enumeration.FlowElementType;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ModelProviderFactory;
import piecework.persistence.ProcessInstanceProvider;
import piecework.service.ProcessService;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author James Renfro
 */
@Service("Demonstration")
public class Demonstration implements TaskListener {

    private static final Logger LOG = Logger.getLogger(Demonstration.class);

    private static String NAMESPACE = "activiti";
    private static String PROCESS_DEFINITION_KEY = "Demonstration";

    @Autowired
    Environment environment;

    @Autowired
    ProcessService processService;

    @Autowired
    ModelProviderFactory modelProviderFactory;

    @Autowired
    ProcessEngineFacade facade;

    @Override
    public void notify(DelegateTask delegateTask) {
        String processBusinessKey = delegateTask.getExecution().getProcessBusinessKey();
        String processInstanceId = delegateTask.getProcessInstanceId();
        String taskDefinitionKey = delegateTask.getTaskDefinitionKey();

        ProcessInstanceProvider instanceProvider = modelProviderFactory.instanceProvider(PROCESS_DEFINITION_KEY, processBusinessKey, new SystemUser());

        ProcessInstance processInstance;
        try {
            processInstance = instanceProvider.instance();
        } catch (PieceworkException e) {
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

        if (submitter != null)
            delegateTask.setAssignee(submitter.getUserId());
    }

    @PostConstruct
    public void configure() throws IOException, PieceworkException {
        boolean isDemoMode = environment.getProperty("demo.mode", Boolean.class, Boolean.FALSE);

        if (!isDemoMode)
            return;

        synchronize();

//        Process process = demoProcess();
//        ProcessDeployment deployment = demoProcessDeployment();
//
//        ClassPathResource classPathResource = new ClassPathResource("META-INF/demo/Demonstration.bpmn20.xml");
//        ContentResource resource = new BasicContentResource.Builder()
//                .contentType("application/xml")
//                .filename("Demonstration.bpmn20.xml")
//                .inputStream(classPathResource.getInputStream())
//                .build();
//
//        processService.synchronize(process, deployment, resource);

//        try {
////            ProcessDeploymentProvider deploymentProvider = modelProviderFactory.deploymentProvider(process.getProcessDefinitionKey(), new SystemUser());
//            processService.update(process.getProcessDefinitionKey(), process, new SystemUser());
//            processService.updateAndPublishDeployment(process, deployment, resource, false, new SystemUser());
//        } catch (MappingException mappingException) {
//            LOG.fatal("Could not create Demonstration process because of a spring mapping exception", mappingException);
//        } catch (Exception e) {
//            processService.create(process);
//            processService.createAndPublishDeployment(process, deployment, resource, false, new SystemUser());
//        }
    }

    public String getProcessDefinitionKey() {
        return PROCESS_DEFINITION_KEY;
    }

    public void synchronize() throws IOException, PieceworkException {
        Process process = demoProcess();
        ProcessDeployment deployment = demoProcessDeployment();

        ClassPathResource classPathResource = new ClassPathResource("META-INF/demo/Demonstration.bpmn20.xml");
        ContentResource resource = new BasicContentResource.Builder()
                .contentType("application/xml")
                .filename("Demonstration.bpmn20.xml")
                .inputStream(classPathResource.getInputStream())
                .build();

//        processService.updateAndPublishDeployment(process, deployment, resource, false, new SystemUser());
        processService.synchronize(process, deployment, resource);
    }

    public static Field approvedField() {
        return new Field.Builder()
                .type(Constants.FieldTypes.HTML)
                .defaultValue("<h4>Form has been approved. </h4>")
                .name("ApprovedText")
                .build();
    }

    public static Field rejectedField() {
        return new Field.Builder()
                .type(Constants.FieldTypes.HTML)
                .defaultValue("<h4>Form has been returned to reviewer. </h4>")
                .name("RejectedText")
                .build();
    }


    public ProcessDeployment demoProcessDeployment() {
        ProcessDeployment current = new ProcessDeployment.Builder()
                .processInstanceLabelTemplate("{{Submitter.DisplayName}} likes {{FavoriteColor}}")
                .engine("activiti")
                .engineProcessDefinitionKey("DemonstrationProcess")
                .engineProcessDefinitionLocation("classpath:META-INF/demo/Demonstration.bpmn20.xml")
                .base("classpath:META-INF/demo")
                .startActivityKey("start")
                .activity("start", multistepActivity())
                .activity("demonstrationReviewTask", multistepActivity())
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

    public static List<Container> MULTISTEP_INPUT_CONTAINERS = Arrays.asList(GeneralInformation.INPUT_CONTAINER, ContactInformation.INPUT_CONTAINER);

}
