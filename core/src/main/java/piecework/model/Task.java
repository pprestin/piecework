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
import org.springframework.data.mongodb.core.mapping.Document;
import piecework.common.model.User;
import piecework.common.view.ViewContext;
import piecework.security.Sanitizer;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author James Renfro
 */
@XmlRootElement(name = Task.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = Task.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = Task.Constants.ROOT_ELEMENT_NAME)
public class Task implements Serializable {

    private static final long serialVersionUID = 8102389797252020510L;

	@XmlAttribute
    @XmlID
    @Id
    private final String taskInstanceId;

    @XmlAttribute
    private final String processInstanceId;

    @XmlAttribute
    private final String processInstanceAlias;

    @XmlAttribute
    private final String processDefinitionKey;

    @XmlAttribute
    private final String taskDefinitionKey;

    @XmlElement
    private final String processInstanceLabel;

    @XmlElement
    private final String processDefinitionLabel;

    @XmlElement
    private final String taskLabel;

    @XmlElement
    private final String taskDescription;

    @XmlElement
    private final User assignee;

    @XmlElementWrapper(name="candidateAssignees")
    @XmlElementRef(name="candidateAssignee")
    private final List<User> candidateAssignees;

    @XmlElement
    private final Date startTime;

    @XmlElement
    private final Date dueDate;

    @XmlAttribute
    private final int priority;

    @XmlAttribute
    private final String link;

    @XmlAttribute
    private final String uri;

    @XmlTransient
    @JsonIgnore
    private final boolean isDeleted;

    private Task() {
        this(new Task.Builder(), new ViewContext());
    }

    private Task(Task.Builder builder, ViewContext context) {
        this.taskInstanceId = builder.taskInstanceId;
        this.processDefinitionKey = builder.processDefinitionKey;
        this.taskDefinitionKey = builder.taskDefinitionKey;
        this.taskLabel = builder.taskLabel;
        this.taskDescription = builder.taskDescription;
        this.processDefinitionLabel = builder.processDefinitionLabel;
        this.processInstanceLabel = builder.processInstanceLabel;
        this.processInstanceId = builder.processInstanceId;
        this.processInstanceAlias = builder.processInstanceAlias;
        this.assignee = builder.assignee;
        this.candidateAssignees = builder.candidateAssignees;
        this.startTime = builder.startTime;
        this.dueDate = builder.dueDate;
        this.priority = builder.priority;
        this.link = context != null ? context.getApplicationUri(builder.processDefinitionKey, builder.taskInstanceId) : null;
        this.uri = context != null ? context.getServiceUri(builder.processDefinitionKey, builder.taskInstanceId) : null;
        this.isDeleted = builder.isDeleted;
    }

    public String getTaskInstanceId() {
		return taskInstanceId;
	}

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public String getTaskDefinitionKey() {
		return taskDefinitionKey;
	}

	public String getTaskLabel() {
		return taskLabel;
	}

    public String getTaskDescription() {
        return taskDescription;
    }

    public String getProcessDefinitionLabel() {
        return processDefinitionLabel;
    }

    public String getProcessInstanceLabel() {
        return processInstanceLabel;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public String getProcessInstanceAlias() {
        return processInstanceAlias;
    }

    public User getAssignee() {
		return assignee;
	}

	public List<User> getCandidateAssignees() {
		return candidateAssignees;
	}

    public Date getStartTime() {
        return startTime;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public int getPriority() {
        return priority;
    }

    public String getLink() {
        return link;
    }

    public String getUri() {
		return uri;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public final static class Builder {

        private String taskInstanceId;
        private String taskDefinitionKey;
        private String processInstanceId;
        private String processInstanceAlias;
        private String processDefinitionKey;
        private String processDefinitionLabel;
        private String processInstanceLabel;
        private String taskLabel;
        private String taskDescription;
        private User assignee;
        private List<User> candidateAssignees;
        private Date startTime;
        private Date dueDate;
        private int priority;
        private boolean isDeleted;

        public Builder() {
            super();
        }

        public Builder(Task task, Sanitizer sanitizer) {
            this.taskInstanceId = sanitizer.sanitize(task.taskInstanceId);
            this.taskDefinitionKey = sanitizer.sanitize(task.taskDefinitionKey);
            this.taskLabel = sanitizer.sanitize(task.taskLabel);
            this.taskDescription = sanitizer.sanitize(task.taskDescription);
            this.processDefinitionLabel = sanitizer.sanitize(task.processDefinitionLabel);
            this.processInstanceLabel = sanitizer.sanitize(task.processInstanceLabel);
            this.processInstanceId = sanitizer.sanitize(task.processInstanceId);
            this.processInstanceAlias = sanitizer.sanitize(task.processInstanceAlias);
            this.assignee = task.assignee != null ? new User.Builder(task.assignee, sanitizer).build() : null;
            if (task.candidateAssignees != null && !task.candidateAssignees.isEmpty()) {
                this.candidateAssignees = new ArrayList<User>(task.candidateAssignees.size());
                for (User candidateAssignee : candidateAssignees) {
                    this.candidateAssignees.add(new User.Builder(candidateAssignee, sanitizer).build());
                }
            }
            this.startTime = task.startTime;
            this.dueDate = task.dueDate;
            this.priority = task.priority;
            this.isDeleted = task.isDeleted;
        }

        public Task build() {
            return new Task(this, null);
        }

        public Task build(ViewContext context) {
            return new Task(this, context);
        }

        public Builder taskInstanceId(String taskInstanceId) {
            this.taskInstanceId = taskInstanceId;
            return this;
        }

        public Builder taskDefinitionKey(String taskDefinitionKey) {
            this.taskDefinitionKey = taskDefinitionKey;
            return this;
        }

        public Builder processDefinitionKey(String processDefinitionKey) {
            this.processDefinitionKey = processDefinitionKey;
            return this;
        }

        public Builder taskLabel(String taskLabel) {
            this.taskLabel = taskLabel;
            return this;
        }

        public Builder taskDescription(String taskDescription) {
            this.taskDescription = taskDescription;
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

        public Builder processInstanceId(String processInstanceId) {
            this.processInstanceId = processInstanceId;
            return this;
        }

        public Builder processInstanceAlias(String processInstanceAlias) {
            this.processInstanceAlias = processInstanceAlias;
            return this;
        }

        public Builder assignee(User assignee) {
            this.assignee = assignee;
            return this;
        }

        public Builder candidateAssignee(User candidateAssignee) {
            if (this.candidateAssignees == null)
                this.candidateAssignees = new ArrayList<User>();
            this.candidateAssignees.add(candidateAssignee);
            return this;
        }

        public Builder startTime(Date startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder dueDate(Date dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public Builder priority(int priority) {
            this.priority = priority;
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
        public static final String RESOURCE_LABEL = "Task";
        public static final String ROOT_ELEMENT_NAME = "task";
        public static final String TYPE_NAME = "TaskType";
    }

}
