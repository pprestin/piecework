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
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import piecework.common.ManyMap;
import piecework.common.ViewContext;
import piecework.enumeration.OperationType;
import piecework.model.bind.FormNameMessageMapAdapter;
import piecework.model.bind.FormNameValueEntryMapAdapter;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.*;

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

    @XmlJavaTypeAdapter(FormNameMessageMapAdapter.class)
    private final Map<String, List<Message>> messages;

    private final Map<String, Activity> activityMap;

    @XmlElement
    private final Date startTime;

    @XmlElement
    @LastModifiedDate
    private final Date lastModifiedTime;

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
    private final String deploymentId;

    @XmlTransient
    @JsonIgnore
    private final Set<String> keywords;

    @XmlTransient
    @JsonIgnore
    private final List<Operation> operations;

    @XmlTransient
    @JsonIgnore
    private final Map<String, Task> tasks;

    @XmlTransient
    @JsonIgnore
    private final List<String> submissionIds;

    @XmlElementWrapper(name="attachments")
    @XmlElementRef
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
        this.deploymentId = builder.deploymentId;
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
        this.tasks = Collections.unmodifiableMap(builder.tasks);
        this.activityMap = builder.activityMap != null ? Collections.unmodifiableMap(builder.activityMap) : null;
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

        this.messages = Collections.unmodifiableMap(builder.messages);
        this.attachments = Collections.unmodifiableSet(builder.attachments);
        this.operations = Collections.unmodifiableList(builder.operations);
        this.submissionIds = builder.submissionIds != null ? Collections.unmodifiableList(builder.submissionIds) : null;
        this.startTime = builder.startTime;
        this.lastModifiedTime = builder.lastModifiedTime;
        this.endTime = builder.endTime;
        this.initiatorId = builder.initiatorId;
        this.link = context != null ? context.getApplicationUri(Constants.ROOT_ELEMENT_NAME, builder.processDefinitionKey, builder.processInstanceId) : null;
        this.uri = context != null ? context.getServiceUri(Constants.ROOT_ELEMENT_NAME, builder.processDefinitionKey, builder.processInstanceId) : null;
        this.isDeleted = builder.isDeleted;
    }

    @JsonIgnore
    public boolean isInitiator(Entity principal) {
        return getInitiatorId() != null && principal != null && getInitiatorId().equals(principal.getEntityId());
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

    @JsonIgnore
    public String getDeploymentId() {
        return deploymentId;
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

    public Date getLastModifiedTime() {
        return lastModifiedTime;
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
    public Map<String, List<Message>> getMessages() {
        return messages;
    }

    public Map<String, Activity> getActivityMap() {
        return activityMap;
    }

    @JsonIgnore
    public Set<Task> getTasks() {
        return new TreeSet<Task>(tasks.values());
    }

    @JsonIgnore
    public Task getTask(String id) {
        return tasks.get(id);
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
        private String deploymentId;
        private String alias;
        private String processDefinitionKey;
        private String processDefinitionLabel;
        private String processInstanceLabel;
        private String processStatus;
        private String applicationStatus;
        private String applicationStatusExplanation;
        private String previousApplicationStatus;
        private ManyMap<String, Value> data;
        private ManyMap<String, Message> messages;
        private Map<String, Activity> activityMap;
        private Set<String> keywords;
        private Set<Attachment> attachments;
        private Set<String> attachmentIds;
        private List<Operation> operations;
        private List<String> submissionIds;
        private Map<String, Task> tasks;
        private Date startTime;
        private Date lastModifiedTime;
        private Date endTime;
        private String initiatorId;
        private boolean isDeleted;

        public Builder() {
            super();
            this.attachments = new TreeSet<Attachment>();
            this.attachmentIds = new HashSet<String>();
            this.keywords = new HashSet<String>();
            this.data = new ManyMap<String, Value>();
            this.messages = new ManyMap<String, Message>();
            this.operations = new ArrayList<Operation>();
            this.submissionIds = new ArrayList<String>();
            this.tasks = new HashMap<String, Task>();
        }

        public Builder(ProcessInstance instance) {
            this.processInstanceId = instance.processInstanceId;
            this.engineProcessInstanceId = instance.engineProcessInstanceId;
            this.deploymentId = instance.deploymentId;
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
            this.lastModifiedTime = instance.lastModifiedTime;
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

            if (instance.messages != null && !instance.messages.isEmpty())
                this.messages = new ManyMap<String, Message>(instance.messages);
            else
                this.messages = new ManyMap<String, Message>();

            if (instance.operations != null && !instance.operations.isEmpty())
                this.operations = new ArrayList<Operation>(instance.operations);
            else
                this.operations = new ArrayList<Operation>();

            if (instance.submissionIds != null && !instance.submissionIds.isEmpty())
                this.submissionIds = new ArrayList<String>(instance.submissionIds);
            else
                this.submissionIds = new ArrayList<String>();

            if (instance.tasks != null && !instance.tasks.isEmpty())
                this.tasks = new HashMap<String, Task>(instance.tasks);
            else
                this.tasks = new HashMap<String, Task>();

            if (instance.activityMap != null && !instance.activityMap.isEmpty()) {
                this.activityMap = new HashMap<String, Activity>(instance.activityMap.size());
                for (Map.Entry<String, Activity> entry : instance.activityMap.entrySet()) {
                    String key = entry.getKey();
                    if (key == null)
                        continue;
                    if (entry.getValue() == null)
                        continue;

                    this.activityMap.put(key, entry.getValue());
                }
            }

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

        public Builder clearTasks() {
            this.tasks = new HashMap<String, Task>();
            return this;
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

        public Builder deploymentId(String deploymentId) {
            this.deploymentId = deploymentId;
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

        public Builder task(Task task) {
            this.tasks.put(task.getTaskInstanceId(), task);
            return this;
        }

        public Builder tasks(List<Task> tasks) {
            if (tasks != null) {
                for (Task task : tasks) {
                    task(task);
                }
            }
            return this;
        }

        public Builder data(Map<String, List<Value>> data) {
            if (data != null && !data.isEmpty()) {
                for (Map.Entry<String, List<Value>> entry : data.entrySet()) {
                    List<Value> values = entry.getValue();
                    this.data.put(entry.getKey(), values);

                    for (Value value : values) {
                        if (value == null)
                            continue;

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

        public Builder formValue(String key, Value value) {
            if (value != null) {
                this.data.putOne(key, value);

                if (StringUtils.isNotEmpty(value.getValue()))
                    this.keywords.add(value.getValue().toLowerCase());
            }
            return this;
        }

        public Builder removeAttachment(String attachmentId) {
            if (this.attachmentIds != null && attachmentId != null)
                this.attachmentIds.remove(attachmentId);

            return this;
        }

        public Builder attachmentId(String attachmentId) {
            if (StringUtils.isNotEmpty(attachmentId))
                this.attachmentIds.add(attachmentId);
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

        public Builder activityMap(Map<String, Activity> activityMap) {
            this.activityMap = activityMap;
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
