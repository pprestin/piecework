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
package piecework.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * @author James Renfro
 */
@XmlRootElement(name = ValidationResult.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = ValidationResult.Constants.TYPE_NAME)
public class ValidationResult implements Serializable {

	private static final long serialVersionUID = 2651603055420420813L;
	
	@XmlElement(name = ValidationResult.Elements.TYPE)
	private final String type;
	
	@XmlElement(name = ValidationResult.Elements.PROPERTY_NAME)
	private final String propertyName;
		
	@XmlElement(name = ValidationResult.Elements.MESSAGE)
	private final String message;
	
	@SuppressWarnings("unused")
	private ValidationResult() {
		this.type = null;
		this.propertyName = null;
		this.message = null;
	}
	
	public ValidationResult(String type, String propertyName, String message) {
		this.type = type;
		this.propertyName = propertyName;
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}

	public String getPropertyName() {
		return propertyName;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append(propertyName != null ? propertyName : "Property name not found")
			.append(": ").append(message);
		
		return builder.toString();
	}
	
	static class Constants {
		public static final String ROOT_ELEMENT_NAME = "validationResult";
		public static final String TYPE_NAME = "ValidationResultType";
	}
	
	static class Elements {
		final static String MESSAGE = "message";
		final static String PROPERTY_NAME = "propertyName";
		final static String TYPE = "type";
	}

	public String getType() {
		return type;
	}
	
}
