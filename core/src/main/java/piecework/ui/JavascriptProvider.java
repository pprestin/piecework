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
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.provider.AbstractConfigurableProvider;
import org.apache.log4j.Logger;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import piecework.identity.IdentityDetails;
import piecework.model.Explanation;
import piecework.model.Form;
import piecework.model.SearchResults;
import piecework.model.User;
import piecework.persistence.ContentRepository;
import piecework.service.FormTemplateService;

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
import java.util.HashMap;
import java.util.Map;

/**
 * @author James Renfro
 */
@Produces("text/javascript")
@Provider
@Service
public class JavascriptProvider extends AbstractConfigurableProvider implements MessageBodyWriter<Object> {
    private static final Logger LOG = Logger.getLogger(JavascriptProvider.class);

    @Autowired
    Environment environment;

    @Autowired
    FormTemplateService formTemplateService;

    @Autowired
    JacksonJaxbJsonProvider jacksonJaxbJsonProvider;

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return !type.equals(StreamingPageContent.class) && hasScriptResource(type);
    }

    @Override
    public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        Resource resource = formTemplateService.getExternalScriptResource(type, t);
        long size = 0;
        if (resource.exists()) {
            try {
                size = resource.contentLength();
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

        PrintWriter writer = new PrintWriter(entityStream);

        if (type.equals(Form.class)) {
            Form form = Form.class.cast(t);

            if (form.isExternal() && form.getContainer() != null && !form.getContainer().isReadonly()) {
                Resource script = formTemplateService.getExternalScriptResource(type, t);
                InputStream input = script.getInputStream();

                String applicationTitle = environment.getProperty("application.name");
                final String assetsUrl = environment.getProperty("ui.static.urlbase");

                User user = new User.Builder().userId(internalId).visibleId(externalId).displayName(userName).build(null);
                PageContext pageContext = new PageContext.Builder()
                        .applicationTitle(applicationTitle)
                        .assetsUrl(assetsUrl)
                        .user(user)
                        .build();

                ObjectMapper objectMapper = jacksonJaxbJsonProvider.locateMapper(type, MediaType.APPLICATION_JSON_TYPE);

                final String pageContextAsJson = objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(pageContext);
                final String modelAsJson = objectMapper.writer().writeValueAsString(t);
                final boolean isExplanation = type != null && type.equals(Explanation.class);

                Map<String, String> scopes = new HashMap<String, String>();

                scopes.put("pageContext", pageContextAsJson);
                scopes.put("model", modelAsJson);

                MustacheFactory mf = new DefaultMustacheFactory();
                Mustache mustache = mf.compile(new BufferedReader(new InputStreamReader(input)), "resource");
                mustache.execute(writer, scopes);
                writer.flush();
                return;
            }
        }

//        Resource script = formTemplateService.getScriptResource(type, t);
//        if (script.exists()) {
//            InputStream input = script.getInputStream();
//            IOUtils.copy(input, writer);
//            writer.flush();
//        } else {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
//        }
    }

    private boolean hasScriptResource(Class<?> type) {
        if (type.equals(SearchResults.class))
            return true;
        if (InputStream.class.isAssignableFrom(type))
            return false;

        Resource resource = formTemplateService.getTemplateResource(type, null);
        return resource != null && resource.exists();
    }


}
