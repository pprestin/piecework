package piecework.resource;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.cxf.rs.security.cors.LocalPreflight;
import piecework.ApplicationResource;
import piecework.authorization.AuthorizationRole;
import piecework.exception.PieceworkException;
import piecework.process.AttachmentQueryParameters;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * @author James Renfro
 */
@Path("instance")
@Produces({"application/json"})
public interface ProcessInstanceApplicationResource extends ApplicationResource {

    @GET
    @Path("")
    @RolesAllowed({AuthorizationRole.ADMIN})
    @Produces({"application/json", "text/csv", "application/vnd.ms-excel"})
    Response search(@Context MessageContext context) throws PieceworkException;

    /*
     * SUBRESOURCES
     */
    @POST
    @Path("{processDefinitionKey}/{processInstanceId}/activation")
    @RolesAllowed({AuthorizationRole.ADMIN})
    @Consumes({"application/x-www-form-urlencoded"})
    Response activate(@Context MessageContext context, @PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, @FormParam("reason") String reason) throws PieceworkException;

    @POST
    @Path("{processDefinitionKey}/{processInstanceId}/attachment")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.OVERSEER})
    @Consumes("application/x-www-form-urlencoded")
    Response attach(@Context MessageContext context, @PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, MultivaluedMap<String, String> parameters) throws PieceworkException;

    @POST
    @Path("{processDefinitionKey}/{processInstanceId}/attachment")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.OVERSEER})
    @Consumes("multipart/form-data")
    Response attach(@Context MessageContext context, @PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, MultipartBody body) throws PieceworkException;

    @OPTIONS
    @Path("{processDefinitionKey}/{processInstanceId}/attachment")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.OVERSEER})
    @Consumes({"*/*"})
    @LocalPreflight
    Response attachOptions(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId) throws PieceworkException;

    @GET
    @Path("{processDefinitionKey}/{processInstanceId}/attachment")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.OVERSEER})
    Response attachments(@Context MessageContext context, @PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, @QueryParam("") AttachmentQueryParameters queryParameters) throws PieceworkException;

    @GET
    @Path("{processDefinitionKey}/{processInstanceId}/attachment/{attachmentId}")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.OVERSEER})
    Response attachment(@Context MessageContext context, @PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, @PathParam("attachmentId") String attachmentId) throws PieceworkException;

    @POST
    @Path("{processDefinitionKey}/{processInstanceId}/cancellation")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.ADMIN})
    @Consumes("application/x-www-form-urlencoded")
    Response cancel(@Context MessageContext context, @PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, @FormParam("reason") String reason) throws PieceworkException;

    @POST
    @Path("{processDefinitionKey}/{processInstanceId}/attachment/{attachmentId}/removal")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.OVERSEER})
    @Consumes("application/x-www-form-urlencoded")
    Response detachment(@Context MessageContext context, @PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, @PathParam("attachmentId") String attachmentId) throws PieceworkException;

    @DELETE
    @Path("{processDefinitionKey}/{processInstanceId}/attachment/{attachmentId}")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.OVERSEER})
    Response detach(@Context MessageContext context, @PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, @PathParam("attachmentId") String attachmentId) throws PieceworkException;

    @GET
    @Path("{processDefinitionKey}/{processInstanceId}/diagram.png")
    @RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.CREATOR})
    @Produces({"image/png"})
    Response diagram(@Context MessageContext context, @PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId) throws PieceworkException;

    @GET
    @Path("{processDefinitionKey}/{processInstanceId}/history")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.OVERSEER})
    Response history(@Context MessageContext context, @PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId) throws PieceworkException;

    @POST
    @Path("{processDefinitionKey}/{processInstanceId}/value/{fieldName}/{valueId}/checkout")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.OVERSEER})
    @Consumes("application/x-www-form-urlencoded")
    Response checkout(@Context MessageContext context, @PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, @PathParam("fieldName") String fieldName, @PathParam("valueId") String valueId) throws PieceworkException;

    @POST
    @Path("{processDefinitionKey}/{processInstanceId}/value/{fieldName}/{valueId}/removal")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.OVERSEER})
    @Consumes("application/x-www-form-urlencoded")
    Response removal(@Context MessageContext context, @PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, @PathParam("fieldName") String fieldName, @PathParam("valueId") String valueId) throws PieceworkException;

    @DELETE
    @Path("{processDefinitionKey}/{processInstanceId}/value/{fieldName}/{valueId}")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.OVERSEER})
    Response remove(@Context MessageContext context, @PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, @PathParam("fieldName") String fieldName, @PathParam("valueId") String valueId) throws PieceworkException;

    @OPTIONS
    @Path("{processDefinitionKey}/{processInstanceId}/value/{fieldName}/{valueId}")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.OVERSEER})
    @Consumes({"*/*"})
    @LocalPreflight
    Response removeOptions(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, @PathParam("fieldName") String fieldName, @PathParam("valueId") String valueId) throws PieceworkException;

    @POST
    @Path("{processDefinitionKey}/{processInstanceId}/suspension")
    @RolesAllowed({AuthorizationRole.ADMIN})
    @Consumes({"application/x-www-form-urlencoded"})
    Response suspend(@Context MessageContext context, @PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, @FormParam("reason") String reason) throws PieceworkException;

    @POST
    @Path("{processDefinitionKey}/{processInstanceId}/restart")
    @RolesAllowed({AuthorizationRole.ADMIN})
    @Consumes({"application/x-www-form-urlencoded"})
    Response restart(@Context MessageContext context, @PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, @FormParam("reason") String reason) throws PieceworkException;

    @OPTIONS
    @Path("{processDefinitionKey}/{processInstanceId}/suspension")
    @RolesAllowed({AuthorizationRole.ADMIN})
    @Consumes({"application/json"})
    @LocalPreflight
    Response suspendOptions(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId) throws PieceworkException;

    @GET
    @Path("{processDefinitionKey}/{processInstanceId}/value/{fieldName}/{valueId}")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.OVERSEER})
    @Produces("*/*")
    Response readValue(@Context MessageContext context, @PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, @PathParam("fieldName") String fieldName, @PathParam("valueId") String valueId, @QueryParam("inline") Boolean inline) throws PieceworkException;

    @POST
    @Path("{processDefinitionKey}/{processInstanceId}/value/{fieldName}")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.OVERSEER})
    @Consumes("text/plain")
    Response value(@Context MessageContext context, @PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, @PathParam("fieldName") String fieldName, String value) throws PieceworkException;

    @POST
    @Path("{processDefinitionKey}/{processInstanceId}/value/{fieldName}")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.OVERSEER})
    @Consumes("multipart/form-data")
    Response value(@Context MessageContext context, @PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, @PathParam("fieldName") String fieldName, MultipartBody body) throws PieceworkException;

    @GET
    @Path("{processDefinitionKey}/{processInstanceId}/value/{fieldId}")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.OVERSEER})
    Response values(@Context MessageContext context, @PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, @PathParam("fieldId") String fieldId) throws PieceworkException;

}
