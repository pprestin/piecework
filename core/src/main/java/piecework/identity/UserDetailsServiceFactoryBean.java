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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import piecework.ServiceLocator;
import piecework.ldap.CustomLdapUserDetailsMapper;
import piecework.ldap.CustomLdapUserDetailsService;
import piecework.ldap.LdapSettings;
import piecework.ldap.LdapUtility;
import piecework.service.CacheService;

import javax.net.ssl.SSLSocketFactory;

/**
 * @author James Renfro
 */
public class UserDetailsServiceFactoryBean implements FactoryBean<UserDetailsService> {

    @Autowired(required = false)
    AuthenticationPrincipalConverter authenticationPrincipalConverter;

    @Autowired
    CacheService cacheService;

    @Autowired(required = false)
    DisplayNameConverter displayNameConverter;

    @Autowired
    Environment environment;

    @Autowired
    LdapSettings ldapSettings;

    @Autowired
    ServiceLocator serviceLocator;

    @Autowired
    SSLSocketFactory sslSocketFactory;

    @Override
    public UserDetailsService getObject() throws Exception {
        Boolean isDebugMode = environment.getProperty("debug.mode", Boolean.class, Boolean.FALSE);
        Boolean isDebugIdentity = environment.getProperty("debug.identity", Boolean.class, Boolean.FALSE);

        if (isDebugIdentity) {
            DebugUserDetailsService userDetailsService = new DebugUserDetailsService(environment, serviceLocator);
            userDetailsService.init();
            return userDetailsService;
        }
        String identityProviderProtocol = environment.getProperty("identity.provider.protocol");

//        LdapSettings ldapSettings = ldapSettings(environment);
        LdapContextSource contextSource = LdapUtility.personLdapContextSource(ldapSettings, sslSocketFactory);
        LdapUserSearch userSearch = LdapUtility.userSearch(contextSource, ldapSettings);
        LdapAuthoritiesPopulator authoritiesPopulator = LdapUtility.authoritiesPopulator(ldapSettings, sslSocketFactory);
        CustomLdapUserDetailsMapper userDetailsMapper = new CustomLdapUserDetailsMapper(new LdapUserDetailsMapper(), displayNameConverter, ldapSettings);
        return new CustomLdapUserDetailsService(authenticationPrincipalConverter, cacheService, userSearch, authoritiesPopulator, userDetailsMapper);
    }

    @Override
    public Class<?> getObjectType() {
        return UserDetailsService.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
