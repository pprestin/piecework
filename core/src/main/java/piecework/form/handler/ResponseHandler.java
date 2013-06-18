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
import piecework.model.*;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.ui.StreamingPageContent;
import piecework.persistence.ContentRepository;
import piecework.util.ConstraintUtil;
import piecework.util.FormDataUtil;
import piecework.util.ManyMap;

import javax.ws.rs.core.Response;
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
