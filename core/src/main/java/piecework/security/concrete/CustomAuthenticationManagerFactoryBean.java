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
package piecework.security.concrete;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import piecework.authorization.AuthorizationRoleMapper;
import piecework.security.AuthorityMappingAnonymousAuthenticationProvider;
import piecework.security.AuthorityMappingPreAuthenticatedProvider;
import piecework.service.IdentityService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author James Renfro
 */
@Deprecated
public class CustomAuthenticationManagerFactoryBean implements FactoryBean<AuthenticationManager> {

    private static final Logger LOG = Logger.getLogger(CustomAuthenticationManagerFactoryBean.class);
    private enum AuthenticationType { NONE, PREAUTH, NORMAL }

    @Autowired
    AuthenticationProvider[] authenticationProviders;

    @Autowired
    AuthorizationRoleMapper authorizationRoleMapper;

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    Environment environment;

    @Autowired
    IdentityService identityService;

    @Override
    public AuthenticationManager getObject() throws Exception {
        switch (authenticationType()) {
            case NONE:
            case PREAUTH:
                List<AuthenticationProvider> providers = new ArrayList<AuthenticationProvider>();
                AuthorityMappingPreAuthenticatedProvider authorityMappingPreAuthenticatedProvider = new AuthorityMappingPreAuthenticatedProvider();
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

    @Override
    public Class<?> getObjectType() {
        return AuthenticationManager.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
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
}
