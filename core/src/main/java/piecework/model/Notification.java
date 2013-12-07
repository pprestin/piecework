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
import piecework.security.Sanitizer;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.*;

/**
 * @author James Renfro
 */
@XmlRootElement(name = Notification.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = Notification.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "notification")
public class Notification implements Serializable {

    @XmlAttribute
    @XmlID
    @Id
    private final String notificationId;

    @XmlElement
    private final String label;

    @XmlElement
    private final String subject;

    @XmlElement
    private final String text;

    @XmlElement
    private final String type;

    @XmlElementWrapper(name="taskDefinitionKeys")
    private final Set<String> taskDefinitionKeys;

    @XmlElementWrapper(name="taskEvents")
    private final Set<String> taskEvents;

    @XmlElement
    private final Set<String> candidateRoles;

    @XmlAttribute
    private final int ordinal;

    @XmlTransient
    @JsonIgnore
    private final boolean isDeleted;

    private final Map<String, String> dataMap;

    private Notification() {
        this(new Builder());
    }

    public Notification(Builder builder) {
        this.notificationId = builder.notificationId;
        this.label = builder.label;
        this.subject = builder.subject;
        this.text = builder.text;
        this.type = builder.type;
        this.taskDefinitionKeys = (Set<String>) (builder.taskDefinitionKeys != null ? Collections.unmodifiableSet(builder.taskDefinitionKeys) : Collections.emptySet());
        this.taskEvents = (Set<String>) (builder.taskEvents != null ? Collections.unmodifiableSet(builder.taskEvents) : Collections.emptySet());
        this.candidateRoles = (Set<String>) (builder.candidateRoles != null ? Collections.unmodifiableSet(builder.candidateRoles) : Collections.emptySet());
        this.ordinal = builder.ordinal;
        this.isDeleted = builder.isDeleted;
        this.dataMap = (Map<String, String>) ( builder.dataMap == null ? Collections.emptyMap() : Collections.unmodifiableMap(builder.dataMap) );
    }

    public String getNotificationId() {
        return notificationId;
    }

    public String getLabel() {
        return label;
    }

    public String getSubject() {
        String subj = get("subject"); // get subject from dataMap
        return subj == null || subj.isEmpty() ? subject : subj;
    }

    public String getText() {
        String txt = get("text,body"); // get text from dataMap
        return txt == null || txt.isEmpty() ? text : txt;
    }

    public String getType() {
        return type;
    }

    public Set<String> getCandidateRoles() {
        return candidateRoles;
    }

    public Set<String> getTaskDefinitionKeys() {
        return taskDefinitionKeys;
    }

    public Set<String> getTaskEvents() {
        return taskEvents;
    }

    public int getOrdinal() {
        return ordinal;
    }

    /**
     * returns the first non-empty value found for a list of keys separated with comma.
     * @param  key  a comma-separated list of keys
     * @return      the first non-empty value of the given keys,
     *              or null if none of the keys exist in the notifications
     */ 
    public String get(String keys) {
        if ( dataMap == null || dataMap.isEmpty() || keys == null || keys.isEmpty() ) {
            return null;
        }

        String[] ks = keys.split(",");
        for ( String k: ks ) {
            String v = dataMap.get(k);
            if ( v != null && !v.isEmpty() ) {
                return v;
            }
        }

        return null;
    }

    @JsonIgnore
    public boolean isDeleted() {
        return isDeleted;
    }

    public Map<String, String> getDataMap() {
        return dataMap;
    }  

    public String getSenderEmail() {
        return get("senderEmail,senderEmailAddress,sender,from");
    }

    public String getSenderName() {
        return get("senderName,senderLabel");
    }

    public String getRecipients() {
        return get("recipients,recipient,to,assignee");
    }

    public String getBcc() {
        return get("bcc,cc");
    }

    public final static class Builder {

