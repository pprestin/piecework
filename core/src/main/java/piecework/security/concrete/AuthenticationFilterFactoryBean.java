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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;

/**
 * @author James Renfro
 */
public class AuthenticationFilterFactoryBean implements FactoryBean<AbstractPreAuthenticatedProcessingFilter> {
    private enum AuthenticationType { NONE, PREAUTH, NORMAL };
    private static final Logger LOG = Logger.getLogger(AuthenticationFilterFactoryBean.class);

    //@Autowired
    private final AuthenticationManager authenticationManager;

    @Autowired
    Environment environment;

    public AuthenticationFilterFactoryBean(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public AbstractPreAuthenticatedProcessingFilter getObject() throws Exception {
        String preauthenticationUserRequestHeader = environment.getProperty("preauthentication.user.request.header");
        String testUser = environment.getProperty("authentication.testuser");
        String testCredentials = environment.getProperty("authentication.testcredentials");
        Boolean isDebugMode = environment.getProperty("debug.mode", Boolean.class, Boolean.FALSE);

        if (isDebugMode) {
            LOG.fatal("DISABLING AUTHENTICATION -- THIS SHOULD NOT HAPPEN IN A PRODUCTION SYSTEM");

            DebugAuthenticationFilter debugAuthenticationFilter = new DebugAuthenticationFilter(authenticationManager, testUser, testCredentials);
            if (StringUtils.isNotEmpty(preauthenticationUserRequestHeader))
                debugAuthenticationFilter.setPrincipalRequestHeader(preauthenticationUserRequestHeader);
            return debugAuthenticationFilter;
        }

        if (preauthenticationUserRequestHeader != null) {
            RequestHeaderAuthenticationFilter requestHeaderAuthenticationFilter = new RequestHeaderAuthenticationFilter();
            requestHeaderAuthenticationFilter.setPrincipalRequestHeader(preauthenticationUserRequestHeader);
            requestHeaderAuthenticationFilter.setAuthenticationManager(authenticationManager);
            return requestHeaderAuthenticationFilter;
        }

        if (authenticationType() == AuthenticationType.PREAUTH) {
            SingleSignOnAuthenticationFilter singleSignOnAuthenticationFilter = new SingleSignOnAuthenticationFilter();
            singleSignOnAuthenticationFilter.setAuthenticationManager(authenticationManager);
            return singleSignOnAuthenticationFilter;
        }
        return null;
    }

    @Override
    public Class<?> getObjectType() {
        return AbstractPreAuthenticatedProcessingFilter.class;
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
