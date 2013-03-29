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
package piecework.form;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import piecework.Resource;
import piecework.authorization.AuthorizationRole;
import piecework.exception.StatusCodeError;
import piecework.form.model.view.FormView;

/**
 * @author James Renfro
 */
@Path("secure/v1/form")
public interface FormResourceVersion1 extends Resource {

	@GET
	@Path("{processDefinitionKey}")
	@RolesAllowed({AuthorizationRole.OWNER, AuthorizationRole.INITIATOR})
	public FormView read(@PathParam("processDefinitionKey") String processDefinitionKey) throws StatusCodeError;
	
	
	@GET
	@Path("{processDefinitionKey}/{processInstanceId}")
	@RolesAllowed({AuthorizationRole.USER})
	public FormView read(@PathParam("processDefinitionKey") String processDefinitionKey, @PathParam("processInstanceId") String processInstanceId) throws StatusCodeError;

}
