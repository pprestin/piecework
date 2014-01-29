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
package piecework.security;

import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import piecework.identity.AuthenticationPrincipalConverter;

/**
 * Handles the remapping of authorities using a GrantedAuthoritiesMapper the same way that it's done in other
 * Spring Security AuthenticationProvider classes. 
 * 
 * @author James Renfro
 */
public class AuthorityMappingPreAuthenticatedProvider extends PreAuthenticatedAuthenticationProvider {

	private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        PreAuthenticatedAuthenticationToken result;
        if (authentication != null && authentication.getCredentials() != null && authentication.getCredentials() instanceof X509Certificate) {
            GrantedAuthority authority = new SimpleGrantedAuthority("CERTIFICATE_" + authentication.getPrincipal());
            Collection<? extends GrantedAuthority> authorities = authoritiesMapper.mapAuthorities(Collections.singletonList(authority));
            result = new PreAuthenticatedAuthenticationToken(authentication.getPrincipal(), authentication.getCredentials(), authorities);
        } else {
            PreAuthenticatedAuthenticationToken token = PreAuthenticatedAuthenticationToken.class.cast(super.authenticate(authentication));
            Collection<? extends GrantedAuthority> authorities = authoritiesMapper.mapAuthorities(token.getAuthorities());
            result = new PreAuthenticatedAuthenticationToken(token.getPrincipal(), token.getCredentials(), authorities);
            result.setDetails(token.getDetails());
        }
        return result;
    }

	public GrantedAuthoritiesMapper getAuthoritiesMapper() {
		return authoritiesMapper;
	}

	public void setAuthoritiesMapper(GrantedAuthoritiesMapper authoritiesMapper) {
		this.authoritiesMapper = authoritiesMapper;
	}
	
}
