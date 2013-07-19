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

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.apache.cxf.configuration.jsse.SSLUtils;
import org.apache.cxf.configuration.security.FiltersType;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.ldap.authentication.DefaultValuesAuthenticationSourceDecorator;
import org.springframework.ldap.core.AuthenticationSource;
import org.springframework.ldap.core.support.DirContextAuthenticationStrategy;
import org.springframework.ldap.core.support.ExternalTlsDirContextAuthenticationStrategy;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.core.support.SimpleDirContextAuthenticationStrategy;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticator;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.authentication.PasswordComparisonAuthenticator;
import org.springframework.security.ldap.authentication.SpringSecurityAuthenticationSource;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.ldap.userdetails.LdapUserDetailsService;

import org.springframework.util.StringUtils;
import piecework.authorization.AuthorizationRoleMapper;
import piecework.identity.InternalUserDetailsService;
import piecework.ldap.CustomLdapUserDetailsMapper;
import piecework.security.CustomAuthenticationSource;
import piecework.util.KeyManagerCabinet;

/**
 * @author James Renfro
 */
@Configuration
@Profile("ldap")
public class LdapConfiguration {

	private static final Logger LOG = Logger.getLogger(LdapConfiguration.class);
    public static final List<String> CIPHER_SUITES_LIST = Arrays.asList("SSL_RSA_WITH_RC4_128_MD5", "SSL_RSA_WITH_RC4_128_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_DSS_WITH_AES_128_CBC_SHA", "SSL_RSA_WITH_3DES_EDE_CBC_SHA", "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA", "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA", "SSL_RSA_WITH_DES_CBC_SHA", "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA", "SSL_RSA_EXPORT_WITH_RC4_40_MD5", "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA", "TLS_EMPTY_RENEGOTIATION_INFO_SCSV", "TLS_KRB5_WITH_RC4_128_SHA", "TLS_KRB5_WITH_RC4_128_MD5", "TLS_KRB5_WITH_3DES_EDE_CBC_SHA", "TLS_KRB5_WITH_3DES_EDE_CBC_MD5", "TLS_KRB5_WITH_DES_CBC_SHA", "TLS_KRB5_WITH_DES_CBC_MD5", "TLS_KRB5_EXPORT_WITH_RC4_40_SHA", "TLS_KRB5_EXPORT_WITH_RC4_40_MD5", "TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA", "TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5" );
	public static int sslCacheTimeout = 86400000;
	
	private enum LdapAuthenticationEncryption { NONE, TLS, SSL }
	private enum LdapAuthenticationType { PASSWORDCOMPARE, BIND }

    @Autowired
    AuthorizationRoleMapper authorizationRoleMapper;

    @Autowired
    CustomLdapUserDetailsMapper userDetailsMapper;

	@Bean
	public InternalUserDetailsService userDetailsService(Environment environment) throws Exception {
        String ldapExternalIdAttribute = environment.getProperty("ldap.attribute.id.external");

		return new InternalUserDetailsService(userSearch(environment), userSearchInternal(environment), authoritiesPopulator(environment), userDetailsMapper, ldapExternalIdAttribute);
	}

    @Bean
    public CustomLdapUserDetailsMapper userDetailsMapper(Environment environment) throws Exception {
        return new CustomLdapUserDetailsMapper(new LdapUserDetailsMapper(), environment);
    }

	@Bean
	public LdapContextSource groupLdapContextSource(Environment environment) throws Exception {
        String ldapGroupUrl = environment.getProperty("ldap.group.url");
        String ldapGroupBase = environment.getProperty("ldap.group.base");
        String ldapPersonDn = environment.getProperty("ldap.person.dn");

		LdapContextSource context = new LdapContextSource();
		context.setUrl(ldapGroupUrl);
		context.setBase(ldapGroupBase);
        context.setUserDn(ldapPersonDn);
		context.setAuthenticationSource(authenticationSource(environment));
		context.setAuthenticationStrategy(authenticationStrategy(environment));

		return context;
	}
	
