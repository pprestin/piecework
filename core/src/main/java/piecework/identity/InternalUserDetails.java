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
package piecework.identity;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetails;

/**
 * @author James Renfro
 */
public class InternalUserDetails implements LdapUserDetails {

	private static final long serialVersionUID = 1L;
	
	private final UserDetails delegate;
    private final String internalId;
    private final String externalId;
	private final String displayName;
    private final String emailAddress;
	
	public InternalUserDetails(UserDetails delegate, String internalId, String externalId, String displayName, String emailAddress) {
		this.delegate = delegate;
        this.internalId = internalId;
        this.externalId = externalId;
		this.displayName = displayName;
        this.emailAddress = emailAddress;
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return delegate.getAuthorities();
	}

	@Override
	public String getPassword() {
		return delegate.getPassword();
	}

	@Override
	public String getUsername() {
		return delegate.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		return delegate.isAccountNonExpired();
	}

	@Override
	public boolean isAccountNonLocked() {
		return delegate.isAccountNonLocked();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return delegate.isCredentialsNonExpired();
	}

	@Override
	public boolean isEnabled() {
		return delegate.isEnabled();
	}

    public String getInternalId() {
        return internalId;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getDisplayName() {
		return displayName;
	}

    public String getEmailAddress() {
        return emailAddress;
    }

    @Override
    public String getDn() {
        if (delegate instanceof LdapUserDetails) {
            LdapUserDetails ldapUserDetails = LdapUserDetails.class.cast(delegate);
            return ldapUserDetails.getDn();
        }

        return null;
    }
}
