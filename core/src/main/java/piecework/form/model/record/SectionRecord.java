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

import piecework.form.model.FormField;
import piecework.form.model.Section;
import piecework.form.model.builder.FormFieldBuilder;
import piecework.form.model.builder.SectionBuilder;
import piecework.form.model.view.FormFieldView;

/**
 * @author James Renfro
 */
public class SectionRecord implements Section, Serializable {

	private static final long serialVersionUID = -1248427736902647987L;
	
	private String id;
	private String name;
	private String label;
	private String description;
//	@DBRef
	private List<FormFieldRecord> fields;
	private String actionValue;
	
	private SectionRecord() {
		
	}
	
	private SectionRecord(SectionRecord.Builder section) {
		this.id = section.getId();
		this.name = section.getName();
		this.label = section.getLabel();
		this.description = section.getDescription();
		List<FormFieldBuilder<?>> fields = section.getFields();
		if (fields != null && !fields.isEmpty()) {
			this.fields = new ArrayList<FormFieldRecord>(fields.size());
			for (FormFieldBuilder<?> field : fields) {
				this.fields.add((FormFieldRecord) field.build(false));
			}
		}
		this.actionValue = section.getActionValue();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setFields(List<FormFieldRecord> fields) {
		this.fields = fields;
	}

	public String getActionValue() {
		return actionValue;
	}

	public void setActionValue(String actionValue) {
		this.actionValue = actionValue;
	}

	@Override
	public <F extends FormField> List<F> getFields() {
		return (List<F>) fields;
	}

	@Override
	public Boolean getSelected() {
		return null;
	}

	@Override
	public Boolean getVisible() {
		return null;
	}

	@Override
	public Boolean getEditable() {
		return null;
	}

	public final static class Builder extends SectionBuilder<SectionRecord> {

		public Builder() {
			super();
		}
		
		public Builder(Section section) {
			super(section);
		}

		@Override
		public SectionRecord build() {
			return new SectionRecord(this);
		}
		
		@SuppressWarnings("unchecked")
		protected <F extends FormField> FormFieldBuilder<F> fieldBuilder(F field) {
			return (FormFieldBuilder<F>) new FormFieldRecord.Builder(field);
		}
	}
}
