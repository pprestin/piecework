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

import javax.xml.bind.annotation.*;
import java.util.Date;

/**
 * @author James Renfro
 */
@XmlRootElement(name = ProcessDeploymentVersion.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = ProcessDeploymentVersion.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessDeploymentVersion {

    @XmlElement
    private final String label;

    @XmlElement
    private final String deploymentId;

    @XmlAttribute
    private final String version;

    @XmlElement
    private final Date date;

    @XmlTransient
    private final boolean deleted;

    private ProcessDeploymentVersion() {
        this(null, null, null, new Date(), false);
    }

    public ProcessDeploymentVersion(String label, String deploymentId, String version) {
        this(label, deploymentId, version, new Date(), false);
    }

    public ProcessDeploymentVersion(String label, String deploymentId, String version, Date date, boolean deleted) {
        this.label = label;
        this.deploymentId = deploymentId;
        this.version = version;
        this.date = date;
        this.deleted = deleted;
    }

    public ProcessDeploymentVersion(ProcessDeploymentVersion original, boolean deleted) {
        this(original.getLabel(), original.getDeploymentId(), original.getVersion(), original.getDate(), deleted);
    }

    public String getLabel() {
        return label;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public String getVersion() {
        return version;
    }

    public Date getDate() {
        return date;
    }

    @JsonIgnore
    public boolean isDeleted() {
        return deleted;
    }

    public static class Constants {
        public static final String RESOURCE_LABEL = "Process Detail Version";
        public static final String ROOT_ELEMENT_NAME = "processDetailVersion";
        public static final String TYPE_NAME = "ProcessDetailVersionType";
    }

}
