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
import java.util.UUID;

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
public class Section {

	@XmlAttribute
	@XmlID
	@Id
	private final String sectionId;

    @XmlElement
    private final String type;
	
	@XmlElement
	private final String tagId;
	
	@XmlElement
	private final String title;
	
	@XmlElement
	private final String description;

    @XmlElementWrapper(name="references")
    private final List<String> references;
	
	@XmlElementWrapper(name="fields")
	@XmlElementRef
    private final List<Field> fields;
	
//	@XmlElementWrapper(name="buttons")
//	@XmlElementRef
//    private final List<Button> buttons;
    
	@XmlAttribute
    private final int ordinal;
	
    @XmlTransient
	@JsonIgnore
	private final boolean isDeleted;
	
	@XmlAttribute
	private final String link;
	
	private Section() {
		this(new Section.Builder(), new ViewContext());
	}

	private Section(Section.Builder builder, ViewContext context) {
		this.sectionId = builder.sectionId;
        this.type = builder.type;
		this.tagId = builder.tagId;
		this.title = builder.title;
		this.description = builder.description;
		this.ordinal = builder.ordinal;
		this.isDeleted = builder.isDeleted;
        this.references = builder.references != null ? Collections.unmodifiableList(builder.references) : null;
		this.fields = builder.fields != null ? Collections.unmodifiableList(builder.fields) : null;
//		this.buttons = builder.buttons != null ? Collections.unmodifiableList(builder.buttons) : null;
		this.link = context != null ? context.getApplicationUri(builder.processDefinitionKey, builder.sectionId) : null;
	}
	
	public String getSectionId() {
		return sectionId;
	}

	public String getTagId() {
		return tagId;
	}

    public String getType() {
        return type;
    }

    public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

    public List<String> getReferences() {
        return references;
    }

    public List<Field> getFields() {
		return fields;
	}

	public int getOrdinal() {
		return ordinal;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public String getLink() {
		return link;
	}

	public final static class Builder {

		private String sectionId;
        private String type;
		private String processDefinitionKey;
		private String tagId;
		private String title;
		private String description;
        private List<String> references;
        private List<Field> fields;
//        private List<Button> buttons;
        private int ordinal;
        private boolean isDeleted;

        public Builder() {
            super();
            this.sectionId = UUID.randomUUID().toString();
            this.type = piecework.Constants.SectionTypes.STANDARD;
            this.fields = new ArrayList<Field>();
//            this.buttons = new ArrayList<Button>();
        }

        public Builder(Section section, Sanitizer sanitizer) {
            this(section, sanitizer, true);
        }

        public Builder(Section section, Sanitizer sanitizer, boolean includeFields) {
            this.sectionId = section.sectionId != null ? sanitizer.sanitize(section.sectionId) : UUID.randomUUID().toString();
            this.type = sanitizer.sanitize(section.type);
            this.tagId = sanitizer.sanitize(section.tagId);
            this.title = sanitizer.sanitize(section.title);
            this.description = sanitizer.sanitize(section.description);
            this.ordinal = section.ordinal;
            this.isDeleted = section.isDeleted;

            if (section.references != null && !section.references.isEmpty()) {
                this.references = new ArrayList<String>(section.references.size());
                for (String reference : section.references) {
                    this.references.add(sanitizer.sanitize(reference));
                }
            }

            if (includeFields && section.fields != null && !section.fields.isEmpty()) {
                this.fields = new ArrayList<Field>(section.fields.size());
                for (Field field : section.fields) {
                    this.fields.add(new Field.Builder(field, sanitizer).processDefinitionKey(processDefinitionKey).build());
                }
            } else {
                this.fields = new ArrayList<Field>();
            }
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

        public Builder type(String type) {
            this.type = type;
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

        public Builder reference(String reference) {
            if (this.references == null)
                this.references = new ArrayList<String>();
            this.references.add(reference);
            return this;
        }

        public Builder field(Field field) {
			if (this.fields == null)
				this.fields = new ArrayList<Field>();
			this.fields.add(field);
			return this;
		}
        
//        public Builder button(Button button) {
//			if (this.buttons == null)
//				this.buttons = new ArrayList<Button>();
//			this.buttons.add(button);
//			return this;
//		}
        
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

//        public int numberOfButtons() {
//            return buttons != null ? buttons.size() : 0;
//        }

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
