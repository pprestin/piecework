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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import piecework.common.ServiceLocator;
import piecework.engine.Mediator;
import piecework.security.config.DataFilterTestConfiguration;
import piecework.service.TaskService;
import piecework.submission.config.SubmissionConfiguration;
import piecework.validation.ValidationFactory;

/**
 * @author James Renfro
 */
@Configuration
@ComponentScan(basePackages = "piecework.command")
@Import({ SubmissionConfiguration.class, DataFilterTestConfiguration.class })
public class CommandConfiguration {

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
