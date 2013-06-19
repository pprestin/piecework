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
import org.springframework.data.mongodb.core.mapping.Document;

import javax.xml.bind.annotation.*;
import java.util.Date;
import java.util.Map;

/**
 * Details of a process execution that are stored by the engine and may need to be used
 * to decorate a process instance with a start or end date, for example.
 *
 * @author James Renfro
 */
@XmlRootElement(name = ProcessExecution.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = ProcessExecution.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = ProcessExecution.Constants.ROOT_ELEMENT_NAME)
public class ProcessExecution {

    @XmlAttribute
    @XmlID
    @Id
    private final String executionId;

    @XmlTransient
    @JsonIgnore
    private final String businessKey;

    @XmlElement
    private final Date startTime;

    @XmlElement
    private final Date endTime;

    @XmlAttribute
    private final long duration;

    @XmlTransient
    @JsonIgnore
    private final String initiatorId;

    @XmlTransient
    @JsonIgnore
    private final String deleteReason;

    @XmlTransient
    @JsonIgnore
    private final Map<String, ?> data;

    private ProcessExecution() {
        this(new Builder());
    }

    private ProcessExecution(Builder builder) {
        this.businessKey = builder.businessKey;
        this.executionId = builder.executionId;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.duration = builder.duration;
        this.initiatorId = builder.initiatorId;
        this.data = builder.data;
        this.deleteReason = builder.deleteReason;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public String getExecutionId() {
        return executionId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public long getDuration() {
        return duration;
    }

    public String getInitiatorId() {
        return initiatorId;
    }

    public String getDeleteReason() {
        return deleteReason;
    }

    public Map<String, ?> getData() {
        return data;
    }

    public final static class Builder {

        private String businessKey;
        private String executionId;
        private Date startTime;
        private Date endTime;
        private long duration;
        private String initiatorId;
        private Map<String, ?> data;
        private String deleteReason;

        public Builder() {

        }

        public ProcessExecution build() {
            return new ProcessExecution(this);
        }

        public Builder businessKey(String businessKey) {
            this.businessKey = businessKey;
            return this;
        }

        public Builder executionId(String executionId) {
            this.executionId = executionId;
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

        public Builder duration(long duration) {
            this.duration = duration;
            return this;
        }

        public Builder initiatorId(String initiatorId) {
            this.initiatorId = initiatorId;
            return this;
        }

        public Builder data(Map<String, ?> data) {
            this.data = data;
            return this;
        }

        public Builder deleteReason(String deleteReason) {
            this.deleteReason = deleteReason;
            return this;
        }

    }

    public static class Constants {
        public static final String RESOURCE_LABEL = "Execution";
        public static final String ROOT_ELEMENT_NAME = "execution";
        public static final String TYPE_NAME = "ExecutionType";
    }
}
