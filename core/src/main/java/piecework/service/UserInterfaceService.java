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
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.TagNodeVisitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.common.ViewContext;
import piecework.content.ContentResource;
import piecework.content.concrete.RemoteContentProvider;
import piecework.content.concrete.RemoteResource;
import piecework.enumeration.CacheName;
import piecework.enumeration.DataInjectionStrategy;
import piecework.exception.*;
import piecework.form.FormDisposition;
import piecework.identity.IdentityHelper;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ContentProfileProvider;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.repository.ContentRepository;
import piecework.settings.UserInterfaceSettings;
import piecework.ui.CustomJaxbJsonProvider;
import piecework.ui.InlinePageModelSerializer;
import piecework.ui.streaming.HtmlCleanerStreamingOutput;
import piecework.ui.visitor.*;
import piecework.util.UserInterfaceUtility;

import javax.servlet.ServletContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

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
    private FormTemplateService formTemplateService;

    @Autowired
    private IdentityHelper helper;

    @Autowired
    private CustomJaxbJsonProvider jsonProvider;

    @Autowired(required=false)
    private RemoteContentProvider remoteContentProvider;

    @Autowired
    private UserInterfaceSettings settings;


    public boolean hasPage(Class<?> type) {
        if (type.equals(SearchResults.class))
            return true;
        if (InputStream.class.isAssignableFrom(type))
            return false;

        try {
            ContentResource resource = formTemplateService.getTemplateResource(type, null);
            return resource != null;
        } catch (NotFoundError nfe) {
            return false;
        }
    }

    public StreamingOutput getExplanationAsStreaming(ServletContext servletContext, Explanation explanation) {
        try {
            ContentResource template = formTemplateService.getTemplateResource(Explanation.class, explanation);
            if (template != null) {
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
        ContentResource template = formTemplateService.getTemplateResource(type, t);
        if (template != null) {
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

    public Set<Field> getRemoteFields(ProcessDeploymentProvider modelProvider, Action action, Container parentContainer, Container container, ViewContext context) throws PieceworkException {
        LOG.info("Calling getRemoteFields");

        if (action.getStrategy() == null || action.getStrategy() != DataInjectionStrategy.REMOTE)
            return Collections.<Field>emptySet();

        Set<Field> fields = null;
        if (remoteContentProvider != null) {
            Process process = modelProvider.process();
            ProcessDeployment deployment = modelProvider.deployment();
            FormDisposition formDisposition = FormDisposition.Builder.build(process, deployment, action, context);
            URI uri = formDisposition.getPageUri();

            if (LOG.isInfoEnabled())
                LOG.info("Retrieving remote resource from " + uri.toString());

            ContentResource contentResource = remoteContentProvider.findByLocation(modelProvider, uri.toString());

            try {
                InputStream inputStream = contentResource.getInputStream();
                // Sanity check
                if (inputStream == null)
                    throw new InternalServerError(Constants.ExceptionCodes.system_misconfigured, "Unable to view remote template");

                RemoteTemplateVisitor templateVisitor = new RemoteTemplateVisitor(parentContainer, container);
                CleanerProperties cleanerProperties = new CleanerProperties();
                cleanerProperties.setOmitXmlDeclaration(true);
                HtmlCleaner cleaner = new HtmlCleaner(cleanerProperties);
                TagNode node = cleaner.clean(inputStream);
                node.traverse(templateVisitor);

                fields = templateVisitor.getFields();

            } catch (IOException ioe) {
                LOG.error("Error retrieving remote fields", ioe);
            }
        }

        if (fields != null)
            LOG.info("Retrieved " + fields.size() + " remote fields");

        return fields;
    }

    public ContentResource getCustomPage(ContentProfileProvider modelProvider, Form form) throws PieceworkException {
        // Sanity checks
        if (form == null)
            throw new MisconfiguredProcessException("No form");

        return getContentFromDisposition(modelProvider, form.getDisposition());
    }

    public <P extends ProcessDeploymentProvider> StreamingOutput getCustomPageAsStreaming(P modelProvider, final Form form) throws PieceworkException, IOException {
        // Sanity checks
        if (form == null)
            throw new MisconfiguredProcessException("No form");

        FormDisposition disposition = form.getDisposition();
        ContentResource contentResource = getContentFromDisposition(modelProvider, disposition);

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
        return new HtmlCleanerStreamingOutput(contentResource.getInputStream(), visitor);
    }

    public ContentResource getScriptResource(ServletContext servletContext, ContentProfileProvider modelProvider, Form form) throws StatusCodeError {
        ContentResource template = formTemplateService.getTemplateResource(Form.class, form);
        return getScriptResource(servletContext, modelProvider, form, template);
    }

    public ContentResource getScriptResource(ServletContext servletContext, ContentProfileProvider modelProvider, Form form, ContentResource template) throws StatusCodeError {
        return getResource(CacheName.SCRIPT, servletContext, modelProvider, form, template);
    }

    public ContentResource getScriptResource(ServletContext servletContext, ContentProfileProvider modelProvider, String templateName, boolean isAnonymous) throws StatusCodeError {
        ContentResource template = formTemplateService.getTemplateResource(templateName);
        return getResource(CacheName.SCRIPT, servletContext, modelProvider, null, template);
    }

    public ContentResource getStylesheetResource(ServletContext servletContext, ContentProfileProvider modelProvider, Form form) throws StatusCodeError {
        ContentResource template = formTemplateService.getTemplateResource(Form.class, form);
        return getStylesheetResource(servletContext, modelProvider, form, template);
    }

    public ContentResource getStylesheetResource(ServletContext servletContext, ContentProfileProvider modelProvider, Form form, ContentResource template) throws StatusCodeError {
        return getResource(CacheName.STYLESHEET, servletContext, modelProvider, form, template);
    }

    public ContentResource getStylesheetResource(ServletContext servletContext, ContentProfileProvider modelProvider, String templateName) throws StatusCodeError {
        ContentResource template = formTemplateService.getTemplateResource(templateName);
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
            ContentResource resource = formTemplateService.getTemplateResource(type, t);
            return resource.contentLength();
        } catch (NotFoundError nfe) {
            return 0;
        }
    }

//    public long getExternalScriptSize(Class<?> type, Object t) {
//        Resource resource = formTemplateService.getExternalScriptResource(type, t);
//        return UserInterfaceUtility.resourceSize(resource);
//    }

    private ContentResource getContentFromDisposition(ContentProfileProvider modelProvider, FormDisposition disposition) throws PieceworkException {
        if (disposition == null)
            throw new MisconfiguredProcessException("No form disposition");
        if (disposition.getType() != FormDisposition.FormDispositionType.CUSTOM)
            throw new MisconfiguredProcessException("Form disposition is not custom");
        if (StringUtils.isEmpty(disposition.getPath()))
            throw new MisconfiguredProcessException("Form disposition path is empty");

        String base = disposition.getBase();
        String path = disposition.getPath();
        String location = base + "/" + path;

        ContentResource contentResource = contentRepository.findByLocation(modelProvider, location);
        if (contentResource == null)
            throw new MisconfiguredProcessException("No content found for disposition base: " + disposition.getBase() + " and path: " + disposition.getPath());

        return contentResource;
    }

    private ContentResource getResourceFromCache(ContentResource template, CacheName cacheName) {

        Cache.ValueWrapper wrapper = settings.isDisableResourceCaching() ? null : cacheService.get(cacheName, template.getFilename());

        if (wrapper != null) {
            ContentResource resource = (ContentResource) wrapper.get();

            if (resource != null && (resource.lastModified() <= 0 || resource.lastModified() >= template.lastModified()))
                return resource;
        }

        return null;
    }

    private ContentResource getResource(CacheName cacheName, ServletContext servletContext, ContentProfileProvider modelProvider, Form form, ContentResource template) throws StatusCodeError {
        ContentResource scriptResource = getResourceFromCache(template, cacheName);
        if (scriptResource != null)
            return scriptResource;

        scriptResource = UserInterfaceUtility.resource(cacheName, modelProvider, form, template, contentRepository, servletContext, settings);
        cacheService.put(cacheName, template.getFilename(), scriptResource);

        return scriptResource;
    }

}
