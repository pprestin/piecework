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
 * @author James Renfro
 */
public class UnauthorizedError extends StatusCodeError {

	private static final long serialVersionUID = -2712741734573548504L;

	public static final int UNAUTHORIZED_CODE = 401;
	
	public UnauthorizedError() {
		super(UNAUTHORIZED_CODE);
	}
	
	public UnauthorizedError(String resourceKey, Object... messageArguments) {
		super(UNAUTHORIZED_CODE, resourceKey, messageArguments);
	}
	
	public UnauthorizedError(Throwable cause) {
		super(cause, UNAUTHORIZED_CODE);
	}
	
}
