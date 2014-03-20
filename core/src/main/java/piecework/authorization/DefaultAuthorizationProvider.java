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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import piecework.enumeration.CacheName;
import piecework.model.Authorization;
import piecework.repository.AuthorizationRepository;
import piecework.service.CacheService;

import java.util.*;

/**
 * @author James Renfro
 */
@Service
public class DefaultAuthorizationProvider implements AuthorizationProvider {
    private static final Logger LOG = Logger.getLogger(DefaultAuthorizationProvider.class);

    @Autowired
    AuthorizationRepository repository;

    @Autowired
    CacheService cacheService;

    @Override
    public AccessAuthority authority(Collection<? extends GrantedAuthority> authorities) {
        AccessAuthority.Builder builder = new AccessAuthority.Builder();

        Set<Authorization> authorizations = new HashSet<Authorization>();
        Set<String> authorizationIds = new HashSet<String>();
        for (GrantedAuthority authority : authorities) {
            if (authority instanceof SuperUserAccessAuthority) {
                return (SuperUserAccessAuthority) authority;
            } else {
                String authorizationId = authority.getAuthority();
                builder.groupId(authorizationId);

                Cache.ValueWrapper valueWrapper = cacheService.get(CacheName.AUTHORIZATIONS, authorizationId);
                if (valueWrapper != null) {
                    Authorization authorization = Authorization.class.cast(valueWrapper.get());
                    if (authorization != null)
                        authorizations.add(authorization);
                } else {
                    authorizationIds.add(authorizationId);
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            int numberOfCachedAuthorizations = authorizations.size();
            int numberOfUncachedAuthorizations = authorizationIds.size();
            LOG.debug("Retrieved " + numberOfCachedAuthorizations + " cached authorizations, looking up " + numberOfUncachedAuthorizations + " potential authorizations");
        }

        if (!authorizationIds.isEmpty()) {
            Iterable<Authorization> uncached = repository.findAll(authorizationIds);
            if (uncached != null) {
                for (Authorization authorization : uncached) {
                    authorizations.add(authorization);
                    authorizationIds.remove(authorization.getAuthorizationId());
                    cacheService.put(CacheName.AUTHORIZATIONS, authorization.getAuthorizationId(), authorization);
                }
                // Ensure that we cache the fact that nothing came back so we can stop asking for this authorization
                for (String authorizationId : authorizationIds) {
                    cacheService.put(CacheName.AUTHORIZATIONS, authorizationId, null);
                }
            }
        }

        if (authorizations != null && !authorizations.isEmpty()) {
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
