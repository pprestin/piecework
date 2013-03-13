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

import java.security.Principal;
import java.util.Arrays;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.cxf.common.security.SecurityToken;
import org.apache.cxf.common.security.TokenType;
import org.apache.cxf.common.security.UsernameToken;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.interceptor.security.AbstractAuthorizingInInterceptor;
import org.apache.cxf.interceptor.security.AccessDeniedException;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.apache.cxf.security.SecurityContext;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import piecework.authentication.AuthenticationHandler;

/**
 * @author James Renfro
 */
@Provider
public class LdapAuthenticationHandler implements AuthenticationHandler {

	private final ProviderManager providerManager;	
	private final AbstractAuthorizingInInterceptor interceptor;

	public LdapAuthenticationHandler(AuthenticationProvider[] authenticationProviders, AbstractAuthorizingInInterceptor interceptor) {
		this.providerManager = new ProviderManager(Arrays.asList(authenticationProviders)); 
		this.interceptor = interceptor;
	}
	
	public Response handleRequest(Message m, ClassResourceInfo resourceClass) {
        AuthorizationPolicy policy = (AuthorizationPolicy)m.get(AuthorizationPolicy.class);
        String username = null;
        String password = null; 
        
        if (policy != null) {
	        username = policy.getUserName();
	        password = policy.getPassword(); 
        } else {
        	SecurityToken token = m.get(SecurityToken.class);
            if (token != null && token.getTokenType() == TokenType.UsernameToken) {
                UsernameToken ut = (UsernameToken)token;
                username = ut.getName();
                password = ut.getPassword();
            }
        }
        
        if (username != null) {
        	if (password == null) {
        		password = "";
        	}      	
	        Authentication authentication = providerManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
	        	
	        if (authentication.isAuthenticated()) {
	        	m.put(SecurityContext.class, new AuthenticationSecurityContext(authentication));
	        	
	        	try {
	                interceptor.handleMessage(m);
	                return null;
	            } catch (AccessDeniedException ex) {
	                return Response.status(Response.Status.FORBIDDEN).build();
	            }
	        }
        }
        
        return Response.status(401).build();
    }
	
//	public boolean isAuthenticated(String username, String password) {
//		Authentication authentication = providerManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
//		return authentication.isAuthenticated();
//	}
	
}
