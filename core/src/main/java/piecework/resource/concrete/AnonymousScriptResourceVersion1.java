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
import piecework.exception.*;
import piecework.form.FormDisposition;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ModelProviderFactory;
import piecework.repository.ProcessRepository;
import piecework.resource.AnonymousScriptResource;
import piecework.service.FormTemplateService;
import piecework.service.ProcessService;

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

    @Override
    public Response readScript(final String rawProcessDefinitionKey, final MessageContext context) throws PieceworkException {
        Form form = getForm(rawProcessDefinitionKey, context);
        ServletContext servletContext = context.getServletContext();

        Resource scriptResource;
        Entity principal = null;

        FormDisposition disposition = form.getDisposition();
        if (disposition != null && disposition.getType() == FormDisposition.FormDispositionType.CUSTOM) {
            try {
                Resource pageResource = userInterfaceService.getCustomPage(form, null);
                scriptResource = userInterfaceService.getScriptResource(servletContext, form, pageResource, principal);
            } catch (MisconfiguredProcessException e) {
                throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
            }
        } else {
            scriptResource = userInterfaceService.getScriptResource(servletContext, form, principal);
        }

        return response(scriptResource, "text/javascript");
    }

    @Override
    public Response readStylesheet(final String rawProcessDefinitionKey, final MessageContext context) throws PieceworkException {
        Form form = getForm(rawProcessDefinitionKey, context);
        ServletContext servletContext = context.getServletContext();
        Resource stylesheetResource;
        FormDisposition disposition = form.getDisposition();
        Entity principal = null;
        if (disposition != null && disposition.getType() == FormDisposition.FormDispositionType.CUSTOM) {
            try {
                Resource pageResource = userInterfaceService.getCustomPage(form, null);
                stylesheetResource = userInterfaceService.getStylesheetResource(servletContext, form, pageResource, principal);
            } catch (MisconfiguredProcessException e) {
                throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
            }
        } else {
            stylesheetResource = userInterfaceService.getStylesheetResource(servletContext, form, principal);
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
