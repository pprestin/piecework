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

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Versions;
import piecework.service.LegacyFormService;
import piecework.model.SearchResults;
import piecework.common.ViewContext;
import piecework.exception.StatusCodeError;
import piecework.resource.FormResource;
import piecework.model.Process;
import piecework.identity.IdentityHelper;
import piecework.security.Sanitizer;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.*;
import java.util.List;

/**
 * @author James Renfro
 */
@Service
public class FormResourceVersion1 extends AbstractFormResource implements FormResource {

    private static final Logger LOG = Logger.getLogger(FormResourceVersion1.class);

    @Autowired
    IdentityHelper identityHelper;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    Versions versions;

    @Override
    public Response read(final String rawProcessDefinitionKey, final MessageContext context) throws StatusCodeError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        Process process = identityHelper.findProcess(processDefinitionKey, true);
        return startForm(context, process);
    }

    @Override
    public Response readTask(final String rawProcessDefinitionKey, final String taskId, final MessageContext context) throws StatusCodeError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        Process process = identityHelper.findProcess(processDefinitionKey, true);
        return taskForm(context, process, taskId);
    }

    @Override
    public Response readRequest(final String rawProcessDefinitionKey, final String requestId, final MessageContext context) throws StatusCodeError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        Process process = identityHelper.findProcess(processDefinitionKey, true);
        return requestForm(context, process, requestId);
    }

    @Override
    public Response save(final String rawProcessDefinitionKey, final String rawRequestId, final MessageContext context, final MultipartBody body) throws StatusCodeError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        Process process = identityHelper.findProcess(processDefinitionKey, true);
        return saveForm(context, process, rawRequestId, body);
    }

    @Override
    public Response submit(final String rawProcessDefinitionKey, final String rawRequestId, final MessageContext context, final MultipartBody body) throws StatusCodeError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        Process process = identityHelper.findProcess(processDefinitionKey, true);
        return submitForm(context, process, rawRequestId, body);
    }

    @Override
    public Response validate(final String rawProcessDefinitionKey, final String rawRequestId, final String rawValidationId, final MessageContext context, final MultipartBody body) throws StatusCodeError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        Process process = identityHelper.findProcess(processDefinitionKey, true);
        return validateForm(context, process, body, rawRequestId, rawValidationId);
    }

    @Override
    public SearchResults search(MessageContext context) throws StatusCodeError {
        UriInfo uriInfo = context.getContext(UriInfo.class);

        MultivaluedMap<String, String> rawQueryParameters = uriInfo != null ? uriInfo.getQueryParameters() : null;
        return search(rawQueryParameters);
    }

}
