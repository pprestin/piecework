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
import org.apache.commons.lang.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import piecework.enumeration.OperationType;
import piecework.model.bind.FormNameValueEntryMapAdapter;
import piecework.common.ViewContext;
import piecework.security.concrete.PassthroughSanitizer;
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

    @XmlElement
    private final String previousApplicationStatus;

    @XmlJavaTypeAdapter(FormNameValueEntryMapAdapter.class)
    private final Map<String, List<Value>> data;

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
    private final List<Operation> operations;

    @XmlTransient
    @JsonIgnore
//    @DBRef
    private final List<String> submissionIds;

    @XmlElementWrapper(name="attachments")
    @XmlElementRef
//    @DBRef
    @Transient
    private final Set<Attachment> attachments;

    @XmlTransient
    @JsonIgnore
    private final Set<String> attachmentIds;

    @XmlTransient
    @JsonIgnore
    private final boolean isDeleted;

    private ProcessInstance() {
        this(new ProcessInstance.Builder(), new ViewContext());
    }

    private ProcessInstance(Builder builder, ViewContext context) {
        this.processInstanceId = builder.processInstanceId;
        this.engineProcessInstanceId = builder.engineProcessInstanceId;
        this.alias = builder.alias;
        this.processDefinitionKey = builder.processDefinitionKey;
        this.processDefinitionLabel = builder.processDefinitionLabel;
        this.processInstanceLabel = builder.processInstanceLabel;
        this.processStatus = builder.processStatus;
        this.applicationStatus = builder.applicationStatus;
        this.applicationStatusExplanation = builder.applicationStatusExplanation;
        this.previousApplicationStatus = builder.previousApplicationStatus;
        this.attachmentIds = Collections.unmodifiableSet(builder.attachmentIds);
        this.keywords = builder.keywords;

//        Set<Attachment> attachments = null;
//        if (includeSubresources) {
//            attachments = builder.attachments != null && !builder.attachments.isEmpty() ? new TreeSet<Attachment>(builder.attachments.size()) : Collections.<Attachment>emptyList();
//            if (builder.attachments != null) {
//                for (Attachment attachment : builder.attachments) {
//                    attachments.add(new Attachment.Builder(attachment).build(context));
//                }
//            }
//            if (builder.data != null && !builder.data.isEmpty()) {
//                ManyMap<String, Value> data = new ManyMap<String, Value>();
//                for (Map.Entry<String, List<Value>> entry : builder.data.entrySet()) {
//
//                    List<Value> values = entry.getValue();
//                    if (values == null)
//                        continue;
//
//                    String fieldName = entry.getKey();
//                    for (Value value : values) {
//                        if (value instanceof File) {
//                            File file = File.class.cast(value);
//
//                            data.putOne(fieldName, new File.Builder(file)
//                                    .processDefinitionKey(processDefinitionKey)
//                                    .processInstanceId(processInstanceId)
//                                    .fieldName(fieldName)
//                                    .build(context));
//                        } else {
//                            data.putOne(fieldName, value);
//                        }
//                    }
//                }
//                this.data = Collections.unmodifiableMap(data);
//            } else {
//                this.data = Collections.emptyMap();
//            }
//        } else {
//            attachments = Collections.emptyList();
//            this.data = Collections.emptyMap();
//        }

        if (context != null) {
            if (builder.data != null && !builder.data.isEmpty()) {
                ManyMap<String, Value> data = new ManyMap<String, Value>();
                for (Map.Entry<String, List<Value>> entry : builder.data.entrySet()) {

                    List<Value> values = entry.getValue();
                    if (values == null)
                        continue;

                    String fieldName = entry.getKey();
                    for (Value value : values) {
                        if (value instanceof File) {
                            File file = File.class.cast(value);

                            data.putOne(fieldName, new File.Builder(file)
                                    .processDefinitionKey(processDefinitionKey)
                                    .processInstanceId(processInstanceId)
                                    .fieldName(fieldName)
                                    .build(context));
                        } else {
                            data.putOne(fieldName, value);
                        }
                    }
                }
                this.data = Collections.unmodifiableMap(data);
            } else {
                this.data = Collections.emptyMap();
            }
        } else {
            this.data = Collections.unmodifiableMap(builder.data);
        }

        this.attachments = Collections.unmodifiableSet(builder.attachments);
        this.operations = Collections.unmodifiableList(builder.operations);
        this.submissionIds = builder.submissionIds != null ? Collections.unmodifiableList(builder.submissionIds) : null;
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

    public String getPreviousApplicationStatus() {
        return previousApplicationStatus;
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

    public Map<String, List<Value>> getData() {
        return data;
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

    public List<Operation> getOperations() {
        return operations;
    }

    public List<String> getSubmissionIds() {
        return submissionIds;
    }
	
	public Set<Attachment> getAttachments() {
		return attachments;
	}

    public Set<String> getAttachmentIds() {
        return attachmentIds;
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
        private String previousApplicationStatus;
        private ManyMap<String, Value> data;
        private Set<String> keywords;
        private Set<Attachment> attachments;
        private Set<String> attachmentIds;
        private List<Operation> operations;
        private List<String> submissionIds;
        private Date startTime;
        private Date endTime;
        private String initiatorId;
        private boolean isDeleted;

        public Builder() {
            super();
            this.attachments = new TreeSet<Attachment>();
            this.attachmentIds = new HashSet<String>();
            this.keywords = new HashSet<String>();
            this.data = new ManyMap<String, Value>();
            this.operations = new ArrayList<Operation>();
            this.submissionIds = new ArrayList<String>();
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
            this.previousApplicationStatus = instance.previousApplicationStatus;
            this.startTime = instance.startTime;
            this.endTime = instance.endTime;
            this.initiatorId = instance.initiatorId;
            this.isDeleted = instance.isDeleted;

            if (instance.attachments != null && !instance.attachments.isEmpty())
                this.attachments = new TreeSet<Attachment>(instance.attachments);
            else
                this.attachments = new TreeSet<Attachment>();

            if (instance.attachmentIds != null && !instance.attachmentIds.isEmpty())
                this.attachmentIds = new HashSet<String>(instance.attachmentIds);
            else
                this.attachmentIds = new HashSet<String>();

            if (instance.keywords != null && !instance.keywords.isEmpty())
                this.keywords = new HashSet<String>(instance.keywords);
            else
                this.keywords = new HashSet<String>();

            if (instance.data != null && !instance.data.isEmpty())
				this.data = new ManyMap<String, Value>(instance.data);
			else
                this.data = new ManyMap<String, Value>();

            if (instance.operations != null && !instance.operations.isEmpty())
                this.operations = new ArrayList<Operation>(instance.operations);
            else
                this.operations = new ArrayList<Operation>();

            if (instance.submissionIds != null && !instance.submissionIds.isEmpty())
                this.submissionIds = new ArrayList<String>(instance.submissionIds);
            else
                this.submissionIds = new ArrayList<String>();

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

        public Builder previousApplicationStatus(String previousApplicationStatus) {
            this.previousApplicationStatus = previousApplicationStatus;
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

        public Builder operation(String id, OperationType type, String reason, Date date, String userId) {
            this.operations.add(new Operation(id, type, reason, date, userId));
            return this;
        }

        public Builder data(Map<String, List<Value>> data) {
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

        public Builder removeAttachment(String attachmentId) {
            if (this.attachmentIds != null && attachmentId != null)
                this.attachmentIds.remove(attachmentId);

            return this;
        }

        public Builder attachment(Attachment attachment) {
            if (attachment != null) {
                this.attachments.add(attachment);
                this.attachmentIds.add(attachment.getAttachmentId());
            }
            return this;
        }

        public Builder attachments(Iterable<Attachment> attachments) {
            if (attachments != null) {
                for (Attachment attachment : attachments) {
                    attachment(attachment);
                }
            }

        	return this;
        }

        public Builder submission(Submission submission) {
            if (submission != null)
                this.submissionIds.add(submission.getSubmissionId());
            return this;
        }

        public Builder submissions(List<Submission> submissions) {
            if (submissions != null) {
                for (Submission submission : submissions) {
                    if (submission == null)
                        continue;
                    this.submissionIds.add(submission.getSubmissionId());
                }
            }
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
