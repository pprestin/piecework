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
import java.util.*;

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import piecework.security.Sanitizer;
import piecework.common.ViewContext;
import piecework.util.ManyMap;

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
    private String processInstanceId;

    @XmlAttribute
    private final String alias;

    @XmlAttribute
    private final String processDefinitionKey;

    @XmlElement
    private final String processDefinitionLabel;
    
    @XmlElement
    private final String processInstanceLabel;

    @XmlElement
    private final String processStatus;

    @XmlElement
    private final String applicationStatus;

    @XmlElement
    private final String applicationStatusExplanation;

    @XmlElementWrapper(name="formData")
	@XmlElementRef 
	private final List<FormValue> formData;

    @XmlElement
    private final Date startTime;

    @XmlElement
    private final Date endTime;

    @XmlElement
    private final String initiatorId;

    @XmlAttribute
    private final String link;

    @XmlAttribute
    private final String uri;

    @XmlTransient
    @JsonIgnore
    private final String engineProcessInstanceId;

    @XmlTransient
    @JsonIgnore
    private final List<FormValue> restrictedData;

    @XmlTransient
    @JsonIgnore
    private final Set<String> keywords;

    @XmlTransient
    @JsonIgnore
    @DBRef
    private final List<FormSubmission> submissions;

    @XmlTransient
    @JsonIgnore
    @DBRef
    private final List<Attachment> attachments;

    @XmlTransient
    @JsonIgnore
    private final boolean isDeleted;

    private ProcessInstance() {
        this(new ProcessInstance.Builder(), new ViewContext());
    }

    private ProcessInstance(ProcessInstance.Builder builder, ViewContext context) {
        this.processInstanceId = builder.processInstanceId;
        this.engineProcessInstanceId = builder.engineProcessInstanceId;
        this.alias = builder.alias;
        this.processDefinitionKey = builder.processDefinitionKey;
        this.processDefinitionLabel = builder.processDefinitionLabel;
        this.processInstanceLabel = builder.processInstanceLabel;
        this.processStatus = builder.processStatus;
        this.applicationStatus = builder.applicationStatus;
        this.applicationStatusExplanation = builder.applicationStatusExplanation;
        this.formData = builder.formData != null ? Collections.unmodifiableList(builder.formData) : null;
        this.restrictedData = builder.restrictedData != null ? Collections.unmodifiableList(builder.restrictedData) : null;
        this.keywords = builder.keywords;
        List<Attachment> attachments = builder.attachmentBuilders != null && !builder.attachmentBuilders.isEmpty() ? new ArrayList<Attachment>(builder.attachmentBuilders.size()) : Collections.<Attachment>emptyList();
        if (builder.attachmentBuilders != null) {
            for (Attachment.Builder attachmentBuilder : builder.attachmentBuilders) {
                attachments.add(attachmentBuilder.build(context));
            }
        }
        this.attachments = Collections.unmodifiableList(attachments);
        this.submissions = builder.submissions != null ? Collections.unmodifiableList(builder.submissions) : null;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.initiatorId = builder.initiatorId;
        this.link = context != null ? context.getApplicationUri(builder.processDefinitionKey, builder.processInstanceId) : null;
        this.uri = context != null ? context.getServiceUri(builder.processDefinitionKey, builder.processInstanceId) : null;
        this.isDeleted = builder.isDeleted;
    }

    public String getProcessInstanceId() {
		return processInstanceId;
	}

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    @JsonIgnore
    public String getEngineProcessInstanceId() {
        return engineProcessInstanceId;
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

    public String getProcessStatus() {
        return processStatus;
    }

    public String getApplicationStatus() {
        return applicationStatus;
    }

    public String getApplicationStatusExplanation() {
        return applicationStatusExplanation;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public String getInitiatorId() {
        return initiatorId;
    }

    public List<FormValue> getFormData() {
		return formData;
	}

    @JsonIgnore
    public List<FormValue> getRestrictedData() {
        return restrictedData;
    }

    @JsonIgnore
    public Set<String> getKeywords() {
        return keywords;
    }

    @JsonIgnore
	public ManyMap<String, String> getFormValueMap() {
    	ManyMap<String, String> map = new ManyMap<String, String>();
    	if (formData != null && !formData.isEmpty()) {
    		for (FormValue formValue : formData) {
    			map.put(formValue.getName(), formValue.getAllValues());
    		}
    	}
    	return map;
    }

    public List<FormSubmission> getSubmissions() {
        return submissions;
    }
	
	public List<Attachment> getAttachments() {
		return attachments;
	}

    public String getLink() {
        return link;
    }

    public String getUri() {
		return uri;
	}

    @JsonIgnore
	public boolean isDeleted() {
		return isDeleted;
	}

	public final static class Builder {

        private String processInstanceId;
        private String engineProcessInstanceId;
        private String alias;
        private String processDefinitionKey;
        private String processDefinitionLabel;
        private String processInstanceLabel;
        private String processStatus;
        private String applicationStatus;
        private String applicationStatusExplanation;
        private List<FormValue> formData;
        private List<FormValue> restrictedData;
        private Set<String> keywords;
        private List<Attachment.Builder> attachmentBuilders;
        private List<FormSubmission> submissions;
        private Date startTime;
        private Date endTime;
        private String initiatorId;
        private boolean isDeleted;

        public Builder() {
            super();
            this.keywords = new HashSet<String>();
            this.attachmentBuilders = new ArrayList<Attachment.Builder>();
        }

        public Builder(piecework.model.ProcessInstance instance, Sanitizer sanitizer) {
            this.processInstanceId = sanitizer.sanitize(instance.processInstanceId);
            this.engineProcessInstanceId = sanitizer.sanitize(instance.engineProcessInstanceId);
            this.alias = sanitizer.sanitize(instance.alias);
            this.processDefinitionKey = sanitizer.sanitize(instance.processDefinitionKey);
            this.processDefinitionLabel = sanitizer.sanitize(instance.processDefinitionLabel);
            this.processInstanceLabel = sanitizer.sanitize(instance.processInstanceLabel);
            this.processStatus = sanitizer.sanitize(instance.processStatus);
            this.applicationStatus = sanitizer.sanitize(instance.applicationStatus);
            this.applicationStatusExplanation = sanitizer.sanitize(instance.applicationStatusExplanation);
            this.startTime = instance.startTime;
            this.endTime = instance.endTime;
            this.initiatorId = sanitizer.sanitize(instance.initiatorId);
            this.isDeleted = instance.isDeleted;
            
            if (instance.formData != null && !instance.formData.isEmpty()) {
				this.formData = new ArrayList<FormValue>(instance.formData.size());
				for (FormValue formValue : instance.formData) {
					this.formData.add(new FormValue.Builder(formValue, sanitizer).build());
				}
			}

            if (instance.submissions != null && !instance.submissions.isEmpty()) {
                this.submissions = new ArrayList<FormSubmission>(instance.submissions.size());
                for (FormSubmission submission : instance.submissions) {
                    if (submission == null)
                        continue;
                    this.submissions.add(new FormSubmission.Builder(submission, sanitizer).build());
                }
            }
            
            if (instance.attachments != null && !instance.attachments.isEmpty()) {
                this.attachmentBuilders = new ArrayList<Attachment.Builder>(instance.attachments.size());
                for (Attachment attachment : instance.attachments) {
                    this.attachmentBuilders.add(new Attachment.Builder(attachment, sanitizer));
                }
            } else {
                this.attachmentBuilders = new ArrayList<Attachment.Builder>();
            }
            this.keywords = new HashSet<String>();
            if (StringUtils.isNotEmpty(this.processInstanceLabel))
                this.keywords.add(this.processInstanceLabel.toLowerCase());
            if (StringUtils.isNotEmpty(this.alias))
                this.keywords.add(this.alias.toLowerCase());
            if (StringUtils.isNotEmpty(this.engineProcessInstanceId))
                this.keywords.add(this.engineProcessInstanceId);
            if (StringUtils.isNotEmpty(this.alias))
                this.keywords.add(this.alias.toLowerCase());
        }

        public ProcessInstance build() {
            return new ProcessInstance(this, null);
        }

        public ProcessInstance build(ViewContext context) {
            return new ProcessInstance(this, context);
        }

        public Builder processInstanceId(String processInstanceId) {
            this.processInstanceId = processInstanceId;
            if (StringUtils.isNotEmpty(this.processInstanceId))
                this.keywords.add(this.processInstanceId);
            return this;
        }

        public Builder engineProcessInstanceId(String engineProcessInstanceId) {
            this.engineProcessInstanceId = engineProcessInstanceId;
            if (StringUtils.isNotEmpty(this.engineProcessInstanceId))
                this.keywords.add(this.engineProcessInstanceId);
            return this;
        }

        public Builder alias(String alias) {
            this.alias = alias;
            if (StringUtils.isNotEmpty(this.alias))
                this.keywords.add(this.alias.toLowerCase());
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
            if (StringUtils.isNotEmpty(this.processInstanceLabel))
                this.keywords.add(this.processInstanceLabel.toLowerCase());
            return this;
        }

        public Builder processStatus(String processStatus) {
            this.processStatus = processStatus;
            return this;
        }

        public Builder applicationStatus(String applicationStatus) {
            this.applicationStatus = applicationStatus;
            return this;
        }

        public Builder applicationStatusExplanation(String applicationStatusExplanation) {
            this.applicationStatusExplanation = applicationStatusExplanation;
            return this;
        }

        public Builder startTime(Date startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(Date endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder initiatorId(String initiatorId) {
            this.initiatorId = initiatorId;
            return this;
        }

        public Builder formValue(String key, String ... values) {
            if (this.formData == null)
                this.formData = new ArrayList<FormValue>();
            this.formData.add(new FormValue.Builder().name(key).values(values).build());
            if (values.length > 0) {
                for (String value : values) {
                    if (StringUtils.isNotEmpty(value))
                        this.keywords.add(value.toLowerCase());
                }
            }

            return this;
        }

        public Builder formData(List<FormValue> formData) {
            this.formData = formData;
            return this;
        }

        public Builder formValueMap(Map<String, List<String>> formValueMap) {
            ManyMap<String, String> map = new ManyMap<String, String>();
            if (this.formData != null && !this.formData.isEmpty()) {
                for (FormValue formValue : this.formData) {
                    map.put(formValue.getName(), formValue.getAllValues());
                }
            }
            if (formValueMap != null && !formValueMap.isEmpty()) {
                for (Map.Entry<String, List<String>> entry : formValueMap.entrySet()) {
                    map.put(entry.getKey(), entry.getValue());
                }
            }
            this.formData = new ArrayList<FormValue>();
            if (!map.isEmpty()) {
                for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                    List<String> values = entry.getValue();
                    this.formData.add(new FormValue.Builder().name(entry.getKey()).values(values.toArray(new String[values.size()])).build());
                    if (values.size() > 0) {
                        for (String value : values) {
                            if (StringUtils.isNotEmpty(value))
                                this.keywords.add(value.toLowerCase());
                        }
                    }
                }
            }
            return this;
        }

        public Builder restrictedData(List<FormValue> restrictedData) {
            this.restrictedData = restrictedData;
            return this;
        }

        public Builder restrictedValueMap(Map<String, List<String>> restrictedValueMap) {
            ManyMap<String, String> map = new ManyMap<String, String>();
            if (this.restrictedData != null && !this.restrictedData.isEmpty()) {
                for (FormValue formValue : this.restrictedData) {
                    map.put(formValue.getName(), formValue.getAllValues());
                }
            }
            if (restrictedValueMap != null && !restrictedValueMap.isEmpty()) {
                for (Map.Entry<String, List<String>> entry : restrictedValueMap.entrySet()) {
                    map.put(entry.getKey(), entry.getValue());
                }
            }
            this.restrictedData = new ArrayList<FormValue>();
            if (!map.isEmpty()) {
                for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                    List<String> values = entry.getValue();
                    this.restrictedData.add(new FormValue.Builder().name(entry.getKey()).values(values.toArray(new String[values.size()])).build());
                }
            }
            return this;
        }

        public Builder attachment(Attachment attachment, Sanitizer sanitizer) {
            if (this.attachmentBuilders == null)
                this.attachmentBuilders = new ArrayList<Attachment.Builder>();
            this.attachmentBuilders.add(new Attachment.Builder(attachment, sanitizer));
            return this;
        }

        public Builder attachments(List<Attachment> attachments, Sanitizer sanitizer) {
        	if (this.attachmentBuilders != null)
        		this.attachmentBuilders = new ArrayList<Attachment.Builder>();
            if (attachments != null) {
        	    for (Attachment attachment : attachments) {
                    this.attachmentBuilders.add(new Attachment.Builder(attachment, sanitizer));
                }
            }
        	return this;
        }

        public Builder submission(FormSubmission submission) {
            if (this.submissions == null)
                this.submissions = new ArrayList<FormSubmission>();
            this.submissions.add(submission);
            return this;
        }

        public Builder submissions(List<FormSubmission> submissions) {
            if (this.submissions != null)
                this.submissions = new ArrayList<FormSubmission>();
            if (submissions != null)
                this.submissions.addAll(submissions);
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
