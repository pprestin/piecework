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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import piecework.util.ManyMapSet;


/**
 * @author James Renfro
 */
public class AuthorizationConstraints {

	private final Map<String, Set<ProcessRoleAuthorization>> processRoleAuthorizationMap;
	
	private AuthorizationConstraints(AuthorizationConstraints.Builder builder) {
		this.processRoleAuthorizationMap = Collections.unmodifiableMap(builder.processRoleAuthorizationMap);
	}
	
	public Set<String> getAllowedProcessDefinitionKeys(String namespace) {
		return getAllowedProcessDefinitionKeys(namespace, null);
	}
	
	public Set<String> getAllowedProcessDefinitionKeys(String namespace, String roleId) {
		Set<ProcessRoleAuthorization> authorizations = processRoleAuthorizationMap.get(namespace);
		if (authorizations == null)
			return Collections.emptySet();
		
		Set<String> processDefinitionKeys = new HashSet<String>();
		for (ProcessRoleAuthorization authorization : authorizations) {
			if (roleId == null || roleId.equals(authorization.getRoleId()))
				processDefinitionKeys.add(authorization.getProcessDefinitionKey());
		}
		return Collections.unmodifiableSet(processDefinitionKeys);
	}
	
	public boolean isAuthorized() {
		return processRoleAuthorizationMap != null && !processRoleAuthorizationMap.isEmpty();
	}
	
	public boolean isAuthorized(String namespace, String roleId, String processDefinitionKey) {
		Set<String> allowed = getAllowedProcessDefinitionKeys(namespace, roleId);
		return allowed != null && allowed.contains(processDefinitionKey);
	}
	
	public static final class Builder {
		
		private final ManyMapSet<String, ProcessRoleAuthorization> processRoleAuthorizationMap;
		
		public Builder() {
			this.processRoleAuthorizationMap = new ManyMapSet<String, ProcessRoleAuthorization>();
		}
		
		public AuthorizationConstraints build() {
			return new AuthorizationConstraints(this);
		}
		
		public Builder constraint(String namespace, String roleId, String processDefinitionKey) {
			this.processRoleAuthorizationMap.putOne(namespace, new ProcessRoleAuthorization(processDefinitionKey, roleId));
			return this;
		}
		
	}
	
}
