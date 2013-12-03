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
import org.springframework.cache.CacheManager;
import org.springframework.ldap.SizeLimitExceededException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapUserDetailsService;
import piecework.identity.IdentityDetails;
import piecework.model.User;
import piecework.service.IdentityService;

import javax.naming.directory.SearchControls;
import java.util.*;

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
    private final LdapAuthoritiesPopulator authoritiesPopulator;
    private final CustomLdapUserDetailsMapper userDetailsMapper;

    public LdapIdentityService() {
        this.cacheManager = null;
        this.personLdapContextSource = null;
        this.delegate = null;
        this.userDetailsMapper = null;
        this.userSearch = null;
        this.authoritiesPopulator = null;
        this.ldapSettings = null;
    }

    public LdapIdentityService(LdapContextSource personLdapContextSource, LdapUserSearch userSearch, LdapAuthoritiesPopulator authoritiesPopulator, CustomLdapUserDetailsMapper userDetailsMapper, LdapSettings ldapSettings, CacheManager cacheManager) {
        this.personLdapContextSource = personLdapContextSource;
        this.delegate = new LdapUserDetailsService(userSearch, authoritiesPopulator);
        this.delegate.setUserDetailsMapper(userDetailsMapper);
        this.userDetailsMapper = userDetailsMapper;
        this.userSearch = userSearch;
        this.authoritiesPopulator = authoritiesPopulator;
        this.ldapSettings = ldapSettings;
        this.cacheManager = cacheManager;
    }

    @Override
    public Map<String, User> findUsers(Set<String> ids) {
        long start = 0l;
        if (LOG.isDebugEnabled())
            start = System.currentTimeMillis();
        Map<String, User> map = new HashMap<String, User>();
        if (ids != null) {
            String internalId = ldapSettings.getLdapPersonAttributeIdInternal();
            OrFilter filter = new OrFilter();
            for (String id : ids) {
                filter.or(new EqualsFilter(internalId, id));
            }
            List<User> users = findMany(filter, -1);
            if (users != null) {
                for (User user : users) {
                    if (user != null)
                        map.put(user.getUserId(), user);
                }
            }
        }

        if (LOG.isDebugEnabled())
            LOG.debug("Retrieving users for " + ids.size() + " took " + (System.currentTimeMillis() - start) + " ms");
        return map;
    }

    public List<User> findUsersByDisplayName(String displayNameLike, Long maxResults) {
        if (LOG.isDebugEnabled())
            LOG.debug("Looking for users by display name " + displayNameLike);

        displayNameLike = displayNameLike.replaceAll(" ", "*");

        String ldapDisplayNameAttribute = ldapSettings.getLdapPersonAttributeDisplayName();
        String ldapExternalIdAttribute = ldapSettings.getLdapPersonAttributeIdExternal();
        String ldapEmailAttribute = ldapSettings.getLdapPersonAttributeEmail();

        long countLimit = maxResults != null ? maxResults.longValue() : 100l;
        Filter filter = new AndFilter().and(new LikeFilter(ldapDisplayNameAttribute, displayNameLike + "*")).and(new LikeFilter(ldapExternalIdAttribute, "*"));
        return findMany(filter, countLimit);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (LOG.isDebugEnabled())
            LOG.debug("Looking for user by uniqueId " + username);

        Cache cache = cacheManager.getCache("loadUserByUsername");
        Cache.ValueWrapper wrapper = cache.get(username);

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
            userDetails = delegate.loadUserByUsername(username);
        } finally {
            cache.put(username, userDetails);
        }
        return userDetails;
    }

    public User getUser(String id) {
        try {
            UserDetails userDetails = IdentityDetails.class.cast(loadUserByUsername(id));
            return new User.Builder(userDetails).build();
        } catch (UsernameNotFoundException e) {
            return null;
        }
    }

    private List<User> findMany(Filter filter, long countLimit) {
        String encoded = filter.encode();
        Cache cache = cacheManager.getCache("userCache");
        Cache.ValueWrapper wrapper = cache.get(encoded);
        if (wrapper != null)
            return (List<User>) wrapper.get();

        String ldapPersonSearchBase = ldapSettings.getLdapPersonSearchBase();
        SpringSecurityLdapTemplate template = new SpringSecurityLdapTemplate(personLdapContextSource);

        SearchControls defaultSearchControls = ldapSettings.getSearchControls();
        SearchControls searchControls = new SearchControls(defaultSearchControls.getSearchScope(),
                countLimit, defaultSearchControls.getTimeLimit(), defaultSearchControls.getReturningAttributes(),
                defaultSearchControls.getReturningObjFlag(), defaultSearchControls.getDerefLinkFlag());

        template.setSearchControls(searchControls);

        try {
            List<IdentityDetails> identityDetailsList = template.search(ldapPersonSearchBase, encoded, userDetailsMapper);
            List<User> users = identityDetailsList != null && !identityDetailsList.isEmpty() ? new ArrayList<User>(identityDetailsList.size()) : Collections.<User>emptyList();
            if (identityDetailsList != null) {
                for (IdentityDetails identityDetails : identityDetailsList) {
                    users.add(new User.Builder(identityDetails).build());
                }
            }
            cache.put(encoded, users);
            return users;
        } catch (SizeLimitExceededException e) {
            return null;
        }
    }
}
