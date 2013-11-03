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

import org.apache.log4j.Logger;
import piecework.model.Authorization;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import piecework.persistence.AuthorizationRepository;

/**
 * @author James Renfro
 */
public class AuthorizationRoleMapper implements GrantedAuthoritiesMapper {

    private static final Logger LOG = Logger.getLogger(AuthorizationRoleMapper.class);

    @Autowired
    AuthorizationRepository repository;

	@Override
	public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
		
		if (authorities != null) {
			long start = 0;
            if (LOG.isDebugEnabled())
                start = System.currentTimeMillis();

            Collection<ResourceAuthority> mapped = new ArrayList<ResourceAuthority>();
			Set<String> authorizationIds = new HashSet<String>();
            for (GrantedAuthority authority : authorities) {
                if (authority instanceof DebugResourceAuthority) {
                    mapped.add((DebugResourceAuthority)authority);
                } else {
				    String authorizationId = authority.getAuthority();
                    authorizationIds.add(authorizationId);
                }
			}

            Iterable<Authorization> authorizations = repository.findAll(authorizationIds);
            if (authorizations != null) {
                for (Authorization authorization : authorizations) {
                    if (authorization != null) {
                        List<ResourceAuthority> resourceAuthorities = authorization.getAuthorities();
                        if (resourceAuthorities != null && !resourceAuthorities.isEmpty()) {
                            for (ResourceAuthority resourceAuthority : resourceAuthorities) {
                                mapped.add(resourceAuthority);
                            }
                        }
                    }
                }
            }

            if (LOG.isDebugEnabled())
                LOG.debug("Mapped authorization roles in " + (System.currentTimeMillis() - start) + " ms");

			return mapped;
		}
		
		return null;
	}

}
