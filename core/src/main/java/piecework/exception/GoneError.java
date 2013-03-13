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
 * This is an exception class to wrap gone exceptions.
 * 
 * @author James Renfro
 * @since 1.0.2.0
 * @added 8/6/2010
 */
public class GoneError extends StatusCodeError {

	private static final long serialVersionUID = -5205410533228945471L;
	
	public static final int GONE_CODE = 410;
	
	public GoneError() {
		super(GONE_CODE);
	}
	
	public GoneError(String resourceKey, Object... messageArguments) {
		super(GONE_CODE, resourceKey, messageArguments);
	}
	
	public GoneError(Throwable cause) {
		super(cause, GONE_CODE);
	}
	
}
