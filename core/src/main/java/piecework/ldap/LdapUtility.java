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

import org.springframework.ldap.authentication.DefaultValuesAuthenticationSourceDecorator;
import org.springframework.ldap.core.AuthenticationSource;
import org.springframework.ldap.core.support.DirContextAuthenticationStrategy;
import org.springframework.ldap.core.support.ExternalTlsDirContextAuthenticationStrategy;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.core.support.SimpleDirContextAuthenticationStrategy;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import piecework.security.CustomAuthenticationSource;

import javax.net.ssl.SSLSocketFactory;

/**
 * @author James Renfro
 */
public class LdapUtility {

    public static AuthenticationSource authenticationSource(LdapSettings ldapSettings) {
        CustomAuthenticationSource authenticationSource = new CustomAuthenticationSource();

        LdapSettings.LdapAuthenticationEncryption encryption = ldapSettings.getEncryption();

        if (encryption == LdapSettings.LdapAuthenticationEncryption.TLS)
            return new DefaultValuesAuthenticationSourceDecorator(authenticationSource, ldapSettings.getLdapDefaultUser(), new String(ldapSettings.getLdapDefaultPassword()));

        return authenticationSource;
    }

    public static DirContextAuthenticationStrategy authenticationStrategy(LdapSettings ldapSettings, SSLSocketFactory sslSocketFactory) throws Exception {
        LdapSettings.LdapAuthenticationEncryption encryption = ldapSettings.getEncryption();

        DirContextAuthenticationStrategy strategy = null;

        switch (encryption) {
            case TLS:
                strategy = new ExternalTlsDirContextAuthenticationStrategy();
                ((ExternalTlsDirContextAuthenticationStrategy)strategy).setSslSocketFactory(sslSocketFactory);
                break;
            default:
                strategy = new SimpleDirContextAuthenticationStrategy();
        }

        return strategy;
    }

    public static LdapAuthoritiesPopulator authoritiesPopulator(LdapSettings ldapSettings, SSLSocketFactory sslSocketFactory) throws Exception {
        DefaultLdapAuthoritiesPopulator authoritiesPopulator = new DefaultLdapAuthoritiesPopulator(groupLdapContextSource(ldapSettings, sslSocketFactory), ldapSettings.getLdapGroupSearchBase());
        authoritiesPopulator.setGroupSearchFilter(ldapSettings.getLdapGroupMemberSearchFilter());
        return authoritiesPopulator;
    }

    public static LdapContextSource groupLdapContextSource(LdapSettings ldapSettings, SSLSocketFactory sslSocketFactory) throws Exception {

        LdapContextSource context = new LdapContextSource();
        context.setUrl(ldapSettings.getLdapGroupUrl());
        context.setBase(ldapSettings.getLdapGroupBase());
        context.setUserDn(ldapSettings.getLdapPersonDn());
        context.setAuthenticationSource(authenticationSource(ldapSettings));
        context.setAuthenticationStrategy(authenticationStrategy(ldapSettings, sslSocketFactory));
        context.afterPropertiesSet();

        return context;
    }

    public static LdapContextSource personLdapContextSource(LdapSettings ldapSettings, SSLSocketFactory sslSocketFactory) throws Exception {

        LdapContextSource context = new LdapContextSource();
        context.setUrl(ldapSettings.getLdapPersonUrl());
        context.setBase(ldapSettings.getLdapPersonBase());
        context.setUserDn(ldapSettings.getLdapPersonDn());
        context.setAuthenticationSource(authenticationSource(ldapSettings));
        context.setAuthenticationStrategy(authenticationStrategy(ldapSettings, sslSocketFactory));
        context.afterPropertiesSet();

        return context;
    }

    public static LdapUserSearch userSearch(LdapContextSource personLdapContextSource, LdapSettings ldapSettings) throws Exception {
        LdapUserSearch userSearch = new FilterBasedLdapUserSearch(ldapSettings.getLdapPersonSearchBase(), ldapSettings.getLdapPersonSearchFilter(), personLdapContextSource);
        ((FilterBasedLdapUserSearch)userSearch).setReturningAttributes(null);
        ((FilterBasedLdapUserSearch)userSearch).setSearchSubtree(true);
        ((FilterBasedLdapUserSearch)userSearch).setSearchTimeLimit(10000);
        return userSearch;
    }
}
