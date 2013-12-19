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
import org.springframework.stereotype.Service;
import piecework.exception.*;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessRepository;
import piecework.resource.AnonymousScriptResource;
import piecework.service.FormTemplateService;

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

    @Override
    public Response readScript(final String rawProcessDefinitionKey, final MessageContext context) throws StatusCodeError {
        Form form = getForm(rawProcessDefinitionKey, context);
        ServletContext servletContext = context.getServletContext();
        return processScript(servletContext, form);
    }

    @Override
    public Response readStylesheet(final String rawProcessDefinitionKey, final MessageContext context) throws StatusCodeError {
        Form form = getForm(rawProcessDefinitionKey, context);
        ServletContext servletContext = context.getServletContext();
        return processStylesheet(servletContext, form);
    }

    private Form getForm(final String rawProcessDefinitionKey, final MessageContext context) throws NotFoundError {
        Process process = verifyProcessAllowsAnonymousSubmission(rawProcessDefinitionKey);
        return form(process, context, null);
    }

    private Process verifyProcessAllowsAnonymousSubmission(final String rawProcessDefinitionKey) throws NotFoundError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        Process process = processRepository.findOne(processDefinitionKey);

        if (process == null)
            throw new NotFoundError();

        // Since this is a public resource, don't provide any additional information back beyond the fact that this form does not exist
        if (!process.isAnonymousSubmissionAllowed())
            throw new NotFoundError();

        return process;
    }

    @Override
    protected boolean isAnonymous() {
        return true;
    }
}
