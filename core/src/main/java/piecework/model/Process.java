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
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import piecework.common.ViewContext;
import piecework.security.Sanitizer;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.*;

/**
 * @author James Renfro
 */
@XmlRootElement(name = Process.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = Process.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "process")
public class Process implements Serializable {

	private static final long serialVersionUID = -4257025274360046038L;

	@XmlAttribute
	@XmlID
	@Id
	private final String processDefinitionKey;

    @XmlElement
    private final String processDefinitionLabel;

    @XmlElement
    private final String processSummary;

    @XmlElement
    private final String participantSummary;

    @XmlElement
    private final String deploymentId;

    @XmlElement
    private final String deploymentLabel;

    @XmlElement
    private final Date deploymentDate;

    @XmlElement
    private final String deploymentVersion;

    @XmlElement
    private final ProcessCodeRepository repository;

    @XmlTransient
    @JsonIgnore
    @DBRef
    private final ProcessDeployment deployment;

    @XmlElementWrapper(name="versions")
    @XmlElement(name="version")
    private final List<ProcessDeploymentVersion> versions;

    @XmlAttribute
    @Transient
    private final String link;

	@XmlAttribute
    @Transient
	private final String uri;

    @XmlAttribute
    private final boolean isAnonymousSubmissionAllowed;

    @XmlAttribute
    private final boolean allowPerInstanceActivities;

    @XmlAttribute
    private final boolean assignmentRestrictedToCandidates;

	@XmlTransient
	@JsonIgnore
	private final boolean isDeleted;

//    @Version
    @XmlTransient
    private final long version;

	private Process() {
		this(new Process.Builder(), new ViewContext());
	}
			
	@SuppressWarnings("unchecked")
	private Process(Process.Builder builder, ViewContext context) {
		this.processDefinitionKey = builder.processDefinitionKey;
        this.processDefinitionLabel = builder.processDefinitionLabel;
        this.deploymentId = builder.deploymentId;
        this.deploymentLabel = builder.deploymentLabel;
        this.deploymentDate = builder.deploymentDate;
        this.deploymentVersion = builder.deploymentVersion;
        this.processSummary = builder.processSummary;
        this.participantSummary = builder.participantSummary;
        this.repository = builder.repository;
		this.deployment = builder.deployment;
        this.versions = Collections.unmodifiableList(builder.versions);
        this.link = context != null ? context.getApplicationUri(Constants.ROOT_ELEMENT_NAME, builder.processDefinitionKey) : null;
		this.uri = context != null ? context.getServiceUri(Constants.ROOT_ELEMENT_NAME, builder.processDefinitionKey) : null;
        this.isAnonymousSubmissionAllowed = builder.isAnonymousSubmissionAllowed;
        this.allowPerInstanceActivities = builder.allowPerInstanceActivities;
        this.assignmentRestrictedToCandidates = builder.assignmentRestrictedToCandidates;
        this.isDeleted = builder.isDeleted;
        this.version = builder.version;
	}
	
	public String getProcessDefinitionKey() {
		return processDefinitionKey;
	}

    public String getProcessDefinitionLabel() {
        return processDefinitionLabel;
    }

    public String getProcessSummary() {
        return processSummary;
    }

    public String getParticipantSummary() {
        return participantSummary;
    }

    public ProcessCodeRepository getRepository() {
        return repository;
    }

