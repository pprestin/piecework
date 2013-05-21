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
package piecework.engine;

import java.io.IOException;
import java.util.Map;

import junit.framework.Assert;

import org.activiti.engine.RepositoryService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import piecework.engine.config.TestConfiguration;
import piecework.model.ProcessInstance;
import piecework.util.ManyMap;

/**
 * @author James Renfro
 */
@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes={EngineConfiguration.class}, initializers={PropertySourceInitializer.class})
@ContextConfiguration(classes={TestConfiguration.class})
@ActiveProfiles("test")
public class ActivitiEngineProxyTest {

	private static final String EXAMPLE_PROCESS_DEFINITION_KEY = "example";
	
	@Autowired
	ProcessEngineProxy engineProxy;
	
	@Autowired
	RepositoryService repositoryService;
	
	@Before
	public void setup() throws IOException {
		ClassPathResource resource = new ClassPathResource("META-INF/example.bpmn20.xml");
		repositoryService.createDeployment().name(EXAMPLE_PROCESS_DEFINITION_KEY).addInputStream("example.bpmn20.xml", resource.getInputStream()).deploy();
	}
	
	@Test
	public void testStartWithNoData() {
		ProcessInstance instance = engineProxy.start(EXAMPLE_PROCESS_DEFINITION_KEY, null, null);
		Assert.assertNotNull(instance.getProcessInstanceId());
		Assert.assertNull(instance.getAlias());
	}
	
	@Test
	public void testStartWithAliasAndNoData() {
		ProcessInstance instance = engineProxy.start(EXAMPLE_PROCESS_DEFINITION_KEY, "test1", null);
		Assert.assertNotNull(instance.getProcessInstanceId());
		Assert.assertNotNull(instance.getAlias());
		Assert.assertEquals("test1", instance.getAlias());
	}
	
	@Test
	public void testStartWithAliasAndSomeData() {
		Map<String, ?> data = new ManyMap<String, String>();
		((ManyMap<String, String>)data).putOne("InitiatorId", "testuser");
		ProcessInstance instance = engineProxy.start(EXAMPLE_PROCESS_DEFINITION_KEY, "test1", data);
		Assert.assertNotNull(instance.getProcessInstanceId());
		Assert.assertNotNull(instance.getAlias());
		Assert.assertEquals("test1", instance.getAlias());
		
		instance = engineProxy.findInstance(EXAMPLE_PROCESS_DEFINITION_KEY, instance.getProcessInstanceId(), null, true);
		Assert.assertEquals("testuser", instance.getFormData().get(0).getValue());
	}
	
}
