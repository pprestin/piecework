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
package piecework.exception;

import piecework.common.view.ValidationResultList;



/**
 * This is an exception class to wrap bad request exceptions.
 * 
 * @author James Renfro
 */
public class BadRequestError extends StatusCodeError {

	private static final long serialVersionUID = -8223072822788955978L;
	
	public static final int BAD_REQUEST_ERROR_CODE = 400;
	
	public BadRequestError() {
		super(BAD_REQUEST_ERROR_CODE);
	}
	
	public BadRequestError(String resourceKey, Object... messageArguments) {
		super(BAD_REQUEST_ERROR_CODE, resourceKey, messageArguments);
	}
	
	public BadRequestError(Throwable cause) {
		super(cause, BAD_REQUEST_ERROR_CODE);
	}
	
	public BadRequestError(Object entity) {
		this();
		setEntity(entity);
	}
	
	public BadRequestError(ValidationResultList entity) {
		this();
		setEntity(entity);
	}
	
}
