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
package piecework.authorization.config;

import com.google.common.collect.Sets;
import org.mockito.Mockito;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import piecework.authorization.*;
import piecework.model.Authorization;
import piecework.repository.AuthorizationRepository;
import piecework.repository.config.MockRepositoryConfiguration;
import piecework.service.CacheService;

import java.util.Collections;

/**
 * @author James Renfro
 */
@Configuration
@Import(MockRepositoryConfiguration.class)
public class AuthorizationConfiguration {

    @Bean
    public AccessFactory accessFactory() {
        return new AccessFactory();
    }

    @Bean
    public AuthorizationRepository authorizationRepository() {

        ResourceAuthority resourceAuthority1 = Mockito.mock(ResourceAuthority.class);
        ResourceAuthority resourceAuthority2 = Mockito.mock(ResourceAuthority.class);

        Authorization testGroup1Authorization = new Authorization.Builder()
                .authorizationId("ROLE_TESTGROUP1")
                .authority(resourceAuthority1)
                .build();

        Authorization testGroup2Authorization = new Authorization.Builder()
                .authorizationId("ROLE_TESTGROUP2")
                .authority(resourceAuthority2)
                .build();

        Mockito.when(resourceAuthority1.getRole())
                .thenReturn(AuthorizationRole.USER);

        Mockito.when(resourceAuthority1.getProcessDefinitionKeys())
                .thenReturn(Sets.newHashSet("TESTPROCESS1"));

        Mockito.when(resourceAuthority2.getRole())
                .thenReturn(AuthorizationRole.USER);

        Mockito.when(resourceAuthority2.getProcessDefinitionKeys())
                .thenReturn(Sets.newHashSet("TESTPROCESS2"));

//        Mockito.when(testGroup1Authorization.getAuthorities())
//                .thenReturn(Collections.singletonList(resourceAuthority1));
//
//        Mockito.when(testGroup2Authorization.getAuthorities())
//                .thenReturn(Collections.singletonList(resourceAuthority2));

        AuthorizationRepository authorizationRepository = Mockito.mock(AuthorizationRepository.class);
        Mockito.when(authorizationRepository.findAll(Sets.newHashSet("ROLE_TESTGROUP1")))
                .thenReturn(Sets.newHashSet(testGroup1Authorization));

        Mockito.when(authorizationRepository.findAll(Sets.newHashSet("ROLE_TESTGROUP2")))
                .thenReturn(Sets.newHashSet(testGroup2Authorization));

        Mockito.when(authorizationRepository.findAll(Sets.newHashSet("ROLE_TESTGROUP1", "ROLE_TESTGROUP2")))
                .thenReturn(Sets.newHashSet(testGroup1Authorization, testGroup2Authorization));

        return authorizationRepository;
    }

    @Bean
    public AuthorizationRoleMapper authorizationRoleMapper() {
        return new AuthorizationRoleMapper();
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }

    @Bean
    public CacheService cacheService() {
        return new CacheService();
    }

    @Bean
    public DefaultAuthorizationProvider defaultAuthorizationProvider() {
        return new DefaultAuthorizationProvider();
    }

}
