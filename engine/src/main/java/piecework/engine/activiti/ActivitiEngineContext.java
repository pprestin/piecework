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
package piecework.engine.activiti;

import org.activiti.engine.EngineServices;
import org.activiti.engine.FormService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.form.TaskFormData;
import piecework.engine.EngineContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
public class ActivitiEngineContext implements EngineContext {

    private final EngineServices services;
    private final String processDefinitionId;
    private final String processInstanceId;

    public ActivitiEngineContext(EngineServices services, String processDefinitionId, String processInstanceId) {
        this.services = services;
        this.processDefinitionId = processDefinitionId;
        this.processInstanceId = processInstanceId;
    }

    @Override
    public Map<String, String> getStartFormProperties() {
        StartFormData startFormData = services.getFormService().getStartFormData(processDefinitionId);
        return convert(startFormData != null ? startFormData.getFormProperties() : null);
    }

    @Override
    public Map<String, String> getTaskFormProperties(String taskId) {
        TaskFormData taskFormData = services.getFormService().getTaskFormData(taskId);
        return convert(taskFormData != null ? taskFormData.getFormProperties() : null);
    }

    @Override
    public <T> T getInstanceVariable(String name) {
        return (T) services.getRuntimeService().getVariable(processInstanceId, name);
    }

    @Override
    public <T> T getTaskVariable(String taskId, String name) {
        return (T) services.getTaskService().getVariable(taskId, name);
    }

    private Map<String, String> convert(List<FormProperty> formProperties) {
        Map<String, String> map = new HashMap<String, String>();
        if (formProperties != null) {
            for (FormProperty formProperty : formProperties) {
                map.put(formProperty.getName(), formProperty.getValue());
            }
        }
        return map;
    }
}
