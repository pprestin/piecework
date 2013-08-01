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

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import piecework.model.bind.FormNameValueEntryMapAdapter;
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
@XmlSeeAlso({Value.class, File.class})
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

    @XmlJavaTypeAdapter(FormNameValueEntryMapAdapter.class)
    private final Map<String, List<? extends Value>> data;

    @XmlTransient
    private final Map<String, List<? extends Value>> restrictedData;

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
    private final Set<String> keywords;

    @XmlTransient
    @JsonIgnore
    @DBRef
    private final List<Submission> submissions;

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
        this.data = Collections.unmodifiableMap((Map<String, List<? extends Value>>)builder.data);
        this.restrictedData = Collections.unmodifiableMap((Map<String, List<? extends Value>>)builder.restrictedData);
        this.keywords = builder.keywords;
        List<Attachment> attachments = builder.attachments != null && !builder.attachments.isEmpty() ? new ArrayList<Attachment>(builder.attachments.size()) : Collections.<Attachment>emptyList();
        if (builder.attachments != null) {
            for (Attachment attachment : builder.attachments) {
                attachments.add(new Attachment.Builder(attachment).build(context));
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

    public Map<String, List<? extends Value>> getData() {
        return data;
    }

    @JsonIgnore
    public Map<String, List<? extends Value>> getRestrictedData() {
        return restrictedData;
    }

    @JsonIgnore
    public Set<String> getKeywords() {
        return keywords;
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

//    @JsonIgnore
//    public Map<String, FormValue> getFormValueMap() {
//        Map<String, FormValue> map = new HashMap<String, FormValue>();
//        if (formData != null && !formData.isEmpty()) {
//            for (FormValue formValue : formData) {
//                String name = formValue.getName();
//                List<String> values = formValue.getValues();
//                if (name != null && values != null)
//                    map.put(name, formValue);
//            }
//        }
//        return map;
//    }
//
//    @JsonIgnore
//	public ManyMap<String, String> getFormValueContentMap() {
//    	ManyMap<String, String> map = new ManyMap<String, String>();
//    	if (formData != null && !formData.isEmpty()) {
//    		for (FormValue formValue : formData) {
//    			map.put(formValue.getName(), formValue.getValues());
//    		}
//    	}
//    	return map;
//    }

    public List<Submission> getSubmissions() {
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
        private ManyMap<String, Value> data;
        private ManyMap<String, Value> restrictedData;
        private Set<String> keywords;
        private List<Attachment> attachments;
        private List<Submission> submissions;
        private Date startTime;
        private Date endTime;
        private String initiatorId;
        private boolean isDeleted;

        public Builder() {
            super();
            this.attachments = new ArrayList<Attachment>();
            this.keywords = new HashSet<String>();
            this.data = new ManyMap<String, Value>();
            this.restrictedData = new ManyMap<String, Value>();
            this.submissions = new ArrayList<Submission>();
        }

        public Builder(ProcessInstance instance) {
            this.processInstanceId = instance.processInstanceId;
            this.engineProcessInstanceId = instance.engineProcessInstanceId;
            this.alias = instance.alias;
            this.processDefinitionKey = instance.processDefinitionKey;
            this.processDefinitionLabel = instance.processDefinitionLabel;
            this.processInstanceLabel = instance.processInstanceLabel;
            this.processStatus = instance.processStatus;
            this.applicationStatus = instance.applicationStatus;
            this.applicationStatusExplanation = instance.applicationStatusExplanation;
            this.startTime = instance.startTime;
            this.endTime = instance.endTime;
            this.initiatorId = instance.initiatorId;
            this.isDeleted = instance.isDeleted;

            if (instance.attachments != null && !instance.attachments.isEmpty())
                this.attachments = new ArrayList<Attachment>(instance.attachments);
            else
                this.attachments = new ArrayList<Attachment>();

            if (instance.keywords != null && !instance.keywords.isEmpty())
                this.keywords = new HashSet<String>(instance.keywords);
            else
                this.keywords = new HashSet<String>();

            if (instance.data != null && !instance.data.isEmpty())
				this.data = new ManyMap<String, Value>((Map<String,List<Value>>) instance.data);
			else
                this.data = new ManyMap<String, Value>();

            if (instance.restrictedData != null && !instance.restrictedData.isEmpty())
                this.restrictedData = new ManyMap<String, Value>((Map<String,List<Value>>) instance.restrictedData);
            else
                this.restrictedData = new ManyMap<String, Value>();

            if (instance.submissions != null && !instance.submissions.isEmpty())
                this.submissions = new ArrayList<Submission>(instance.submissions);
            else
                this.submissions = new ArrayList<Submission>();

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

        public Builder data(ManyMap<String, Value> data) {
            if (data != null && !data.isEmpty()) {
                for (Map.Entry<String, List<Value>> entry : data.entrySet()) {
                    List<Value> values = entry.getValue();
                    this.data.put(entry.getKey(), values);

                    for (Value value : values) {
                        if (value instanceof File) {
                            File file = File.class.cast(value);
                            if (StringUtils.isNotEmpty(file.getName()))
                                this.keywords.add(file.getName().toLowerCase());
                        } else {
                            if (StringUtils.isNotEmpty(value.getValue()))
                                this.keywords.add(value.getValue().toLowerCase());
                        }
                    }
                }
            }
            return this;
        }

        public Builder restrictedData(ManyMap<String, Value> data) {
            if (data != null && !data.isEmpty()) {
                for (Map.Entry<String, List<Value>> entry : data.entrySet()) {
                    this.restrictedData.put(entry.getKey(), entry.getValue());
                }
            }
            return this;
        }

        public Builder formValue(String key, String ... values) {
            if (values != null && values.length > 0) {
                List<Value> list = new ArrayList<Value>(values.length);

                for (String value : values) {
                    list.add(new Value(value));
                    if (StringUtils.isNotEmpty(value))
                        this.keywords.add(value.toLowerCase());
                }
                this.data.put(key, list);
            }

            return this;
        }

//        public Builder formData(List<FormValue> formData) {
//            this.formData = formData;
//            return this;
//        }
//
//        public Builder formValueMap(Map<String, FormValue> formValueMap) {
//            Map<String, FormValue> map = new HashMap<String, FormValue>();
//            if (this.formData != null && !this.formData.isEmpty()) {
//                for (FormValue formValue : this.formData) {
//                    map.put(formValue.getName(), formValue);
//                }
//            }
//            if (formValueMap != null && !formValueMap.isEmpty()) {
//                for (Map.Entry<String, FormValue> entry : formValueMap.entrySet()) {
//                    map.put(entry.getKey(), entry.getValue());
//                }
//            }
//            this.formData = new ArrayList<FormValue>();
//            if (!map.isEmpty()) {
//                for (Map.Entry<String, FormValue> entry : map.entrySet()) {
//                    FormValue formValue = entry.getValue();
//                    if (formValue == null)
//                        continue;
//
//                    this.formData.add(formValue);
//                    List<String> values = formValue.getValues();
//                    if (values != null && !values.isEmpty()) {
//                        for (String value : values) {
//                            if (StringUtils.isNotEmpty(value))
//                                this.keywords.add(value.toLowerCase());
//                        }
//                    }
//                }
//            }
//            return this;
//        }

        public Builder removeAttachment(String attachmentId) {
            if (this.attachments != null && attachmentId != null) {
                List<Attachment> all = this.attachments;
                this.attachments = new ArrayList<Attachment>();
                for (Attachment attachment : all) {
                    if (attachment.getAttachmentId() == null || !attachmentId.equals(attachment.getAttachmentId()))
                        this.attachments.add(attachment);
                }
            }
            return this;
        }

//        public Builder restrictedData(List<FormValue> restrictedData) {
//            this.restrictedData = restrictedData;
//            return this;
//        }
//
//        public Builder restrictedValueMap(Map<String, FormValue> restrictedValueMap) {
//            Map<String, FormValue> map = new HashMap<String, FormValue>();
//            if (this.restrictedData != null && !this.restrictedData.isEmpty()) {
//                for (FormValue formValue : this.restrictedData) {
//                    map.put(formValue.getName(), formValue);
//                }
//            }
//            if (restrictedValueMap != null && !restrictedValueMap.isEmpty()) {
//                for (Map.Entry<String, FormValue> entry : restrictedValueMap.entrySet()) {
//                    map.put(entry.getKey(), entry.getValue());
//                }
//            }
//            this.restrictedData = new ArrayList<FormValue>();
//            if (!map.isEmpty()) {
//                for (Map.Entry<String, FormValue> entry : map.entrySet()) {
//                    FormValue formValue = entry.getValue();
//                    if (formValue == null)
//                        continue;
//
//                    this.restrictedData.add(formValue);
//                }
//            }
//            return this;
//        }

        public Builder attachment(Attachment attachment) {
            if (attachment != null)
                this.attachments.add(attachment);
            return this;
        }

        public Builder attachments(List<Attachment> attachments) {
            if (attachments != null)
                this.attachments.addAll(attachments);
        	return this;
        }

        public Builder submission(Submission submission) {
            if (submission != null)
                this.submissions.add(submission);
            return this;
        }

        public Builder submissions(List<Submission> submissions) {
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
