package piecework.resource;

import org.apache.cxf.jaxrs.ext.MessageContext;
import piecework.ApplicationResource;
import piecework.authorization.AuthorizationRole;
import piecework.exception.PieceworkException;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * @author James Renfro
 */
@Path("report")
public interface ReportResource extends ApplicationResource {

    @GET
    @Path("")
    @RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.OVERSEER})
    @Produces({"text/html"})
    Response read() throws PieceworkException;

    @GET
    @Path("{processDefinitionKey}/{reportName}")
    @RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.OVERSEER})
    @Produces({"application/json"})
    Response read(@Context MessageContext context, @PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("reportName") String reportName) throws PieceworkException;

}
