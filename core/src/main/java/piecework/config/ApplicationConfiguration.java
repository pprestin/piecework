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
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSBindingFactory;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.log4j.Logger;
import org.owasp.validator.html.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import piecework.exception.AccessDeniedExceptionMapper;
import piecework.exception.GeneralExceptionMapper;
import piecework.exception.StatusCodeErrorMapper;
import piecework.ui.HtmlProvider;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
@Configuration
public class ApplicationConfiguration {

	private static final Logger LOG = Logger.getLogger(ApplicationConfiguration.class);

	@Autowired 
	piecework.Resource[] resources;

    @Autowired
    piecework.ApiResource[] apiResources;
	
	@Autowired
    HtmlProvider htmlProvider;

    @Autowired
    JacksonJsonProvider jsonProvider;
	
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
//		providers.add(htmlProvider);
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
        extensionMappings.put("xml", "application/xml");
        extensionMappings.put("html", "text/html");

        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setServiceBeanObjects((Object[])resources);
        sf.setAddress("/app");
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
	public JacksonJsonProvider jsonProvider() {
		return new JacksonJsonProvider();
	}
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer loadProperties(Environment environment) throws IOException {
        CustomPropertySourcesConfigurer configurer = new CustomPropertySourcesConfigurer();
        configurer.setCustomLocations(environment);
		return configurer;
	}
	
	@Bean
	public Policy antisamyPolicy() throws Exception {
		ClassPathResource policyResource = new ClassPathResource("META-INF/piecework/antisamy-1.4.3.xml");
		URL policyUrl = policyResource.getURL();
				
		return Policy.getInstance(policyUrl);
	}
		
}
