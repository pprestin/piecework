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

import java.util.List;
import java.util.Map;

import piecework.model.ProcessInstance;
import piecework.util.ManyMap;

/**
 * @author James Renfro
 */
public interface ProcessEngineProxy {

	String getEngineKey();
	
	ProcessInstance start(String engineProcessDefinitionKey, String processBusinessKey, Map<String, ?> data);
	
	ProcessInstance cancel(String engineProcessDefinitionKey, String processInstanceId, String processBusinessKey, String reason);

	ProcessInstance findInstance(String engineProcessDefinitionKey, String processInstanceId, String processBusinessKey, boolean includeVariables);
	
	List<ProcessInstance> findInstances(String engineProcessDefinitionKey, ManyMap<String, String> queryParameters);
	
}
