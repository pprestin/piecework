/*
 * Copyright 2012 University of Washington
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
package piecework.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import piecework.security.Sanitizer;
import piecework.util.ManyMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
@Document(collection = "request")
public class FormRequest {

    @Id
    private final String requestId;

    private final String processDefinitionKey;

    private final String processInstanceId;

    private final String taskId;

    private final String remoteAddr;

    private final String remoteHost;

    private final int remotePort;

    private final String remoteUser;

    private final String actAsUser;

    private final String certificateSubject;

    private final String certificateIssuer;

    @DBRef
    private final Screen screen;

    private final String contentType;

    private final List<String> acceptableMediaTypes;

    private final Map<String, List<Message>> messages;

    @Transient
    private final ProcessInstance instance;

    @Transient
    private final Task task;

    private FormRequest() {
        this(new FormRequest.Builder());
    }

    private FormRequest(FormRequest.Builder builder) {
        this.requestId = builder.requestId;
        this.processDefinitionKey = builder.processDefinitionKey;
        this.processInstanceId = builder.processInstanceId;
        this.taskId = builder.taskId;
        this.remoteAddr = builder.remoteAddr;
        this.remoteHost = builder.remoteHost;
        this.remotePort = builder.remotePort;
        this.remoteUser = builder.remoteUser;
        this.actAsUser = builder.actAsUser;
        this.certificateSubject = builder.certificateSubject;
        this.certificateIssuer = builder.certificateIssuer;
        this.screen = builder.screen;
        this.contentType= builder.contentType;
        this.acceptableMediaTypes = Collections.unmodifiableList(builder.acceptableMediaTypes);
        this.messages = Collections.unmodifiableMap(builder.messages);
        this.instance = builder.instance;
        this.task = builder.task;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public String getActAsUser() {
        return actAsUser;
    }

    public String getRemoteUser() {
        return remoteUser;
    }

    public String getCertificateSubject() {
        return certificateSubject;
    }

    public String getCertificateIssuer() {
        return certificateIssuer;
    }

    public String getTaskId() {
        return taskId;
    }

    public Screen getScreen() {
        return screen;
    }

    public String getContentType() {
        return contentType;
    }

    public List<String> getAcceptableMediaTypes() {
        return acceptableMediaTypes;
    }

    public Map<String, List<Message>> getMessages() {
        return messages;
    }

    public ProcessInstance getInstance() {
        return instance;
    }

    public Task getTask() {
        return task;
    }

    public final static class Builder {

        private String requestId;
        private String processDefinitionKey;
        private String processInstanceId;
        private String remoteAddr;
        private String remoteHost;
        private int remotePort;
        private String remoteUser;
        private String actAsUser;
        private String certificateSubject;
        private String certificateIssuer;
        private String taskId;
        private ProcessInstance instance;
        private Task task;
        private Screen screen;
        private String contentType;
        private List<String> acceptableMediaTypes;
        private ManyMap<String, Message> messages;

        public Builder() {
            super();
            this.acceptableMediaTypes = new ArrayList<String>();
            this.messages = new ManyMap<String, Message>();
        }

        public Builder(FormRequest request, Sanitizer sanitizer) {
            this.requestId = sanitizer.sanitize(request.requestId);
            this.processDefinitionKey = sanitizer.sanitize(request.processDefinitionKey);
            this.processInstanceId = sanitizer.sanitize(request.processInstanceId);
            this.remoteAddr = sanitizer.sanitize(request.remoteAddr);
            this.remoteHost = sanitizer.sanitize(request.remoteHost);
            this.remotePort = request.remotePort;
            this.remoteUser = sanitizer.sanitize(request.remoteUser);
            this.actAsUser = sanitizer.sanitize(request.actAsUser);
            this.certificateSubject = sanitizer.sanitize(request.certificateSubject);
            this.certificateIssuer = sanitizer.sanitize(request.certificateIssuer);
            this.taskId = sanitizer.sanitize(request.taskId);
            this.screen = request.screen != null ? new Screen.Builder(request.screen, sanitizer).build() : null;
            this.contentType = sanitizer.sanitize(request.contentType);
            this.acceptableMediaTypes = new ArrayList<String>(request.acceptableMediaTypes);
            this.messages = new ManyMap<String, Message>(request.getMessages());
        }

        public FormRequest build() {
            return new FormRequest(this);
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder processDefinitionKey(String processDefinitionKey) {
            this.processDefinitionKey = processDefinitionKey;
            return this;
        }

        public Builder processInstanceId(String processInstanceId) {
            this.processInstanceId = processInstanceId;
            return this;
        }

        public Builder taskId(String taskId) {
            this.taskId = taskId;
            return this;
        }

        public Builder remoteAddr(String remoteAddr) {
            this.remoteAddr = remoteAddr;
            return this;
        }

        public Builder remoteHost(String remoteHost) {
            this.remoteHost = remoteHost;
            return this;
        }

        public Builder remotePort(int remotePort) {
            this.remotePort = remotePort;
            return this;
        }

        public Builder remoteUser(String remoteUser) {
            this.remoteUser = remoteUser;
            return this;
        }

        public Builder actAsUser(String actAsUser) {
            this.actAsUser = actAsUser;
            return this;
        }

        public Builder certificateSubject(String certificateSubject) {
            this.certificateSubject = certificateSubject;
            return this;
        }

        public Builder certificateIssuer(String certificateIssuer) {
            this.certificateIssuer = certificateIssuer;
            return this;
        }

        public Builder screen(Screen screen) {
            this.screen = screen;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder acceptableMediaType(String acceptableMediaType) {
            if (acceptableMediaType != null) {
                this.acceptableMediaTypes.add(acceptableMediaType);
            }
            return this;
        }

        public Builder messages(Map<String, List<Message>> messages) {
            if (messages != null) {
                this.messages = new ManyMap<String, Message>(messages);
            }
            return this;
        }

        public Builder instance(ProcessInstance instance) {
            this.instance = instance;
            if (instance != null)
                this.processInstanceId = instance.getProcessInstanceId();
            return this;
        }

        public Builder task(Task task) {
            this.task = task;
            if (task != null)
                this.taskId = task.getTaskInstanceId();
            return this;
        }
    }

}
