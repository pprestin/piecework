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
package piecework.security.concrete;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import org.springframework.util.StringUtils;

/**
 * @author James Renfro
 */
public class DebugAuthenticationFilter extends RequestHeaderAuthenticationFilter {

	private final String testUser;
    private final String testCredentials;
	
	public DebugAuthenticationFilter(AuthenticationManager authenticationManager, String testUser, String testCredentials) {
		setAuthenticationManager(authenticationManager);
		this.testUser = testUser;
        this.testCredentials = testCredentials;
        this.setCheckForPrincipalChanges(true);
        this.setInvalidateSessionOnPrincipalChange(true);
	}
	
	protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        String principal = request.getParameter("userId");

        if (principal == null)
            principal = String.class.cast(request.getSession(true).getAttribute("backdoorDebugUserId"));
        else
            request.getSession(true).setAttribute("backdoorDebugUserId", principal);

        if (StringUtils.isEmpty(principal))
            principal = request.getRemoteUser();

        if (principal == null)
        	principal = this.testUser;
        
        if (principal == null) 
            return super.getPreAuthenticatedPrincipal(request);

        return principal;
    }

    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        if (this.testCredentials != null) {
            return this.testCredentials;
        }

        return "N/A";
    }
	
}
