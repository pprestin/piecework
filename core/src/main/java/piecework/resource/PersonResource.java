package piecework.resource;

import piecework.ApiResource;
import piecework.ApplicationResource;
import piecework.authorization.AuthorizationRole;
import piecework.exception.StatusCodeError;
import piecework.identity.PersonSearchCriteria;
import piecework.model.SearchResults;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 * @author James Renfro
 */
@Path("person")
public interface PersonResource extends ApplicationResource, ApiResource {

    @GET
    @Path("")
    @RolesAllowed({AuthorizationRole.USER})
    SearchResults search(@QueryParam("") PersonSearchCriteria criteria) throws StatusCodeError;

}
