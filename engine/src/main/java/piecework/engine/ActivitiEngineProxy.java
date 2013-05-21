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
import java.util.HashMap;
import java.util.Iterator;
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
	public ProcessInstance start(String engineProcessDefinitionKey, String processBusinessKey, Map<String, ?> data) {
		
		Map<String, Object> engineData = data != null ? new HashMap<String, Object>(data) : null;
		org.activiti.engine.runtime.ProcessInstance activitiInstance = runtimeService.startProcessInstanceByKey(engineProcessDefinitionKey, processBusinessKey, engineData);
		
		ProcessInstance.Builder builder = new ProcessInstance.Builder()
			.processInstanceId(activitiInstance.getId())
			.alias(processBusinessKey);
		
		return builder.build();
	}

	@Override
	public ProcessInstance cancel(String engineProcessDefinitionKey, String processInstanceId, String processBusinessKey, String reason) {
		
		org.activiti.engine.runtime.ProcessInstance activitiInstance = findActivitiInstance(engineProcessDefinitionKey, processInstanceId, processBusinessKey);
		
		if (activitiInstance != null) {
			ProcessInstance deletedInstance = new ProcessInstance.Builder()
				.processInstanceId(activitiInstance.getId())
				.alias(activitiInstance.getBusinessKey())
				.build();
			
			runtimeService.deleteProcessInstance(processInstanceId, reason);
		
			return deletedInstance;
		}
		
		return null;
	}
	
	@Override
	public ProcessInstance findInstance(String engineProcessDefinitionKey,
			String processInstanceId, String processBusinessKey,
			boolean includeVariables) {
		
		org.activiti.engine.runtime.ProcessInstance activitiInstance = findActivitiInstance(engineProcessDefinitionKey, processInstanceId, processBusinessKey);
		
		ProcessInstance.Builder builder = new ProcessInstance.Builder()
				.processInstanceId(activitiInstance.getId())
				.alias(processBusinessKey);

		if (includeVariables) {
			Map<String, Object> variables = runtimeService
					.getVariables(activitiInstance.getId());
			if (variables != null) {
				for (Map.Entry<String, Object> entry : variables.entrySet()) {
					Object value = entry.getValue();
				
					if (value instanceof Iterable) {
						Iterable<?> iterable = Iterable.class.cast(value);
						Iterator<?> iterator = iterable.iterator();
						List<String> values = new ArrayList<String>();
						while (iterator.hasNext()) {
							Object item = iterator.next();
							values.add(String.valueOf(item));
						}
						if (!values.isEmpty())
							builder.formValue(entry.getKey(), values.toArray(new String[values.size()]));
					} else {
						builder.formValue(entry.getKey(), String.valueOf(value));
					}
				}
			}
		}
		return builder.build();
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
	
	private org.activiti.engine.runtime.ProcessInstance findActivitiInstance(String engineProcessDefinitionKey, String processInstanceId, String processBusinessKey) {
		org.activiti.engine.runtime.ProcessInstance activitiInstance = null;
		if (processInstanceId == null)
			activitiInstance = runtimeService
				.createProcessInstanceQuery()
				.processDefinitionKey(engineProcessDefinitionKey)
				.processInstanceBusinessKey(processBusinessKey)
				.singleResult();
		else
			activitiInstance = runtimeService
				.createProcessInstanceQuery()
				.processDefinitionKey(engineProcessDefinitionKey)
				.processInstanceId(processInstanceId)
				.singleResult();
		
		return activitiInstance;
	}

}
