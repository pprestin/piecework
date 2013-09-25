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
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.apache.cxf.jaxrs.provider.AbstractConfigurableProvider;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
import piecework.model.SearchResults;
import piecework.model.User;

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
    private static final Logger LOG = Logger.getLogger(HtmlProvider.class);

    @Autowired
    private Environment environment;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return !type.equals(StreamingPageContent.class) && hasScriptResource(type);
    }

    @Override
    public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        Resource resource = getScriptResource(type, t);
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

        Resource script = getScriptResource(type, t);
        if (script.exists()) {
            String applicationTitle = environment.getProperty("application.name");
            final String assetsUrl = environment.getProperty("ui.static.urlbase");

            User user = new User.Builder().userId(internalId).visibleId(externalId).displayName(userName).build(null);
            PageContext pageContext = new PageContext.Builder()
                    .applicationTitle(applicationTitle)
                    .assetsUrl(assetsUrl)
                    .user(user)
                    .build();

            final String pageContextAsJson = objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(pageContext);
            final String modelAsJson = objectMapper.writer().writeValueAsString(t);
            final boolean isExplanation = type != null && type.equals(Explanation.class);

            script.getInputStream();

            Map<String, String> scopes = new HashMap<String, String>();

            scopes.put("pageContext", pageContextAsJson);
            scopes.put("model", modelAsJson);

            PrintWriter writer = new PrintWriter(entityStream);
            MustacheFactory mf = new DefaultMustacheFactory();
            Mustache mustache = mf.compile(new BufferedReader(new InputStreamReader(script.getInputStream())), "resource");
            mustache.execute(writer, scopes);
            writer.flush();
        } else {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    private boolean checkForStaticPath(String path) {
        if (path == null)
            return false;

        return path.startsWith("static/") || path.startsWith("../static/") || path.startsWith(("../../static"));
    }

    private String recomputeStaticPath(final String path, String assetsUrl) {
        int indexOf = path.indexOf("static/");

        if (indexOf > path.length())
            return path;

        String adjustedPath = path.substring(indexOf);
        return new StringBuilder(assetsUrl).append("/").append(adjustedPath).toString();
    }

    private boolean hasScriptResource(Class<?> type) {
        if (type.equals(SearchResults.class))
            return true;
        if (InputStream.class.isAssignableFrom(type))
            return false;

        Resource resource = getScriptResource(type, null);
        return resource != null && resource.exists();
    }

    private Resource getScriptResource(Class<?> type, Object t) {
        String scriptsDirectory = environment.getProperty("scripts.directory");

        StringBuilder templateNameBuilder = new StringBuilder(type.getSimpleName());

        if (type.equals(SearchResults.class)) {
            SearchResults results = SearchResults.class.cast(t);
            templateNameBuilder.append(".").append(results.getResourceName());
        }

        templateNameBuilder.append(".js");

        String templateName = templateNameBuilder.toString();
        Resource resource = null;
        if (scriptsDirectory != null && !scriptsDirectory.equals("${scripts.directory}")) {
            resource = new FileSystemResource(scriptsDirectory + File.separator + templateName);

            if (!resource.exists())
                resource = new FileSystemResource(scriptsDirectory + File.separator + "script.js");

        } else {
            resource = new ClassPathResource("META-INF/piecework/scripts/" + templateName);

            if (!resource.exists())
                resource = new ClassPathResource("META-INF/piecework/scripts/" + "script.js");
        }


        return resource;
    }
}
