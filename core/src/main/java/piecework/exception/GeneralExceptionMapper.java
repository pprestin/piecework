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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import piecework.common.view.Explanation;


/**
 * This provider maps runtime exceptions to responses. 
 * 
 * @author James Renfro
 */
@Provider
public class GeneralExceptionMapper implements ExceptionMapper<RuntimeException> {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(GeneralExceptionMapper.class);
	
	/**
	 * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
	 */
	public Response toResponse(RuntimeException exception) {
		LOG.info("Uncaught exception. Sending exception message to client with status " + Status.INTERNAL_SERVER_ERROR + " and message " + exception.getMessage(), exception);
		exception.printStackTrace();
		Explanation explanation = new Explanation();
		explanation.setMessage(exception.getMessage());
		return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.getMessage()).build();
	}

}
