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
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.apache.cxf.Bus;
import org.apache.cxf.configuration.jsse.SSLUtils;
import org.apache.cxf.configuration.security.FiltersType;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
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

import piecework.authorization.AuthorizationRoleMapper;
import piecework.ldap.CustomLdapUserDetailsMapper;
import piecework.util.KeyManagerCabinet;

/**
 * @author James Renfro
 */
@Configuration
@Profile("ldap")
public class LdapConfiguration {

	private static final Logger LOG = Logger.getLogger(LdapConfiguration.class);
	private static final String[] CIPHER_SUITES = { "SSL_RSA_WITH_RC4_128_MD5", "SSL_RSA_WITH_RC4_128_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_DSS_WITH_AES_128_CBC_SHA", "SSL_RSA_WITH_3DES_EDE_CBC_SHA", "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA", "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA", "SSL_RSA_WITH_DES_CBC_SHA", "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA", "SSL_RSA_EXPORT_WITH_RC4_40_MD5", "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA", "TLS_EMPTY_RENEGOTIATION_INFO_SCSV", "TLS_KRB5_WITH_RC4_128_SHA", "TLS_KRB5_WITH_RC4_128_MD5", "TLS_KRB5_WITH_3DES_EDE_CBC_SHA", "TLS_KRB5_WITH_3DES_EDE_CBC_MD5", "TLS_KRB5_WITH_DES_CBC_SHA", "TLS_KRB5_WITH_DES_CBC_MD5", "TLS_KRB5_EXPORT_WITH_RC4_40_SHA", "TLS_KRB5_EXPORT_WITH_RC4_40_MD5", "TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA", "TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5" };
	private static final List<String> CIPHER_SUITES_LIST;
	private static int sslCacheTimeout = 86400000;
	
	private enum LdapAuthenticationEncryption { NONE, TLS, SSL }
	private enum LdapAuthenticationType { PASSWORDCOMPARE, BIND }
	
	static {
		CIPHER_SUITES_LIST = Arrays.asList(CIPHER_SUITES);
	}

	@Autowired 
	Environment env;
	
	@Autowired
	Bus cxf;
	
	@Value("${ldap.authentication.encryption}")
	String ldapAuthenticationEncryption;
	
	@Value("${ldap.authentication.type}")
	String ldapAuthenticationType;
	
	@Value("${ldap.person.url}")
	String ldapPersonUrl;
	
	@Value("${ldap.person.base}")
	String ldapPersonBase;
	
	@Value("${ldap.person.search.base}")
	String ldapPersonSearchBase;
	
	@Value("${ldap.person.search.filter}")
	String ldapPersonSearchFilter;
	
	@Value("${ldap.person.dn}")
	String ldapPersonDn;
	
	@Value("${ldap.group.url}")
	String ldapGroupUrl;
	
	@Value("${ldap.group.base}")
	String ldapGroupBase;
	
	@Value("${ldap.group.search.base}")
	String ldapGroupSearchBase;
	
	@Value("${ldap.group.search.filter}")
	String ldapGroupSearchFilter;
	
	@Value("${keystore.file}")
	String keystoreFile;
	
	@Value("${keystore.password}")
	String keystorePassword;
		
	@Bean
	public UserDetailsService userDetailsService() throws Exception {		
		LdapUserDetailsService userDetailsService = new LdapUserDetailsService(userSearch(), authoritiesPopulator());
		userDetailsService.setUserDetailsMapper(new CustomLdapUserDetailsMapper(new LdapUserDetailsMapper()));
		return userDetailsService;
	}
	
	@Bean
	public LdapContextSource groupLdapContextSource() throws Exception {
		// TODO: Should we be using DefaultSpringSecurityContextSource? 
		LdapContextSource context = new LdapContextSource();
		context.setUrl(ldapGroupUrl);
		context.setBase(ldapGroupBase);
		context.setAuthenticationSource(authenticationSource());
		context.setAuthenticationStrategy(authenticationStrategy());
		context.setAnonymousReadOnly(true);
		return context;
	}
	
