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
package piecework.process.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import piecework.Sanitizer;
import piecework.common.view.ViewContext;

import javax.xml.bind.annotation.*;

/**
 * @author James Renfro
 */
@XmlRootElement(name = ProcessInstance.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = ProcessInstance.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = ProcessInstance.Constants.ROOT_ELEMENT_NAME)
public class ProcessInstance {

    @XmlAttribute
    @XmlID
    @Id
    private final String processInstanceId;

    @XmlAttribute
    private final String processInstanceAlias;

    @XmlAttribute
    private final String processDefinitionKey;

    @XmlElement
    private final String processInstanceLabel;

    @XmlElement
    private final String uri;

    @XmlTransient
    @JsonIgnore
    private final boolean isDeleted;

    private ProcessInstance() {
        this(new ProcessInstance.Builder(), new ViewContext());
    }

    @SuppressWarnings("unchecked")
    private ProcessInstance(ProcessInstance.Builder builder, ViewContext context) {
        this.processInstanceId = builder.processInstanceId;
        this.processInstanceAlias = builder.processInstanceAlias;
        this.processDefinitionKey = builder.processDefinitionKey;
        this.processInstanceLabel = builder.processInstanceLabel;
        this.uri = context != null ? context.getApplicationUri(builder.processDefinitionKey, builder.processInstanceId) : null;
        this.isDeleted = builder.isDeleted;
    }

    public final static class Builder {

        private String processInstanceId;
        private String processInstanceAlias;
        private String processDefinitionKey;
        private String processInstanceLabel;
        private boolean isDeleted;

        public Builder() {
            super();
        }

        public Builder(piecework.process.model.ProcessInstance process, Sanitizer sanitizer) {
            this.processInstanceId = sanitizer.sanitize(process.processInstanceId);
            this.processInstanceAlias = sanitizer.sanitize(process.processInstanceAlias);
            this.processDefinitionKey = sanitizer.sanitize(process.processDefinitionKey);
            this.processInstanceLabel = sanitizer.sanitize(process.processInstanceLabel);
            this.isDeleted = process.isDeleted;
        }

        public ProcessInstance build() {
            return new ProcessInstance(this, null);
        }

        public ProcessInstance build(ViewContext context) {
            return new ProcessInstance(this, context);
        }

        public Builder processInstanceId(String processInstanceId) {
            this.processInstanceId = processInstanceId;
            return this;
        }

        public Builder processInstanceAlias(String processInstanceAlias) {
            this.processInstanceAlias = processInstanceAlias;
            return this;
        }

        public Builder processDefinitionKey(String processDefinitionKey) {
            this.processDefinitionKey = processDefinitionKey;
            return this;
        }

        public Builder processInstanceLabel(String processInstanceLabel) {
            this.processInstanceLabel = processInstanceLabel;
            return this;
        }

        public Builder delete() {
            this.isDeleted = true;
            return this;
        }

        public Builder undelete() {
            this.isDeleted = false;
            return this;
        }
    }

    public static class Constants {
        public static final String RESOURCE_LABEL = "Instance";
        public static final String ROOT_ELEMENT_NAME = "instance";
        public static final String TYPE_NAME = "InstanceType";
    }

}
