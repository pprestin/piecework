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
	
	public static final String AUTHENTICATION_STRATEGY_SINGLE_SIGN_ON = "SSO";
	
	public static final String AUTHORIZATION_CONTEXT_KEY = "restflow.AuthorizationContext";
	
	public static final String RESTFLOW_NMSPC_CD = "RESTFLOW";
	public static final String WEBSERVICE_DTL_CD = "WorkflowWS1";
	
	public static final String XML_NAMESPACE = "piecework";
	
	public static final String ANON_REQUESTER_PRNCP_ID = "requester";
	public static final String SESSION_VARIABLE = "usersessionid";
	
	public static final String ID = "id";
	public static final String ACTION_CODE = "actionCode";
	public static final String TASK_ID = "taskId";
	public static final String USER_ID = "userId";
	public static final String USER_ID_TYPE = "userIdType";
	public static final String WORKFLOW_NAMESPACE = "workflowNamespace";
	public static final String WORKFLOW_TYPE = "workflowType";
	public static final String WORKFLOW_TYPE_CODE = "WORKFLOWTYPE";
	
	public static final String USER_KEY = "currentWorkflowUser";
	public static final String SEARCH_DOCUMENT = "Search Document";
	
	public static final String APPROVER_ROLE_PERMISSION = "Approver";
	public static final String WATCHER_ROLE_PERMISSION = "Watcher";
	public static final String OVERSEER_ROLE_PERMISSION = "Overseer";
	public static final String OWNER_ROLE_PERMISSION = "Owner";
	
	public static final String PROPERTY_SET_WORKFLOW = "workflowType";
	public static final String PROPERTY_SET_STEP = "step";
	public static final String PROPERTY_SET_SCREEN = "screen";
	public static final String PROPERTY_SET_DIALOG = "dialog";
	public static final String PROPERTY_TYPE_PROPERTY = "property";
	public static final String PROPERTY_TYPE_ACTION = "action";
	
	public static final String WEBFORM_APPLICATION_NAME = "webform";
	
	
	public static final String PROPERTY_RESOURCE_NAME = "property";
	public static final String PROPERTY_RESOURCE_LABEL = "Property";
	
}
