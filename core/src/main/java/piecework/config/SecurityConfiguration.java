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
package piecework.config;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;

import org.apache.log4j.Logger;
import org.owasp.validator.html.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.DefaultLoginPageConfigurer;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import piecework.authorization.AuthorizationRole;
import piecework.authorization.AuthorizationRoleMapper;
import piecework.authorization.ResourceAccessVoter;
import piecework.security.AuthorityMappingPreAuthenticatedProvider;
import piecework.security.RequestParameterAuthenticationFilter;

/**
 * @author James Renfro
 */
//@Configuration
//@EnableWebSecurity
@Profile("disabled")
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	
//	private static final Logger LOG = Logger.getLogger(SecurityConfiguration.class);
//	private enum AuthenticationType { NONE, PREAUTH, NORMAL }
//
//	@Autowired
//	AuthenticationProvider[] authenticationProviders;
//
//    @Autowired
//    AuthorizationRoleMapper authorizationRoleMapper;
//
//    @Autowired
//    Environment environment;
//
//	@Autowired
//	UserDetailsService userDetailsService;
//
//	@Value("${authentication.type}")
//	String authenticationType;
//
//	@Value("${authentication.testuser}")
//	String testUser;
//
//	@Bean
//	public AccessDecisionManager resourceAccessDecisionManager() {
//		@SuppressWarnings("rawtypes")
//		AccessDecisionVoter voter = new ResourceAccessVoter();
//		return new AffirmativeBased(Collections.singletonList(voter));
//	}
//
//	@Bean
//    public AuthenticationManager authenticationManager() throws Exception {
//		switch (authenticationType()) {
//		case NONE:
//		case PREAUTH:
//			AuthorityMappingPreAuthenticatedProvider provider = new AuthorityMappingPreAuthenticatedProvider();
//			provider.setAuthoritiesMapper(authorizationRoleMapper);
//			provider.setPreAuthenticatedUserDetailsService(new UserDetailsByNameServiceWrapper<PreAuthenticatedAuthenticationToken>(userDetailsService));
//			return new ProviderManager(Collections.singletonList(AuthenticationProvider.class.cast(provider)));
//		}
//
//		return new ProviderManager(Arrays.asList(authenticationProviders));
//    }
//
//	private AuthenticationType authenticationType() {
//		AuthenticationType type = AuthenticationType.NORMAL;
//
//		try {
//			if (authenticationType != null && !authenticationType.equalsIgnoreCase("${authentication.type}"))
//				type = AuthenticationType.valueOf(authenticationType.toUpperCase());
//		} catch (IllegalArgumentException iae) {
//			LOG.warn("Authentication type: " + authenticationType.toUpperCase() + " is not valid");
//		}
//
//		if (LOG.isDebugEnabled())
//			LOG.debug("Using authentication type " + type.toString());
//
//		if (type == AuthenticationType.NONE)
//			LOG.fatal("AUTHENTICATION HAS BEEN DISABLED!!! This should only be allowed for development and should never happen in production.");
//
//		return type;
//	}
//
//	@Override
//    public void configure(WebSecurity web) throws Exception {
//        web
//            .ignoring()
//                .antMatchers("/static/**");
//
//    }
//
//	@Override
//	public void configure(HttpSecurity http) throws Exception {
//		AuthenticationType type = authenticationType();
//
////        http
////            .authorizeUrls()
////            .antMatchers("/api/**").authenticated() //.hasRole("SYSTEM")
////            .and()
////            .x509();
//
//		http
//			.authorizeUrls()
//			.antMatchers("/static/**").permitAll()
//            .antMatchers("/public/**").permitAll()
//        	.antMatchers("/secure/**").authenticated();
//
//		switch (type) {
//		case NORMAL:
//			http.formLogin().usernameParameter("j_username")
//					.passwordParameter("j_password")
//					.loginProcessingUrl("/login")
//					.loginPage("/static/login.html")
//					.failureUrl("/static/login_error.html")
//					.defaultSuccessUrl("/", false).permitAll();
//			break;
//        case PREAUTH:
//            String credentialsRequestHeader = environment.getProperty("preauthentication.user.request.header");
//            RequestHeaderAuthenticationFilter authenticationFilter = new RequestHeaderAuthenticationFilter();
//            authenticationFilter.setAuthenticationManager(authenticationManager());
//            authenticationFilter.setPrincipalRequestHeader(credentialsRequestHeader);
//            http.addFilter(authenticationFilter);
//            break;
//		case NONE:
//			http.addFilter(new RequestParameterAuthenticationFilter(authenticationManager(), testUser));
//			break;
//		}
//	}
//
//    @Bean
//    public Policy antisamyPolicy() throws Exception {
//        ClassPathResource policyResource = new ClassPathResource("META-INF/piecework/antisamy-1.4.3.xml");
//        URL policyUrl = policyResource.getURL();
//
//        return Policy.getInstance(policyUrl);
//    }
		
}
