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
import piecework.util.ManyMap;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author James Renfro
 */
@Service
public class ResponseHandler {

    @Autowired
    ContentRepository contentRepository;

    public Response handle(FormRequest formRequest, ProcessInstance instance, ViewContext viewContext) throws StatusCodeError {

        Screen screen = formRequest.getScreen();

        if (screen != null) {
            ManyMap<String, String> formValueMap = instance != null ? instance.getFormValueMap() : new ManyMap<String, String>();

            PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
            Screen.Builder screenBuilder = new Screen.Builder(screen, passthroughSanitizer, false);

            if (screen.getSections() != null) {
                for (Section section : screen.getSections()) {
                    Section.Builder sectionBuilder = new Section.Builder(section, passthroughSanitizer, false);

                    if (section.getFields() == null)
                        continue;

                    Map<String, Field> fieldMap = new HashMap<String, Field>();

                    for (Field field : section.getFields()) {
                        fieldMap.put(field.getName(), field);
                    }

                    for (Field field : section.getFields()) {
                        Field.Builder fieldBuilder = new Field.Builder(field, passthroughSanitizer);

                        List<Constraint> constraints = field.getConstraints();
                        if (constraints != null) {
                            boolean visible = true;

                            for (Constraint constraint : constraints) {
                                String constraintType = constraint.getType();

                                if (constraintType != null && constraintType.equals(Constants.ConstraintTypes.IS_ONLY_VISIBLE_WHEN)) {
                                    String constraintName = constraint.getName();
                                    String constraintValue = constraint.getValue();

                                    Field constraintField = fieldMap.get(constraintName);

                                    if (constraintField != null) {
                                        String defaultFieldValue = constraintField.getDefaultValue();

                                        if (defaultFieldValue == null || !defaultFieldValue.equals(constraintValue)) {
                                            visible = false;
                                            break;
                                        }
                                    }
                                }
                            }

                            if (!visible)
                                fieldBuilder.invisible();
                        }

                        sectionBuilder.field(fieldBuilder.build());
                    }
                    screenBuilder.section(sectionBuilder.build());
                }
            }
            screen = screenBuilder.build();
        }

        Form form = new Form.Builder()
                .formInstanceId(formRequest.getRequestId())
                .processDefinitionKey(formRequest.getProcessDefinitionKey())
                .submissionType(formRequest.getSubmissionType())
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

}
