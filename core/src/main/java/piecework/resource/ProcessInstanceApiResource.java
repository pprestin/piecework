package piecework.resource;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import piecework.ApiResource;
import piecework.authorization.AuthorizationRole;
import piecework.exception.PieceworkException;
import piecework.model.OperationDetails;
import piecework.model.ProcessInstance;
import piecework.model.Submission;
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
public interface ProcessInstanceApiResource extends ApiResource {

    @POST
    @Path("{processDefinitionKey}")
    @RolesAllowed({AuthorizationRole.INITIATOR})
    @Consumes({"application/json"})
    Response create(@Context MessageContext context, @PathParam("processDefinitionKey") String processDefinitionKey, Submission submission) throws PieceworkException;

    @POST
    @Path("{processDefinitionKey}")
    @RolesAllowed({AuthorizationRole.INITIATOR})
    @Consumes("application/x-www-form-urlencoded")
    Response create(@Context MessageContext context, @PathParam("processDefinitionKey") String processDefinitionKey, MultivaluedMap<String, String> formData) throws PieceworkException;

    @POST
    @Path("{processDefinitionKey}")
    @RolesAllowed({AuthorizationRole.INITIATOR})
    @Consumes("multipart/form-data")
    Response createMultipart(@Context MessageContext context, @PathParam("processDefinitionKey") String processDefinitionKey, MultipartBody body) throws PieceworkException;

    @GET
    @Path("{processDefinitionKey}/{processInstanceId}")
    @RolesAllowed({AuthorizationRole.ADMIN})
    Response read(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId) throws PieceworkException;

    @PUT
    @Path("{processDefinitionKey}/{processInstanceId}")
    @RolesAllowed({AuthorizationRole.ADMIN})
    @Consumes({"application/json"})
    Response update(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, ProcessInstance instance) throws PieceworkException;

    @DELETE
    @Path("{processDefinitionKey}/{processInstanceId}")
    @RolesAllowed({AuthorizationRole.ADMIN})
    @Consumes({"application/json"})
    Response delete(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId) throws PieceworkException;

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
    @Consumes({"application/json"})
    Response activate(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, OperationDetails reason) throws PieceworkException;

    @GET
    @Path("{processDefinitionKey}/{processInstanceId}/attachment")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.OVERSEER})
    Response attachments(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, @QueryParam("") AttachmentQueryParameters queryParameters) throws PieceworkException;

    @GET
    @Path("{processDefinitionKey}/{processInstanceId}/attachment/{attachmentId}")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.OVERSEER})
    Response attachment(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, @PathParam("attachmentId") String attachmentId) throws PieceworkException;

    @POST
    @Path("{processDefinitionKey}/{processInstanceId}/cancellation")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.OVERSEER})
    @Consumes({"application/json"})
    Response cancel(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, OperationDetails reason) throws PieceworkException;

    @DELETE
    @Path("{processDefinitionKey}/{processInstanceId}/attachment/{attachmentId}")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.OVERSEER})
    Response detach(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, @PathParam("attachmentId") String attachmentId) throws PieceworkException;

    @DELETE
    @Path("{processDefinitionKey}/{processInstanceId}/value/{fieldName}/{valueId}")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.OVERSEER})
    Response remove(@Context MessageContext context, @PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, @PathParam("fieldName") String fieldName, @PathParam("valueId") String valueId) throws PieceworkException;

    @POST
    @Path("{processDefinitionKey}/{processInstanceId}/restart")
    @RolesAllowed({AuthorizationRole.ADMIN})
    @Consumes({"application/json"})
    Response restart(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, OperationDetails reason) throws PieceworkException;

    @POST
    @Path("{processDefinitionKey}/{processInstanceId}/suspension")
    @RolesAllowed({AuthorizationRole.ADMIN})
    @Consumes({"application/json"})
    Response suspend(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId, OperationDetails reason) throws PieceworkException;


}
