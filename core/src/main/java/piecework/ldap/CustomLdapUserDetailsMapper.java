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
import java.util.Collections;

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
import piecework.identity.IdentityDetails;

/**
 * @author James Renfro
 */
public class CustomLdapUserDetailsMapper extends LdapUserDetailsMapper implements ContextMapper {

	private final Log logger = LogFactory.getLog(CustomLdapUserDetailsMapper.class);

    @Autowired(required = false)
    DisplayNameConverter displayNameConverter;

    @Autowired
    LdapSettings ldapSettings;

	private final LdapUserDetailsMapper delegate;

	public CustomLdapUserDetailsMapper(LdapUserDetailsMapper delegate) {
		this.delegate = delegate;
    }
	
	public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
        UserDetails userDetails = delegate.mapUserFromContext(ctx, username, authorities);
        String internalId = ctx.getStringAttribute(ldapSettings.getLdapPersonAttributeIdInternal());
        String externalId = ctx.getStringAttribute(ldapSettings.getLdapPersonAttributeIdExternal());
        String displayName = ctx.getStringAttribute(ldapSettings.getLdapPersonAttributeDisplayName());
        String emailAddress = StringUtils.isNotEmpty(ldapSettings.getLdapPersonAttributeEmail()) ? ctx.getStringAttribute(ldapSettings.getLdapPersonAttributeEmail()) : "";

        if (displayNameConverter != null)
            displayName = displayNameConverter.convert(displayName);

        return new IdentityDetails(userDetails, internalId, externalId, displayName, emailAddress);
    }

    @Override
    public Object mapFromContext(Object ctx) {
        if (ctx instanceof DirContextOperations) {
            DirContextOperations contextOperations = DirContextOperations.class.cast(ctx);
            String internalId = contextOperations.getStringAttribute(ldapSettings.getLdapPersonAttributeIdInternal());
            Collection<GrantedAuthority> authorities = Collections.emptyList();
            return mapUserFromContext(contextOperations, internalId, authorities);
        }
        return null;
    }
}
