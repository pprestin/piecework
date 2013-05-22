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
import piecework.model.Process;
import piecework.model.ProcessInstance;
import piecework.util.ManyMap;

/**
 * @author James Renfro
 */
@RunWith(SpringJUnit4ClassRunner.class)
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
        Process process = new Process.Builder().processDefinitionKey(EXAMPLE_PROCESS_DEFINITION_KEY).build();
		String instanceId = engineProxy.start(process, null, null);
		Assert.assertNotNull(instanceId);

        ProcessExecutionCriteria criteria = new ProcessExecutionCriteria.Builder()
                .engine(process.getEngine())
                .engineProcessDefinitionKey(process.getEngineProcessDefinitionKey())
                .executionId(instanceId)
                .build();

        ProcessExecution execution = engineProxy.findExecution(criteria);

        Assert.assertNotNull(execution);
        Assert.assertEquals(instanceId, execution.getExecutionId());
    }
	
	@Test
	public void testStartWithAliasAndNoData() {
        Process process = new Process.Builder().processDefinitionKey(EXAMPLE_PROCESS_DEFINITION_KEY).build();
		String instanceId = engineProxy.start(process, "test1", null);
		Assert.assertNotNull(instanceId);

        ProcessExecutionCriteria criteria = new ProcessExecutionCriteria.Builder()
                .engine(process.getEngine())
                .engineProcessDefinitionKey(process.getEngineProcessDefinitionKey())
                .executionId(instanceId)
                .build();

        ProcessExecution execution = engineProxy.findExecution(criteria);

        Assert.assertNotNull(execution);
        Assert.assertEquals(instanceId, execution.getExecutionId());
        Assert.assertEquals("test1", execution.getBusinessKey());
	}
	
	@Test
	public void testStartWithAliasAndSomeData() {
        Process process = new Process.Builder().processDefinitionKey(EXAMPLE_PROCESS_DEFINITION_KEY).build();
		Map<String, ?> data = new ManyMap<String, String>();
		((ManyMap<String, String>)data).putOne("InitiatorId", "testuser");
        String instanceId = engineProxy.start(process, "test1", data);
        Assert.assertNotNull(instanceId);

        ProcessExecutionCriteria criteria = new ProcessExecutionCriteria.Builder()
                .engine(process.getEngine())
                .engineProcessDefinitionKey(process.getEngineProcessDefinitionKey())
                .executionId(instanceId)
                .build();

        ProcessExecution execution = engineProxy.findExecution(criteria);

        Assert.assertNotNull(execution);
        Assert.assertEquals(instanceId, execution.getExecutionId());
        Assert.assertEquals("testuser", execution.getData().get("InitiatorId"));
	}
	
}
