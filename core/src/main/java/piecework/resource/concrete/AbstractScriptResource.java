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

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import piecework.Constants;
import piecework.enumeration.ActionType;
import piecework.enumeration.DataInjectionStrategy;
import piecework.exception.*;
import piecework.form.FormFactory;
import piecework.model.*;
import piecework.model.Process;
import piecework.service.DeploymentService;
import piecework.service.ProcessService;
import piecework.service.UserInterfaceService;
import piecework.ui.streaming.ResourceStreamingOutput;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.util.Date;

/**
 * @author James Renfro
 */
public abstract class AbstractScriptResource {

    private static final Logger LOG = Logger.getLogger(AbstractScriptResource.class);

    @Autowired
    DeploymentService deploymentService;

    @Autowired
    protected FormFactory formFactory;

    @Autowired
    ProcessService processService;

    @Autowired
    protected UserInterfaceService userInterfaceService;

    protected abstract boolean isAnonymous();

    protected Response response(FormRequest request, Entity principal, MediaType mediaType) throws StatusCodeError {
        if (request == null)
            throw new NotFoundError();

        try {
            ActionType actionType = request.getAction();
            Process process = processService.read(request.getProcessDefinitionKey());
            ProcessDeployment deployment = deploymentService.read(process, request.getInstance());

            // Don't include restricted data in a script form
            boolean includeRestrictedData = false;
            Form form = formFactory.form(process, deployment, request, actionType, principal, mediaType, null, null, includeRestrictedData, isAnonymous());

            Activity activity = request.getActivity();
            Action action = activity.action(actionType);

            try {
                if (action.getStrategy() == DataInjectionStrategy.INCLUDE_SCRIPT) {
                    StreamingOutput externalScript = userInterfaceService.getExternalScriptAsStreaming(Form.class, form);
                    if (externalScript != null) {
                        CacheControl cacheControl = new CacheControl();
                        cacheControl.setNoCache(true);
                        cacheControl.setNoStore(true);
                        return Response.ok(externalScript, new MediaType("text", "javascript")).cacheControl(cacheControl).build();
                    }
                }
            } catch (IOException e) {
                throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
            }
        } catch (MisconfiguredProcessException mpe) {
            LOG.error("Process is misconfigured", mpe);
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
        } catch (FormBuildingException e) {
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
        }

        throw new NotFoundError();
    }

    protected static Response response(Resource resource, String mediaType) {
        DateTime today = new DateTime();
        return Response
                .ok(new ResourceStreamingOutput(resource), mediaType)
                .expires(today.plusDays(1).toDate())
                .lastModified(lastModified(resource))
                .build();
    }

    private static Date lastModified(Resource resource) {
        DateTime lastModified;
        try {
            lastModified = new DateTime(resource.lastModified());
        } catch (IOException ioe) {
            lastModified = new DateTime();
        }
        return lastModified.toDate();
    }

}
