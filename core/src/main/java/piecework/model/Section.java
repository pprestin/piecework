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
import org.springframework.data.mongodb.core.mapping.Document;

import piecework.security.Sanitizer;
import piecework.common.view.ViewContext;

/**
 * @author James Renfro
 */
@XmlRootElement(name = Section.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = Section.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "section")
public class Section {

	@XmlAttribute
	@XmlID
	@Id
	private final String sectionId;
	
	@XmlElement
	private final String tagId;
	
	@XmlElement
	private final String title;
	
	@XmlElement
	private final String description;
	
	@XmlElementWrapper(name="fields")
	@XmlElementRef
    private final List<Field> fields;
	
	@XmlElementWrapper(name="buttons")
	@XmlElementRef
    private final List<Button> buttons;
    
	@XmlAttribute
    private final int ordinal;
	
    @XmlTransient
	@JsonIgnore
	private final boolean isDeleted;
	
	@XmlAttribute
	private final String uri;
	
	private Section() {
		this(new Section.Builder(), new ViewContext());
	}

	private Section(Section.Builder builder, ViewContext context) {
		this.sectionId = builder.sectionId;
		this.tagId = builder.tagId;
		this.title = builder.title;
		this.description = builder.description;
		this.ordinal = builder.ordinal;
		this.isDeleted = builder.isDeleted;
		this.fields = builder.fields != null ? Collections.unmodifiableList(builder.fields) : null;
		this.buttons = builder.buttons != null ? Collections.unmodifiableList(builder.buttons) : null;
		this.uri = context != null ? context.getApplicationUri(builder.processDefinitionKey, builder.sectionId) : null;
	}
	
	public String getSectionId() {
		return sectionId;
	}

	public String getTagId() {
		return tagId;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public List<Field> getFields() {
		return fields;
	}

	public List<Button> getButtons() {
		return buttons;
	}

	public int getOrdinal() {
		return ordinal;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public String getUri() {
		return uri;
	}

	public final static class Builder {

		private String sectionId;
		private String processDefinitionKey;
		private String tagId;
		private String title;
		private String description;
        private List<Field> fields;
        private List<Button> buttons;
        private int ordinal;
        private boolean isDeleted;

        public Builder() {
            super();
        }

        public Builder(Section field, Sanitizer sanitizer) {
            this.sectionId = sanitizer.sanitize(field.sectionId);
            this.tagId = sanitizer.sanitize(field.tagId);
            this.title = sanitizer.sanitize(field.title);
            this.description = sanitizer.sanitize(field.description);
            this.ordinal = field.ordinal;
            this.isDeleted = field.isDeleted;
        }

        public Section build() {
            return new Section(this, null);
        }

        public Section build(ViewContext context) {
            return new Section(this, context);
        }

        public Builder sectionId(String sectionId) {
            this.sectionId = sectionId;
            return this;
        }
        
        public Builder processDefinitionKey(String processDefinitionKey) {
            this.processDefinitionKey = processDefinitionKey;
            return this;
        }
        
        public Builder tagId(String tagId) {
            this.tagId = tagId;
            return this;
        }
        
        public Builder title(String title) {
            this.title = title;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder field(Field field) {
			if (this.fields == null)
				this.fields = new ArrayList<Field>();
			this.fields.add(field);
			return this;
		}
        
        public Builder button(Button button) {
			if (this.buttons == null)
				this.buttons = new ArrayList<Button>();
			this.buttons.add(button);
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

        public int numberOfButtons() {
            return buttons != null ? buttons.size() : 0;
        }

        public int numberOfFields() {
            return fields != null ? fields.size() : 0;
        }
	}
	
	public static class Constants {
        public static final String RESOURCE_LABEL = "Section";
        public static final String ROOT_ELEMENT_NAME = "section";
        public static final String TYPE_NAME = "SectionType";
    }
}
