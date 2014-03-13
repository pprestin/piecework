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
package piecework.repository.config;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import piecework.repository.*;

import static org.mockito.Matchers.any;

/**
 * @author James Renfro
 */
@Configuration
public class MockRepositoryConfiguration {

    @Bean
    public AccessEventRepository accessEventRepository() {
        return mockRepository(AccessEventRepository.class);
    }

    @Bean
    public ActivityRepository activityRepository() {
        return mockRepository(ActivityRepository.class);
    }

    @Bean
    public AttachmentRepository attachmentRepository() {
        return mockRepository(AttachmentRepository.class);
    }

    @Bean
    public CacheEventRepository cacheEventRepository() {
        return mockRepository(CacheEventRepository.class);
    }

    @Bean
    public CommandEventRepository commandEventRepository() {
        return mockRepository(CommandEventRepository.class);
    }

    @Bean
    public ContentRepository contentRepository() {
        ContentRepository mock = Mockito.mock(ContentRepository.class);
        return mock;
    }

    @Bean
    public DeploymentRepository deploymentRepository() {
        return mockRepository(DeploymentRepository.class);
    }

    @Bean
    public MongoTemplate mongoOperations() {
        return Mockito.mock(MongoTemplate.class);
    }

    @Bean
     public ProcessRepository processRepository() {
        return mockRepository(ProcessRepository.class);
    }

    @Bean
    public ProcessInstanceRepository processInstanceRepository() {
        return mockRepository(ProcessInstanceRepository.class);
    }

    @Bean
    public RequestRepository requestRepository() {
        return mockRepository(RequestRepository.class);
    }

    @Bean
    public SubmissionRepository submissionRepository() {
        return mockRepository(SubmissionRepository.class);
    }

    @Bean
    public ValidationRepository validationRepository() {
        return mockRepository(ValidationRepository.class);
    }

    @Bean
    public BucketListRepository bucketListRepository() {
        return mockRepository(BucketListRepository.class);
    } 

    private static <R extends MongoRepository> R mockRepository(Class<R> cls) {
        R mock = Mockito.mock(cls);
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArguments()[0];
            }
        }).when(mock).save(any(cls));
        return mock;
    }

}
