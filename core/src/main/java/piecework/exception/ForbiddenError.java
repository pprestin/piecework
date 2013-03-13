/*
 * Copyright 2011 University of Washington
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
 * @author James Renfro
 */
public class ForbiddenError extends StatusCodeError {

	public static final int FORBIDDEN_CODE = 403;
	
	private static final long serialVersionUID = 1L;

	public ForbiddenError() {
		super(FORBIDDEN_CODE);
	}
	
	public ForbiddenError(String resourceKey, Object... messageArguments) {
		super(FORBIDDEN_CODE, resourceKey, messageArguments);
	}
	
	public ForbiddenError(Throwable cause) {
		super(cause, FORBIDDEN_CODE);
	}
	
}
