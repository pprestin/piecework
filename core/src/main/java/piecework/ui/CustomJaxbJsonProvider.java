/*
 * Copyright 2013 University of Washington
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
package piecework.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * This is necessary because of a bug in CXF 2.7.5 -- fixed in 2.7.6
 * @link https://issues.apache.org/jira/browse/CXF-4996
 *
 * @author James Renfro
 */
@Provider
@Consumes(MediaType.WILDCARD) // NOTE: required to support "non-standard" JSON variants
@Produces(MediaType.WILDCARD)
public class CustomJaxbJsonProvider implements MessageBodyReader, MessageBodyWriter {

    private final JacksonJaxbJsonProvider jacksonJaxbJsonProvider;

    public CustomJaxbJsonProvider(JacksonJaxbJsonProvider jacksonJaxbJsonProvider) {
        this.jacksonJaxbJsonProvider = jacksonJaxbJsonProvider;
    }

    public ObjectMapper locateMapper(Class<?> type, MediaType mediaType) {
        return jacksonJaxbJsonProvider.locateMapper(type, mediaType);
    }

    @Override
    public boolean isReadable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return jacksonJaxbJsonProvider.isReadable(type, genericType, annotations, mediaType);
    }

    @Override
    public Object readFrom(Class type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        return jacksonJaxbJsonProvider.readFrom(type, genericType, annotations, mediaType, httpHeaders, entityStream);
    }

    @Override
    public boolean isWriteable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (InputStream.class.isAssignableFrom(type))
            return false;

        return jacksonJaxbJsonProvider.isWriteable(type, genericType, annotations, mediaType);
    }

    @Override
    public long getSize(Object o, Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return jacksonJaxbJsonProvider.getSize(o, type, genericType, annotations, mediaType);
    }

    @Override
    public void writeTo(Object o, Class type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        jacksonJaxbJsonProvider.writeTo(o, type, genericType, annotations, mediaType, httpHeaders, entityStream);
    }
}
