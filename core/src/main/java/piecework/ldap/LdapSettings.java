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
import org.springframework.core.env.Environment;

import javax.naming.directory.SearchControls;

/**
 * @author James Renfro
 */
public class LdapSettings {
    public enum LdapAuthenticationEncryption { NONE, TLS, SSL }
    public enum LdapAuthenticationType { PASSWORDCOMPARE, BIND }

    private static final Logger LOG = Logger.getLogger(LdapSettings.class);

    private final LdapAuthenticationEncryption encryption;
    private final LdapAuthenticationType authenticationType;
    private final String ldapGroupUrl;
    private final String ldapGroupBase;
    private final String ldapGroupSearchBase;
    private final String ldapGroupSearchFilter;
    private final String ldapGroupMemberSearchFilter;
    private final String ldapGroupAttributeMember;
    private final String ldapGroupAttributeId;
    private final String ldapGroupAttributeDisplayName;
    private final String ldapGroupUseridPrefix;
    private final String ldapPersonDn;
    private final String ldapPersonAttributeDisplayName;
    private final String ldapPersonAttributeIdInternal;
    private final String ldapPersonAttributeIdExternal;
    private final String ldapPersonAttributeEmail;
    private final String ldapPersonUrl;
    private final String ldapPersonBase;
    private final String ldapPersonSearchBase;
    private final String ldapPersonSearchFilter;
    private final String ldapPersonSearchFilterInternal;
    private final String ldapDefaultUser;
    private final char[] ldapDefaultPassword;
    private final SearchControls searchControls;

    public LdapSettings(Environment environment) {
        this.encryption = authenticationEncryption(environment.getProperty("ldap.authentication.encryption"));
        this.authenticationType = authenticationType(environment.getProperty("ldap.authentication.type"));
        this.ldapGroupSearchBase = environment.getProperty("ldap.group.search.base");
        this.ldapGroupSearchFilter  = environment.getProperty("ldap.group.search.filter");
        this.ldapGroupMemberSearchFilter  = environment.getProperty("ldap.group.member.search.filter");
        this.ldapPersonUrl = environment.getProperty("ldap.person.url");
        this.ldapPersonBase = environment.getProperty("ldap.person.base");
        this.ldapPersonDn = environment.getProperty("ldap.person.dn");
        this.ldapPersonAttributeDisplayName = environment.getProperty("ldap.attribute.name.display");
        this.ldapPersonAttributeIdInternal = environment.getProperty("ldap.attribute.id.internal");
        this.ldapPersonAttributeIdExternal = environment.getProperty("ldap.attribute.id.external");
        this.ldapGroupAttributeMember = environment.getProperty("ldap.attribute.group.member");
        this.ldapGroupAttributeId = environment.getProperty("ldap.attribute.group.id");
        this.ldapGroupAttributeDisplayName  = environment.getProperty("ldap.attribute.group.name.display");
        this.ldapGroupUseridPrefix = environment.getProperty("ldap.group.userid.prefix");
        this.ldapPersonAttributeEmail = environment.getProperty("ldap.attribute.email");
        this.ldapPersonSearchBase = environment.getProperty("ldap.person.search.base");
        this.ldapPersonSearchFilter = environment.getProperty("ldap.person.search.filter");
        this.ldapPersonSearchFilterInternal = environment.getProperty("ldap.person.search.filter.internal");
        this.ldapGroupUrl = environment.getProperty("ldap.group.url");
        this.ldapGroupBase = environment.getProperty("ldap.group.base");
        this.ldapDefaultUser = environment.getProperty("ldap.authentication.user");
        this.ldapDefaultPassword = environment.getProperty("ldap.authentication.password") != null ? environment.getProperty("ldap.authentication.password").toCharArray() : null;
        this.searchControls = new SearchControls();
        this.searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        this.searchControls.setTimeLimit(10000);
        this.searchControls.setReturningAttributes(null);
        this.searchControls.setCountLimit(20);
    }

    public LdapAuthenticationEncryption getEncryption() {
        return encryption;
    }

    public LdapAuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    public String getLdapGroupSearchBase() {
        return ldapGroupSearchBase;
    }

    public String getLdapGroupSearchFilter() {
        return ldapGroupSearchFilter;
    }

    public String getLdapGroupMemberSearchFilter() {
        return ldapGroupMemberSearchFilter;
    }

    public String getLdapGroupUrl() {
        return ldapGroupUrl;
    }

    public String getLdapGroupBase() {
        return ldapGroupBase;
    }

    public String getLdapGroupAttributeMember() {
        return ldapGroupAttributeMember;
    }

    public String getLdapGroupAttributeId() {
        return ldapGroupAttributeId;
    }

    public String getLdapGroupAttributeDisplayName() {
        return ldapGroupAttributeDisplayName;
    }

    public String getLdapGroupUseridPrefix() {
        return ldapGroupUseridPrefix;
    }

    public String getLdapPersonDn() {
        return ldapPersonDn;
    }

    public String getLdapPersonUrl() {
        return ldapPersonUrl;
    }

    public String getLdapPersonBase() {
        return ldapPersonBase;
    }

    public String getLdapPersonSearchBase() {
        return ldapPersonSearchBase;
    }

    public String getLdapPersonSearchFilter() {
        return ldapPersonSearchFilter;
    }

    public String getLdapPersonAttributeDisplayName() {
        return ldapPersonAttributeDisplayName;
    }

    public String getLdapPersonAttributeIdInternal() {
        return ldapPersonAttributeIdInternal;
    }

    public String getLdapPersonAttributeIdExternal() {
        return ldapPersonAttributeIdExternal;
    }

    public String getLdapPersonAttributeEmail() {
        return ldapPersonAttributeEmail;
    }

    public String getLdapDefaultUser() {
        return ldapDefaultUser;
    }

    public char[] getLdapDefaultPassword() {
        return ldapDefaultPassword;
    }

    public SearchControls getSearchControls() {
        return searchControls;
    }

    public String getLdapPersonSearchFilterInternal() {
        return ldapPersonSearchFilterInternal;
    }

    private static LdapAuthenticationEncryption authenticationEncryption(String ldapAuthenticationEncryption) {
        LdapAuthenticationEncryption encryption = LdapAuthenticationEncryption.NONE;

        try {
            if (ldapAuthenticationEncryption != null && !ldapAuthenticationEncryption.equalsIgnoreCase("${ldap.authentication.encryption}"))
                encryption = LdapAuthenticationEncryption.valueOf(ldapAuthenticationEncryption.toUpperCase());
        } catch (IllegalArgumentException iae) {
            LOG.warn("Authentication encryption: " + ldapAuthenticationEncryption.toUpperCase() + " is not valid");
        }

        if (LOG.isDebugEnabled())
            LOG.debug("Using ldap authentication encryption " + encryption.toString());

        return encryption;
    }

    private LdapAuthenticationType authenticationType(String ldapAuthenticationType) {

        LdapAuthenticationType type = LdapAuthenticationType.BIND;

        try {
            if (ldapAuthenticationType != null && !ldapAuthenticationType.equalsIgnoreCase("${ldap.authentication.type}"))
                type = LdapAuthenticationType.valueOf(ldapAuthenticationType.toUpperCase());
        } catch (IllegalArgumentException iae) {
            LOG.warn("Authentication type: " + ldapAuthenticationType.toUpperCase() + " is not valid");
        }

        if (LOG.isDebugEnabled())
            LOG.debug("Using ldap authentication type " + type.toString());

        return type;
    }
}
