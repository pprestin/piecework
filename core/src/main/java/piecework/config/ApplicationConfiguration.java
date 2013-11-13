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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.configuration.jsse.SSLUtils;
import org.apache.cxf.configuration.security.FiltersType;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSBindingFactory;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.env.Environment;
import org.springframework.ldap.authentication.DefaultValuesAuthenticationSourceDecorator;
import org.springframework.ldap.core.AuthenticationSource;
import org.springframework.ldap.core.support.DirContextAuthenticationStrategy;
import org.springframework.ldap.core.support.ExternalTlsDirContextAuthenticationStrategy;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.core.support.SimpleDirContextAuthenticationStrategy;
import org.springframework.security.ldap.authentication.*;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.util.StringUtils;
import piecework.authorization.AuthorizationRoleMapper;
import piecework.exception.AccessDeniedExceptionMapper;
import piecework.exception.GeneralExceptionMapper;
import piecework.exception.StatusCodeErrorMapper;
import piecework.form.AnonymousFormResource;
import piecework.identity.DebugIdentityService;
import piecework.identity.DisplayNameConverter;
import piecework.ldap.CustomLdapUserDetailsMapper;
import piecework.ldap.LdapIdentityService;
import piecework.ldap.LdapSettings;
import piecework.security.CustomAuthenticationSource;
import piecework.security.SecuritySettings;
import piecework.service.IdentityService;
import piecework.ui.CustomJaxbJsonProvider;
import piecework.ui.HtmlProvider;
import piecework.ui.JavascriptProvider;
import piecework.util.KeyManagerCabinet;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.*;

/**
 * @author James Renfro
 */
@Configuration
public class ApplicationConfiguration {

	private static final Logger LOG = Logger.getLogger(ApplicationConfiguration.class);
    private static final List<String> CIPHER_SUITES_LIST = Arrays.asList("SSL_RSA_WITH_RC4_128_MD5", "SSL_RSA_WITH_RC4_128_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_DSS_WITH_AES_128_CBC_SHA", "SSL_RSA_WITH_3DES_EDE_CBC_SHA", "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA", "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA", "SSL_RSA_WITH_DES_CBC_SHA", "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA", "SSL_RSA_EXPORT_WITH_RC4_40_MD5", "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA", "TLS_EMPTY_RENEGOTIATION_INFO_SCSV", "TLS_KRB5_WITH_RC4_128_SHA", "TLS_KRB5_WITH_RC4_128_MD5", "TLS_KRB5_WITH_3DES_EDE_CBC_SHA", "TLS_KRB5_WITH_3DES_EDE_CBC_MD5", "TLS_KRB5_WITH_DES_CBC_SHA", "TLS_KRB5_WITH_DES_CBC_MD5", "TLS_KRB5_EXPORT_WITH_RC4_40_SHA", "TLS_KRB5_EXPORT_WITH_RC4_40_MD5", "TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA", "TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5" );
    private static int SSL_CACHE_TIMEOUT = 86400000;

    @Autowired
	piecework.ApplicationResource[] applicationResources;

    @Autowired
    piecework.ApiResource[] apiResources;

    @Autowired
    AnonymousFormResource formResource;
	
	@Autowired
    HtmlProvider htmlProvider;

    @Autowired
    CustomJaxbJsonProvider jsonProvider;

    @Autowired
    JavascriptProvider javascriptProvider;

	@Bean
	public Bus cxf() {
		return BusFactory.newInstance().createBus();
	}

