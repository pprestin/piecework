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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import piecework.security.Sanitizer;
import piecework.common.view.ViewContext;

/**
 * @author James Renfro
 */
@XmlRootElement(name = Screen.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = Screen.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "screen")
public class Screen implements Serializable {

	private static final long serialVersionUID = -455579611494172459L;

	@XmlAttribute
	@XmlID
	@Id
	private final String screenId;
	
	@XmlAttribute
	@Transient
	private final String processDefinitionKey;
	
	@XmlElement
	private final String title;
	
	@XmlElement
	private final String type;
	
	@XmlElement
	private final String location;

    @XmlAttribute
    private final boolean isAttachmentAllowed;
	
	@XmlElementWrapper(name="sections")
	@XmlElementRef
	private final List<Section> sections;
	
	@XmlAttribute
    private final int ordinal;
	
	@XmlTransient
	@JsonIgnore
	private final boolean isDeleted;
	
	@XmlAttribute
	private final String uri;


	private Screen() {
		this(new Screen.Builder(), new ViewContext());
	}

	private Screen(Screen.Builder builder, ViewContext context) {
		this.screenId = builder.screenId;
		this.processDefinitionKey = builder.processDefinitionKey;
		this.title = builder.title;
		this.type = builder.type;
		this.location = builder.location;
		this.ordinal = builder.ordinal;
		this.isDeleted = builder.isDeleted;
        this.isAttachmentAllowed = builder.isAttachmentAllowed;
		this.sections = builder.sections != null ? Collections.unmodifiableList(builder.sections) : null;
		this.uri = context != null ? context.getApplicationUri(builder.processDefinitionKey, builder.interactionId, builder.screenId) : null;
	}
	
	public String getScreenId() {
		return screenId;
	}

	public String getProcessDefinitionKey() {
		return processDefinitionKey;
	}

	public String getUri() {
		return uri;
	}

	public String getTitle() {
		return title;
	}

	public String getType() {
		return type;
	}

	public String getLocation() {
		return location;
	}

    public boolean isAttachmentAllowed() {
        return isAttachmentAllowed;
    }

    public List<Section> getSections() {
        return sections;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public boolean isDeleted() {
		return isDeleted;
	}

	public final static class Builder {

		private String screenId;
		private String processDefinitionKey;
		private String interactionId;
		private String title;
		private String type;
		private String location;
        private boolean isAttachmentAllowed;
		private List<Section> sections;
		private int ordinal;
		private boolean isDeleted;
		
		public Builder() {
			super();
		}

		public Builder(Screen screen, Sanitizer sanitizer) {
			this.screenId = sanitizer.sanitize(screen.screenId);
			this.processDefinitionKey = sanitizer.sanitize(screen.processDefinitionKey);
			this.title = sanitizer.sanitize(screen.title);
			this.type = sanitizer.sanitize(screen.type);
            this.isAttachmentAllowed = screen.isAttachmentAllowed;
			this.location = sanitizer.sanitize(screen.location);
			this.ordinal = screen.ordinal;
			
			if (screen.sections != null && !screen.sections.isEmpty()) {
				this.sections = new ArrayList<Section>(screen.sections.size());
				for (Section section : screen.sections) {
					this.sections.add(new Section.Builder(section, sanitizer).processDefinitionKey(processDefinitionKey).build());
				}
			}
		}

		public Screen build() {
			return new Screen(this, null);
		}

		public Screen build(ViewContext context) {
			return new Screen(this, context);
		}
		
		public Builder screenId(String screenId) {
			this.screenId = screenId;
			return this;
		}
		
		public Builder processDefinitionKey(String processDefinitionKey) {
			this.processDefinitionKey = processDefinitionKey;
			return this;
		}
		
		public Builder interactionId(String interactionId) {
			this.interactionId = interactionId;
			return this;
		}
		
		public Builder title(String title) {
			this.title = title;
			return this;
		}
		
		public Builder type(String type) {
			this.type = type;
			return this;
		}
		
		public Builder location(String location) {
			this.location = location;
			return this;
		}

        public Builder attachmentAllowed(boolean isAttachmentAllowed) {
            this.isAttachmentAllowed = isAttachmentAllowed;
            return this;
        }
		
		public Builder section(Section section) {
			if (this.sections == null)
				this.sections = new ArrayList<Section>();
			this.sections.add(section);
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
		public static final String RESOURCE_LABEL = "Screen";
		public static final String ROOT_ELEMENT_NAME = "screen";
		public static final String TYPE_NAME = "ScreenType";
	}
}
