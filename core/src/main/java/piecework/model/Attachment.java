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
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import piecework.common.ViewContext;
import piecework.security.Sanitizer;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author James Renfro
 */
@XmlRootElement(name = Attachment.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = Attachment.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = Attachment.Constants.ROOT_ELEMENT_NAME)
public class Attachment implements Serializable, Comparable<Attachment> {

	private static final long serialVersionUID = 3332973881472650839L;

	@XmlAttribute
	@XmlID
    @Id
	private final String attachmentId;
	
	@XmlElement
	private final String name;

	@XmlElement
	private final String description;
	
	@XmlElement
	private final String contentType;
	
	@XmlTransient
    @JsonIgnore
	private final String location;
	
	@XmlElement
    @Transient
	private final User user;

    @XmlTransient
    @JsonIgnore
    private final String userId;
	
	@XmlAttribute
    private final int ordinal;
	
	@XmlElement
	private final Date lastModified;

    @XmlAttribute
    private final String link;

	@XmlAttribute
    private final String uri;

    @XmlTransient
    @JsonIgnore
    private final boolean isFieldAttachment;

	@XmlTransient
    @JsonIgnore
    private final boolean isDeleted;
	
	private Attachment() {
        this(new Attachment.Builder(), new ViewContext());
    }

    private Attachment(Attachment.Builder builder, ViewContext context) {
        this.attachmentId = builder.attachmentId;
        this.name = builder.name;
        this.description = builder.description;
        this.contentType = builder.contentType;
        this.location = builder.location;
        this.user = builder.user;
        this.userId = builder.userId;
        this.ordinal = builder.ordinal;
        this.lastModified = builder.lastModified;
        this.isFieldAttachment = builder.isFieldAttachment;
        this.isDeleted = builder.isDeleted;
        this.link = context != null ? context.getApplicationUri(builder.processDefinitionKey, builder.processInstanceId, Constants.ROOT_ELEMENT_NAME, builder.attachmentId) : null;
        this.uri = context != null ? context.getServiceUri(builder.processDefinitionKey, builder.processInstanceId, Constants.ROOT_ELEMENT_NAME, builder.attachmentId) : null;
    }
	
	public String getAttachmentId() {
		return attachmentId;
	}

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
		return description;
	}

	public String getContentType() {
		return contentType;
	}

	public User getUser() {
		return user;
	}

    @JsonIgnore
    public String getUserId() {
        return userId;
    }

    public int getOrdinal() {
		return ordinal;
	}

	public Date getLastModified() {
		return lastModified;
	}

    public String getLink() {
        return link;
    }

    public String getUri() {
		return uri;
	}

    @JsonIgnore
    public boolean isFieldAttachment() {
        return isFieldAttachment;
    }

    @JsonIgnore
	public boolean isDeleted() {
		return isDeleted;
	}

    @Override
    public int compareTo(Attachment o) {
        int result = 0;
        if (lastModified != null && o.lastModified != null)
            result = o.lastModified.compareTo(lastModified);
        if (result == 0 && description != null && o.description != null)
            result = description.compareTo(o.description);
        if (result == 0 && name != null && o.name != null)
            result = name.compareTo(o.name);
        if (result == 0 && attachmentId != null && o.attachmentId != null)
            result = attachmentId.compareTo(o.attachmentId);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Attachment that = (Attachment) o;

        if (!attachmentId.equals(that.attachmentId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return attachmentId.hashCode();
    }

    public final static class Builder {

    	private String attachmentId;
    	private String processDefinitionKey;
        private String processInstanceId;
    	private String name;
        private String description;
        private String contentType;
        private String location;
        private User user;
        private String userId;
        private Date lastModified;
        private int ordinal;
        private boolean isFieldAttachment;
        private boolean isDeleted;

        public Builder() {
            super();
        }

        public Builder(Attachment attachment) {
            this.attachmentId = attachment.attachmentId;
            this.name = attachment.name;
            this.description = attachment.description;
            this.contentType = attachment.contentType;
            this.location = attachment.location;
            this.user = attachment.user != null ? new User.Builder(attachment.user).build() : null;
            this.userId = attachment.user != null && attachment.user.getUserId() != null ? attachment.user.getUserId() : attachment.userId;
            this.lastModified = attachment.lastModified;
            this.ordinal = attachment.ordinal;
            this.isFieldAttachment = attachment.isFieldAttachment;
            this.isDeleted = attachment.isDeleted;
        }

        public Builder(Attachment attachment, Sanitizer sanitizer) {
            this.attachmentId = sanitizer.sanitize(attachment.attachmentId);
            this.name = sanitizer.sanitize(attachment.name);
            this.description = sanitizer.sanitize(attachment.description);
            this.contentType = attachment.contentType;
            this.location = attachment.location;
            this.user = attachment.user != null ? new User.Builder(attachment.user, sanitizer).build() : null;
            this.userId = attachment.user != null && attachment.user.getUserId() != null ? sanitizer.sanitize(attachment.user.getUserId()) : sanitizer.sanitize(attachment.userId);
            this.lastModified = attachment.lastModified;
            this.ordinal = attachment.ordinal;
            this.isFieldAttachment = attachment.isFieldAttachment;
            this.isDeleted = attachment.isDeleted;
        }

        public Attachment build() {
            return new Attachment(this, null);
        }

        public Attachment build(ViewContext context) {
            return new Attachment(this, context);
        }

        public Builder attachmentId(String attachmentId) {
            this.attachmentId = attachmentId;
            return this;
        }

        public Builder processDefinitionKey(String processDefinitionKey) {
            this.processDefinitionKey = processDefinitionKey;
            return this;
        }

        public Builder processInstanceId(String processInstanceId) {
            this.processInstanceId = processInstanceId;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }
        
        public Builder location(String location) {
            this.location = location;
            return this;
        }
        
        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public Builder lastModified(Date lastModified) {
            this.lastModified = lastModified;
            return this;
        }
        
        public Builder ordinal(int ordinal) {
            this.ordinal = ordinal;
            return this;
        }

        public Builder fieldAttachment() {
            this.isFieldAttachment = true;
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

        public String getAttachmentId() {
            return attachmentId;
        }

        public String getUserId() {
            return userId;
        }
    }
	
	public static class Constants {
        public static final String RESOURCE_LABEL = "Attachment";
        public static final String ROOT_ELEMENT_NAME = "attachment";
        public static final String TYPE_NAME = "AttachmentType";
    }
}
