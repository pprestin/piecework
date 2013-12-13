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
package piecework.authorization;

import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import piecework.model.Authorization;
import piecework.persistence.AuthorizationRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthorizationRoleMapperTest {

    @InjectMocks
    private AuthorizationRoleMapper authorizationRoleMapper;

    @Mock
    private AuthorizationRepository authorizationRepository;

    @Mock
    private Authorization testGroup1Authorization;

    @Mock
    private Authorization testGroup2Authorization;

    @Mock
    private ResourceAuthority resourceAuthority1;

    @Mock
    private ResourceAuthority resourceAuthority2;

    @Before
    public void setup() {
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

        Mockito.when(authorizationRepository.findAll(Sets.newHashSet("ROLE_TESTGROUP1")))
                .thenReturn(Sets.newHashSet(testGroup1Authorization));

        Mockito.when(authorizationRepository.findAll(Sets.newHashSet("ROLE_TESTGROUP2")))
                .thenReturn(Sets.newHashSet(testGroup2Authorization));

        Mockito.when(authorizationRepository.findAll(Sets.newHashSet("ROLE_TESTGROUP1", "ROLE_TESTGROUP2")))
                .thenReturn(Sets.newHashSet(testGroup1Authorization, testGroup2Authorization));
    }

    @Test
    public void mapAuthoritiesForTestGroup1() {
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_TESTGROUP1");
        Collection<? extends GrantedAuthority> authorities = authorizationRoleMapper.mapAuthorities(Sets.newHashSet(grantedAuthority));
        Assert.assertEquals(1, authorities.size());
        GrantedAuthority authority = authorities.iterator().next();
        Assert.assertTrue(authority instanceof AccessAuthority);
        AccessAuthority accessAuthority = AccessAuthority.class.cast(authority);
        Set<String> processDefinitionKeys = accessAuthority.getProcessDefinitionKeys(Sets.newHashSet(AuthorizationRole.USER));
        Assert.assertEquals(1, processDefinitionKeys.size());
        Assert.assertTrue(processDefinitionKeys.contains("TESTPROCESS1"));
    }

    @Test
    public void mapAuthoritiesForTestGroup2() {
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_TESTGROUP2");
        Collection<? extends GrantedAuthority> authorities = authorizationRoleMapper.mapAuthorities(Sets.newHashSet(grantedAuthority));
        Assert.assertEquals(1, authorities.size());
        GrantedAuthority authority = authorities.iterator().next();
        Assert.assertTrue(authority instanceof AccessAuthority);
        AccessAuthority accessAuthority = AccessAuthority.class.cast(authority);
        Set<String> processDefinitionKeys = accessAuthority.getProcessDefinitionKeys(Sets.newHashSet(AuthorizationRole.USER));
        Assert.assertEquals(1, processDefinitionKeys.size());
        Assert.assertTrue(processDefinitionKeys.contains("TESTPROCESS2"));
    }

}
