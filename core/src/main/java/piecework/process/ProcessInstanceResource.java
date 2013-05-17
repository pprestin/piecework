package piecework.process;

import piecework.Resource;
import piecework.authorization.AuthorizationRole;
import piecework.common.view.SearchResults;
import piecework.exception.StatusCodeError;
import piecework.process.model.*;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * @author James Renfro
 */
@Path("secure/v1/instance")
public interface ProcessInstanceResource extends Resource {

    @POST
    @Path("{processDefinitionKey}")
    @RolesAllowed({AuthorizationRole.INITIATOR})
    Response create(@PathParam("processDefinitionKey") String processDefinitionKey, ProcessInstance instance) throws StatusCodeError;

    @GET
    @Path("{processDefinitionKey}/{processInstanceId}")
    @RolesAllowed({AuthorizationRole.OVERSEER})
    Response read(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId) throws StatusCodeError;

    @PUT
    @Path("{processDefinitionKey}/{processInstanceId}")
    @RolesAllowed({AuthorizationRole.OVERSEER})
    Response update(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, ProcessInstance instance) throws StatusCodeError;

    @DELETE
    @Path("{processDefinitionKey}/{processInstanceId}")
    @RolesAllowed({AuthorizationRole.OWNER})
    Response delete(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId) throws StatusCodeError;

    @GET
    @Path("")
    @RolesAllowed({AuthorizationRole.OVERSEER})
    SearchResults search(@Context UriInfo uriInfo) throws StatusCodeError;

}
