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

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import piecework.ldap.LdapGroupService;
import piecework.ldap.LdapSettings;
import piecework.ldap.LdapUtility;
import piecework.service.CacheService;
import piecework.service.GroupService;
import piecework.service.IdentityService;

import javax.net.ssl.SSLSocketFactory;

/**
 * @author James Renfro
 */
public class GroupServiceFactoryBean implements FactoryBean<GroupService> {

    @Autowired
    CacheService cacheService;

    @Autowired
    Environment environment;

    @Autowired
    IdentityService identityService;

    @Autowired
    LdapSettings ldapSettings;

    @Autowired
    SSLSocketFactory sslSocketFactory;

    @Override
    public GroupService getObject() throws Exception {
        LdapContextSource contextSource = LdapUtility.groupLdapContextSource(ldapSettings, sslSocketFactory);
        FilterBasedLdapUserSearch groupSearch = new FilterBasedLdapUserSearch(ldapSettings.getLdapGroupSearchBase(), ldapSettings.getLdapGroupSearchFilter(), contextSource);
        groupSearch.setReturningAttributes(null);
        groupSearch.setSearchSubtree(true);
        groupSearch.setSearchTimeLimit(10000);

        return new LdapGroupService(cacheService, identityService, contextSource, groupSearch, ldapSettings);
    }

    @Override
    public Class<?> getObjectType() {
        return GroupService.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
