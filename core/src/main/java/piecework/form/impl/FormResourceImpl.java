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
package piecework.form.impl;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import piecework.form.FormRepository;
import piecework.form.FormResource;
import piecework.form.model.FormRecord;

/**
 * @author James Renfro
 */
public class FormResourceImpl implements FormResource {

	@Autowired 
	private FormRepository repository;
	
	@Value("${statement}") 
	private String statement;
	
	@Override
	public Response read(String version, String formDefinitionKey) {
		FormRecord form = repository.findOne(formDefinitionKey);
		if (form == null) {
			form = new FormRecord();
			form.setId(formDefinitionKey);
			form.setName("This is a quick test");
			repository.save(form);
			return Response.status(Status.NOT_FOUND).build();
		}
		return Response.ok(statement + " " + form.getName()).build();
	}

}
