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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import piecework.enumeration.ActionType;
import piecework.security.Sanitizer;
import piecework.common.ManyMap;

import java.util.*;

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
    private final Activity activity;

    private final ActionType action;

    @DBRef
    private final Screen screen;

    private final String contentType;

    private final List<String> acceptableMediaTypes;

    private final Map<String, List<Message>> messages;

    private final String referrer;

    private final String userAgent;

    private final Date requestDate;

    @Transient
    private final ProcessInstance instance;

    @Transient
    private final ProcessDeployment deployment;

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
        this.action = builder.action;
        this.activity = builder.activity;
        this.screen = builder.screen;
        this.contentType= builder.contentType;
        this.acceptableMediaTypes = Collections.unmodifiableList(builder.acceptableMediaTypes);
        this.messages = Collections.unmodifiableMap(builder.messages);
        this.deployment = builder.deployment;
        this.instance = builder.instance;
        this.referrer = builder.referrer;
        this.userAgent = builder.userAgent;
        this.task = builder.task;
        this.requestDate = new Date();
    }

    @JsonIgnore
    public Action action() {
        return this.activity != null && this.action != null ? this.activity.action(this.action) : null;
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

    public ActionType getAction() {
        return action;
    }

    public Activity getActivity() {
        return activity;
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

    public String getReferrer() {
        return referrer;
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

    public ProcessDeployment getDeployment() {
        return deployment;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    @JsonIgnore
    public boolean validate(Process process) {
        return getProcessDefinitionKey() != null && process.getProcessDefinitionKey() != null && getProcessDefinitionKey().equals(process.getProcessDefinitionKey());
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
        private ProcessDeployment deployment;
        private ProcessInstance instance;
        private Task task;
        private Activity activity;
        private ActionType action;
        private Screen screen;
        private String contentType;
        private List<String> acceptableMediaTypes;
        private ManyMap<String, Message> messages;
        private String referrer;
        private String userAgent;

        public Builder() {
            super();
            this.acceptableMediaTypes = new ArrayList<String>();
            this.messages = new ManyMap<String, Message>();
        }

        public Builder(FormRequest request) {
            this.requestId = request.requestId;
            this.processDefinitionKey = request.processDefinitionKey;
            this.processInstanceId = request.processInstanceId;
            this.remoteAddr = request.remoteAddr;
            this.remoteHost = request.remoteHost;
            this.remotePort = request.remotePort;
            this.remoteUser = request.remoteUser;
            this.actAsUser = request.actAsUser;
            this.certificateSubject = request.certificateSubject;
            this.certificateIssuer = request.certificateIssuer;
            this.taskId = request.taskId;
            this.action = request.action;
            this.activity = request.activity;
            this.contentType = request.contentType;
            this.acceptableMediaTypes = new ArrayList<String>(request.acceptableMediaTypes);
            this.messages = new ManyMap<String, Message>(request.getMessages());
            this.referrer = request.referrer;
            this.userAgent = request.userAgent;
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
            this.action = request.action != null ? request.action : ActionType.CREATE;
            this.activity = request.activity != null ? new Activity.Builder(request.activity, sanitizer).build() : null;
            this.screen = request.screen != null ? new Screen.Builder(request.screen, sanitizer).build() : null;
            this.contentType = sanitizer.sanitize(request.contentType);
            this.acceptableMediaTypes = new ArrayList<String>(request.acceptableMediaTypes);
            this.messages = new ManyMap<String, Message>(request.getMessages());
            this.referrer = sanitizer.sanitize(request.referrer);
            this.userAgent = sanitizer.sanitize(request.userAgent);
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

        public Builder activity(Activity activity) {
            this.activity = activity;
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

        public Builder action(ActionType action) {
            this.action = action;
            return this;
        }

        public Builder deployment(ProcessDeployment deployment) {
            this.deployment = deployment;
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

        public Builder referrer(String referrer) {
            this.referrer = referrer;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }
    }

}
