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
package piecework.authorization;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import piecework.Constants;

/**
 * @author James Renfro
 * @date Apr 21, 2011
 */
public final class AuthorizationRole implements Comparable<AuthorizationRole> {

	public static final String ANONYMOUS = "anonymous";
	public static final String CREATOR = "creator";
	public static final String OWNER = "owner";
	public static final String USER = "user";
	public static final String INITIATOR = "initiator";
	public static final String APPROVER = "approver";
	public static final String WATCHER = "watcher";
	public static final String OVERSEER = "overseer";
	public static final String SUPERUSER = "superuser";
	public static final String SYSTEM = "system";
	
	private final String role;
	private final Map<String, String> limits;
	
	@SuppressWarnings("unchecked")
	private AuthorizationRole(Builder builder) {
		this.role = builder.role;
		this.limits = (Map<String, String>) (builder.limits != null ? Collections.unmodifiableMap(builder.limits) : Collections.emptyMap());
	}
	
	public boolean hasKey(String key) {
		return hasLimits() && limits.containsKey(key);
	}
	
	public boolean hasValue(String key, String value) {
		return hasLimits() && limits.get(key) != null;
	}
	
	public boolean hasLimits() {
		return limits != null && !limits.isEmpty();
	}
	
	public String getLimit(String key) {
		return hasLimits() ? limits.get(key) : null;
	}

	/**
	 * @return the role
	 */
	public String getRole() {
		return role;
	}

	
	@Override
	public boolean equals(Object o) {
		if (o instanceof AuthorizationRole) {
			AuthorizationRole other = (AuthorizationRole)o;
			return this.getRole().equals(other.getRole());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.getRole().hashCode();
	}

	@Override
	public int compareTo(AuthorizationRole o) {
		AuthorizationRole other = (AuthorizationRole)o;
		return this.getRole().compareTo(other.getRole());
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AuthorizationRole [role=").append(role).append("]");
		return builder.toString();
	}
	
	/*
	 * Fluent builder class, as per Joshua Bloch's Effective Java
	 */
	public final static class Builder {
		
		private String role;
		private Map<String, String> limits;
		
		public Builder(String role) {
			this.role = role;
		}
		
		public AuthorizationRole build() {
			return new AuthorizationRole(this);
		}
		
		public Builder limit(String key, String value) {
			if (limits == null)
				limits = new HashMap<String, String>();
			
			limits.put(key, value);
			return this;
		}
		
		public Builder processDefinitionKey(String processDefinitionKey) {
			return limit(Constants.LimitType.PROCESS_DEFINITION_KEY, processDefinitionKey);
		}
		
		public String getRole() {
			return role;
		}
		
	}

}
