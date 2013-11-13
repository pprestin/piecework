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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.data.annotation.Id;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import piecework.authorization.AccessAuthority;
import piecework.identity.IdentityDetails;
import piecework.security.Sanitizer;
import piecework.common.ViewContext;
import piecework.util.ManyMap;

/**
 * @author James Renfro
 */
@XmlRootElement(name = User.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = User.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
public class User extends Value {

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

    @XmlTransient
    private final ManyMap<String, String> attributes;

    @XmlTransient
    private final AccessAuthority accessAuthority;

    private User() {
        this(new User.Builder(), new ViewContext());
    }

    private User(User.Builder builder, ViewContext context) {
        this.userId = builder.userId;
        this.visibleId = builder.visibleId;
        this.displayName = builder.displayName;
        this.emailAddress = builder.emailAddress;
        this.phoneNumber = builder.phoneNumber;
        this.attributes = builder.attributes;
        this.accessAuthority = builder.accessAuthority;
        this.uri = context != null ? context.getApplicationUri(Constants.ROOT_ELEMENT_NAME, builder.userId) : null;
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

    @JsonIgnore
    public ManyMap<String, String> getAttributes() {
        return attributes;
    }

    @JsonIgnore
    public AccessAuthority getAccessAuthority() {
        return accessAuthority;
    }

    @JsonValue(value=false)
    @JsonIgnore
    public String getValue() {
        return null;
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
        private ManyMap<String, String> attributes;
        private AccessAuthority accessAuthority;

        public Builder() {
            super();
        }

        public Builder(User user) {
            this.userId = user.userId;
            this.visibleId = user.visibleId;
            this.displayName = user.displayName;
            this.emailAddress = user.emailAddress;
            this.phoneNumber = user.phoneNumber;
            this.attributes = new ManyMap<String, String>();
            if (user.attributes != null) {
                for (Map.Entry<String, List<String>> entry : user.attributes.entrySet()) {
                    String key = entry.getKey();
                    List<String> values = entry.getValue();
                    if (key == null || values == null)
                        continue;
                    for (String value : values) {
                        this.attributes.putOne(key, value);
                    }
                }
            }
        }

        public Builder(User user, Sanitizer sanitizer) {
            this.userId = sanitizer.sanitize(user.userId);
            this.visibleId = sanitizer.sanitize(user.visibleId);
            this.displayName = sanitizer.sanitize(user.displayName);
            this.emailAddress = sanitizer.sanitize(user.emailAddress);
            this.phoneNumber = sanitizer.sanitize(user.phoneNumber);
            this.attributes = new ManyMap<String, String>();
            if (user.attributes != null) {
                for (Map.Entry<String, List<String>> entry : user.attributes.entrySet()) {
                    String key = sanitizer.sanitize(entry.getKey());
                    List<String> values = entry.getValue();
                    if (key == null || values == null)
                        continue;
                    for (String value : values) {
                        this.attributes.putOne(key, sanitizer.sanitize(value));
                    }
                }
            }
        }

        public Builder(UserDetails details) {
            if (details instanceof IdentityDetails) {
                IdentityDetails identityDetails = IdentityDetails.class.cast(details);
                this.userId = identityDetails.getInternalId();
                this.visibleId = identityDetails.getExternalId();
                this.displayName = identityDetails.getDisplayName();
                this.emailAddress = identityDetails.getEmailAddress();
            } else {
                this.userId = details.getUsername();
                this.displayName = details.getUsername();
            }
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

        public Builder attribute(String name, String value) {
            if (this.attributes == null)
                this.attributes = new ManyMap<String, String>();
            if (name != null && value != null)
                this.attributes.putOne(name, value);
            return this;
        }

        public Builder accessAuthority(AccessAuthority accessAuthority) {
            this.accessAuthority = accessAuthority;
            return this;
        }
    }

    public static class Constants {
        public static final String RESOURCE_LABEL = "User";
        public static final String ROOT_ELEMENT_NAME = "user";
        public static final String TYPE_NAME = "UserType";
    }

}
