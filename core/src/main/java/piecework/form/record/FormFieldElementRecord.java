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
package piecework.form.record;

import piecework.form.model.FormFieldElement;

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
	
	public FormFieldElementRecord() {
		
	}
	
	public FormFieldElementRecord(FormFieldElement contract) {
		this.id = contract.getId();
		this.tagName = contract.getTagName();
		this.classAttr = contract.getClassAttr();
		this.styleAttr = contract.getStyleAttr();
		this.titleAttr = contract.getTitleAttr();
		this.typeAttr = contract.getTypeAttr();
		this.valueAttr = contract.getValueAttr();
		this.maxSize = contract.getMaxSize();
		this.minSize = contract.getMinSize();
		this.displaySize = contract.getDisplaySize();
		this.text = contract.getText();
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

}
