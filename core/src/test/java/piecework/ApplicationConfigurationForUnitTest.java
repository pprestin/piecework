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
package piecework;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import piecework.process.ProcessRepository;
import piecework.process.ProcessResource;
import piecework.process.ProcessService;
import piecework.process.concrete.ProcessRepositoryStub;
import piecework.process.concrete.ProcessResourceVersion1Impl;
import piecework.process.concrete.ProcessServiceImpl;
import piecework.security.PassthroughSanitizer;

/**
 * @author James Renfro
 */
@Configuration
@Profile("test")
public class ApplicationConfigurationForUnitTest {

	@Bean
	public Sanitizer sanitizer() {
		return new PassthroughSanitizer();
	}
	
	@Bean
	public ProcessResource processResource() {
		ProcessResourceVersion1Impl resource = new ProcessResourceVersion1Impl();
		return resource;
	}
	
	@Bean
	public ProcessService processService() {
//		ProcessService mocked = Mockito.mock(ProcessService.class);
//		Mockito.when(mocked.storeProcess((Process)Mockito.any()))
		
		return new ProcessServiceImpl();
	}
	
	@Bean 
	public ProcessRepository processRepository() {
		return new ProcessRepositoryStub();
	}
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer loadProperties(Environment environment) {
		// This is the list of places to look for configuration properties
		List<Resource> resources = new ArrayList<Resource>();
		resources.add(new ClassPathResource("META-INF/piecework/default.properties"));
		
		PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
		configurer.setEnvironment(environment);
		configurer.setLocations(resources.toArray(new Resource[resources.size()]));
		configurer.setIgnoreUnresolvablePlaceholders(true);
		return configurer;
	}
}
