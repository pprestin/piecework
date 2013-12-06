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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.model.Explanation;
import piecework.service.UserInterfaceService;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.ext.ExceptionMapper;
import java.io.IOException;

/**
 * This provider maps StatusCodeError exceptions to 
 * responses that can be sent back to the client. 
 * 
 * @author James Renfro
 * @since 1.0.2.1
 * @date 8/18/2010
 */
@Service
public class StatusCodeErrorMapper implements ExceptionMapper<StatusCodeError> {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(StatusCodeErrorMapper.class);

    @Autowired
    private UserInterfaceService userInterfaceService;

	/**
	 * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
	 */
	public Response toResponse(StatusCodeError error) {
        // If the application is trying to return an object then don't get in the way
        // and return HTML
        if (error.getEntity() != null)
            return Response.status(error.getStatusCode()).entity(error.getEntity()).build();

        if (error instanceof BadRequestError)
            LOG.warn("Bad request error");
        else if (LOG.isInfoEnabled())
            LOG.info("Parsing a status code error", error);

        int statusCode = error.getStatusCode();

        if (statusCode != 400) {
            LOG.warn("Building response for status code error ", error);
        }

		Explanation explanation = ErrorResponseBuilder.buildExplanation(statusCode, error.getLocalizedMessage(), error.getMessageDetail());
        return Response.status(statusCode).entity(explanation).build();

//        try {
//            StreamingOutput streamingOutput = userInterfaceService.getExplanationAsStreaming(explanation);
//            return Response.status(error.getStatusCode()).entity(streamingOutput).type(MediaType.TEXT_HTML_TYPE).build();
//        } catch (NotFoundError nfe) {
//            return Response.status(Response.Status.NOT_FOUND).build();
//        } catch (IOException ioe) {
//            LOG.error("Unable to get explanation page as a streaming output", ioe);
//        }
//        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	}
	
}
