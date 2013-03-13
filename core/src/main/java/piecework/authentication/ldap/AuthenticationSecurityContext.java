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
package piecework.authentication.ldap;

import java.security.Principal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.cxf.security.SecurityContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author James Renfro
 */
public class AuthenticationSecurityContext implements SecurityContext {

	private final Authentication authentication;
	private final Set<String> rolesAllowed;
	
	public AuthenticationSecurityContext(Authentication authentication) {
		this.authentication = authentication;
		this.rolesAllowed = new HashSet<String>();
		Collection<? extends GrantedAuthority> grantedAuthorities = authentication.getAuthorities();
		
		if (grantedAuthorities != null && !grantedAuthorities.isEmpty()) {
			for (GrantedAuthority grantedAuthority : grantedAuthorities) {
				String authority = grantedAuthority.getAuthority();
				rolesAllowed.add(authority);
			}
		}
	}
	
	@Override
	public Principal getUserPrincipal() {
		return (Principal) authentication.getPrincipal();
	}

	@Override
	public boolean isUserInRole(String role) {
		
		if (role != null) 	
			return rolesAllowed.contains(role);
		
		return false;
	}

}
