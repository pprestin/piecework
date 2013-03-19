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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import piecework.form.model.Option;

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
		this.id = builder.id;
		this.value = builder.value;
		this.selected = builder.selected ? "selected" : null;
		this.label = builder.label;
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
	
	/*
	 * Fluent builder class, as per Joshua Bloch's Effective Java
	 */
	public final static class Builder {
		private final String id;
		private String value;
		private boolean selected;
		private String label;
		
		public Builder() {
			this((String)null);
		}
		
		public Builder(String id) {
			this.id = id;
		}
		
		public Builder(Option contract) {
			this.id = contract.getId();
			this.value = contract.getValue();
			this.label = contract.getLabel();
			this.selected = contract.getSelected() != null && !contract.getSelected().equalsIgnoreCase("false");
		}
		
		public OptionView build() {
			return new OptionView(this);
		}
		
		public OptionView.Builder value(String value) {
			this.value = value;
			return this;
		}
		
		public OptionView.Builder selected(boolean selected) {
			this.selected = selected;
			return this;
		}
		
		public OptionView.Builder label(String label) {
			this.label = label;
			return this;
		}
		
		public String getValue() {
			return this.value;
		}
		
	}

}