    @XmlTransient
    @JsonIgnore
    public ProcessDeployment getDeployment() {
        return deployment;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public String getDeploymentLabel() {
        return deploymentLabel;
    }

    public Date getDeploymentDate() {
        return deploymentDate;
    }

    public String getDeploymentVersion() {
        return deploymentVersion;
    }

    public List<ProcessDeploymentVersion> getVersions() {
        return versions;
    }

    public String getProcessInstanceLabelTemplate() {
        return deployment != null ? deployment.getProcessInstanceLabelTemplate() : null;
    }

    public boolean isAnonymousSubmissionAllowed() {
        return isAnonymousSubmissionAllowed;
    }

    public boolean isAssignmentRestrictedToCandidates() {
        return assignmentRestrictedToCandidates;
    }

    public boolean isAllowPerInstanceActivities() {
        return allowPerInstanceActivities;
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

    @XmlTransient
    @JsonIgnore
    public boolean isEmpty() {
        return StringUtils.isEmpty(processDefinitionLabel) && StringUtils.isEmpty(processSummary) && StringUtils.isEmpty(participantSummary);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Process process = (Process) o;

        if (!processDefinitionKey.equals(process.processDefinitionKey)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return processDefinitionKey.hashCode();
    }

    public final static class Builder {
		
		private String processDefinitionKey;
        private String processDefinitionLabel;
        private String deploymentId;
        private String deploymentLabel;
        private Date deploymentDate;
        private String deploymentVersion;
        private String processSummary;
        private String participantSummary;
        private ProcessCodeRepository repository;
		private ProcessDeployment deployment;
        private List<ProcessDeploymentVersion> versions;
        private boolean isAnonymousSubmissionAllowed;
		private boolean isDeleted;
        private boolean allowPerInstanceActivities;
        private boolean assignmentRestrictedToCandidates;
        private long version;
		
		public Builder() {
			super();
            this.versions = new ArrayList<ProcessDeploymentVersion>();
            this.version = 1;
		}
				
		public Builder(Process process, Sanitizer sanitizer) {
			this.processDefinitionKey = sanitizer.sanitize(process.processDefinitionKey);
            this.processDefinitionLabel = sanitizer.sanitize(process.processDefinitionLabel);
            this.deploymentId = sanitizer.sanitize(process.deploymentId);
            this.deploymentLabel = sanitizer.sanitize(process.deploymentLabel);
            this.deploymentDate = process.deploymentDate;
            this.deploymentVersion = process.deploymentVersion;
            this.processSummary = sanitizer.sanitize(process.processSummary);
            this.participantSummary = sanitizer.sanitize(process.participantSummary);
            this.repository = process.repository != null ? new ProcessCodeRepository.Builder(process.repository, sanitizer).build() : null;
            this.deployment = process.deployment != null ? new ProcessDeployment.Builder(process.deployment, process.processDefinitionKey, sanitizer, true).build() : null;
            if (process.versions == null || process.versions.isEmpty())
                this.versions = new ArrayList<ProcessDeploymentVersion>();
            else
                this.versions = new ArrayList<ProcessDeploymentVersion>(process.versions);
            this.isAnonymousSubmissionAllowed = process.isAnonymousSubmissionAllowed;
            this.isDeleted = process.isDeleted;
            this.allowPerInstanceActivities = process.allowPerInstanceActivities;
            this.assignmentRestrictedToCandidates = process.assignmentRestrictedToCandidates;
            this.version = process.version;
		}

		public Process build() {
			return new Process(this, null);
		}
		
		public Process build(ViewContext context) {
			return new Process(this, context);
		}

		public Builder processDefinitionKey(String processDefinitionKey) {
			this.processDefinitionKey = processDefinitionKey;
			return this;
		}

        public Builder processDefinitionLabel(String processDefinitionLabel) {
            this.processDefinitionLabel = processDefinitionLabel;
            return this;
        }

        public Builder processSummary(String processSummary) {
            this.processSummary = processSummary;
            return this;
        }

        public Builder participantSummary(String participantSummary) {
            this.participantSummary = participantSummary;
            return this;
        }

        public Builder repository(ProcessCodeRepository repository) {
            this.repository = repository;
            return this;
        }

        public Builder deploy(ProcessDeploymentVersion version, ProcessDeployment deployment) {
            this.deploymentId = version.getDeploymentId();
            this.deploymentLabel = version.getLabel();
            this.deploymentVersion = version.getVersion();
            this.deploymentDate = new Date();
            this.deployment = deployment;
            return this;
        }

        public Builder allowAnonymousSubmission(boolean allowAnonymousSubmission) {
            this.isAnonymousSubmissionAllowed = allowAnonymousSubmission;
            return this;
        }

        public Builder assignmentRestrictedToCandidates(boolean assignmentRestrictedToCandidates) {
            this.assignmentRestrictedToCandidates = assignmentRestrictedToCandidates;
            return this;
        }

        public Builder deleteDeployment(String deploymentId) {
            if (this.versions != null) {
                for (int i=0;i<this.versions.size();i++) {
                    ProcessDeploymentVersion version = this.versions.get(i);

                    if (version.getDeploymentId().equals(deploymentId))
                        this.versions.set(i, new ProcessDeploymentVersion(version, true));
                }
            }
            return this;
        }

        public Builder version(ProcessDeploymentVersion version) {
            this.versions.add(version);
            return this;
        }

        public Builder allowPerInstanceActivities() {
            this.allowPerInstanceActivities = true;
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
		public static final String RESOURCE_LABEL = "Process";
		public static final String ROOT_ELEMENT_NAME = "process";
		public static final String TYPE_NAME = "ProcessType";
	}
}
