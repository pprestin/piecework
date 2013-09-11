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
package piecework.resource.concrete;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import piecework.common.ViewContext;
import piecework.resource.DesignerResource;
import piecework.designer.model.view.IndexView;
import piecework.exception.StatusCodeError;

/**
 * @author James Renfro
 */
@Service
public class DesignerResourceImpl implements DesignerResource {

	@Override
	public Response root() throws StatusCodeError {
		return Response.seeOther(UriBuilder.fromPath("designer").build()).build();
	}
	
	@Override
	public IndexView index() throws StatusCodeError {
		return new IndexView();
	}

}
