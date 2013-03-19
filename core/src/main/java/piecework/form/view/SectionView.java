/*
 * Copyright 2011 University of Washington
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

import piecework.form.model.Section;

/**
 * @author James Renfro
 */
@XmlRootElement(name = SectionView.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = SectionView.Constants.TYPE_NAME)
public final class SectionView implements Section {

	@XmlAttribute(name=SectionView.Attributes.ID)
	@XmlID
	private final String id;
	
	@XmlElement(name = SectionView.Elements.NAME, required = true)
	private final String name;
	
	@XmlElement(name = SectionView.Elements.LABEL)
	private final String label;
	
	@XmlElement(name = SectionView.Elements.DESCRIPTION)
	private final String description;
	
	@XmlAttribute(name = SectionView.Attributes.SELECTED)
	private final String selected;
	
	@XmlAttribute(name = SectionView.Attributes.SELECTABLE)
	private final String selectable;
	
	@XmlAttribute(name = SectionView.Attributes.READ_ONLY)
	private final String readOnly;
	
	@XmlElementWrapper(name = SectionView.Lists.FIELDS)
	@XmlElementRef
	private final List<FormFieldView> fields;
		
	@XmlElement(name = SectionView.Elements.ACTION_VALUE)
	private final String actionValue;
	
	private SectionView() {
		this(new SectionView.Builder(), false, false, false);
	}
	
	private SectionView(SectionView.Builder builder, boolean readOnly, boolean selected, boolean selectable) {
		this.id = builder.id;
		this.name = builder.name;
		this.label = builder.label;
		this.description = builder.description;
		this.readOnly = Boolean.toString(readOnly);
		this.selected = Boolean.toString(selected);
		this.selectable = Boolean.toString(selectable);
		this.fields = buildFields(builder.fields, false);
		this.actionValue = builder.actionValue;
	}
	
	private static List<FormFieldView> buildFields(List<FormFieldView.Builder> builders, boolean readOnly) {
		if (builders == null || builders.isEmpty())
			return null;
		
		List<FormFieldView> fields = new ArrayList<FormFieldView>(builders.size());
		for (FormFieldView.Builder builder : builders) {
			fields.add(builder.build(readOnly));
		}
		return Collections.unmodifiableList(fields);
	}
	
	public String getActionValue() {
		return actionValue;
	}
	
	public List<FormFieldView> getFields() {
		return fields;
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

	static class Attributes {
		final static String ID = "id";
		final static String READ_ONLY = "readOnly";
		final static String SELECTED = "selected";
		final static String SELECTABLE = "selectable";
	}
	
	public static class Constants {
		public static final String ROOT_ELEMENT_NAME = "section";
		public static final String TYPE_NAME = "SectionType";
	}
	
	static class Elements {
		final static String ACTION_VALUE = "actionValue";
		final static String DESCRIPTION = "description";
		final static String NAME = "name";
		final static String LABEL = "label";
	}
	
	static class Lists {
		static final String FIELDS = "fields";
		static final String PROPERTIES = "properties";
	}
	
	/*
	 * Fluent builder class, as per Joshua Bloch's Effective Java
	 */
	public final static class Builder {
		private String id;
		private String name;
		private String label;
		private String description;
		private List<FormFieldView.Builder> fields;
		private String actionValue;
		private boolean readOnly;
		private boolean selected;
		private boolean visible;
		
		public Builder() {
			
		}

		public Builder(Section transfer) {
			this.id = transfer.getId();
			this.name = transfer.getName();
			this.label = transfer.getLabel();
			this.description = transfer.getDescription();
			List<? extends FormFieldView> formFieldContracts = transfer.getFields();
			if (formFieldContracts != null && !formFieldContracts.isEmpty()) {
				this.fields = new ArrayList<FormFieldView.Builder>(formFieldContracts.size());
				for (FormFieldView formFieldContract : formFieldContracts) {
					this.fields.add(new FormFieldView.Builder(formFieldContract));
				}
			} else {
				this.fields = null;
			}
			this.actionValue = transfer.getActionValue();
			this.readOnly = Boolean.valueOf(transfer.getReadOnly()).booleanValue();
			this.selected = Boolean.valueOf(transfer.getSelected()).booleanValue();
			this.visible = Boolean.valueOf(transfer.getSelectable()).booleanValue();
		}
		
		public SectionView build() {
			return new SectionView(this, readOnly, selected, visible);
		}
		
		public SectionView.Builder id(String id) {
			this.id = id;
			return this;
		}
		
		public SectionView.Builder name(String name) {
			this.name = name;
			return this;
		}
		
		public SectionView.Builder label(String label) {
			this.label = label;
			return this;
		}
		
		public SectionView.Builder description(String description) {
			this.description = description;
			return this;
		}
		
		public SectionView.Builder field(FormFieldView.Builder field) {
			if (this.fields == null)
				this.fields = new ArrayList<FormFieldView.Builder>();
			this.fields.add(field);
			return this;
		}
		
		public SectionView.Builder fields(List<FormFieldView.Builder> fields) {
			if (fields != null && !fields.isEmpty()) {
				if (this.fields == null)
					this.fields = new ArrayList<FormFieldView.Builder>();
				this.fields.addAll(fields);
			}
			return this;
		}
				
		public SectionView.Builder actionValue(String actionValue) {
			this.actionValue = actionValue;
			return this;
		}
		
		public List<FormFieldView.Builder> getFields() {
			return this.fields;
		}
		
		public String getId() {
			return id;
		}
		
		public String getName() {
			return name;
		}

	}

	public String getReadOnly() {
		return readOnly;
	}

	public String getSelected() {
		return selected;
	}

	public String getSelectable() {
		return selectable;
	}
}
