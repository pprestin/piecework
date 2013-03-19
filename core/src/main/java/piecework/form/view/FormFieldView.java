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
package piecework.form.view;

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
import javax.xml.bind.annotation.XmlType;

import piecework.form.model.Constraint;
import piecework.form.model.FormField;
import piecework.form.model.FormFieldElement;
import piecework.form.model.Option;
import piecework.form.model.OptionProvider;

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
	private final String editable;
	
	@XmlAttribute(name = FormFieldView.Attributes.REQUIRED)
	private final String required;
	
	@XmlAttribute(name = FormFieldView.Attributes.RESTRICTED)
	private final String restricted;
	
	@XmlElement(name = FormFieldView.Elements.MESSAGE)
	private final String message;
	
	@XmlElement(name = FormFieldView.Elements.MESSAGE_TYPE)
	private final String messageType;
	
	private FormFieldView() {
		this(new FormFieldView.Builder(), false);
	}
	
	private FormFieldView(FormFieldView.Builder builder, boolean readOnly) {
		this.id = builder.id;
		this.propertyName = builder.propertyName;
		this.typeAttr = builder.typeAttr;
		this.elements = buildElements(builder.elements);
		this.label = builder.label != null ? builder.label.build() : null;
		this.directions = builder.directions != null ? builder.directions.build() : null;
		this.optionProvider = builder.optionProvider != null ? builder.optionProvider.build() : null;
		this.options = buildOptions(builder.options);
		this.editable = !readOnly && builder.editable != null ? Boolean.toString(builder.editable) : Boolean.FALSE.toString();
		this.required = builder.required != null ? Boolean.toString(builder.required) : null;
		this.restricted = builder.restricted != null ? Boolean.toString(builder.restricted) : null;
		this.constraints = buildConstraints(builder.constraints);
		this.message = builder.message;
		this.messageType = builder.messageType;
	}
	
	private static List<ConstraintView> buildConstraints(List<ConstraintView.Builder> builders) {
		if (builders == null || builders.isEmpty())
			return null;
		List<ConstraintView> constraints = new ArrayList<ConstraintView>(builders.size());
		for (ConstraintView.Builder builder : builders) {
			constraints.add(builder.build());
		}
		return Collections.unmodifiableList(constraints);
	}
	
	private static List<FormFieldElementView> buildElements(List<FormFieldElementView.Builder> builders) {
		if (builders == null || builders.isEmpty())
			return null;
		List<FormFieldElementView> elements = new ArrayList<FormFieldElementView>(builders.size());
		for (FormFieldElementView.Builder builder : builders) {
			elements.add(builder.build());
		}
		return Collections.unmodifiableList(elements);
	}
	
	private static List<OptionView> buildOptions(List<OptionView.Builder> builders) {
		if (builders == null || builders.isEmpty())
			return null;
		List<OptionView> options = new ArrayList<OptionView>(builders.size());
		for (OptionView.Builder builder : builders) {
			options.add(builder.build());
		}
		return Collections.unmodifiableList(options);
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

	public List<FormFieldElementView> getElements() {
		return elements;
	}

	public FormFieldElementView getLabel() {
		return label;
	}

	public FormFieldElementView getDirections() {
		return directions;
	}

	@SuppressWarnings("unchecked")
	public OptionProvider getOptionProvider() {
		return optionProvider;
	}

	public List<OptionView> getOptions() {
		return options;
	}

	public String getEditable() {
		return editable;
	}

	public String getRequired() {
		return required;
	}

	public String getRestricted() {
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
	
	/*
	 * Fluent builder class, as per Joshua Bloch's Effective Java
	 */
	public final static class Builder {
		
		private final String id;
		private String propertyName;
		private String typeAttr;
		private List<FormFieldElementView.Builder> elements;
		private FormFieldElementView.Builder label;
		private FormFieldElementView.Builder directions;
		private OptionProviderView.Builder optionProvider;
		private List<OptionView.Builder> options;
		private List<ConstraintView.Builder> constraints;
		private Boolean editable;
		private Boolean required;
		private Boolean restricted;
		private String message;
		private String messageType;
		
		public Builder() {
			this((String)null);
		}
		
		public Builder(String id) {
			this.id = id;
		}
		
		public Builder(FormField contract) {
			this.id = contract.getId();
			this.propertyName = contract.getPropertyName();
			this.label = contract.getLabel() != null ? new FormFieldElementView.Builder(contract.getLabel()) : null;
			this.directions = contract.getDirections() != null ? new FormFieldElementView.Builder(contract.getDirections()) : null;
			this.editable = Boolean.valueOf(contract.getEditable());
			this.required = Boolean.valueOf(contract.getRequired());
			this.restricted = Boolean.valueOf(contract.getRestricted());
			this.typeAttr = contract.getTypeAttr();
			List<? extends FormFieldElement> elementContracts = contract.getElements();
			if (elementContracts != null && !elementContracts.isEmpty()) {
				this.elements = new ArrayList<FormFieldElementView.Builder>(elementContracts.size());
				for (FormFieldElement elementContract : elementContracts) {
					elements.add(new FormFieldElementView.Builder(elementContract));
				}
			} else {
				this.elements = null;
			}
			String valueExpression = contract.getOptionProvider() != null ? contract.getOptionProvider().getValueExpression() : null;
			this.optionProvider = valueExpression != null && valueExpression.length() > 0 ? new OptionProviderView.Builder(contract.getOptionProvider()) : null;
			List<? extends Option> optionContracts = contract.getOptions();
//			if (optionResolver != null) {
//				List<? extends OptionContract> resolvedOptionContracts = optionResolver.getOptions(optionProvider);
//				if (resolvedOptionContracts != null && !resolvedOptionContracts.isEmpty())
//					optionContracts = resolvedOptionContracts;
//			}
			
			if (optionContracts != null && !optionContracts.isEmpty()) {
				this.options = new ArrayList<OptionView.Builder>(optionContracts.size());
				for (Option optionContract : optionContracts) {
					options.add(new OptionView.Builder(optionContract));
				}
			} else {
				this.options = null;
			}
			List<? extends Constraint> constraintContracts = contract.getConstraints();
			if (constraintContracts != null && !constraintContracts.isEmpty()) {
				this.constraints = new ArrayList<ConstraintView.Builder>(constraintContracts.size());
				for (Constraint constraintContract : constraintContracts) {
					this.constraints.add(new ConstraintView.Builder(constraintContract));
				}
			} else {
				this.constraints = null;
			}
			this.message = contract.getMessage();
			this.messageType = contract.getMessageType();
		}
		
		public FormFieldView build(boolean readOnly) {
			return new FormFieldView(this, readOnly);
		}
		
		public FormFieldView.Builder propertyName(String propertyName) {
			this.propertyName = propertyName;
			return this;
		}
		
		public FormFieldView.Builder typeAttr(String typeAttr) {
			this.typeAttr = typeAttr;
			return this;
		}
		
		public FormFieldView.Builder element(FormFieldElementView.Builder element) {
			if (this.elements == null)
				this.elements = new ArrayList<FormFieldElementView.Builder>();
			this.elements.add(element);
			return this;
		}
		
		public FormFieldView.Builder elements(List<FormFieldElementView.Builder> elements) {
			if (this.elements == null)
				this.elements = new ArrayList<FormFieldElementView.Builder>();
			this.elements.addAll(elements);
			return this;
		}
		
		public FormFieldView.Builder label(FormFieldElementView.Builder label) {
			this.label = label;
			return this;
		}
		
		public FormFieldView.Builder directions(FormFieldElementView.Builder directions) {
			this.directions = directions;
			return this;
		}
		
		public FormFieldView.Builder option(OptionView.Builder option) {
			if (this.options == null)
				this.options = new ArrayList<OptionView.Builder>();
			this.options.add(option);
			return this;
		}
		
		public FormFieldView.Builder options(List<OptionView.Builder> options) {
			if (options != null && !options.isEmpty()) {
				if (this.options == null)
					this.options = new ArrayList<OptionView.Builder>();
				this.options.addAll(options);
			}
			return this;
		}
		
		public FormFieldView.Builder constraint(ConstraintView.Builder constraint) {
			if (this.constraints == null)
				this.constraints = new ArrayList<ConstraintView.Builder>();
			this.constraints.add(constraint);
			return this;
		}
		
		public FormFieldView.Builder constraints(List<ConstraintView.Builder> constraints) {
			if (this.constraints == null)
				this.constraints = new ArrayList<ConstraintView.Builder>();
			this.constraints.addAll(constraints);
			return this;
		}
		
		public FormFieldView.Builder editable(Boolean editable) {
			this.editable = editable;
			return this;
		}
		
		public FormFieldView.Builder required(Boolean required) {
			this.required = required;
			return this;
		}
		
		public FormFieldView.Builder restricted(Boolean restricted) {
			this.restricted = restricted;
			return this;
		}
		
		public FormFieldView.Builder message(String message) {
			this.message = message;
			return this;
		}
		
		public FormFieldView.Builder messageType(String messageType) {
			this.messageType = messageType;
			return this;
		}
		
		public String getPropertyName() {
			return this.propertyName;
		}
		
		public String getTypeAttr() {
			return this.typeAttr;
		}

		public OptionProviderView.Builder getOptionProvider() {
			return optionProvider;
		}

		public List<OptionView.Builder> getOptions() {
			return this.options;
		}
		
		public Boolean getRestricted() {
			return this.restricted;
		}
		
		public List<FormFieldElementView.Builder> getElements() {
			return this.elements;
		}
	}
}
