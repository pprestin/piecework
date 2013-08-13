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

import org.springframework.core.env.Environment;

import javax.naming.directory.SearchControls;

/**
 * @author James Renfro
 */
public class LdapSettings {

    private final String ldapGroupUrl;
    private final String ldapGroupBase;
    private final String ldapPersonDn;
    private final String ldapPersonAttributeDisplayName;
    private final String ldapPersonAttributeIdInternal;
    private final String ldapPersonAttributeIdExternal;
    private final String ldapPersonUrl;
    private final String ldapPersonBase;
    private final String ldapPersonSearchBase;
    private final String ldapPersonSearchFilter;
    private final String ldapPersonSearchFilterInternal;
    private final SearchControls searchControls;

    public LdapSettings(Environment environment) {
        this.ldapPersonUrl = environment.getProperty("ldap.person.url");
        this.ldapPersonBase = environment.getProperty("ldap.person.base");
        this.ldapPersonDn = environment.getProperty("ldap.person.dn");
        this.ldapPersonAttributeDisplayName = environment.getProperty("ldap.attribute.name.display");
        this.ldapPersonAttributeIdInternal = environment.getProperty("ldap.attribute.id.internal");
        this.ldapPersonAttributeIdExternal = environment.getProperty("ldap.attribute.id.external");
        this.ldapPersonSearchBase = environment.getProperty("ldap.person.search.base");
        this.ldapPersonSearchFilter = environment.getProperty("ldap.person.search.filter");
        this.ldapPersonSearchFilterInternal = environment.getProperty("ldap.person.search.filter.internal");
        this.ldapGroupUrl = environment.getProperty("ldap.group.url");
        this.ldapGroupBase = environment.getProperty("ldap.group.base");

        this.searchControls = new SearchControls();
        this.searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        this.searchControls.setTimeLimit(10000);
        this.searchControls.setReturningAttributes(null);
        this.searchControls.setCountLimit(20);
    }

    public String getLdapGroupUrl() {
        return ldapGroupUrl;
    }

    public String getLdapGroupBase() {
        return ldapGroupBase;
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

    public SearchControls getSearchControls() {
        return searchControls;
    }

    public String getLdapPersonSearchFilterInternal() {
        return ldapPersonSearchFilterInternal;
    }

}
