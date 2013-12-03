/*
 * Copyright 2010 University of Washington
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
package piecework.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Simple wrapper class on a Map<String, List<String>> that also can contains a previous version. 
 * 
 * @author James Renfro
 */
@Deprecated
public class PropertyValueReader {

	private final Map<String, List<String>> formData;
	private String title;
	private final PropertyValueReader previous;
	
	public PropertyValueReader(Map<String, List<String>> formData) {
		this(formData, null, null);
	}
	
	public PropertyValueReader(Map<String, List<String>> formData, String title) {
		this(formData, title, null);
	}
	
	public PropertyValueReader(Map<String, List<String>> formData, String title, PropertyValueReader previous) {
		this.formData = formData;
		this.title = title;
		this.previous = previous;
	}
	
	public PropertyValueReader(PropertyValueReader delegate, PropertyValueReader previous) {
		this.formData = delegate.formData;
		this.title = delegate.title;
		this.previous = previous;
	}
	
	public String getFirstValue(String propertyName) {
		List<String> values = getValuesAsStrings(propertyName);
		return values != null && !values.isEmpty() ? values.iterator().next() : null;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getValuesAsStrings(String propertyName) {	
		return (List<String>) (formData != null ? formData.get(propertyName) : Collections.emptyList());
	}
	
	public boolean hasPreviousValue(String propertyName) {
		List<String> previousValues = previous != null ? previous.getValuesAsStrings(propertyName) : null;
		
		return previousValues != null && !previousValues.isEmpty() && !StringUtils.isEmpty(previousValues.iterator().next());
	}

	public List<String> getPreviousValues(String propertyName) {
		return previous != null ? previous.getValuesAsStrings(propertyName) : null;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
}
