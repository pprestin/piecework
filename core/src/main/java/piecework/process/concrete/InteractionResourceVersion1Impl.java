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
package piecework.process.concrete;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import piecework.Sanitizer;
import piecework.common.view.SearchResults;
import piecework.exception.StatusCodeError;
import piecework.process.InteractionResource;
import piecework.process.ProcessService;
import piecework.process.model.view.InteractionView;
import piecework.process.model.view.ProcessView;

/**
 * @author James Renfro
 */
@Service
public class InteractionResourceVersion1Impl implements InteractionResource {
	
	@Autowired
	ProcessService service;
	
	@Autowired
	Sanitizer sanitizer;
	
	@Value("${base.application.uri}")
	String baseApplicationUri;
	
	@Value("${base.service.uri}")
	String baseServiceUri;
	
	@Override
	public Response create(String processDefinitionKey, ProcessView process)
			throws StatusCodeError {
		return null;
	}

	@Override
	public Response read(String processDefinitionKey, String interactionId)
			throws StatusCodeError {
		return null;
	}

	@Override
	public Response update(String processDefinitionKey, String interactionId,
			InteractionView interaction) throws StatusCodeError {
		return null;
	}

	@Override
	public Response delete(String processDefinitionKey, String interactionId)
			throws StatusCodeError {
		return null;
	}

	@Override
	public SearchResults searchInteractions(String processDefinitionKey,
			UriInfo uriInfo) throws StatusCodeError {
		return null;
	}
	
	@Override
	public String getPageName() {
		return "Interaction";
	}

}
