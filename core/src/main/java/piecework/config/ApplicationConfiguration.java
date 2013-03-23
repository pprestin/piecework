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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import piecework.exception.AccessDeniedExceptionMapper;
import piecework.exception.GeneralExceptionMapper;
import piecework.exception.StatusCodeErrorMapper;
import piecework.ui.MustacheHtmlTransformer;
import piecework.util.AcceptablePropertiesFilenameFilter;

/**
 * @author James Renfro
 */
@Configuration
public class ApplicationConfiguration {

	private static final Logger LOG = Logger.getLogger(ApplicationConfiguration.class);
	
	@Autowired 
	Environment env;
	
	@Autowired 
	piecework.Resource[] resources;
			
//	@Autowired
//	AuthenticationHandler authenticationHandler;
	
	@Autowired
	MustacheHtmlTransformer mustacheHtmlTransformer;
	
	@Bean
	public Bus cxf() {
		return BusFactory.newInstance().createBus();
	}
	
	@Bean 
	public Server server() {
		Map<Object, Object> extensionMappings = new HashMap<Object, Object>();
		extensionMappings.put("json", "application/json");
		extensionMappings.put("xml", "application/xml");
		extensionMappings.put("html", "text/html");
		
		JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
		sf.setServiceBeanObjects((Object[])resources);
		sf.setAddress("/");
		sf.setExtensionMappings(extensionMappings);
		
		List<Object> providers = new ArrayList<Object>();
		providers.add(new GeneralExceptionMapper());
		providers.add(new StatusCodeErrorMapper());
		providers.add(new AccessDeniedExceptionMapper());
		providers.add(mustacheHtmlTransformer);
//		providers.add(authenticationHandler);
		sf.setProviders(providers);

		BindingFactoryManager manager = sf.getBus().getExtension(BindingFactoryManager.class);
		JAXRSBindingFactory factory = new JAXRSBindingFactory();
		factory.setBus(sf.getBus());
		manager.registerBindingFactory(JAXRSBindingFactory.JAXRS_BINDING_ID, factory);
		return sf.create();
	}
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer loadProperties(Environment environment) {
		// This is the list of places to look for configuration properties
		List<Resource> resources = new ArrayList<Resource>();
		resources.add(new ClassPathResource("META-INF/piecework/default.properties"));
		
		// Check if there is a system property pointing to another config location
		String location = System.getProperty("piecework.config.location");
		
		if (location == null)
			location = "/etc/piecework";
		
		File configDirectory = new File(location);
		
		// Check to make sure the location actually exists
		if (configDirectory.exists()) {
			if (configDirectory.isFile()) {
				// If the location that was passed in is a file, then go ahead and use it as the single properties file
				resources.add(new FileSystemResource(configDirectory));
			} else {
				// Otherwise, read all of the properties files, and exclude ones that have a -dev, -test, etc... if those are not active profiles
				File[] files = configDirectory.listFiles(new AcceptablePropertiesFilenameFilter(environment));
				if (files != null && files.length > 0) {
					for (File file : files) {
						if (file.isFile())
							resources.add(new FileSystemResource(file));
						else if (LOG.isDebugEnabled()) 
							LOG.debug("Cannot read configuration file " + file.getAbsolutePath() + " because it's actually a directory");
					}
				}
			}
		} else {
			LOG.warn("No configuration properties found at " + location);
		}
		
		PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
		configurer.setEnvironment(environment);
		configurer.setLocations(resources.toArray(new Resource[resources.size()]));
		configurer.setIgnoreUnresolvablePlaceholders(true);
		return configurer;
	}
	
}
