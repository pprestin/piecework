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
import org.apache.commons.io.IOUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.log4j.Logger;
import org.htmlcleaner.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import piecework.common.ContentResource;
import piecework.enumeration.CacheName;
import piecework.exception.MisconfiguredProcessException;
import piecework.exception.NotFoundError;
import piecework.exception.StatusCodeError;
import piecework.form.FormDisposition;
import piecework.identity.IdentityHelper;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ContentRepository;
import piecework.ui.*;
import piecework.ui.streaming.HtmlCleanerStreamingOutput;
import piecework.ui.visitor.*;

import javax.servlet.ServletContext;
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

    //private final static CacheManager cacheManager = new org.springframework.cache.concurrent.ConcurrentMapCacheManager();
    private static final Logger LOG = Logger.getLogger(UserInterfaceService.class);

    @Autowired
    protected ContentRepository contentRepository;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private Environment environment;

    @Autowired
    private FormTemplateService formTemplateService;

    @Autowired
    private IdentityHelper helper;

    @Autowired
    private CustomJaxbJsonProvider jsonProvider;

    @Autowired
    private UserInterfaceSettings settings;


    public boolean hasPage(Class<?> type) {
        if (type.equals(SearchResults.class))
            return true;
        if (InputStream.class.isAssignableFrom(type))
            return false;

        try {
            Resource resource = formTemplateService.getTemplateResource(type, null);
            return resource != null && resource.exists();
        } catch (NotFoundError nfe) {
            return false;
        }
    }

    public boolean hasExternalScriptResource(Class<?> type) {
        if (type.equals(SearchResults.class))
            return true;
        if (InputStream.class.isAssignableFrom(type))
            return false;
        try {
            Resource resource = formTemplateService.getTemplateResource(type, null);
            return resource != null && resource.exists();
        } catch (NotFoundError nfe) {
            return false;
        }
    }

    public StreamingOutput getExplanationAsStreaming(ServletContext servletContext, Explanation explanation) {
        try {
            Resource template = formTemplateService.getTemplateResource(Explanation.class, explanation);
            if (template.exists()) {
                Entity user = helper.getPrincipal();
                ObjectMapper objectMapper = jsonProvider.locateMapper(Explanation.class, MediaType.APPLICATION_JSON_TYPE);
                InlinePageModelSerializer modelSerializer = new InlinePageModelSerializer(settings, explanation, Explanation.class, user, objectMapper);
                StaticResourceAggregatingVisitor aggregatingVisitor =
                        new StaticResourceAggregatingVisitor(servletContext, null, null, settings, contentRepository, true);

                InputStream inputStream = template.getInputStream();
                // Sanity check
                if (inputStream == null) {
                    throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
                }

                CleanerProperties cleanerProperties = new CleanerProperties();
                cleanerProperties.setOmitXmlDeclaration(true);
                HtmlCleaner cleaner = new HtmlCleaner(cleanerProperties);
                TagNode node = cleaner.clean(inputStream);
                node.traverse(aggregatingVisitor);

                ResourceInliningVisitor visitor =
                        new ResourceInliningVisitor(settings, modelSerializer, aggregatingVisitor, true);

                return new HtmlCleanerStreamingOutput(template.getInputStream(), visitor);
            }
        } catch (Exception e) {
            LOG.error("Exception handling exception", e);
        }
        return null;
    }

    public StreamingOutput getDefaultPageAsStreaming(Class<?> type, Object t) throws IOException, NotFoundError {
        Resource template = formTemplateService.getTemplateResource(type, t);
        if (template.exists()) {
            Entity user = helper.getPrincipal();
            ObjectMapper objectMapper = jsonProvider.locateMapper(type, MediaType.APPLICATION_JSON_TYPE);

            boolean isAnonymous = false;
            if (type.equals(Form.class)) {
                Form form = Form.class.cast(t);
                isAnonymous = form.isAnonymous();
            }
            InlinePageModelSerializer modelSerializer = new InlinePageModelSerializer(settings, t, type, user, objectMapper);
            LinkOptimizingVisitor visitor =
                    new LinkOptimizingVisitor(settings, modelSerializer, isAnonymous);

            return new HtmlCleanerStreamingOutput(template.getInputStream(), visitor);
        }
        return null;
    }

    public Resource getCustomPage(Form form) throws MisconfiguredProcessException, IOException {
        // Sanity checks
        if (form == null)
            throw new MisconfiguredProcessException("No form");

        Content content = getContentFromDisposition(form.getProcess(), form.getDisposition());
        return new ContentResource(content);
    }

    public StreamingOutput getCustomPageAsStreaming(final Process process, final Form form) throws MisconfiguredProcessException, IOException {
        // Sanity checks
        if (form == null)
            throw new MisconfiguredProcessException("No form");

        FormDisposition disposition = form.getDisposition();
        Content content = getContentFromDisposition(process, disposition);

        TagNodeVisitor visitor;
        switch (disposition.getStrategy()) {
            case DECORATE_HTML:
                visitor = new DecoratingVisitor(settings, process, form);
                break;
            case INCLUDE_DIRECTIVES:
                visitor = new StaticPathAdjustingVisitor(form);
                break;
            default:
                visitor = new ScriptInjectingVisitor(form);
                break;
        }
        return new HtmlCleanerStreamingOutput(content.getInputStream(), visitor);
    }

    public StreamingOutput getExternalScriptAsStreaming(Class<?> type, Object t) throws IOException {
        if (type.equals(Form.class)) {
            Form form = Form.class.cast(t);
            if (form.isExternal() && form.getContainer() != null && !form.getContainer().isReadonly()) {
                Entity user = helper.getPrincipal();
                Resource script = formTemplateService.getExternalScriptResource(type, t);

                PageContext pageContext = new PageContext.Builder()
                        .applicationTitle(settings.getApplicationTitle())
                        .assetsUrl(settings.getAssetsUrl())
                        .user(user)
                        .build();

                ObjectMapper objectMapper = jsonProvider.locateMapper(type, MediaType.APPLICATION_JSON_TYPE);

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

    public Resource getScriptResource(ServletContext servletContext, Process process, String templateName, String base, boolean isAnonymous) throws StatusCodeError {
        Resource template = formTemplateService.getTemplateResource(templateName);
        return getScriptResource(servletContext, process, template, base, isAnonymous);
    }

    public Resource getScriptResource(ServletContext servletContext, Process process, Resource template, String base, boolean isAnonymous) throws StatusCodeError {
        if (!template.exists())
            throw new NotFoundError();

        StaticResourceAggregatingVisitor visitor = null;
        InputStream inputStream = null;
        try {
            Cache.ValueWrapper wrapper = settings.isDisableResourceCaching() ? null : cacheService.get(CacheName.SCRIPT, template.getFilename());

            if (wrapper != null) {
                Resource scriptResource = (Resource) wrapper.get();

                if (scriptResource != null && scriptResource.exists() && scriptResource.lastModified() >= template.lastModified())
                    return scriptResource;
            }

            CleanerProperties cleanerProperties = new CleanerProperties();
            cleanerProperties.setOmitXmlDeclaration(true);
            HtmlCleaner cleaner = new HtmlCleaner(cleanerProperties);
            visitor = new StaticResourceAggregatingVisitor(servletContext, process, base, settings, contentRepository, isAnonymous);

            inputStream = template.getInputStream();
            TagNode node = cleaner.clean(inputStream);
            node.traverse(visitor);

            if (visitor != null) {
                Resource scriptResource = visitor.getScriptResource();
                cacheService.put(CacheName.SCRIPT, template.getFilename(), scriptResource);

                return scriptResource;
            }
        } catch (IOException ioe) {
            LOG.error("Unable to read template", ioe);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return null;
    }

    public Resource getStylesheetResource(ServletContext servletContext, Process process, String templateName, String base, boolean isAnonymous) throws StatusCodeError {
        Resource template = formTemplateService.getTemplateResource(templateName);
        return getStylesheetResource(servletContext, process, template, base, isAnonymous);
    }

    public Resource getStylesheetResource(ServletContext servletContext, Process process, Resource template, String base, boolean isAnonymous) throws StatusCodeError {
        if (!template.exists())
            throw new NotFoundError();

        StaticResourceAggregatingVisitor visitor = null;
        try {
            Cache.ValueWrapper wrapper = settings.isDisableResourceCaching() ? null : cacheService.get(CacheName.STYLESHEET, template.getFilename());

            if (wrapper != null) {
                Resource stylesheetResource = (Resource) wrapper.get();

                if (stylesheetResource != null && stylesheetResource.exists() && stylesheetResource.lastModified() >= template.lastModified())
                    return stylesheetResource;
            }

            CleanerProperties cleanerProperties = new CleanerProperties();
            cleanerProperties.setOmitXmlDeclaration(true);
            HtmlCleaner cleaner = new HtmlCleaner(cleanerProperties);
            visitor = new StaticResourceAggregatingVisitor(servletContext, process, base, settings, contentRepository, isAnonymous);
            TagNode node = cleaner.clean(template.getInputStream());
            node.traverse(visitor);

            if (visitor != null) {
                Resource stylesheetResource = visitor.getStylesheetResource();
                cacheService.put(CacheName.STYLESHEET, template.getFilename(), stylesheetResource);
                return stylesheetResource;
            }
        } catch (IOException ioe) {
            LOG.error("Unable to read template", ioe);
        }

        return null;
    }

    public boolean serveExternalScriptResource(Class<?> type, Object t, OutputStream out) throws IOException {
        StreamingOutput streamingOutput = getExternalScriptAsStreaming(type, t);
        if (streamingOutput != null) {
            streamingOutput.write(out);
            return true;
        }
        return false;
    }

    public boolean servePage(StreamingOutput streamingOutput, OutputStream out) throws IOException {
//        try {

            if (streamingOutput != null) {
                streamingOutput.write(out);
                return true;
            }
//        } catch (NotFoundError nfe) {
//            throw new IOException();
//        }
        return false;
    }

    public long getPageSize(Class<?> type, Object t) {
        try {
            Resource resource = formTemplateService.getTemplateResource(type, t);
            return getResourceSize(resource);
        } catch (NotFoundError nfe) {
            return 0;
        }
    }

    public long getExternalScriptSize(Class<?> type, Object t) {
        Resource resource = formTemplateService.getExternalScriptResource(type, t);
        return getResourceSize(resource);
    }

    private Content getContentFromDisposition(Process process, FormDisposition disposition) throws MisconfiguredProcessException {
        if (disposition == null)
            throw new MisconfiguredProcessException("No form disposition");
        if (disposition.getType() != FormDisposition.FormDispositionType.CUSTOM)
            throw new MisconfiguredProcessException("Form disposition is not custom");
        if (StringUtils.isEmpty(disposition.getPath()))
            throw new MisconfiguredProcessException("Form disposition path is empty");

        Content content = contentRepository.findByLocation(process, disposition.getPath());
        if (content == null)
            throw new MisconfiguredProcessException("No content found for disposition path: " + disposition.getPath());

        return content;
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
