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

import org.apache.commons.lang.NotImplementedException;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.authorization.AuthorizationRole;
import piecework.common.FacetFactory;
import piecework.common.SearchCriteria;
import piecework.common.SearchQueryParameters;
import piecework.common.ViewContext;
import piecework.exception.NotFoundError;
import piecework.exception.PieceworkException;
import piecework.exception.StatusCodeError;
import piecework.identity.IdentityHelper;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ModelProviderFactory;
import piecework.persistence.SearchProvider;
import piecework.persistence.TaskProvider;
import piecework.resource.TaskResource;
import piecework.security.AccessTracker;
import piecework.security.Sanitizer;
import piecework.service.TaskService;
import piecework.settings.SecuritySettings;
import piecework.settings.UserInterfaceSettings;
import piecework.util.FormUtility;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Set;

/**
 * @author James Renfro
 */
@Service
public class TaskResourceVersion1 implements TaskResource {

    private static final Logger LOG = Logger.getLogger(TaskResourceVersion1.class);
    private static final String VERSION = "v1";

    @Autowired
    IdentityHelper helper;

    @Autowired
    ModelProviderFactory modelProviderFactory;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    SecuritySettings securitySettings;

    @Autowired
    TaskService taskService;

    @Autowired
    UserInterfaceSettings settings;


    @Override
    public Response assign(String rawProcessDefinitionKey, String rawTaskId, String rawAction, MessageContext context, String rawAssigneeId) throws PieceworkException {
        String assigneeId = sanitizer.sanitize(rawAssigneeId);
        Submission submission = new Submission.Builder()
                .assignee(assigneeId)
                .build();
        return complete(rawProcessDefinitionKey, rawTaskId, rawAction, context, submission);
    }

    @Override
    public Response complete(String rawProcessDefinitionKey, String rawTaskId, String rawAction, MessageContext context, Submission rawSubmission) throws PieceworkException {
        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        TaskProvider taskProvider = taskService.complete(rawProcessDefinitionKey, rawTaskId, rawAction, rawSubmission, requestDetails, helper.getPrincipal());
        return FormUtility.noContentResponse(settings, taskProvider, false);
    }

    public Response read(MessageContext context, String rawProcessDefinitionKey, String rawTaskId) throws PieceworkException {
        TaskProvider taskProvider = modelProviderFactory.taskProvider(rawProcessDefinitionKey, rawTaskId, helper.getPrincipal());
        Task task = taskProvider.task(new ViewContext(settings, VERSION), false);
        if (task == null)
            throw new NotFoundError();

        return FormUtility.okResponse(settings, taskProvider, task, null, false);
    }

    @Override
    public Response update(String processDefinitionKey, String taskId, HttpServletRequest request, Task task) throws StatusCodeError {
        throw new NotImplementedException();
    }

    @Override
    public SearchResults search(MessageContext context, SearchQueryParameters queryParameters) throws PieceworkException {
        UriInfo uriInfo = context != null ? context.getContext(UriInfo.class) : null;
        MultivaluedMap<String, String> rawQueryParameters = uriInfo != null ? uriInfo.getQueryParameters() : null;
        SearchProvider searchProvider = modelProviderFactory.searchProvider(helper.getPrincipal());
        Set<Process> processes = searchProvider.processes(AuthorizationRole.USER, AuthorizationRole.OVERSEER);
        return search(new SearchCriteria.Builder(rawQueryParameters, processes, FacetFactory.facetMap(processes), sanitizer).build());
    }

    public SearchResults search(SearchCriteria criteria) throws PieceworkException {
        ViewContext version = new ViewContext(settings, VERSION);
        SearchProvider searchProvider = modelProviderFactory.searchProvider(helper.getPrincipal());
        return searchProvider.tasks(criteria, version);
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

}
