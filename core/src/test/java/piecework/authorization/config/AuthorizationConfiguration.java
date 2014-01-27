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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import piecework.authorization.*;
import piecework.model.Authorization;
import piecework.persistence.AuthorizationRepository;

import java.util.Collections;

/**
 * @author James Renfro
 */
@Configuration
public class AuthorizationConfiguration {

    @Bean
    public AccessFactory accessFactory() {
        return new AccessFactory();
    }

    @Bean
    public AuthorizationRepository authorizationRepository() {
        Authorization testGroup1Authorization = Mockito.mock(Authorization.class);
        Authorization testGroup2Authorization = Mockito.mock(Authorization.class);
        ResourceAuthority resourceAuthority1 = Mockito.mock(ResourceAuthority.class);
        ResourceAuthority resourceAuthority2 = Mockito.mock(ResourceAuthority.class);

        Mockito.when(resourceAuthority1.getRole())
                .thenReturn(AuthorizationRole.USER);

        Mockito.when(resourceAuthority1.getProcessDefinitionKeys())
                .thenReturn(Sets.newHashSet("TESTPROCESS1"));

        Mockito.when(resourceAuthority2.getRole())
                .thenReturn(AuthorizationRole.USER);

        Mockito.when(resourceAuthority2.getProcessDefinitionKeys())
                .thenReturn(Sets.newHashSet("TESTPROCESS2"));

        Mockito.when(testGroup1Authorization.getAuthorities())
                .thenReturn(Collections.singletonList(resourceAuthority1));

        Mockito.when(testGroup2Authorization.getAuthorities())
                .thenReturn(Collections.singletonList(resourceAuthority2));

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
    public DefaultAuthorizationProvider defaultAuthorizationProvider() {
        return new DefaultAuthorizationProvider();
    }

}
