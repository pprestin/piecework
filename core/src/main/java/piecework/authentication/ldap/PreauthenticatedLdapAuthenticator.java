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
 * @author James Renfro
 */
public class PreauthenticatedLdapAuthenticator extends AbstractLdapAuthenticator {

	public PreauthenticatedLdapAuthenticator(ContextSource contextSource) {
		super(contextSource);
	}

	public DirContextOperations authenticate(final Authentication authentication) {
        Assert.isInstanceOf(UsernamePasswordAuthenticationToken.class, authentication,
                "Can only process UsernamePasswordAuthenticationToken objects");
        // locate the user and check the password

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
