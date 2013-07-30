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
package piecework.form.handler;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.common.ViewContext;
import piecework.engine.ProcessEngineFacade;
import piecework.form.FormService;
import piecework.identity.InternalUserDetailsService;
import piecework.process.ProcessInstanceService;
import piecework.task.TaskCriteria;
import piecework.engine.exception.ProcessEngineException;
import piecework.exception.InternalServerError;
import piecework.exception.NotFoundError;
import piecework.exception.StatusCodeError;
import piecework.form.validation.FormValidation;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessRepository;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.ui.StreamingAttachmentContent;
import piecework.ui.StreamingPageContent;
import piecework.persistence.ContentRepository;
import piecework.util.ConstraintUtil;
import piecework.util.ManyMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;


/**
 * @author James Renfro
 */
@Service
public class ResponseHandler {

    private static final Logger LOG = Logger.getLogger(ResponseHandler.class);

    @Autowired
    ContentRepository contentRepository;

    @Autowired
    Environment environment;

    @Autowired
    ProcessEngineFacade facade;

    @Autowired
    FormService formService;

    @Autowired
    ProcessRepository processRepository;

    @Autowired
    ProcessInstanceService processInstanceService;


    public Response handle(FormRequest formRequest, ViewContext viewContext) throws StatusCodeError {
        return handle(formRequest, viewContext, null);
    }

    public Response handle(FormRequest formRequest, ViewContext viewContext, FormValidation validation) throws StatusCodeError {

        Form form = buildResponseForm(formRequest, viewContext, validation);

        if (form != null && form.getScreen() != null) {
            String location = form.getScreen().getLocation();

            if (StringUtils.isNotEmpty(location)) {
                // If the location is not blank then retrieve from that location
                Content content;
                if (location.startsWith("classpath:")) {
                    ClassPathResource resource = new ClassPathResource(location.substring("classpath:".length()));
                    try {
                        content = new Content.Builder().inputStream(resource.getInputStream()).contentType("text/html").build();
                    } catch (IOException e) {
                        throw new InternalServerError();
                    }
                } else if (location.startsWith("file:")) {
                    FileSystemResource resource = new FileSystemResource(location.substring("file:".length()));
                    try {
                        content = new Content.Builder().inputStream(resource.getInputStream()).contentType("text/html").build();
                    } catch (IOException e) {
                        throw new InternalServerError();
                    }
                } else {
                    content = contentRepository.findByLocation(location);
                }
                return Response.ok(new StreamingPageContent(form, content), content.getContentType()).build();
            }
        }

        return Response.ok(form).build();
    }

