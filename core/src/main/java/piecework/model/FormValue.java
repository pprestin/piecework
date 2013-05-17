/*
 * Copyright 2013 University of Washington
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
package piecework.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import piecework.Sanitizer;
import piecework.common.view.ViewContext;

/**
 * @author James Renfro
 */
@XmlRootElement(name = FormValue.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = FormValue.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FormValue implements Serializable {

	private static final long serialVersionUID = 2672648053686796625L;

	@XmlElement
	private final String name;
	
	@XmlElement
	private final String value;
	
	@XmlElementWrapper(name="values")
	@XmlElement
	private final List<String> values;
	
	private FormValue() {
		this(new FormValue.Builder(), new ViewContext());
	}

	private FormValue(FormValue.Builder builder, ViewContext context) {
		this.name = builder.name;

		String temporarySingle = null;
		List<String> temporaryList = null;
		if (builder.values != null) {
			int size = builder.values.size();
			if (size == 1) 
				temporarySingle = builder.values.get(0);
			else
				temporaryList = Collections.unmodifiableList(builder.values);
		} 
		this.value = temporarySingle;
		this.values = temporaryList;
	}
	
	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public List<String> getValues() {
		return values;
	}

	public final static class Builder {

		private String name;
		private List<String> values;
		
		public Builder() {
			super();
		}

		public Builder(FormValue screen, Sanitizer sanitizer) {
			this.name = sanitizer.sanitize(screen.name);
			
			if (screen.values != null && !screen.values.isEmpty()) {
				this.values = new ArrayList<String>(screen.values.size());
				for (String value : screen.values) {
					this.values.add(sanitizer.sanitize(value));
				}
			}
		}

		public FormValue build() {
			return new FormValue(this, null);
		}

		public FormValue build(ViewContext context) {
			return new FormValue(this, context);
		}
		
		public Builder name(String name) {
			this.name = name;
			return this;
		}
		
		public Builder value(String value) {
			if (this.values == null) 
				this.values = new ArrayList<String>();
			this.values.add(value);
			return this;
		}
	}
	
	public static class Constants {
        public static final String RESOURCE_LABEL = "FormValue";
        public static final String ROOT_ELEMENT_NAME = "formValue";
        public static final String TYPE_NAME = "FormValueType";
    }
	
}
