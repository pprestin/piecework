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
package piecework.ui.visitor;

import org.apache.cxf.jaxrs.provider.AbstractConfigurableProvider;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import piecework.exception.NotFoundError;
import piecework.identity.IdentityHelper;
import piecework.model.Explanation;
import piecework.service.UserInterfaceService;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author James Renfro
 */
@Produces("text/html")
@Provider
public class HtmlProvider extends AbstractConfigurableProvider implements MessageBodyWriter<Object> {

	private static final Logger LOG = Logger.getLogger(HtmlProvider.class);

    @Autowired
    private IdentityHelper helper;

    @Context
    private javax.servlet.ServletContext servletContext;

    @Autowired
    UserInterfaceService userInterfaceService;

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return !StreamingOutput.class.isAssignableFrom(type) && userInterfaceService.hasPage(type);
	}

	@Override
	public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return userInterfaceService.getPageSize(type, t);
	}

	@Override
	public void writeTo(Object t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException,
			WebApplicationException {

        try {
            StreamingOutput streamingOutput;

            if (type.equals(Explanation.class))
                streamingOutput = userInterfaceService.getExplanationAsStreaming(servletContext, Explanation.class.cast(t));
            else
                streamingOutput = userInterfaceService.getDefaultPageAsStreaming(type, t);

            if (!userInterfaceService.servePage(streamingOutput, entityStream)) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
        } catch (NotFoundError nfe) {
            throw new IOException();
        }
	}

}
