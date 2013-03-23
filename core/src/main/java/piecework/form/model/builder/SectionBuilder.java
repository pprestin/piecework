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

import piecework.form.model.FormField;
import piecework.form.model.Section;

/**
 * @author James Renfro
 */
public abstract class SectionBuilder<S extends Section> extends Builder {
	private String id;
	private String name;
	private String label;
	private String description;
	private List<FormFieldBuilder<?>> fields;
	private String actionValue;
	private boolean readOnly;
	private boolean selected;
	private boolean visible;
	
	public SectionBuilder() {
		
	}

	public SectionBuilder(Section section) {
		this.id = section.getId();
		this.name = section.getName();
		this.label = section.getLabel();
		this.description = section.getDescription();
		this.fields = fieldBuilders(section.getFields());
		this.actionValue = section.getActionValue();
		this.readOnly = Boolean.valueOf(section.getReadOnly()).booleanValue();
		this.selected = Boolean.valueOf(section.getSelected()).booleanValue();
		this.visible = Boolean.valueOf(section.getSelectable()).booleanValue();
	}
	
	public abstract S build();
	
	protected abstract <F extends FormField> FormFieldBuilder<F> fieldBuilder(F field);
	
	private List<FormFieldBuilder<?>> fieldBuilders(List<FormField> fields) {
		if (fields == null || fields.isEmpty())
			return null;
		
		List<FormFieldBuilder<?>> builders = new ArrayList<FormFieldBuilder<?>>(fields.size());
		for (FormField field : fields) {
			builders.add(fieldBuilder(field));
		}
		return Collections.unmodifiableList(builders);
	}
	
	public SectionBuilder<S> name(String name) {
		this.name = name;
		return this;
	}
	
	public SectionBuilder<S> label(String label) {
		this.label = label;
		return this;
	}
	
	public SectionBuilder<S> description(String description) {
		this.description = description;
		return this;
	}
	
	public SectionBuilder<S> field(FormFieldBuilder<?> field) {
		if (this.fields == null)
			this.fields = new ArrayList<FormFieldBuilder<?>>();
		this.fields.add(field);
		return this;
	}
	
	public SectionBuilder<S> fields(List<FormFieldBuilder<?>> fields) {
		if (fields != null && !fields.isEmpty()) {
			if (this.fields == null)
				this.fields = new ArrayList<FormFieldBuilder<?>>();
			this.fields.addAll(fields);
		}
		return this;
	}
			
	public SectionBuilder<S> actionValue(String actionValue) {
		this.actionValue = actionValue;
		return this;
	}
	
	public List<FormFieldBuilder<?>> getFields() {
		return this.fields;
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	public String getLabel() {
		return label;
	}

	public String getDescription() {
		return description;
	}

	public String getActionValue() {
		return actionValue;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public boolean isSelected() {
		return selected;
	}

	public boolean isVisible() {
		return visible;
	}
}
