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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import piecework.form.FormResource;
import piecework.form.impl.FormResourceImpl;

/**
 * @author James Renfro
 */
@Configuration
public class ApplicationConfiguration {

	@Autowired Environment env;
	
	@Bean
	public FormResource formResource() {
		return new FormResourceImpl();
	}
		
	@Bean
	public static PropertySourcesPlaceholderConfigurer loadProperties() {
		PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
		Resource[] resources = new ClassPathResource[] { 
				new ClassPathResource("META-INF/piecework/default.properties") 
		};
		configurer.setLocations(resources);
		configurer.setIgnoreUnresolvablePlaceholders(true);
		return configurer;
	}
	
}
