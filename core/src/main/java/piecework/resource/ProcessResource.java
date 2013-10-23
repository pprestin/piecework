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
package piecework.resource;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import piecework.ApiResource;
import piecework.ApplicationResource;
import piecework.Resource;
import piecework.authorization.AuthorizationRole;
import piecework.model.*;
import piecework.exception.StatusCodeError;
import piecework.model.Process;

/**
 * @author James Renfro
 */
@Path("process")
public interface ProcessResource extends ApplicationResource, ApiResource {

	@POST
	@Path("")
	@RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.CREATOR})
	Response create(Process process) throws StatusCodeError;
	
	@GET
	@Path("{processDefinitionKey}")
	@RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.CREATOR})
	Response read(@PathParam("processDefinitionKey") String processDefinitionKey) throws StatusCodeError;

    @POST
	@PUT
	@Path("{processDefinitionKey}")
	@RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.CREATOR})
	Response update(@PathParam("processDefinitionKey") String processDefinitionKey, Process process) throws StatusCodeError;
	
	@DELETE
	@Path("{processDefinitionKey}")
	@RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.CREATOR})
	Response delete(@PathParam("processDefinitionKey") String processDefinitionKey) throws StatusCodeError;
	
	@GET
	@Path("")
	@RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.CREATOR})
	SearchResults search(@Context UriInfo uriInfo) throws StatusCodeError;

    /*
     * SUBRESOURCES
     */
    @POST
    @Path("{processDefinitionKey}/deployment")
    @RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.CREATOR})
    Response createDeployment(@PathParam("processDefinitionKey") String processDefinitionKey) throws StatusCodeError;

    @POST
    @Path("{processDefinitionKey}/deployment/{deploymentId}")
    @RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.CREATOR})
    Response cloneDeployment(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("deploymentId") String deploymentId) throws StatusCodeError;

    @GET
    @Path("{processDefinitionKey}/deployment/{deploymentId}")
    @RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.CREATOR})
    Response getDeployment(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("deploymentId") String deploymentId) throws StatusCodeError;

    @PUT
    @Path("{processDefinitionKey}/deployment/{deploymentId}")
    @RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.CREATOR})
    Response updateDeployment(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("deploymentId") String deploymentId, ProcessDeployment deployment) throws StatusCodeError;

    @DELETE
    @Path("{processDefinitionKey}/deployment/{deploymentId}")
    @RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.CREATOR})
    Response deleteDeployment(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("deploymentId") String deploymentId) throws StatusCodeError;

    @POST
    @Path("{processDefinitionKey}/deploy/{deploymentId}")
    @RolesAllowed({AuthorizationRole.OWNER})
    Response publishDeployment(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("deploymentId") String deploymentId) throws StatusCodeError;

    @GET
    @Path("{processDefinitionKey}/deployment")
    @RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.CREATOR})
    SearchResults searchDeployments(@PathParam("processDefinitionKey") String processDefinitionKey,@Context UriInfo uriInfo) throws StatusCodeError;

    /*
     * SUB-SUBRESOURCES
     */
    @GET
    @Path("{processDefinitionKey}/deployment/{deploymentId}/resource")
    @RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.CREATOR})
    Response getDeploymentResource(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("deploymentId") String deploymentId) throws StatusCodeError;

    @GET
    @Path("{processDefinitionKey}/deployment/{deploymentId}/diagram")
    @RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.CREATOR})
    @Produces({"image/png"})
    Response getDiagram(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("deploymentId") String deploymentId) throws StatusCodeError;

    @GET
    @Path("{processDefinitionKey}/deployment/{deploymentId}/interaction/{interactionId}")
    @RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.CREATOR})
    Response getInteraction(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("deploymentId") String deploymentId, @PathParam("interactionId") String interactionId) throws StatusCodeError;

    @DELETE
    @Path("{processDefinitionKey}/deployment/{deploymentId}/interaction/{interactionId}")
    @RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.CREATOR})
    Response deleteInteraction(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("deploymentId") String deploymentId, @PathParam("interactionId") String interactionId) throws StatusCodeError;

    @DELETE
    @Path("{processDefinitionKey}/deployment/{deploymentId}/interaction/{interactionId}/screen/{actionTypeId}/grouping/{groupingId}/section/{sectionId}")
    @RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.CREATOR})
    Response deleteSection(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("deploymentId") String deploymentId, @PathParam("interactionId") String interactionId, @PathParam("actionTypeId") String actionTypeId, @PathParam("groupingId") String groupingId, @PathParam("sectionId") String sectionId) throws StatusCodeError;

    @DELETE
    @Path("{processDefinitionKey}/deployment/{deploymentId}/section/{sectionId}/field/{fieldId}")
    @RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.CREATOR})
    Response deleteField(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("deploymentId") String deploymentId, @PathParam("interactionId") String interactionId, @PathParam("sectionId") String sectionId, @PathParam("fieldId") String fieldId) throws StatusCodeError;

    @POST
    @PUT
    @Path("{processDefinitionKey}/deployment/{deploymentId}/interaction/{interactionId}")
    @RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.CREATOR})
    Response updateInteraction(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("deploymentId") String deploymentId, @PathParam("interactionId") String interactionId, Interaction interaction) throws StatusCodeError;

    @POST
    @PUT
    @Path("{processDefinitionKey}/deployment/{deploymentId}/section/{sectionId}")
    @RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.CREATOR})
    Response updateSection(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("deploymentId") String deploymentId, @PathParam("sectionId") String sectionId, Section section) throws StatusCodeError;

    @POST
    @PUT
    @Path("{processDefinitionKey}/deployment/{deploymentId}/section/{sectionId}/field/{fieldId}")
    @RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.CREATOR})
    Response updateField(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("deploymentId") String deploymentId, @PathParam("interactionId") String interactionId, @PathParam("sectionId") String sectionId, @PathParam("fieldId") String fieldId, Field field) throws StatusCodeError;


}
