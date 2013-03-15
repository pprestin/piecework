/* 
 * This file is a modification of the following class from the spring-security-ldap
 * release 3.1.3: 
 * org.springframework.security.ldap.authentication.PasswordComparisonAuthenticator
 * 
 * It is included in compliance with the following license. 
 * 
 * [Original copyright and license follows]
 * -------------------------------------------------------------------
 * Copyright 2004, 2005, 2006 Acegi Technology Pty Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * --------------------------------------------------------------------
 */
package piecework.authentication.ldap;

import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticator;
import org.springframework.util.Assert;

/**
 * This "authenticator" assumes that you're running the web app behind a single-sign-on
 * service, and have already authenticated your principal, and merely want to use
 * LDAP to populate your user object, probably because you are going to make use 
 * of an LDAP authorities populator to get the list of groups you'll use during 
 * authorization. 
 *
 */
public class PreauthenticatedLdapAuthenticator extends AbstractLdapAuthenticator {

	public PreauthenticatedLdapAuthenticator(ContextSource contextSource) {
		super(contextSource);
	}

	public DirContextOperations authenticate(final Authentication authentication) {
        Assert.isInstanceOf(UsernamePasswordAuthenticationToken.class, authentication,
                "Can only process UsernamePasswordAuthenticationToken objects");

        DirContextOperations user = null;
        String username = authentication.getName();

        SpringSecurityLdapTemplate ldapTemplate = new SpringSecurityLdapTemplate(getContextSource());
        ldapTemplate.setIgnoreNameNotFoundException(true);
        
        for (String userDn : getUserDns(username)) {
            try {
                user = ldapTemplate.retrieveEntry(userDn, getUserAttributes());
            } catch (NameNotFoundException ignore) {
            	ignore.printStackTrace();
            }
            if (user != null) {
                break;
            }
        }

        if (user == null && getUserSearch() != null) {
            user = getUserSearch().searchForUser(username);
        }

        return user;
    }
	
}
