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
import piecework.repository.ContentRepository;
import piecework.settings.UserInterfaceSettings;
import piecework.ui.*;
import piecework.ui.streaming.HtmlCleanerStreamingOutput;
import piecework.ui.visitor.*;
import piecework.util.UserInterfaceUtility;

import javax.servlet.ServletContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;

/**
 * @author James Renfro
 */
@Service
public class UserInterfaceService {

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

    public StreamingOutput getExplanationAsStreaming(ServletContext servletContext, Explanation explanation, Entity principal) {
        try {
            Resource template = formTemplateService.getTemplateResource(Explanation.class, explanation);
            if (template.exists()) {
                Entity user = helper.getPrincipal();
                ObjectMapper objectMapper = jsonProvider.locateMapper(Explanation.class, MediaType.APPLICATION_JSON_TYPE);
                InlinePageModelSerializer modelSerializer = new InlinePageModelSerializer(settings, explanation, Explanation.class, user, objectMapper);
                StaticResourceAggregatingVisitor aggregatingVisitor =
                        new StaticResourceAggregatingVisitor(servletContext, null, null, settings, contentRepository, principal, true);

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

    public Resource getCustomPage(Form form, Entity principal) throws MisconfiguredProcessException {
        // Sanity checks
        if (form == null)
            throw new MisconfiguredProcessException("No form");

        Content content = getContentFromDisposition(form.getProcess(), form.getDisposition(), principal);
        return new ContentResource(content);
    }

    public StreamingOutput getCustomPageAsStreaming(final Process process, final Form form, Entity principal) throws MisconfiguredProcessException, IOException {
        // Sanity checks
        if (form == null)
            throw new MisconfiguredProcessException("No form");

        FormDisposition disposition = form.getDisposition();
        Content content = getContentFromDisposition(process, disposition, principal);

        TagNodeVisitor visitor;
        switch (disposition.getType()) {
            case CUSTOM:
                visitor = new DecoratingVisitor(settings, process, form);
                break;
            case REMOTE:
                visitor = new StaticPathAdjustingVisitor(form);
                break;
            default:
                visitor = new ScriptInjectingVisitor(form);
                break;
        }
        return new HtmlCleanerStreamingOutput(content.getInputStream(), visitor);
    }

//    public StreamingOutput getExternalScriptAsStreaming(Class<?> type, Object t) throws IOException {
//        if (type.equals(Form.class)) {
//            Form form = Form.class.cast(t);
//            if (form.isExternal() && form.getContainer() != null && !form.getContainer().isReadonly()) {
//                Entity user = helper.getPrincipal();
//                Resource script = formTemplateService.getExternalScriptResource(type, t);
//
//                PageContext pageContext = new PageContext.Builder()
//                        .applicationTitle(settings.getApplicationTitle())
//                        .assetsUrl(settings.getAssetsUrl())
//                        .user(user)
//                        .build();
//
//                ObjectMapper objectMapper = jsonProvider.locateMapper(type, MediaType.APPLICATION_JSON_TYPE);
//
//                final String pageContextAsJson = objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(pageContext);
//                final String modelAsJson = objectMapper.writer().writeValueAsString(t);
//                final boolean isExplanation = type != null && type.equals(Explanation.class);
//
//                Map<String, String> scopes = new HashMap<String, String>();
//
//                scopes.put("pageContext", pageContextAsJson);
//                scopes.put("model", modelAsJson);
//
//                return new TemplateResourceStreamingOutput(script, scopes);
//            }
//        }
//        return null;
//    }

    public Resource getScriptResource(ServletContext servletContext, Form form, Entity principal) throws StatusCodeError {
        Resource template = formTemplateService.getTemplateResource(Form.class, form);
        return getScriptResource(servletContext, form, template, principal);
    }

    public Resource getScriptResource(ServletContext servletContext, Form form, Resource template, Entity principal) throws StatusCodeError {
        return getResource(CacheName.SCRIPT, servletContext, form, template, principal);
    }

    public Resource getScriptResource(ServletContext servletContext, String templateName, boolean isAnonymous, Entity principal) throws StatusCodeError {
        Resource template = formTemplateService.getTemplateResource(templateName);
        return getResource(CacheName.SCRIPT, servletContext, null, template, principal);
    }

    public Resource getStylesheetResource(ServletContext servletContext, Form form, Entity principal) throws StatusCodeError {
        Resource template = formTemplateService.getTemplateResource(Form.class, form);
        return getStylesheetResource(servletContext, form, template, principal);
    }

    public Resource getStylesheetResource(ServletContext servletContext, Form form, Resource template, Entity principal) throws StatusCodeError {
        return getResource(CacheName.STYLESHEET, servletContext, form, template, principal);
    }

    public Resource getStylesheetResource(ServletContext servletContext, String templateName, Entity principal) throws StatusCodeError {
        Resource template = formTemplateService.getTemplateResource(templateName);
        return getResource(CacheName.STYLESHEET, servletContext, null, template, principal);
    }

//    public boolean serveExternalScriptResource(Class<?> type, Object t, OutputStream out) throws IOException {
//        StreamingOutput streamingOutput = getExternalScriptAsStreaming(type, t);
//        if (streamingOutput != null) {
//            streamingOutput.write(out);
//            return true;
//        }
//        return false;
//    }

    public boolean servePage(StreamingOutput streamingOutput, OutputStream out) throws IOException {
        if (streamingOutput != null) {
            streamingOutput.write(out);
            return true;
        }

        return false;
    }

    public long getPageSize(Class<?> type, Object t) {
        try {
            Resource resource = formTemplateService.getTemplateResource(type, t);
            return UserInterfaceUtility.resourceSize(resource);
        } catch (NotFoundError nfe) {
            return 0;
        }
    }

//    public long getExternalScriptSize(Class<?> type, Object t) {
//        Resource resource = formTemplateService.getExternalScriptResource(type, t);
//        return UserInterfaceUtility.resourceSize(resource);
//    }

    private Content getContentFromDisposition(Process process, FormDisposition disposition, Entity principal) throws MisconfiguredProcessException {
        if (disposition == null)
            throw new MisconfiguredProcessException("No form disposition");
        if (disposition.getType() != FormDisposition.FormDispositionType.CUSTOM)
            throw new MisconfiguredProcessException("Form disposition is not custom");
        if (StringUtils.isEmpty(disposition.getPath()))
            throw new MisconfiguredProcessException("Form disposition path is empty");

        Content content = contentRepository.findByLocation(process, disposition.getBase(), disposition.getPath(), principal);
        if (content == null)
            throw new MisconfiguredProcessException("No content found for disposition base: " + disposition.getBase() + " and path: " + disposition.getPath());

        return content;
    }

    private Resource getResourceFromCache(Resource template, CacheName cacheName) {
        try {
            Cache.ValueWrapper wrapper = settings.isDisableResourceCaching() ? null : cacheService.get(cacheName, template.getFilename());

            if (wrapper != null) {
                Resource resource = (Resource) wrapper.get();

                if (resource != null && resource.exists() && resource.lastModified() >= template.lastModified())
                    return resource;
            }
        } catch (IOException ioe) {
            LOG.error("Unable to read template", ioe);
        }
        return null;
    }

    private Resource getResource(CacheName cacheName, ServletContext servletContext, Form form, Resource template, Entity principal) throws StatusCodeError {
        if (!template.exists())
            throw new NotFoundError();

        Resource scriptResource = getResourceFromCache(template, cacheName);
        if (scriptResource != null)
            return scriptResource;

        scriptResource = UserInterfaceUtility.resource(cacheName, form, template, contentRepository, servletContext, settings, principal);
        cacheService.put(cacheName, template.getFilename(), scriptResource);

        return scriptResource;
    }

}
