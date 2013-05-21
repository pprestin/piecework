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

import piecework.model.Interaction;
import piecework.model.Process;
import piecework.model.Screen;
import piecework.designer.InteractionRepository;
import piecework.designer.InteractionResource;
import piecework.process.ProcessRepository;
import piecework.process.ProcessResource;
import piecework.designer.ScreenRepository;
import piecework.designer.ScreenResource;
import piecework.designer.concrete.InteractionResourceVersion1Impl;
import piecework.process.concrete.MongoRepositoryStub;
import piecework.process.concrete.ProcessResourceVersion1Impl;
import piecework.process.concrete.ResourceHelper;
import piecework.designer.concrete.ScreenResourceVersion1Impl;
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
	public ResourceHelper helper() {
		return new ResourceHelper();
	}
	
	@Bean
	public ProcessResource processResource() {
		ProcessResourceVersion1Impl resource = new ProcessResourceVersion1Impl();
		return resource;
	}
	
	@Bean
	public InteractionResource interactionResource() {
		InteractionResourceVersion1Impl resource = new InteractionResourceVersion1Impl();
		return resource;
	}
	
	@Bean
	public ScreenResource screenResource() {
		ScreenResourceVersion1Impl resource = new ScreenResourceVersion1Impl();
		return resource;
	}
	
	@Bean 
	public ProcessRepository processRepository() {
		return new ProcessRepositoryStub();
	}
	
	@Bean 
	public InteractionRepository interactionRepository() {
		return new InteractionRepositoryStub();
	}
	
	@Bean 
	public ScreenRepository screenRepository() {
		return new ScreenRepositoryStub();
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
	
	public class ProcessRepositoryStub extends MongoRepositoryStub<Process> implements ProcessRepository {
		
	}
	
	public class InteractionRepositoryStub extends MongoRepositoryStub<Interaction> implements InteractionRepository {

	}

	public class ScreenRepositoryStub extends MongoRepositoryStub<Screen> implements ScreenRepository {

	}
}
