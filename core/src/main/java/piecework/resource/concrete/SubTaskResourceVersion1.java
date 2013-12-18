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
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.Versions;
import piecework.resource.SubTaskResource;
import piecework.service.*;
import piecework.enumeration.ActionType;
import piecework.model.SearchResults;
import piecework.common.ViewContext;
import piecework.exception.*;
import piecework.identity.IdentityHelper;
import piecework.security.SecuritySettings;
import piecework.model.*;
import piecework.security.Sanitizer;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.resource.TaskResource;
import piecework.submission.SubmissionHandler;
import piecework.submission.SubmissionHandlerRegistry;
import piecework.submission.SubmissionTemplate;
import piecework.submission.SubmissionTemplateFactory;
import piecework.validation.ValidationFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Service
public class SubTaskResourceVersion1 implements SubTaskResource {

    private static final Logger LOG = Logger.getLogger(SubTaskResourceVersion1.class);

    @Autowired
    DeploymentService deploymentService;

    @Autowired
    IdentityHelper helper;

    @Autowired
    ProcessService processService;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    RequestService requestService;

    @Autowired
    Sanitizer sanitizer;

    @Autowired
    SecuritySettings securitySettings;

    @Autowired
    SubmissionHandlerRegistry submissionHandlerRegistry;

    @Autowired
    SubmissionTemplateFactory submissionTemplateFactory;

    @Autowired
    TaskService taskService;

    @Autowired
    ValidationFactory validationFactory;

    @Autowired
    Versions versions;

    @Override
    public Response create(String rawProcessDefinitionKey, String rawTaskId, MessageContext context, Submission rawSubmission) throws StatusCodeError {
        String processDefinitionKey = sanitizer.sanitize(rawProcessDefinitionKey);
        String taskId = sanitizer.sanitize(rawTaskId);

        piecework.model.Process process = processService.read(processDefinitionKey);

        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();

        String actingUser = null;
        Entity principal = helper.getPrincipal();
        if (principal != null) {
            if (principal.getEntityType() == Entity.EntityType.SYSTEM && StringUtils.isNotEmpty(requestDetails.getActAsUser()))
                actingUser = requestDetails.getActAsUser();
            else
                actingUser = principal.getEntityId();
        }

        Task task = taskId != null ? taskService.read(process, taskId, true) : null;

        if (task == null)
            throw new NotFoundError();

        ProcessInstance instance = null;

        if (task != null && task.getProcessInstanceId() != null)
            instance = processInstanceService.read(process, task.getProcessInstanceId(), false);

        SubmissionHandler handler = submissionHandlerRegistry.handler(Submission.class);

        try{
            FormRequest formRequest = requestService.create(requestDetails, process, instance, task, ActionType.SUBCREATE);
            ProcessDeployment deployment = deploymentService.read(process, instance);

            SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, deployment, formRequest);
            Submission submission = handler.handle(rawSubmission, template, principal);


            processInstanceService.createSubTask(principal, process, instance, task, taskId, template, submission);

            return Response.noContent().build();
        } catch (Exception mpe) {
            LOG.error("Unable to create subtask", mpe);
            throw new InternalServerError(Constants.ExceptionCodes.subtask_create_invalid);
        }
    }

    @Override
    public String getVersion() {
        return versions.getVersion1().getVersion();
    }

}
