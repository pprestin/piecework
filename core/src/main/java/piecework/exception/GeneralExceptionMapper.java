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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import piecework.identity.IdentityHelper;
import piecework.model.Explanation;
import piecework.service.UserInterfaceService;

import java.util.List;


/**
 * This provider maps runtime exceptions to responses. 
 * 
 * @author James Renfro
 */
@Provider
@Service
public class GeneralExceptionMapper implements ExceptionMapper<RuntimeException> {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(GeneralExceptionMapper.class);

    @Autowired
    private IdentityHelper helper;

    @Autowired
    private UserInterfaceService userInterfaceService;

    @Context
    private javax.servlet.ServletContext servletContext;

    @Context
    private MessageContext messageContext;

	/**
	 * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
	 */
	public Response toResponse(RuntimeException exception) {
        if (exception instanceof AccessDeniedException) {
            AccessDeniedException accessDeniedException = AccessDeniedException.class.cast(exception);
            Explanation explanation = new Explanation();
            explanation.setMessage("Not authorized");
            explanation.setMessageDetail("You have not been granted the necessary permission to take this action.");
            return Response.status(Status.UNAUTHORIZED).entity(explanation).build();
        }

        String messageHeader = "Internal Server Error";
        String messageDetail = exception.getMessage();
        Status status = null;

        if (StringUtils.isEmpty(messageDetail))
            messageDetail = "The system is unable to complete your request at this time.";

        if (exception instanceof WebApplicationException) {
            WebApplicationException webApplicationException = WebApplicationException.class.cast(exception);
            Response response = webApplicationException.getResponse();
            int statusCode = response != null ? response.getStatus() : Status.INTERNAL_SERVER_ERROR.getStatusCode();
            status = Status.fromStatusCode(statusCode);
        }

        if (status != null)
            messageHeader = status.getReasonPhrase();
        else
            status = Status.INTERNAL_SERVER_ERROR;

		LOG.info("Uncaught exception. Sending exception message to client with status " + Status.INTERNAL_SERVER_ERROR + " and message " + messageDetail, exception);
		exception.printStackTrace();
		Explanation explanation = new Explanation();
		explanation.setMessage(messageHeader);
        explanation.setMessageDetail(messageDetail);

        List<MediaType> mediaTypes = messageContext.getHttpHeaders().getAcceptableMediaTypes();
        MediaType mediaType = mediaTypes != null && !mediaTypes.isEmpty() ? mediaTypes.iterator().next() : MediaType.TEXT_HTML_TYPE;

        if (!mediaType.equals(MediaType.TEXT_HTML_TYPE))
            return Response.status(status).entity(explanation).build();

        StreamingOutput streamingOutput = userInterfaceService.getExplanationAsStreaming(servletContext, explanation);
        return Response.status(status).entity(streamingOutput).type(MediaType.TEXT_HTML_TYPE).build();
	}

}
