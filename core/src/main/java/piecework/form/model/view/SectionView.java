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
package piecework.form.model.view;

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

import piecework.form.model.FormField;
import piecework.form.model.Section;
import piecework.form.model.builder.FormFieldBuilder;
import piecework.form.model.builder.SectionBuilder;

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
		this(new SectionView.Builder());
	}
	
	private SectionView(SectionView.Builder builder) {
		this.id = builder.getId();
		this.name = builder.getName();
		this.label = builder.getLabel();
		this.description = builder.getDescription();
		this.readOnly = Boolean.toString(builder.isReadOnly());
		this.selected = Boolean.toString(builder.isSelected());
		this.selectable = Boolean.toString(builder.isVisible());
		this.fields = buildFields(builder.getFields(), false);
		this.actionValue = builder.getActionValue();
	}
	
	private static List<FormFieldView> buildFields(List<FormFieldBuilder<?>> builders, boolean readOnly) {
		if (builders == null || builders.isEmpty())
			return null;
		
		List<FormFieldView> fields = new ArrayList<FormFieldView>(builders.size());
		for (FormFieldBuilder<?> builder : builders) {
			fields.add(FormFieldView.class.cast(builder.build(readOnly)));
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
	
	public final static class Builder extends SectionBuilder<SectionView> {

		public Builder() {
			super();
		}
		
		public Builder(SectionView section) {
			super(section);
		}

		@Override
		public SectionView build() {
			return new SectionView(this);
		}
		
		@SuppressWarnings("unchecked")
		protected <F extends FormField> FormFieldBuilder<F> fieldBuilder(F field) {
			return (FormFieldBuilder<F>) new FormFieldView.Builder(field);
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
