package piecework.resource;

import org.apache.cxf.jaxrs.ext.MessageContext;
import piecework.ApiResource;
import piecework.ApplicationResource;
import piecework.authorization.AuthorizationRole;
import piecework.exception.StatusCodeError;
import piecework.model.Submission;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

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
