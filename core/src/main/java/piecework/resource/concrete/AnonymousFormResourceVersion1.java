/*
 * Copyright 2012 University of Washington
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

import javax.ws.rs.core.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import piecework.persistence.ProcessRepository;
import piecework.exception.*;
import piecework.model.Process;
import piecework.resource.AnonymousFormResource;
import piecework.security.AccessTracker;

import java.util.Map;

/**
 * @author James Renfro
 */
@Service
public class AnonymousFormResourceVersion1 extends AbstractFormResource implements AnonymousFormResource {

    private static final Logger LOG = Logger.getLogger(FormResourceVersion1.class);

    @Autowired
    ProcessRepository processRepository;

    @Override
    public Response read(final String rawProcessDefinitionKey, final MessageContext context) throws PieceworkException {
        Process process = verifyProcessAllowsAnonymousSubmission(rawProcessDefinitionKey);

        return startForm(context, process);
    }

    @Override
    public Response submit(final String rawProcessDefinitionKey, final String rawRequestId, final MessageContext context, final MultivaluedMap<String, String> formData) throws StatusCodeError {
        Process process = verifyProcessAllowsAnonymousSubmission(rawProcessDefinitionKey);

        return submitForm(context, process, rawRequestId, formData, Map.class);
    }

    @Override
    public Response validate(final String rawProcessDefinitionKey, final String rawRequestId, final String rawValidationId, final MessageContext context, final MultivaluedMap<String, String> formData) throws PieceworkException {
        Process process = verifyProcessAllowsAnonymousSubmission(rawProcessDefinitionKey);

        return validateForm(context, process, formData, rawRequestId, rawValidationId);
    }

    @Override
    protected boolean isAnonymous() {
        return true;
    }

    private Process verifyProcessAllowsAnonymousSubmission(final String rawProcessDefinitionKey) throws StatusCodeError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        Process process = processRepository.findOne(processDefinitionKey);

        if (process == null)
            throw new NotFoundError();

        // Since this is a public resource, don't provide any additional information back beyond the fact that this form does not exist
        if (!process.isAnonymousSubmissionAllowed())
            throw new NotFoundError();

        return process;
    }
}
