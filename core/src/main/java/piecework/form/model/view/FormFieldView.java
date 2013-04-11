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
package piecework.form.model.view;

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

import piecework.form.model.Constraint;
import piecework.form.model.FormField;
import piecework.form.model.FormFieldElement;
import piecework.form.model.Option;
import piecework.form.model.OptionProvider;
import piecework.form.model.builder.ConstraintBuilder;
import piecework.form.model.builder.FormFieldBuilder;
import piecework.form.model.builder.FormFieldElementBuilder;
import piecework.form.model.builder.OptionBuilder;
import piecework.form.model.builder.OptionProviderBuilder;

/**
 * @author James Renfro
 */
@XmlRootElement(name = FormFieldView.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = FormFieldView.Constants.TYPE_NAME)
public class FormFieldView implements FormField {

	@XmlAttribute(name = FormFieldView.Attributes.ID)
	@XmlID
	private final String id;
	
	@XmlAttribute(name = FormFieldView.Attributes.PROPERTY_NAME)
	private final String propertyName;
	
	@XmlAttribute(name = FormFieldView.Attributes.TYPE_ATTR)
	private final String typeAttr;
	
	@XmlElementWrapper(name = FormFieldView.Lists.ELEMENTS)
	@XmlElementRef(name = FormFieldElementView.Constants.ROOT_ELEMENT_NAME)
	private final List<FormFieldElementView> elements;
	
	@XmlElement(name = FormFieldView.Elements.LABEL)
	private final FormFieldElementView label;
	
	@XmlElement(name = FormFieldView.Elements.DIRECTIONS)
	private final FormFieldElementView directions;

	@XmlElement(name = FormFieldView.Elements.OPTION_PROVIDER)
	private final OptionProviderView optionProvider;
	
	@XmlElementWrapper(name = FormFieldView.Lists.OPTIONS)
	@XmlElementRef
	private final List<OptionView> options;
	
	@XmlElementWrapper(name = FormFieldView.Lists.CONSTRAINTS)
	@XmlElementRef
	private final List<ConstraintView> constraints;
	
	@XmlAttribute(name = FormFieldView.Attributes.EDITABLE)
	private final Boolean editable;
	
	@XmlAttribute(name = FormFieldView.Attributes.REQUIRED)
	private final Boolean required;
	
	@XmlAttribute(name = FormFieldView.Attributes.RESTRICTED)
	private final Boolean restricted;
	
	@XmlElement(name = FormFieldView.Elements.MESSAGE)
	private final String message;
	
	@XmlElement(name = FormFieldView.Elements.MESSAGE_TYPE)
	private final String messageType;
	
	private FormFieldView() {
		this(new FormFieldView.Builder(), false);
	}
	
	private FormFieldView(FormFieldView.Builder builder, boolean readOnly) {
		this.id = builder.getId();
		this.propertyName = builder.getPropertyName();
		this.typeAttr = builder.getTypeAttr();
		this.elements = builder.buildElements(builder.getElements());
		this.label = builder.buildElement(builder.getLabel());
		this.directions = builder.buildElement(builder.getDirections());;
		this.optionProvider = FormFieldView.Builder.buildOptionProvider(builder.getOptionProvider());
		this.options = builder.buildOptions(builder.getOptions());
		this.editable = FormFieldView.Builder.getEditable(builder.getEditable(), readOnly);
		this.required = builder.getRequired();
		this.restricted = builder.getRestricted();
		this.constraints = builder.buildConstraints(builder.getConstraints());
		this.message = builder.getMessage();
		this.messageType = builder.getMessageType();
	}
	
	public String getId() {
		return id;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public String getTypeAttr() {
		return typeAttr;
	}

	@SuppressWarnings("unchecked")
	public List<FormFieldElementView> getElements() {
		return elements;
	}

	@SuppressWarnings("unchecked")
	public FormFieldElementView getLabel() {
		return label;
	}

	@SuppressWarnings("unchecked")
	public FormFieldElementView getDirections() {
		return directions;
	}

	@SuppressWarnings("unchecked")
	public OptionProvider<?> getOptionProvider() {
		return optionProvider;
	}

	public List<OptionView> getOptions() {
		return options;
	}

	public Boolean getEditable() {
		return editable;
	}

	public Boolean getRequired() {
		return required;
	}

	public Boolean getRestricted() {
		return restricted;
	}
	
	public String getMessage() {
		return message;
	}

	public String getMessageType() {
		return messageType;
	}

	public List<ConstraintView> getConstraints() {
		return constraints;
	}

	static class Attributes {
		final static String EDITABLE = "editable";
		final static String ID = "id";
		final static String PROPERTY_NAME = "propertyName";
		final static String REQUIRED = "required";
		final static String RESTRICTED = "restricted";
		final static String TYPE_ATTR = "type";
	}

	static class Constants {
		public static final String ROOT_ELEMENT_NAME = "formField";
		public static final String TYPE_NAME = "FormFieldType";
	}
	
	static class Elements {
		final static String DIRECTIONS = "directions";
		final static String LABEL = "label";
		final static String MESSAGE = "message";
		final static String MESSAGE_TYPE = "messageType";
		final static String OPTION_PROVIDER = "optionProvider";
		final static String SHORT_LABEL = "shortLabel";
	}
	
	static class Lists {
		final static String CONSTRAINTS = "constraints";
		final static String ELEMENTS = "elements";
		final static String OPTIONS = "options";
	}
	
	public final static class Builder extends FormFieldBuilder<FormFieldView> {

		public Builder() {
			super();
		}
		
		public Builder(FormField field) {
			super(field);
		}

		@Override
		public FormFieldView build(boolean readOnly) {
			return new FormFieldView(this, readOnly);
		}

		@Override
		protected ConstraintBuilder<?> constraintBuilder(Constraint constraint) {
			if (constraint == null)
				return null;
			
			return new ConstraintView.Builder(constraint);
		}
		
		@Override
		protected FormFieldElementBuilder<?> elementBuilder(FormFieldElement element) {
			if (element == null)
				return null;
			
			return new FormFieldElementView.Builder(FormFieldElement.class.cast(element));
		}
		
		protected OptionBuilder<?> optionBuilder(Option option) {
			if (option == null)
				return null;
			
			return new OptionView.Builder(option);
		}
						
		static OptionProviderView buildOptionProvider(OptionProviderBuilder<?> builder) {
			return (OptionProviderView) (builder != null ? builder.build() : null);
		}
			
		private static String getSafeBooleanString(Boolean b) {
			return b != null ? Boolean.toString(b) : null;
		}
		
		private static Boolean getEditable(Boolean editable, boolean readOnly) {
			return !readOnly && editable != null ? editable : Boolean.FALSE;
		}
	}
}
