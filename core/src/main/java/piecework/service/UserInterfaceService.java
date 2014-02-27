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
import piecework.Constants;
import piecework.common.ContentResource;
import piecework.enumeration.CacheName;
import piecework.exception.*;
import piecework.form.FormDisposition;
import piecework.identity.IdentityHelper;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessDeploymentProvider;
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

    public <P extends ProcessDeploymentProvider> Resource getCustomPage(P modelProvider, Form form) throws PieceworkException {
        // Sanity checks
        if (form == null)
            throw new MisconfiguredProcessException("No form");

        Content content = getContentFromDisposition(modelProvider, form.getDisposition());
        return new ContentResource(content);
    }

    public <P extends ProcessDeploymentProvider> StreamingOutput getCustomPageAsStreaming(P modelProvider, final Form form) throws PieceworkException, IOException {
        // Sanity checks
        if (form == null)
            throw new MisconfiguredProcessException("No form");

        FormDisposition disposition = form.getDisposition();
        Content content = getContentFromDisposition(modelProvider, disposition);

        TagNodeVisitor visitor;
        switch (disposition.getType()) {
            case CUSTOM:
                Process process = modelProvider.process();
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

    public <P extends ProcessDeploymentProvider> Resource getScriptResource(ServletContext servletContext, P modelProvider, Form form) throws StatusCodeError {
        Resource template = formTemplateService.getTemplateResource(Form.class, form);
        return getScriptResource(servletContext, modelProvider, form, template);
    }

    public <P extends ProcessDeploymentProvider> Resource getScriptResource(ServletContext servletContext, P modelProvider, Form form, Resource template) throws StatusCodeError {
        return getResource(CacheName.SCRIPT, servletContext, modelProvider, form, template);
    }

    public <P extends ProcessDeploymentProvider> Resource getScriptResource(ServletContext servletContext, P modelProvider, String templateName, boolean isAnonymous) throws StatusCodeError {
        Resource template = formTemplateService.getTemplateResource(templateName);
        return getResource(CacheName.SCRIPT, servletContext, modelProvider, null, template);
    }

    public <P extends ProcessDeploymentProvider> Resource getStylesheetResource(ServletContext servletContext, P modelProvider, Form form) throws StatusCodeError {
        Resource template = formTemplateService.getTemplateResource(Form.class, form);
        return getStylesheetResource(servletContext, modelProvider, form, template);
    }

    public <P extends ProcessDeploymentProvider> Resource getStylesheetResource(ServletContext servletContext, P modelProvider, Form form, Resource template) throws StatusCodeError {
        return getResource(CacheName.STYLESHEET, servletContext, modelProvider, form, template);
    }

    public <P extends ProcessDeploymentProvider> Resource getStylesheetResource(ServletContext servletContext, P modelProvider, String templateName) throws StatusCodeError {
        Resource template = formTemplateService.getTemplateResource(templateName);
        return getResource(CacheName.STYLESHEET, servletContext, modelProvider, null, template);
    }

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

    private <P extends ProcessDeploymentProvider> Content getContentFromDisposition(P modelProvider, FormDisposition disposition) throws PieceworkException {
        if (disposition == null)
            throw new MisconfiguredProcessException("No form disposition");
        if (disposition.getType() != FormDisposition.FormDispositionType.CUSTOM)
            throw new MisconfiguredProcessException("Form disposition is not custom");
        if (StringUtils.isEmpty(disposition.getPath()))
            throw new MisconfiguredProcessException("Form disposition path is empty");


        Content content = contentRepository.findByLocation(modelProvider, disposition.getPath());
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

    private <P extends ProcessDeploymentProvider> Resource getResource(CacheName cacheName, ServletContext servletContext, P modelProvider, Form form, Resource template) throws StatusCodeError {
        if (!template.exists())
            throw new NotFoundError();

        Resource scriptResource = getResourceFromCache(template, cacheName);
        if (scriptResource != null)
            return scriptResource;

        scriptResource = UserInterfaceUtility.resource(cacheName, modelProvider, form, template, contentRepository, servletContext, settings);
        cacheService.put(cacheName, template.getFilename(), scriptResource);

        return scriptResource;
    }

}
