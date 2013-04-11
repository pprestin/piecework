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

import piecework.form.model.Option;
import piecework.form.model.builder.OptionBuilder;

/**
 * @author James Renfro
 */
public class OptionRecord implements Option {

	private String id;
	private String label;
	private String value;
	
	private OptionRecord() {
		
	}
	
	private OptionRecord(OptionRecord.Builder builder) {
		this.id = builder.getId();
		this.label = builder.getLabel();
		this.value = builder.getValue();
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public String getSelected() {
		return null;
	}

	public final static class Builder extends OptionBuilder<OptionRecord> {

		public Builder() {
			super();
		}
		
		public Builder(Option option) {
			super(option);
		}


		@Override
		public OptionRecord build() {
			return new OptionRecord(this);
		}
		
	}
	
}
