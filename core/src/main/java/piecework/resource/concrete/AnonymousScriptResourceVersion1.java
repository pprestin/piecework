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
import piecework.content.ContentResource;
import piecework.exception.*;
import piecework.form.FormDisposition;
import piecework.model.*;
import piecework.persistence.ModelProviderFactory;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.resource.AnonymousScriptResource;
import piecework.service.FormTemplateService;
import piecework.service.UserInterfaceService;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Response;

/**
 * @author James Renfro
 */
@Service
public class AnonymousScriptResourceVersion1 extends AbstractScriptResource implements AnonymousScriptResource {

    private static final Logger LOG = Logger.getLogger(AnonymousScriptResourceVersion1.class);

    @Autowired
    private FormTemplateService formTemplateService;

    @Autowired
    private ModelProviderFactory modelProviderFactory;

    @Autowired
    private UserInterfaceService userInterfaceService;

    @Override
    public Response readScript(final String rawProcessDefinitionKey, final MessageContext context) throws PieceworkException {
        Form form = getForm(rawProcessDefinitionKey, context);
        ServletContext servletContext = context.getServletContext();

        ContentResource scriptResource;
        Entity principal = null;
        ProcessDeploymentProvider modelProvider = modelProviderFactory.deploymentProvider(rawProcessDefinitionKey, principal);
        FormDisposition disposition = form.getDisposition();
        if (disposition != null && disposition.getType() == FormDisposition.FormDispositionType.CUSTOM) {
            try {
                ContentResource pageResource = userInterfaceService.getCustomPage(modelProvider, form);
                scriptResource = userInterfaceService.getScriptResource(servletContext, modelProvider, form, pageResource);
            } catch (MisconfiguredProcessException e) {
                throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
            }
        } else {
            scriptResource = userInterfaceService.getScriptResource(servletContext, modelProvider, form);
        }

        return response(scriptResource, "text/javascript");
    }

    @Override
    public Response readStylesheet(final String rawProcessDefinitionKey, final MessageContext context) throws PieceworkException {
        Form form = getForm(rawProcessDefinitionKey, context);
        ServletContext servletContext = context.getServletContext();
        ContentResource stylesheetResource;
        FormDisposition disposition = form.getDisposition();
        Entity principal = null;
        ProcessDeploymentProvider modelProvider = modelProviderFactory.deploymentProvider(rawProcessDefinitionKey, principal);

        if (disposition != null && disposition.getType() == FormDisposition.FormDispositionType.CUSTOM) {
            try {
                ContentResource pageResource = userInterfaceService.getCustomPage(modelProvider, form);
                stylesheetResource = userInterfaceService.getStylesheetResource(servletContext, modelProvider, form, pageResource);
            } catch (MisconfiguredProcessException e) {
                throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
            }
        } else {
            stylesheetResource = userInterfaceService.getStylesheetResource(servletContext, modelProvider, form);
        }

        return response(stylesheetResource, "text/css");
    }

    private Form getForm(final String rawProcessDefinitionKey, final MessageContext context) throws PieceworkException {
        return form(rawProcessDefinitionKey, context, null);
    }

    @Override
    protected boolean isAnonymous() {
        return true;
    }
}
