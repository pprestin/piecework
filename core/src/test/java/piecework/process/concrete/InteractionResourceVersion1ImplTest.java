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

import piecework.ApplicationConfigurationForUnitTest;
import piecework.exception.GoneError;
import piecework.exception.StatusCodeError;
import piecework.process.InteractionResource;
import piecework.process.ProcessResource;
import piecework.process.model.Interaction;
import piecework.process.model.Process;

/**
 * @author James Renfro
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={ApplicationConfigurationForUnitTest.class})
@ActiveProfiles("test")
public class InteractionResourceVersion1ImplTest {

	@Autowired
	ProcessResource processResource;
	
	@Autowired
	InteractionResource interactionResource;
	
	String exampleProcessDefinitionKey;
	String exampleProcessLabel;
	
	@Before
	public void setup() throws StatusCodeError {
		this.exampleProcessDefinitionKey = "demo";
		this.exampleProcessLabel = "Testing";
		
		Process process = new Process.Builder().processDefinitionKey(exampleProcessDefinitionKey)
				.processLabel(exampleProcessLabel).build();
		Response response = processResource.create(process);
		Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
		Assert.assertEquals("/piecework/secure/v1/process/demo", response.getLocation().toString());
	}
	
	@Test
	public void testCreateReadUpdateAndDeleteInteraction() throws StatusCodeError {
		// Create
		Interaction interaction = new Interaction.Builder().build();
		Response response = interactionResource.create(exampleProcessDefinitionKey, interaction);
		Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
		
		Interaction actual = (Interaction) response.getEntity();
		Assert.assertEquals("/piecework/secure/v1/interaction/demo/" + actual.getId(), actual.getUri());

		// Read
		response = interactionResource.read(exampleProcessDefinitionKey, actual.getId());
		Interaction result = (Interaction) response.getEntity();
		Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
		Assert.assertEquals("/piecework/secure/v1/interaction/demo/" + result.getId(), result.getUri());
		
		// Update
		Interaction updated = new Interaction.Builder().id(actual.getId()).label("New Label").build();
		response = interactionResource.update(exampleProcessDefinitionKey, actual.getId(), updated);
		result = (Interaction) response.getEntity();
		Assert.assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());
		
		// Read again
		response = interactionResource.read(exampleProcessDefinitionKey, actual.getId());
		result = (Interaction) response.getEntity();
		Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
		Assert.assertEquals("New Label", result.getLabel());
		
		// Delete
		response = interactionResource.delete("demo", actual.getId());
		result = (Interaction) response.getEntity();
		Assert.assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());

		// Read one final time
		try {
			response = interactionResource.read(exampleProcessDefinitionKey, actual.getId());
			Assert.fail("Didn't throw an exception that the process was gone");
		} catch (GoneError e) {
			// Ok, it worked
		} 
	}

}
