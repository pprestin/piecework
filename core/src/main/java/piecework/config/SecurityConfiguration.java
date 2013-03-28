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
package piecework.config;

import java.util.Arrays;
import java.util.Collections;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.AuthenticationBuilder;
import org.springframework.security.config.annotation.web.EnableWebSecurity;
import org.springframework.security.config.annotation.web.ExpressionUrlAuthorizations;
import org.springframework.security.config.annotation.web.HttpConfiguration;
import org.springframework.security.config.annotation.web.WebSecurityConfigurerAdapater;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import piecework.authorization.AuthorizationRoleMapper;
import piecework.authorization.ResourceAccessVoter;
import piecework.security.AuthorityMappingPreAuthenticatedProvider;
import piecework.security.RequestParameterAuthenticationFilter;

/**
 * @author James Renfro
 */
@Configuration
//@EnableGlobalMethodSecurity(securedEnabled=true, jsr250Enabled=true)
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapater {
	
	private static final Logger LOG = Logger.getLogger(SecurityConfiguration.class);
	private enum AuthenticationType { NONE, PREAUTH, NORMAL }
	
	@Autowired
	AuthenticationProvider[] authenticationProviders;
	
	@Autowired
	UserDetailsService userDetailsService;
	
	@Value("${authentication.type}")
	String authenticationType;
	
	@Value("${authentication.testuser}")
	String testUser;
	
	@Bean
	public AccessDecisionManager resourceAccessDecisionManager() {
		@SuppressWarnings("rawtypes")
		AccessDecisionVoter voter = new ResourceAccessVoter();
		return new AffirmativeBased(Collections.singletonList(voter));
	}
	
//	@Bean
//    public HttpConfiguration httpConfiguration() throws Exception {
//		HttpConfiguration httpConfiguration = 
//        		new HttpConfiguration(authenticationManager());
//        httpConfiguration.setSharedObject(UserDetailsService.class, userDetailsService);
//        httpConfiguration.applyDefaultConfigurators();
//        httpConfiguration.authorizeUrls()
//	        .antMatchers("/static/**").permitAll()
//	        .antMatchers("/secure/**").authenticated();
//        
//        AuthenticationType type = authenticationType();
//        
//        switch (type) {
//        case NORMAL:
//        	httpConfiguration.formLogin()
//	        	.usernameParameter("j_username")
//	            .passwordParameter("j_password")
//	        	.loginProcessingUrl("/login")
//	        	.loginPage("/static/login.html")
//	        	.failureUrl("/static/login_error.html")
//	        	.defaultSuccessUrl("/", false)
//	        	.permitAll();
//        	break;
//        case NONE:
//        	httpConfiguration.addFilter(new RequestParameterAuthenticationFilter(authenticationManager()));
//        	break;
//        }
//        
//        return httpConfiguration;
//
////        .securityFilterChains(springSecurityFilterChain);
//////        result.ignoring(ignoredRequests());
//////        configure(result);
////        return result;
//    }
	
	@Bean
	public AuthenticationBuilder authenticationBuilder() {
		return new AuthenticationBuilder();
	}
	
	@Bean
    public AuthenticationManager authenticationManager(AuthenticationBuilder builder) throws Exception {
		switch (authenticationType()) {
		case NONE:
		case PREAUTH:
			AuthorityMappingPreAuthenticatedProvider provider = new AuthorityMappingPreAuthenticatedProvider();
			provider.setAuthoritiesMapper(new AuthorizationRoleMapper());
			provider.setPreAuthenticatedUserDetailsService(new UserDetailsByNameServiceWrapper<PreAuthenticatedAuthenticationToken>(userDetailsService));
			return new ProviderManager(Collections.singletonList(AuthenticationProvider.class.cast(provider)));
		}
		
		return new ProviderManager(Arrays.asList(authenticationProviders));
    }
	
	private AuthenticationType authenticationType() {
		AuthenticationType type = AuthenticationType.NORMAL;
		
		try {
			if (authenticationType != null && !authenticationType.equalsIgnoreCase("${authentication.type}")) 
				type = AuthenticationType.valueOf(authenticationType.toUpperCase());
		} catch (IllegalArgumentException iae) {
			LOG.warn("Authentication type: " + authenticationType.toUpperCase() + " is not valid");
		}
			
		if (LOG.isDebugEnabled())
			LOG.debug("Using authentication type " + type.toString());
		
		if (type == AuthenticationType.NONE)
			LOG.fatal("AUTHENTICATION HAS BEEN DISABLED!!! This should only be allowed for development and should never happen in production.");
		
		return type;
	}


	@Override
	protected void authorizeUrls(ExpressionUrlAuthorizations authorizations) {
		authorizations.antMatchers("/static/**").permitAll()
        	.antMatchers("/secure/**").authenticated();
	}

	@Override
	protected void configure(HttpConfiguration httpConfiguration)
			throws Exception {
		AuthenticationType type = authenticationType();

		switch (type) {
		case NORMAL:
			httpConfiguration.formLogin().usernameParameter("j_username")
					.passwordParameter("j_password")
					.loginProcessingUrl("/login")
					.loginPage("/static/login.html")
					.failureUrl("/static/login_error.html")
					.defaultSuccessUrl("/", false).permitAll();
			break;
		case NONE:
			httpConfiguration
					.addFilter(new RequestParameterAuthenticationFilter(authenticationManager(), testUser));
			break;
		}
	}
		
}
