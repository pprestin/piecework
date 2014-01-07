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
package piecework.resource;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import piecework.ApplicationResource;
import piecework.authorization.AuthorizationRole;
import piecework.exception.PieceworkException;
import piecework.model.SearchResults;
import piecework.exception.StatusCodeError;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

/**
 * @author James Renfro
 */
@Path("form")
public interface FormResource extends ApplicationResource {

    @GET
    @Path("{processDefinitionKey}")
    @RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.INITIATOR})
    @Produces({"text/html","application/json"})
    Response read(@PathParam("processDefinitionKey") String processDefinitionKey, @Context MessageContext context) throws PieceworkException;

    @GET
    @Path("{processDefinitionKey}/{taskId}")
	@RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.INITIATOR, AuthorizationRole.USER})
    @Produces({"text/html","application/json"})
    Response readTask(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("taskId") String taskId, @Context MessageContext context) throws PieceworkException;

    @GET
    @Path("{processDefinitionKey}/receipt/{requestId}")
    @RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.INITIATOR, AuthorizationRole.USER})
    @Produces({"text/html","application/json"})
    Response readReceipt(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("requestId") String requestId, @Context MessageContext context) throws PieceworkException;

    @POST
    @Path("{processDefinitionKey}/save/{requestId}")
    @RolesAllowed({AuthorizationRole.INITIATOR, AuthorizationRole.USER})
    @Consumes("multipart/form-data")
    @Produces({"text/html","application/json"})
    Response save(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("requestId") String requestId, @Context MessageContext context, MultipartBody body) throws PieceworkException;

    @POST
    @Path("{processDefinitionKey}/submission/{requestId}")
    @RolesAllowed({AuthorizationRole.INITIATOR, AuthorizationRole.USER})
    @Consumes("application/x-www-form-urlencoded")
    @Produces({"text/html","application/json"})
    Response submit(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("requestId") String requestId, @Context MessageContext context, MultivaluedMap<String, String> formData) throws PieceworkException;

    @POST
    @Path("{processDefinitionKey}/submission/{requestId}")
    @RolesAllowed({AuthorizationRole.INITIATOR, AuthorizationRole.USER})
    @Consumes("multipart/form-data")
    @Produces({"text/html","application/json"})
    Response submit(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("requestId") String requestId, @Context MessageContext context, MultipartBody body) throws PieceworkException;

    @POST
    @Path("{processDefinitionKey}/submission/{requestId}/{validationId}")
    @RolesAllowed({AuthorizationRole.INITIATOR, AuthorizationRole.USER})
    @Produces("application/json")
    @Consumes("application/x-www-form-urlencoded")
    Response validate(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("requestId") String requestId, @PathParam("validationId") String validationId, @Context MessageContext context, MultivaluedMap<String, String> formData) throws PieceworkException;

    @POST
    @Path("{processDefinitionKey}/submission/{requestId}/{validationId}")
    @RolesAllowed({AuthorizationRole.INITIATOR, AuthorizationRole.USER})
    @Produces("application/json")
    @Consumes("multipart/form-data")
    Response validate(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("requestId") String requestId, @PathParam("validationId") String validationId, @Context MessageContext context, MultipartBody body) throws PieceworkException;

    @GET
    @Path("")
    @RolesAllowed({AuthorizationRole.OVERSEER, AuthorizationRole.USER})
    @Produces({"text/html", "application/json", "text/csv"})
    SearchResults search(@Context MessageContext context) throws PieceworkException;

}