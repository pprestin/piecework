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

/**
 * @author James Renfro
 */
public class SectionRecord implements Section, Serializable {

	private static final long serialVersionUID = -1248427736902647987L;
	
	private String id;
	private String name;
	private String label;
	private String description;
	private List<FormFieldRecord> fields;
	private String actionValue;
	
	public SectionRecord() {
		
	}
	
	public SectionRecord(Section contract) {
		this.id = contract.getId();
		this.name = contract.getName();
		this.label = contract.getLabel();
		this.description = contract.getDescription();
		List<? extends FormField> formFieldContracts =
				contract.getFields();
		if (formFieldContracts != null && !formFieldContracts.isEmpty()) {
			this.fields = new ArrayList<FormFieldRecord>(formFieldContracts.size());
			for (FormField formFieldContract : formFieldContracts) {
				this.fields.add(new FormFieldRecord(formFieldContract));
			}
		}
		this.actionValue = contract.getActionValue();
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
	public String getSelected() {
		return null;
	}

	@Override
	public String getSelectable() {
		return null;
	}

	@Override
	public String getReadOnly() {
		return null;
	}

}
