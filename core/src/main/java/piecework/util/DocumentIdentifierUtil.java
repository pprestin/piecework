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
package piecework.util;

/**
 * @author James Renfro
 */
public class DocumentIdentifierUtil {

	public static final String FORM_SUFFIX = "form";
	public static final String PROCESS_SUFFIX = "process";
	public static final String PROCESS_INSTANCE_SUFFIX = "instance";
	public static final String HISTORY_SUFFIX = "history";
	
	public static String getFormId(String processDefinitionKey) {
		return new StringBuilder().append(processDefinitionKey).append(".").append(FORM_SUFFIX).toString();
	}
	
//	public static String getProcessCollectionName(String namespace) {
//		return new StringBuilder(namespace).append(".").append(PROCESS_SUFFIX).toString();
//	}
//	
//	public static String getProcessInstanceCollectionName(String namespace, String processDefinitionKey) {
//		return new StringBuilder(namespace).append(".").append(processDefinitionKey).append(".").append(PROCESS_INSTANCE_SUFFIX).toString();
//	}
//	
//	public static String getHistoryCollectionName(String namespace, String processDefinitionKey) {
//		return new StringBuilder(namespace).append(".").append(processDefinitionKey).append(".").append(HISTORY_SUFFIX).toString();
//	}
	
}
