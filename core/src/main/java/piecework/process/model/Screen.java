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
package piecework.process.model;

import java.io.Serializable;

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

import piecework.Sanitizer;
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
	private final String id;
	
	@XmlAttribute
	private final String uri;
	
	@XmlElement
	private final String title;
	
	@XmlElement
	private final String type;
	
	@XmlElement
	private final String location;
	
	@XmlTransient
	@JsonIgnore
	private final boolean isDeleted;
	
//	@XmlElementWrapper(name="screens")
//	@XmlElementRef
//	private final List<Section> sections;

	private Screen() {
		this(new Screen.Builder(), new ViewContext());
	}

	private Screen(Screen.Builder builder, ViewContext context) {
		this.id = builder.id;
		this.title = builder.title;
		this.type = builder.type;
		this.location = builder.location;
		this.isDeleted = builder.isDeleted;
		this.uri = context != null ? context.getApplicationUri(builder.processDefinitionKey, builder.interactionId, builder.id) : null;
//		this.sections = builder.sections != null ? Collections.unmodifiableList(builder.sections) : null;
	}
	
	public String getId() {
		return id;
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

	public boolean isDeleted() {
		return isDeleted;
	}

	public final static class Builder {

		private String id;
		private String processDefinitionKey;
		private String interactionId;
		private String title;
		private String type;
		private String location;
		private boolean isDeleted;
		
		public Builder() {
			super();
		}

		public Builder(Screen interaction, Sanitizer sanitizer) {
			this.id = sanitizer.sanitize(interaction.id);
			this.title = sanitizer.sanitize(interaction.title);
			this.type = sanitizer.sanitize(interaction.type);
			this.location = sanitizer.sanitize(interaction.location);
		}

		public Screen build() {
			return new Screen(this, null);
		}

		public Screen build(ViewContext context) {
			return new Screen(this, context);
		}
		
		public Builder id(String id) {
			this.id = id;
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
		
//		public Builder screen(Screen screen) {
//			if (this.screens == null)
//				this.screens = new ArrayList<Screen>();
//			this.screens.add(screen);
//			return this;
//		}
		
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
