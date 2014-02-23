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

@Path("subtask")
@Produces({"application/json", "application/xml"})
@Consumes({"application/json", "application/xml"})
public interface SubTaskResource extends ApplicationResource, ApiResource {

    @POST
    @Path("{processDefinitionKey}/{parentTaskId}")
    @RolesAllowed({AuthorizationRole.INITIATOR})
    @Consumes({"application/xml","application/json"})
    Response create(@Context MessageContext context, @PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("parentTaskId") String taskId, Submission submission) throws StatusCodeError;

}
