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

	public static class ConstraintTypes {
		public static final String IS_ALL_VALUES_MATCH = "IS_ALL_VALUES_MATCH";
		public static final String IS_EMAIL_ADDRESS = "IS_EMAIL_ADDRESS";
		public static final String IS_NUMERIC = "IS_NUMERIC";
		public static final String IS_ONLY_REQUIRED_WHEN = "IS_ONLY_REQUIRED_WHEN";
		public static final String IS_VALID_USER = "IS_VALID_USER";
		public static final String IS_LIMITED_TO = "IS_LIMITED_TO";
	}
	
	public static class ExceptionCodes {
		public static final String interaction_id_invalid = "interaction_id_invalid";
		public static final String interaction_invalid = "interaction_invalid";
        public static final String process_business_key_limit = "process_business_key_limit";
		public static final String process_change_key_duplicate = "process_change_key_duplicate";
		public static final String process_does_not_exist = "process_does_not_exist";
        public static final String request_does_not_match = "request_does_not_match";
		public static final String screen_id_invalid = "screen_id_invalid";
        public static final String user_does_not_match = "user_does_not_match";
	}

    public static class FieldTypes {
        public static final String CHECKBOX = "checkbox";
        public static final String FILE = "file";
        public static final String RADIO = "radio";
        public static final String SELECT_ONE = "select-one";
        public static final String SELECT_MULTIPLE = "select-multiple";
        public static final String TEXT = "text";
        public static final String TEXTAREA = "textarea";
    }

    public static class SubmissionTypes {
        public static final String START = "start";
        public static final String COMPLETE = "complete";
    }

    public static class SubmissionDirectives {
        public static final String SUBMISSION_DISPOSITION = "__PIECEWORK_SUBMISSION_DISPOSITION";
    }

    public static class SubmissionDirectiveDispositionValues {
        public static final String CANCEL = "CANCEL";
        public static final String COMPLETE = "COMPLETE";
        public static final String SAVE = "SAVE";
    }

    public static class ValidationStatus {
    	public static final String ERROR = "error";
    	public static final String SUCCESS = "success";
    	public static final String WARNING = "warning";
    }
}
