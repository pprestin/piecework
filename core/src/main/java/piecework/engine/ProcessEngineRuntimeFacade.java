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
package piecework.engine;

import java.util.List;
import java.util.Map;

import piecework.model.ProcessInstance;
import piecework.model.Task;
import piecework.util.ManyMap;

/**
 * @author James Renfro
 */
public interface ProcessEngineRuntimeFacade {

	String start(String engine, String engineProcessDefinitionKey, String alias, Map<String, ?> data);

	ProcessInstance cancel(String engine, String engineProcessDefinitionKey, String processInstanceId, String alias);
	
    ProcessInstance findInstance(String engine, String engineProcessDefinitionKey, String processInstanceId, String alias);

    List<ProcessInstance> findInstances(String engine, String engineProcessDefinitionKey, ManyMap<String, String> queryParameters);

    Task findTask(String processDefinitionKey, String taskId);

    List<Task> findTasks(String engine, String engineProcessDefinitionKey, ManyMap<String, String> queryParameters, String userId);

    void completeTask(String processDefinitionKey, String taskId);

}
