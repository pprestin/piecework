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
import piecework.content.ContentResource;
import piecework.enumeration.ActionType;
import piecework.exception.*;
import piecework.form.FormFactory;
import piecework.model.*;
import piecework.model.Form;
import piecework.model.Process;
import piecework.persistence.ModelProviderFactory;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.repository.ContentRepository;
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

    private static final String VERSION = "v1";
    private static final Logger LOG = Logger.getLogger(AbstractScriptResource.class);

    @Autowired
    ContentRepository contentRepository;

    @Autowired
    protected FormFactory formFactory;

    @Autowired
    private ModelProviderFactory modelProviderFactory;

    @Autowired
    private RequestService requestService;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    SecuritySettings securitySettings;


    protected abstract boolean isAnonymous();

    protected Form form(final String processDefinitionKey, final MessageContext context, final Entity principal) throws PieceworkException {
        try {
            ProcessDeploymentProvider deploymentProvider = modelProviderFactory.deploymentProvider(processDefinitionKey, principal);
            boolean includeRestrictedData = false;
            RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
            FormRequest request = requestService.create(requestDetails, deploymentProvider);
            Form form = formFactory.form(deploymentProvider, request, ActionType.CREATE, null, null, includeRestrictedData, isAnonymous(), VERSION);
            return form;
        } catch (MisconfiguredProcessException mpe) {
            LOG.error("Unable to create new instance because process is misconfigured", mpe);
            throw new NotFoundError();
        }
    }

//    protected Response staticResponse(final Process process, final RequestDetails requestDetails, final List<PathSegment> pathSegments, Entity principal) throws StatusCodeError {
//        Iterator<PathSegment> pathSegmentIterator = pathSegments.iterator();
//
//        if (pathSegmentIterator.hasNext()) {
//            String name = "";
//            while (pathSegmentIterator.hasNext()) {
//                String segment = sanitizer.sanitize(pathSegmentIterator.next().getPath());
//                // Don't include empty segments or segments that might move us up the file system tree
//                // (above the base, for example)
//                if (StringUtils.isEmpty(segment) || segment.contains(".."))
//                    continue;
//
//                name += segment;
//                if (pathSegmentIterator.hasNext())
//                    name += "/";
//            }
//            ProcessDeployment detail = process.getDeployment();
//            if (detail == null)
//                throw new ConflictError();
//
//            String base = detail.getBase();
//
//            // This base ensures that each process's resources is separated from each other process's resources
//            // and in the case of anonymous requests, that we're not retrieving a non-anonymous process's
//            // resource thru the public url
//            if (StringUtils.isNotEmpty(base)) {
//                String requestLocation = base + "/" + name;
//                // Note that the content providers should be doing additional checking to ensure
//                // that resources are retrieved only from below the approved filesystem base and root
//                Content content = contentRepository.findByLocation(modelProvider, name);
//
//                // Ensure that content that is retrieved wasn't sneakily different from what was
//                // requested (i.e. by some other mechanism of substituting
//                if (content != null) {
//                    if (!content.getLocation().equals(requestLocation))
//                        throw new NotFoundError();
//
//                    return Response.ok(content.getInputStream()).type(content.getContentType()).build();
//                }
//            }
//
//            LOG.warn("Unable to retrieve static resource for path " + base + "/" + name);
//            throw new NotFoundError();
//        }
//
//        return null;
//    }

    protected static Response response(ContentResource resource, String mediaType) throws NotFoundError {
        if (resource == null)
            throw new NotFoundError();

        DateTime today = new DateTime();
        Date lastModifiedDate = resource.lastModified() >= 0 ? new Date(resource.lastModified()) : null;
        return Response
                .ok(resource, mediaType)
                .expires(today.plusDays(1).toDate())
                .lastModified(lastModifiedDate)
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
