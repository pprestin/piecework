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

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;

import piecework.Sanitizer;
import piecework.common.view.ViewContext;

/**
 * @author James Renfro
 */
@XmlRootElement(name = User.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = User.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements Serializable {

    private static final long serialVersionUID = -4312076944171057691L;

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

    private User(User.Builder builder, ViewContext context) {
        this.userId = builder.userId;
        this.visibleId = builder.visibleId;
        this.displayName = builder.displayName;
        this.emailAddress = builder.emailAddress;
        this.phoneNumber = builder.phoneNumber;
        this.uri = context != null ? context.getApplicationUri(builder.userId) : null;
    }

    public String getUserId() {
		return userId;
	}

	public String getVisibleId() {
		return visibleId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getUri() {
		return uri;
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

        public Builder(User user, Sanitizer sanitizer) {
            this.userId = sanitizer.sanitize(user.userId);
            this.visibleId = sanitizer.sanitize(user.visibleId);
            this.displayName = sanitizer.sanitize(user.displayName);
            this.emailAddress = sanitizer.sanitize(user.emailAddress);
            this.phoneNumber = sanitizer.sanitize(user.phoneNumber);
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
