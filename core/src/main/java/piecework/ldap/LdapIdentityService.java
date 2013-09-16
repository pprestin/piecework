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
import org.springframework.beans.factory.annotation.Autowired;
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

    private final LdapContextSource personLdapContextSource;
    private final LdapSettings ldapSettings;
    private final LdapUserDetailsService delegate;
    private final LdapUserSearch userSearch;
    private final LdapUserSearch internalUserSearch;
    private final LdapAuthoritiesPopulator authoritiesPopulator;
    private final CustomLdapUserDetailsMapper userDetailsMapper;

    public LdapIdentityService() {
        this.personLdapContextSource = null;
        this.delegate = null;
        this.userDetailsMapper = null;
        this.userSearch = null;
        this.internalUserSearch = null;
        this.authoritiesPopulator = null;
        this.ldapSettings = null;
    }

    public LdapIdentityService(LdapContextSource personLdapContextSource, LdapUserSearch userSearch, LdapUserSearch internalUserSearch, LdapAuthoritiesPopulator authoritiesPopulator, CustomLdapUserDetailsMapper userDetailsMapper, LdapSettings ldapSettings) {
        this.personLdapContextSource = personLdapContextSource;
        this.delegate = new LdapUserDetailsService(userSearch, authoritiesPopulator);
        this.delegate.setUserDetailsMapper(userDetailsMapper);
        this.userDetailsMapper = userDetailsMapper;
        this.userSearch = userSearch;
        this.internalUserSearch = internalUserSearch;
        this.authoritiesPopulator = authoritiesPopulator;
        this.ldapSettings = ldapSettings;
    }

    public List<User> findUsersByDisplayName(String displayNameLike) {
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
            return users;
        } catch (SizeLimitExceededException e) {
            return null;
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return delegate.loadUserByUsername(username);
    }

    public UserDetails loadUserByInternalId(String internalId) {
        DirContextOperations userData = internalUserSearch.searchForUser(internalId);
        String username = userData.getStringAttribute(ldapSettings.getLdapPersonAttributeIdInternal());
        if (username == null)
            return null;
        return userDetailsMapper.mapUserFromContext(userData, username,
                authoritiesPopulator.getGrantedAuthorities(userData, username));
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

    @Cacheable(value="userCache")
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

    @Cacheable(value="userAnyIdCache")
    public User getUserByAnyId(String id) {
        UserDetails userDetails = loadUserByAnyId(id);

        if (userDetails != null)
            return new User.Builder(userDetails).build();

        return null;
    }


}
