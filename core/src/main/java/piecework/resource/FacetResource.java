package piecework.resource;

import org.apache.cxf.jaxrs.ext.MessageContext;
import piecework.ApplicationResource;
import piecework.authorization.AuthorizationRole;
import piecework.exception.PieceworkException;
import piecework.model.Authorization;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * @author James Renfro
 */
@Path("facet")
public interface FacetResource extends ApplicationResource {

    @GET
    @Path("")
    @RolesAllowed({AuthorizationRole.USER, AuthorizationRole.OVERSEER})
    @Produces("application/json")
    Response search(@Context MessageContext context, @QueryParam("label") String label) throws PieceworkException;

}
