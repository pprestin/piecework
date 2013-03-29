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
package piecework.form.model.view;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import piecework.form.model.FormFieldElement;
import piecework.form.model.builder.FormFieldElementBuilder;

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
		this.id = builder.getId();
		this.text = builder.getText();
		this.tagName = builder.getTagName();
		this.classAttr = builder.getClassAttr();
		this.styleAttr = builder.getStyleAttr();
		this.typeAttr = builder.getTypeAttr();
		this.titleAttr = builder.getTitleAttr();
		this.valueAttr = builder.getValueAttr();
		this.maxSize = builder.getMaxSize();
		this.minSize = builder.getMinSize();
		this.displaySize = builder.getDisplaySize();
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
	
	public final static class Builder extends FormFieldElementBuilder<FormFieldElementView> {
	
		public Builder() {
			super();
		}
		
		public Builder(FormFieldElement element) {
			super(element);
		}
		
		public FormFieldElementView build() {
			return new FormFieldElementView(this);
		}

	}
}
