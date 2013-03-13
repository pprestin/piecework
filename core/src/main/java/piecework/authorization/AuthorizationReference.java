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
import java.util.Set;

import piecework.util.SystemCredential;

/**
 * @author James Renfro
 */
public class AuthorizationReference {

	private final String namespace;
	private final String processDefinitionKey;
	private final Set<String> userIds;
	private final Set<String> groupIds;
	private final Set<SystemCredential> systemCredentials;
	
	private AuthorizationReference(Builder builder) {
		this.namespace = builder.namespace;
		this.processDefinitionKey = builder.processDefinitionKey;
		this.userIds = builder.userIds != null ? Collections.unmodifiableSet(builder.userIds) : null;
		this.groupIds = builder.groupIds != null ? Collections.unmodifiableSet(builder.groupIds) : null;
		this.systemCredentials = builder.systemCredentials != null ? Collections.unmodifiableSet(builder.systemCredentials) : null;
	}
	
	public String getNamespace() {
		return namespace;
	}

	public String getProcessDefinitionKey() {
		return processDefinitionKey;
	}

	public Set<String> getUserIds() {
		return userIds;
	}

	public Set<String> getGroupIds() {
		return groupIds;
	}

	public Set<SystemCredential> getSystemCredentials() {
		return systemCredentials;
	}
	
	public boolean isAnyValidUserAuthorized() {
		return userIds != null && userIds.contains("*");
	}

	public static class Builder {
		
		private final String namespace;
		private final String processDefinitionKey;
		private Set<String> userIds;
		private Set<String> groupIds;
		private Set<SystemCredential> systemCredentials; 
		
		public Builder(String namespace, String processDefinitionKey) {
			this.namespace = namespace;
			this.processDefinitionKey = processDefinitionKey;
		}
		
		public AuthorizationReference build() {
			return new AuthorizationReference(this);
		}
		
		public Builder userId(String userId) {
			if (this.userIds == null)
				this.userIds = new HashSet<String>();
			this.userIds.add(userId);
			return this;
		}
		
		public Builder userIds(Set<String> userIds) {
			if (userIds != null) {
				if (this.userIds == null)
					this.userIds = userIds;
				else
					this.userIds.addAll(userIds);
			}
			return this;
		}
		
		public Builder groupId(String groupId) {
			if (this.groupIds == null)
				this.groupIds = new HashSet<String>();
			this.groupIds.add(groupId);
			return this;
		}
		
		public Builder groupIds(Set<String> groupIds) {
			if (groupIds != null) {
				if (this.groupIds == null)
					this.groupIds = groupIds;
				else
					this.groupIds.addAll(groupIds);
			}
			return this;
		}
		
		public Builder systemCredential(SystemCredential systemCredential) {
			if (this.systemCredentials == null)
				this.systemCredentials = new HashSet<SystemCredential>();
			this.systemCredentials.add(systemCredential);
			return this;
		}
		
		public Builder systemCredentials(Set<SystemCredential> systemCredentials) {
			if (systemCredentials != null) {
				if (this.systemCredentials == null)
					this.systemCredentials = systemCredentials;
				else
					this.systemCredentials.addAll(systemCredentials);
			}
			return this;
		}
	}
	
	
}
