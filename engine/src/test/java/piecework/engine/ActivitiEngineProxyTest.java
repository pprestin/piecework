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
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.repository.Deployment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import piecework.common.RequestDetails;
import piecework.engine.activiti.config.TestConfiguration;
import piecework.engine.exception.ProcessEngineException;
import piecework.engine.test.ExampleFactory;
import piecework.model.*;
import piecework.model.Process;
import piecework.process.ProcessInstanceSearchCriteria;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.util.ManyMap;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
    ProcessEngine processEngine;

    private Process process;
    private ProcessDeployment deployment;

	@Before
	public void setup() throws ProcessEngineException {
        process = ExampleFactory.exampleProcess();
        deployment = engineProxy.deploy(process, ExampleFactory.exampleProcessDeployment());

        process = new Process.Builder(process, new PassthroughSanitizer())
                .deploy(new ProcessDeploymentVersion(deployment), deployment)
                .build();
    }
	
	@Test
	public void testStartWithNoData() throws ProcessEngineException {
        ProcessInstance instance = Mockito.mock(ProcessInstance.class);
		String instanceId = engineProxy.start(process, instance);
		Assert.assertNotNull(instanceId);

        ProcessInstanceSearchCriteria criteria = new ProcessInstanceSearchCriteria.Builder()
                .engine(deployment.getEngine())
                .engineProcessDefinitionKey(deployment.getEngineProcessDefinitionKey())
                .executionId(instanceId)
                .build();

        ProcessExecution execution = engineProxy.findExecution(criteria);

        Assert.assertNotNull(execution);
        Assert.assertEquals(instanceId, execution.getExecutionId());
    }
	
	@Test
	public void testStartWithAliasAndNoData() throws ProcessEngineException {
        ProcessInstance instance = Mockito.mock(ProcessInstance.class);
        Mockito.when(instance.getProcessInstanceId()).thenReturn("test1");
        String instanceId = engineProxy.start(process, instance);
		Assert.assertNotNull(instanceId);

        ProcessInstanceSearchCriteria criteria = new ProcessInstanceSearchCriteria.Builder()
                .engine(deployment.getEngine())
                .engineProcessDefinitionKey(deployment.getEngineProcessDefinitionKey())
                .executionId(instanceId)
                .build();

        ProcessExecution execution = engineProxy.findExecution(criteria);

        Assert.assertNotNull(execution);
        Assert.assertEquals(instanceId, execution.getExecutionId());
        Assert.assertEquals("test1", execution.getBusinessKey());
	}
	
	@Test
	public void testStartWithAliasAndSomeData() throws ProcessEngineException {
        ProcessInstance instance = Mockito.mock(ProcessInstance.class);
        Map<String, List<Value>> data = new ManyMap<String, Value>();
        ((ManyMap<String, Value>)data).putOne("EmployeeID", new Value("testuser"));
        Mockito.when(instance.getData()).thenReturn(data);
        String instanceId = engineProxy.start(process, instance);
        Assert.assertNotNull(instanceId);

        // First, retrieve without including variables
        ProcessInstanceSearchCriteria criteria = new ProcessInstanceSearchCriteria.Builder()
                .engine(deployment.getEngine())
                .engineProcessDefinitionKey(deployment.getEngineProcessDefinitionKey())
                .executionId(instanceId)
                .build();

        ProcessExecution execution = engineProxy.findExecution(criteria);

        Assert.assertNotNull(execution);
        Assert.assertEquals(instanceId, execution.getExecutionId());
        Assert.assertNull(execution.getData());

        // Then include variables in criteria
        criteria = new ProcessInstanceSearchCriteria.Builder()
                .engine(deployment.getEngine())
                .engineProcessDefinitionKey(deployment.getEngineProcessDefinitionKey())
                .executionId(instanceId)
                .includeVariables()
                .build();

        execution = engineProxy.findExecution(criteria);

        Assert.assertNotNull(execution);
        Assert.assertEquals(instanceId, execution.getExecutionId());
        @SuppressWarnings("unchecked")
        String employeeID = (String)execution.getData().get("EmployeeID");
        Assert.assertEquals("testuser", employeeID);
	}
	
}
