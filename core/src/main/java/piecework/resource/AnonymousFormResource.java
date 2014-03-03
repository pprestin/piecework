/*
 * Copyright 2012 University of Washington
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package piecework.resource;

import org.apache.cxf.jaxrs.ext.MessageContext;
import piecework.PublicApplicationResource;
import piecework.exception.PieceworkException;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * @author James Renfro
 */
@Path("form")
public interface AnonymousFormResource extends PublicApplicationResource {

    @GET
    @Path("{processDefinitionKey}")
    @Produces("text/html")
    Response read(@PathParam("processDefinitionKey") String processDefinitionKey, @Context MessageContext context) throws PieceworkException;

    @POST
    @Path("{processDefinitionKey}/{requestId}")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("text/html")
    Response submit(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("requestId") String requestId, @Context MessageContext context, MultivaluedMap<String, String> formData) throws PieceworkException;

    @POST
    @Path("{processDefinitionKey}/{requestId}/{validationId}")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    Response validate(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("requestId") String requestId, @PathParam("validationId") String validationId, @Context MessageContext context, MultivaluedMap<String, String> formData) throws PieceworkException;

}