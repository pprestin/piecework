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
import org.apache.commons.lang.StringUtils;
import org.springframework.data.annotation.Id;
import piecework.model.bind.FormNameMessageMapAdapter;
import piecework.model.bind.FormNameValueEntryMapAdapter;
import piecework.security.Sanitizer;
import piecework.common.ViewContext;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.util.ManyMap;

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
    private final String submissionType;

    @XmlElement
    private final Task task;

    @XmlElement
    private final Screen screen;

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
    private final String suspension;

    @XmlAttribute
    private final int attachmentCount;

    @XmlAttribute
    private final boolean valid;

    @XmlTransient
    @JsonIgnore
    private final List<Attachment> attachments;


    private Form() {
        this(new Form.Builder(), new ViewContext());
    }

    private Form(Form.Builder builder, ViewContext context) {
        this.formInstanceId = builder.formInstanceId;
        this.submissionType = builder.submissionType;
        this.task = builder.task;
        this.screen = builder.screen;
        this.data = builder.data;
        this.validation = builder.validation;
        this.root = context != null ? context.getApplicationUri() : null;
        this.action = context != null ? context.getApplicationUri(builder.processDefinitionKey, "submission", builder.formInstanceId) : null;
        if (task != null && task.getTaskInstanceId() != null)
            this.link = context != null ? context.getApplicationUri(builder.processDefinitionKey, task.getTaskInstanceId()) : null;
        else
            this.link = context != null ? context.getApplicationUri(builder.processDefinitionKey) : null;
        this.assignment = builder.assignment;
        this.activation = builder.activation;
        this.attachment = builder.attachment;
        this.cancellation = builder.cancellation;
        this.history = builder.history;
        this.suspension = builder.suspension;
        this.attachmentCount = builder.attachmentCount;
        this.attachments = builder.attachments != null ? Collections.unmodifiableList(builder.attachments) : Collections.<Attachment>emptyList();
        this.valid = builder.valid;
    }

    public String getFormInstanceId() {
        return formInstanceId;
    }

    public String getSubmissionType() {
        return submissionType;
    }

    public Screen getScreen() {
        return screen;
    }

    public Task getTask() {
        return task;
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

    public String getSuspension() {
        return suspension;
    }

    public int getAttachmentCount() {
        return attachmentCount;
    }

    public boolean isValid() {
        return valid;
    }

    public final static class Builder {

        private String formInstanceId;
        private String processDefinitionKey;
        private String submissionType;
        private Task task;
        private Screen screen;
        private ManyMap<String, Value> data;
        private ManyMap<String, Message> validation;
        private String activation;
        private String assignment;
        private String attachment;
        private String cancellation;
        private String history;
        private String suspension;
        private int attachmentCount;
        private List<Attachment> attachments;
        private boolean valid;

        public Builder() {
            super();
            this.attachmentCount = 0;
            this.data = new ManyMap<String, Value>();
            this.validation = new ManyMap<String, Message>();
            this.valid = true;
        }

        public Builder(Form form, Sanitizer sanitizer) {
            this.formInstanceId = sanitizer.sanitize(form.formInstanceId);
            this.submissionType = sanitizer.sanitize(form.submissionType);
            this.screen = form.screen != null ? new Screen.Builder(form.screen, sanitizer).build() : null;
            this.task = form.task != null ? new Task.Builder(form.task, sanitizer).build() : null;
            if (form.data != null && !form.data.isEmpty())
                this.data = new ManyMap<String, Value>(form.data);
            else
                this.data = new ManyMap<String, Value>();

            if (form.validation != null && !form.validation.isEmpty())
                this.validation = new ManyMap<String, Message>(form.validation);
            else
                this.validation = new ManyMap<String, Message>();

            this.attachmentCount = form.attachmentCount;
            this.attachments = form.getAttachments();
            this.valid = form.valid;
        }

        public Form build() {
            return new Form(this, null);
        }

        public Form build(ViewContext context) {
            return new Form(this, context);
        }

        public Builder instanceSubresources(String processDefinitionKey, String processInstanceId, List<Attachment> attachments, ViewContext context) {
            this.activation = context.getApplicationUri(processDefinitionKey, processInstanceId, "activation");
            this.attachment = context.getApplicationUri(processDefinitionKey, processInstanceId, Attachment.Constants.ROOT_ELEMENT_NAME);
            this.cancellation = context.getApplicationUri(processDefinitionKey, processInstanceId, "cancellation");
            this.history = context.getApplicationUri(processDefinitionKey, processInstanceId, History.Constants.ROOT_ELEMENT_NAME);
            this.suspension = context.getApplicationUri(processDefinitionKey, processInstanceId, "suspension");
            if (attachments != null && !attachments.isEmpty()) {
                PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
                this.attachments = new ArrayList<Attachment>(attachments.size());
                for (Attachment attachment : attachments) {
                    this.attachments.add(new Attachment.Builder(attachment, passthroughSanitizer).processDefinitionKey(processDefinitionKey).processInstanceId(processInstanceId).build(context));
                }
            }
            return this;
        }

        public Builder taskSubresources(String processDefinitionKey, Task task, ViewContext context) {
            this.assignment = context != null && task != null && task.getTaskInstanceId() != null ? context.getApplicationUri(processDefinitionKey, task.getTaskInstanceId(), "assign") : null;
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

        public Builder screen(Screen screen) {
            this.screen = screen;
            return this;
        }

        public Builder task(Task task) {
            this.task = task;
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
    }

    public static class Constants {
        public static final String RESOURCE_LABEL = "Form";
        public static final String ROOT_ELEMENT_NAME = "form";
        public static final String TYPE_NAME = "FormType";
    }

}
