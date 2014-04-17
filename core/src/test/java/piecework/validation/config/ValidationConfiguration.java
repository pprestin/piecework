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
package piecework.validation.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import piecework.Versions;
import piecework.security.config.DataFilterTestConfiguration;
import piecework.security.data.DataFilterService;
import piecework.security.config.EncryptionTestConfiguration;
import piecework.validation.ValidationFactory;
import piecework.manager.StorageManager;
import piecework.ServiceLocator;

/**
 * @author James Renfro
 */
@Configuration
@Import({DataFilterTestConfiguration.class})
public class ValidationConfiguration {

    @Bean
    public ValidationFactory validationFactory() {
        return new ValidationFactory();
    }

    @Bean
    public Versions versions() {
        return new Versions();
    }

    @Bean
    public ServiceLocator serviceLocator() {
        ServiceLocator serviceLocator = new ServiceLocator();
        StorageManager mockStorageManager = Mockito.mock(StorageManager.class);
        serviceLocator.setService(StorageManager.class, mockStorageManager);
        return serviceLocator;
    }   
}
