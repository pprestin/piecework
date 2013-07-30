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
package piecework.util;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.apache.commons.lang.StringUtils;
import piecework.form.validation.FormValidation;
import piecework.model.FormValue;
import piecework.model.Process;
import piecework.model.ProcessInstance;

import java.io.StringReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.*;

/**
 * @author James Renfro
 */
public class ProcessInstanceUtility {

    public static String processInstanceLabel(Process process, ProcessInstance instance, FormValidation validation, String submissionLabel) {
        String processInstanceLabel = submissionLabel;
        String processInstanceLabelTemplate = process.getProcessInstanceLabelTemplate();

        if (StringUtils.isEmpty(processInstanceLabel) && processInstanceLabelTemplate != null && processInstanceLabelTemplate.indexOf('{') != -1) {
            Map<String, String> scopes = new HashMap<String, String>();

            Map<String, FormValue> instanceFormValueMap = instance != null ? instance.getFormValueMap() : null;
            Map<String, FormValue> validationFormValueMap = validation.getFormValueMap();
            if (instanceFormValueMap != null) {
                for (Map.Entry<String,FormValue> entry : instanceFormValueMap.entrySet()) {
                    FormValue formValue = entry.getValue();
                    List<String> values = formValue != null ? formValue.getValues() : null;
                    if (values != null && !values.isEmpty())
                        scopes.put(entry.getKey(), values.iterator().next());
                }
            }
            if (validationFormValueMap != null) {
                for (Map.Entry<String,FormValue> entry : validationFormValueMap.entrySet()) {
                    FormValue formValue = entry.getValue();
                    List<String> values = formValue != null ? formValue.getValues() : null;
                    if (values != null && !values.isEmpty())
                        scopes.put(entry.getKey(), values.iterator().next());
                }
            }

            StringWriter writer = new StringWriter();
            MustacheFactory mf = new DefaultMustacheFactory();
            Mustache mustache = mf.compile(new StringReader(processInstanceLabelTemplate), "processInstanceLabel");
            mustache.execute(writer, scopes);

            processInstanceLabel = writer.toString();

            if (StringUtils.isEmpty(processInstanceLabel))
                processInstanceLabel = "Submission " + DateFormat.getDateTimeInstance(
                        DateFormat.SHORT,
                        DateFormat.SHORT,
                        Locale.getDefault());
        }

        return processInstanceLabel;
    }

}
