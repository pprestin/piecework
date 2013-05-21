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
import org.activiti.engine.TaskService;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import piecework.common.model.User;
import piecework.model.ProcessInstance;
import piecework.model.Task;
import piecework.util.ManyMap;

/**
 * @author James Renfro
 */
@Service
public class ActivitiEngineProxy implements ProcessEngineProxy {

	@Autowired
	RuntimeService runtimeService;

    @Autowired
    TaskService taskService;

	@Override
	public String start(String engineProcessDefinitionKey, String processBusinessKey, Map<String, ?> data) {
		
		Map<String, Object> engineData = data != null ? new HashMap<String, Object>(data) : null;
		org.activiti.engine.runtime.ProcessInstance activitiInstance = runtimeService.startProcessInstanceByKey(engineProcessDefinitionKey, processBusinessKey, engineData);
		
		return activitiInstance.getId();
	}

	@Override
	public ProcessInstance cancel(String engineProcessDefinitionKey, String engineProcessInstanceId, String processBusinessKey, String reason) {
		
		org.activiti.engine.runtime.ProcessInstance activitiInstance = findActivitiInstance(engineProcessDefinitionKey, engineProcessInstanceId, processBusinessKey);
		
		if (activitiInstance != null) {
			ProcessInstance deletedInstance = new ProcessInstance.Builder()
				.engineProcessInstanceId(activitiInstance.getId())
				.alias(activitiInstance.getBusinessKey())
				.build();
			
			runtimeService.deleteProcessInstance(engineProcessInstanceId, reason);
		
			return deletedInstance;
		}
		
		return null;
	}
	
	@Override
	public ProcessInstance findInstance(String engineProcessDefinitionKey,
			String engineProcessInstanceId, String processBusinessKey,
			boolean includeVariables) {
		
		org.activiti.engine.runtime.ProcessInstance activitiInstance = findActivitiInstance(engineProcessDefinitionKey, engineProcessInstanceId, processBusinessKey);
		
		ProcessInstance.Builder builder = new ProcessInstance.Builder()
				.engineProcessInstanceId(activitiInstance.getId())
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

    @Override
    public Task findTask(String engineProcessDefinitionKey, String taskId) {
        org.activiti.engine.task.Task activitiTask = taskService.createTaskQuery().processDefinitionKey(engineProcessDefinitionKey).taskId(taskId).singleResult();

        Task.Builder taskBuilder = new Task.Builder()
                .taskInstanceId(activitiTask.getId())
                .taskInstanceLabel(activitiTask.getDescription());

        List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);

        if (identityLinks != null && !identityLinks.isEmpty()) {
            for (IdentityLink identityLink : identityLinks) {
                String type = identityLink.getType();

                if (type == null)
                    continue;

                if (type.equals(IdentityLinkType.ASSIGNEE))
                    taskBuilder.assignee(new User.Builder().userId(identityLink.getUserId()).build());
                else if (type.equals(IdentityLinkType.CANDIDATE))
                    taskBuilder.candidateAssignee(new User.Builder().userId(identityLink.getUserId()).build());
            }
        }

        return taskBuilder.build();
    }

    private org.activiti.engine.runtime.ProcessInstance findActivitiInstance(String engineProcessDefinitionKey, String engineProcessInstanceId, String processBusinessKey) {
		org.activiti.engine.runtime.ProcessInstance activitiInstance = null;
		if (engineProcessInstanceId == null)
			activitiInstance = runtimeService
				.createProcessInstanceQuery()
				.processDefinitionKey(engineProcessDefinitionKey)
				.processInstanceBusinessKey(processBusinessKey)
				.singleResult();
		else
			activitiInstance = runtimeService
				.createProcessInstanceQuery()
				.processDefinitionKey(engineProcessDefinitionKey)
				.processInstanceId(engineProcessInstanceId)
				.singleResult();
		
		return activitiInstance;
	}

    @Override
    public Class<ProcessEngineProxy> getType() {
        return ProcessEngineProxy.class;
    }

    @Override
    public String getKey() {
        return "activiti";
    }
}
