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

import piecework.authorization.ResourceAuthority;
import piecework.model.Process;
import piecework.process.ProcessRepository;

import com.google.common.collect.Sets;

/**
 * @author James Renfro
 */
@Service
public class ResourceHelper {

	@Autowired
	ProcessRepository processRepository;
	
	public List<piecework.model.Process> findProcesses(String ... allowedRoles) {
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
		
		List<piecework.model.Process> processes = new ArrayList<piecework.model.Process>();
		Iterator<Process> iterator = processRepository.findAll(allowedProcessDefinitionKeys).iterator();
		while (iterator.hasNext()) {
			Process record = iterator.next();
			if (!record.isDeleted())
				processes.add(record);
		}
		
		return processes;
	}
	
}
