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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import piecework.form.model.Option;
import piecework.form.model.builder.OptionBuilder;

@XmlRootElement(name = OptionView.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = OptionView.Constants.TYPE_NAME)
public class OptionView implements Option {

	@XmlAttribute(name = OptionView.Attributes.ID)
	private final String id;
	@XmlAttribute(name = OptionView.Attributes.VALUE)
	private final String value;
	@XmlAttribute(name = OptionView.Attributes.SELECTED)
	private String selected;
	@XmlValue
	private final String label;
	
	private OptionView() {
		this(new OptionView.Builder());
	}
	
	private OptionView(OptionView.Builder builder) {
		this.id = builder.getId();
		this.value = builder.getValue();
		this.selected = selected(builder.isSelected());
		this.label = builder.getLabel();
	}

	private static String selected(boolean s) {
		return s ? "selected" : null;
	}
	
	public String getId() {
		return id;
	}

	public String getValue() {
		return value;
	}

	public String getLabel() {
		return label;
	}
	
	public synchronized String getSelected() {
		return selected;
	}
	
	public synchronized void setSelected(String selected) {
		this.selected = selected;
	}
	
	static class Constants {
		public static final String ROOT_ELEMENT_NAME = "option";
		public static final String TYPE_NAME = "OptionType";
	}
	
	static class Attributes {
		final static String ID = "id";
		final static String VALUE = "value";
		final static String SELECTED = "selected";
	}
	
	static class Elements {
		
	}

	public final static class Builder extends OptionBuilder<OptionView> {

		public Builder() {
			super();
		}
		
		public Builder(Option option) {
			super(option);
		}


		@Override
		public OptionView build() {
			return new OptionView(this);
		}
		
	}

}
