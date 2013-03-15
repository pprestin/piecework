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
package piecework.form.resource;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import piecework.form.FormRepository;
import piecework.form.FormResourceVersion1;
import piecework.form.FormService;
import piecework.form.record.FormRecord;

/**
 * @author James Renfro
 */
@Service
public class FormResourceVersion1Impl implements FormResourceVersion1 {

	@Autowired 
	private FormService service;
	
	@Autowired 
	private FormRepository repository;
	
	@Value("${statement}") 
	private String statement;
	
	public Response read(String processDefinitionKey) {
		FormRecord form = repository.findOne(processDefinitionKey);
		if (form == null) {
			form = new FormRecord();
			form.setId(processDefinitionKey);
			form.setName("This is a quick test");
			repository.save(form);
			return Response.status(Status.NOT_FOUND).build();
		}
		return Response.ok(statement + " " + form.getName()).build();

	}
	
}
