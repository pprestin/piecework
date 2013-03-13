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

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.apache.cxf.configuration.jsse.SSLUtils;
import org.apache.cxf.configuration.security.FiltersType;
import org.apache.cxf.interceptor.security.SecureAnnotationsInterceptor;
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
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.PasswordComparisonAuthenticator;
import org.springframework.security.ldap.authentication.SpringSecurityAuthenticationSource;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;

import piecework.authentication.AuthenticationHandler;
import piecework.util.KeyManagerCabinet;

/**
 * @author James Renfro
 */
@Configuration
@Profile("ldap")
public class AuthenticationConfigurationForLdap {

	private static final java.util.logging.Logger LOG = Logger.getLogger(AuthenticationConfigurationForLdap.class.getName());
	private static final String[] CIPHER_SUITES = { "SSL_RSA_WITH_RC4_128_MD5", "SSL_RSA_WITH_RC4_128_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_DSS_WITH_AES_128_CBC_SHA", "SSL_RSA_WITH_3DES_EDE_CBC_SHA", "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA", "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA", "SSL_RSA_WITH_DES_CBC_SHA", "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA", "SSL_RSA_EXPORT_WITH_RC4_40_MD5", "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA", "TLS_EMPTY_RENEGOTIATION_INFO_SCSV", "TLS_KRB5_WITH_RC4_128_SHA", "TLS_KRB5_WITH_RC4_128_MD5", "TLS_KRB5_WITH_3DES_EDE_CBC_SHA", "TLS_KRB5_WITH_3DES_EDE_CBC_MD5", "TLS_KRB5_WITH_DES_CBC_SHA", "TLS_KRB5_WITH_DES_CBC_MD5", "TLS_KRB5_EXPORT_WITH_RC4_40_SHA", "TLS_KRB5_EXPORT_WITH_RC4_40_MD5", "TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA", "TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5" };
	private static final List<String> CIPHER_SUITES_LIST;
	private static int sslCacheTimeout = 86400000;
	
	static {
		CIPHER_SUITES_LIST = Arrays.asList(CIPHER_SUITES);
	}

	@Autowired 
	Environment env;
	
	@Value("${ldap.url}")
	String ldapUrl;
	
	@Value("${ldap.base}")
	String ldapBase;
	
	@Value("${ldap.userdn}")
	String ldapUserDn;
	
	@Value("${ldap.groupbase}")
	String ldapGroupBase;
	
	@Value("${keystore.file}")
	String keystoreFile;
	
	@Value("${keystore.password}")
	String keystorePassword;
	
	@Bean
	public AuthenticationHandler authenticationHandler() throws Exception {
		AuthenticationProvider[] authenticationProviders = new AuthenticationProvider[1]; 
		authenticationProviders[0] = authenticationProvider();
		
		AuthenticationHandler authenticationHandler = new LdapAuthenticationHandler(authenticationProviders, new SecureAnnotationsInterceptor());
	
		if (env.acceptsProfiles("dev")) 
			return new AuthenticationHandlerForDevelopment(authenticationHandler);
		
		return authenticationHandler;
	}
	
	@Bean 
	public LdapContextSource ldapContextSource() throws Exception {
		LdapContextSource context = new LdapContextSource();
		context.setUrl(ldapUrl);
		context.setBase(ldapBase);
		context.setUserDn(ldapUserDn);
		context.setAuthenticationSource(authenticationSource());
		context.setAuthenticationStrategy(authenticationStrategy());
		return context;
	}
	
	@Bean
	public AuthenticationProvider authenticationProvider() throws Exception {
		LdapContextSource context = ldapContextSource();
		
		AbstractLdapAuthenticator authenticator = null;
		if (env.acceptsProfiles("sso")) 
			authenticator = new PreauthenticatedLdapAuthenticator(context);
		else
			authenticator = new PasswordComparisonAuthenticator(context);
		
		return new LdapAuthenticationProvider(authenticator, new DefaultLdapAuthoritiesPopulator(context, ldapGroupBase));
	}
	
	public AuthenticationSource authenticationSource() {
		SpringSecurityAuthenticationSource authenticationSource = new SpringSecurityAuthenticationSource();
		return authenticationSource;
	}
	
	public DirContextAuthenticationStrategy authenticationStrategy() throws Exception {
		ExternalTlsDirContextAuthenticationStrategy strategy = new ExternalTlsDirContextAuthenticationStrategy();
		strategy.setSslSocketFactory(sslSocketFactory());
		return strategy;
	}
	
	public SSLSocketFactory sslSocketFactory() throws NoSuchAlgorithmException, NoSuchProviderException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {
		KeyManagerCabinet cabinet = new KeyManagerCabinet.Builder(keystoreFile, keystorePassword).build();
		String provider = null;
        String protocol = "TLS";

        SSLContext ctx = provider == null ? SSLContext.getInstance(protocol) : SSLContext
            .getInstance(protocol, provider);
        
        ctx.getClientSessionContext().setSessionTimeout(sslCacheTimeout);
        ctx.init(cabinet.getKeyManagers(), cabinet.getTrustManagers(), null);

        FiltersType filter = new FiltersType();
        String[] cs = SSLUtils.getCiphersuites(CIPHER_SUITES_LIST, SSLUtils.getSupportedCipherSuites(ctx), filter, LOG, false);

        return new piecework.util.SSLSocketFactoryWrapper(ctx.getSocketFactory(), cs, protocol);
	}
	
}
