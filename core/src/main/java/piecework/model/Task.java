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

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import piecework.Sanitizer;
import piecework.common.model.User;
import piecework.common.view.ViewContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.*;

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
    private final String taskDefinitionKey;

    @XmlElement
    private final String taskInstanceLabel;

    @XmlElement
    private final User assignee;

    @XmlElementWrapper(name="candidateAssignees")
    private final List<User> candidateAssignees;

    @XmlElement
    private final String uri;

    @XmlTransient
    @JsonIgnore
    private final boolean isDeleted;

    private Task() {
        this(new Task.Builder(), new ViewContext());
    }

    private Task(Task.Builder builder, ViewContext context) {
        this.taskInstanceId = builder.taskInstanceId;
        this.taskDefinitionKey = builder.taskDefinitionKey;
        this.taskInstanceLabel = builder.taskInstanceLabel;
        this.assignee = builder.assignee;
        this.candidateAssignees = builder.candidateAssignees;
        this.uri = context != null ? context.getApplicationUri(builder.processDefinitionKey, builder.taskInstanceId) : null;
        this.isDeleted = builder.isDeleted;
    }

    public String getTaskInstanceId() {
		return taskInstanceId;
	}

	public String getTaskDefinitionKey() {
		return taskDefinitionKey;
	}

	public String getTaskInstanceLabel() {
		return taskInstanceLabel;
	}

	public User getAssignee() {
		return assignee;
	}

	public List<User> getCandidateAssignees() {
		return candidateAssignees;
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
        private String processDefinitionKey;
        private String taskInstanceLabel;
        private User assignee;
        private List<User> candidateAssignees;
        private boolean isDeleted;

        public Builder() {
            super();
        }

        public Builder(Task task, Sanitizer sanitizer) {
            this.taskInstanceId = sanitizer.sanitize(task.taskInstanceId);
            this.taskDefinitionKey = sanitizer.sanitize(task.taskDefinitionKey);
            this.taskInstanceLabel = sanitizer.sanitize(task.taskInstanceLabel);
            this.assignee = task.assignee != null ? new User.Builder(task.assignee, sanitizer).build() : null;
            if (task.candidateAssignees != null && !task.candidateAssignees.isEmpty()) {
                this.candidateAssignees = new ArrayList<User>(task.candidateAssignees.size());
                for (User candidateAssignee : candidateAssignees) {
                    this.candidateAssignees.add(new User.Builder(candidateAssignee, sanitizer).build());
                }
            }
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

        public Builder taskInstanceLabel(String taskInstanceLabel) {
            this.taskInstanceLabel = taskInstanceLabel;
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
