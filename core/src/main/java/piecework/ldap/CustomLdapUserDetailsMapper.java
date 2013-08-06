/*
 * Copyright 2012 University of Washington
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

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;

import piecework.identity.DisplayNameConverter;
import piecework.identity.InternalUserDetails;

/**
 * @author James Renfro
 */
public class CustomLdapUserDetailsMapper extends LdapUserDetailsMapper implements ContextMapper {

	private final Log logger = LogFactory.getLog(CustomLdapUserDetailsMapper.class);
	
	private final LdapUserDetailsMapper delegate;
    private final String ldapInternalIdAttribute;
    private final String ldapExternalIdAttribute;
    private final String ldapDisplayNameAttribute;
    private final String ldapEmailAttribute;

    @Autowired(required = false)
    DisplayNameConverter displayNameConverter;

	public CustomLdapUserDetailsMapper(LdapUserDetailsMapper delegate, Environment environment) {
		this.delegate = delegate;
        this.ldapInternalIdAttribute = environment.getProperty("ldap.attribute.id.internal");
        this.ldapExternalIdAttribute = environment.getProperty("ldap.attribute.id.external");
        this.ldapDisplayNameAttribute = environment.getProperty("ldap.attribute.name.display");
        this.ldapEmailAttribute = environment.getProperty("ldap.attribute.email");
	}
	
	public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
        UserDetails userDetails = delegate.mapUserFromContext(ctx, username, authorities);
        String internalId = ctx.getStringAttribute(ldapInternalIdAttribute);
        String externalId = ctx.getStringAttribute(ldapExternalIdAttribute);
        String displayName = ctx.getStringAttribute(ldapDisplayNameAttribute);
        String emailAddress = StringUtils.isNotEmpty(ldapEmailAttribute) ? ctx.getStringAttribute(ldapEmailAttribute) : "";

        if (displayNameConverter != null)
            displayName = displayNameConverter.convert(displayName);

        return new InternalUserDetails(userDetails, internalId, externalId, displayName, emailAddress);
    }

    @Override
    public Object mapFromContext(Object ctx) {
        return mapUserFromContext((DirContextOperations) ctx, null, null);
    }
}
