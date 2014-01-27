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
package piecework.command.config;

import org.mockito.Mockito;
import org.owasp.validator.html.Policy;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import piecework.ServiceLocator;
import piecework.command.CommandFactory;
import piecework.engine.Mediator;
import piecework.model.User;
import piecework.persistence.ActivityRepository;
import piecework.security.DataFilterService;
import piecework.security.Sanitizer;
import piecework.security.concrete.UserInputSanitizer;
import piecework.service.IdentityService;
import piecework.service.TaskService;
import piecework.submission.SubmissionHandlerRegistry;
import piecework.submission.config.SubmissionConfiguration;
import piecework.validation.ValidationFactory;

import java.net.URL;

/**
 * @author James Renfro
 */
@Configuration
@ComponentScan(basePackages = "piecework.command")
@Import({ SubmissionConfiguration.class })
public class CommandConfiguration {

    @Bean
    public DataFilterService dataFilterService() {
        return new DataFilterService();
    }

    @Bean
    public Mediator mediator() {
        return new Mediator();
    }

    @Bean
    public ServiceLocator serviceLocator() {
        ServiceLocator serviceLocator = new ServiceLocator();
        TaskService mockTaskService = Mockito.mock(TaskService.class);
        serviceLocator.setService(TaskService.class, mockTaskService);
        return serviceLocator;
    }

    @Bean
    public ValidationFactory validationFactory() {
       return new ValidationFactory();
    }

}
