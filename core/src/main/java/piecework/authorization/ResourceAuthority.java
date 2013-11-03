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

import java.util.*;

import org.springframework.security.core.GrantedAuthority;
import piecework.model.Process;
import piecework.security.Sanitizer;

/**
 * @author James Renfro
 */
public class ResourceAuthority implements GrantedAuthority {

	private static final long serialVersionUID = 1L;
	
	private final String role;
	private final Set<String> processDefinitionKeys;

    protected ResourceAuthority() {
        this(new Builder());
    }
	
	private ResourceAuthority(Builder builder) {
		this.role = builder.role;
		this.processDefinitionKeys = builder.processDefinitionKeys != null ? Collections.unmodifiableSet(builder.processDefinitionKeys) : null;
	}

    public boolean hasRole(Process process, Set<String> allowedRoleSet) {
        if (allowedRoleSet == null || allowedRoleSet.contains(getRole())) {
            Set<String> processDefinitionKeys = getProcessDefinitionKeys();
            if (processDefinitionKeys == null || processDefinitionKeys.contains(process.getProcessDefinitionKey()))
                return true;
        }
        return false;
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

    public Set<String> getProcessDefinitionKeys(Set<String> allowedRoleSet) {
        if (allowedRoleSet == null || allowedRoleSet.contains(getRole())) {
            Set<String> processDefinitionKeys = getProcessDefinitionKeys();
            if (processDefinitionKeys != null && !processDefinitionKeys.isEmpty())
                return processDefinitionKeys;
        }
        return Collections.emptySet();
    }

	public Set<String> getProcessDefinitionKeys() {
		return processDefinitionKeys;
	}

	public String getRole() {
		return role;
	}


    public final static class Builder {

        private String role;
        private Set<String> processDefinitionKeys;

        public Builder() {
            super();
        }

        public Builder(ResourceAuthority authority, Sanitizer sanitizer) {
            this.role = sanitizer.sanitize(authority.role);
            if (authority.processDefinitionKeys != null && !authority.processDefinitionKeys.isEmpty()) {
                this.processDefinitionKeys = new HashSet<String>(authority.processDefinitionKeys.size());
                for (String processDefinitionKey : authority.processDefinitionKeys) {
                    this.processDefinitionKeys.add(sanitizer.sanitize(processDefinitionKey));
                }
            }
        }

        public ResourceAuthority build() {
            return new ResourceAuthority(this);
        }

        public Builder role(String role) {
            this.role = role;
            return this;
        }

        public Builder processDefinitionKey(String processDefinitionKey) {
            if (this.processDefinitionKeys == null)
                this.processDefinitionKeys = new HashSet<String>();
            this.processDefinitionKeys.add(processDefinitionKey);
            return this;
        }

    }

}
