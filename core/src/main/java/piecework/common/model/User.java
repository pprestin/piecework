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
package piecework.common.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import piecework.Sanitizer;
import piecework.common.view.ViewContext;
import piecework.model.ProcessInstance;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
@XmlRootElement(name = User.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = User.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = User.Constants.ROOT_ELEMENT_NAME)
public class User implements Serializable {

    @XmlAttribute
    @XmlID
    @Id
    private final String userId;

    @XmlAttribute
    private final String visibleId;

    @XmlElement
    private final String displayName;

    @XmlElement
    private final String emailAddress;

    @XmlElement
    private final String phoneNumber;

    @XmlElement
    private final String uri;

    private User() {
        this(new User.Builder(), new ViewContext());
    }

    @SuppressWarnings("unchecked")
    private User(User.Builder builder, ViewContext context) {
        this.userId = builder.userId;
        this.visibleId = builder.visibleId;
        this.displayName = builder.displayName;
        this.emailAddress = builder.emailAddress;
        this.phoneNumber = builder.phoneNumber;
        this.uri = context != null ? context.getApplicationUri(builder.userId) : null;
    }

    public final static class Builder {

        private String userId;
        private String visibleId;
        private String displayName;
        private String emailAddress;
        private String phoneNumber;

        public Builder() {
            super();
        }

        public Builder(User task, Sanitizer sanitizer) {
            this.userId = sanitizer.sanitize(task.userId);
            this.visibleId = sanitizer.sanitize(task.visibleId);
            this.displayName = sanitizer.sanitize(task.displayName);
            this.emailAddress = sanitizer.sanitize(task.emailAddress);
            this.phoneNumber = sanitizer.sanitize(task.phoneNumber);
        }

        public User build() {
            return new User(this, null);
        }

        public User build(ViewContext context) {
            return new User(this, context);
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder visibleId(String visibleId) {
            this.visibleId = visibleId;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder emailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }
    }

    public static class Constants {
        public static final String RESOURCE_LABEL = "User";
        public static final String ROOT_ELEMENT_NAME = "user";
        public static final String TYPE_NAME = "UserType";
    }

}
