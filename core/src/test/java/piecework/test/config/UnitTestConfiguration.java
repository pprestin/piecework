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

import com.mongodb.DB;
import com.mongodb.DBCollection;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.ldap.core.support.LdapContextSource;
import piecework.CommandExecutor;
import piecework.Versions;
import piecework.service.*;
import piecework.Registry;
import piecework.common.UuidGenerator;
import piecework.engine.ProcessEngineFacade;
import piecework.engine.concrete.ProcessEngineConcreteFacade;
import piecework.form.FormFactory;
import piecework.validation.SubmissionTemplateFactory;
import piecework.identity.IdentityDetails;
import piecework.identity.IdentityService;
import piecework.ldap.LdapSettings;
import piecework.persistence.AuthorizationRepository;
import piecework.common.CustomPropertySourcesConfigurer;
import piecework.engine.ProcessEngineProxy;
import piecework.handler.ResponseHandler;
import piecework.persistence.*;
import piecework.handler.RequestHandler;
import piecework.handler.SubmissionHandler;
import piecework.persistence.concrete.InMemoryContentRepository;
import piecework.model.*;
import piecework.persistence.InteractionRepository;
import piecework.identity.IdentityHelper;
import piecework.resource.InteractionResource;
import piecework.model.Process;
import piecework.persistence.ScreenRepository;
import piecework.resource.ScreenResource;
import piecework.resource.concrete.InteractionResourceVersion1Impl;
import piecework.process.concrete.MongoRepositoryStub;
import piecework.resource.concrete.ProcessResourceVersion1;
import piecework.resource.concrete.ScreenResourceVersion1Impl;
import piecework.resource.ProcessResource;
import piecework.security.EncryptionService;
import piecework.security.SecuritySettings;
import piecework.security.concrete.PassthroughEncryptionService;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.security.Sanitizer;
import piecework.service.TaskService;

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
    public MongoTemplate mongoTemplate() {
        DB db = Mockito.mock(DB.class);
        DBCollection collection = Mockito.mock(DBCollection.class);
        MongoDbFactory factory = Mockito.mock(MongoDbFactory.class);
        MongoMappingContext mappingContext = new MongoMappingContext();
        MappingMongoConverter converter = new MappingMongoConverter(factory, mappingContext);

        Mockito.when(factory.getDb()).thenReturn(db);
        Mockito.when(db.getCollection(Mockito.any(String.class))).thenReturn(collection);

        return new MongoTemplate(factory, converter);
    }

	@Bean
	public Sanitizer sanitizer() {
		return new PassthroughSanitizer();
	}

    @Bean
    public SubmissionTemplateFactory submissionTemplateFactory() {
        return new SubmissionTemplateFactory();
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
    public FormFactory formFactory() {
        return new FormFactory();
    }

	@Bean
	public IdentityHelper helper() {
        IdentityHelper helper = Mockito.mock(IdentityHelper.class);
        IdentityDetails user = Mockito.mock(IdentityDetails.class);
        Mockito.when(user.getInternalId()).thenReturn("123456789");
        Mockito.when(user.getDisplayName()).thenReturn("Test User");
        Mockito.when(user.getExternalId()).thenReturn("testuser");
        Mockito.when(helper.getAuthenticatedPrincipal()).thenReturn(user);
        Mockito.when(helper.hasRole(Mockito.any(Process.class), Mockito.any(String.class))).thenReturn(true);
		return helper;
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
    public TaskService allowedTaskService() {
        return new TaskService();
    }

    @Bean
    public EncryptionService encryptionService() {
        return new PassthroughEncryptionService();
    }

    @Bean
    public FormService formService() {
        return new FormService();
    }

    @Bean
    public ProcessHistoryService historyService() {
        return new ProcessHistoryService();
    }

    @Bean
    public ProcessService processService() {
        return new ProcessService();
    }

    @Bean
    public ProcessInstanceService processInstanceService() {
        return new ProcessInstanceService();
    }

    @Bean
    public ValidationService validationService() {
        return new ValidationService();
    }

    @Bean
    public CommandExecutor toolkit() {
        return new CommandExecutor();
    }

    @Bean
    public Versions versions() {
        return new Versions();
    }

    @Bean
    public AttachmentRepository attachmentRepository() {
        return new AttachmentRepositoryStub();
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
    public NotificationRepository notificationRepository() {
        return new NotificationRepositoryStub();
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
    public SectionRepository sectionRepository() {
        return new SectionRepositoryStub();
    }

    @Bean
    public SubmissionRepository submissionRepository() {
        return new SubmissionRepositoryStub();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer loadProperties(Environment environment) throws IOException {
        CustomPropertySourcesConfigurer configurer = new CustomPropertySourcesConfigurer();
        configurer.setCustomLocations(environment);
        return configurer;
    }

    @Bean
    public LdapContextSource ldapContextSource() {
        return Mockito.mock(LdapContextSource.class);
    }

    @Bean
    public LdapSettings ldapSettings(Environment environment) {
        return new LdapSettings(environment);
    }

    @Bean
    public IdentityService internalUserDetailsService() {
        return Mockito.mock(IdentityService.class);
    }

    @Bean
    public MongoOperations mongoOperations() {
        return Mockito.mock(MongoOperations.class);
    }

    @Bean
    public SecuritySettings securitySettings(Environment environment) {
        return new SecuritySettings(environment);
    }

    @Bean
    public UuidGenerator uuidGenerator() {
        return new UuidGenerator();
    }

    public class AttachmentRepositoryStub extends MongoRepositoryStub<Attachment> implements AttachmentRepository {

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
        public ProcessInstance findByProcessDefinitionKeyAndEngineProcessInstanceId(String processDefinitionKey, String engineProcessInstanceId) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public List<ProcessInstance> findByProcessInstanceIdIn(Iterable<String> processInstanceIds) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public List<ProcessInstance> findByProcessDefinitionKeyInAndEngineProcessInstanceIdIn(Iterable<String> processDefinitionKeys, Iterable<String> engineProcessInstanceIds) {
            return null;
        }

        @Override
        public List<ProcessInstance> findByProcessDefinitionKeyInAndEngineProcessInstanceIdInAndKeyword(Iterable<String> processDefinitionKeys, Iterable<String> engineProcessInstanceIds, String keyword) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
	
	public class InteractionRepositoryStub extends MongoRepositoryStub<Interaction> implements InteractionRepository {

	}

    public class NotificationRepositoryStub extends MongoRepositoryStub<Notification> implements NotificationRepository {

    }

    public class RequestRepositoryStub extends MongoRepositoryStub<FormRequest> implements RequestRepository {

    }

	public class ScreenRepositoryStub extends MongoRepositoryStub<Screen> implements ScreenRepository {

	}

    public class SectionRepositoryStub extends MongoRepositoryStub<Section> implements SectionRepository {

    }

    public class SubmissionRepositoryStub extends MongoRepositoryStub<Submission> implements SubmissionRepository {

    }

}
