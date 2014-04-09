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
import org.springframework.data.annotation.Transient;
import piecework.authorization.AccessAuthority;
import piecework.common.ViewContext;
import piecework.security.Sanitizer;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.*;

/**
 * @author James Renfro
 */
@XmlRootElement(name = Task.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = Task.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Task implements Serializable, Comparable<Task> {

    private static final long serialVersionUID = 8102389797252020510L;

	@XmlAttribute
    @XmlID
    @Id
    private final String taskInstanceId;

    @XmlAttribute
    private final String processInstanceId;

    @XmlTransient
    private final String engineProcessInstanceId;

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
    private final String taskStatus;

    @XmlElement
    private final String taskAction;

    @XmlElement
    @Transient
    private final User assignee;

    @XmlTransient
    private final String assigneeId;

    @XmlElementWrapper(name="candidateAssignees")
    @XmlElementRef(name="candidateAssignee")
    @Transient
    private final List<User> candidateAssignees;

    @XmlTransient
    private final Set<String> candidateAssigneeIds;

    @XmlElement
    private final Date startTime;

    @XmlElement
    private final Date endTime;

    @XmlElement
    private final Date claimTime;

    @XmlElement
    private final Date dueDate;

    @XmlAttribute
    private final int priority;

    @XmlAttribute
    private final boolean active;

    @XmlAttribute
    private final String link;

    @XmlAttribute
    private final String uri;

    @XmlTransient
    @JsonIgnore
    private final boolean isDeleted;

    @XmlTransient
    @JsonIgnore
    private final boolean disconnected;

    private Task() {
        this(new Task.Builder(), new ViewContext());
    }

    private Task(Task.Builder builder, ViewContext context) {
        this.taskInstanceId = builder.taskInstanceId;
        this.processDefinitionKey = builder.processDefinitionKey;
        this.taskDefinitionKey = builder.taskDefinitionKey;
        this.taskLabel = builder.taskLabel;
        this.taskDescription = builder.taskDescription;
        this.taskStatus = builder.taskStatus;
        this.taskAction = builder.taskAction;
        this.processDefinitionLabel = builder.processDefinitionLabel;
        this.processInstanceLabel = builder.processInstanceLabel;
        this.processInstanceId = builder.processInstanceId;
        this.processInstanceAlias = builder.processInstanceAlias;
        this.engineProcessInstanceId = builder.engineProcessInstanceId;
        this.assignee = builder.assignee;
        this.assigneeId = builder.assigneeId;
        this.candidateAssignees = Collections.unmodifiableList(builder.candidateAssignees);
        this.candidateAssigneeIds = Collections.unmodifiableSet(builder.candidateAssigneeIds);
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.claimTime = builder.claimTime;
        this.dueDate = builder.dueDate;
        this.priority = builder.priority;
        this.active = builder.active;
        this.link = context != null ? context.getApplicationUri(Constants.ROOT_ELEMENT_NAME, builder.processDefinitionKey, builder.taskInstanceId) : null;
        this.uri = context != null ? context.getServiceUri(Constants.ROOT_ELEMENT_NAME, builder.processDefinitionKey, builder.taskInstanceId) : null;
        this.isDeleted = builder.isDeleted;
        this.disconnected = builder.disconnected;
    }

    @JsonIgnore
    public boolean isAssignee(Entity entity) {
        String entityId = entity != null ? entity.getEntityId() : null;
        if (entityId == null)
            return false;  // no signed in user

        if (entity != null) {
            if (entity.getEntityType() == Entity.EntityType.SYSTEM) {
                Entity actingAs = entity.getActingAs();

                if (actingAs != null && StringUtils.isNotEmpty(actingAs.getEntityId()))
                    entityId = actingAs.getEntityId();
            }
        }

        // Check to see if the user is the assignee
        String assigneeId = getAssigneeId();
        if (StringUtils.isNotEmpty(assigneeId) && entityId.equals(assigneeId))
            return true;

        return false;
    }

    @JsonIgnore
    // returns true if my roleIds includes one of my roles
    // returns false otherwise
    public boolean isCandidateOrAssignee(Entity entity) {
        String entityId = entity != null ? entity.getEntityId() : null;
        if (entityId == null)
            return false;  // no signed in user

        // Check to see if the user is the assignee
        String assigneeId = getAssigneeId();
        if (StringUtils.isNotEmpty(assigneeId) && entityId.equals(assigneeId))
            return true;

        Set<String> candidateAssigneeIds = getCandidateAssigneeIds();
        // sanity check
        if ( candidateAssigneeIds == null || candidateAssigneeIds.isEmpty())
            return false;

        // Check if user is a candidate assignee
        if (candidateAssigneeIds.contains(entityId))
            return true;

        // Check if the user has access authority based on a group id that is in the collection of candidate assignee ids
        AccessAuthority accessAuthority = entity.getAccessAuthority();
        if (accessAuthority != null && accessAuthority.hasGroup(candidateAssigneeIds))
            return true;

        return false;
    }

    @JsonIgnore
    public boolean canEdit(Entity entity) {
        // Nobody can edit a task that is not open
        if (taskStatus == null || !taskStatus.equals(piecework.Constants.TaskStatuses.OPEN))
            return false;

        String entityId = entity != null ? entity.getEntityId() : null;
        if (entityId == null)
            return false;

        return isAssignee(entity);
    }

    @Override
    public int compareTo(Task o) {
        int result = 0;
        if (result == 0 && endTime != null && o.endTime != null)
            result = o.endTime.compareTo(endTime);
        if (result == 0 && startTime != null && o.startTime != null)
            result = o.startTime.compareTo(startTime);
        if (result == 0 && taskDefinitionKey != null && o.taskDefinitionKey != null)
            result = taskDefinitionKey.compareTo(o.taskDefinitionKey);
        if (result == 0 && taskInstanceId != null && o.taskInstanceId != null)
            result = taskInstanceId.compareTo(o.taskInstanceId);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Task that = (Task) o;

        if (!taskInstanceId.equals(that.taskInstanceId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return taskInstanceId.hashCode();
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

    public String getTaskStatus() {
        return taskStatus;
    }

    public String getTaskAction() {
        return taskAction;
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

    @JsonIgnore
    public String getEngineProcessInstanceId() {
        return engineProcessInstanceId;
    }

    public User getAssignee() {
		return assignee;
	}

	public List<User> getCandidateAssignees() {
		return candidateAssignees;
	}

    @JsonIgnore
    public String getAssigneeId() {
        return assigneeId;
    }

    @JsonIgnore
    public Set<String> getCandidateAssigneeIds() {
        return candidateAssigneeIds;
    }

    @JsonIgnore
    public Set<String> getAssigneeAndCandidateAssigneeIds() {
        Set<String> set = new HashSet<String>();
        if (candidateAssigneeIds != null)
            set.addAll(candidateAssigneeIds);
        set.add(assigneeId);
        return Collections.unmodifiableSet(set);
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public Date getClaimTime() {
        return claimTime;
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

    public boolean isActive() {
        return active;
    }

    @JsonIgnore
    public boolean isDeleted() {
		return isDeleted;
	}

    @JsonIgnore
    public boolean isDisconnected() {
        return disconnected;
    }

    public final static class Builder {

        private String taskInstanceId;
        private String taskDefinitionKey;
        private String processInstanceId;
        private String processInstanceAlias;
        private String processDefinitionKey;
        private String processDefinitionLabel;
        private String processInstanceLabel;
        private String engineProcessInstanceId;
        private String taskLabel;
        private String taskDescription;
        private String taskStatus;
        private String taskAction;
        private User assignee;
        private String assigneeId;
        private List<User> candidateAssignees;
        private Set<String> candidateAssigneeIds;
        private Date startTime;
        private Date endTime;
        private Date claimTime;
        private Date dueDate;
        private int priority;
        private boolean active;
        private boolean isDeleted;
        private boolean disconnected;

        public Builder() {
            super();
            this.candidateAssignees = new ArrayList<User>();
            this.candidateAssigneeIds = new HashSet<String>();
        }

        public Builder(Task task, Sanitizer sanitizer) {
            this.taskInstanceId = sanitizer.sanitize(task.taskInstanceId);
            this.taskDefinitionKey = sanitizer.sanitize(task.taskDefinitionKey);
            this.taskLabel = sanitizer.sanitize(task.taskLabel);
            this.taskDescription = sanitizer.sanitize(task.taskDescription);
            this.taskStatus = sanitizer.sanitize(task.taskStatus);
            this.taskAction = sanitizer.sanitize(task.taskAction);
            this.processDefinitionKey = sanitizer.sanitize(task.processDefinitionKey);
            this.processDefinitionLabel = sanitizer.sanitize(task.processDefinitionLabel);
            this.processInstanceLabel = sanitizer.sanitize(task.processInstanceLabel);
            this.processInstanceId = sanitizer.sanitize(task.processInstanceId);
            this.processInstanceAlias = sanitizer.sanitize(task.processInstanceAlias);
            this.engineProcessInstanceId = sanitizer.sanitize(task.engineProcessInstanceId);
            this.assignee = task.assignee != null ? new User.Builder(task.assignee, sanitizer).build() : null;
            this.assigneeId = task.assigneeId;
            if (task.candidateAssignees != null && !task.candidateAssignees.isEmpty()) {
                this.candidateAssignees = new ArrayList<User>(task.candidateAssignees.size());
                for (User candidateAssignee : task.candidateAssignees) {
                    this.candidateAssignees.add(new User.Builder(candidateAssignee, sanitizer).build());
                }
            } else {
                this.candidateAssignees = new ArrayList<User>();
            }
            this.candidateAssigneeIds = task.candidateAssigneeIds != null ? new HashSet<String>(task.candidateAssigneeIds) : new HashSet<String>();
            this.startTime = task.startTime;
            this.dueDate = task.dueDate;
            this.endTime = task.endTime;
            this.claimTime = task.claimTime;
            this.priority = task.priority;
            this.active = task.active;
            this.isDeleted = task.isDeleted;
            this.disconnected = task.disconnected;
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

        public Builder taskStatus(String taskStatus) {
            this.taskStatus = taskStatus;
            return this;
        }

        public Builder taskAction(String taskAction) {
            this.taskAction = taskAction;
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

        public Builder engineProcessInstanceId(String engineProcessInstanceId) {
            this.engineProcessInstanceId = engineProcessInstanceId;
            return this;
        }

        public Builder processInstanceAlias(String processInstanceAlias) {
            this.processInstanceAlias = processInstanceAlias;
            return this;
        }

        public Builder assignee(User assignee) {
            this.assignee = assignee;
            if (assignee != null)
                this.assigneeId = assignee.getUserId();
            return this;
        }

        public Builder assigneeId(String assigneeId) {
            this.assigneeId = assigneeId;
            return this;
        }

        public Builder candidateAssignee(User candidateAssignee) {
            if (this.candidateAssignees == null)
                this.candidateAssignees = new ArrayList<User>();
            if (candidateAssignee != null) {
                this.candidateAssignees.add(candidateAssignee);
                this.candidateAssigneeIds.add(candidateAssignee.getUserId());
            }
            return this;
        }

        public Builder candidateAssigneeIds(Collection<String> ... collections) {
            if (collections != null) {
                this.candidateAssigneeIds = new HashSet<String>();
                for (Collection<String> collection : collections) {
                    if (collection == null || collection.isEmpty())
                        continue;
                    this.candidateAssigneeIds.addAll(collection);
                }
            } else {
                this.candidateAssigneeIds = new HashSet<String>();
            }
            return this;
        }

        public Builder candidateAssigneeId(String candidateAssigneeId) {
            this.candidateAssigneeIds.add(candidateAssigneeId);
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

        public Builder claimTime(Date claimTime) {
            this.claimTime = claimTime;
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

        public Builder active() {
            this.active = true;
            return this;
        }

        public Builder disconnected() {
            this.disconnected = true;
            return this;
        }

        public Builder finished() {
            this.active = false;
            return this;
        }

        public Builder suspended() {
            this.active = false;
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

        public Builder clearCandidateAssignees() {
            this.candidateAssignees = new ArrayList<User>();
            return this;
        }
    }

    public static class Constants {
        public static final String RESOURCE_LABEL = "Task";
        public static final String ROOT_ELEMENT_NAME = "task";
        public static final String TYPE_NAME = "TaskType";
    }

}
