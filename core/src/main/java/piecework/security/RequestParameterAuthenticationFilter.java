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

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;

/**
 * @author James Renfro
 */
public class RequestParameterAuthenticationFilter extends RequestHeaderAuthenticationFilter {

	public RequestParameterAuthenticationFilter(AuthenticationManager authenticationManager) {
		setAuthenticationManager(authenticationManager);
	}
	
	protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        String principal = request.getParameter("userId");

        if (principal == null) {
            throw new PreAuthenticatedCredentialsNotFoundException("userId parameter not found in request.");
        }

        return principal;
    }
	
}