	@Bean 
	public Server apiServer() {
		Map<Object, Object> extensionMappings = new HashMap<Object, Object>();
		extensionMappings.put("json", "application/json");
		extensionMappings.put("xml", "application/xml");
		
		JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
		sf.setServiceBeanObjects((Object[])apiResources);
		sf.setAddress("/api/v1");
		sf.setExtensionMappings(extensionMappings);
		
		List<Object> providers = new ArrayList<Object>();
		providers.add(new GeneralExceptionMapper());
		providers.add(new StatusCodeErrorMapper());
		providers.add(new AccessDeniedExceptionMapper());
		providers.add(htmlProvider);
		providers.add(jsonProvider);
		sf.setProviders(providers);

		BindingFactoryManager manager = sf.getBus().getExtension(BindingFactoryManager.class);
		JAXRSBindingFactory factory = new JAXRSBindingFactory();
		factory.setBus(sf.getBus());
		manager.registerBindingFactory(JAXRSBindingFactory.JAXRS_BINDING_ID, factory);
		return sf.create();
	}

    @Bean
    public Server applicationServer() {
        Map<Object, Object> extensionMappings = new HashMap<Object, Object>();
        extensionMappings.put("css", "text/css");
        extensionMappings.put("js", "text/javascript");
        extensionMappings.put("json", "application/json");
        extensionMappings.put("xml", "application/xml");
        extensionMappings.put("html", "text/html");

        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setServiceBeanObjects((Object[])applicationResources);
        sf.setAddress("/secure");
        sf.setExtensionMappings(extensionMappings);

        List<Object> providers = new ArrayList<Object>();
        providers.add(new GeneralExceptionMapper());
        providers.add(new StatusCodeErrorMapper());
        providers.add(new AccessDeniedExceptionMapper());
        providers.add(htmlProvider);
        providers.add(jsonProvider);
//        providers.add(javascriptProvider);
        sf.setProviders(providers);

        BindingFactoryManager manager = sf.getBus().getExtension(BindingFactoryManager.class);
        JAXRSBindingFactory factory = new JAXRSBindingFactory();
        factory.setBus(sf.getBus());
        manager.registerBindingFactory(JAXRSBindingFactory.JAXRS_BINDING_ID, factory);
        return sf.create();
    }

    @Bean
    public Server formServer() {
        Map<Object, Object> extensionMappings = new HashMap<Object, Object>();
        extensionMappings.put("json", "application/json");
        extensionMappings.put("html", "text/html");

        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setServiceBeanObjects(formResource);
        sf.setAddress("/public");
        sf.setExtensionMappings(extensionMappings);

        List<Object> providers = new ArrayList<Object>();
        providers.add(new GeneralExceptionMapper());
        providers.add(new StatusCodeErrorMapper());
        providers.add(new AccessDeniedExceptionMapper());
        providers.add(htmlProvider);
        providers.add(jsonProvider);
        sf.setProviders(providers);

        BindingFactoryManager manager = sf.getBus().getExtension(BindingFactoryManager.class);
        JAXRSBindingFactory factory = new JAXRSBindingFactory();
        factory.setBus(sf.getBus());
        manager.registerBindingFactory(JAXRSBindingFactory.JAXRS_BINDING_ID, factory);
        return sf.create();
    }

    @Bean
    public HtmlProvider htmlProvider() {
        return new HtmlProvider();
    }

