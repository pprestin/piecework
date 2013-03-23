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

import piecework.form.model.FormFieldElement;

/**
 * @author James Renfro
 */
public abstract class FormFieldElementBuilder<E extends FormFieldElement> extends Builder {

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
	
	public FormFieldElementBuilder() {
		super();
	}
	
	public FormFieldElementBuilder(String id) {
		super(id);
	}
	
	public FormFieldElementBuilder(FormFieldElement element) {
		super(element.getId());
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
	
	public abstract E build();
	
	public FormFieldElementBuilder<E> tagName(String tagName) {
		this.tagName = tagName;
		return this;
	}
	
	public FormFieldElementBuilder<E> classAttr(String classAttr) {
		this.classAttr = classAttr;
		return this;
	}

	public FormFieldElementBuilder<E> styleAttr(String styleAttr) {
		this.styleAttr = styleAttr;
		return this;
	}
	
	public FormFieldElementBuilder<E> titleAttr(String titleAttr) {
		this.titleAttr = titleAttr;
		return this;
	}
	
	public FormFieldElementBuilder<E> typeAttr(String typeAttr) {
		this.typeAttr = typeAttr;
		return this;
	}
	
	public FormFieldElementBuilder<E> valueAttr(String valueAttr) {
		this.valueAttr = valueAttr;
		return this;
	}
	
	public FormFieldElementBuilder<E> maxSize(String maxSize) {
		this.maxSize = maxSize;
		return this;
	}
	
	public FormFieldElementBuilder<E> minSize(String minSize) {
		this.minSize = minSize;
		return this;
	}
	
	public FormFieldElementBuilder<E> displaySize(String displaySize) {
		this.displaySize = displaySize;
		return this;
	}
	
	public FormFieldElementBuilder<E> text(String text) {
		this.text = text;
		return this;
	}
	
	public String getMaxSize() {
		return this.maxSize;
	}
	
	public String getMinSize() {
		return this.minSize;
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

	public String getTypeAttr() {
		return typeAttr;
	}

	public String getTitleAttr() {
		return titleAttr;
	}

	public String getValueAttr() {
		return valueAttr;
	}

	public String getDisplaySize() {
		return displaySize;
	}

	public String getText() {
		return text;
	}
}
