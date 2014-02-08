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
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import piecework.Constants;
import piecework.enumeration.OperationType;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.ui.streaming.StreamingAttachmentContent;
import piecework.validation.Validation;
import piecework.model.*;
import piecework.model.Process;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

/**
 * @author James Renfro
 */
public class ProcessInstanceUtility {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.mediumDateTime();

    public static Set<Task> tasks(Set<Task> originals, OperationType operation) {
        Set<Task> tasks = new TreeSet<Task>();
        if (originals != null && !originals.isEmpty()) {
            PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
            for (Task original : originals) {
                Task.Builder builder = new Task.Builder(original, passthroughSanitizer);
                switch (operation) {
                    case ACTIVATION:
                        if (original.getTaskStatus().equals(Constants.TaskStatuses.SUSPENDED)) {
                            builder.taskStatus(Constants.TaskStatuses.OPEN);
                            builder.active();
                        }
                        break;
                    case CANCELLATION:
                        if (original.getTaskStatus().equals(Constants.TaskStatuses.OPEN) || original.getTaskStatus().equals(Constants.TaskStatuses.SUSPENDED)) {
                            builder.taskStatus(Constants.TaskStatuses.CANCELLED);
                            builder.finished();
                        }
                        break;
                    case SUSPENSION:
                        if (original.getTaskStatus().equals(Constants.TaskStatuses.OPEN)) {
                            builder.taskStatus(Constants.TaskStatuses.SUSPENDED);
                            builder.suspended();
                        }
                        break;
                }
                tasks.add(builder.build());
            }
        }

        return tasks;
    }

    public static String template(String template, Map<String, List<Value>> data) {
        return template(template, data, null);
    }

    public static String template(String template, Map<String, List<Value>> data, Map<String, List<Value>> validationData) {
        Map<String, Value> scopes = new HashMap<String, Value>();
        scopes(scopes, data);
        if (validationData != null)
            scopes(scopes, validationData);

        StringWriter writer = new StringWriter();
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(new StringReader(template), "my-template");
        mustache.execute(writer, scopes);

        return StringEscapeUtils.unescapeXml(writer.toString());
    }

    public static String processInstanceLabel(Process process, ProcessInstance instance, Map<String, List<Value>> validationData, String submissionLabel) {
        String processInstanceLabel = instance != null ? instance.getProcessInstanceLabel() : null;
        String processInstanceLabelTemplate = process.getProcessInstanceLabelTemplate();

        if (StringUtils.isEmpty(processInstanceLabel))
            processInstanceLabel = submissionLabel;

        if (processInstanceLabelTemplate != null && processInstanceLabelTemplate.indexOf('{') != -1) {
            Map<String, List<Value>> data = instance != null ? instance.getData() : null;

            if (StringUtils.isEmpty(processInstanceLabel) || data != null)
                processInstanceLabel = template(processInstanceLabelTemplate, data, validationData);
        }

        if (StringUtils.isEmpty(processInstanceLabel))
            processInstanceLabel = "Submission " + dateTimeFormatter.print(System.currentTimeMillis());

        return processInstanceLabel;
    }

    private static void scopes(Map<String, Value> scopes, Map<String, List<Value>> data) {
        if (data != null) {
            for (Map.Entry<String, List<Value>> entry : data.entrySet()) {
                List<Value> values = entry.getValue();
                if (values != null && !values.isEmpty()) {
                    String key = entry.getKey();
                    Value value = values.iterator().next();
                    scopes.put(key, value);
                }
            }
        }
    }

    public static List<String> allStrings(String name, Map<String, List<Value>> map) {
        List<Value> values = allValues(name, map);
        List<String> strings = new ArrayList<String>();
        for (Value value : values) {
            strings.add(value.getValue());
        }
        return strings;
    }

    public static List<Value> allValues(String name, Map<String, List<Value>> map) {
        if (map != null && name != null) {
            List<Value> values = map.get(name);

            if (values != null)
                return values;
            else
                return Collections.emptyList();
        }
        return null;
    }

    public static File firstFile(String name, Map<String, List<Value>> map) {
        Value value = firstValue(name, map);
        return value != null && value instanceof File ? File.class.cast(value) : null;
    }

    public static User firstUser(String name, Map<String, List<Value>> map) {
        Value value = firstValue(name, map);
        return value != null && value instanceof User ? User.class.cast(value) : null;
    }

    public static String firstString(String name, Map<String, List<Value>> map) {
        Value value = firstValue(name, map);
        return value != null ? value.getValue() : null;
    }

    public static Value firstValue(String name, Map<String, List<Value>> map) {
        if (map != null && name != null) {
            List<Value> values = map.get(name);

            if (values != null && !values.isEmpty())
                return values.iterator().next();
        }
        return null;
    }

    public static Value firstMatchingFileOrLink(String name, Map<String, List<Value>> map, String valueId) {
        if (map != null && name != null && StringUtils.isNotEmpty(valueId)) {
            List<Value> values = map.get(name);

            if (values != null && !values.isEmpty()) {
                for (Value value : values) {
                    if (value == null)
                        continue;

                    if (value instanceof File) {
                        File file = File.class.cast(value);

                        if (StringUtils.isEmpty(file.getId()))
                            continue;

                        if (file.getId().equals(valueId))
                            return file;
                    } else {
                        String link = value.getValue();
                        String id = Base64Utility.safeBase64(link);
                        if (id != null && id.equals(valueId)) {
                            if (!link.startsWith("http"))
                                link = "http://" + link;
                            return new Value(link);
                        }
                    }
                }
            }
        }
        return null;
    }

}
