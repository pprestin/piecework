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
package piecework.test.config;

import java.io.IOException;
import java.util.List;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

import piecework.Registry;
import piecework.engine.ProcessEngineFacade;
import piecework.engine.concrete.ProcessEngineConcreteFacade;
import piecework.persistence.AuthorizationRepository;
import piecework.common.CustomPropertySourcesConfigurer;
import piecework.engine.ProcessEngineProxy;
import piecework.form.handler.ResponseHandler;
import piecework.persistence.*;
import piecework.form.handler.RequestHandler;
import piecework.form.handler.SubmissionHandler;
import piecework.persistence.concrete.InMemoryContentRepository;
import piecework.model.*;
import piecework.persistence.InteractionRepository;
import piecework.designer.InteractionResource;
import piecework.model.Process;
import piecework.process.*;
import piecework.persistence.ScreenRepository;
import piecework.designer.ScreenResource;
import piecework.designer.concrete.InteractionResourceVersion1Impl;
import piecework.process.concrete.MongoRepositoryStub;
import piecework.process.concrete.ProcessResourceVersion1;
import piecework.process.concrete.ResourceHelper;
import piecework.designer.concrete.ScreenResourceVersion1Impl;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.security.Sanitizer;

/**
 * @author James Renfro
 */
@Configuration
@Profile("test")
public class UnitTestConfiguration {

    @Bean
    public ProcessEngineFacade facade() {
        return new ProcessEngineConcreteFacade();
    }

    @Bean
    public ProcessEngineProxy proxy() {
        return Mockito.mock(ProcessEngineProxy.class);
    }

    @Bean
    public Registry registry() {
        return new Registry();
    }

	@Bean
	public Sanitizer sanitizer() {
		return new PassthroughSanitizer();
	}

    @Bean
    public RequestHandler requestHandler() {
        return new RequestHandler();
    }

    @Bean
    public ResponseHandler responseHandler() {
        return new ResponseHandler();
    }

    @Bean
    public SubmissionHandler submissionHandler() {
        return new SubmissionHandler();
    }

	@Bean
	public ResourceHelper helper() {
		return new ResourceHelper();
	}
	
	@Bean
	public ProcessResource processResource() {
		ProcessResourceVersion1 resource = new ProcessResourceVersion1();
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
    public AuthorizationRepository authorizationRepository() {
        return new AuthorizationRepositoryStub();
    }

    @Bean
    public ContentRepository contentRepository() {
        return new InMemoryContentRepository();
    }

    @Bean
    public InteractionRepository interactionRepository() {
        return new InteractionRepositoryStub();
    }

	@Bean 
	public ProcessRepository processRepository() {
		return new ProcessRepositoryStub();
	}

    @Bean
    public ProcessInstanceRepository processInstanceRepository() {
        return new ProcessInstanceRepositoryStub();
    }

    @Bean
    public RequestRepositoryStub requestRepository() {
        return new RequestRepositoryStub();
    }
	
	@Bean 
	public ScreenRepository screenRepository() {
		return new ScreenRepositoryStub();
	}

    @Bean
    public SubmissionRepository submissionRepository() {
        return new SubmissionRepositoryStub();
    }
	
//	@Bean
//	public static PropertySourcesPlaceholderConfigurer loadProperties(Environment environment) {
//		// This is the list of places to look for configuration properties
//		List<Resource> resources = new ArrayList<Resource>();
//		resources.add(new ClassPathResource("META-INF/piecework/default.properties"));
//
//		PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
//		configurer.setEnvironment(environment);
//		configurer.setLocations(resources.toArray(new Resource[resources.size()]));
//		configurer.setIgnoreUnresolvablePlaceholders(true);
//		return configurer;
//	}

    @Bean
    public static PropertySourcesPlaceholderConfigurer loadProperties(Environment environment) throws IOException {
        CustomPropertySourcesConfigurer configurer = new CustomPropertySourcesConfigurer();
        configurer.setCustomLocations(environment);
        return configurer;
    }

    public class AuthorizationRepositoryStub extends MongoRepositoryStub<Authorization> implements AuthorizationRepository {

    }
	
	public class ProcessRepositoryStub extends MongoRepositoryStub<Process> implements ProcessRepository {
        @Override
        public List<Process> findAllBasic(Iterable<String> processDefinitionKeys) {
            return null;
        }
    }

    public class ProcessInstanceRepositoryStub extends MongoRepositoryStub<ProcessInstance> implements ProcessInstanceRepository {
        @Override
        public List<ProcessInstance> findByKeywordsRegex(String keyword) {
            return null;
        }

        @Override
        public List<ProcessInstance> findByProcessInstanceIdInAndKeywordsRegex(Iterable<String> processInstanceIds, String keyword) {
            return null;
        }

        @Override
        public List<ProcessInstance> findByProcessDefinitionKeyInAndEngineProcessInstanceIdIn(Iterable<String> processDefinitionKeys, Iterable<String> engineProcessInstanceIds) {
            return null;
        }
    }
	
	public class InteractionRepositoryStub extends MongoRepositoryStub<Interaction> implements InteractionRepository {

	}

    public class RequestRepositoryStub extends MongoRepositoryStub<FormRequest> implements RequestRepository {

    }

	public class ScreenRepositoryStub extends MongoRepositoryStub<Screen> implements ScreenRepository {

	}

    public class SubmissionRepositoryStub extends MongoRepositoryStub<Submission> implements SubmissionRepository {

    }

}
