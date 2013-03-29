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
package piecework.form.model.record;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import piecework.form.model.Constraint;
import piecework.form.model.FormField;
import piecework.form.model.FormFieldElement;
import piecework.form.model.Option;

/**
 * @author James Renfro
 */
@Document(collection="field")
public class FormFieldRecord implements FormField, Serializable {

	private static final long serialVersionUID = 832929218519020243L;
	
	@Id
	private String id;
	private String propertyName;
	private FormFieldElementRecord label;
	private FormFieldElementRecord directions;
	private Boolean editable;
	private Boolean required;
	private Boolean restricted;
	private String typeAttr;
	private List<FormFieldElementRecord> elements;
	private OptionProviderRecord optionProvider;
	private List<OptionRecord> options;
	private List<ConstraintRecord> constraints;
	private String message;
	private String messageType;
	
	public FormFieldRecord() {
		
	}
	
	public FormFieldRecord(FormField field) {
		this.id = field.getId() != null ? field.getId() : UUID.randomUUID().toString();
		this.propertyName = field.getPropertyName();
		this.label = field.getLabel() != null ? new FormFieldElementRecord(field.getLabel()) : null;
		this.directions = field.getDirections() != null ? new FormFieldElementRecord(field.getDirections()) : null;
		this.editable = field.getEditable();
		this.required = field.getRequired();
		this.restricted = field.getRestricted();
		this.typeAttr = field.getTypeAttr();
		List<? extends FormFieldElement> elementContracts = field.getElements();
		if (elementContracts != null && !elementContracts.isEmpty()) {
			this.elements = new ArrayList<FormFieldElementRecord>(elementContracts.size());
			for (FormFieldElement elementContract : elementContracts) {
				elements.add(new FormFieldElementRecord(elementContract));
			}
		}
		this.optionProvider = field.getOptionProvider() != null ? new OptionProviderRecord(field.getOptionProvider()) : null;
		List<? extends Option> optionContracts = field.getOptions();
		if (optionContracts != null && !optionContracts.isEmpty()) {
			this.options = new ArrayList<OptionRecord>(optionContracts.size());
			for (Option optionContract : optionContracts) {
				options.add(new OptionRecord(optionContract));
			}
		}
		List<? extends Constraint> constraintContracts = field.getConstraints();
		if (constraintContracts != null && !constraintContracts.isEmpty()) {
			this.constraints = new ArrayList<ConstraintRecord>(constraintContracts.size());
			for (Constraint constraintContract : constraintContracts) {
				this.constraints.add(new ConstraintRecord(constraintContract));
			}
		} else {
			this.constraints = null;
		}
		this.message = field.getMessage();
		this.messageType = field.getMessageType();
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPropertyName() {
		return propertyName;
	}
	
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	
	public FormFieldElementRecord getLabel() {
		return label;
	}
	
	public void setLabel(FormFieldElementRecord label) {
		this.label = label;
	}
	
	public FormFieldElementRecord getDirections() {
		return directions;
	}

	public void setDirections(FormFieldElementRecord directions) {
		this.directions = directions;
	}
	
	@SuppressWarnings("unchecked")
	public OptionProviderRecord getOptionProvider() {
		return optionProvider;
	}

	public void setOptionProvider(OptionProviderRecord optionProvider) {
		this.optionProvider = optionProvider;
	}

	public Boolean getEditable() {
		return editable;
	}
	
	public void setEditable(Boolean editable) {
		this.editable = editable;
	}
	
	public Boolean getRequired() {
		return required;
	}
	
	public void setRequired(Boolean required) {
		this.required = required;
	}
	
	public Boolean getRestricted() {
		return restricted;
	}

	public void setRestricted(Boolean restricted) {
		this.restricted = restricted;
	}

	public String getTypeAttr() {
		return typeAttr;
	}
	
	public void setTypeAttr(String typeAttr) {
		this.typeAttr = typeAttr;
	}
	
	public List<FormFieldElementRecord> getElements() {
		return elements;
	}
	
	public void setElements(List<FormFieldElementRecord> elements) {
		this.elements = elements;
	}
	
	public List<OptionRecord> getOptions() {
		return options;
	}
	
	public void setOptions(List<OptionRecord> options) {
		this.options = options;
	}

	public List<ConstraintRecord> getConstraints() {
		return constraints;
	}

	public void setConstraints(List<ConstraintRecord> constraints) {
		this.constraints = constraints;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	
}
