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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import piecework.Sanitizer;
import piecework.common.view.ViewContext;

/**
 * @author James Renfro
 */
@XmlRootElement(name = ProcessInstance.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = ProcessInstance.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = ProcessInstance.Constants.ROOT_ELEMENT_NAME)
public class ProcessInstance implements Serializable {

    private static final long serialVersionUID = 4843247727802261648L;

	@XmlAttribute
    @XmlID
    @Id
    private final String processInstanceId;

    @XmlAttribute
    private final String alias;

    @XmlAttribute
    private final String processDefinitionKey;

    @XmlElement
    private final String processDefinitionLabel;
    
    @XmlElement
    private final String processInstanceLabel;

    @XmlElementWrapper(name="formData")
	@XmlElementRef 
	private final List<FormValue> formData;
    
    @XmlElement
    private final String uri;

    @XmlTransient
    @JsonIgnore
    private final boolean isDeleted;

    private ProcessInstance() {
        this(new ProcessInstance.Builder(), new ViewContext());
    }

    private ProcessInstance(ProcessInstance.Builder builder, ViewContext context) {
        this.processInstanceId = builder.processInstanceId;
        this.alias = builder.alias;
        this.processDefinitionKey = builder.processDefinitionKey;
        this.processDefinitionLabel = builder.processDefinitionLabel;
        this.processInstanceLabel = builder.processInstanceLabel;
        this.formData = builder.formData != null ? Collections.unmodifiableList(builder.formData) : null;
        this.uri = context != null ? context.getApplicationUri(builder.processDefinitionKey, builder.processInstanceId) : null;
        this.isDeleted = builder.isDeleted;
    }

    public String getProcessInstanceId() {
		return processInstanceId;
	}

	public String getAlias() {
		return alias;
	}

	public String getProcessDefinitionKey() {
		return processDefinitionKey;
	}

	public String getProcessDefinitionLabel() {
		return processDefinitionLabel;
	}

	public String getProcessInstanceLabel() {
		return processInstanceLabel;
	}

	public List<FormValue> getFormData() {
		return formData;
	}
	
	public String getUri() {
		return uri;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public final static class Builder {

        private String processInstanceId;
        private String alias;
        private String processDefinitionKey;
        private String processDefinitionLabel;
        private String processInstanceLabel;
        private List<FormValue> formData;
        private boolean isDeleted;

        public Builder() {
            super();
        }

        public Builder(piecework.model.ProcessInstance instance, Sanitizer sanitizer) {
            this.processInstanceId = sanitizer.sanitize(instance.processInstanceId);
            this.alias = sanitizer.sanitize(instance.alias);
            this.processDefinitionKey = sanitizer.sanitize(instance.processDefinitionKey);
            this.processDefinitionLabel = sanitizer.sanitize(instance.processDefinitionLabel);
            this.processInstanceLabel = sanitizer.sanitize(instance.processInstanceLabel);
            this.isDeleted = instance.isDeleted;
            
            if (instance.formData != null && !instance.formData.isEmpty()) {
				this.formData = new ArrayList<FormValue>(instance.formData.size());
				for (FormValue formValue : instance.formData) {
					this.formData.add(new FormValue.Builder(formValue, sanitizer).build());
				}
			}
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

        public Builder alias(String alias) {
            this.alias = alias;
            return this;
        }

        public Builder processDefinitionKey(String processDefinitionKey) {
            this.processDefinitionKey = processDefinitionKey;
            return this;
        }

        public Builder processDefinitionLabel(String processDefinitionLabel) {
            this.processDefinitionLabel = processDefinitionLabel;
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
