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
package piecework.engine.concrete;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import piecework.Registry;
import piecework.engine.ProcessEngineProxy;
import piecework.engine.ProcessEngineRuntimeFacade;
import piecework.model.ProcessInstance;
import piecework.model.Task;
import piecework.util.ManyMap;

/**
 * @author James Renfro
 */
@Service
public class ProcessEngineRuntimeConcreteFacade implements ProcessEngineRuntimeFacade {

	@Autowired
    Registry registry;
	
	@Override
	public ProcessInstance cancel(String engine, String engineProcessDefinitionKey, String processInstanceId, String processInstanceAlias) {
		return null;
	}
	
    @Override
    public ProcessInstance findInstance(String engine, String engineProcessDefinitionKey, String processInstanceId, String processInstanceAlias) {
        return null;
    }

    @Override
    public List<ProcessInstance> findInstances(String engine, String engineProcessDefinitionKey, ManyMap<String, String> queryParameters) {
//        List<ProcessInstance> instances = new ArrayList<ProcessInstance>();
//
//        List<ProcessEngineProxy> proxies = registry.retrieve(ProcessEngineProxy.class);
//
//        if (proxies != null) {
//	        for (ProcessEngineProxy proxy : proxies) {
//	        	if (proxy.getKey().equals(engine))
//	        		instances.addAll(proxy.findInstances(engineProcessDefinitionKey, queryParameters));
//	        }
//        }

        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, engine);
    	return proxy.findInstances(engineProcessDefinitionKey, queryParameters);
    }

    @Override
    public Task findTask(String engine, String engineProcessDefinitionKey, String taskId) {
        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, engine);

        return proxy.findTask(engineProcessDefinitionKey, taskId);
    }

    @Override
    public List<Task> findTasks(String engine, String engineProcessDefinitionKey, ManyMap<String, String> queryParameters, String userId) {
        return null;
    }

    @Override
    public void completeTask(String processDefinitionKey, String taskId) {

    }

    @Override
	public String start(String engine, final String engineProcessDefinitionKey, final String processBusinessKey, final Map<String, ?> data) {
        ProcessEngineProxy proxy = registry.retrieve(ProcessEngineProxy.class, engine);

        return proxy.start(engineProcessDefinitionKey, processBusinessKey, data);
	}



}
