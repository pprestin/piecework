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

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;

import org.apache.cxf.common.security.SecurityToken;
import org.apache.cxf.common.security.UsernameToken;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.apache.log4j.Logger;

import piecework.authentication.AuthenticationHandler;


/**
 * @author James Renfro
 */
public class AuthenticationHandlerForDevelopment implements AuthenticationHandler {

	private static final Logger LOG = Logger.getLogger(AuthenticationHandlerForDevelopment.class);
	
	private AuthenticationHandler delegate;
	
	public AuthenticationHandlerForDevelopment(AuthenticationHandler delegate) {
		this.delegate = delegate;
	}

	public Response handleRequest(Message m, ClassResourceInfo resourceClass) {
		LOG.warn("DEVELOPMENT AUTHENTICATION HANDLER IN USE!!! Everybody has superuser access... don't use this in production.");
		String username = System.getProperty("REMOTE_USER");
		String password = null;
		
		if (username == null) {
			HttpServletRequest request = (HttpServletRequest)m.get("HTTP.REQUEST");
			
			if (request != null) {
				HttpSession session = request.getSession(true);
				
				username = request.getParameter("username");
				password = request.getParameter("password");
				
				if (username == null) 
					username = (String) session.getAttribute("username");
				else
					session.setAttribute("username", username);
			}
		}
		
		if (username != null) {
			UsernameToken token = new UsernameToken(username, password, null, false, null, new Date().toString());
			m.put(SecurityToken.class, token);
		}
		return delegate.handleRequest(m, resourceClass);
	}
	
}
