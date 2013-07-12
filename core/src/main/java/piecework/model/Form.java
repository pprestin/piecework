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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import piecework.security.Sanitizer;
import piecework.common.ViewContext;

import javax.xml.bind.annotation.*;
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

    @XmlElementWrapper(name="formData")
    @XmlElementRef
    private final List<FormValue> formData;

    @XmlAttribute
    private final String root;

    @XmlAttribute
    private final String action;

    @XmlAttribute
    private final String link;

    @XmlAttribute
    private final String activation;

    @XmlAttribute
    private final String attachment;

    @XmlAttribute
    private final String cancellation;

    @XmlAttribute
    private final String suspension;

    @XmlAttribute
    private final int attachmentCount;

    @XmlAttribute
    private final boolean valid;

    private Form() {
        this(new Form.Builder(), new ViewContext());
    }

    private Form(Form.Builder builder, ViewContext context) {
        this.formInstanceId = builder.formInstanceId;
        this.submissionType = builder.submissionType;
        this.task = builder.task;
        this.screen = builder.screen;
        this.formData = builder.formData != null ? Collections.unmodifiableList(builder.formData) : null;
        this.root = context != null ? context.getApplicationUri() : null;
        this.action = context != null ? context.getApplicationUri(builder.processDefinitionKey, "submission", builder.formInstanceId) : null;
        if (task != null && task.getTaskInstanceId() != null)
            this.link = context != null ? context.getApplicationUri(builder.processDefinitionKey, task.getTaskInstanceId()) : null;
        else
            this.link = context != null ? context.getApplicationUri(builder.processDefinitionKey) : null;
        this.activation = context != null ? context.getApplicationUri(builder.processDefinitionKey, "activation", builder.formInstanceId) : null;
        this.attachment = context != null ? context.getApplicationUri(builder.processDefinitionKey, "attachment", builder.formInstanceId) : null;
        this.cancellation = context != null ? context.getApplicationUri(builder.processDefinitionKey, "cancellation", builder.formInstanceId) : null;
        this.suspension = context != null ? context.getApplicationUri(builder.processDefinitionKey, "suspension", builder.formInstanceId) : null;
        this.attachmentCount = builder.attachmentCount;
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

    public List<FormValue> getFormData() {
        return formData;
    }

    public Map<String, FormValue> getFormValueMap() {
        Map<String, FormValue> map = new HashMap<String, FormValue>();
        if (formData != null && !formData.isEmpty()) {
            for (FormValue formValue : formData) {
                map.put(formValue.getName(), formValue);
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

    public String getAttachment() {
        return attachment;
    }

    public String getCancellation() {
        return cancellation;
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
        private List<FormValue> formData;
        private int attachmentCount;
        private boolean valid;

        public Builder() {
            super();
            this.attachmentCount = 0;
            this.valid = true;
        }

        public Builder(Form form, Sanitizer sanitizer) {
            this.formInstanceId = sanitizer.sanitize(form.formInstanceId);
            this.submissionType = sanitizer.sanitize(form.submissionType);
            this.screen = form.screen != null ? new Screen.Builder(form.screen, sanitizer).build() : null;
            this.task = form.task != null ? new Task.Builder(form.task, sanitizer).build() : null;
            if (form.formData != null && !form.formData.isEmpty()) {
                this.formData = new ArrayList<FormValue>(form.formData.size());
                for (FormValue formValue : form.formData) {
                    this.formData.add(new FormValue.Builder(formValue, sanitizer).build());
                }
            }
            this.attachmentCount = form.attachmentCount;
            this.valid = form.valid;
        }

        public Form build() {
            return new Form(this, null);
        }

        public Form build(ViewContext context) {
            return new Form(this, context);
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

        public Builder formValue(String key, String ... values) {
            if (this.formData == null)
                this.formData = new ArrayList<FormValue>();
            this.formData.add(new FormValue.Builder().name(key).values(values).build());
            return this;
        }

        public Builder formValues(List<FormValue> formValues) {
            if (this.formData == null)
                this.formData = new ArrayList<FormValue>();
            if (formValues != null)
                this.formData.addAll(formValues);
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
