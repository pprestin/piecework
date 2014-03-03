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

import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;

import java.util.Collection;

/**
 * @author James Renfro
 */
public class AuthorityMappingAnonymousAuthenticationProvider extends AnonymousAuthenticationProvider {

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        AnonymousAuthenticationToken result;
        AnonymousAuthenticationToken token = AnonymousAuthenticationToken.class.cast(super.authenticate(authentication));

        Collection<? extends GrantedAuthority> authorities = authoritiesMapper.mapAuthorities(token.getAuthorities());
        return new AnonymousAuthenticationToken(token.getPrincipal().toString(), token.getCredentials(), authorities);
    }

    public GrantedAuthoritiesMapper getAuthoritiesMapper() {
        return authoritiesMapper;
    }

    public void setAuthoritiesMapper(GrantedAuthoritiesMapper authoritiesMapper) {
        this.authoritiesMapper = authoritiesMapper;
    }

}
