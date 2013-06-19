/*
 * Copyright 2013 University of Washington
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
package piecework.engine.activiti.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

import piecework.engine.activiti.ActivitiEngineProxy;
import piecework.engine.ProcessEngineProxy;

/**
 * @author James Renfro
 */
@Configuration
@Profile("test")
@PropertySource("classpath:META-INF/test.properties")
@Import(EngineConfiguration.class)
public class TestConfiguration {

	@Bean
	public ProcessEngineProxy activitiEngineProxy() {
		return new ActivitiEngineProxy();
	}
	
}
