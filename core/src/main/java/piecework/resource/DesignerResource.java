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

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import piecework.ApplicationResource;
import piecework.Resource;
import piecework.authorization.AuthorizationRole;
import piecework.designer.model.view.IndexView;
import piecework.exception.StatusCodeError;

/**
 * @author James Renfro
 */
@Path("")
public interface DesignerResource extends ApplicationResource {

	@GET
	@Path("")
	@RolesAllowed({AuthorizationRole.USER})
	public Response root() throws StatusCodeError;
	
	@GET
	@Path("designer")
	@RolesAllowed({AuthorizationRole.USER})
	public IndexView index() throws StatusCodeError;
		
}
