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
package piecework.identity;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.SizeLimitExceededException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.ldap.userdetails.LdapUserDetailsService;
import piecework.ldap.CustomLdapUserDetailsMapper;
import piecework.ldap.LdapSettings;
import piecework.model.User;

import javax.naming.directory.SearchControls;
import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
public class InternalUserDetailsService implements UserDetailsService {

    @Autowired
    Environment environment;

    @Autowired
    LdapContextSource personLdapContextSource;

    @Autowired
    LdapSettings ldapSettings;

    private final LdapUserDetailsService delegate;
    private final LdapUserSearch userSearch;
    private final LdapUserSearch internalUserSearch;
    private final LdapAuthoritiesPopulator authoritiesPopulator;
    private final CustomLdapUserDetailsMapper userDetailsMapper;
    private final String usernameAttribute;

    public InternalUserDetailsService() {
        this.delegate = null;
        this.userDetailsMapper = null;
        this.userSearch = null;
        this.internalUserSearch = null;
        this.authoritiesPopulator = null;
        this.usernameAttribute = null;
    }

    public InternalUserDetailsService(LdapUserSearch userSearch, LdapUserSearch internalUserSearch, LdapAuthoritiesPopulator authoritiesPopulator, CustomLdapUserDetailsMapper userDetailsMapper, String usernameAttribute) {
        this.delegate = new LdapUserDetailsService(userSearch, authoritiesPopulator);
        this.delegate.setUserDetailsMapper(userDetailsMapper);
        this.userDetailsMapper = userDetailsMapper;
        this.userSearch = userSearch;
        this.internalUserSearch = internalUserSearch;
        this.authoritiesPopulator = authoritiesPopulator;
        this.usernameAttribute = usernameAttribute;
    }

    public List<InternalUserDetails> findUsersByDisplayName(String displayNameLike) {
        String ldapPersonSearchBase = ldapSettings.getLdapPersonSearchBase();
        String ldapDisplayNameAttribute = ldapSettings.getLdapPersonAttributeDisplayName();
        String ldapExternalIdAttribute = ldapSettings.getLdapPersonAttributeIdExternal();
        SpringSecurityLdapTemplate template = new SpringSecurityLdapTemplate(personLdapContextSource);

        template.setSearchControls(ldapSettings.getSearchControls());

        displayNameLike = displayNameLike.replaceAll(" ", "*");

        String filter = new AndFilter().and(new LikeFilter(ldapDisplayNameAttribute, displayNameLike + "*")).and(new LikeFilter(ldapExternalIdAttribute, "*")).encode();
        try {
            return template.search(ldapPersonSearchBase, filter, userDetailsMapper);
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
        String username = userData.getStringAttribute(usernameAttribute);
        if (username == null)
            return null;
        return userDetailsMapper.mapUserFromContext(userData, username,
                authoritiesPopulator.getGrantedAuthorities(userData, username));
    }

    public InternalUserDetails loadUserByAnyId(String id) {
        try {
            return InternalUserDetails.class.cast(loadUserByInternalId(id));
        } catch (UsernameNotFoundException e) {
            try {
                return InternalUserDetails.class.cast(loadUserByUsername(id));
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
