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
 * This is an exception class to wrap internal server exceptions.
 * 
 * @author James Renfro
 *
 */
public class InternalServerError extends StatusCodeError {

	private static final long serialVersionUID = 8853634378592315417L;
	public static final int INTERNAL_SERVER_CODE = 500;
	
	public InternalServerError() {
		super(INTERNAL_SERVER_CODE);
	}
	
	public InternalServerError(String resourceKey, Object... messageArguments) {
		super(INTERNAL_SERVER_CODE, resourceKey, messageArguments);
	}
	
	public InternalServerError(Throwable cause) {
		super(cause, INTERNAL_SERVER_CODE);
	}
	
}
