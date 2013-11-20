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

import piecework.form.FieldAttributeDefinition;
import piecework.form.FieldTagDefinition;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This interface contains constant messages for resource parameters - path, query, etc.
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

    public static class ActionTypes {
        public static final String COMPLETE = "complete";
    }

    public static class ButtonTypes {
        public static final String BUTTON = "button";
        public static final String BUTTON_LINK = "button-link";
        public static final String SUBMIT = "submit";
    }

    public static class CandidateTypes {
        public static final String PERSON = "PERSON";
        public static final String GROUP = "GROUP";
    }

    public static class CandidateRoles {
        public static final String APPROVER = "APPROVER";
        public static final String WATCHER = "WATCHER";
    }

	public static class ConstraintTypes {
        public static final String AND = "AND";
        public static final String OR = "OR";
		public static final String IS_ALL_VALUES_MATCH = "IS_ALL_VALUES_MATCH";
        public static final String IS_CONFIRMATION_NUMBER = "IS_CONFIRMATION_NUMBER";
		public static final String IS_EMAIL_ADDRESS = "IS_EMAIL_ADDRESS";
		public static final String IS_NUMERIC = "IS_NUMERIC";
		public static final String IS_ONLY_REQUIRED_WHEN = "IS_ONLY_REQUIRED_WHEN";
        public static final String IS_ONLY_VISIBLE_WHEN = "IS_ONLY_VISIBLE_WHEN";
		//public static final String IS_VALID_USER = "IS_VALID_USER";
		public static final String IS_LIMITED_TO = "IS_LIMITED_TO";
        public static final String IS_STATE = "IS_STATE";
        public static final String SCREEN_IS_DISPLAYED_WHEN_ACTION_TYPE = "SCREEN_IS_DISPLAYED_WHEN_ACTION_TYPE";
	}

    public static class DeleteReasons {
        public static final String CANCELLED = "Cancelled";
    }

	public static class ExceptionCodes {
        public static final String attachment_could_not_be_saved = "attachment_could_not_be_saved";
        public static final String active_task_required = "active_task_required";
        public static final String attachment_does_not_exist = "attachment_does_not_exist";
        public static final String attachment_is_too_large = "attachment_is_too_large";
        public static final String certificate_does_not_match = "certificate_does_not_match";
        public static final String encryption_error = "encryption_error";
        public static final String form_access_forbidden = "form_access_forbidden";
        public static final String instance_cannot_be_modified = "instance_cannot_be_modified";
        public static final String instance_does_not_exist = "instance_does_not_exist";
        public static final String insufficient_permission = "insufficient_permission";
		public static final String interaction_id_invalid = "interaction_id_invalid";
		public static final String interaction_invalid = "interaction_invalid";
        public static final String invalid_assignment = "invalid_assignment";
        public static final String invalid_process_status = "invalid_process_status";
        public static final String not_editable = "not_editable";
        public static final String process_business_key_limit = "process_business_key_limit";
		public static final String process_change_key_duplicate = "process_change_key_duplicate";
		public static final String process_does_not_exist = "process_does_not_exist";
        public static final String process_is_misconfigured = "process_is_misconfigured";
        public static final String process_not_deployed = "process_not_deployed";
        public static final String request_does_not_match = "request_does_not_match";
        public static final String request_expired = "request_expired";
		public static final String request_id_required = "request_id_required";
        public static final String request_type_required = "request_type_required";
        public static final String screen_id_invalid = "screen_id_invalid";
        public static final String system_action_only = "system_action_only";
        public static final String task_action_invalid = "task_action_invalid";
        public static final String task_does_not_exist = "task_does_not_exist";
        public static final String task_id_required = "task_id_required";
        public static final String task_required = "task_required";
        public static final String user_does_not_match = "user_does_not_match";
	}

    public static class FieldTypes {
        public static final String CHECKBOX = "checkbox";
        public static final String DATE = "date";
        public static final String EMAIL = "email";
        public static final String FILE = "file";
        public static final String HTML = "html";
        public static final String NUMBER = "number";
        public static final String PERSON = "person";
        public static final String RADIO = "radio";
        public static final String SELECT_ONE = "select-one";
        public static final String SELECT_MULTIPLE = "select-multiple";
        public static final String TEXT = "text";
        public static final String TEXTAREA = "textarea";
        public static final String URL = "url";
    }

    public static class ProcessStatuses {
        public static final String OPEN = "open";
        public static final String COMPLETE = "complete";
        public static final String CANCELLED = "cancelled";
        public static final String SUSPENDED = "suspended";
        public static final String ALL = "all";
    }

    public static class RequestTypes {
        public static final String SUBMISSION = "submission";
        public static final String TASK = "task";
    }

    public static class ScreenTypes {
        public static final String STAGED = "staged";
        public static final String STANDARD = "standard";
        public static final String WIZARD = "wizard";
        public static final String WIZARD_TEMPLATE = "wizardTemplate";
    }

    public static class SectionTypes {
        public static final String STANDARD = "standard";
        public static final String REVIEW = "review";
    }

    public static class Settings {
        public static final String
            ACT_AS_USER_HEADER = "security.act.as.header",
            BASE_APPLICATION_URI = "base.application.uri",
            BASE_SERVICE_URI = "base.service.uri",
            KEYSTORE_FILE = "keystore.file",
            KEYSTORE_PASSWORD = "keystore.password",
            CERTIFICATE_ISSUER_HEADER = "certificate.issuer.header",
            CERTIFICATE_SUBJECT_HEADER = "certificate.subject.header",
            MAIL_SERVER_HOST = "mail.server.host",
            OPTIMIZATIONS_OFF = "optimizations.off";
    }

    public static class SubmissionTypes {
        public static final String FINAL = "final";
        public static final String INTERIM = "interim";
    }

    public static class SubmissionDirectives {
        public static final String SUBMISSION_DISPOSITION = "__PIECEWORK_SUBMISSION_DISPOSITION";
    }

    public static class SubmissionDirectiveDispositionValues {
        public static final String CANCEL = "CANCEL";
        public static final String COMPLETE = "FINAL";
        public static final String SAVE = "SAVE";
    }

    public static class TaskEventTypes {
        public static final String CREATE = "CREATE";
        public static final String COMPLETE = "COMPLETE";
    }

    public static class TaskStatuses {
        public static final String OPEN = "Open";
        public static final String COMPLETE = "Complete";
        public static final String CANCELLED = "Cancelled";
        public static final String REJECTED = "Rejected";
        public static final String SUSPENDED = "Suspended";
        public static final String ALL = "all";
    }

    public static class ValidationStatus {
    	public static final String ERROR = "error";
    	public static final String SUCCESS = "success";
    	public static final String WARNING = "warning";
    }
}
