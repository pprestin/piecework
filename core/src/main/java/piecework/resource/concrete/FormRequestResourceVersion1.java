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
import org.cyberneko.html.filters.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Versions;
import piecework.exception.ForbiddenError;
import piecework.exception.NotFoundError;
import piecework.exception.StatusCodeError;
import piecework.handler.RequestHandler;
import piecework.identity.IdentityHelper;
import piecework.model.Entity;
import piecework.model.FormRequest;
import piecework.model.Process;
import piecework.model.RequestDetails;
import piecework.resource.FormRequestResource;
import piecework.service.ProcessService;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author James Renfro
 */
@Service
public class FormRequestResourceVersion1 implements FormRequestResource {

    @Autowired
    IdentityHelper helper;

    @Autowired
    ProcessService processService;

    @Autowired
    RequestHandler requestHandler;

    @Autowired
    Versions versions;

    @Override
    public Response create(String processDefinitionKey, String taskId, MessageContext context, RequestDetails requestDetails) throws StatusCodeError {
        Entity principal = helper.getPrincipal();
        if (principal.getEntityType() != Entity.EntityType.SYSTEM)
            throw new ForbiddenError();
        Process process = processService.read(processDefinitionKey);
        FormRequest request = requestHandler.create(requestDetails, process);
        if (request == null)
            throw new NotFoundError();

        String link = versions.getVersion1().getApplicationUri("resource", process.getProcessDefinitionKey(), request.getRequestId());
        return Response.ok(link, MediaType.TEXT_PLAIN_TYPE).build();
    }

    @Override
    public String getVersion() {
        return versions.getVersion1().getVersion();
    }

}
