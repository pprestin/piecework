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
import org.springframework.data.annotation.Version;
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

    @XmlTransient
    @JsonIgnore
    @DBRef
    private final ProcessDeployment current;

    @XmlElementWrapper(name="versions")
    @XmlElement(name="version")
    private final List<ProcessDeploymentVersion> versions;

    @XmlAttribute
    @Transient
    private final String link;

	@XmlAttribute
    @Transient
	private final String uri;

	@XmlTransient
	@JsonIgnore
	private final boolean isDeleted;

    @Version
    private final long version;

	private Process() {
		this(new Process.Builder(), new ViewContext());
	}
			
	@SuppressWarnings("unchecked")
	private Process(Process.Builder builder, ViewContext context) {
		this.processDefinitionKey = builder.processDefinitionKey;
        this.processDefinitionLabel = builder.processDefinitionLabel;
        this.processSummary = builder.processSummary;
        this.participantSummary = builder.participantSummary;
		this.current = builder.current;
        this.versions = Collections.unmodifiableList(builder.versions);
        this.link = context != null ? context.getApplicationUri(Constants.ROOT_ELEMENT_NAME, builder.processDefinitionKey) : null;
		this.uri = context != null ? context.getServiceUri(Constants.ROOT_ELEMENT_NAME, builder.processDefinitionKey) : null;
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

    @XmlTransient
    @JsonIgnore
    public ProcessDeployment getDeployment() {
        return current;
    }

    public List<ProcessDeploymentVersion> getVersions() {
        return versions;
    }

    public String getProcessInstanceLabelTemplate() {
        return current != null ? current.getProcessInstanceLabelTemplate() : null;
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
        private String processSummary;
        private String participantSummary;
		private ProcessDeployment current;
        private List<ProcessDeploymentVersion> versions;
		private boolean isDeleted;
        private long version;
		
		public Builder() {
			super();
            this.versions = new ArrayList<ProcessDeploymentVersion>();
            this.version = 1;
		}
				
		public Builder(piecework.model.Process process, Sanitizer sanitizer, boolean includeDetails) {
			this.processDefinitionKey = sanitizer.sanitize(process.processDefinitionKey);
            this.processDefinitionLabel = sanitizer.sanitize(process.processDefinitionLabel);
            this.processSummary = sanitizer.sanitize(process.processSummary);
            this.participantSummary = sanitizer.sanitize(process.participantSummary);
            this.current = new ProcessDeployment.Builder(process.current, process.processDefinitionKey, sanitizer, includeDetails).build();
            if (process.versions.isEmpty())
                this.versions = new ArrayList<ProcessDeploymentVersion>();
            else
                this.versions = new ArrayList<ProcessDeploymentVersion>(process.versions);
            this.isDeleted = process.isDeleted;
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

        public Builder deploy(ProcessDeployment current) {
            this.current = current;
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
