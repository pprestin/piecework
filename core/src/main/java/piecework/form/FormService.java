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
package piecework.form;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import piecework.exception.StatusCodeError;
import piecework.form.model.Form;
import piecework.form.model.Message;
import piecework.form.model.record.FormRecord;
import piecework.process.ProcessService;
import piecework.process.exception.ProcessNotFoundException;
import piecework.util.ManyMap;
import piecework.util.PropertyValueReader;

/**
 * @author James Renfro
 */
@Service
public class FormService {

	private static final Logger LOG = Logger.getLogger(FormService.class);
	
	@Autowired
	private ProcessService processService;
	
	@Autowired 
	private FormRepository repository;
	
	public List<String> findProcessInstanceIds(String processDefinitionKey) throws StatusCodeError {
		return null;
	}
	
	public  Set<String> findProcessInstanceIdsByParameterMap(Collection<String> processDefinitionKeys, Map<String, List<String>> parameterMap) throws StatusCodeError {
		return null;
	}
	
	public Map<String, String> getColumnMap(String processDefinitionKey, String taskDefinitionKey) throws StatusCodeError {
		return null;
	}
	
//	public List<Form> getForms(String processDefinitionKey, String taskDefinitionKey) throws StatusCodeError {
//		return null;
//	}
	
	public Form getForm(String processDefinitionKey, FormPosition position, String taskDefinitionKey) throws ProcessNotFoundException {
		FormRecord form = getFormRecord(processDefinitionKey, position, taskDefinitionKey);
		
		return form;
	}
	
//	public Form getForm(String processDefinitionKey, String taskDefinitionKey, List<ValidationResult> results, Map<String, List<String>> formData, List<Attachment> attachments) throws StatusCodeError {
//		return null;
//	}
//	
//	public Form getForm(String processDefinitionKey, String taskDefinitionKey, String processInstanceId, boolean includeRestricted, boolean readOnly) throws StatusCodeError {
//		return null;
//	}
	
	public Form storeForm(String processDefinitionKey, FormPosition position, String taskDefinitionKey, Form form) throws ProcessNotFoundException {
		
		FormRecord.Builder builder = new FormRecord.Builder(form);
		if (processDefinitionKey != null)
			builder.processDefinitionKey(processDefinitionKey);
		if (taskDefinitionKey != null)
			builder.taskDefinitionKey(taskDefinitionKey);
		
		FormRecord record = builder.build();
		record = repository.save(record);
		
		processService.addForm(position, record);
		
		return record;
	}
	
	public ManyMap<String, String> getValues(String processDefinitionKey, String processInstanceId) throws StatusCodeError {
		return null;
	}
	
	public void validate(String processDefinitionKey, String taskDefinitionKey, List<String> sectionNames, PropertyValueReader reader) throws StatusCodeError {

	}
	
	public void storeValues(String processDefinitionKey, String taskDefinitionKey, String processInstanceId, List<String> sectionNames, PropertyValueReader reader, boolean isRequiredNecessary) throws StatusCodeError {

	}

	public void storeMessages(String processDefinitionKey, String taskDefinitionKey, String processInstanceId, List<Message> messages) throws StatusCodeError {

	}

	private FormRecord getFormRecord(String processDefinitionKey, FormPosition position, String taskDefinitionKey) throws ProcessNotFoundException {
		
		piecework.process.model.Process process = processService.getProcess(processDefinitionKey);
		
		String formId = getFormId(process, position, taskDefinitionKey);
		
		FormRecord form = null;
		
		if (formId != null) {
			form = repository.findOne(formId);
	
			if (form != null) {
				String layout = form.getLayout();
				// If the layout is wizard then only the start task form is going to
				// be returned, and the only section that will be editable is the
				// one that
				if (layout != null && layout.equalsIgnoreCase("wizard")) {
					form = repository.findOne(process.getStartRequestFormIdentifier());
				}
			}
		}
		
		return form;
	}

	private String getFormId(piecework.process.model.Process process, FormPosition position, String taskDefinitionKey) {
		String formId = null;
		Map<String, String> map = null;
		switch (position) {
		case START_REQUEST:
			formId = process.getStartRequestFormIdentifier();
			break;
		case START_RESPONSE:
			formId = process.getStartResponseFormIdentifier();
			break;
		case TASK_REQUEST:
			map = process.getTaskRequestFormIdentifiers();
			break;
		case TASK_RESPONSE:
			map = process.getTaskRequestFormIdentifiers();
			break;
		}
		
		if (map != null && !map.isEmpty()) {
			String potentialFormId = map.get(taskDefinitionKey);
			if (potentialFormId != null)
				formId = potentialFormId;
		}
		return formId;
	}
	
}
