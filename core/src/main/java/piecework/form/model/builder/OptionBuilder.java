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

import piecework.form.model.Option;

/**
 * @author James Renfro
 */
public abstract class OptionBuilder<O extends Option> extends Builder {

	private String value;
	private boolean selected;
	private String label;
	
	public OptionBuilder() {
		super();
	}
	
	public OptionBuilder(String id) {
		super(id);
	}
	
	public OptionBuilder(Option option) {
		super(option.getId());
		this.value = option.getValue();
		this.label = option.getLabel();
		this.selected = option.getSelected() != null && !option.getSelected().equalsIgnoreCase("false");
	}
	
	public abstract O build();
	
	public OptionBuilder<O> value(String value) {
		this.value = value;
		return this;
	}
	
	public OptionBuilder<O> selected(boolean selected) {
		this.selected = selected;
		return this;
	}
	
	public OptionBuilder<O> label(String label) {
		this.label = label;
		return this;
	}
	
	public String getValue() {
		return this.value;
	}

	public boolean isSelected() {
		return selected;
	}

	public String getLabel() {
		return label;
	}
}
