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
package piecework.resource.config;

import org.mockito.Mockito;
import org.owasp.validator.html.Policy;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import piecework.common.CustomPropertySourcesConfigurer;
import piecework.common.ServiceLocator;
import piecework.Versions;
import piecework.common.UuidGenerator;
import piecework.config.PropertiesConfiguration;
import piecework.config.ProviderConfiguration;
import piecework.content.concrete.InMemoryContentProviderReceiver;
import piecework.engine.activiti.config.EngineConfiguration;
import piecework.identity.DebugIdentityService;
import piecework.identity.DebugUserDetailsService;
import piecework.identity.IdentityHelper;
import piecework.persistence.ModelProviderFactory;
import piecework.persistence.concrete.ModelRepositoryProviderFactory;
import piecework.security.AccessTracker;
import piecework.security.Sanitizer;
import piecework.security.data.UserInputSanitizer;
import piecework.service.IdentityService;
import piecework.service.NotificationService;
import piecework.settings.SecuritySettings;

import java.io.IOException;
import java.net.URL;

import static org.mockito.Matchers.any;

/**
 * @author James Renfro
 */
@Configuration
@Import({EngineConfiguration.class, FongoConfiguration.class, ProviderConfiguration.class})
@ComponentScan(basePackages = {"piecework.command", "piecework.common", "piecework.component", "piecework.content", "piecework.engine", "piecework.form", "piecework.manager", "piecework.resource", "piecework.security", "piecework.service", "piecework.settings", "piecework.submission", "piecework.validation"})
//@ComponentScan(basePackages= {"piecework"})
public class TestResourceConfiguration {

    @Bean
    public AccessTracker accessTracker() {
        return new AccessTracker();
    }

    @Bean
    public Policy antisamyPolicy() throws Exception {
        ClassPathResource policyResource = new ClassPathResource("META-INF/piecework/antisamy-piecework-1.4.4.xml");
        URL policyUrl = policyResource.getURL();
        return Policy.getInstance(policyUrl);
    }

    @Bean
    public CacheManager cacheManager() {
        return Mockito.mock(CacheManager.class);
    }

    @Bean
    public IdentityHelper identityHelper() {
        return Mockito.mock(IdentityHelper.class);
    }

    @Bean
    public IdentityService identityService(DebugUserDetailsService userDetailsService) {
        return new DebugIdentityService(userDetailsService);
    }

    @Bean
    @Primary
    public DebugUserDetailsService debugUserDetailsService(Environment environment, ServiceLocator serviceLocator) {
        return new DebugUserDetailsService(environment, serviceLocator);
    }

    @Bean
    public ModelProviderFactory modelProviderFactory() {
        return new ModelRepositoryProviderFactory();
    }

    @Bean
    public InMemoryContentProviderReceiver memoryContentProviderReceiver() {
        return new InMemoryContentProviderReceiver();
    }

    @Bean
    public NotificationService notificationService() {
        return Mockito.mock(NotificationService.class);
    }

    @Bean
    public SecuritySettings securitySettings(Environment environment) {
        return new SecuritySettings(environment);
    }

    @Bean
    public UuidGenerator uuidGenerator() {
        return new UuidGenerator();
    }

    @Bean
    public Sanitizer userInputSanitizer() {
        return new UserInputSanitizer();
    }

    @Bean
    public Versions versions() {
        return new Versions();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer loadProperties(Environment environment) throws IOException {
        CustomPropertySourcesConfigurer configurer = new CustomPropertySourcesConfigurer();
        configurer.setCustomLocations(environment);
        return configurer;
    }

}
