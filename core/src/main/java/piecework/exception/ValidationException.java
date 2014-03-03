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
package piecework.exception;

import piecework.form.legacy.AttributeValidation;

import java.util.List;

/**
 * @author James Renfro
 */
public class ValidationException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private final List<AttributeValidation> validations;
	
	public ValidationException(final List<AttributeValidation> validations) {
		this.validations = validations;
	}

	public List<AttributeValidation> getValidations() {
		return validations;
	}
	
}
