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
package piecework.form.concrete;

import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import piecework.common.view.SearchResults;
import piecework.common.view.ViewContext;
import piecework.exception.StatusCodeError;
import piecework.form.FormResource;
import piecework.form.handler.RequestHandler;
import piecework.form.handler.ResponseHandler;
import piecework.model.Form;
import piecework.model.Process;
import piecework.process.ProcessInstanceService;
import piecework.process.ProcessRepository;
import piecework.process.concrete.ResourceHelper;
import piecework.security.Sanitizer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.*;
import java.util.List;

/**
 * @author James Renfro
 */
@Service
public class FormResourceVersion1 implements FormResource {

    private static final Logger LOG = Logger.getLogger(FormResourceVersion1.class);

    @Autowired
    Environment environment;

    @Autowired
    ProcessRepository processRepository;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    RequestHandler requestHandler;

    @Autowired
    FormService formService;

    @Autowired
    ResourceHelper resourceHelper;

    @Autowired
    ResponseHandler responseHandler;

    @Autowired
    Sanitizer sanitizer;

    @Override
    public Response read(final String rawProcessDefinitionKey, final HttpServletRequest request) throws StatusCodeError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        Process process = resourceHelper.findProcess(processDefinitionKey, true);
        return formService.provideFormResponse(request, getViewContext(), process, null);
    }

    @Override
    public Response read(final String rawProcessDefinitionKey, final List<PathSegment> pathSegments, final HttpServletRequest request) throws StatusCodeError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        Process process = resourceHelper.findProcess(processDefinitionKey, true);
        return formService.provideFormResponse(request, getViewContext(), process, pathSegments);
    }

    @Override
    public Response submit(final String rawProcessDefinitionKey, final String rawRequestId, final HttpServletRequest request, final MultipartBody body) throws StatusCodeError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        Process process = resourceHelper.findProcess(processDefinitionKey, true);
        return formService.submitForm(request, getViewContext(), process, rawRequestId, body);
    }

    @Override
    public Response validate(final String rawProcessDefinitionKey, final String rawRequestId, final String rawValidationId, final HttpServletRequest request, final MultipartBody body) throws StatusCodeError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        Process process = resourceHelper.findProcess(processDefinitionKey, true);
        return formService.validateForm(request, getViewContext(), process, body, rawRequestId, rawValidationId);
    }

    @Override
    public SearchResults search(UriInfo uriInfo) throws StatusCodeError {
        MultivaluedMap<String, String> rawQueryParameters = uriInfo != null ? uriInfo.getQueryParameters() : null;
        return formService.search(rawQueryParameters, getViewContext());
    }

    @Override
	public ViewContext getViewContext() {
        String baseApplicationUri = environment.getProperty("base.application.uri");
		return new ViewContext(baseApplicationUri, null, null, Form.Constants.ROOT_ELEMENT_NAME, "Form");
	}

}
