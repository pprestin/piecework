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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import piecework.model.ProcessInstance;
import piecework.util.ManyMap;

/**
 * @author James Renfro
 */
@Service
public class ActivitiEngineProxy implements ProcessEngineProxy {

	@Autowired
	RuntimeService runtimeService;
	
	@Override
	public String getEngineKey() {
		return "activiti";
	}

	@Override
	public ProcessInstance start(String engineProcessDefinitionKey,
			String processBusinessKey, Map<String, ?> data) {
		return null;
	}

	@Override
	public ProcessInstance cancel(String engineProcessDefinitionKey,
			String processInstanceId, String processBusinessKey) {
		return null;
	}

	@Override
	public List<ProcessInstance> findInstances(String engineProcessDefinitionKey, ManyMap<String, String> queryParameters) {
		List<ProcessInstance> instances = new ArrayList<ProcessInstance>();
		List<org.activiti.engine.runtime.ProcessInstance> sources = runtimeService.createProcessInstanceQuery().list();
		if (sources != null) {
			for (org.activiti.engine.runtime.ProcessInstance source : sources) {
				ProcessInstance instance = new ProcessInstance.Builder()
					.processInstanceId(source.getProcessInstanceId())
					.alias(source.getBusinessKey())
					.build();
				instances.add(instance);
			}
		}
		return instances;
	}

}