        private String notificationId;
        private String label;
        private String subject;
        private String text;
        private String type;
        private Set<String> candidateRoles;
        private Set<String> taskDefinitionKeys;
        private Set<String> taskEvents;
        private int ordinal;
        private boolean isDeleted;
        private Map<String, String> dataMap;

        public Builder() {
            super();
            this.notificationId = UUID.randomUUID().toString();
            this.dataMap = new HashMap<String, String>();
        }

        public Builder(Notification notification, Sanitizer sanitizer) {
            this.notificationId = notification.notificationId != null ? sanitizer.sanitize(notification.notificationId) : UUID.randomUUID().toString();;
            this.subject = sanitizer.sanitize(notification.subject);
            this.text = sanitizer.sanitize(notification.text);
            this.type = sanitizer.sanitize(notification.type);

            if (notification.candidateRoles != null && !notification.candidateRoles.isEmpty()) {
                this.candidateRoles = new HashSet<String>(notification.candidateRoles.size());
                for (String candidateRole : notification.candidateRoles) {
                    this.candidateRoles.add(sanitizer.sanitize(candidateRole));
                }
            } else {
                this.candidateRoles = new HashSet<String>();
            }

            if (notification.taskDefinitionKeys != null && !notification.taskDefinitionKeys.isEmpty()) {
                this.taskDefinitionKeys = new HashSet<String>(notification.taskDefinitionKeys.size());
                for (String taskDefinitionKey : notification.taskDefinitionKeys) {
                    this.taskDefinitionKeys.add(sanitizer.sanitize(taskDefinitionKey));
                }
            } else {
                this.taskDefinitionKeys = new HashSet<String>();
            }

            if (notification.taskEvents != null && !notification.taskEvents.isEmpty()) {
                this.taskEvents = new HashSet<String>(notification.taskEvents.size());
                for (String taskEvent : notification.taskEvents) {
                    this.taskEvents.add(sanitizer.sanitize(taskEvent));
                }
            } else {
                this.taskEvents = new HashSet<String>();
            }

            if (notification.dataMap != null && !notification.dataMap.isEmpty()) {
                this.dataMap = new HashMap<String, String>(notification.dataMap);
            } else {
                this.dataMap = new HashMap<String, String>();
            }
        }

        public Notification build() {
            return new Notification(this);
        }

        public Builder notificationId(String notificationId) {
            this.notificationId = notificationId;
            return this;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder candidateRole(String candidateRole) {
            if (this.candidateRoles == null)
                this.candidateRoles = new HashSet<String>();
            this.candidateRoles.add(candidateRole);
            return this;
        }

        public Builder taskDefinitionKey(String taskDefinitionKey) {
            if (this.taskDefinitionKeys == null)
                this.taskDefinitionKeys = new HashSet<String>();
            this.taskDefinitionKeys.add(taskDefinitionKey);
            return this;
        }

        public Builder taskEvent(String taskEvent) {
            if (this.taskEvents == null)
                this.taskEvents = new HashSet<String>();
            this.taskEvents.add(taskEvent);
            return this;
        }

        public Builder ordinal(int ordinal) {
            this.ordinal = ordinal;
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

        public Builder put(String key, String value) {
            if ( key != null && ! key.isEmpty() ) {
                dataMap.put(key, value);
            }
            return this;
        }
    }

    public static class Constants {
        public static final String RESOURCE_LABEL = "Notification";
        public static final String ROOT_ELEMENT_NAME = "notification";
        public static final String TYPE_NAME = "NotificationType";
        public static final String SENDER_NAME = "senderName";
        public static final String SENDER_EMAIL = "senderEmail";
        public static final String RECIPIENTS = "recipients";
        public static final String SUBJECT = "subject";
        public static final String TEXT = "text";
        public static final String EVENT = "event";
        public static final String BCC = "bcc";
        public static final String COMMON = "common";	// notificaiton common for all tasks in a workflow
    }
}
