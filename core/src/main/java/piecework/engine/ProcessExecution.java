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
package piecework.engine;

import java.util.Date;

/**
 * Details of a process execution that are stored by the engine and may need to be used
 * to decorate a process instance with a start or end date, for example.
 *
 * @author James Renfro
 */
public class ProcessExecution {

    private final String businessKey;
    private final String executionId;
    private final Date startTime;
    private final Date endTime;
    private final long duration;
    private final String initiatorId;
    private final String deleteReason;

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

    public final static class Builder {

        private String businessKey;
        private String executionId;
        private Date startTime;
        private Date endTime;
        private long duration;
        private String initiatorId;
        private String deleteReason;

        public Builder() {
            super();
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

        public Builder deleteReason(String deleteReason) {
            this.deleteReason = deleteReason;
            return this;
        }

    }

}
