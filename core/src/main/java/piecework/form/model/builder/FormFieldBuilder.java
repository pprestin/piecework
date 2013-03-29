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
package piecework.form.model.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import piecework.form.model.Constraint;
import piecework.form.model.FormField;
import piecework.form.model.FormFieldElement;
import piecework.form.model.Option;
import piecework.form.model.OptionProvider;
import piecework.form.model.view.ConstraintView;
import piecework.form.model.view.FormFieldElementView;
import piecework.form.model.view.OptionProviderView;

/**
 * @author James Renfro
 */
public abstract class FormFieldBuilder<F extends FormField> extends Builder {
	
	private String propertyName;
	private String typeAttr;
	private List<FormFieldElementBuilder<?>> elements;
	private FormFieldElementBuilder<?> label;
	private FormFieldElementBuilder<?> directions;
	private OptionProviderView.Builder optionProvider;
	private List<OptionBuilder<?>> options;
	private List<ConstraintBuilder<?>> constraints;
	private Boolean editable;
	private Boolean required;
	private Boolean restricted;
	private String message;
	private String messageType;
	
	public FormFieldBuilder() {
		super();
	}
	
	public FormFieldBuilder(FormField field) {
		super(field.getId());
		this.propertyName = field.getPropertyName();
		this.label = elementBuilder(field.getLabel());
		this.directions = elementBuilder(field.getDirections());
		this.editable = Boolean.valueOf(field.getEditable());
		this.required = Boolean.valueOf(field.getRequired());
		this.restricted = Boolean.valueOf(field.getRestricted());
		this.typeAttr = field.getTypeAttr();
		this.elements = elementBuilders(field.getElements());
		this.optionProvider = optionProviderBuilder(field.getOptionProvider());
		this.options = optionBuilders(field.getOptions());
		this.constraints = constraintBuilders(field.getConstraints());
		this.message = field.getMessage();
		this.messageType = field.getMessageType();
	}
	
	public abstract F build(boolean readOnly);

