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

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import piecework.enumeration.ActionType;
import piecework.exception.*;
import piecework.form.FormFactory;
import piecework.model.*;
import piecework.model.Form;
import piecework.model.Process;
import piecework.persistence.ContentRepository;
import piecework.security.Sanitizer;
import piecework.settings.SecuritySettings;
import piecework.service.*;
import piecework.ui.streaming.ResourceStreamingOutput;

import javax.ws.rs.core.*;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author James Renfro
 */
public abstract class AbstractScriptResource {

    private static final Logger LOG = Logger.getLogger(AbstractScriptResource.class);

    @Autowired
    ContentRepository contentRepository;

    @Autowired
    DeploymentService deploymentService;

    @Autowired
    protected FormFactory formFactory;

    @Autowired
    private FormTemplateService formTemplateService;

    @Autowired
    ProcessService processService;

    @Autowired
    private RequestService requestService;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    SecuritySettings securitySettings;

    @Autowired
    protected UserInterfaceService userInterfaceService;

    protected abstract boolean isAnonymous();

    protected Form form(final Process process, final MessageContext context, final Entity principal) throws NotFoundError {
        try {
            RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
            FormRequest request = requestService.create(requestDetails, process);
            ProcessDeployment deployment = deploymentService.read(process, request.getInstance());
            boolean includeRestrictedData = false;
            Form form = formFactory.form(process, deployment, request, ActionType.CREATE, principal, null, null, includeRestrictedData, isAnonymous());
            return form;
        } catch (FormBuildingException fbe) {
            LOG.error("Caught form building exception", fbe);
            throw new NotFoundError();
        } catch (MisconfiguredProcessException mpe) {
            LOG.error("Unable to create new instance because process is misconfigured", mpe);
            throw new NotFoundError();
        }
    }

//    protected Response processScript(ServletContext servletContext, Form form) throws StatusCodeError {
//        try {
//            FormDisposition formDisposition = form.getDisposition();
//            Resource pageResource = userInterfaceService.getCustomPage(form);
//            Resource scriptResource = userInterfaceService.getScriptResource(servletContext, form.getProcess(), formDisposition, pageResource, form.isAnonymous());
//            return response(scriptResource, "text/javascript");
//        } catch (IOException ioe) {
//            LOG.error("Caught io exception", ioe);
//            throw new NotFoundError();
//        } catch (MisconfiguredProcessException mpe) {
//            LOG.error("Unable to create new instance because process is misconfigured", mpe);
//            throw new NotFoundError();
//        }
//    }
//
//    protected Response processStylesheet(ServletContext servletContext, Form form) throws StatusCodeError {
//        try {
//            FormDisposition disposition = form.getDisposition();
//            Resource pageResource = userInterfaceService.getCustomPage(form);
//            Resource stylesheetResource = userInterfaceService.getStylesheetResource(servletContext, form.getProcess(), disposition, pageResource, isAnonymous());
//            return response(stylesheetResource, "text/css");
//        } catch (IOException ioe) {
//            LOG.error("Caught io exception", ioe);
//            throw new NotFoundError();
//        } catch (MisconfiguredProcessException mpe) {
//            LOG.error("Unable to create new instance because process is misconfigured", mpe);
//            throw new NotFoundError();
//        }
//    }

//    protected Response response(FormRequest request, Entity principal, MediaType mediaType) throws StatusCodeError {
//        if (request == null)
//            throw new NotFoundError();
//
//        try {
//            ActionType actionType = request.getAction();
//            Process process = processService.read(request.getProcessDefinitionKey());
//            ProcessDeployment deployment = deploymentService.read(process, request.getInstance());
//
//            // Don't include restricted data in a script form
//            boolean includeRestrictedData = false;
//            Form form = formFactory.form(process, deployment, request, actionType, principal, null, null, includeRestrictedData, isAnonymous());
//
//            Activity activity = request.getActivity();
//            Action action = activity.action(actionType);
//
//            try {
//                if (action.getStrategy() == DataInjectionStrategy.INCLUDE_SCRIPT) {
//                    StreamingOutput externalScript = userInterfaceService.getExternalScriptAsStreaming(Form.class, form);
//                    if (externalScript != null) {
//                        CacheControl cacheControl = new CacheControl();
//                        cacheControl.setNoCache(true);
//                        cacheControl.setNoStore(true);
//                        return Response.ok(externalScript, new MediaType("text", "javascript")).cacheControl(cacheControl).build();
//                    }
//                }
//            } catch (IOException e) {
//                throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
//            }
//        } catch (MisconfiguredProcessException mpe) {
//            LOG.error("Process is misconfigured", mpe);
//            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
//        } catch (FormBuildingException e) {
//            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
//        }
//
//        throw new NotFoundError();
//    }

    protected Response staticResponse(final Process process, final RequestDetails requestDetails, final List<PathSegment> pathSegments) throws StatusCodeError {
        Iterator<PathSegment> pathSegmentIterator = pathSegments.iterator();

        if (pathSegmentIterator.hasNext()) {
            String name = "";
            while (pathSegmentIterator.hasNext()) {
                String segment = sanitizer.sanitize(pathSegmentIterator.next().getPath());
                // Don't include empty segments or segments that might move us up the file system tree
                // (above the base, for example)
                if (StringUtils.isEmpty(segment) || segment.contains(".."))
                    continue;

                name += segment;
                if (pathSegmentIterator.hasNext())
                    name += "/";
            }
            ProcessDeployment detail = process.getDeployment();
            if (detail == null)
                throw new ConflictError();

            String base = detail.getBase();

            // This base ensures that each process's resources is separated from each other process's resources
            // and in the case of anonymous requests, that we're not retrieving a non-anonymous process's
            // resource thru the public url
            if (StringUtils.isNotEmpty(base)) {
                String requestLocation = base + "/" + name;
                // Note that the content providers should be doing additional checking to ensure
                // that resources are retrieved only from below the approved filesystem base and root
                Content content = contentRepository.findByLocation(process, base, name);

                // Ensure that content that is retrieved wasn't sneakily different from what was
                // requested (i.e. by some other mechanism of substituting
                if (content != null) {
                    if (!content.getLocation().equals(requestLocation))
                        throw new NotFoundError();

                    return Response.ok(content.getInputStream()).type(content.getContentType()).build();
                }
            }

            LOG.warn("Unable to retrieve static resource for path " + base + "/" + name);
            throw new NotFoundError();
        }

        return null;
    }

    protected static Response response(Resource resource, String mediaType) throws NotFoundError {
        if (resource == null)
            throw new NotFoundError();

        DateTime today = new DateTime();
        return Response
                .ok(new ResourceStreamingOutput(resource), mediaType)
                .expires(today.plusDays(1).toDate())
                .lastModified(lastModified(resource))
                .build();
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

}
