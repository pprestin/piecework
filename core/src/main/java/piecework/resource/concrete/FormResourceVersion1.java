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

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Versions;
import piecework.exception.PieceworkException;
import piecework.model.Entity;
import piecework.model.SearchResults;
import piecework.resource.FormResource;
import piecework.model.Process;
import piecework.identity.IdentityHelper;
import piecework.security.Sanitizer;
import piecework.service.ProcessService;

import javax.ws.rs.core.*;
import java.util.Map;

/**
 * @author James Renfro
 */
@Service
public class FormResourceVersion1 extends AbstractFormResource implements FormResource {

    private static final Logger LOG = Logger.getLogger(FormResourceVersion1.class);

    @Autowired
    IdentityHelper identityHelper;

    @Autowired
    ProcessService processService;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    Versions versions;

    @Override
    public Response read(final MessageContext context, final String rawProcessDefinitionKey, final String rawTaskId, final String rawRequestId, final String rawSubmissionId) throws PieceworkException {
        Process process = processService.read(rawProcessDefinitionKey);

        if (StringUtils.isNotEmpty(rawTaskId))
            return taskForm(context, process, rawTaskId);
        if (StringUtils.isNotEmpty(rawRequestId))
            return requestForm(context, process, rawRequestId);
        if (StringUtils.isNotEmpty(rawSubmissionId))
            return submissionForm(context, process, rawSubmissionId);

        return startForm(context, process);
    }

    @Override
    public Response submit(final String rawProcessDefinitionKey, final String rawRequestId, final MessageContext context, final MultivaluedMap<String, String> formData) throws PieceworkException {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        Process process = processService.read(rawProcessDefinitionKey);
        return submitForm(context, process, rawRequestId, formData, Map.class);
    }

    @Override
    public Response submit(final String rawProcessDefinitionKey, final String rawRequestId, final MessageContext context, final MultipartBody body) throws PieceworkException {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        Process process = processService.read(rawProcessDefinitionKey);
        return submitForm(context, process, rawRequestId, body, MultipartBody.class);
    }

    @Override
    public Response validate(final String rawProcessDefinitionKey, final String rawRequestId, final String rawValidationId, final MessageContext context, final MultivaluedMap<String, String> formData) throws PieceworkException {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        Process process = processService.read(rawProcessDefinitionKey);
        return validateForm(context, process, formData, rawRequestId, rawValidationId);
    }

    @Override
    public Response validate(final String rawProcessDefinitionKey, final String rawRequestId, final String rawValidationId, final MessageContext context, final MultipartBody body) throws PieceworkException {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        Process process = processService.read(rawProcessDefinitionKey);
        return validateForm(context, process, body, rawRequestId, rawValidationId);
    }

    @Override
    public SearchResults search(MessageContext context) throws PieceworkException {
        UriInfo uriInfo = context.getContext(UriInfo.class);

        MultivaluedMap<String, String> rawQueryParameters = uriInfo != null ? uriInfo.getQueryParameters() : null;
        return search(context, rawQueryParameters);
    }

    @Override
    protected boolean isAnonymous() {
        return false;
    }

}
