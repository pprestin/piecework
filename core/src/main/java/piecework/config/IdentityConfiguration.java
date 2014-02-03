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

import org.apache.cxf.configuration.jsse.SSLUtils;
import org.apache.cxf.configuration.security.FiltersType;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import piecework.ServiceLocator;
import piecework.identity.*;
import piecework.ldap.*;
import piecework.security.KeyManagerCabinet;
import piecework.security.SSLSocketFactoryWrapper;
import piecework.settings.SecuritySettings;
import piecework.service.CacheService;
import piecework.service.IdentityService;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;

/**
 * @author James Renfro
 */
@Configuration
public class IdentityConfiguration {

    private static final Logger LOG = Logger.getLogger(IdentityConfiguration.class);

    @Autowired(required = false)
    AuthenticationPrincipalConverter authenticationPrincipalConverter;

    @Autowired
    CacheService cacheService;

    @Autowired(required=false)
    DisplayNameConverter displayNameConverter;

    @Autowired
    ServiceLocator serviceLocator;

    @Bean
    public LdapSettings ldapSettings(Environment environment) {
        return new LdapSettings(environment);
    }

    @Bean
    public GroupServiceFactoryBean groupServiceFactoryBean() {
        return new GroupServiceFactoryBean();
    }

    @Bean
    public IdentityService identityService(Environment environment) throws Exception {
        Boolean isDebugMode = environment.getProperty("debug.mode", Boolean.class, Boolean.FALSE);
        Boolean isDebugIdentity = environment.getProperty("debug.identity", Boolean.class, Boolean.FALSE);

        if (isDebugIdentity) {
            DebugUserDetailsService debugUserDetailsService = new DebugUserDetailsService(environment, serviceLocator);
            debugUserDetailsService.init();
            return new DebugIdentityService(debugUserDetailsService);
        }

        SSLSocketFactory sslSocketFactory = sslSocketFactory(environment);
        String identityProviderProtocol = environment.getProperty("identity.provider.protocol");
        LdapSettings ldapSettings = ldapSettings(environment);
        LdapContextSource contextSource = LdapUtility.personLdapContextSource(ldapSettings, sslSocketFactory);
        LdapUserSearch userSearch = LdapUtility.userSearch(contextSource, ldapSettings);
        LdapAuthoritiesPopulator authoritiesPopulator = LdapUtility.authoritiesPopulator(ldapSettings, sslSocketFactory);
        CustomLdapUserDetailsMapper userDetailsMapper = new CustomLdapUserDetailsMapper(new LdapUserDetailsMapper(), displayNameConverter, ldapSettings);
        return new LdapIdentityService(userDetailsService(environment), cacheService, contextSource, userSearch, authoritiesPopulator, userDetailsMapper, ldapSettings);
    }

    @Bean
    public UserDetailsService userDetailsService(Environment environment) throws Exception {
        Boolean isDebugMode = environment.getProperty("debug.mode", Boolean.class, Boolean.FALSE);
        Boolean isDebugIdentity = environment.getProperty("debug.identity", Boolean.class, Boolean.FALSE);

        if (isDebugIdentity) {
            DebugUserDetailsService userDetailsService = new DebugUserDetailsService(environment, serviceLocator);
            userDetailsService.init();
            return userDetailsService;
        }
        String identityProviderProtocol = environment.getProperty("identity.provider.protocol");

        SSLSocketFactory sslSocketFactory = sslSocketFactory(environment);
        LdapSettings ldapSettings = ldapSettings(environment);
        LdapContextSource contextSource = LdapUtility.personLdapContextSource(ldapSettings, sslSocketFactory);
        LdapUserSearch userSearch = LdapUtility.userSearch(contextSource, ldapSettings);
        LdapAuthoritiesPopulator authoritiesPopulator = LdapUtility.authoritiesPopulator(ldapSettings, sslSocketFactory);
        CustomLdapUserDetailsMapper userDetailsMapper = new CustomLdapUserDetailsMapper(new LdapUserDetailsMapper(), displayNameConverter, ldapSettings);
        return new CustomLdapUserDetailsService(authenticationPrincipalConverter, cacheService, userSearch, authoritiesPopulator, userDetailsMapper);
    }


    private static final List<String> CIPHER_SUITES_LIST = Arrays.asList("SSL_RSA_WITH_RC4_128_MD5", "SSL_RSA_WITH_RC4_128_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_DSS_WITH_AES_128_CBC_SHA", "SSL_RSA_WITH_3DES_EDE_CBC_SHA", "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA", "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA", "SSL_RSA_WITH_DES_CBC_SHA", "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA", "SSL_RSA_EXPORT_WITH_RC4_40_MD5", "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA", "TLS_EMPTY_RENEGOTIATION_INFO_SCSV", "TLS_KRB5_WITH_RC4_128_SHA", "TLS_KRB5_WITH_RC4_128_MD5", "TLS_KRB5_WITH_3DES_EDE_CBC_SHA", "TLS_KRB5_WITH_3DES_EDE_CBC_MD5", "TLS_KRB5_WITH_DES_CBC_SHA", "TLS_KRB5_WITH_DES_CBC_MD5", "TLS_KRB5_EXPORT_WITH_RC4_40_SHA", "TLS_KRB5_EXPORT_WITH_RC4_40_MD5", "TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA", "TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5");
    private static int SSL_CACHE_TIMEOUT = 86400000;

    @Bean
    public SSLSocketFactory sslSocketFactory(Environment environment) throws Exception {
        KeyManagerCabinet cabinet = keyManagerCabinet(environment);
        String provider = null;
        String protocol = "TLS";

        SSLContext ctx = provider == null ? SSLContext.getInstance(protocol) : SSLContext
                .getInstance(protocol, provider);

        ctx.getClientSessionContext().setSessionTimeout(SSL_CACHE_TIMEOUT);
        ctx.init(cabinet.getKeyManagers(), cabinet.getTrustManagers(), null);

        FiltersType filter = new FiltersType();
        String[] cs = SSLUtils.getCiphersuites(CIPHER_SUITES_LIST, SSLUtils.getSupportedCipherSuites(ctx), filter, java.util.logging.Logger.getLogger(this.getClass().getCanonicalName()), false);

        return new SSLSocketFactoryWrapper(ctx.getSocketFactory(), cs, protocol);
    }

    @Bean
    public KeyManagerCabinet keyManagerCabinet(Environment environment) throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        SecuritySettings securitySettings = securitySettings(environment);
        return keyManagerCabinet(securitySettings);
    }

    @Bean
    public SecuritySettings securitySettings(Environment environment) {
        return new SecuritySettings(environment);
    }

    private KeyManagerCabinet keyManagerCabinet(SecuritySettings securitySettings) throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {

        try {
            return new KeyManagerCabinet.Builder(securitySettings).build();
        } catch (FileNotFoundException e) {
            LOG.error("Could not createDeployment key manager cabinet because keystore file not found");
        }

        return null;
    }

}
