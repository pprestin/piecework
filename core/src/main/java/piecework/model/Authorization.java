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
import piecework.authorization.ResourceAuthority;
import piecework.common.view.ViewContext;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author James Renfro
 */
@XmlRootElement(name = Authorization.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = Authorization.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = Authorization.Constants.ROOT_ELEMENT_NAME)
public class Authorization {

    private static final long serialVersionUID = -8642937056889667692L;

    @XmlAttribute
    @XmlID
    @Id
    private final String authorizationId;

    @XmlElement
    private final String groupId;

    @XmlElement
    private final String groupDisplayName;

    @XmlElement
    private final String groupNamespace;

    @XmlAttribute
    private final List<ResourceAuthority> authorities;

    @XmlTransient
    @JsonIgnore
    private final boolean isDeleted;

    @XmlAttribute
    private final String link;

    @XmlAttribute
    private final String uri;

    private Authorization() {
        this(new Authorization.Builder(), new ViewContext());
    }

    private Authorization(Authorization.Builder builder, ViewContext context) {
        this.authorizationId = builder.authorizationId;
        this.groupId = builder.groupId;
        this.groupDisplayName = builder.groupDisplayName;
        this.groupNamespace = builder.groupNamespace;
        this.isDeleted = builder.isDeleted;
        this.authorities = builder.authorities != null ? Collections.unmodifiableList(builder.authorities) : null;
        this.link = context != null ? context.getApplicationUri(builder.groupNamespace, builder.groupId) : null;
        this.uri = context != null ? context.getServiceUri(builder.groupNamespace, builder.groupId) : null;
    }

    public String getAuthorizationId() {
        return authorizationId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getGroupDisplayName() {
        return groupDisplayName;
    }

    public String getGroupNamespace() {
        return groupNamespace;
    }

    public List<ResourceAuthority> getAuthorities() {
        return authorities;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public String getUri() {
        return uri;
    }

    public final static class Builder {

        private String authorizationId;
        private String groupId;
        private String groupDisplayName;
        private String groupNamespace;
        private List<ResourceAuthority> authorities;
        private boolean isDeleted;

        public Builder() {
            super();
        }

        public Builder(Authorization field, Sanitizer sanitizer) {
            this.authorizationId = sanitizer.sanitize(field.authorizationId);
            this.groupId = sanitizer.sanitize(field.groupId);
            this.groupDisplayName = sanitizer.sanitize(field.groupDisplayName);
            this.groupNamespace = field.groupNamespace;
            this.isDeleted = field.isDeleted;

            if (field.authorities != null && !field.authorities.isEmpty()) {
                this.authorities = new ArrayList<ResourceAuthority>(field.authorities.size());
                for (ResourceAuthority authority : field.authorities) {
                    this.authorities.add(new ResourceAuthority.Builder(authority, sanitizer).build());
                }
            }
        }

        public Authorization build() {
            return new Authorization(this, null);
        }

        public Authorization build(ViewContext context) {
            return new Authorization(this, context);
        }

        public Builder authorizationId(String authorizationId) {
            this.authorizationId = authorizationId;
            return this;
        }

        public Builder groupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder groupDisplayName(String groupDisplayName) {
            this.groupDisplayName = groupDisplayName;
            return this;
        }

        public Builder groupNamespace(String groupNamespace) {
            this.groupNamespace = groupNamespace;
            return this;
        }

        public Builder authority(ResourceAuthority authority) {
            if (this.authorities == null)
                this.authorities = new ArrayList<ResourceAuthority>();
            this.authorities.add(authority);
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
        public static final String RESOURCE_LABEL = "Authorization";
        public static final String ROOT_ELEMENT_NAME = "authorization";
        public static final String TYPE_NAME = "AuthorizationType";
    }

}