	@Bean 
	public LdapContextSource personLdapContextSource() throws Exception {
		// TODO: Should we be using DefaultSpringSecurityContextSource? 
		LdapContextSource context = new LdapContextSource();
		context.setUrl(ldapPersonUrl);
		context.setBase(ldapPersonBase);
		context.setUserDn(ldapPersonDn);
		context.setAuthenticationSource(authenticationSource());
		context.setAuthenticationStrategy(authenticationStrategy());
		context.setAnonymousReadOnly(true);
		return context;
	}

    @Bean
    public AuthorizationRoleMapper authorizationRoleMapper() {
        return new AuthorizationRoleMapper();
    }

	@Bean
	public AuthenticationProvider authenticationProvider() throws Exception {
		LdapAuthenticationProvider provider = new LdapAuthenticationProvider(authenticator(), authoritiesPopulator());
		provider.setAuthoritiesMapper(authorizationRoleMapper());
		return provider;
	}
	
	private LdapAuthenticator authenticator() throws Exception {
		LdapContextSource context = personLdapContextSource();
		
		AbstractLdapAuthenticator authenticator = null;
		
		LdapAuthenticationType type = authenticationType();
		
		switch (type) {
		case PASSWORDCOMPARE:
			authenticator = new PasswordComparisonAuthenticator(context);
			break;
		default:
			authenticator = new BindAuthenticator(context);
		}

		authenticator.setUserSearch(userSearch());
		
		return authenticator;
	}
	
	private LdapAuthenticationEncryption authenticationEncryption() {
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
	
	private AuthenticationSource authenticationSource() {
		SpringSecurityAuthenticationSource authenticationSource = new SpringSecurityAuthenticationSource();
		return authenticationSource;
	}
	
	private DirContextAuthenticationStrategy authenticationStrategy() throws Exception {
		LdapAuthenticationEncryption encryption = authenticationEncryption();
		
		DirContextAuthenticationStrategy strategy = null;
		
		switch (encryption) {
		case TLS:
			strategy = new ExternalTlsDirContextAuthenticationStrategy();
			((ExternalTlsDirContextAuthenticationStrategy)strategy).setSslSocketFactory(sslSocketFactory());
			break;
		default:
			strategy = new SimpleDirContextAuthenticationStrategy();
		}
		
		return strategy;
	}
	
	private LdapAuthenticationType authenticationType() {
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
	
	private LdapAuthoritiesPopulator authoritiesPopulator() throws Exception {
		DefaultLdapAuthoritiesPopulator authoritiesPopulator = new DefaultLdapAuthoritiesPopulator(groupLdapContextSource(), ldapGroupSearchBase);
		authoritiesPopulator.setGroupSearchFilter(ldapGroupSearchFilter);
		return authoritiesPopulator;
	}
	
	private SSLSocketFactory sslSocketFactory() throws NoSuchAlgorithmException, NoSuchProviderException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {
		KeyManagerCabinet cabinet = new KeyManagerCabinet.Builder(keystoreFile, keystorePassword).build();
		String provider = null;
        String protocol = "TLS";

        SSLContext ctx = provider == null ? SSLContext.getInstance(protocol) : SSLContext
            .getInstance(protocol, provider);
        
        ctx.getClientSessionContext().setSessionTimeout(sslCacheTimeout);
        ctx.init(cabinet.getKeyManagers(), cabinet.getTrustManagers(), null);

        FiltersType filter = new FiltersType();
        String[] cs = SSLUtils.getCiphersuites(CIPHER_SUITES_LIST, SSLUtils.getSupportedCipherSuites(ctx), filter, null, false);

        return new piecework.util.SSLSocketFactoryWrapper(ctx.getSocketFactory(), cs, protocol);
	}
	
	private LdapUserSearch userSearch() throws Exception {
		LdapUserSearch userSearch = new FilterBasedLdapUserSearch(ldapPersonSearchBase, ldapPersonSearchFilter, personLdapContextSource());
		((FilterBasedLdapUserSearch)userSearch).setReturningAttributes(null);
		((FilterBasedLdapUserSearch)userSearch).setSearchSubtree(true);
		((FilterBasedLdapUserSearch)userSearch).setSearchTimeLimit(10000);
		return userSearch;
	}

}
