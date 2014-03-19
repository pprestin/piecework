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
package piecework.submission.config;

import org.mockito.Mockito;
import org.owasp.validator.html.Policy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import piecework.common.UuidGenerator;
import piecework.model.User;
import piecework.repository.config.MockRepositoryConfiguration;
import piecework.security.Sanitizer;
import piecework.security.data.UserInputSanitizer;
import piecework.service.IdentityService;
import piecework.service.SubmissionStorageService;
import piecework.service.UserInterfaceService;
import piecework.submission.SubmissionHandlerRegistry;
import piecework.submission.SubmissionTemplateFactory;
import piecework.submission.concrete.FormValueSubmissionHandler;
import piecework.submission.concrete.MultipartSubmissionHandler;

import java.net.URL;

import static org.mockito.Matchers.any;

/**
 * @author James Renfro
 */
@Configuration
@Import(MockRepositoryConfiguration.class)
public class SubmissionConfiguration {

    @Bean
    public Policy antisamyPolicy() throws Exception {
        ClassPathResource policyResource = new ClassPathResource("META-INF/piecework/antisamy-piecework-1.4.4.xml");
        URL policyUrl = policyResource.getURL();
        return Policy.getInstance(policyUrl);
    }

    @Bean
    public FormValueSubmissionHandler formValueSubmissionHandler() {
        return new FormValueSubmissionHandler();
    }

    @Bean
    public MultipartSubmissionHandler multipartSubmissionHandler() {
        return new MultipartSubmissionHandler();
    }

    @Bean
    public IdentityService identityService() {
        User testUser = Mockito.mock(User.class);
        Mockito.doReturn("testuser")
                .when(testUser).getUserId();

        IdentityService mock = Mockito.mock(IdentityService.class);
        Mockito.doReturn(testUser)
                .when(mock).getUser("testuser");

        return mock;
    }

    @Bean
    public SubmissionHandlerRegistry submissionHandlerRegistry() {
        return new SubmissionHandlerRegistry();
    }

    @Bean
    public SubmissionStorageService submissionStorageService() {
        return new SubmissionStorageService();
    }

    @Bean
    public SubmissionTemplateFactory submissionTemplateFactory() {
        return new SubmissionTemplateFactory();
    }

    @Bean
    public UuidGenerator uuidGenerator() {
        return new UuidGenerator();
    }

    @Bean
    public Sanitizer userInputSanitizer() {
        return new UserInputSanitizer();
    }

}
