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
package piecework.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.apache.cxf.common.util.StringUtils;
import org.apache.log4j.Logger;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import piecework.exception.MisconfiguredProcessException;
import piecework.exception.NotFoundError;
import piecework.exception.StatusCodeError;
import piecework.form.FormDisposition;
import piecework.identity.IdentityHelper;
import piecework.model.*;
import piecework.persistence.ContentRepository;
import piecework.ui.streaming.HtmlCleanerStreamingOutput;
import piecework.ui.visitor.LinkOptimizingVisitor;
import piecework.ui.visitor.OptimizingHtmlProviderVisitor;
import piecework.ui.PageContext;
import piecework.ui.TemplateResourceStreamingOutput;
import piecework.ui.visitor.ScriptInjectingVisitor;

import javax.annotation.PostConstruct;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author James Renfro
 */
@Service
public class UserInterfaceService {

    private final static CacheManager cacheManager = new org.springframework.cache.concurrent.ConcurrentMapCacheManager();
    private static final Logger LOG = Logger.getLogger(UserInterfaceService.class);

    @Autowired
    protected ContentRepository contentRepository;

    @Autowired
    private Environment environment;

    @Autowired
    private FormTemplateService formTemplateService;

    @Autowired
    private IdentityHelper helper;

    @Autowired
    private JacksonJaxbJsonProvider jacksonJaxbJsonProvider;

    private String applicationTitle;
    private String applicationUrl;
    private String publicUrl;
    private String assetsUrl;
    private boolean disableResourceCaching;

    @PostConstruct
    public void init() {
        this.applicationTitle = environment.getProperty("application.name");
        this.applicationUrl = environment.getProperty("base.application.uri");
        this.publicUrl = environment.getProperty("base.public.uri");
        this.assetsUrl = environment.getProperty("ui.static.urlbase");
        this.disableResourceCaching = environment.getProperty("disable.resource.caching", Boolean.class, Boolean.FALSE);
    }

    public boolean hasPage(Class<?> type) {
        if (type.equals(SearchResults.class))
            return true;
        if (InputStream.class.isAssignableFrom(type))
            return false;

        Resource resource = formTemplateService.getTemplateResource(type, null);
        return resource != null && resource.exists();
    }

    public boolean hasExternalScriptResource(Class<?> type) {
        if (type.equals(SearchResults.class))
            return true;
        if (InputStream.class.isAssignableFrom(type))
            return false;

        Resource resource = formTemplateService.getTemplateResource(type, null);
        return resource != null && resource.exists();
    }

    public StreamingOutput getDefaultPageAsStreaming(Class<?> type, Object t) throws IOException {
        Resource template = formTemplateService.getTemplateResource(type, t);
        if (template.exists()) {
            Entity user = helper.getPrincipal();
            ObjectMapper objectMapper = jacksonJaxbJsonProvider.locateMapper(type, MediaType.APPLICATION_JSON_TYPE);
            LinkOptimizingVisitor visitor =
                    new LinkOptimizingVisitor(applicationTitle, applicationUrl, publicUrl, assetsUrl, t, type, user, objectMapper, environment);

            return new HtmlCleanerStreamingOutput(template.getInputStream(), visitor);
        }
        return null;
    }

    public StreamingOutput getCustomPageAsStreaming(Form form) throws MisconfiguredProcessException, IOException {
        // Sanity checks
        if (form == null)
            throw new MisconfiguredProcessException("No form");

        FormDisposition disposition = form.getDisposition();

        if (disposition == null)
            throw new MisconfiguredProcessException("No form disposition");
        if (disposition.getType() != FormDisposition.FormDispositionType.CUSTOM)
            throw new MisconfiguredProcessException("Form disposition is not custom");
        if (StringUtils.isEmpty(disposition.getPath()))
            throw new MisconfiguredProcessException("Form disposition path is empty");

        Content content = contentRepository.findByLocation(disposition.getPath());
        if (content == null)
            throw new MisconfiguredProcessException("No content found for disposition path: " + disposition.getPath());

        ScriptInjectingVisitor visitor = new ScriptInjectingVisitor(form);
        return new HtmlCleanerStreamingOutput(content.getInputStream(), visitor);
    }

    public StreamingOutput getExternalScriptAsStreaming(Class<?> type, Object t) throws IOException {
        if (type.equals(Form.class)) {
            Form form = Form.class.cast(t);
            if (form.isExternal() && form.getContainer() != null && !form.getContainer().isReadonly()) {
                Entity user = helper.getPrincipal();
                Resource script = formTemplateService.getExternalScriptResource(type, t);

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

                return new TemplateResourceStreamingOutput(script, scopes);
            }
        }
        return null;
    }

