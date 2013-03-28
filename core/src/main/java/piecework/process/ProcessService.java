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
package piecework.process;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import piecework.form.FormPosition;
import piecework.form.model.Form;
import piecework.process.exception.ProcessNotFoundException;
import piecework.process.model.record.ProcessRecord;

/**
 * @author James Renfro
 */
@Service
public class ProcessService {

	@Autowired
	private ProcessRepository repository;
	
	public piecework.process.model.Process getProcess(String processDefinitionKey) throws ProcessNotFoundException {
		ProcessRecord record = repository.findOne(processDefinitionKey);
	
		if (record == null)
			throw new ProcessNotFoundException(processDefinitionKey);
		return record;
	}
	
	public piecework.process.model.Process storeProcess(piecework.process.model.Process process) {
		ProcessRecord.Builder builder = new ProcessRecord.Builder(process);
		ProcessRecord record = builder.build();
		return repository.save(record);
	}
	
	public void addForm(FormPosition position, Form form) throws ProcessNotFoundException {
		String processDefinitionKey = form.getProcessDefinitionKey();
		
		if (processDefinitionKey == null)
			throw new ProcessNotFoundException(null);
		
		String taskDefinitionKey = form.getTaskDefinitionKey();
		ProcessRecord record = repository.findOne(processDefinitionKey);
		
		if (record == null)
			throw new ProcessNotFoundException(processDefinitionKey);
		
		String formId = form.getId();
		
		ProcessRecord.Builder builder = new ProcessRecord.Builder(record);
		switch (position) {
		case START_REQUEST:
			builder.startRequestFormIdentifier(formId);
			break;
		case START_RESPONSE:
			builder.startResponseFormIdentifier(formId);
			break;
		case TASK_REQUEST:
			builder.taskRequestFormIdentifier(taskDefinitionKey, formId);
			break;
		case TASK_RESPONSE:
			builder.taskResponseFormIdentifier(taskDefinitionKey, formId);
			break;
		}
		
		repository.save(builder.build());
	}
	
}
