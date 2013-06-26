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
import piecework.PublicApplicationResource;
import piecework.authorization.AuthorizationRole;
import piecework.common.view.SearchResults;
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
//@Produces("text/html")
public interface FormResource extends ApplicationResource {

    @GET
    @Path("{processDefinitionKey}")
    @RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.INITIATOR})
    Response read(@PathParam("processDefinitionKey") String processDefinitionKey, @Context HttpServletRequest request) throws StatusCodeError;

    @GET
	@Path("{processDefinitionKey}/{segments:.*}")
	@RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.INITIATOR})
    Response read(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("segments") List<PathSegment> pathSegments, @Context HttpServletRequest request) throws StatusCodeError;

    @POST
    @Path("{processDefinitionKey}/{requestId}")
    @RolesAllowed({AuthorizationRole.INITIATOR})
    @Consumes("multipart/form-data")
    Response submit(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("requestId") String requestId, @Context HttpServletRequest request, MultipartBody body) throws StatusCodeError;

    @POST
    @Path("{processDefinitionKey}/{requestId}/{validationId}")
    @RolesAllowed({AuthorizationRole.INITIATOR})
    @Produces("application/json")
    @Consumes("multipart/form-data")
    Response validate(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("requestId") String requestId, @PathParam("validationId") String validationId, @Context HttpServletRequest request, MultipartBody body) throws StatusCodeError;

    @GET
    @Path("")
    @RolesAllowed({AuthorizationRole.OVERSEER, AuthorizationRole.USER})
    SearchResults search(@Context UriInfo uriInfo) throws StatusCodeError;

}