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

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;

/**
 * @author James Renfro
 */
public class AuthorizationRoleMapper implements GrantedAuthoritiesMapper {

	@Override
	public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
		
		if (authorities != null) {
			Collection<ResourceAuthority> mapped = new ArrayList<ResourceAuthority>();
			for (GrantedAuthority authority : authorities) {
				String grantedAuthority = authority.getAuthority();
			
				if (grantedAuthority.equals("ROLE_ADMIN")) {
					mapped.add(new ResourceAuthority(AuthorizationRole.CREATOR));
					mapped.add(new ResourceAuthority(AuthorizationRole.OWNER, "demo"));
					mapped.add(new ResourceAuthority(AuthorizationRole.INITIATOR, "demo"));
					mapped.add(new ResourceAuthority(AuthorizationRole.USER, "demo"));
					mapped.add(new ResourceAuthority(AuthorizationRole.OVERSEER, "Demonstration"));
					
					mapped.add(new ResourceAuthority(AuthorizationRole.OWNER, "Demo"));
					mapped.add(new ResourceAuthority(AuthorizationRole.OWNER, "Demonstration"));
				}
			}
			return mapped;
		}
		
		return null;
	}

}
