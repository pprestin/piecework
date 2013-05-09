/*
 * Copyright 2010 University of Washington
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
package piecework;

/**
 * This interface contains constant values for resource parameters - path, query, etc.
 * 
 * @author James Renfro
 */
public interface Constants {
	
	public static class LimitType {
		public static final String PROCESS_DEFINITION_KEY = "processDefinitionKey";
	}
	
	public static final String ACTIVITI_NS_URI = "http://activiti.org/bpmn";
	
	public static final String XML_NAMESPACE = "piecework";
	
	public static final String APPROVER_ROLE_PERMISSION = "Approver";
	public static final String WATCHER_ROLE_PERMISSION = "Watcher";
	public static final String OVERSEER_ROLE_PERMISSION = "Overseer";
	public static final String OWNER_ROLE_PERMISSION = "Owner";

	public static final String START_TASK_DEFINITION_KEY = "__startTask";
	public static final String OVERSIGHT_DEFINITION_KEY = "__general";
	
	public static class ExceptionCodes {
		public static final String process_change_key_duplicate = "process_change_key_duplicate";
		public static final String process_does_not_exist = "process_does_not_exist";
	}
	
}
