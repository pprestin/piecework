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
import org.apache.cxf.rs.security.cors.LocalPreflight;
import piecework.ApplicationResource;
import piecework.authorization.AuthorizationRole;
import piecework.common.SearchQueryParameters;
import piecework.exception.PieceworkException;
import piecework.model.SearchResults;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * @author James Renfro
 */
@Path("form")
public interface FormResource extends ApplicationResource {

    @OPTIONS
    @Path("{processDefinitionKey}")
    @RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.INITIATOR, AuthorizationRole.USER})
    @Consumes("*/*")
    @Produces("*/*")
    @LocalPreflight
    Response readOptions(@Context MessageContext context, @PathParam("processDefinitionKey") String processDefinitionKey, @QueryParam("taskId") String taskId, @QueryParam("requestId") String requestId, @QueryParam("submissionId") String submissionId) throws PieceworkException;

    @GET
    @Path("{processDefinitionKey}")
    @RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.INITIATOR, AuthorizationRole.USER})
    @Produces({"text/html","application/json"})
    Response read(@Context MessageContext context, @PathParam("processDefinitionKey") String processDefinitionKey, @QueryParam("taskId") String taskId, @QueryParam("requestId") String requestId, @QueryParam("submissionId") String submissionId, @QueryParam("validationId") String validationId, @QueryParam("redirectCount") String redirectCount) throws PieceworkException;

    @OPTIONS
    @Path("{processDefinitionKey}/{requestId}")
    @RolesAllowed({AuthorizationRole.INITIATOR, AuthorizationRole.USER})
    @Consumes("*/*")
    @Produces("*/*")
    @LocalPreflight
    Response submitOptions(@Context MessageContext context, @PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("requestId") String requestId) throws PieceworkException;

    @POST
    @Path("{processDefinitionKey}/{requestId}")
    @RolesAllowed({AuthorizationRole.INITIATOR, AuthorizationRole.USER})
    @Consumes("application/x-www-form-urlencoded")
    @Produces({"text/html","application/json"})
    Response submit(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("requestId") String requestId, @Context MessageContext context, MultivaluedMap<String, String> formData) throws PieceworkException;

    @POST
    @Path("{processDefinitionKey}/{requestId}")
    @RolesAllowed({AuthorizationRole.INITIATOR, AuthorizationRole.USER})
    @Consumes("multipart/form-data")
    @Produces({"text/html","application/json"})
    Response submit(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("requestId") String requestId, @Context MessageContext context, MultipartBody body) throws PieceworkException;

    @POST
    @Path("{processDefinitionKey}/{requestId}/{validationId}")
    @RolesAllowed({AuthorizationRole.INITIATOR, AuthorizationRole.USER})
    @Produces("application/json")
    @Consumes("application/x-www-form-urlencoded")
    Response validate(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("requestId") String requestId, @PathParam("validationId") String validationId, @Context MessageContext context, MultivaluedMap<String, String> formData) throws PieceworkException;

    @POST
    @Path("{processDefinitionKey}/{requestId}/{validationId}")
    @RolesAllowed({AuthorizationRole.INITIATOR, AuthorizationRole.USER})
    @Produces("application/json")
    @Consumes("multipart/form-data")
    Response validate(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("requestId") String requestId, @PathParam("validationId") String validationId, @Context MessageContext context, MultipartBody body) throws PieceworkException;

    @GET
    @Path("")
    @RolesAllowed({AuthorizationRole.OVERSEER, AuthorizationRole.USER})
    @Produces({"text/html", "application/json", "text/csv"})
    SearchResults search(@Context MessageContext context, @QueryParam("") SearchQueryParameters queryParameters) throws PieceworkException;

}