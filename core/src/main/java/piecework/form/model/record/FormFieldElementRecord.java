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

import piecework.form.model.FormFieldElement;
import piecework.form.model.builder.FormFieldElementBuilder;

/**
 * @author James Renfro
 */
public class FormFieldElementRecord implements FormFieldElement {

	private String id;
	private String tagName;
	private String classAttr;
	private String styleAttr;
	private String titleAttr;
	private String typeAttr;
	private String valueAttr;
	private String maxSize;
	private String minSize;
	private String displaySize;
	private String text;
	
	private FormFieldElementRecord() {
		
	}
	
	private FormFieldElementRecord(FormFieldElementRecord.Builder builder) {
		this.id = builder.getId();
		this.tagName = builder.getTagName();
		this.classAttr = builder.getClassAttr();
		this.styleAttr = builder.getStyleAttr();
		this.titleAttr = builder.getTitleAttr();
		this.typeAttr = builder.getTypeAttr();
		this.valueAttr = builder.getValueAttr();
		this.maxSize = builder.getMaxSize();
		this.minSize = builder.getMinSize();
		this.displaySize = builder.getDisplaySize();
		this.text = builder.getText();
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTagName() {
		return tagName;
	}
	
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
	
	public String getClassAttr() {
		return classAttr;
	}
	
	public void setClassAttr(String classAttr) {
		this.classAttr = classAttr;
	}
	
	public String getStyleAttr() {
		return styleAttr;
	}
	
	public void setStyleAttr(String styleAttr) {
		this.styleAttr = styleAttr;
	}
	
	public String getTitleAttr() {
		return titleAttr;
	}

	public void setTitleAttr(String titleAttr) {
		this.titleAttr = titleAttr;
	}

	public String getTypeAttr() {
		return typeAttr;
	}
	
	public void setTypeAttr(String typeAttr) {
		this.typeAttr = typeAttr;
	}
	
	public String getValueAttr() {
		return valueAttr;
	}

	public void setValueAttr(String valueAttr) {
		this.valueAttr = valueAttr;
	}

	public String getMaxSize() {
		return maxSize;
	}
	
	public void setMaxSize(String maxSize) {
		this.maxSize = maxSize;
	}
	public String getMinSize() {
		return minSize;
	}
	
	public void setMinSize(String minSize) {
		this.minSize = minSize;
	}
	
	public String getDisplaySize() {
		return displaySize;
	}
	
	public void setDisplaySize(String displaySize) {
		this.displaySize = displaySize;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}

	public final static class Builder extends FormFieldElementBuilder<FormFieldElementRecord> {
		
		public Builder() {
			super();
		}
		
		public Builder(FormFieldElement element) {
			super(element);
		}
		
		public FormFieldElementRecord build() {
			return new FormFieldElementRecord(this);
		}

	}
}
