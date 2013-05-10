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
package piecework.process.concrete;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import piecework.Sanitizer;
import piecework.authorization.ResourceAuthority;
import piecework.form.FormPosition;
import piecework.form.model.Form;
import piecework.process.ProcessRepository;
import piecework.process.ProcessService;
import piecework.process.exception.ProcessDeletedException;
import piecework.process.exception.ProcessNotFoundException;
import piecework.process.model.record.ProcessRecord;
import piecework.security.PassthroughSanitizer;

import com.google.common.collect.Sets;

/**
 * @author James Renfro
 */
@Service
public class ProcessServiceImpl implements ProcessService {

	@Autowired
	ProcessRepository repository;
	
	@Autowired
	Sanitizer sanitizer;
	
	public void addForm(FormPosition position, Form form) throws ProcessNotFoundException {
		String processDefinitionKey = form.getProcessDefinitionKey();
		
		if (processDefinitionKey == null)
			throw new ProcessNotFoundException(null);
		
		String taskDefinitionKey = form.getTaskDefinitionKey();
		ProcessRecord record = repository.findOne(processDefinitionKey);
		
		if (record == null)
			throw new ProcessNotFoundException(processDefinitionKey);
		
		String formId = form.getId();
		
		// Since the data is coming from storage, use the PassthroughSanitizer
		ProcessRecord.Builder builder = new ProcessRecord.Builder(record, new PassthroughSanitizer());
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
	
	public piecework.process.model.Process deleteProcess(String processDefinitionKey) throws ProcessNotFoundException {
		ProcessRecord record = repository.findOne(processDefinitionKey);
		if (record == null)
			throw new ProcessNotFoundException(processDefinitionKey);
		
		record.setDeleted(true);
		return repository.save(record);
	}
	
	public List<piecework.process.model.Process> findProcesses(String ... allowedRoles) {
		SecurityContext context = SecurityContextHolder.getContext();
		Collection<? extends GrantedAuthority> authorities = context.getAuthentication().getAuthorities();
		
		Set<String> allowedRoleSet = allowedRoles != null && allowedRoles.length > 0 ? Sets.newHashSet(allowedRoles) : null;
		Set<String> allowedProcessDefinitionKeys = new HashSet<String>();
		if (authorities != null && !authorities.isEmpty()) {
			for (GrantedAuthority authority : authorities) {		
				if (authority instanceof ResourceAuthority) {
					ResourceAuthority resourceAuthority = ResourceAuthority.class.cast(authority);
					if (allowedRoleSet == null || allowedRoleSet.contains(resourceAuthority.getRole()))
						allowedProcessDefinitionKeys.addAll(resourceAuthority.getProcessDefinitionKeys());
				}
			}
		}
		
		List<piecework.process.model.Process> processes = new ArrayList<piecework.process.model.Process>();
		Iterator<ProcessRecord> iterator = repository.findAll(allowedProcessDefinitionKeys).iterator();
		while (iterator.hasNext()) {
			ProcessRecord record = iterator.next();
			if (!record.isDeleted())
				processes.add(record);
		}
		
		return processes;
	}
	
	public piecework.process.model.Process getProcess(String processDefinitionKey) throws ProcessNotFoundException, ProcessDeletedException {
		ProcessRecord record = repository.findOne(processDefinitionKey);
	
		if (record == null)
			throw new ProcessNotFoundException(processDefinitionKey);
		if (record.isDeleted())
			throw new ProcessDeletedException(processDefinitionKey);
		
		return record;
	}
	
	public piecework.process.model.Process storeProcess(piecework.process.model.Process process) {
		ProcessRecord.Builder builder = new ProcessRecord.Builder(process, sanitizer);
		ProcessRecord record = builder.build();
		return repository.save(record);
	}
	
	public piecework.process.model.Process undeleteProcess(String processDefinitionKey) {
		ProcessRecord record = repository.findOne(processDefinitionKey);
		record.setDeleted(false);
		return repository.save(record);
	}
	
}
