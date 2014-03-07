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
import org.apache.commons.lang.StringUtils;
import piecework.enumeration.EventType;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * @author James Renfro
 */
@XmlRootElement(name = Event.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = Event.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Event implements Serializable, Comparable<Event> {

    @XmlID
    @XmlAttribute
    private final String id;

    @XmlElement
    private final EventType type;

    @XmlElement
    private final String description;

    @XmlElement
    private final Operation operation;

    @XmlElement
    private final Task task;

    @XmlElement
    private final Date date;

    @XmlElement
    private final User user;

    private Event() {
        this(new Builder());
    }

    private Event(Builder builder) {
        this.id = StringUtils.isNotEmpty(builder.id) ? builder.id : UUID.randomUUID().toString();
        this.type = builder.type;
        this.description = builder.description;
        this.operation = builder.operation;
        this.task = builder.task;
        this.date = builder.date;
        this.user = builder.user;
    }

    public EventType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public Operation getOperation() {
        return operation;
    }

    public Task getTask() {
        return task;
    }

    public Date getDate() {
        return date;
    }

    public User getUser() {
        return user;
    }

    public boolean equals(Object other) {
        if (other instanceof Event) {
            Event otherEvent = Event.class.cast(other);
            return id.equals(otherEvent.id);
        }
        return false;
    }

    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int compareTo(Event other) {
        if (date == null)
            return 1;
        if (other.date == null)
            return -1;

        int result = date.compareTo(other.date);
        if (result == 0)
            return id.compareTo(other.id);

        return result;
    }

    public final static class Builder {

        private String id;
        private EventType type;
        private String description;
        private Operation operation;
        private Task task;
        private Date date;
        private User user;

        public Builder() {

        }

        public Event build() {
            return new Event(this);
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder type(EventType type) {
            this.type = type;
            return this;
        }

        public Builder operation(Operation operation) {
            this.description = operation.getType().description();
            this.operation = operation;
            return this;
        }

        public Builder task(Task task) {
            this.description = "User task";
            this.task = task;
            return this;
        }

        public Builder date(Date date) {
            this.date = date;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }
    }

    public static class Constants {
        public static final String RESOURCE_LABEL = "Event";
        public static final String ROOT_ELEMENT_NAME = "event";
        public static final String TYPE_NAME = "EventType";
    }

}
