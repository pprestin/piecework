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
import java.util.Set;

import piecework.util.UserAgent;


/**
 * @author James Renfro
 * @date Apr 12, 2011
 */
public class AuthorizationContext {

	private enum Type { PUBLIC, FILTERED, AUTHORIZED_BY_ROLE };
	
//	private static final Logger LOG = Logger.getLogger(AuthorizationContext.class);

	private final Type type;
	private final UserAgent userAgent;
	private final String limitType;
	private final Set<String> authorizedRoles;
	private final AuthorizationConstraints constraints;

	public AuthorizationContext(UserAgent userAgent) {
		this.type = Type.PUBLIC;
		this.userAgent = userAgent;
		this.limitType = null;
		this.authorizedRoles = null;
		this.constraints = null;
	}
	
	public AuthorizationContext(UserAgent userAgent, Set<String> authorizedRoles) {
		this.type = Type.AUTHORIZED_BY_ROLE;
		this.userAgent = userAgent;
		this.limitType = null;
		this.authorizedRoles = authorizedRoles;
		this.constraints = null;
	}
	
	public AuthorizationContext(UserAgent userAgent, String limitType, AuthorizationConstraints constraints) {
		this.type = Type.FILTERED;
		this.userAgent = userAgent;
		this.limitType = limitType;
		this.authorizedRoles = null;
		this.constraints = constraints;
	}
	
	public Set<String> getAllowedProcessDefinitionKeys(String namespace) {
		return getAllowedProcessDefinitionKeys(namespace, null);
	}
	
	public Set<String> getAllowedProcessDefinitionKeys(String namespace, String roleId) {
		Set<String> emptySet = Collections.emptySet();
		return constraints != null ? constraints.getAllowedProcessDefinitionKeys(namespace, roleId) : emptySet;
	}
	
	public boolean isAllowedProcessDefinitionKey(String namespace, String processDefinitionKey) {
		Set<String> processDefinitionKeys = getAllowedProcessDefinitionKeys(namespace, null);
		return processDefinitionKeys != null ? processDefinitionKeys.contains(processDefinitionKey) : false;
	}
	
	public boolean isAllowedProcessDefinitionKeyForRole(String namespace, String processDefinitionKey, String roleId) {
		Set<String> processDefinitionKeys = getAllowedProcessDefinitionKeys(namespace, roleId);
		return processDefinitionKeys != null ? processDefinitionKeys.contains(processDefinitionKey) : false;
	}

	public UserAgent getUserAgent() {
		return userAgent;
	}

	public String getLimitType() {
		return limitType;
	}

	public boolean isAuthorized() {
		return type == Type.PUBLIC || constraints.isAuthorized();
	}
	
	public boolean isAuthorizedByRole(String namespace, String role) {
		return type == Type.AUTHORIZED_BY_ROLE && authorizedRoles != null && authorizedRoles.contains(role);
	}
	
	public boolean isSystemCall() {
		return userAgent != null && userAgent.getSystem() != null;
	}

	public AuthorizationConstraints getConstraints() {
		return constraints;
	}
	
	
}
