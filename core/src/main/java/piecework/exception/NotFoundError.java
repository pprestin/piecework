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


/**
 * This is an exception class to wrap not found exceptions.
 * 
 * @author James Renfro
 */
public class NotFoundError extends StatusCodeError {

	private static final long serialVersionUID = -4402958687768073463L;
	
	public static final int NOT_FOUND_CODE = 404;
	
	public NotFoundError() {
		super(NOT_FOUND_CODE);
	}
	
	public NotFoundError(String resourceKey, Object... messageArguments) {
		super(NOT_FOUND_CODE, resourceKey, messageArguments);
	}
	
	public NotFoundError(Throwable cause) {
		super(cause, NOT_FOUND_CODE);
	}

}
