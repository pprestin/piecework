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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import piecework.form.model.FormFieldElement;

/**
 * @author James Renfro
 */
@XmlRootElement(name = FormFieldElementView.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = FormFieldElementView.Constants.TYPE_NAME)
public final class FormFieldElementView implements FormFieldElement {

	@XmlAttribute(name = Attributes.ID)
	@XmlID
	private final String id;
	
	@XmlAttribute(name = Attributes.TAG_NAME)
	private final String tagName;
	
	@XmlAttribute(name = Attributes.CLASS_ATTR)
	private final String classAttr;
	
	@XmlAttribute(name = Attributes.STYLE_ATTR)
	private final String styleAttr;
	
	@XmlAttribute(name = Attributes.TYPE_ATTR)
	private final String typeAttr;
	
	@XmlAttribute(name = Attributes.TITLE_ATTR)
	private final String titleAttr;
	
	@XmlAttribute(name = Attributes.VALUE_ATTR)
	private final String valueAttr;

	@XmlAttribute(name = Attributes.MAX_SIZE)
	private final String maxSize;
	
	@XmlAttribute(name = Attributes.MIN_SIZE)
	private final String minSize;
	
	@XmlAttribute(name = Attributes.DISPLAY_SIZE)
	private final String displaySize;
	
	@XmlValue
	private final String text;
	
	@SuppressWarnings("unused")
	private FormFieldElementView() {
		this(new FormFieldElementView.Builder());
	}
	
	public FormFieldElementView(FormFieldElementView.Builder builder) {
		this.id = builder.id;
		this.text = builder.text;
		this.tagName = builder.tagName;
		this.classAttr = builder.classAttr;
		this.styleAttr = builder.styleAttr;
		this.typeAttr = builder.typeAttr;
		this.titleAttr = builder.titleAttr;
		this.valueAttr = builder.valueAttr;
		this.maxSize = builder.maxSize;
		this.minSize = builder.minSize;
		this.displaySize = builder.displaySize;
	}
	
	public String getId() {
		return id;
	}

	public String getTagName() {
		return tagName;
	}

	public String getClassAttr() {
		return classAttr;
	}

	public String getStyleAttr() {
		return styleAttr;
	}

	public String getTitleAttr() {
		return titleAttr;
	}
	
	public String getTypeAttr() {
		return typeAttr;
	}

	public String getValueAttr() {
		return valueAttr;
	}

	public String getMaxSize() {
		return maxSize;
	}

	public String getMinSize() {
		return minSize;
	}

	public String getDisplaySize() {
		return displaySize;
	}

	public String getText() {
		return text;
	}

	static class Attributes {
		final static String CLASS_ATTR = "class";
		final static String DISPLAY_SIZE = "displaySize";
		final static String ID = "id";
		final static String MAX_SIZE = "maxSize";
		final static String MIN_SIZE = "minSize";
		final static String STYLE_ATTR = "style";
		final static String TAG_NAME = "tagName";
		final static String TITLE_ATTR = "title";
		final static String TYPE_ATTR = "type";
		final static String VALUE_ATTR = "value";
	}

	static class Constants {
		public static final String ROOT_ELEMENT_NAME = "formFieldElement";
		public static final String TYPE_NAME = "FormFieldElementType";
	}
	
	/*
	 * Fluent builder class, as per Joshua Bloch's Effective Java
	 */
	public final static class Builder {
	
		private final String id;
		private String tagName;
		private String classAttr;
		private String styleAttr;
		private String typeAttr;
		private String titleAttr;
		private String valueAttr;
		private String maxSize;
		private String minSize;
		private String displaySize;
		private String text;
		
		public Builder() {
			this((String)null);
		}
		
		public Builder(String id) {
			this.id = id;
		}
		
		public Builder(FormFieldElement element) {
			this.id = element.getId();
			this.tagName = element.getTagName();
			this.classAttr = element.getClassAttr();
			this.styleAttr = element.getStyleAttr();
			this.titleAttr = element.getTitleAttr();
			this.typeAttr = element.getTypeAttr();
			this.valueAttr = element.getValueAttr();
			this.maxSize = element.getMaxSize();
			this.minSize = element.getMinSize();
			this.displaySize = element.getDisplaySize();
			this.text = element.getText();
		}
		
		public FormFieldElementView build() {
			return new FormFieldElementView(this);
		}
		
		public FormFieldElementView.Builder tagName(String tagName) {
			this.tagName = tagName;
			return this;
		}
		
		public FormFieldElementView.Builder classAttr(String classAttr) {
			this.classAttr = classAttr;
			return this;
		}

		public FormFieldElementView.Builder styleAttr(String styleAttr) {
			this.styleAttr = styleAttr;
			return this;
		}
		
		public FormFieldElementView.Builder titleAttr(String titleAttr) {
			this.titleAttr = titleAttr;
			return this;
		}
		
		public FormFieldElementView.Builder typeAttr(String typeAttr) {
			this.typeAttr = typeAttr;
			return this;
		}
		
		public FormFieldElementView.Builder valueAttr(String valueAttr) {
			this.valueAttr = valueAttr;
			return this;
		}
		
		public FormFieldElementView.Builder maxSize(String maxSize) {
			this.maxSize = maxSize;
			return this;
		}
		
		public FormFieldElementView.Builder minSize(String minSize) {
			this.minSize = minSize;
			return this;
		}
		
		public FormFieldElementView.Builder displaySize(String displaySize) {
			this.displaySize = displaySize;
			return this;
		}
		
		public FormFieldElementView.Builder text(String text) {
			this.text = text;
			return this;
		}
		
		public String getMaxSize() {
			return this.maxSize;
		}
		
		public String getMinSize() {
			return this.minSize;
		}
	}
}
