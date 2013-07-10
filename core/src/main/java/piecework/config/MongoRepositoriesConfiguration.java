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
package piecework.config;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactoryBean;
import org.springframework.data.repository.Repository;
import piecework.authorization.AuthorizationRepository;
import piecework.designer.InteractionRepository;
import piecework.designer.ScreenRepository;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessInstanceRepository;
import piecework.process.ProcessRepository;
import piecework.persistence.RequestRepository;
import piecework.persistence.SubmissionRepository;

/**
 * @author James Renfro
 */
@Configuration
@Profile("mongo")
public class MongoRepositoriesConfiguration {

    @Autowired
    BeanFactory beanFactory;

    @Autowired
    MongoOperations mongoOperations;

    @Bean
    public AuthorizationRepository authorizationRepository() {
        return factoryBean(AuthorizationRepository.class, Authorization.class);
    }

    @Bean
    public InteractionRepository interactionRepository() {
        return factoryBean(InteractionRepository.class, Interaction.class);
    }

    @Bean
    public ProcessRepository processRepository() {
        return factoryBean(ProcessRepository.class, Process.class);
    }

    @Bean
    public ProcessInstanceRepository processInstanceRepository() {
        return factoryBean(ProcessInstanceRepository.class, ProcessInstance.class);
    }

    @Bean
    public RequestRepository requestRepository() {
        return factoryBean(RequestRepository.class, FormRequest.class);
    }

    @Bean
    public ScreenRepository screenRepository() {
        return factoryBean(ScreenRepository.class, Screen.class);
    }

    @Bean
    public SubmissionRepository submissionRepository() {
        return factoryBean(SubmissionRepository.class, FormSubmission.class);
    }

    private <T extends Repository<S, String>, S> T factoryBean(Class<T> repositoryInterface, Class<S> type) {
        MongoRepositoryFactoryBean<T, S, String> factory = new MongoRepositoryFactoryBean<T, S, String>();
        factory.setMongoOperations(mongoOperations);
        factory.setRepositoryInterface(repositoryInterface);
        factory.afterPropertiesSet();

        return factory.getObject();
    }

}
