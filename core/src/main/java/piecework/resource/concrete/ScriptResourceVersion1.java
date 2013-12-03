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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import piecework.service.RequestService;
import piecework.model.RequestDetails;
import piecework.exception.*;
import piecework.identity.IdentityHelper;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ContentRepository;
import piecework.resource.ScriptResource;
import piecework.security.Sanitizer;
import piecework.security.SecuritySettings;
import piecework.service.FormTemplateService;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * @author James Renfro
 */
@Service
public class ScriptResourceVersion1 extends AbstractScriptResource implements ScriptResource {

    private static final Logger LOG = Logger.getLogger(ScriptResourceVersion1.class);

    @Autowired
    ContentRepository contentRepository;

    @Autowired
    FormTemplateService formTemplateService;

    @Autowired
    IdentityHelper identityHelper;

    @Autowired
    RequestService requestService;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    SecuritySettings securitySettings;

    @Override
    public Response read(String rawProcessDefinitionKey, String rawRequestId, MessageContext context) throws StatusCodeError {
        Entity principal = identityHelper.getPrincipal();
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        String requestId = sanitizer.sanitize(rawRequestId);
        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        FormRequest request = requestService.read(requestDetails, requestId);

        return response(request, principal, new MediaType("text", "javascript"));
    }

    @Override
    public Response readScript(String scriptId) throws StatusCodeError {
        String templateName = formTemplateService.getTemplateName(scriptId, isAnonymous());

        Resource scriptResource = userInterfaceService.getScriptResource(templateName);
        return response(scriptResource, "text/javascript");
    }

    @Override
    public Response readStylesheet(String stylesheetId) throws StatusCodeError {
        String templateName = formTemplateService.getTemplateName(stylesheetId, isAnonymous());

        Resource stylesheetResource = userInterfaceService.getStylesheetResource(templateName);
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

            LOG.warn("Unable to retrieve static resource for path " + base + "/" + name);
            throw new NotFoundError();
        }

        return null;
    }

    @Override
    protected boolean isAnonymous() {
        return false;
    }
}
