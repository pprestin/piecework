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
package piecework.ui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.SequenceInputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.cxf.jaxrs.provider.AbstractConfigurableProvider;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

/**
 * @author James Renfro
 */
@Produces("text/html")
@Provider
@Service
public class MustacheHtmlTransformer<T> extends AbstractConfigurableProvider implements MessageBodyWriter<T> {

	private static final Logger LOG = Logger.getLogger(MustacheHtmlTransformer.class);
	
	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		Resource resource = getResource(type);
		return resource != null && resource.exists();
	}

	@Override
	public long getSize(T t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		Resource resource = getResource(type);
		long size = 0;
		if (resource.exists()) {
			try {
				size = resource.getFile().length();
			} catch (IOException e) {
				LOG.error("Unable to determine size of template for " + type.getSimpleName(), e);
			}
		}
		return size;
	}

	@Override
	public void writeTo(T t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException,
			WebApplicationException {
		
		Resource resource = getResource(type);
		if (resource.exists()) {
			InputStream input = new SequenceInputStream(new ByteArrayInputStream("{{=<% %>=}}".getBytes()), resource.getInputStream());
			try {
				MustacheFactory mf = new DefaultMustacheFactory();
			    Mustache mustache = mf.compile(new InputStreamReader(input), getTemplateName(type));
			    mustache.execute(new PrintWriter(entityStream), t).flush();
			} catch (IOException e) {
				LOG.error("Unable to determine size of template for " + type.getSimpleName(), e);
			} finally {
				input.close();
				entityStream.close();
			}
		} else {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}	
	}
	
	private String getTemplateName(Class<?> type) {
		return new StringBuilder(type.getSimpleName()).append(".template").toString();
	}

	private Resource getResource(Class<?> type) {
		String templateName = new StringBuilder(type.getSimpleName()).append(".template.html").toString();
		Resource resource = new ClassPathResource("META-INF/piecework/templates/" + templateName);
		return resource;
	}

}
