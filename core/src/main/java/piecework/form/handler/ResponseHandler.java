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
import piecework.common.ViewContext;
import piecework.form.FormFactory;
import piecework.form.FormService;
import piecework.form.validation.ValidationService;
import piecework.process.ProcessInstanceService;
import piecework.exception.InternalServerError;
import piecework.exception.StatusCodeError;
import piecework.form.validation.FormValidation;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessRepository;
import piecework.ui.StreamingPageContent;
import piecework.persistence.ContentRepository;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;


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
    FormFactory formFactory;

    @Autowired
    FormService formService;

    @Autowired
    ProcessRepository processRepository;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    ValidationService validationService;


    public Response handle(FormRequest formRequest, Process process) throws StatusCodeError {
        return handle(formRequest, process, null, null);
    }

    public Response handle(FormRequest formRequest, Process process, Task task, FormValidation validation) throws StatusCodeError {

        Form form = formFactory.form(formRequest, process, task, validation);

        if (form != null && form.getScreen() != null && !form.getScreen().isReadonly()) {
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

/*    public Form buildResponseForm(FormRequest formRequest, ViewContext viewContext, FormValidation validation) throws StatusCodeError {
        int attachmentCount = 0;
        ProcessInstance processInstance = null;
        ManyMap<String, Value> combinedFormValueMap = new ManyMap<String, Value>();
        if (StringUtils.isNotBlank(formRequest.getProcessInstanceId())) {
            processInstance = processInstanceService.read(formRequest.getProcessDefinitionKey(), formRequest.getProcessInstanceId());
            Map<String, List<Value>> processInstanceFormValueMap = processInstance.getData();
            attachmentCount = processInstance.getAttachments() != null ? processInstance.getAttachments().size() : 0;
            combinedFormValueMap.putAll(validationService.decrypted(processInstanceFormValueMap));
        }

        List<ValidationResult> results = validation != null ? validation.getResults() : null;
        Map<String, ValidationResult> resultMap = new HashMap<String, ValidationResult>();
        if (results != null && !results.isEmpty()) {
            for (ValidationResult result : results) {
                resultMap.put(result.getPropertyName(), result);
            }
        }

        Map<String, List<Value>> validationFormValueMap = validation != null ? validation.getData() : null;
        if (validationFormValueMap != null)
            combinedFormValueMap.putAll(validationService.decrypted(validationFormValueMap));

//        Set<String> includedFieldNames = new HashSet<String>();

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

        List<FormValue> includedFormValues = new ArrayList<FormValue>();
        PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
//        Map<String, Field> fieldMap = new HashMap<String, Field>();
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
                            if (!ConstraintUtil.checkAll(Constants.ConstraintTypes.IS_ONLY_VISIBLE_WHEN, null, combinedFormValueMap, constraints))
                                fieldBuilder.invisible();
                            if (ConstraintUtil.hasConstraint(Constants.ConstraintTypes.IS_STATE, constraints))
                                addStateOptions(fieldBuilder);
                            if (ConstraintUtil.hasConstraint(Constants.ConstraintTypes.IS_CONFIRMATION_NUMBER, constraints))
                                addConfirmationNumber(fieldBuilder, formRequest.getProcessInstanceId());
//                            if (ConstraintUtil.hasConstraint(Constants.ConstraintTypes.IS_VALID_USER, constraints)) {
//                                includedFieldNames.add(field.getName() + "__displayName");
//                                includedFieldNames.add(field.getName() + "__visibleId");
//                            }
                        }
                        String fieldName = field.getName();

                        if (StringUtils.isNotEmpty(fieldName)) {
//                            includedFieldNames.add(fieldName);
//                            fieldMap.put(fieldName, field);

                            List<Value> values = combinedFormValueMap.get(fieldName);
                            ValidationResult result = resultMap.get(fieldName);


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

//        List<FormValue> includedFormValues = new ArrayList<FormValue>();
//        if (combinedFormValueMap != null) {
//            for (Map.Entry<String, List<Value>> entry : combinedFormValueMap.entrySet()) {
//                String formValueName = entry.getKey();
//                if (includedFieldNames.contains(formValueName)) {
//                    Field field = fieldMap.get(formValueName);
//                    List<Value> messages = entry.getValue();
//                    if (messages == null)
//                        continue;
//
//                    FormValue.Builder formValueBuilder = new FormValue.Builder().messages(messages);
//
////                    if (field != null && field.getType() != null && field.getType().equals(Constants.FieldTypes.FILE)) {
////                        formValueBuilder.processDefinitionKey(formRequest.getProcessDefinitionKey()).formInstanceId(formRequest.getRequestId());
////                    }
//
//                    ValidationResult result = resultMap.remove(formValueName);
//                    if (result != null) {
//                        formValueBuilder.message(new Message.Builder().type(result.getType()).text(result.getMessage()).build());
//                    }
//
//                    includedFormValues.add(formValueBuilder.build(formService.getFormViewContext()));
//                }
//            }
//        }
//
//        // Add any missing form messages that have validation results -- usually will be fields that are required
//        for (String includedFieldName : includedFieldNames) {
//            ValidationResult result = resultMap.remove(includedFieldName);
//            if (result != null) {
//                FormValue.Builder formValueBuilder = new FormValue.Builder().name(includedFieldName)
//                        .message(new Message.Builder().type(result.getType()).text(result.getMessage())
//                                .build());
//
//                includedFormValues.add(formValueBuilder.build());
//            }
//        }


    }  */

    public Response redirect(FormRequest formRequest, ViewContext viewContext) throws StatusCodeError {
        String hostUri = environment.getProperty("host.uri");
        return Response.status(Response.Status.SEE_OTHER).header(HttpHeaders.LOCATION, hostUri + formService.getFormViewContext().getApplicationUri(formRequest.getProcessDefinitionKey(), formRequest.getRequestId())).build();
    }

}
