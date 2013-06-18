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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.common.view.ViewContext;
import piecework.exception.StatusCodeError;
import piecework.form.validation.FormValidation;
import piecework.model.*;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.ui.StreamingPageContent;
import piecework.persistence.ContentRepository;
import piecework.util.ConstraintUtil;
import piecework.util.FormDataUtil;
import piecework.util.ManyMap;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * @author James Renfro
 */
@Service
public class ResponseHandler {

    @Autowired
    ContentRepository contentRepository;

    public Response handle(FormRequest formRequest, List<FormValue> formValues, ViewContext viewContext) throws StatusCodeError {

        Screen screen = buildScreen(formRequest, formValues);

        Form form = new Form.Builder()
                .formInstanceId(formRequest.getRequestId())
                .processDefinitionKey(formRequest.getProcessDefinitionKey())
                .submissionType(formRequest.getSubmissionType())
                .formValues(formValues)
                .screen(screen)
                .build(viewContext);

        if (screen != null) {
            String location = screen.getLocation();

            if (StringUtils.isNotEmpty(location)) {
                // If the location is not blank then delegate to the
                Content content = contentRepository.findByLocation(location);
                String contentType = content.getContentType();
                return Response.ok(new StreamingPageContent(form, content), contentType).build();
            }
        }

        return Response.ok(form).build();
    }

    public Response handleBadRequest(FormRequest formRequest, FormValidation validation, ViewContext viewContext) {

        List<FormValue> formValues = new ArrayList<FormValue>();

        if (validation != null && validation.getResults() != null) {
            Map<String, List<String>> validationFormValueMap = validation.getFormValueMap();
            for (ValidationResult result : validation.getResults()) {
                formValues.add(new FormValue.Builder()
                        .name(result.getPropertyName())
                        .values(validationFormValueMap.get(result.getPropertyName()))
                        .message(new Message.Builder()
                                .text(result.getMessage())
                                .type(result.getType())
                                .build())
                        .build());
            }
        }

        Screen screen = buildScreen(formRequest, formValues);

        Form form = new Form.Builder()
                .formInstanceId(formRequest.getRequestId())
                .processDefinitionKey(formRequest.getProcessDefinitionKey())
                .submissionType(formRequest.getSubmissionType())
                .formValues(formValues)
                .screen(screen)
                .invalid()
                .build(viewContext);

        if (screen != null) {
            String location = screen.getLocation();

            if (StringUtils.isNotEmpty(location)) {
                // If the location is not blank then delegate to the
                Content content = contentRepository.findByLocation(location);
                String contentType = content.getContentType();
                return Response.ok(new StreamingPageContent(form, content), contentType).build();
            }
        }

        return Response.ok(form).build();
    }

    public Screen buildScreen(FormRequest formRequest, List<FormValue> formValues) {
        Screen screen = formRequest.getScreen();

        if (screen != null) {
            ManyMap<String, String> formValueMap = FormDataUtil.getFormValueMap(formValues);

            PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
            Screen.Builder screenBuilder = new Screen.Builder(screen, passthroughSanitizer, false);

            if (screen.getSections() != null) {
                Map<String, Field> fieldMap = new HashMap<String, Field>();
                for (Section section : screen.getSections()) {
                    if (section.getFields() == null)
                        continue;

                    for (Field field : section.getFields()) {
                        if (field.getName() == null)
                            continue;

                        fieldMap.put(field.getName(), field);
                    }
                }
                for (Section section : screen.getSections()) {
                    Section.Builder sectionBuilder = new Section.Builder(section, passthroughSanitizer, false);

                    for (Field field : section.getFields()) {
                        Field.Builder fieldBuilder = new Field.Builder(field, passthroughSanitizer);

                        List<Constraint> constraints = field.getConstraints();
                        if (constraints != null) {
                            if (!ConstraintUtil.checkAll(Constants.ConstraintTypes.IS_ONLY_VISIBLE_WHEN, fieldMap, formValueMap, constraints))
                                fieldBuilder.invisible();
                            if (ConstraintUtil.hasConstraint(Constants.ConstraintTypes.IS_STATE, constraints))
                                addStateOptions(fieldBuilder);
                        }

                        sectionBuilder.field(fieldBuilder.build());
                    }
                    screenBuilder.section(sectionBuilder.build());
                }
            }
            screen = screenBuilder.build();
        }

        return screen;
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

//    private boolean isVisible(Map<String, Field> fieldMap, List<Constraint> constraints, boolean requireAll) {
//        if (constraints == null || constraints.isEmpty())
//            return true;
//
//        boolean visible = requireAll;
//        for (Constraint constraint : constraints) {
//            String constraintType = constraint.getType();
//
//            if (constraintType == null)
//                continue;
//
//            boolean satisfied = false;
//            if (constraintType.equals(Constants.ConstraintTypes.IS_ONLY_VISIBLE_WHEN)) {
//                String constraintName = constraint.getName();
//                String constraintValue = constraint.getValue();
//                Pattern pattern = Pattern.compile(constraintValue);
//
//                Field constraintField = fieldMap.get(constraintName);
//
//                if (constraintField != null) {
//                    String defaultFieldValue = constraintField.getDefaultValue();
//                    satisfied = defaultFieldValue != null && pattern.matcher(defaultFieldValue).matches();
//                }
//            } else if (constraintType.equals(Constants.ConstraintTypes.AND)) {
//                satisfied = isVisible(fieldMap, constraint.getSubconstraints(), true);
//            } else if (constraintType.equals(Constants.ConstraintTypes.OR)) {
//                satisfied = isVisible(fieldMap, constraint.getSubconstraints(), false);
//            } else {
//                continue;
//            }
//
//            if (requireAll && !satisfied) {
//                visible = false;
//                break;
//            } else if (!requireAll && satisfied) {
//                visible = true;
//                break;
//            }
//        }
//
//        return visible;
//    }

}
