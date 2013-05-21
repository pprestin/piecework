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
package piecework.form.legacy;

import java.io.Serializable;
import java.util.List;

/**
 * @author James Renfro
 */
public class AttributeValidation implements Serializable {

	private static final long serialVersionUID = 2839610389625237496L;

	public enum Status { ERROR("error"), WARNING("warning"), SUCCESS("success"), NONE("");
	
		private String text;
	
		private Status(String text) {
			this.text = text;
		}
		
		public String toString() {
			return text;
		}
	
	};
	
	private final String attributeName;
	private final Status status;
	private final List<String> values;
	private final String message;
	private final boolean isRestricted;
	private final boolean isText;
	private final boolean isUnchanged;
	private final String propertyId;
	
	public AttributeValidation(Status status, String attributeName, 
			List<String> values, String message, boolean isRestricted, boolean isText, boolean isUnchanged) {
		this(status, attributeName, values, message, isRestricted, isText, isUnchanged, null);
	}
	
	public AttributeValidation(Status status, String attributeName, 
			List<String> values, String message, boolean isRestricted, boolean isText, boolean isUnchanged, String propertyId) {
		this.attributeName = attributeName;
		this.status = status;
		this.values = values;
		this.message = message;
		this.isRestricted = isRestricted;
		this.isText = isText;
		this.isUnchanged = isUnchanged;
		this.propertyId = propertyId;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public Status getStatus() {
		return status;
	}

	public List<String> getValues() {
		return values;
	}

	public String getMessage() {
		return message;
	}

	public boolean isRestricted() {
		return isRestricted;
	}

	public String getPropertyId() {
		return propertyId;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder("Validation Result (").append(status).append(") ");
		
		builder.append(attributeName);
		
		if (message != null)
			builder.append(": ").append(message);
		
		return builder.toString();
	}

	public boolean isText() {
		return isText;
	}

	public boolean isUnchanged() {
		return isUnchanged;
	}
	
}
