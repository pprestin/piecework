package piecework.resource;

import org.apache.cxf.jaxrs.ext.MessageContext;
import piecework.ApiResource;
import piecework.ApplicationResource;
import piecework.authorization.AuthorizationRole;
import piecework.exception.PieceworkException;
import piecework.model.SearchResults;
import piecework.exception.StatusCodeError;
import piecework.model.Submission;
import piecework.model.Task;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * @author James Renfro
 */
@Path("task")
@Produces({"application/json", "application/xml"})
@Consumes({"application/json", "application/xml"})
public interface TaskResource extends ApplicationResource, ApiResource {

    @GET
    @Path("{processDefinitionKey}/{taskId}")
    @RolesAllowed({AuthorizationRole.USER})
    Response read(@Context MessageContext context, @PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("taskId") String taskId) throws PieceworkException;

    @PUT
    @Path("{processDefinitionKey}/{taskId}")
    @RolesAllowed({AuthorizationRole.INITIATOR})
    @Consumes("multipart/form-data")
    Response update(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("taskId") String taskId, @Context HttpServletRequest request, Task task) throws PieceworkException;

    @POST
    @Path("{processDefinitionKey}/{taskId}/{action}")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.SYSTEM})
    @Consumes("application/x-www-form-urlencoded")
    Response assign(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("taskId") String taskId, @PathParam("action") String action, @Context MessageContext context, @FormParam("assignee") String assignee) throws PieceworkException;

    @POST
    @Path("{processDefinitionKey}/{taskId}/{action}")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.SYSTEM})
    Response complete(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("taskId") String taskId, @PathParam("action") String action, @Context MessageContext context, Submission submission) throws PieceworkException;

    @GET
    @Path("")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.OVERSEER})
    SearchResults search(@Context MessageContext context) throws PieceworkException;

    SearchResults search(MultivaluedMap<String, String> rawQueryParameters) throws PieceworkException;

}
