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
package piecework.resource.concrete;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.log4j.Logger;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.enumeration.DataInjectionStrategy;
import piecework.model.RequestDetails;
import piecework.enumeration.ActionType;
import piecework.exception.*;
import piecework.form.FormFactory;
import piecework.handler.RequestHandler;
import piecework.identity.IdentityHelper;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ContentRepository;
import piecework.resource.ScriptResource;
import piecework.security.Sanitizer;
import piecework.security.SecuritySettings;
import piecework.service.FormTemplateService;
import piecework.ui.OptimizingHtmlProviderVisitor;
import piecework.ui.PageContext;
import piecework.ui.ResourceStreamingOutput;
import piecework.ui.TemplateResourceStreamingOutput;

import javax.annotation.PostConstruct;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

/**
 * @author James Renfro
 */
@Service
public class ScriptResourceVersion1 implements ScriptResource {

    private static final Logger LOG = Logger.getLogger(ScriptResourceVersion1.class);

    CacheManager cacheManager = new org.springframework.cache.concurrent.ConcurrentMapCacheManager();

    @Autowired
    ContentRepository contentRepository;

    @Autowired
    Environment environment;

    @Autowired
    FormFactory formFactory;

    @Autowired
    FormTemplateService formTemplateService;

    @Autowired
    IdentityHelper identityHelper;

    @Autowired
    JacksonJaxbJsonProvider jacksonJaxbJsonProvider;

    @Autowired
    RequestHandler requestHandler;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    SecuritySettings securitySettings;

    private String applicationTitle;
    private String applicationUrl;
    private String assetsUrl;
    private boolean disableResourceCaching;

    @PostConstruct
    public void init() {
        this.applicationTitle = environment.getProperty("application.name");
        this.applicationUrl = environment.getProperty("base.application.uri");
        this.assetsUrl = environment.getProperty("ui.static.urlbase");
        this.disableResourceCaching = environment.getProperty("disable.resource.caching", Boolean.class, Boolean.FALSE);
    }

//    @Override
//    public Response read(String rawProcessDefinitionKey, MessageContext context) throws StatusCodeError {
////        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
////        piecework.model.Process process = identityHelper.findProcess(processDefinitionKey, true);
////        Form form = legacyFormService.startForm(context, process);
//
//        return null;
//    }

    @Override
    public Response read(String rawProcessDefinitionKey, String rawRequestId, MessageContext context) throws StatusCodeError {
        Entity principal = identityHelper.getPrincipal();
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        String requestId = sanitizer.sanitize(rawRequestId);
        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        FormRequest request = requestHandler.handle(requestDetails, requestId);

        return response(request, principal, new MediaType("text", "javascript"));
    }

    @Override
    public Response readScript(String scriptId) throws StatusCodeError {
        String templateName = "SearchResults.form.template.html";

        if (scriptId.equals("Form"))
            templateName = "Form.template.html";
        else if (scriptId.equals("IndexView"))
            templateName = "IndexView.template.html";
        else if (scriptId.equals("Explanation"))
            templateName = "Explanation.template.html";

        Resource templateResource = formTemplateService.getTemplateResource(templateName);
        Resource scriptResource = getScriptResource(templateResource);

        return response(scriptResource, "text/javascript");
    }

    @Override
    public Response readStylesheet(String stylesheetId) throws StatusCodeError {
        String templateName = "SearchResults.form.template.html";

        if (stylesheetId.equals("Form"))
            templateName = "Form.template.html";
        else if (stylesheetId.equals("IndexView"))
            templateName = "IndexView.template.html";
        else if (stylesheetId.equals("Explanation"))
            templateName = "Explanation.template.html";

        Resource templateResource = formTemplateService.getTemplateResource(templateName);
        Resource stylesheetResource = getStylesheetResource(templateResource);

        return response(stylesheetResource, "text/css");
    }

    @Override
    public Response readStatic(final String rawProcessDefinitionKey, final List<PathSegment> pathSegments, final MessageContext context) throws StatusCodeError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        Process process = identityHelper.findProcess(processDefinitionKey, true);
        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();

        Iterator<PathSegment> pathSegmentIterator = pathSegments.iterator();

        if (pathSegmentIterator.hasNext()) {
            String name = "";
            while (pathSegmentIterator.hasNext()) {
                name += sanitizer.sanitize(pathSegmentIterator.next().getPath());
                if (pathSegmentIterator.hasNext())
                    name += "/";
            }
            ProcessDeployment detail = process.getDeployment();
            if (detail == null)
                throw new ConflictError();

            String base = detail.getBase();

            if (StringUtils.isNotEmpty(base)) {
                Content content = contentRepository.findByLocation(base + "/" + name);

                if (content != null)
                    return Response.ok(content.getInputStream()).type(content.getContentType()).build();
            }

            throw new NotFoundError();
        }

        return null;
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
            visitor = new OptimizingHtmlProviderVisitor(applicationTitle, applicationUrl, assetsUrl, environment, contentRepository);
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
            visitor = new OptimizingHtmlProviderVisitor(applicationTitle, applicationUrl, assetsUrl, environment, contentRepository);
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

    private Response response(FormRequest request, Entity principal, MediaType mediaType) throws StatusCodeError {
        if (request == null)
            throw new NotFoundError();

        try {
            ActionType actionType = request.getAction();
            Form form = formFactory.form(request, actionType, principal, mediaType, null, null);

            Activity activity = request.getActivity();
            Action action = activity.action(actionType);

            if (form.isExternal() && form.getContainer() != null && action != null && action.getStrategy() == DataInjectionStrategy.INCLUDE_SCRIPT) {
                try {
                    Resource script = formTemplateService.getExternalScriptResource(Form.class, form);
                    String applicationTitle = environment.getProperty("application.name");
                    final String assetsUrl = environment.getProperty("ui.static.urlbase");

                    User currentUser = null;
                    if (principal instanceof User)
                        currentUser = User.class.cast(principal);

                    PageContext pageContext = new PageContext.Builder()
                            .applicationTitle(applicationTitle)
                            .assetsUrl(assetsUrl)
                            .user(currentUser)
                            .build();

                    ObjectMapper objectMapper = jacksonJaxbJsonProvider.locateMapper(Form.class, MediaType.APPLICATION_JSON_TYPE);

                    final String pageContextAsJson = objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(pageContext);
                    final String modelAsJson = objectMapper.writer().writeValueAsString(form);

                    Map<String, String> scopes = new HashMap<String, String>();
                    scopes.put("pageContext", pageContextAsJson);
                    scopes.put("model", modelAsJson);

                    return Response.ok(new TemplateResourceStreamingOutput(script, scopes)).build();
                } catch (IOException ioe) {
                    LOG.error("Failed to generate script", ioe);
                }
            }
        } catch (FormBuildingException e) {
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
        }

        throw new NotFoundError();
    }

    private static Date lastModified(Resource resource) {
        DateTime lastModified;
        try {
            lastModified = new DateTime(resource.lastModified());
        } catch (IOException ioe) {
            lastModified = new DateTime();
        }
        return lastModified.toDate();
    }

    private static Response response(Resource resource, String mediaType) {
        DateTime today = new DateTime();
        return Response
                .ok(new ResourceStreamingOutput(resource), mediaType)
                .expires(today.plusDays(1).toDate())
                .lastModified(lastModified(resource))
                .build();
    }


}
