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

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.authorization.AuthorizationRole;
import piecework.form.FormDisposition;
import piecework.persistence.ModelProviderFactory;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.service.RequestService;
import piecework.exception.*;
import piecework.identity.IdentityHelper;
import piecework.model.*;
import piecework.model.Process;
import piecework.resource.ScriptResource;
import piecework.security.Sanitizer;
import piecework.service.UserInterfaceService;
import piecework.settings.SecuritySettings;
import piecework.service.FormTemplateService;
import piecework.util.UserInterfaceUtility;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * @author James Renfro
 */
@Service
public class ScriptResourceVersion1 extends AbstractScriptResource implements ScriptResource {

    private static final Logger LOG = Logger.getLogger(ScriptResourceVersion1.class);

    @Autowired
    private FormTemplateService formTemplateService;

    @Autowired
    private IdentityHelper identityHelper;

    @Autowired
    private ModelProviderFactory modelProviderFactory;

    @Autowired
    private RequestService requestService;

    @Autowired
    private Sanitizer sanitizer;

    @Autowired
    private SecuritySettings securitySettings;

    @Autowired
    private UserInterfaceService userInterfaceService;

    @Override
    public Response readScript(final String rawProcessDefinitionKey, final MessageContext context) throws PieceworkException {
        String scriptId = sanitizer.sanitize(rawProcessDefinitionKey);
        String templateName = UserInterfaceUtility.templateName(scriptId, isAnonymous());

        ProcessDeploymentProvider modelProvider = modelProviderFactory.deploymentProvider(rawProcessDefinitionKey, identityHelper.getPrincipal());
        Resource scriptResource;
        Entity principal = modelProvider.principal();
        if (templateName != null) {
            ServletContext servletContext = context.getServletContext();
            scriptResource = userInterfaceService.getScriptResource(servletContext, modelProvider, templateName, isAnonymous());
        } else {
            Form form = getForm(rawProcessDefinitionKey, identityHelper.getPrincipal(), context);
            ServletContext servletContext = context.getServletContext();

            FormDisposition disposition = form.getDisposition();
            if (disposition != null && disposition.getType() == FormDisposition.FormDispositionType.CUSTOM) {
                try {
                    Resource pageResource = userInterfaceService.getCustomPage(modelProvider, form);
                    scriptResource = userInterfaceService.getScriptResource(servletContext, modelProvider, form, pageResource);
                } catch (MisconfiguredProcessException e) {
                    throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
                }
            } else {
                scriptResource = userInterfaceService.getScriptResource(servletContext, modelProvider, form);
            }
        }

        return response(scriptResource, "text/javascript");
    }

    @Override
    public Response readStylesheet(final String rawProcessDefinitionKey, final MessageContext context) throws PieceworkException {
        String stylesheetId = sanitizer.sanitize(rawProcessDefinitionKey);
        String templateName = UserInterfaceUtility.templateName(stylesheetId, isAnonymous());

        Resource stylesheetResource;
        Entity principal = identityHelper.getPrincipal();
        ProcessDeploymentProvider modelProvider = modelProviderFactory.deploymentProvider(rawProcessDefinitionKey, principal);
        if (templateName != null) {
            ServletContext servletContext = context.getServletContext();
            stylesheetResource = userInterfaceService.getStylesheetResource(servletContext, modelProvider, templateName);
        } else {
            Form form = getForm(rawProcessDefinitionKey, identityHelper.getPrincipal(), context);
            ServletContext servletContext = context.getServletContext();
            FormDisposition disposition = form.getDisposition();
            if (disposition != null && disposition.getType() == FormDisposition.FormDispositionType.CUSTOM) {
                try {
                    Resource pageResource = userInterfaceService.getCustomPage(modelProvider, form);
                    stylesheetResource = userInterfaceService.getStylesheetResource(servletContext, modelProvider, form, pageResource);
                } catch (MisconfiguredProcessException e) {
                    throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
                }
            } else {
                stylesheetResource = userInterfaceService.getStylesheetResource(servletContext, modelProvider, form);
            }
        }

        return response(stylesheetResource, "text/css");
    }


//    @Override
//    public Response read(String rawProcessDefinitionKey, String rawRequestId, MessageContext context) throws StatusCodeError {
//        Entity principal = identityHelper.getPrincipal();
//        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
//        String requestId = sanitizer.sanitize(rawRequestId);
//        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
//        FormRequest request = requestService.read(requestDetails, requestId);
//        return response(request, principal, new MediaType("text", "javascript"));
//    }

/*    @Override
    public Response readScript(String rawScriptId, MessageContext context) throws StatusCodeError {
        String scriptId = sanitizer.sanitize(rawScriptId);
//        Entity principal = identityHelper.getPrincipal();
        String templateName = UserInterfaceUtility.templateName(scriptId, isAnonymous());
        ServletContext servletContext = context.getServletContext();
        if (templateName == null) {
            throw new NotFoundError();
//            Form form = getForm(scriptId, principal, context);
//            return processScript(servletContext, form);
        }

        Resource scriptResource = userInterfaceService.getScriptResource(servletContext, templateName, isAnonymous());
        return response(scriptResource, "text/javascript");
    }

    @Override
    public Response readStylesheet(String rawStylesheetId, MessageContext context) throws StatusCodeError {
        String stylesheetId = sanitizer.sanitize(rawStylesheetId);
//        Entity principal = identityHelper.getPrincipal();
        String templateName = UserInterfaceUtility.templateName(stylesheetId, isAnonymous());
        ServletContext servletContext = context.getServletContext();
        if (templateName == null) {
            throw new NotFoundError();
//            Form form = getForm(stylesheetId, principal, context);
//            return processStylesheet(servletContext, form);
        }

        Resource stylesheetResource = userInterfaceService.getStylesheetResource(servletContext, templateName);
        return response(stylesheetResource, "text/css");
    } */

//    @Override
//    public Response readStatic(final String rawProcessDefinitionKey, final List<PathSegment> pathSegments, final MessageContext context) throws StatusCodeError {
//        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
//        Process process = identityHelper.findProcess(processDefinitionKey, true);
//        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
//        return staticResponse(process, requestDetails, pathSegments);
//    }

    @Override
    protected boolean isAnonymous() {
        return false;
    }

    private Form getForm(String processDefinitionKey, Entity principal, MessageContext context) throws PieceworkException {
        if (principal == null)
            throw new ForbiddenError();

        Set<String> processDefinitionKeys = principal.getProcessDefinitionKeys(AuthorizationRole.OVERSEER, AuthorizationRole.USER);
        if (!processDefinitionKeys.contains(processDefinitionKey))
            throw new ForbiddenError();

        Process process = identityHelper.findProcess(processDefinitionKey, true);
        if (process == null)
            throw new NotFoundError();

        return form(processDefinitionKey, context, principal);
    }

}
