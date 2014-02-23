/*
 * Copyright 2013 University of Washington
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import piecework.model.Authorization;
import piecework.repository.AuthorizationRepository;

import java.util.*;

/**
 * @author James Renfro
 */
@Service
public class DefaultAuthorizationProvider implements AuthorizationProvider {

    @Autowired
    AuthorizationRepository repository;

    @Override
    public AccessAuthority authority(Collection<? extends GrantedAuthority> authorities) {
        AccessAuthority.Builder builder = new AccessAuthority.Builder();

        Set<String> authorizationIds = new HashSet<String>();
        for (GrantedAuthority authority : authorities) {
            if (authority instanceof SuperUserAccessAuthority) {
                return (SuperUserAccessAuthority) authority;
            } else {
                String authorizationId = authority.getAuthority();
                authorizationIds.add(authorizationId);
                builder.groupId(authorizationId);
            }
        }

        Iterable<Authorization> authorizations = repository.findAll(authorizationIds);
        if (authorizations != null) {
            for (Authorization authorization : authorizations) {
                if (authorization != null) {
                    List<ResourceAuthority> resourceAuthorities = authorization.getAuthorities();
                    if (resourceAuthorities != null && !resourceAuthorities.isEmpty()) {
                        for (ResourceAuthority resourceAuthority : resourceAuthorities) {
                            builder.resourceAuthority(resourceAuthority);
                        }
                    }
                }
            }
        }

        return builder.build();
    }

}
