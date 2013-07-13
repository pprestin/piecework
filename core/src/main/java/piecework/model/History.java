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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author James Renfro
 */
@XmlRootElement(name = History.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = History.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
public class History {

    @XmlElementWrapper(name="tasks")
    @XmlElementRef
    private final List<Task> tasks;

    @XmlElement
    private final User initiator;

    @XmlElement
    private final Date startTime;

    @XmlElement
    private final Date endTime;

    private History() {
        this(new Builder());
    }

    private History(Builder builder) {
        this.tasks = Collections.unmodifiableList(builder.tasks);
        this.initiator = builder.initiator;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public User getInitiator() {
        return initiator;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public final static class Builder {

        private List<Task> tasks;
        private User initiator;
        private Date startTime;
        private Date endTime;

        public Builder() {
            this.tasks = new ArrayList<Task>();
        }

        public History build() {
            return new History(this);
        }

        public Builder task(Task task) {
            if (this.tasks == null)
                this.tasks = new ArrayList<Task>();
            if (task != null)
                this.tasks.add(task);
            return this;
        }

        public Builder initiator(User initiator) {
            this.initiator = initiator;
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
    }


    public static class Constants {
        public static final String RESOURCE_LABEL = "History";
        public static final String ROOT_ELEMENT_NAME = "history";
        public static final String TYPE_NAME = "HistoryType";
    }
}
