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
import piecework.exception.*;
import piecework.form.FormDisposition;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessRepository;
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

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private ProcessService processService;

    @Override
    public Response readScript(final String rawProcessDefinitionKey, final MessageContext context) throws StatusCodeError {
        Form form = getForm(rawProcessDefinitionKey, context);
        ServletContext servletContext = context.getServletContext();
        Resource scriptResource = userInterfaceService.getScriptResource(servletContext, form);
        return response(scriptResource, "text/javascript");
    }

    @Override
    public Response readStylesheet(final String rawProcessDefinitionKey, final MessageContext context) throws StatusCodeError {
        Form form = getForm(rawProcessDefinitionKey, context);
        ServletContext servletContext = context.getServletContext();
        Resource stylesheetResource = userInterfaceService.getStylesheetResource(servletContext, form);
        return response(stylesheetResource, "text/css");
//        return processStylesheet(servletContext, form);
    }

    private Form getForm(final String rawProcessDefinitionKey, final MessageContext context) throws StatusCodeError {
        Process process = processService.read(rawProcessDefinitionKey);
        return form(process, context, null);
    }

    @Override
    protected boolean isAnonymous() {
        return true;
    }
}
