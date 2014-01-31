/*
 * Copyright 2013 University of Washington
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
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import piecework.enumeration.FlowElementType;
import piecework.enumeration.State;
import piecework.security.Sanitizer;
import piecework.common.ManyMap;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.*;

/**
 * @author James Renfro
 */
@XmlRootElement(name = ProcessDeployment.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = ProcessDeployment.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "deployment")
public class ProcessDeployment implements Serializable {

    @XmlAttribute
    @XmlID
    @Id
    private final String deploymentId;

    @XmlElement
    private final String deploymentLabel;

    @XmlElement
    private final String deploymentVersion;

    @XmlElement
    private final String processInstanceLabelTemplate;

    @XmlElement
    private final String engine;

    @XmlElement
    private final String engineProcessDefinitionKey;

    @XmlElement
    private final String engineProcessDefinitionResource;

    @XmlElement
    private final String engineProcessDefinitionId;

    @XmlElement
    private final String engineProcessDefinitionLocation;

    @XmlElement
    private final String engineDeploymentId;

    @XmlElement
    private final String startActivityKey;

    @DBRef
    private final Map<String, Activity> activityMap;

    private final List<FlowElement> flowElements;

    private Map<State, String> applicationStatus;

    @XmlAttribute
    private final String base;

    private final String remoteHost;

    @XmlAttribute
    private final boolean deployed;

    @XmlAttribute
    private final boolean published;

    @XmlAttribute
    private final boolean editable;

    @XmlTransient
    @JsonIgnore
    private final boolean isDeleted;

    @XmlElement
    private final Date dateCreated;

    @XmlElement
    private final Date dateDeployed;

    @XmlElement
    private final Date datePublished;

    private final ManyMap<String, Notification> notificationMap;

    private ProcessDeployment() {
        this(new ProcessDeployment.Builder());
    }

