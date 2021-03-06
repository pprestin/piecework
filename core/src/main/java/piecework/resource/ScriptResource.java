/*
 * Copyright 2013 University of Washington
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
import piecework.ApplicationResource;
import piecework.exception.PieceworkException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * @author James Renfro
 */
@Path("resource")
public interface ScriptResource extends ApplicationResource {


    @GET
    @Path("css/{processDefinitionKey}.css")
    @Produces({"text/css"})
    Response readStylesheet(@PathParam("processDefinitionKey") String processDefinitionKey, @Context MessageContext context) throws PieceworkException;

    @GET
    @Path("script/{processDefinitionKey}.js")
    @Produces({"text/javascript"})
    Response readScript(@PathParam("processDefinitionKey") String processDefinitionKey, @Context MessageContext context) throws PieceworkException;

}
