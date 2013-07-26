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
import javax.ws.rs.core.Response.Status;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import piecework.test.config.UnitTestConfiguration;
import piecework.exception.GoneError;
import piecework.exception.StatusCodeError;
import piecework.model.Process;
import piecework.process.ProcessResource;

/**
 * @author James Renfro
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={UnitTestConfiguration.class})
@ActiveProfiles("test")
public class ProcessResourceVersion1ImplTest {

	@Autowired
	ProcessResource resource;
	
	String exampleProcessDefinitionKey;
	String exampleProcessLabel;
	
	@Before
	public void setup() {
		this.exampleProcessDefinitionKey = "demo";
		this.exampleProcessLabel = "Testing";
	}
	
	@Test
	public void testCreateReadUpdateAndDeleteProcess() throws StatusCodeError {
		// Create
		Process process = new Process.Builder().processDefinitionKey(exampleProcessDefinitionKey)
				.processDefinitionLabel(exampleProcessLabel).build();
		Response response = resource.create(process);
		Process result = (Process) response.getEntity();
		Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
		Assert.assertEquals(exampleProcessDefinitionKey, result.getProcessDefinitionKey());
		Assert.assertEquals(exampleProcessLabel, result.getProcessDefinitionLabel());
		Assert.assertEquals("/piecework/api/v1/process/demo", result.getUri());
		
		// Read
		response = resource.read(exampleProcessDefinitionKey);
		result = (Process) response.getEntity();
		Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
		Assert.assertEquals(exampleProcessDefinitionKey, result.getProcessDefinitionKey());
		Assert.assertEquals(exampleProcessLabel, result.getProcessDefinitionLabel());
		
		// Update
		Process updated = new Process.Builder().processDefinitionKey(exampleProcessDefinitionKey)
				.processDefinitionLabel("New Label").build();
		response = resource.update("demo", updated);
		result = (Process) response.getEntity();
		Assert.assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());
		
		// Read again
		response = resource.read(exampleProcessDefinitionKey);
		result = (Process) response.getEntity();
		Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
		Assert.assertEquals(exampleProcessDefinitionKey, result.getProcessDefinitionKey());
		Assert.assertEquals("New Label", result.getProcessDefinitionLabel());
		
		// Delete
		response = resource.delete("demo");
		result = (Process) response.getEntity();
		Assert.assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());

		// Read one final time
		try {
			response = resource.read(exampleProcessDefinitionKey);
			Assert.fail("Didn't throw an exception that the process was gone");
		} catch (GoneError e) {
			// Ok, it worked
		} 
	}

}
