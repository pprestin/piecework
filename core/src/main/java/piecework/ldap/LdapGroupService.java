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

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.search.LdapUserSearch;

import piecework.model.User;
import piecework.model.Group;
import piecework.service.IdentityService;
import piecework.service.GroupService;


/**
 * @author Jiefeng Shen
 */
public class LdapGroupService implements GroupService {

    private static final Logger LOG = Logger.getLogger(LdapGroupService.class);

    private final CacheManager cacheManager;
    private final LdapContextSource groupLdapContextSource;
    private final LdapSettings ldapSettings;
    private final LdapUserSearch groupSearch;

    IdentityService userDetailsService;  // needed to populate user details of group members.

    public LdapGroupService() {
        this.cacheManager = null;
        this.groupLdapContextSource = null;
        this.groupSearch = null;
        this.ldapSettings = null;
    }

    public LdapGroupService(LdapContextSource groupLdapContextSource, LdapUserSearch groupSearch, LdapSettings ldapSettings, IdentityService userService, CacheManager cacheManager) {
        this.groupLdapContextSource = groupLdapContextSource;
        this.groupSearch = groupSearch;
        this.ldapSettings = ldapSettings;
        this.userDetailsService = userService;
        this.cacheManager = cacheManager;
    }

    /**
     * returns a group uniquely identified by a group ID.
     * The unique identifier could be a group ID or a group name, as long as it could
     * uniquely identify a group on the ldap server. The attributes used for group search
     * are configured in a configuration file.
     * @param  groupId an Id or a name that unqiuely identifies a ldap group.
     * @return         a Group object if found, null otherwise.
     */
    public Group getGroupById(String groupId) {
        if (LOG.isDebugEnabled())
            LOG.debug("Looking in cache for group by groupId " + groupId);

        // sanity check
        if ( groupId == null || groupId.isEmpty() ) {
            return null;
        }

        Cache cache = cacheManager.getCache("loadGroupByName");
        Cache.ValueWrapper wrapper = cache.get(groupId);

        if (wrapper != null)
            return (Group) wrapper.get();

        if (LOG.isDebugEnabled())
            LOG.debug("Retrieving group from ldap server by groupId " + groupId);

        try {
            DirContextOperations groupData = groupSearch.searchForUser(groupId);
            Group.Builder builder = new Group.Builder();

            // get group attributes
            String memberKey = ldapSettings.getLdapGroupAttributeMember();
            if ( memberKey == null || memberKey.isEmpty() ) {
                memberKey = "member";  // default
            }
            String[] members = groupData.getStringAttributes(memberKey);

            String groupIdKey = ldapSettings.getLdapGroupAttributeId();
            if ( groupIdKey != null && !groupIdKey.isEmpty() ) {
                 builder.groupId( groupData.getStringAttribute(groupIdKey));
            }

            String displayNameKey = ldapSettings.getLdapGroupAttributeDisplayName();
            if ( displayNameKey != null && !displayNameKey.isEmpty() ) {
                 builder.displayName(groupData.getStringAttribute(displayNameKey));
            }

            String prefix = ldapSettings.getLdapGroupUseridPrefix();
            prefix = prefix == null ? "" : prefix; // set prefix to "" for easier use later.
            for ( String m : members ) {
                if ( prefix == null || prefix.isEmpty() || m.startsWith(prefix) ) {
                    String userId = m.substring(prefix.length());
                    User user = userDetailsService.getUserByAnyId(userId);
                    if ( user != null ) {
                        builder.member(user);
                    }
                }
            }
    
            Group group = builder.build();
            cache.put(groupId, group);
            return group;
        } catch (UsernameNotFoundException e) {
            // log an error message and continue
            LOG.warn("group " + groupId + " not found");
        }   
        return null;
    }
}
