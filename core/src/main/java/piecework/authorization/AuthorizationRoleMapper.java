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

import piecework.model.Authorization;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;

/**
 * @author James Renfro
 */
public class AuthorizationRoleMapper implements GrantedAuthoritiesMapper {

    @Autowired
    AuthorizationRepository repository;

	@Override
	public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
		
		if (authorities != null) {
			Collection<ResourceAuthority> mapped = new ArrayList<ResourceAuthority>();
			for (GrantedAuthority authority : authorities) {
				String grantedAuthority = authority.getAuthority();
			
//				if (grantedAuthority.equals("ROLE_ADMIN")) {
//					mapped.add(new ResourceAuthority.Builder().role(AuthorizationRole.CREATOR).build());
//					mapped.add(new ResourceAuthority.Builder().role(AuthorizationRole.OWNER).processDefinitionKey("demo").build());
//					mapped.add(new ResourceAuthority.Builder().role(AuthorizationRole.INITIATOR).processDefinitionKey("demo").build());
//					mapped.add(new ResourceAuthority.Builder().role(AuthorizationRole.USER).processDefinitionKey("demo").build());
//					mapped.add(new ResourceAuthority.Builder().role(AuthorizationRole.OVERSEER).processDefinitionKey("Demonstration").build());
//
//					mapped.add(new ResourceAuthority.Builder().role(AuthorizationRole.OWNER).processDefinitionKey("Demo").build());
//					mapped.add(new ResourceAuthority.Builder().role(AuthorizationRole.OWNER).processDefinitionKey("Demonstration").build());
//				} else {
                    Authorization authorization = repository.findOne(grantedAuthority);
                    List<ResourceAuthority> resourceAuthorities = authorization.getAuthorities();
                    if (resourceAuthorities != null && !resourceAuthorities.isEmpty()) {
                        for (ResourceAuthority resourceAuthority : resourceAuthorities) {
                            mapped.add(resourceAuthority);
                        }
                    }
//                }
			}
			return mapped;
		}
		
		return null;
	}

}
