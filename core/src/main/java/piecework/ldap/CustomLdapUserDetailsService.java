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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.cache.Cache;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.util.Assert;
import piecework.enumeration.CacheName;
import piecework.identity.AuthenticationPrincipalConverter;
import piecework.service.CacheService;

/**
 * @author James Renfro
 */
public class CustomLdapUserDetailsService implements UserDetailsService {

    private static final Logger LOG = Logger.getLogger(CustomLdapUserDetailsService.class);

    private final LdapUserSearch userSearch;
    private final LdapAuthoritiesPopulator authoritiesPopulator;
    private UserDetailsContextMapper userDetailsMapper;
    private final AuthenticationPrincipalConverter authenticationPrincipalConverter;
    private final CacheService cacheService;
    private final LdapSettings ldapSettings;

    public CustomLdapUserDetailsService(AuthenticationPrincipalConverter authenticationPrincipalConverter,
                                        CacheService cacheService, LdapUserSearch userSearch,
                                        LdapAuthoritiesPopulator authoritiesPopulator,
                                        CustomLdapUserDetailsMapper userDetailsMapper) {
        Assert.notNull(userSearch, "userSearch must not be null");
        Assert.notNull(authoritiesPopulator, "authoritiesPopulator must not be null");
        this.userSearch = userSearch;
        this.authoritiesPopulator = authoritiesPopulator;
        this.authenticationPrincipalConverter = authenticationPrincipalConverter;
        this.cacheService = cacheService;
        this.userDetailsMapper = userDetailsMapper;
        this.ldapSettings = userDetailsMapper.getLdapSettings();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (LOG.isDebugEnabled())
            LOG.debug("Looking for user by uniqueId " + username);

        if (StringUtils.isEmpty(username))
            return null;

        if (authenticationPrincipalConverter != null)
            username = authenticationPrincipalConverter.convert(username);

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
            DirContextOperations userData = userSearch.searchForUser(username);
            String actualUserName = userData.getStringAttribute(ldapSettings.getLdapGroupMemberUserName());

            if (StringUtils.isEmpty(actualUserName)) {
                LOG.info("No property ldap.group.member.username defined, assuming that person username attribute is the same as the group member attribute");
                actualUserName = username;
            }

            userDetails = userDetailsMapper.mapUserFromContext(userData, username,
                    authoritiesPopulator.getGrantedAuthorities(userData, actualUserName));

        } finally {
            cacheService.put(CacheName.IDENTITY, username, userDetails);
        }
        return userDetails;
    }

    public CacheService getCacheService() {
        return cacheService;
    }
}