	@SuppressWarnings("unchecked")
	public <T extends Constraint> List<T> buildConstraints(List<ConstraintBuilder<?>> builders) {
		if (builders == null || builders.isEmpty())
			return null;
		List<T> constraints = new ArrayList<T>(builders.size());
		for (ConstraintBuilder<?> builder : builders) {
			constraints.add((T)builder.build());
		}
		return Collections.unmodifiableList(constraints);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends FormFieldElement> T buildElement(FormFieldElementBuilder<?> builder) {
		return (T) (builder != null ? builder.build() : null);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends FormFieldElement> List<T> buildElements(List<FormFieldElementBuilder<?>> builders) {
		if (builders == null || builders.isEmpty())
			return null;
		List<T> elements = new ArrayList<T>(builders.size());
		for (FormFieldElementBuilder<?> builder : builders) {
			elements.add((T)buildElement(builder));
		}
		return Collections.unmodifiableList(elements);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Option> List<T> buildOptions(List<OptionBuilder<?>> builders) {
		if (builders == null || builders.isEmpty())
			return null;
		List<T> options = new ArrayList<T>(builders.size());
		for (OptionBuilder<?> builder : builders) {
			options.add((T)builder.build());
		}
		return Collections.unmodifiableList(options);
	}
	
	protected abstract ConstraintBuilder<?> constraintBuilder(Constraint constraint);
	
	protected abstract FormFieldElementBuilder<?> elementBuilder(FormFieldElement element);
	
	protected List<FormFieldElementBuilder<?>> elementBuilders(List<FormFieldElement> elements) {
		if (elements != null && !elements.isEmpty()) {
			List<FormFieldElementBuilder<?>> builders = new ArrayList<FormFieldElementBuilder<?>>(elements.size());
			for (FormFieldElement element : elements) {
				FormFieldElementBuilder<?> builder = elementBuilder(element);
				builders.add(builder);
			}
			return builders;
		} 
		return null;
	}
	
	protected abstract OptionBuilder<?> optionBuilder(Option option);
	
	private List<ConstraintBuilder<?>> constraintBuilders(List<? extends Constraint> constraints) {
		if (constraints != null && !constraints.isEmpty()) {
			List<ConstraintBuilder<?>> builders = new ArrayList<ConstraintBuilder<?>>(constraints.size());
			for (Constraint constraint : constraints) {
				builders.add(constraintBuilder(constraint));
			}
			return builders;
		}
		return null;
	}
	
	private List<OptionBuilder<?>> optionBuilders(List<Option> options) {

//		if (optionResolver != null) {
//			List<? extends OptionContract> resolvedOptionContracts = optionResolver.getOptions(optionProvider);
//			if (resolvedOptionContracts != null && !resolvedOptionContracts.isEmpty())
//				options = resolvedOptionContracts;
//		}
		
		if (options != null && !options.isEmpty()) {
			List<OptionBuilder<?>> builders = new ArrayList<OptionBuilder<?>>(options.size());
			for (Option optionContract : options) {
				builders.add(optionBuilder(optionContract));
			}
			return builders;
		} 
		return null;
	}
	
	private OptionProviderView.Builder optionProviderBuilder(OptionProvider provider) {
		String valueExpression = provider != null ? provider.getValueExpression() : null;
		return valueExpression != null && valueExpression.length() > 0 ? new OptionProviderView.Builder(provider) : null;
	}
	
	public FormFieldBuilder<F> propertyName(String propertyName) {
		this.propertyName = propertyName;
		return this;
	}
	
	public FormFieldBuilder<F> typeAttr(String typeAttr) {
		this.typeAttr = typeAttr;
		return this;
	}
	
	public FormFieldBuilder<F> element(FormFieldElementBuilder<?> builder) {
		if (this.elements == null)
			this.elements = new ArrayList<FormFieldElementBuilder<?>>();
		this.elements.add(builder);
		return this;
	}
	
	public FormFieldBuilder<F> elements(List<FormFieldElementBuilder<?>> builders) {
		if (this.elements == null)
			this.elements = new ArrayList<FormFieldElementBuilder<?>>();
		this.elements.addAll(builders);
		return this;
	}
	
	public FormFieldBuilder<F> label(FormFieldElementView.Builder label) {
		this.label = label;
		return this;
	}
	
	public FormFieldBuilder<F> directions(FormFieldElementView.Builder directions) {
		this.directions = directions;
		return this;
	}
	
	public FormFieldBuilder<F> option(OptionBuilder<?> option) {
		if (this.options == null)
			this.options = new ArrayList<OptionBuilder<?>>();
		this.options.add(option);
		return this;
	}
	
	public FormFieldBuilder<F> options(List<OptionBuilder<?>> options) {
		if (options != null && !options.isEmpty()) {
			if (this.options == null)
				this.options = new ArrayList<OptionBuilder<?>>();
			this.options.addAll(options);
		}
		return this;
	}
	
	public FormFieldBuilder<F> constraint(ConstraintView.Builder constraint) {
		if (this.constraints == null)
			this.constraints = new ArrayList<ConstraintBuilder<?>>();
		this.constraints.add(constraint);
		return this;
	}
	
	public FormFieldBuilder<F> constraints(List<ConstraintView.Builder> constraints) {
		if (this.constraints == null)
			this.constraints = new ArrayList<ConstraintBuilder<?>>();
		this.constraints.addAll(constraints);
		return this;
	}
	
	public FormFieldBuilder<F> editable(Boolean editable) {
		this.editable = editable;
		return this;
	}
	
	public FormFieldBuilder<F> required(Boolean required) {
		this.required = required;
		return this;
	}
	
	public FormFieldBuilder<F> restricted(Boolean restricted) {
		this.restricted = restricted;
		return this;
	}
	
	public FormFieldBuilder<F> message(String message) {
		this.message = message;
		return this;
	}
	
	public FormFieldBuilder<F> messageType(String messageType) {
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

	public List<OptionBuilder<?>> getOptions() {
		return this.options;
	}
	
	public Boolean getRestricted() {
		return this.restricted;
	}
	
	public List<FormFieldElementBuilder<?>> getElements() {
		return this.elements;
	}

	@SuppressWarnings("unchecked")
	public <T extends FormFieldElement> FormFieldElementBuilder<T> getLabel() {
		return (FormFieldElementBuilder<T>) label;
	}

	public FormFieldElementBuilder<?> getDirections() {
		return directions;
	}

	public List<ConstraintBuilder<?>> getConstraints() {
		return constraints;
	}

	public Boolean getEditable() {
		return editable;
	}

	public Boolean getRequired() {
		return required;
	}

	public String getMessage() {
		return message;
	}

	public String getMessageType() {
		return messageType;
	}
}