	@Bean(name="personLdapContextSource")
	public LdapContextSource personLdapContextSource(Environment environment) throws Exception {
        String ldapPersonUrl = environment.getProperty("ldap.person.url");
        String ldapPersonBase = environment.getProperty("ldap.person.base");
        String ldapPersonDn = environment.getProperty("ldap.person.dn");

		LdapContextSource context = new LdapContextSource();
		context.setUrl(ldapPersonUrl);
		context.setBase(ldapPersonBase);
		context.setUserDn(ldapPersonDn);
		context.setAuthenticationSource(authenticationSource(environment));
		context.setAuthenticationStrategy(authenticationStrategy(environment));

		return context;
	}

    @Bean(name="pieceworkAuthorizationRoleMapper")
    public AuthorizationRoleMapper authorizationRoleMapper() {
        return new AuthorizationRoleMapper();
    }

	@Bean
	public AuthenticationProvider authenticationProvider(Environment environment) throws Exception {
		LdapAuthenticationProvider provider = new LdapAuthenticationProvider(authenticator(environment), authoritiesPopulator(environment));
		provider.setAuthoritiesMapper(authorizationRoleMapper);
		return provider;
	}

    @Bean
    public KeyManagerCabinet keyManagerCabinet(Environment environment) throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        String keystoreFile = environment.getProperty("keystore.file");
        String keystorePassword = environment.getProperty("keystore.password");

