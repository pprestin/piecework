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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.cxf.jaxrs.provider.AbstractConfigurableProvider;
import org.apache.log4j.Logger;
import org.htmlcleaner.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import piecework.identity.IdentityDetails;
import piecework.model.Explanation;
import piecework.model.User;
import piecework.model.SearchResults;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author James Renfro
 */
@Produces("text/html")
@Provider
public class HtmlProvider extends AbstractConfigurableProvider implements MessageBodyWriter<Object> {

	private static final Logger LOG = Logger.getLogger(HtmlProvider.class);

    @Autowired
    private Environment environment;

    private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return !type.equals(StreamingPageContent.class) && hasTemplateResource(type);
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
		
		String internalId = null;
        String externalId = null;
		String userName = null;
		
		SecurityContext context = SecurityContextHolder.getContext();
		Authentication authentication = context.getAuthentication();
		
		Object principal = authentication != null ? authentication.getPrincipal() : null;
		
		if (principal != null && principal instanceof IdentityDetails) {
			IdentityDetails userDetails = IdentityDetails.class.cast(principal);
            internalId = userDetails.getInternalId();
            externalId = userDetails.getExternalId();
			userName = userDetails.getDisplayName();
		}

		Resource template = getTemplateResource(type, t);
		if (template.exists()) {

            User user = new User.Builder().userId(internalId).visibleId(externalId).displayName(userName).build(null);

            CleanerProperties cleanerProperties = new CleanerProperties();
            cleanerProperties.setOmitXmlDeclaration(true);
            HtmlCleaner cleaner = new HtmlCleaner(cleanerProperties);

            TagNode node = cleaner.clean(template.getInputStream());
            node.traverse(new HtmlProviderVisitor(t, type, user, objectMapper, environment));

            SimpleHtmlSerializer serializer = new SimpleHtmlSerializer(cleaner.getProperties());
            serializer.writeToStream(node, entityStream);
		} else {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
	}

	private boolean hasTemplateResource(Class<?> type) {
		if (type.equals(SearchResults.class))
			return true;
		if (InputStream.class.isAssignableFrom(type))
            return false;

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
		if (templatesDirectory != null && !templatesDirectory.equals("${templates.directory}")) {
			resource = new FileSystemResource(templatesDirectory + File.separator + templateName);

            if (!resource.exists())
                resource = new FileSystemResource(templatesDirectory + File.separator + "key" + File.separator + templateName);

            if (!resource.exists())
                resource = new FileSystemResource(templatesDirectory + File.separator + "Layout.template.html");

        } else {
			resource = new ClassPathResource("META-INF/piecework/templates/" + templateName);

            if (!resource.exists())
                resource = new ClassPathResource("META-INF/piecework/templates/" + "Layout.template.html");
        }


		return resource;
	}

}
