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
import piecework.engine.activiti.config.TestConfiguration;
import piecework.engine.exception.ProcessEngineException;
import piecework.engine.test.ExampleFactory;
import piecework.model.Process;
import piecework.model.ProcessExecution;
import piecework.process.ProcessInstanceSearchCriteria;
import piecework.util.ManyMap;

import java.io.IOException;
import java.util.List;

/**
 * @author James Renfro
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={TestConfiguration.class})
@ActiveProfiles("test")
public class ActivitiEngineProxyTest {

	
	@Autowired
	ProcessEngineProxy engineProxy;
	
	@Autowired
	RepositoryService repositoryService;

    private Process process;

	@Before
	public void setup() throws IOException {
        process = ExampleFactory.exampleProcess();

		ClassPathResource resource = new ClassPathResource("META-INF/example.bpmn20.xml");
		repositoryService.createDeployment().name(process.getEngineProcessDefinitionKey()).addInputStream("example.bpmn20.xml", resource.getInputStream()).deploy();
    }
	
	@Test
	public void testStartWithNoData() throws ProcessEngineException {
		String instanceId = engineProxy.start(process, null, null);
		Assert.assertNotNull(instanceId);

        ProcessInstanceSearchCriteria criteria = new ProcessInstanceSearchCriteria.Builder()
                .engine(process.getEngine())
                .engineProcessDefinitionKey(process.getEngineProcessDefinitionKey())
                .executionId(instanceId)
                .build();

        ProcessExecution execution = engineProxy.findExecution(criteria);

        Assert.assertNotNull(execution);
        Assert.assertEquals(instanceId, execution.getExecutionId());
    }
	
	@Test
	public void testStartWithAliasAndNoData() throws ProcessEngineException {
        String instanceId = engineProxy.start(process, "test1", null);
		Assert.assertNotNull(instanceId);

        ProcessInstanceSearchCriteria criteria = new ProcessInstanceSearchCriteria.Builder()
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
	public void testStartWithAliasAndSomeData() throws ProcessEngineException {
        ManyMap<String, String> data = new ManyMap<String, String>();
        data.putOne("EmployeeID", "testuser");
        String instanceId = engineProxy.start(process, "test1", data);
        Assert.assertNotNull(instanceId);

        // First, retrieve without including variables
        ProcessInstanceSearchCriteria criteria = new ProcessInstanceSearchCriteria.Builder()
                .engine(process.getEngine())
                .engineProcessDefinitionKey(process.getEngineProcessDefinitionKey())
                .executionId(instanceId)
                .build();

        ProcessExecution execution = engineProxy.findExecution(criteria);

        Assert.assertNotNull(execution);
        Assert.assertEquals(instanceId, execution.getExecutionId());
        Assert.assertNull(execution.getData());


        // Then include variables in criteria
        criteria = new ProcessInstanceSearchCriteria.Builder()
                .engine(process.getEngine())
                .engineProcessDefinitionKey(process.getEngineProcessDefinitionKey())
                .executionId(instanceId)
                .includeVariables()
                .build();

        execution = engineProxy.findExecution(criteria);

        Assert.assertNotNull(execution);
        Assert.assertEquals(instanceId, execution.getExecutionId());
        @SuppressWarnings("unchecked")
        List<String> employeeIDs = List.class.cast(execution.getData().get("EmployeeID"));
        Assert.assertEquals("testuser", employeeIDs.get(0));
	}
	
}