    public Form buildResponseForm(FormRequest formRequest, ViewContext viewContext, FormValidation validation) throws StatusCodeError {
        int attachmentCount = 0;
        ProcessInstance processInstance = null;
        Map<String, FormValue> combinedFormValueMap = new HashMap<String, FormValue>();
        if (StringUtils.isNotBlank(formRequest.getProcessInstanceId())) {
            processInstance = processInstanceService.read(formRequest.getProcessDefinitionKey(), formRequest.getProcessInstanceId());
            Map<String, FormValue> processInstanceFormValueMap = processInstance.getFormValueMap();
            attachmentCount = processInstance.getAttachments() != null ? processInstance.getAttachments().size() : 0;
            combinedFormValueMap.putAll(processInstanceFormValueMap);
        }

        List<ValidationResult> results = validation != null ? validation.getResults() : null;
        Map<String, ValidationResult> resultMap = new HashMap<String, ValidationResult>();
        if (results != null && !results.isEmpty()) {
            for (ValidationResult result : results) {
                resultMap.put(result.getPropertyName(), result);
            }
        }

        Map<String, FormValue> validationFormValueMap = validation != null ? validation.getFormValueMap() : null;
        if (validationFormValueMap != null)
            combinedFormValueMap.putAll(validationFormValueMap);
        Set<String> includedFieldNames = new HashSet<String>();

        Screen screen = formRequest.getScreen();
        Task task = null;

        Process process = processRepository.findOne(formRequest.getProcessDefinitionKey());
        if (process == null)
            throw new NotFoundError(Constants.ExceptionCodes.process_does_not_exist);

        if (StringUtils.isNotEmpty(formRequest.getTaskId())) {

            TaskCriteria criteria = new TaskCriteria.Builder()
                    .process(process)
                    .taskId(formRequest.getTaskId())
                    .build();

            try {
                task = facade.findTask(criteria);
                if (task == null)
                    throw new NotFoundError(Constants.ExceptionCodes.task_does_not_exist);
            } catch (ProcessEngineException e) {
                LOG.error("Process engine unable to find task ", e);
                throw new InternalServerError();
            }

            if (screen == null) {
                Interaction selectedInteraction = selectInteraction(process, task);

                if (selectedInteraction != null && !selectedInteraction.getScreens().isEmpty())
                    screen = selectedInteraction.getScreens().iterator().next();
            }
        }

        PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
        Map<String, Field> fieldMap = new HashMap<String, Field>();
        if (screen != null) {
            Screen.Builder screenBuilder = new Screen.Builder(screen, passthroughSanitizer, false);
            Map<String, Section> sectionMap = process.getSectionMap();
            List<Grouping> groupings = screen.getGroupings();

            for (Grouping grouping : groupings) {
                if (grouping == null)
                    continue;
                List<String> sectionsIds = grouping.getSectionIds();
                if (sectionsIds == null)
                    continue;

                for (String sectionId : sectionsIds) {
                    Section section = sectionMap.get(sectionId);
                    if (section == null)
                        continue;
                    Section.Builder sectionBuilder = new Section.Builder(section, passthroughSanitizer, false);

                    for (Field field : section.getFields()) {
                        Field.Builder fieldBuilder = new Field.Builder(field, passthroughSanitizer)
                                .processDefinitionKey(formRequest.getProcessDefinitionKey())
                                .processInstanceId(formRequest.getProcessInstanceId());

                        List<Constraint> constraints = field.getConstraints();
                        if (constraints != null) {
                            if (!ConstraintUtil.checkAll(Constants.ConstraintTypes.IS_ONLY_VISIBLE_WHEN, fieldMap, combinedFormValueMap, constraints))
                                fieldBuilder.invisible();
                            if (ConstraintUtil.hasConstraint(Constants.ConstraintTypes.IS_STATE, constraints))
                                addStateOptions(fieldBuilder);
                            if (ConstraintUtil.hasConstraint(Constants.ConstraintTypes.IS_CONFIRMATION_NUMBER, constraints))
                                addConfirmationNumber(fieldBuilder, formRequest.getProcessInstanceId());
                            if (ConstraintUtil.hasConstraint(Constants.ConstraintTypes.IS_VALID_USER, constraints)) {
                                includedFieldNames.add(field.getName() + "__displayName");
                                includedFieldNames.add(field.getName() + "__visibleId");
                            }
                        }
                        String fieldName = field.getName();

                        if (StringUtils.isNotEmpty(fieldName)) {
                            includedFieldNames.add(fieldName);
                            fieldMap.put(fieldName, field);
                        }
                        sectionBuilder.field(fieldBuilder.build(processInstanceService.getInstanceViewContext()));
                    }
                    screenBuilder.section(sectionBuilder.build());
                }
            }

            if (task != null && (task.isDeleted() || !task.isActive())) {
                screenBuilder.location(null);
                screenBuilder.readonly();
            }

            screen = screenBuilder.build();
        }

        List<FormValue> includedFormValues = new ArrayList<FormValue>();
        if (combinedFormValueMap != null) {
            for (Map.Entry<String, FormValue> entry : combinedFormValueMap.entrySet()) {
                String formValueName = entry.getKey();
                if (includedFieldNames.contains(formValueName)) {
                    Field field = fieldMap.get(formValueName);
                    FormValue formValue = entry.getValue();
                    if (formValue == null)
                        continue;

                    FormValue.Builder formValueBuilder = new FormValue.Builder(formValue, passthroughSanitizer);

                    if (field != null && field.getType() != null && field.getType().equals(Constants.FieldTypes.FILE)) {
                        formValueBuilder.processDefinitionKey(formRequest.getProcessDefinitionKey()).formInstanceId(formRequest.getRequestId());
                    }

                    ValidationResult result = resultMap.remove(formValueName);
                    if (result != null) {
                        formValueBuilder.message(new Message.Builder().type(result.getType()).text(result.getMessage()).build());
                    }

                    includedFormValues.add(formValueBuilder.build(formService.getFormViewContext()));
                }
            }
        }

        // Add any missing form values that have validation results -- usually will be fields that are required
        for (String includedFieldName : includedFieldNames) {
            ValidationResult result = resultMap.remove(includedFieldName);
            if (result != null) {
                FormValue.Builder formValueBuilder = new FormValue.Builder().name(includedFieldName)
                        .message(new Message.Builder().type(result.getType()).text(result.getMessage())
                                .build());

                includedFormValues.add(formValueBuilder.build());
            }
        }

        List<Attachment> attachments = processInstance != null ? processInstance.getAttachments() : null;

        return new Form.Builder()
                .formInstanceId(formRequest.getRequestId())
                .processDefinitionKey(formRequest.getProcessDefinitionKey())
                .submissionType(formRequest.getSubmissionType())
                .formValues(includedFormValues)
                .screen(screen)
                .task(task)
                .instanceSubresources(formRequest.getProcessDefinitionKey(), formRequest.getProcessInstanceId(), attachments, processInstanceService.getInstanceViewContext())
                .attachmentCount(attachmentCount)
                .build(viewContext);
    }

    public Response redirect(FormRequest formRequest, ViewContext viewContext) throws StatusCodeError {
        String hostUri = environment.getProperty("host.uri");
        return Response.status(Response.Status.SEE_OTHER).header(HttpHeaders.LOCATION, hostUri + formService.getFormViewContext().getApplicationUri(formRequest.getProcessDefinitionKey(), formRequest.getRequestId())).build();
    }

