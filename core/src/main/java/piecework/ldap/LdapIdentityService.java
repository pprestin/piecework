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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.ldap.SizeLimitExceededException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapUserDetailsService;
import piecework.identity.IdentityDetails;
import piecework.model.User;
import piecework.service.IdentityService;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author James Renfro
 */
public class LdapIdentityService implements IdentityService {

    private static final Logger LOG = Logger.getLogger(LdapIdentityService.class);

    private final CacheManager cacheManager;
    private final LdapContextSource personLdapContextSource;
    private final LdapSettings ldapSettings;
    private final LdapUserDetailsService delegate;
    private final LdapUserSearch userSearch;
    private final LdapUserSearch internalUserSearch;
    private final LdapAuthoritiesPopulator authoritiesPopulator;
    private final CustomLdapUserDetailsMapper userDetailsMapper;

    public LdapIdentityService() {
        this.cacheManager = null;
        this.personLdapContextSource = null;
        this.delegate = null;
        this.userDetailsMapper = null;
        this.userSearch = null;
        this.internalUserSearch = null;
        this.authoritiesPopulator = null;
        this.ldapSettings = null;
    }

    public LdapIdentityService(LdapContextSource personLdapContextSource, LdapUserSearch userSearch, LdapUserSearch internalUserSearch, LdapAuthoritiesPopulator authoritiesPopulator, CustomLdapUserDetailsMapper userDetailsMapper, LdapSettings ldapSettings, CacheManager cacheManager) {
        this.personLdapContextSource = personLdapContextSource;
        this.delegate = new LdapUserDetailsService(userSearch, authoritiesPopulator);
        this.delegate.setUserDetailsMapper(userDetailsMapper);
        this.userDetailsMapper = userDetailsMapper;
        this.userSearch = userSearch;
        this.internalUserSearch = internalUserSearch;
        this.authoritiesPopulator = authoritiesPopulator;
        this.ldapSettings = ldapSettings;
        this.cacheManager = cacheManager;
    }

    public List<User> findUsersByDisplayName(String displayNameLike) {
        if (LOG.isDebugEnabled())
            LOG.debug("Looking for users by display name " + displayNameLike);

        Cache cache = cacheManager.getCache("userDisplayNameLike");
        Cache.ValueWrapper wrapper = cache.get(displayNameLike);

        if (wrapper != null)
            return (List<User>) wrapper.get();

        String ldapPersonSearchBase = ldapSettings.getLdapPersonSearchBase();
        String ldapDisplayNameAttribute = ldapSettings.getLdapPersonAttributeDisplayName();
        String ldapExternalIdAttribute = ldapSettings.getLdapPersonAttributeIdExternal();
        SpringSecurityLdapTemplate template = new SpringSecurityLdapTemplate(personLdapContextSource);

        template.setSearchControls(ldapSettings.getSearchControls());

        displayNameLike = displayNameLike.replaceAll(" ", "*");

        String filter = new AndFilter().and(new LikeFilter(ldapDisplayNameAttribute, displayNameLike + "*")).and(new LikeFilter(ldapExternalIdAttribute, "*")).encode();
        try {
            List<IdentityDetails> identityDetailsList = template.search(ldapPersonSearchBase, filter, userDetailsMapper);
            List<User> users = identityDetailsList != null && !identityDetailsList.isEmpty() ? new ArrayList<User>(identityDetailsList.size()) : Collections.<User>emptyList();
            if (identityDetailsList != null) {
                for (IdentityDetails identityDetails : identityDetailsList) {
                    users.add(new User.Builder(identityDetails).build());
                }
            }
            cache.put(displayNameLike, users);
            return users;
        } catch (SizeLimitExceededException e) {
            return null;
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (LOG.isDebugEnabled())
            LOG.debug("Looking for user by username " + username);

        Cache cache = cacheManager.getCache("loadUserByUsername");
        Cache.ValueWrapper wrapper = cache.get(username);

        if (wrapper != null)
            return (UserDetails) wrapper.get();

        if (LOG.isDebugEnabled())
            LOG.debug("Retrieving user by username " + username);

        UserDetails userDetails = delegate.loadUserByUsername(username);
        cache.put(username, userDetails);
        return userDetails;
    }

    public UserDetails loadUserByInternalId(String internalId) {
        if (LOG.isDebugEnabled())
            LOG.debug("Looking for user by internalId " + internalId);

        Cache cache = cacheManager.getCache("loadUserByInternalId");
        Cache.ValueWrapper wrapper = cache.get(internalId);

        if (wrapper != null)
            return (UserDetails) wrapper.get();

        if (LOG.isDebugEnabled())
            LOG.debug("Retrieving user by internalId " + internalId);

        DirContextOperations userData = internalUserSearch.searchForUser(internalId);
        String username = userData.getStringAttribute(ldapSettings.getLdapPersonAttributeIdInternal());
        if (username == null)
            return null;

        UserDetails userDetails = userDetailsMapper.mapUserFromContext(userData, username,
                authoritiesPopulator.getGrantedAuthorities(userData, username));
        cache.put(internalId, userDetails);
        return userDetails;
    }

    public IdentityDetails loadUserByAnyId(String id) {
        try {
            return IdentityDetails.class.cast(loadUserByInternalId(id));
        } catch (UsernameNotFoundException e) {
            try {
                return IdentityDetails.class.cast(loadUserByUsername(id));
            } catch (UsernameNotFoundException e2) {
                return null;
            }
        }
    }

    public User getUser(String internalId) {
        try {
            if (StringUtils.isNotEmpty(internalId)) {
                UserDetails userDetails = loadUserByInternalId(internalId);

                if (userDetails != null)
                    return new User.Builder(userDetails).build();
            }
        } catch (UsernameNotFoundException e) {
            // Ignore and return null
        }
        return null;
    }

    public User getUserByAnyId(String id) {
        UserDetails userDetails = loadUserByAnyId(id);

        if (userDetails != null)
            return new User.Builder(userDetails).build();

        return null;
    }


}
