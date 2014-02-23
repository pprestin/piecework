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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import piecework.authorization.config.AuthorizationConfiguration;

import java.util.Collection;
import java.util.Set;

/**
 * @author James Renfro
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={AuthorizationConfiguration.class})
public class AuthorizationRoleMapperTest {

    @Autowired
    private AuthorizationRoleMapper authorizationRoleMapper;

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