    public Resource getScriptResource(String templateName) throws StatusCodeError {
        Resource template = formTemplateService.getTemplateResource(templateName);
        return getScriptResource(template);
    }

    public Resource getScriptResource(Resource template) throws StatusCodeError {
        if (!template.exists())
            throw new NotFoundError();

        Cache cache = cacheManager.getCache("scriptCache");
        OptimizingHtmlProviderVisitor visitor = null;
        try {
            Cache.ValueWrapper wrapper = disableResourceCaching ? null : cache.get(template.getFilename());

            if (wrapper != null) {
                Resource scriptResource = (Resource) wrapper.get();

                if (scriptResource != null && scriptResource.exists() && scriptResource.lastModified() >= template.lastModified())
                    return scriptResource;
            }

            CleanerProperties cleanerProperties = new CleanerProperties();
            cleanerProperties.setOmitXmlDeclaration(true);
            HtmlCleaner cleaner = new HtmlCleaner(cleanerProperties);
            visitor = new OptimizingHtmlProviderVisitor(applicationTitle, applicationUrl, publicUrl, assetsUrl, environment, contentRepository);
            TagNode node = cleaner.clean(template.getInputStream());
            node.traverse(visitor);
        } catch (IOException ioe) {
            LOG.error("Unable to read template", ioe);
        }

        if (visitor == null)
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);

        Resource scriptResource = visitor.getScriptResource();
        cache.put(template.getFilename(), scriptResource);

        return scriptResource;
    }

    public Resource getStylesheetResource(String templateName) throws StatusCodeError {
        Resource template = formTemplateService.getTemplateResource(templateName);
        return getStylesheetResource(template);
    }

    public Resource getStylesheetResource(Resource template) throws StatusCodeError {
        if (!template.exists())
            throw new NotFoundError();

        Cache cache = cacheManager.getCache("stylesheetCache");
        OptimizingHtmlProviderVisitor visitor = null;
        try {
            Cache.ValueWrapper wrapper = disableResourceCaching ? null : cache.get(template.getFilename());

            if (wrapper != null) {
                Resource stylesheetResource = (Resource) wrapper.get();

                if (stylesheetResource != null && stylesheetResource.exists() && stylesheetResource.lastModified() >= template.lastModified())
                    return stylesheetResource;
            }

            CleanerProperties cleanerProperties = new CleanerProperties();
            cleanerProperties.setOmitXmlDeclaration(true);
            HtmlCleaner cleaner = new HtmlCleaner(cleanerProperties);
            visitor = new OptimizingHtmlProviderVisitor(applicationTitle, applicationUrl, publicUrl, assetsUrl, environment, contentRepository);
            TagNode node = cleaner.clean(template.getInputStream());
            node.traverse(visitor);
        } catch (IOException ioe) {
            LOG.error("Unable to read template", ioe);
        }

        if (visitor == null)
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);

        Resource stylesheetResource = visitor.getStylesheetResource();
        cache.put(template.getFilename(), stylesheetResource);

        return stylesheetResource;
    }

    public boolean serveExternalScriptResource(Class<?> type, Object t, OutputStream out) throws IOException {
        StreamingOutput streamingOutput = getExternalScriptAsStreaming(type, t);
        if (streamingOutput != null) {
            streamingOutput.write(out);
            return true;
        }
        return false;
    }

    public boolean servePage(Class<?> type, Object t, OutputStream out) throws IOException {
        StreamingOutput streamingOutput = getDefaultPageAsStreaming(type, t);
        if (streamingOutput != null) {
            streamingOutput.write(out);
            return true;
        }
        return false;
    }

    public long getPageSize(Class<?> type, Object t) {
        Resource resource = formTemplateService.getTemplateResource(type, t);
        return getResourceSize(resource);
    }

    public long getExternalScriptSize(Class<?> type, Object t) {
        Resource resource = formTemplateService.getExternalScriptResource(type, t);
        return getResourceSize(resource);
    }

    private long getResourceSize(Resource resource) {
        long size = 0;
        if (resource.exists()) {
            try {
                size = resource.contentLength();
            } catch (IOException e) {
                LOG.error("Unable to determine size of template for " + resource.getFilename(), e);
            }
        }
        return size;
    }

}