        return new KeyManagerCabinet.Builder(keystoreFile, keystorePassword).build();
    }
	
	private LdapAuthenticator authenticator(Environment environment) throws Exception {
		LdapContextSource context = personLdapContextSource(environment);
		
		AbstractLdapAuthenticator authenticator = null;
		
		LdapAuthenticationType type = authenticationType(environment);
		
		switch (type) {
		case PASSWORDCOMPARE:
			authenticator = new PasswordComparisonAuthenticator(context);
			break;
		default:
			authenticator = new BindAuthenticator(context);
		}

		authenticator.setUserSearch(userSearch(environment));
		
		return authenticator;
	}
	
	private LdapAuthenticationEncryption authenticationEncryption(Environment environment) {
        String ldapAuthenticationEncryption = environment.getProperty("ldap.authentication.encryption");

		LdapAuthenticationEncryption encryption = LdapAuthenticationEncryption.NONE;
		
		try {
			if (ldapAuthenticationEncryption != null && !ldapAuthenticationEncryption.equalsIgnoreCase("${ldap.authentication.encryption}")) 
				encryption = LdapAuthenticationEncryption.valueOf(ldapAuthenticationEncryption.toUpperCase());
		} catch (IllegalArgumentException iae) {
			LOG.warn("Authentication encryption: " + ldapAuthenticationEncryption.toUpperCase() + " is not valid");
		}
		
		if (LOG.isDebugEnabled())
			LOG.debug("Using ldap authentication encryption " + encryption.toString());
		
		return encryption;
	}
	
	private AuthenticationSource authenticationSource(Environment environment) {
        String defaultUser = environment.getProperty("ldap.authentication.user");
        String defaultPassword = environment.getProperty("ldap.authentication.password");
        CustomAuthenticationSource authenticationSource = new CustomAuthenticationSource();

        LdapAuthenticationEncryption encryption = authenticationEncryption(environment);

        if (encryption == LdapAuthenticationEncryption.TLS)
            return new DefaultValuesAuthenticationSourceDecorator(authenticationSource, defaultUser, defaultPassword);

        return authenticationSource;
	}
	
	private DirContextAuthenticationStrategy authenticationStrategy(Environment environment) throws Exception {
		LdapAuthenticationEncryption encryption = authenticationEncryption(environment);
		
		DirContextAuthenticationStrategy strategy = null;
		
		switch (encryption) {
		case TLS:
			strategy = new ExternalTlsDirContextAuthenticationStrategy();
			((ExternalTlsDirContextAuthenticationStrategy)strategy).setSslSocketFactory(sslSocketFactory(environment));
			break;
		default:
			strategy = new SimpleDirContextAuthenticationStrategy();
		}
		
		return strategy;
	}
	
	private LdapAuthenticationType authenticationType(Environment environment) {
        String ldapAuthenticationType = environment.getProperty("ldap.authentication.type");

		LdapAuthenticationType type = LdapAuthenticationType.BIND;
		
		try {
			if (ldapAuthenticationType != null && !ldapAuthenticationType.equalsIgnoreCase("${ldap.authentication.type}")) 
				type = LdapAuthenticationType.valueOf(ldapAuthenticationType.toUpperCase());
		} catch (IllegalArgumentException iae) {
			LOG.warn("Authentication type: " + ldapAuthenticationType.toUpperCase() + " is not valid");
		}
			
		if (LOG.isDebugEnabled())
			LOG.debug("Using ldap authentication type " + type.toString());
		
		return type;
	}
	
	private LdapAuthoritiesPopulator authoritiesPopulator(Environment environment) throws Exception {
        String ldapGroupSearchBase = environment.getProperty("ldap.group.search.base");
        String ldapGroupSearchFilter  = environment.getProperty("ldap.group.search.filter");

		DefaultLdapAuthoritiesPopulator authoritiesPopulator = new DefaultLdapAuthoritiesPopulator(groupLdapContextSource(environment), ldapGroupSearchBase);
		authoritiesPopulator.setGroupSearchFilter(ldapGroupSearchFilter);
		return authoritiesPopulator;
	}
	
	private SSLSocketFactory sslSocketFactory(Environment environment) throws NoSuchAlgorithmException, NoSuchProviderException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {
        KeyManagerCabinet cabinet = keyManagerCabinet(environment);
		String provider = null;
        String protocol = "TLS";

        SSLContext ctx = provider == null ? SSLContext.getInstance(protocol) : SSLContext
            .getInstance(protocol, provider);
        
        ctx.getClientSessionContext().setSessionTimeout(sslCacheTimeout);
        ctx.init(cabinet.getKeyManagers(), cabinet.getTrustManagers(), null);

        FiltersType filter = new FiltersType();
        String[] cs = SSLUtils.getCiphersuites(CIPHER_SUITES_LIST, SSLUtils.getSupportedCipherSuites(ctx), filter, java.util.logging.Logger.getLogger(this.getClass().getCanonicalName()), false);

        return new piecework.util.SSLSocketFactoryWrapper(ctx.getSocketFactory(), cs, protocol);
	}
	
	private LdapUserSearch userSearch(Environment environment) throws Exception {
        String ldapPersonSearchBase = environment.getProperty("ldap.person.search.base");
        String ldapPersonSearchFilter = environment.getProperty("ldap.person.search.filter");

		LdapUserSearch userSearch = new FilterBasedLdapUserSearch(ldapPersonSearchBase, ldapPersonSearchFilter, personLdapContextSource(environment));
		((FilterBasedLdapUserSearch)userSearch).setReturningAttributes(null);
		((FilterBasedLdapUserSearch)userSearch).setSearchSubtree(true);
		((FilterBasedLdapUserSearch)userSearch).setSearchTimeLimit(10000);
		return userSearch;
	}

    private LdapUserSearch userSearchInternal(Environment environment) throws Exception {
        String ldapPersonSearchBase = environment.getProperty("ldap.person.search.base");
        String ldapPersonSearchFilter = environment.getProperty("ldap.person.search.filter.internal");

        // Fallback to original setting if an internal setting is not defined
        if (StringUtils.isEmpty(ldapPersonSearchFilter))
            ldapPersonSearchFilter = environment.getProperty("ldap.person.search.filter");

        LdapUserSearch userSearch = new FilterBasedLdapUserSearch(ldapPersonSearchBase, ldapPersonSearchFilter, personLdapContextSource(environment));
        ((FilterBasedLdapUserSearch)userSearch).setReturningAttributes(null);
        ((FilterBasedLdapUserSearch)userSearch).setSearchSubtree(true);
        ((FilterBasedLdapUserSearch)userSearch).setSearchTimeLimit(10000);
        return userSearch;
    }

}
