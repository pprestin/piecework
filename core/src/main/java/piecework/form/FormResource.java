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
package piecework.form;

import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import piecework.ApplicationResource;
import piecework.authorization.AuthorizationRole;
import piecework.model.SearchResults;
import piecework.exception.StatusCodeError;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * @author James Renfro
 */
@Path("form")
public interface FormResource extends ApplicationResource {

    @GET
    @Path("{processDefinitionKey}")
    @RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.INITIATOR})
    @Produces({"text/html","application/json", "application/xml"})
    Response read(@PathParam("processDefinitionKey") String processDefinitionKey, @Context HttpServletRequest request) throws StatusCodeError;

    @GET
	@Path("{processDefinitionKey}/{segments:.*}")
	@RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.INITIATOR, AuthorizationRole.USER})
    @Produces({"text/html","application/json", "application/xml"})
    Response read(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("segments") List<PathSegment> pathSegments, @Context HttpServletRequest request) throws StatusCodeError;

    @POST
    @Path("{processDefinitionKey}/activation/{requestId}")
    @RolesAllowed({AuthorizationRole.OVERSEER})
    @Produces("application/json")
    @Consumes("multipart/form-data")
    Response activate(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("requestId") String requestId, @Context HttpServletRequest request, MultipartBody body) throws StatusCodeError;

    @POST
    @Path("{processDefinitionKey}/attachment/{requestId}")
    @RolesAllowed({AuthorizationRole.INITIATOR, AuthorizationRole.USER})
    @Consumes("multipart/form-data")
    Response attach(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("requestId") String requestId, @Context HttpServletRequest request, MultipartBody body) throws StatusCodeError;

    @POST
    @Path("{processDefinitionKey}/submission/{requestId}")
    @RolesAllowed({AuthorizationRole.INITIATOR, AuthorizationRole.USER})
    @Consumes("multipart/form-data")
    @Produces("text/html")
    Response submit(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("requestId") String requestId, @Context HttpServletRequest request, MultipartBody body) throws StatusCodeError;

    @POST
    @Path("{processDefinitionKey}/submission/{requestId}/{validationId}")
    @RolesAllowed({AuthorizationRole.INITIATOR, AuthorizationRole.USER})
    @Produces("application/json")
    @Consumes("multipart/form-data")
    Response validate(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("requestId") String requestId, @PathParam("validationId") String validationId, @Context HttpServletRequest request, MultipartBody body) throws StatusCodeError;

    @POST
    @Path("{processDefinitionKey}/cancellation/{requestId}")
    @RolesAllowed({AuthorizationRole.OVERSEER})
    @Produces("application/json")
    @Consumes("multipart/form-data")
    Response delete(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("requestId") String requestId, @Context HttpServletRequest request, MultipartBody body) throws StatusCodeError;

    @POST
    @Path("{processDefinitionKey}/suspension/{requestId}")
    @RolesAllowed({AuthorizationRole.OVERSEER})
    @Produces("application/json")
    @Consumes("multipart/form-data")
    Response suspend(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("requestId") String requestId, @Context HttpServletRequest request, MultipartBody body) throws StatusCodeError;

    @GET
    @Path("")
    @RolesAllowed({AuthorizationRole.OVERSEER, AuthorizationRole.USER})
    SearchResults search(@Context UriInfo uriInfo) throws StatusCodeError;

}