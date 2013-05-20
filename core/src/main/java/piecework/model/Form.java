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

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import piecework.Sanitizer;
import piecework.common.view.ViewContext;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private final Screen screen;

    @XmlElementWrapper(name="formData")
    @XmlElementRef
    private final List<FormValue> formData;

    @XmlElement
    private final String uri;


    private Form() {
        this(new Form.Builder(), new ViewContext());
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

    public List<FormValue> getFormData() {
        return formData;
    }

    public String getUri() {
        return uri;
    }

    private Form(Form.Builder builder, ViewContext context) {
        this.formInstanceId = builder.formInstanceId;
        this.submissionType = builder.submissionType;
        this.screen = builder.screen;
        this.formData = builder.formData != null ? Collections.unmodifiableList(builder.formData) : null;
        this.uri = context != null ? context.getApplicationUri(builder.processDefinitionKey, builder.formInstanceId) : null;
    }

    public final static class Builder {

        private String formInstanceId;
        private String processDefinitionKey;
        private String submissionType;
        private Screen screen;
        private List<FormValue> formData;

        public Builder() {
            super();
        }

        public Builder(Form form, Sanitizer sanitizer) {
            this.formInstanceId = sanitizer.sanitize(form.formInstanceId);
            this.submissionType = sanitizer.sanitize(form.submissionType);
            this.screen = form.screen != null ? new Screen.Builder(form.screen, sanitizer).build() : null;

            if (form.formData != null && !form.formData.isEmpty()) {
                this.formData = new ArrayList<FormValue>(form.formData.size());
                for (FormValue formValue : form.formData) {
                    this.formData.add(new FormValue.Builder(formValue, sanitizer).build());
                }
            }
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

        public Builder formValue(String key, String ... values) {
            if (this.formData == null)
                this.formData = new ArrayList<FormValue>();
            this.formData.add(new FormValue.Builder().name(key).values(values).build());
            return this;
        }
    }

    public static class Constants {
        public static final String RESOURCE_LABEL = "Form";
        public static final String ROOT_ELEMENT_NAME = "form";
        public static final String TYPE_NAME = "FormType";
    }

}
