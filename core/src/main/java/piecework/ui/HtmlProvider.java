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
import java.io.ByteArrayOutputStream;
import java.io.File;
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
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import piecework.common.model.User;
import piecework.common.view.SearchResults;
import piecework.identity.InternalUserDetails;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

/**
 * @author James Renfro
 */
@Produces("text/html")
@Provider
@Service
public class HtmlProvider extends AbstractConfigurableProvider implements MessageBodyWriter<Object> {

	private static final Logger LOG = Logger.getLogger(HtmlProvider.class);

    @Autowired
    private Environment environment;

	@Autowired
	private JacksonJsonProvider jsonProvider;
	
	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return hasTemplateResource(type);
	}

	@Override
	public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		Resource resource = getTemplateResource(type, t);
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
	public void writeTo(Object t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException,
			WebApplicationException {
		
		String userId = null;
		String userName = null;
		
		SecurityContext context = SecurityContextHolder.getContext();
		Authentication authentication = context.getAuthentication();
		
		Object principal = authentication != null ? authentication.getPrincipal() : null;
		
		if (principal != null && principal instanceof InternalUserDetails) {
			InternalUserDetails userDetails = InternalUserDetails.class.cast(principal);
			userId = userDetails.getUsername();
			userName = userDetails.getDisplayName();
		}
		
		Resource template = getTemplateResource(type, t);
		if (template.exists()) {
            String applicationTitle = environment.getProperty("application.name");
            String assetsUrl = environment.getProperty("ui.static.urlbase");

			InputStream input = new SequenceInputStream(new ByteArrayInputStream("{{=<% %>=}}".getBytes()), template.getInputStream());
			try {
				MustacheFactory mf = new DefaultMustacheFactory();
			    Mustache mustache = mf.compile(new InputStreamReader(input), "page");

			    User user = new User.Builder().visibleId(userId).displayName(userName).build(null);
                PageContext pageContext = new PageContext.Builder()
                        .applicationTitle(applicationTitle)
                        .assetsUrl(assetsUrl)
                        .resource(t)
                        .user(user)
                        .build();

                OutputStream jsonStream = new ByteArrayOutputStream();
                jsonProvider.writeTo(pageContext, PageContext.class, genericType, annotations, mediaType, httpHeaders, jsonStream);
                String json = jsonStream.toString();

			    mustache.execute(new PrintWriter(entityStream), new JsonContext(json)).flush();
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
	
	private boolean hasTemplateResource(Class<?> type) {
		if (type.equals(SearchResults.class))
			return true;
		
		Resource resource = getTemplateResource(type, null);
		return resource != null && resource.exists();
	}
	
	private Resource getTemplateResource(Class<?> type, Object t) {
        String templatesDirectory = environment.getProperty("templates.directory");

		StringBuilder templateNameBuilder = new StringBuilder(type.getSimpleName());
		
		if (type.equals(SearchResults.class)) {
			SearchResults results = SearchResults.class.cast(t);
			templateNameBuilder.append(".").append(results.getResourceName());
		}
			
		templateNameBuilder.append(".template.html");
		
		String templateName = templateNameBuilder.toString();
		Resource resource = null;
		if (templatesDirectory != null && !templatesDirectory.equals("${templates.directory}"))
			resource = new FileSystemResource(templatesDirectory + File.separator + templateName);
		else
			resource = new ClassPathResource("META-INF/piecework/templates/" + templateName);
		return resource;
	}

}
