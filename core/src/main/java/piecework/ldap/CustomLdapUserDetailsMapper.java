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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;

import piecework.identity.InternalUserDetails;

/**
 * @author James Renfro
 */
public class CustomLdapUserDetailsMapper extends LdapUserDetailsMapper {

	private final Log logger = LogFactory.getLog(CustomLdapUserDetailsMapper.class);
	
	private final LdapUserDetailsMapper delegate;
    private final String ldapDisplayNameAttribute;
    private final String ldapEmailAttribute;

	public CustomLdapUserDetailsMapper(LdapUserDetailsMapper delegate, Environment environment) {
		this.delegate = delegate;
        this.ldapDisplayNameAttribute = environment.getProperty("ldap.displayname.attribute");
        this.ldapEmailAttribute = environment.getProperty("ldap.email.attribute");
	}
	
	public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
        UserDetails userDetails = delegate.mapUserFromContext(ctx, username, authorities);
        String displayName = ctx.getStringAttribute(ldapDisplayNameAttribute);
        String emailAddress = ctx.getStringAttribute(ldapEmailAttribute);
        return new InternalUserDetails(userDetails, displayName, emailAddress);
    }
	
}
