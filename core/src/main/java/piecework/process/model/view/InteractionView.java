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
package piecework.process.model.view;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import piecework.Sanitizer;
import piecework.common.view.ViewContext;
import piecework.process.model.Interaction;
import piecework.process.model.builder.InteractionBuilder;

/**
 * @author James Renfro
 */
@XmlRootElement(name = InteractionView.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = InteractionView.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
public class InteractionView implements Interaction {

	@XmlAttribute
	@XmlID
	private final String id;
	
	@XmlElement
	private final String label;
	
//	@XmlElementWrapper(name="screens")
//	@XmlElementRef
//	private final List<ScreenView> screens;
	
	
	private InteractionView() {
		this(new InteractionView.Builder(), new ViewContext());
	}
			
	private InteractionView(InteractionView.Builder builder, ViewContext context) {
		this.id = builder.getId();
		this.label = builder.getLabel();
		//this.screens = builder.getScreens();
	}
	
	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}
	
//	public List<ScreenView> getScreens() {
//		return screens;
//	}

	public final static class Builder extends InteractionBuilder<InteractionView> {
		
		public Builder() {
			super();
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public InteractionView.Builder id(String id) {
			super.id(id);
			return this;
		}
		
		public Builder(Interaction interaction, Sanitizer sanitizer) {
			super(interaction, sanitizer);
		}
		
		public InteractionView build() {
			return new InteractionView(this, null);
		}
		
		public InteractionView build(ViewContext context) {
			return new InteractionView(this, context);
		}
	}

	public static class Constants {
		public static final String RESOURCE_LABEL = "Interaction";
		public static final String ROOT_ELEMENT_NAME = "interaction";
		public static final String TYPE_NAME = "InteractionType";
	}
	
}
