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
package piecework.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.CommandExecutor;
import piecework.Versions;
import piecework.common.ViewContext;
import piecework.exception.NotFoundError;
import piecework.exception.StatusCodeError;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ContentRepository;
import piecework.command.InstanceCommand;
import piecework.command.UpdateInstanceCommand;
import piecework.ui.StreamingAttachmentContent;
import piecework.util.Base64Utility;
import piecework.util.ManyMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
@Service
public class ValuesService {

    @Autowired
    ContentRepository contentRepository;

    @Autowired
    CommandExecutor commandExecutor;

    @Autowired
    Versions versions;

    public Response read(piecework.model.Process process, ProcessInstance instance, String fieldName, String fileId) throws StatusCodeError {
        Map<String, List<Value>> data = instance.getData();
        List<? extends Value> values = fieldName != null ? data.get(fieldName) : null;

        if (values == null || values.isEmpty() || StringUtils.isEmpty(fileId))
            throw new NotFoundError();

        for (Value value : values) {
            if (value == null)
                continue;

            if (value instanceof File) {
                File file = File.class.cast(value);

                if (StringUtils.isEmpty(file.getId()))
                    continue;

                if (file.getId().equals(fileId)) {
                    Content content = contentRepository.findByLocation(file.getLocation());
                    if (content != null) {
                        StreamingAttachmentContent streamingAttachmentContent = new StreamingAttachmentContent(null, content);
                        String contentDisposition = new StringBuilder("attachment; filename=").append(content.getName()).toString();
                        return Response.ok(streamingAttachmentContent, streamingAttachmentContent.getContent().getContentType()).header("Content-Disposition", contentDisposition).build();
                    }
                }
            } else {
                String link = value.getValue();
                String id = Base64Utility.safeBase64(link);
                if (id != null && id.equals(fileId)) {
                    if (!link.startsWith("http"))
                        link = "http://" + link;
                    return Response.status(Response.Status.MOVED_PERMANENTLY).header(HttpHeaders.LOCATION, link).build();
                }
            }
        }

        throw new NotFoundError();
    }

    public void delete(Process process, ProcessInstance instance, String fieldName, String fileId) throws StatusCodeError {
        Map<String, List<Value>> data = instance.getData();
        List<? extends Value> values = fieldName != null ? data.get(fieldName) : null;

        if (values == null || values.isEmpty() || StringUtils.isEmpty(fileId))
            throw new NotFoundError();

        List<Value> remainingValues = new ArrayList<Value>();
        for (Value value : values) {
            if (value == null)
                continue;

            if (value instanceof File) {
                File file = File.class.cast(value);

                if (StringUtils.isEmpty(file.getId()))
                    continue;

                if (!file.getId().equals(fileId))
                    remainingValues.add(value);
            } else {
                String link = value.getValue();
                String id = Base64Utility.safeBase64(link);
                if (id == null || !id.equals(fileId))
                    remainingValues.add(value);
            }
        }

        ManyMap<String, Value> update = new ManyMap<String, Value>();
        update.put(fieldName, remainingValues);

        InstanceCommand persist = new UpdateInstanceCommand(process, instance);
        persist.data(update);
        commandExecutor.execute(persist);
    }

    public List<Value> searchValues(Process process, ProcessInstance instance, String fieldName) throws StatusCodeError {
        Map<String, List<Value>> data = instance.getData();
        List<? extends Value> values = fieldName != null ? data.get(fieldName) : null;

        List<Value> files = new ArrayList<Value>();
        ViewContext version1 = versions.getVersion1();
        if (values != null && !values.isEmpty()) {
            for (Value value : values) {
                if (value == null)
                    continue;

                if (value instanceof File) {
                    File file = File.class.cast(value);
                    files.add(new File.Builder().processDefinitionKey(process.getProcessDefinitionKey()).processInstanceId(instance.getProcessInstanceId()).fieldName(fieldName).name(file.getName()).id(file.getId()).contentType(file.getContentType()).description(file.getDescription()).build(version1));
                } else {
                    String link = value.getValue();
                    String id = Base64Utility.safeBase64(link);
                    if (link != null)
                        files.add(new File.Builder().processDefinitionKey(process.getProcessDefinitionKey()).processInstanceId(instance.getProcessInstanceId()).fieldName(fieldName).name(link).id(id).contentType("text/url").build(version1));
                }
            }
        }

        return files;
    }

}
