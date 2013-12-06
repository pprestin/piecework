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
package piecework.ldap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapUserDetailsService;
import piecework.enumeration.CacheName;
import piecework.service.CacheService;

/**
 * @author James Renfro
 */
public class CustomLdapUserDetailsService extends LdapUserDetailsService {

    private static final Logger LOG = Logger.getLogger(CustomLdapUserDetailsService.class);

    private final CacheService cacheService;

    public CustomLdapUserDetailsService(CacheService cacheService, LdapUserSearch userSearch, LdapAuthoritiesPopulator authoritiesPopulator, CustomLdapUserDetailsMapper userDetailsMapper) {
        super(userSearch, authoritiesPopulator);
        this.cacheService = cacheService;
        setUserDetailsMapper(userDetailsMapper);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (LOG.isDebugEnabled())
            LOG.debug("Looking for user by uniqueId " + username);

        Cache.ValueWrapper wrapper = cacheService.get(CacheName.IDENTITY, username);

        UserDetails userDetails = null;

        if (wrapper != null) {
            userDetails = (UserDetails) wrapper.get();
            if (userDetails == null)
                throw new UsernameNotFoundException(username);

            return userDetails;
        }

        if (LOG.isDebugEnabled())
            LOG.debug("Retrieving user by uniqueId " + username);

        try {
            userDetails = super.loadUserByUsername(username);

        } finally {
            cacheService.put(CacheName.IDENTITY, username, userDetails);
        }
        return userDetails;
    }

    public CacheService getCacheService() {
        return cacheService;
    }
}
