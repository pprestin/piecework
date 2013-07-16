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
package piecework.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.authentication.SpringSecurityAuthenticationSource;
import org.springframework.security.ldap.userdetails.LdapUserDetails;

import java.security.cert.X509Certificate;

/**
 * @author James Renfro
 */
public class CustomAuthenticationSource extends SpringSecurityAuthenticationSource {

    private static final Log log = LogFactory.getLog(CustomAuthenticationSource.class);

    public String getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            log.warn("No Authentication object set in SecurityContext - returning empty String as Principal");
            return "";
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof LdapUserDetails) {
            LdapUserDetails details = (LdapUserDetails) principal;
            return details.getDn();
        } else if (authentication.getCredentials() != null && authentication.getCredentials() instanceof X509Certificate) {
            if (log.isDebugEnabled()) {
                log.debug("Authenticated by certificate, returning certificate subject name as Principal");
            }
            return principal.toString();
        } else if (authentication instanceof AnonymousAuthenticationToken) {
            if (log.isDebugEnabled()) {
                log.debug("Anonymous Authentication, returning empty String as Principal");
            }
            return "";
        } else {
            throw new IllegalArgumentException("The principal property of the authentication object"
                    + "needs to be an LdapUserDetails.");
        }
    }

    public String getCredentials() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            log.warn("No Authentication object set in SecurityContext - returning empty String as Credentials");
            return "";
        } else if (authentication.getCredentials() != null && authentication.getCredentials() instanceof X509Certificate) {
            if (log.isDebugEnabled()) {
                log.debug("Authenticated by certificate, returning empty string as credentials");
            }
            return "";
        }

        return (String) authentication.getCredentials();
    }

}