    @Bean
    public JacksonJaxbJsonProvider jacksonJaxbJsonProvider() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return new JacksonJaxbJsonProvider(objectMapper, JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS);
    }

    @Bean
    public CustomJaxbJsonProvider customJaxbJsonProvider() {
        return new CustomJaxbJsonProvider();
    }

    @Bean(name="pieceworkAuthorizationRoleMapper")
    public AuthorizationRoleMapper authorizationRoleMapper() {
        return new AuthorizationRoleMapper();
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }

    @Bean
    public KeyManagerCabinet keyManagerCabinet(Environment environment) throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        SecuritySettings securitySettings = securitySettings(environment);
        return keyManagerCabinet(securitySettings);
    }

    private KeyManagerCabinet keyManagerCabinet(SecuritySettings securitySettings) throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {

        try {
            return new KeyManagerCabinet.Builder(securitySettings).build();
        } catch (FileNotFoundException e) {
            LOG.error("Could not createDeployment key manager cabinet because keystore file not found");
        }

        return null;
    }

    @Bean
    public LdapSettings ldapSettings(Environment environment) {
        return new LdapSettings(environment);
    }

    @Bean
    public SecuritySettings securitySettings(Environment environment) {
        return new SecuritySettings(environment);
    }

    @Bean
    public IdentityService userDetailsService(Environment environment) throws Exception {
        Boolean isDebugMode = environment.getProperty("debug.mode", Boolean.class, Boolean.FALSE);
        Boolean isDebugIdentity = environment.getProperty("debug.identity", Boolean.class, Boolean.FALSE);

        if (isDebugMode && isDebugIdentity)
            return new DebugIdentityService();

        String identityProviderProtocol = environment.getProperty("identity.provider.protocol");

        LdapSettings ldapSettings = ldapSettings(environment);
        SecuritySettings securitySettings = securitySettings(environment);

        LdapContextSource contextSource = personLdapContextSource(ldapSettings, securitySettings);
        LdapUserSearch userSearch = userSearch(contextSource, ldapSettings);
        LdapUserSearch userSearchInternal = userSearchInternal(environment, contextSource);
        LdapAuthoritiesPopulator authoritiesPopulator = authoritiesPopulator(ldapSettings, securitySettings);
        CustomLdapUserDetailsMapper userDetailsMapper = userDetailsMapper();

        return new LdapIdentityService(contextSource, userSearch, userSearchInternal, authoritiesPopulator, userDetailsMapper, ldapSettings, cacheManager());
    }

    @Bean
    public CustomLdapUserDetailsMapper userDetailsMapper() throws Exception {
        return new CustomLdapUserDetailsMapper(new LdapUserDetailsMapper());
    }

    private LdapAuthenticator authenticator(LdapContextSource personLdapContextSource, LdapSettings ldapSettings) throws Exception {

        AbstractLdapAuthenticator authenticator = null;

        LdapSettings.LdapAuthenticationType type = ldapSettings.getAuthenticationType();

        switch (type) {
            case PASSWORDCOMPARE:
                authenticator = new PasswordComparisonAuthenticator(personLdapContextSource);
                break;
            default:
                authenticator = new BindAuthenticator(personLdapContextSource);
        }

        authenticator.setUserSearch(userSearch(personLdapContextSource, ldapSettings));

        return authenticator;
    }

    private AuthenticationSource authenticationSource(LdapSettings ldapSettings) {
        CustomAuthenticationSource authenticationSource = new CustomAuthenticationSource();

        LdapSettings.LdapAuthenticationEncryption encryption = ldapSettings.getEncryption();

        if (encryption == LdapSettings.LdapAuthenticationEncryption.TLS)
            return new DefaultValuesAuthenticationSourceDecorator(authenticationSource, ldapSettings.getLdapDefaultUser(), new String(ldapSettings.getLdapDefaultPassword()));

        return authenticationSource;
    }

    private DirContextAuthenticationStrategy authenticationStrategy(LdapSettings ldapSettings, SecuritySettings securitySettings) throws Exception {
        LdapSettings.LdapAuthenticationEncryption encryption = ldapSettings.getEncryption();

        DirContextAuthenticationStrategy strategy = null;

        switch (encryption) {
            case TLS:
                strategy = new ExternalTlsDirContextAuthenticationStrategy();
                ((ExternalTlsDirContextAuthenticationStrategy)strategy).setSslSocketFactory(sslSocketFactory(securitySettings));
                break;
            default:
                strategy = new SimpleDirContextAuthenticationStrategy();
        }

        return strategy;
    }

    private LdapAuthoritiesPopulator authoritiesPopulator(LdapSettings ldapSettings, SecuritySettings securitySettings) throws Exception {
        DefaultLdapAuthoritiesPopulator authoritiesPopulator = new DefaultLdapAuthoritiesPopulator(groupLdapContextSource(ldapSettings, securitySettings), ldapSettings.getLdapGroupSearchBase());
        authoritiesPopulator.setGroupSearchFilter(ldapSettings.getLdapGroupSearchFilter());
        return authoritiesPopulator;
    }

    private LdapContextSource groupLdapContextSource(LdapSettings ldapSettings, SecuritySettings securitySettings) throws Exception {

        LdapContextSource context = new LdapContextSource();
        context.setUrl(ldapSettings.getLdapGroupUrl());
        context.setBase(ldapSettings.getLdapGroupBase());
        context.setUserDn(ldapSettings.getLdapPersonDn());
        context.setAuthenticationSource(authenticationSource(ldapSettings));
        context.setAuthenticationStrategy(authenticationStrategy(ldapSettings, securitySettings));
        context.afterPropertiesSet();

        return context;
    }

    private LdapContextSource personLdapContextSource(LdapSettings ldapSettings, SecuritySettings securitySettings) throws Exception {

        LdapContextSource context = new LdapContextSource();
        context.setUrl(ldapSettings.getLdapPersonUrl());
        context.setBase(ldapSettings.getLdapPersonBase());
        context.setUserDn(ldapSettings.getLdapPersonDn());
        context.setAuthenticationSource(authenticationSource(ldapSettings));
        context.setAuthenticationStrategy(authenticationStrategy(ldapSettings, securitySettings));
        context.afterPropertiesSet();

        return context;
    }

    private SSLSocketFactory sslSocketFactory(SecuritySettings securitySettings) throws NoSuchAlgorithmException, NoSuchProviderException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {
        KeyManagerCabinet cabinet = keyManagerCabinet(securitySettings);
        String provider = null;
        String protocol = "TLS";

        SSLContext ctx = provider == null ? SSLContext.getInstance(protocol) : SSLContext
                .getInstance(protocol, provider);

        ctx.getClientSessionContext().setSessionTimeout(SSL_CACHE_TIMEOUT);
        ctx.init(cabinet.getKeyManagers(), cabinet.getTrustManagers(), null);

        FiltersType filter = new FiltersType();
        String[] cs = SSLUtils.getCiphersuites(CIPHER_SUITES_LIST, SSLUtils.getSupportedCipherSuites(ctx), filter, java.util.logging.Logger.getLogger(this.getClass().getCanonicalName()), false);

        return new piecework.util.SSLSocketFactoryWrapper(ctx.getSocketFactory(), cs, protocol);
    }

    private LdapUserSearch userSearch(LdapContextSource personLdapContextSource, LdapSettings ldapSettings) throws Exception {
        LdapUserSearch userSearch = new FilterBasedLdapUserSearch(ldapSettings.getLdapPersonSearchBase(), ldapSettings.getLdapPersonSearchFilter(), personLdapContextSource);
        ((FilterBasedLdapUserSearch)userSearch).setReturningAttributes(null);
        ((FilterBasedLdapUserSearch)userSearch).setSearchSubtree(true);
        ((FilterBasedLdapUserSearch)userSearch).setSearchTimeLimit(10000);
        return userSearch;
    }

    private LdapUserSearch userSearchInternal(Environment environment, LdapContextSource personLdapContextSource) throws Exception {
        LdapSettings ldapSettings = ldapSettings(environment);
        String ldapPersonSearchFilter = ldapSettings.getLdapPersonSearchFilterInternal();

        // Fallback to original setting if an internal setting is not defined
        if (StringUtils.isEmpty(ldapPersonSearchFilter))
            ldapPersonSearchFilter = ldapSettings.getLdapPersonSearchFilter();

        LdapUserSearch userSearch = new FilterBasedLdapUserSearch(ldapSettings.getLdapPersonSearchBase(), ldapPersonSearchFilter, personLdapContextSource);
        ((FilterBasedLdapUserSearch)userSearch).setReturningAttributes(null);
        ((FilterBasedLdapUserSearch)userSearch).setSearchSubtree(true);
        ((FilterBasedLdapUserSearch)userSearch).setSearchTimeLimit(10000);
        return userSearch;
    }
}
