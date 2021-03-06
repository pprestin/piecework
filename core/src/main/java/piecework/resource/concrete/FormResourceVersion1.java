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
import piecework.authorization.AuthorizationRole;
import piecework.common.FacetFactory;
import piecework.common.SearchCriteria;
import piecework.common.SearchQueryParameters;
import piecework.exception.PieceworkException;
import piecework.identity.IdentityHelper;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessDeploymentProvider;
import piecework.persistence.SearchProvider;
import piecework.resource.FormResource;
import piecework.security.Sanitizer;
import piecework.service.ProcessService;
import piecework.settings.UserInterfaceSettings;
import piecework.util.FormUtility;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Map;
import java.util.Set;

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
    UserInterfaceSettings settings;

    @Autowired
    Versions versions;

    @Override
    public Response readOptions(MessageContext context, String rawProcessDefinitionKey, String taskId, String requestId, String submissionId) throws PieceworkException {
        ProcessDeploymentProvider deploymentProvider = modelProviderFactory.deploymentProvider(rawProcessDefinitionKey, helper.getPrincipal());
        return FormUtility.optionsResponse(settings, deploymentProvider, isAnonymous(), "GET");
    }

    @Override
    public Response read(final MessageContext context, final String rawProcessDefinitionKey, final String rawTaskId, final String rawRequestId, final String rawSubmissionId, final String rawValidationId, final String redirectCount) throws PieceworkException {

        int count = StringUtils.isNotEmpty(redirectCount) ? Integer.valueOf(redirectCount) : 1;

        Entity principal = helper.getPrincipal();

        if (StringUtils.isNotEmpty(rawTaskId))
            return taskForm(context, rawProcessDefinitionKey, rawTaskId, count, principal);
        if (StringUtils.isNotEmpty(rawRequestId))
            return requestForm(context, rawProcessDefinitionKey, rawRequestId, principal);
        if (StringUtils.isNotEmpty(rawValidationId))
            return validationForm(context, rawProcessDefinitionKey, rawValidationId, principal);
        if (StringUtils.isNotEmpty(rawSubmissionId))
            return submissionForm(context, rawProcessDefinitionKey, rawSubmissionId, principal);

        return startForm(context, rawProcessDefinitionKey, principal);
    }

    @Override
    public Response submitOptions(final MessageContext context, final String rawProcessDefinitionKey, final String requestId) throws PieceworkException {
        ProcessDeploymentProvider deploymentProvider = modelProviderFactory.deploymentProvider(rawProcessDefinitionKey, helper.getPrincipal());
        return FormUtility.optionsResponse(settings, deploymentProvider, isAnonymous(), "POST");
    }

    @Override
    public Response submit(final String rawProcessDefinitionKey, final String rawRequestId, final MessageContext context, final MultivaluedMap<String, String> formData) throws PieceworkException {
        return submitForm(context, rawProcessDefinitionKey, rawRequestId, formData, Map.class, helper.getPrincipal());
    }

    @Override
    public Response submit(final String rawProcessDefinitionKey, final String rawRequestId, final MessageContext context, final MultipartBody body) throws PieceworkException {
        return submitForm(context, rawProcessDefinitionKey, rawRequestId, body, MultipartBody.class, helper.getPrincipal());
    }

    @Override
    public Response validate(final String rawProcessDefinitionKey, final String rawRequestId, final String rawValidationId, final MessageContext context, final MultivaluedMap<String, String> formData) throws PieceworkException {
        return validateForm(context, rawProcessDefinitionKey, formData, rawRequestId, rawValidationId, helper.getPrincipal());
    }

    @Override
    public Response validate(final String rawProcessDefinitionKey, final String rawRequestId, final String rawValidationId, final MessageContext context, final MultipartBody body) throws PieceworkException {
        return validateForm(context, rawProcessDefinitionKey, body, rawRequestId, rawValidationId, helper.getPrincipal());
    }

    @Override
    public Response search(MessageContext context, SearchQueryParameters queryParameters) throws PieceworkException {
        long start = 0l;
        if (LOG.isDebugEnabled())
            start = System.currentTimeMillis();

        UriInfo uriInfo = context.getContext(UriInfo.class);
        MultivaluedMap<String, String> rawQueryParameters = uriInfo != null ? uriInfo.getQueryParameters() : null;
        SearchProvider searchProvider = modelProviderFactory.searchProvider(helper.getPrincipal());
        Set<Process> processes = searchProvider.processes(AuthorizationRole.USER, AuthorizationRole.OVERSEER);
        SearchResponse searchResponse = search(context, new SearchCriteria.Builder(rawQueryParameters, processes, FacetFactory.facetMap(processes), sanitizer).build(), helper.getPrincipal());

        Response response = FormUtility.okResponse(settings, searchProvider, searchResponse, null, isAnonymous());

        if (LOG.isDebugEnabled())
            LOG.debug("Search for forms took " + (System.currentTimeMillis() - start) + " ms");

        return response;
    }

    @Override
    protected boolean isAnonymous() {
        return false;
    }

}
