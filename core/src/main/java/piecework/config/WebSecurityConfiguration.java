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

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.configuration.jsse.SSLUtils;
import org.apache.cxf.configuration.security.FiltersType;
import org.apache.log4j.Logger;
import org.owasp.validator.html.Policy;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.authentication.AuthenticationManagerFactoryBean;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import piecework.authorization.AuthorizationRoleMapper;
import piecework.authorization.ResourceAccessVoter;
import piecework.identity.AuthenticationPrincipalConverter;
import piecework.ldap.LdapSettings;
import piecework.security.*;
import piecework.security.concrete.AuthenticationFilterFactoryBean;
import piecework.security.concrete.CustomAuthenticationManagerFactoryBean;
import piecework.security.concrete.DebugAuthenticationFilter;
import piecework.security.concrete.SingleSignOnAuthenticationFilter;
import piecework.service.IdentityService;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author James Renfro
 */
@Configuration
@ImportResource("classpath:META-INF/piecework/spring-security-beans.xml")
public class WebSecurityConfiguration {

    private static final Logger LOG = Logger.getLogger(WebSecurityConfiguration.class);
    private enum AuthenticationType { NONE, PREAUTH, NORMAL }

    @Autowired(required = false)
    AuthenticationPrincipalConverter authenticationPrincipalConverter;

    @Autowired
    AuthenticationProvider[] authenticationProviders;

    @Autowired
    AuthorizationRoleMapper authorizationRoleMapper;

    @Autowired
    Environment environment;

    @Autowired
    UserDetailsService userDetailsService;

    @Bean(name="pieceworkAuthorizationRoleMapper")
    public AuthorizationRoleMapper authorizationRoleMapper() {
        return new AuthorizationRoleMapper();
    }

    @Bean(name="pieceworkAccessDecisionManager")
    public AccessDecisionManager resourceAccessDecisionManager() {
        @SuppressWarnings("rawtypes")
        AccessDecisionVoter voter = new ResourceAccessVoter();
        return new AffirmativeBased(Collections.singletonList(voter));
    }

//    @Bean //(name="org.springframework.security.authenticationManager")
//    public CustomAuthenticationManagerFactoryBean authenticationManagerFactoryBean() {
//        return new CustomAuthenticationManagerFactoryBean();
//    }

    @Bean(name="org.springframework.security.authenticationManager")
    public AuthenticationManager authenticationManager() throws Exception {
        switch (authenticationType()) {
            case NONE:
            case PREAUTH:
                List<AuthenticationProvider> providers = new ArrayList<AuthenticationProvider>();
                AuthorityMappingPreAuthenticatedProvider authorityMappingPreAuthenticatedProvider = new AuthorityMappingPreAuthenticatedProvider(authenticationPrincipalConverter);
                authorityMappingPreAuthenticatedProvider.setAuthoritiesMapper(authorizationRoleMapper);
                authorityMappingPreAuthenticatedProvider.setPreAuthenticatedUserDetailsService(new UserDetailsByNameServiceWrapper<PreAuthenticatedAuthenticationToken>(userDetailsService));

                providers.add(authorityMappingPreAuthenticatedProvider);

                AuthorityMappingAnonymousAuthenticationProvider authorityMappingAnonymousAuthenticationProvider = new AuthorityMappingAnonymousAuthenticationProvider();
                authorityMappingAnonymousAuthenticationProvider.setAuthoritiesMapper(authorizationRoleMapper);

                providers.add(authorityMappingAnonymousAuthenticationProvider);

                return new ProviderManager(providers);
        }

        return new ProviderManager(Arrays.asList(authenticationProviders));
    }

    private AuthenticationType authenticationType() {
        AuthenticationType type = AuthenticationType.NORMAL;

        String authenticationType = environment.getProperty("authentication.type");
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

    @Bean
    public Policy antisamyPolicy() throws Exception {
        ClassPathResource policyResource = new ClassPathResource("META-INF/piecework/antisamy-piecework-1.4.4.xml");
        URL policyUrl = policyResource.getURL();

        return Policy.getInstance(policyUrl);
    }

//    @Bean(name="pieceworkPreAuthFilter")
//    public AuthenticationFilterFactoryBean filterFactoryBean() throws Exception {
//        return new AuthenticationFilterFactoryBean(authenticationManager());
//    }

    @Bean(name="pieceworkPreAuthFilter")
    public AbstractPreAuthenticatedProcessingFilter pieceworkPreAuthFilter() throws Exception {
        String preauthenticationUserRequestHeader = environment.getProperty("preauthentication.user.request.header");
        String testUser = environment.getProperty("authentication.testuser");
        String testCredentials = environment.getProperty("authentication.testcredentials");
        Boolean isDebugMode = environment.getProperty("debug.mode", Boolean.class, Boolean.FALSE);

        if (isDebugMode) {
            LOG.fatal("DISABLING AUTHENTICATION -- THIS SHOULD NOT HAPPEN IN A PRODUCTION SYSTEM");

            DebugAuthenticationFilter debugAuthenticationFilter = new DebugAuthenticationFilter(authenticationManager(), testUser, testCredentials);
            if (StringUtils.isNotEmpty(preauthenticationUserRequestHeader))
                debugAuthenticationFilter.setPrincipalRequestHeader(preauthenticationUserRequestHeader);
            return debugAuthenticationFilter;
        }

        if (preauthenticationUserRequestHeader != null) {
            RequestHeaderAuthenticationFilter requestHeaderAuthenticationFilter = new RequestHeaderAuthenticationFilter();
            requestHeaderAuthenticationFilter.setPrincipalRequestHeader(preauthenticationUserRequestHeader);
            requestHeaderAuthenticationFilter.setAuthenticationManager(authenticationManager());
            return requestHeaderAuthenticationFilter;
        }

        SingleSignOnAuthenticationFilter singleSignOnAuthenticationFilter = new SingleSignOnAuthenticationFilter();
        singleSignOnAuthenticationFilter.setAuthenticationManager(authenticationManager());
        return singleSignOnAuthenticationFilter;
    }

}
