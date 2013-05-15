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
package piecework.authorization;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;

/**
 * @author James Renfro
 */
public class ResourceAuthority implements GrantedAuthority {

	private static final long serialVersionUID = 1L;
	
	private final String role;
	private final Set<String> processDefinitionKeys;
	
	public ResourceAuthority(String role, String... processDefinitionKeys) {
		this.role = role;
		this.processDefinitionKeys = new HashSet<String>(Arrays.asList(processDefinitionKeys));
	}
	
	public boolean isAuthorized(String roleAllowed, String processDefinitionKeyAllowed) {
		if (roleAllowed == null)
			return false;
		return role.equals(roleAllowed) && (processDefinitionKeyAllowed == null || processDefinitionKeys.isEmpty() || processDefinitionKeys.contains(processDefinitionKeyAllowed));
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(role);
		builder.append("[");
		if (processDefinitionKeys != null && !processDefinitionKeys.isEmpty()) {
			int count = 1;
			int size = processDefinitionKeys.size();
			for (String processDefinitionKey : processDefinitionKeys) {
				builder.append(processDefinitionKey);
				
				if (count < size)
					builder.append(", ");
				count++;
			}
		}
		builder.append("]");
		return builder.toString();
	}

	@Override
	public String getAuthority() {
		return toString();
	}

	public Set<String> getProcessDefinitionKeys() {
		return processDefinitionKeys;
	}

	public String getRole() {
		return role;
	}
	
}
