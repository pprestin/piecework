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

import org.apache.cxf.interceptor.security.AccessDeniedException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * @author James Renfro
 */
public class AccessDeniedExceptionMapper implements ExceptionMapper<org.apache.cxf.interceptor.security.AccessDeniedException> {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AccessDeniedExceptionMapper.class);
	
	@Override
	public Response toResponse(AccessDeniedException exception) {
		StatusCodeError error = new StatusCodeError(401);
		if (LOG.isDebugEnabled())
			LOG.debug("Parsing a CXF access denied exception", exception);
		
		return ErrorResponseBuilder.buildErrorResponse(error);
	}

}
