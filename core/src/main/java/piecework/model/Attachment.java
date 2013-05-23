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

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import piecework.security.Sanitizer;
import piecework.common.model.User;
import piecework.common.view.ViewContext;

/**
 * @author James Renfro
 */
@XmlRootElement(name = Attachment.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = Attachment.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = Attachment.Constants.ROOT_ELEMENT_NAME)
public class Attachment implements Serializable {

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
	private final User user;
	
	@XmlAttribute
    private final int ordinal;
	
	@XmlElement
	private final Date lastModified;
	
	@XmlElement
    private final String uri;
	
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
        this.ordinal = builder.ordinal;
        this.lastModified = builder.lastModified;
        this.isDeleted = builder.isDeleted;
        this.uri = context != null ? context.getApplicationUri(builder.processDefinitionKey, builder.attachmentId) : null;
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

	public int getOrdinal() {
		return ordinal;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public String getUri() {
		return uri;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public final static class Builder {

    	private String attachmentId;
    	private String processDefinitionKey;
    	private String name;
        private String description;
        private String contentType;
        private String location;
        private User user;
        private Date lastModified;
        private int ordinal;
        private boolean isDeleted;

        public Builder() {
            super();
        }

        public Builder(Attachment field, Sanitizer sanitizer) {
            this.attachmentId = sanitizer.sanitize(field.attachmentId);
            this.name = sanitizer.sanitize(field.name);
            this.description = sanitizer.sanitize(field.description);
            this.contentType = field.contentType;
            this.location = field.location;
            this.user = field.user != null ? new User.Builder(field.user, sanitizer).build() : null;
            this.lastModified = field.lastModified;
            this.ordinal = field.ordinal;
            this.isDeleted = field.isDeleted;
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
        
        public Builder lastModified(Date lastModified) {
            this.lastModified = lastModified;
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
	}
	
	public static class Constants {
        public static final String RESOURCE_LABEL = "Attachment";
        public static final String ROOT_ELEMENT_NAME = "attachment";
        public static final String TYPE_NAME = "AttachmentType";
    }
}