    private void addConfirmationNumber(Field.Builder fieldBuilder, String confirmationNumber) {
        String defaultValue = fieldBuilder.getDefaultValue();
        fieldBuilder.defaultValue(defaultValue.replaceAll("\\{ConfirmationNumber\\}", confirmationNumber));
    }

    private void addStateOptions(Field.Builder fieldBuilder) {
        fieldBuilder.option(new Option.Builder().value("").name("").build())
            .option(new Option.Builder().value("AL").name("Alabama").build())
            .option(new Option.Builder().value("AK").name("Alaska").build())
            .option(new Option.Builder().value("AZ").name("Arizona").build())
            .option(new Option.Builder().value("AR").name("Arkansas").build())
            .option(new Option.Builder().value("CA").name("California").build())
            .option(new Option.Builder().value("CO").name("Colorado").build())
            .option(new Option.Builder().value("CT").name("Connecticut").build())
            .option(new Option.Builder().value("DE").name("Delaware").build())
            .option(new Option.Builder().value("DC").name("District Of Columbia").build())
            .option(new Option.Builder().value("FL").name("Florida").build())
            .option(new Option.Builder().value("GA").name("Georgia").build())
            .option(new Option.Builder().value("HI").name("Hawaii").build())
            .option(new Option.Builder().value("ID").name("Idaho").build())
            .option(new Option.Builder().value("IL").name("Illinois").build())
            .option(new Option.Builder().value("IN").name("Indiana").build())
            .option(new Option.Builder().value("IA").name("Iowa").build())
            .option(new Option.Builder().value("KS").name("Kansas").build())
            .option(new Option.Builder().value("KY").name("Kentucky").build())
            .option(new Option.Builder().value("LA").name("Louisiana").build())
            .option(new Option.Builder().value("ME").name("Maine").build())
            .option(new Option.Builder().value("MD").name("Maryland").build())
            .option(new Option.Builder().value("MA").name("Massachusetts").build())
            .option(new Option.Builder().value("MI").name("Michigan").build())
            .option(new Option.Builder().value("MN").name("Minnesota").build())
            .option(new Option.Builder().value("MS").name("Mississippi").build())
            .option(new Option.Builder().value("MO").name("Missouri").build())
            .option(new Option.Builder().value("MT").name("Montana").build())
            .option(new Option.Builder().value("NE").name("Nebraska").build())
            .option(new Option.Builder().value("NV").name("Nevada").build())
            .option(new Option.Builder().value("NH").name("New Hampshire").build())
            .option(new Option.Builder().value("NJ").name("New Jersey").build())
            .option(new Option.Builder().value("NM").name("New Mexico").build())
            .option(new Option.Builder().value("NY").name("New York").build())
            .option(new Option.Builder().value("NC").name("North Carolina").build())
            .option(new Option.Builder().value("ND").name("North Dakota").build())
            .option(new Option.Builder().value("OH").name("Ohio").build())
            .option(new Option.Builder().value("OK").name("Oklahoma").build())
            .option(new Option.Builder().value("OR").name("Oregon").build())
            .option(new Option.Builder().value("PA").name("Pennsylvania").build())
            .option(new Option.Builder().value("RI").name("Rhode Island").build())
            .option(new Option.Builder().value("SC").name("South Carolina").build())
            .option(new Option.Builder().value("SD").name("South Dakota").build())
            .option(new Option.Builder().value("TN").name("Tennessee").build())
            .option(new Option.Builder().value("TX").name("Texas").build())
            .option(new Option.Builder().value("UT").name("Utah").build())
            .option(new Option.Builder().value("VT").name("Vermont").build())
            .option(new Option.Builder().value("VA").name("Virginia").build())
            .option(new Option.Builder().value("WA").name("Washington").build())
            .option(new Option.Builder().value("WV").name("West Virginia").build())
            .option(new Option.Builder().value("WI").name("Wisconsin").build())
            .option(new Option.Builder().value("WY").name("Wyoming").build());
    }

//    private List<FormValue> findFormValues(FormRequest formRequest, FormValidation validation) {
//        List<FormValue> formValues = null;
//        if (validation != null && validation.getResults() != null) {
//            Map<String, List<String>> validationFormValueMap = validation.getFormValueMap();
//            formValues = new ArrayList<FormValue>();
//            for (ValidationResult result : validation.getResults()) {
//                List<String> values = validationFormValueMap != null ? validationFormValueMap.get(result.getPropertyName()) : null;
//                formValues.add(new FormValue.Builder()
//                        .name(result.getPropertyName())
//                        .values(values)
//                        .message(new Message.Builder()
//                                .text(result.getMessage())
//                                .type(result.getType())
//                                .build())
//                        .build());
//            }
//        }
//
//        return formValues;
//    }

    private Interaction selectInteraction(Process process, Task task) {
        Interaction selectedInteraction = null;
        List<Interaction> interactions = process.getInteractions();
        if (interactions != null && !interactions.isEmpty()) {
            for (Interaction interaction : interactions) {
                if (interaction.getTaskDefinitionKeys().contains(task.getTaskDefinitionKey())) {
                    selectedInteraction = interaction;
                    break;
                }
            }
        }
        return selectedInteraction;
    }

}
