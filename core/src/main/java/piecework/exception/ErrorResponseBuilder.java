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

import piecework.model.Explanation;

/**
 * This utility class builds an error response for a StatusCodeError.
 * 
 * @author James Renfro
 * @since 1.0.2.1
 * @date 8/18/2010
 */
public class ErrorResponseBuilder {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ErrorResponseBuilder.class);
	
	public static Response buildErrorResponse(StatusCodeError error) {
		int statusCode = error.getStatusCode();	
		
		if (statusCode != 400) {
			LOG.warn("Building response for status code error ", error);
		}
		
		return buildErrorResponse(statusCode, error.getLocalizedMessage(), error.getMessageDetail(), error.getEntity());
	}
	
	public static Response buildErrorResponse(int statusCode, String message, String messageDetail, Object entity) {
		if (LOG.isDebugEnabled())
			LOG.debug("Building response for exception message to client with status " + statusCode + " and message " + message);
		
		if (entity != null)
			return Response.status(statusCode).entity(entity).build();
		
		Explanation explanation = new Explanation();
		explanation.setMessage(message);
		explanation.setMessageDetail(messageDetail);
		return Response.status(statusCode).entity(explanation).build();
	}
	
}
