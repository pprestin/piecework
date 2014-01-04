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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import piecework.form.FormDisposition;
import piecework.model.bind.FormNameMessageMapAdapter;
import piecework.model.bind.FormNameValueEntryMapAdapter;
import piecework.security.Sanitizer;
import piecework.common.ViewContext;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.common.ManyMap;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.*;

/**
 * @author James Renfro
 */
@XmlRootElement(name = Form.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = Form.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Form {

    @XmlAttribute
    @XmlID
    @Id
    private final String formInstanceId;

    @XmlAttribute
    private final String processInstanceId;

    @XmlAttribute
    private final String submissionType;

    @XmlAttribute
    private final String layout;

    @XmlElement
    private final String applicationStatusExplanation;

    @XmlElement
    private final Task task;

    @XmlElement
    private final Container container;

    @XmlElement
    private final Explanation explanation;

    @XmlJavaTypeAdapter(FormNameValueEntryMapAdapter.class)
    private final Map<String, List<Value>> data;

    @XmlJavaTypeAdapter(FormNameMessageMapAdapter.class)
    private final Map<String, List<Message>> validation;

    @XmlAttribute
    private final String root;

    @XmlAttribute
    private final String action;

    @XmlAttribute
    private final String link;

    @XmlAttribute
    private final String src;

    @XmlTransient
    private final String staticRoot;

    @XmlAttribute
    private final String activation;

    @XmlAttribute
    private final String assignment;

    @XmlAttribute
    private final String attachment;

    @XmlAttribute
    private final String history;

    @XmlAttribute
    private final String cancellation;

    @XmlAttribute
    private final String restart;

    @XmlAttribute
    private final String suspension;

    @XmlAttribute
    private final int attachmentCount;

    @XmlAttribute
    private final boolean valid;

    @XmlAttribute
    private final boolean external;

    @XmlAttribute
    private final boolean allowAttachments;

    @XmlTransient
    @JsonIgnore
    @Transient
    private final Process process;

    @XmlTransient
    @JsonIgnore
    private final List<Attachment> attachments;

    @JsonIgnore
    private final boolean anonymous;

    private final FormDisposition disposition;

    private final User currentUser;


    private Form() {
        this(new Form.Builder(), new ViewContext());
    }

    private Form(Form.Builder builder, ViewContext context) {
        this.process = builder.process;
        this.formInstanceId = builder.formInstanceId;
        this.processInstanceId = builder.processInstanceId;
        this.submissionType = builder.submissionType;
        this.layout = builder.layout;
        this.task = builder.task;
        this.container = builder.container;
        this.applicationStatusExplanation = builder.applicationStatusExplanation;
        this.explanation = builder.explanation;
        this.data = builder.data;
        this.validation = builder.validation;
        this.root = context != null ? context.getApplicationOrPublicUri(builder.anonymous, Constants.ROOT_ELEMENT_NAME) : null;
        this.action = context != null ? context.getApplicationOrPublicUri(builder.anonymous, Constants.ROOT_ELEMENT_NAME, builder.processDefinitionKey, "submission", builder.formInstanceId) : null;
        if (task != null && task.getTaskInstanceId() != null)
            this.link = context != null ? context.getApplicationOrPublicUri(builder.anonymous, Constants.ROOT_ELEMENT_NAME, builder.processDefinitionKey, task.getTaskInstanceId()) : null;
        else
            this.link = context != null ? context.getApplicationOrPublicUri(builder.anonymous, Constants.ROOT_ELEMENT_NAME, builder.processDefinitionKey) : null;
        this.src = context != null ? context.getApplicationOrPublicUri(builder.anonymous, "resource", builder.processDefinitionKey, builder.formInstanceId) : null;
        this.staticRoot = context != null ? context.getApplicationOrPublicUri(builder.anonymous, "resource", "static", builder.processDefinitionKey) : null;
        this.assignment = builder.assignment;
        this.activation = builder.activation;
        this.attachment = builder.attachment;
        this.cancellation = builder.cancellation;
        this.history = builder.history;
        this.restart = builder.restart;
        this.suspension = builder.suspension;
        this.attachmentCount = builder.attachmentCount;
        this.attachments = builder.attachments != null ? Collections.unmodifiableList(builder.attachments) : Collections.<Attachment>emptyList();
        this.valid = builder.valid;
        this.external = builder.external;
        this.allowAttachments = builder.allowAttachments;
        this.anonymous = builder.anonymous;
        this.disposition = builder.disposition;
        this.currentUser = builder.currentUser;
    }

    public String getFormInstanceId() {
        return formInstanceId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public String getSubmissionType() {
        return submissionType;
    }

    public String getLayout() {
        return layout;
    }

    public Container getContainer() {
        return container;
    }

    @JsonIgnore
    public Process getProcess() {
        return process;
    }

    public String getApplicationStatusExplanation() {
        return applicationStatusExplanation;
    }

    public Task getTask() {
        return task;
    }

    public Explanation getExplanation() {
        return explanation;
    }

    public Map<String, List<Value>> getData() {
        return data;
    }

    public Map<String, List<Message>> getValidation() {
        return validation;
    }

    @JsonIgnore
    public List<Attachment> getAttachments() {
        return attachments;
    }

    @JsonIgnore
    public ManyMap<String, Attachment> getAttachmentMap() {
        ManyMap<String, Attachment> map = new ManyMap<String, Attachment>();
        if (attachments != null && !attachments.isEmpty()) {
            for (Attachment attachment : attachments) {
                map.putOne(attachment.getName(), attachment);
            }
        }
        return map;
    }

    public String getRoot() {
        return root;
    }

    public String getAction() {
        return action;
    }

    public String getLink() {
        return link;
    }

    public String getSrc() {
        return src;
    }

    @JsonIgnore
    public String getStaticRoot() {
        return staticRoot;
    }

    public String getActivation() {
        return activation;
    }

    public String getAssignment() {
        return assignment;
    }

    public String getAttachment() {
        return attachment;
    }

    public String getCancellation() {
        return cancellation;
    }

    public String getHistory() {
        return history;
    }

    public String getRestart() {
        return restart;
    }

    public String getSuspension() {
        return suspension;
    }

    public int getAttachmentCount() {
        return attachmentCount;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isExternal() {
        return external;
    }

    public boolean isAllowAttachments() {
        return allowAttachments;
    }

    @JsonIgnore
    public boolean isAnonymous() {
        return anonymous;
    }

    public FormDisposition getDisposition() {
        return disposition;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public final static class Builder {

        private String formInstanceId;
        private String processInstanceId;
        private String processDefinitionKey;
        private String submissionType;
        private String layout;
        private String applicationStatusExplanation;
        private Task task;
        private Container container;
        private Explanation explanation;
        private ManyMap<String, Value> data;
        private ManyMap<String, Message> validation;
        private String activation;
        private String assignment;
        private String attachment;
        private String cancellation;
        private String history;
        private String restart;
        private String suspension;
        private int attachmentCount;
        private List<Attachment> attachments;
        private boolean valid;
        private boolean external;
        private boolean anonymous;
        private boolean allowAttachments;
        private boolean readonly;
        private FormDisposition disposition;
        private Process process;
        private User currentUser;

        public Builder() {
            super();
            this.attachmentCount = 0;
            this.data = new ManyMap<String, Value>();
            this.validation = new ManyMap<String, Message>();
            this.valid = true;
            this.external = true;
        }

        public Builder(Form form, Sanitizer sanitizer) {
            this.formInstanceId = sanitizer.sanitize(form.formInstanceId);
            this.processInstanceId = sanitizer.sanitize(form.processInstanceId);
            this.submissionType = sanitizer.sanitize(form.submissionType);
            this.layout = sanitizer.sanitize(form.layout);
            this.applicationStatusExplanation = sanitizer.sanitize(form.applicationStatusExplanation);
            this.container = form.container != null ? new Container.Builder(form.container, sanitizer).build() : null;
            this.task = form.task != null ? new Task.Builder(form.task, sanitizer).build() : null;
            this.explanation = form.explanation;
            if (form.data != null && !form.data.isEmpty())
                this.data = new ManyMap<String, Value>(form.data);
            else
                this.data = new ManyMap<String, Value>();

            if (form.validation != null && !form.validation.isEmpty())
                this.validation = new ManyMap<String, Message>(form.validation);
            else
                this.validation = new ManyMap<String, Message>();
            this.attachments = form.getAttachments();
            this.attachmentCount = form.getAttachments().size();
            this.valid = form.valid;
            this.external = form.external;
            this.allowAttachments = form.allowAttachments;
            this.anonymous = form.anonymous;
            this.process = form.process;
            this.currentUser = form.currentUser;
        }

        public Form build() {
            return new Form(this, null);
        }

        public Form build(ViewContext context) {
            return new Form(this, context);
        }

        public Builder process(Process process) {
            this.process = process;
            if (process != null)
                this.processDefinitionKey = process.getProcessDefinitionKey();
            return this;
        }

        public Builder instance(ProcessInstance instance, ViewContext context) {
            if (instance != null) {
                String processDefinitionKey = instance.getProcessDefinitionKey();
                String processInstanceId = instance.getProcessInstanceId();
                Set<Attachment> attachments = instance.getAttachments();
                this.processInstanceId = processInstanceId;
                this.activation = context.getApplicationUri(ProcessInstance.Constants.ROOT_ELEMENT_NAME, processDefinitionKey, processInstanceId, "activation");
                this.attachment = context.getApplicationUri(ProcessInstance.Constants.ROOT_ELEMENT_NAME, processDefinitionKey, processInstanceId, Attachment.Constants.ROOT_ELEMENT_NAME);
                this.cancellation = context.getApplicationUri(ProcessInstance.Constants.ROOT_ELEMENT_NAME, processDefinitionKey, processInstanceId, "cancellation");
                this.history = context.getApplicationUri(ProcessInstance.Constants.ROOT_ELEMENT_NAME, processDefinitionKey, processInstanceId, History.Constants.ROOT_ELEMENT_NAME);
                this.restart = context.getApplicationUri(ProcessInstance.Constants.ROOT_ELEMENT_NAME, processDefinitionKey, processInstanceId, "restart");
                this.suspension = context.getApplicationUri(ProcessInstance.Constants.ROOT_ELEMENT_NAME, processDefinitionKey, processInstanceId, "suspension");
                if (attachments != null && !attachments.isEmpty()) {
                    PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
                    this.attachmentCount = attachments.size();
                    this.attachments = new ArrayList<Attachment>(attachments.size());
                    for (Attachment attachment : attachments) {
                        this.attachments.add(new Attachment.Builder(attachment, passthroughSanitizer).processDefinitionKey(processDefinitionKey).processInstanceId(processInstanceId).build(context));
                    }
                } else {
                    this.attachmentCount = instance.getAttachmentIds().size();
                }
            }
            return this;
        }

        public Builder instanceSubresources(String processDefinitionKey, String processInstanceId, Set<Attachment> attachments, int attachmentCount, ViewContext context) {
            this.processInstanceId = processInstanceId;
            this.activation = context.getApplicationUri(ProcessInstance.Constants.ROOT_ELEMENT_NAME, processDefinitionKey, processInstanceId, "activation");
            this.attachment = context.getApplicationUri(ProcessInstance.Constants.ROOT_ELEMENT_NAME, processDefinitionKey, processInstanceId, Attachment.Constants.ROOT_ELEMENT_NAME);
            this.cancellation = context.getApplicationUri(ProcessInstance.Constants.ROOT_ELEMENT_NAME, processDefinitionKey, processInstanceId, "cancellation");
            this.history = context.getApplicationUri(ProcessInstance.Constants.ROOT_ELEMENT_NAME, processDefinitionKey, processInstanceId, History.Constants.ROOT_ELEMENT_NAME);
            this.restart = context.getApplicationUri(ProcessInstance.Constants.ROOT_ELEMENT_NAME, processDefinitionKey, processInstanceId, "restart");
            this.suspension = context.getApplicationUri(ProcessInstance.Constants.ROOT_ELEMENT_NAME, processDefinitionKey, processInstanceId, "suspension");
            if (attachments != null && !attachments.isEmpty()) {
                PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
                this.attachmentCount = attachments.size();
                this.attachments = new ArrayList<Attachment>(attachments.size());
                for (Attachment attachment : attachments) {
                    this.attachments.add(new Attachment.Builder(attachment, passthroughSanitizer).processDefinitionKey(processDefinitionKey).processInstanceId(processInstanceId).build(context));
                }
            } else {
                this.attachmentCount = attachmentCount;
            }
            return this;
        }

        public Builder applicationStatusExplanation(String applicationStatusExplanation) {
            this.applicationStatusExplanation = applicationStatusExplanation;
            return this;
        }

        public Builder taskSubresources(String processDefinitionKey, Task task, ViewContext context) {
            this.assignment = context != null && task != null && task.getTaskInstanceId() != null ? context.getApplicationUri(Task.Constants.ROOT_ELEMENT_NAME, processDefinitionKey, task.getTaskInstanceId(), "assign") : null;
            this.task = task;
            return this;
        }

        public Builder formInstanceId(String formInstanceId) {
            this.formInstanceId = formInstanceId;
            return this;
        }

        public Builder processDefinitionKey(String processDefinitionKey) {
            this.processDefinitionKey = processDefinitionKey;
            return this;
        }

        public Builder submissionType(String submissionType) {
            this.submissionType = submissionType;
            return this;
        }

        public Builder layout(String layout) {
            this.layout = layout;
            return this;
        }

        public Builder container(Container container) {
            this.container = container;
            return this;
        }

        public Builder task(Task task) {
            this.task = task;
            return this;
        }

        public Builder data(Map<String, List<Value>> data) {
            if (data != null && !data.isEmpty())
                this.data = new ManyMap<String, Value>(data);
            return this;
        }

        public Builder messages(Map<String, List<Message>> messages) {
            if (messages != null && !messages.isEmpty())
                this.validation = new ManyMap<String, Message>(messages);
            return this;
        }

        public Builder variable(String key, List<Value> values) {
            if (values != null)
                this.data.put(key, values);
            else
                this.data.put(key, Collections.<Value>emptyList());
            return this;
        }

        public Builder validation(String key, List<Message> messages) {
            if (messages != null)
                this.validation.put(key, messages);
            else
                this.validation.put(key, Collections.<Message>emptyList());
            return this;
        }

        public Builder attachmentCount(int attachmentCount) {
            this.attachmentCount = attachmentCount;
            return this;
        }

        public Builder invalid() {
            this.valid = false;
            return this;
        }

        public Builder external(boolean external) {
            this.external = external;
            return this;
        }

        public Builder explanation(Explanation explanation) {
            this.explanation = explanation;
            return this;
        }

        public Builder allowAttachments(boolean allowAttachments) {
            this.allowAttachments = allowAttachments;
            return this;
        }

        public Builder anonymous(boolean anonymous) {
            this.anonymous = anonymous;
            return this;
        }

        public Builder readonly() {
            this.readonly = true;
            return this;
        }

        public boolean isReadonly() {
            return readonly;
        }

        public Builder disposition(FormDisposition disposition) {
            this.disposition = disposition;
            return this;
        }

        public Builder currentUser(User currentUser) {
            this.currentUser = currentUser;
            return this;
        }
    }

    public static class Constants {
        public static final String RESOURCE_LABEL = "Form";
        public static final String ROOT_ELEMENT_NAME = "form";
        public static final String TYPE_NAME = "FormType";
    }

}
