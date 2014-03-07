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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Versions;
import piecework.designer.model.view.IndexView;
import piecework.exception.StatusCodeError;
import piecework.resource.DesignerResource;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author James Renfro
 */
@Service
public class DesignerResourceImpl implements DesignerResource {

    @Autowired
    Versions versions;

	@Override
	public Response root() throws StatusCodeError {
		return Response.status(Response.Status.SEE_OTHER).header(HttpHeaders.LOCATION, versions.getVersion1().getApplicationUri("designer")).build();
    }

    @Override
    public IndexView index() throws StatusCodeError {
        return new IndexView();
    }

	@Override
	public IndexView index(List<PathSegment> pathSegments) throws StatusCodeError {
		return new IndexView();
	}

}