    @SuppressWarnings("unchecked")
    private ProcessDeployment(ProcessDeployment.Builder builder) {
        this.deploymentId = builder.deploymentId;
        this.deploymentLabel = builder.deploymentLabel;
        this.deploymentVersion = builder.deploymentVersion;
        this.processInstanceLabelTemplate = builder.processInstanceLabelTemplate;
        this.engine = builder.engine;
        this.engineProcessDefinitionKey = builder.engineProcessDefinitionKey;
        this.engineProcessDefinitionLocation = builder.engineProcessDefinitionLocation;
        this.engineProcessDefinitionId = builder.engineProcessDefinitionId;
        this.engineProcessDefinitionResource = builder.engineProcessDefinitionResource;
        this.engineDeploymentId = builder.engineDeploymentId;
        this.startActivityKey = builder.startActivityKey;
        this.activityMap = Collections.unmodifiableMap(builder.activityMap);
        this.notificationMap = builder.notificationMap;
        this.flowElements = Collections.unmodifiableList(builder.flowElements);
        this.remoteHost = builder.remoteHost;
        this.base = builder.base;
        this.editable = builder.editable;
        this.published = builder.published;
        this.deployed = builder.deployed;
        this.isDeleted = builder.isDeleted;
        this.dateCreated = builder.dateCreated;
        this.dateDeployed = builder.dateDeployed;
        this.datePublished = builder.datePublished;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public String getDeploymentLabel() {
        return deploymentLabel;
    }

    public String getDeploymentVersion() {
        return deploymentVersion;
    }

    public String getProcessInstanceLabelTemplate() {
        return processInstanceLabelTemplate;
    }

    public String getEngine() {
        return engine;
    }

    public String getBase() {
        return base;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public String getEngineProcessDefinitionKey() {
        return engineProcessDefinitionKey;
    }

    public String getEngineProcessDefinitionId() {
        return engineProcessDefinitionId;
    }

    public String getEngineProcessDefinitionLocation() {
        return engineProcessDefinitionLocation;
    }

    public String getEngineDeploymentId() {
        return engineDeploymentId;
    }

    public String getEngineProcessDefinitionResource() {
        return engineProcessDefinitionResource;
    }

    public String getStartActivityKey() {
        return startActivityKey;
    }

    @JsonIgnore
    public Activity getStartActivity() {
        return startActivityKey != null ? getActivity(startActivityKey) : null;
    }

    @JsonIgnore
    public Activity getActivity(String activityKey) {
        if (activityMap != null && activityKey != null)
            return activityMap.get(activityKey);
        return null;
    }

    public Map<String, Activity> getActivityMap() {
        return activityMap;
    }

    public ManyMap<String, Notification> getNotificationMap() {
        return notificationMap;
    }

    public Map<State, String> getApplicationStatus() {
        return applicationStatus;
    }

    @JsonIgnore
    public String getCancellationStatus() {
        return applicationStatus != null ? applicationStatus.get(State.CANCELLATION) : null;
    }

    @JsonIgnore
    public String getSuspensionStatus() {
        return applicationStatus != null ? applicationStatus.get(State.SUSPENSION) : null;
    }

    @JsonIgnore
    public String getInitiationStatus() {
        return applicationStatus != null ? applicationStatus.get(State.INITIATION) : null;
    }

    @JsonIgnore
    public String getCompletionStatus() {
        return applicationStatus != null ? applicationStatus.get(State.COMPLETION) : null;
    }

    public List<FlowElement> getFlowElements() {
        return flowElements;
    }

    public boolean isPublished() {
        return published;
    }

    @JsonIgnore
    public boolean isDeleted() {
        return isDeleted;
    }

    public boolean isDeployed() {
        return deployed;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public Date getDateDeployed() {
        return dateDeployed;
    }

    public Date getDatePublished() {
        return datePublished;
    }

    public boolean isEditable() {
        return editable;
    }

    @JsonIgnore
    public Collection<Notification> getNotifications(String notificationKey) {
        if (notificationMap != null && notificationKey != null)
            return (Collection<Notification>) notificationMap.get(notificationKey);
        return null;
    }

    public final static class Builder {

        private String deploymentId;
        private String deploymentLabel;
        private String deploymentVersion;
        private String processInstanceLabelTemplate;
        private String engine;
        private String engineProcessDefinitionKey;
        private String engineProcessDefinitionId;
        private String engineProcessDefinitionLocation;
        private String engineProcessDefinitionResource;
        private String engineDeploymentId;
        private String base;
        private String remoteHost;
        private String startActivityKey;
        private Map<String, Activity> activityMap;
        private List<FlowElement> flowElements;
        private Map<State, String> applicationStatus;
        private boolean isDeleted;
        private boolean deployed;
        private boolean published;
        private boolean editable;
        private Date dateCreated;
        private Date dateDeployed;
        private Date datePublished;
        private ManyMap<String, Notification> notificationMap;

        public Builder() {
            super();
            this.activityMap = new HashMap<String, Activity>();
            this.notificationMap = new ManyMap<String, Notification>();
            this.flowElements = new ArrayList<FlowElement>();
            this.applicationStatus = new HashMap<State, String>();
            this.dateCreated = new Date();
            this.deployed = false;
            this.published = false;
            this.editable = true;
        }

        public Builder(ProcessDeployment deployment, Sanitizer sanitizer, boolean includeDetails) {
            this.deploymentId = deployment.deploymentId;
            this.deploymentLabel = deployment.deploymentLabel;
            this.deploymentVersion = deployment.deploymentVersion;
            this.processInstanceLabelTemplate = sanitizer.sanitize(deployment.processInstanceLabelTemplate);
            this.engine = sanitizer.sanitize(deployment.engine);
            this.engineProcessDefinitionKey = sanitizer.sanitize(deployment.engineProcessDefinitionKey);
            this.engineProcessDefinitionId = sanitizer.sanitize(deployment.engineProcessDefinitionId);
            this.engineProcessDefinitionLocation = sanitizer.sanitize(deployment.engineProcessDefinitionLocation);
            this.engineProcessDefinitionResource = sanitizer.sanitize(deployment.engineProcessDefinitionResource);
            this.engineDeploymentId = sanitizer.sanitize(deployment.engineDeploymentId);
            this.base = sanitizer.sanitize(deployment.base);
            this.remoteHost = sanitizer.sanitize(deployment.remoteHost);
            this.applicationStatus = new HashMap<State, String>();
            this.startActivityKey = sanitizer.sanitize(deployment.startActivityKey);
            this.activityMap = new HashMap<String, Activity>();
            if (includeDetails && deployment.getActivityMap() != null) {
                for (Map.Entry<String, Activity> entry : deployment.getActivityMap().entrySet()) {
                    String key = sanitizer.sanitize(entry.getKey());
                    if (key == null)
                        continue;
                    if (entry.getValue() == null)
                        continue;

                    this.activityMap.put(key, new Activity.Builder(entry.getValue(), sanitizer).build());
                }
            }
            this.flowElements = new ArrayList<FlowElement>();
            if (deployment.getFlowElements() != null) {
                for (FlowElement flowElement : deployment.getFlowElements()) {
                    String taskDefinitionKey = sanitizer.sanitize(flowElement.getId());
                    String label = sanitizer.sanitize(flowElement.getLabel());
                    FlowElementType type = flowElement.getType();
                    this.flowElements.add(new FlowElement(taskDefinitionKey, label, type));
                }
            }
            if (deployment.getApplicationStatus() != null) {
                for (Map.Entry<State, String> entry : deployment.getApplicationStatus().entrySet()) {
                    State key = entry.getKey();
                    if (key == null)
                        continue;
                    String value = sanitizer.sanitize(entry.getValue());
                    if (value == null)
                        continue;
                    this.applicationStatus.put(key, value);
                }
            }

            this.notificationMap = new ManyMap<String, Notification>();
            if ( deployment.getNotificationMap() != null && deployment.getNotificationMap().size() > 0 ) {
                this.notificationMap.putAll(deployment.getNotificationMap());
            }

            this.isDeleted = deployment.isDeleted;
            this.published = deployment.published;
            this.editable = deployment.editable;
            this.dateCreated = deployment.dateCreated;
            this.dateDeployed = deployment.dateDeployed;
            this.deployed = deployment.deployed;
        }

        public ProcessDeployment build() {
            return new ProcessDeployment(this);
        }

        public Builder deploymentId(String deploymentId) {
            this.deploymentId = deploymentId;
            return this;
        }

        public Builder deploymentLabel(String deploymentLabel) {
            this.deploymentLabel = deploymentLabel;
            return this;
        }

        public Builder deploymentVersion(String deploymentVersion) {
            this.deploymentVersion = deploymentVersion;
            return this;
        }

        public Builder processInstanceLabelTemplate(String processInstanceLabelTemplate) {
            this.processInstanceLabelTemplate = processInstanceLabelTemplate;
            return this;
        }

        public Builder engine(String engine) {
            this.engine = engine;
            return this;
        }

        public Builder engineProcessDefinitionKey(String engineProcessDefinitionKey) {
            this.engineProcessDefinitionKey = engineProcessDefinitionKey;
            return this;
        }

        public Builder engineProcessDefinitionId(String engineProcessDefinitionId) {
            this.engineProcessDefinitionId = engineProcessDefinitionId;
            return this;
        }

        public Builder engineProcessDefinitionLocation(String engineProcessDefinitionLocation) {
            this.engineProcessDefinitionLocation = engineProcessDefinitionLocation;
            return this;
        }

        public Builder engineProcessDefinitionResource(String engineProcessDefinitionResource) {
            this.engineProcessDefinitionResource = engineProcessDefinitionResource;
            return this;
        }

        public Builder engineDeploymentId(String engineDeploymentId) {
            this.engineDeploymentId = engineDeploymentId;
            return this;
        }

        public Builder startActivityKey(String startActivityKey) {
            this.startActivityKey = startActivityKey;
            return this;
        }

        public Builder activity(String activityKey, Activity activity) {
            this.activityMap.put(activityKey, activity);
            return this;
        }

        public Builder notification(String notificationKey, Notification notification) {
            this.notificationMap.putOne(notificationKey, notification);
            return this;
        }

        public Builder notifications(String notificationKey, List<Notification> notifications) {
            this.notificationMap.put(notificationKey, notifications);
            return this;
        }

        public Builder base(String base) {
            this.base = base;
            return this;
        }

        public Builder remoteHost(String remoteHost) {
            this.remoteHost = remoteHost;
            return this;
        }

        public Builder flowElement(String taskDefinitionKey, String label, FlowElementType type) {
            FlowElement flowElement = new FlowElement(taskDefinitionKey, label, type);
            if (!this.flowElements.contains(flowElement))
                this.flowElements.add(flowElement);
            return this;
        }

        public Builder publish() {
            this.published = true;
            this.editable = false;
            this.datePublished = new Date();
            return this;
        }

        public Builder published(boolean published) {
            this.published = published;
            this.editable = !published;
            this.datePublished = null;
            return this;
        }

        public Builder deploy() {
            this.deployed = true;
            this.dateDeployed = new Date();
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

        public Builder clearActivities() {
            this.activityMap = new HashMap<String, Activity>();
            return this;
        }

        public Builder clearNotifications() {
            this.notificationMap = new ManyMap<String, Notification>();
            return this;
        }

        public Builder deleteActivity(String activityKey) {
            this.activityMap.remove(activityKey);
            return this;
        }

        public Builder deleteContainer(String activityKey, String containerId) {
            Activity activity = this.activityMap.get(activityKey);
            if (activity != null) {
                activity = activity.withoutContainer(containerId);
                if (activity != null)
                    this.activityMap.put(activityKey, activity);
            }
            return this;
        }

    }

    public static class Constants {
        public static final String RESOURCE_LABEL = "Process Deployment";
        public static final String ROOT_ELEMENT_NAME = "deployment";
        public static final String TYPE_NAME = "ProcessDeploymentType";
    }

}
