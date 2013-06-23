package piecework.task;

import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import piecework.ApiResource;
import piecework.ApplicationResource;
import piecework.Resource;
import piecework.authorization.AuthorizationRole;
import piecework.common.view.SearchResults;
import piecework.exception.StatusCodeError;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * @author James Renfro
 */
@Path("task")
public interface TaskResource extends ApiResource {

    @GET
    @Path("{processDefinitionKey}/{taskId}")
    @RolesAllowed({AuthorizationRole.USER})
    Response read(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("taskId") String taskId) throws StatusCodeError;

    @PUT
    @Path("{processDefinitionKey}/{taskId}")
    @RolesAllowed({AuthorizationRole.INITIATOR})
    @Consumes("multipart/form-data")
    Response update(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("taskId") String taskId, @Context HttpServletRequest request, MultipartBody body) throws StatusCodeError;

    @POST
    @Path("{processDefinitionKey}/{taskId}/{action}")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.SYSTEM})
    Response complete(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("taskId") String taskId, @PathParam("action") String action, @Context HttpServletRequest request, MultipartBody body) throws StatusCodeError;

    @GET
    @Path("")
    @RolesAllowed({AuthorizationRole.OVERSEER})
    SearchResults search(@Context UriInfo uriInfo) throws StatusCodeError;

}
