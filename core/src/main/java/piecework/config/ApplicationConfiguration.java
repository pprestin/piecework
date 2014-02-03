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

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSBindingFactory;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import piecework.exception.AccessDeniedExceptionMapper;
import piecework.exception.GeneralExceptionMapper;
import piecework.exception.StatusCodeErrorMapper;
import piecework.resource.AnonymousFormResource;
import piecework.resource.AnonymousScriptResource;
import piecework.ui.CustomJaxbJsonProvider;
import piecework.ui.visitor.HtmlProvider;

import java.util.*;

/**
 * @author James Renfro
 */
@Configuration
@Import({PropertiesConfiguration.class, CacheConfiguration.class, IdentityConfiguration.class, WebSecurityConfiguration.class, ProviderConfiguration.class})
public class ApplicationConfiguration {

	private static final Logger LOG = Logger.getLogger(ApplicationConfiguration.class);
    private static final List<String> CIPHER_SUITES_LIST = Arrays.asList("SSL_RSA_WITH_RC4_128_MD5", "SSL_RSA_WITH_RC4_128_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_DSS_WITH_AES_128_CBC_SHA", "SSL_RSA_WITH_3DES_EDE_CBC_SHA", "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA", "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA", "SSL_RSA_WITH_DES_CBC_SHA", "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA", "SSL_RSA_EXPORT_WITH_RC4_40_MD5", "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA", "TLS_EMPTY_RENEGOTIATION_INFO_SCSV", "TLS_KRB5_WITH_RC4_128_SHA", "TLS_KRB5_WITH_RC4_128_MD5", "TLS_KRB5_WITH_3DES_EDE_CBC_SHA", "TLS_KRB5_WITH_3DES_EDE_CBC_MD5", "TLS_KRB5_WITH_DES_CBC_SHA", "TLS_KRB5_WITH_DES_CBC_MD5", "TLS_KRB5_EXPORT_WITH_RC4_40_SHA", "TLS_KRB5_EXPORT_WITH_RC4_40_MD5", "TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA", "TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5" );
    private static int SSL_CACHE_TIMEOUT = 86400000;

    @Autowired(required = false)
	piecework.ApplicationResource[] applicationResources;

    @Autowired(required = false)
    piecework.ApiResource[] apiResources;

    @Autowired
    AnonymousFormResource formResource;

    @Autowired
    AnonymousScriptResource scriptResource;
	
	@Autowired
    HtmlProvider htmlProvider;

    @Autowired
    CustomJaxbJsonProvider jsonProvider;

    @Autowired
    GeneralExceptionMapper generalExceptionMapper;

    @Autowired
    StatusCodeErrorMapper statusCodeErrorMapper;

	@Bean
	public Bus cxf() {
		return BusFactory.newInstance().createBus();
	}

	@Bean 
	public Server apiServer() {
		Map<Object, Object> extensionMappings = new HashMap<Object, Object>();
		extensionMappings.put("json", "application/json");
//		extensionMappings.put("xml", "application/xml");
		
		JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
		sf.setServiceBeanObjects((Object[])apiResources);
		sf.setAddress("/api/v1");
		sf.setExtensionMappings(extensionMappings);
		
		List<Object> providers = new ArrayList<Object>();
		providers.add(generalExceptionMapper);
		providers.add(statusCodeErrorMapper);
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
        extensionMappings.put("json", "application/json");
        extensionMappings.put("html", "text/html");
        extensionMappings.put("csv", "text/csv");

        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setServiceBeanObjects((Object[])applicationResources);
        sf.setAddress("/ui");
        sf.setExtensionMappings(extensionMappings);

        List<Object> providers = new ArrayList<Object>();
//        providers.add(new CrossOriginResourceSharingFilter());
        providers.add(generalExceptionMapper);
        providers.add(statusCodeErrorMapper);
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
    public Server formServer() {
        Map<Object, Object> extensionMappings = new HashMap<Object, Object>();
        extensionMappings.put("json", "application/json");
        extensionMappings.put("html", "text/html");

        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setServiceBeanObjects(formResource, scriptResource);
        sf.setAddress("/public");
        sf.setExtensionMappings(extensionMappings);

        List<Object> providers = new ArrayList<Object>();
        providers.add(generalExceptionMapper);
        providers.add(statusCodeErrorMapper);
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



//    @Bean
//    public JacksonJaxbJsonProvider jacksonJaxbJsonProvider() {
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
//        return new JacksonJaxbJsonProvider(objectMapper, JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS);
//    }



}
