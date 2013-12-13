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

import org.apache.log4j.Logger;
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
import piecework.ldap.LdapIdentityService;
import piecework.ldap.LdapSettings;
import piecework.ldap.LdapUtility;
import piecework.security.KeyManagerCabinet;
import piecework.service.CacheService;
import piecework.service.IdentityService;
import piecework.service.ProcessService;

import javax.net.ssl.SSLSocketFactory;

/**
 * @author James Renfro
 */
public class IdentityServiceFactoryBean implements FactoryBean<IdentityService> {

    private static final Logger LOG = Logger.getLogger(IdentityServiceFactoryBean.class);

    @Autowired
    CacheService cacheService;

    @Autowired(required = false)
    DisplayNameConverter displayNameConverter;

    @Autowired
    Environment environment;

    @Autowired
    KeyManagerCabinet cabinet;

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    LdapSettings ldapSettings;

    @Autowired
    ServiceLocator serviceLocator;

    @Autowired
    SSLSocketFactory sslSocketFactory;


    @Override
    public IdentityService getObject() throws Exception {
        Boolean isDebugMode = environment.getProperty("debug.mode", Boolean.class, Boolean.FALSE);
        Boolean isDebugIdentity = environment.getProperty("debug.identity", Boolean.class, Boolean.FALSE);

        if (isDebugIdentity) {
            DebugUserDetailsService debugUserDetailsService = new DebugUserDetailsService(environment, serviceLocator);
            debugUserDetailsService.init();
            return new DebugIdentityService(debugUserDetailsService);
        }

        String identityProviderProtocol = environment.getProperty("identity.provider.protocol");

        LdapContextSource contextSource = LdapUtility.personLdapContextSource(ldapSettings, sslSocketFactory);
        LdapUserSearch userSearch = LdapUtility.userSearch(contextSource, ldapSettings);
        LdapAuthoritiesPopulator authoritiesPopulator = LdapUtility.authoritiesPopulator(ldapSettings, sslSocketFactory);
        CustomLdapUserDetailsMapper userDetailsMapper = new CustomLdapUserDetailsMapper(new LdapUserDetailsMapper(), displayNameConverter, ldapSettings);
        return new LdapIdentityService(userDetailsService, cacheService, contextSource, userSearch, authoritiesPopulator, userDetailsMapper, ldapSettings);
    }

    @Override
    public Class<?> getObjectType() {
        return IdentityService.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
