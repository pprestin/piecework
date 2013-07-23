package piecework.process;

import piecework.ApiResource;
import piecework.ApplicationResource;
import piecework.authorization.AuthorizationRole;
import piecework.model.SearchResults;
import piecework.exception.StatusCodeError;
import piecework.model.ProcessInstance;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;

/**
 * @author James Renfro
 */
@Path("instance")
public interface ProcessInstanceResource extends ApplicationResource, ApiResource {


    @POST
    @Path("{processDefinitionKey}")
    @RolesAllowed({AuthorizationRole.INITIATOR})
    @Consumes({"application/xml","application/json"})
    Response create(@Context HttpServletRequest request, @PathParam("processDefinitionKey") String processDefinitionKey, ProcessInstance instance) throws StatusCodeError;

    @POST
	@Path("{processDefinitionKey}")
	@RolesAllowed({AuthorizationRole.INITIATOR})
	@Consumes("application/x-www-form-urlencoded")
    Response create(@Context HttpServletRequest request, @PathParam("processDefinitionKey") String processDefinitionKey, MultivaluedMap<String, String> formData) throws StatusCodeError;
	
	@POST
	@Path("{processDefinitionKey}")
	@RolesAllowed({AuthorizationRole.INITIATOR})
	@Consumes("multipart/form-data")
	Response createMultipart(@Context HttpServletRequest request, @PathParam("processDefinitionKey") String processDefinitionKey, MultipartBody body) throws StatusCodeError;
	
    @GET
    @Path("{processDefinitionKey}/{processInstanceId}")
    @RolesAllowed({AuthorizationRole.OVERSEER})
    Response read(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId) throws StatusCodeError;

    @PUT
    @Path("{processDefinitionKey}/{processInstanceId}")
    @RolesAllowed({AuthorizationRole.OVERSEER})
    @Consumes({"application/json", "application/xml"})
    Response update(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, ProcessInstance instance) throws StatusCodeError;

    @DELETE
    @Path("{processDefinitionKey}/{processInstanceId}/{reason}")
    @RolesAllowed({AuthorizationRole.OVERSEER})
    Response delete(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, @PathParam("reason") String reason) throws StatusCodeError;

    @GET
    @Path("")
    @RolesAllowed({AuthorizationRole.OVERSEER})
    SearchResults search(@Context UriInfo uriInfo) throws StatusCodeError;


    /*
     * SUBRESOURCES
     */
    @POST
    @Path("{processDefinitionKey}/{processInstanceId}/activation")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.OVERSEER})
    @Consumes("application/x-www-form-urlencoded")
    Response activate(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, @FormParam("reason") String reason) throws StatusCodeError;

    @POST
    @Path("{processDefinitionKey}/{processInstanceId}/attachment")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.OVERSEER})
    @Consumes("application/x-www-form-urlencoded")
    Response attach(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, MultivaluedMap<String, String> parameters) throws StatusCodeError;

    @POST
    @Path("{processDefinitionKey}/{processInstanceId}/attachment")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.OVERSEER})
    @Consumes("multipart/form-data")
    Response attach(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, MultipartBody body) throws StatusCodeError;

    @GET
    @Path("{processDefinitionKey}/{processInstanceId}/attachment")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.OVERSEER})
    Response attachments(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, @QueryParam("") AttachmentQueryParameters queryParameters) throws StatusCodeError;

    @GET
    @Path("{processDefinitionKey}/{processInstanceId}/attachment/{attachmentId}")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.OVERSEER})
    Response attachment(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, @PathParam("attachmentId") String attachmentId) throws StatusCodeError;

    @POST
    @Path("{processDefinitionKey}/{processInstanceId}/cancellation")
    @RolesAllowed({AuthorizationRole.OVERSEER})
    @Consumes("application/x-www-form-urlencoded")
    Response cancel(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, @FormParam("reason") String reason) throws StatusCodeError;

    @GET
    @Path("{processDefinitionKey}/{processInstanceId}/history")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.OVERSEER})
    Response history(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId) throws StatusCodeError;

    @POST
    @Path("{processDefinitionKey}/{processInstanceId}/suspension")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.OVERSEER})
    @Consumes("application/x-www-form-urlencoded")
    Response suspend(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, @FormParam("reason") String reason) throws StatusCodeError;

}
