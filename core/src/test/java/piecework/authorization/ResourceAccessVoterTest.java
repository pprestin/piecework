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
import junit.framework.Assert;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import piecework.model.Submission;
import piecework.resource.ProcessInstanceApiResource;
import piecework.resource.ProcessInstanceResource;

import java.util.Collection;

/**
 * Basically these tests should just prove equivalence between the access authority isAuthorized()
 * call and the vote -- so if access authority says no, it's denied, if it says yes, access should
 * be granted.
 *
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceAccessVoterTest {

    @Mock
    AccessAuthority accessAuthority;

    @Mock
    TestingAuthenticationToken authentication;

    @Mock
    MethodInvocation methodInvocation;

    @Before
    public void setup() throws Exception {
        Collection<GrantedAuthority> authorities = Sets.newHashSet((GrantedAuthority) accessAuthority);

        Mockito.when(authentication.getAuthorities())
                .thenReturn(authorities);
        Mockito.when(methodInvocation.getArguments())
                .thenReturn(new Object[] {null, "TESTPROCESS1", null});
        Mockito.when(methodInvocation.getMethod())
                .thenReturn(ProcessInstanceApiResource.class.getMethod("create", MessageContext.class, String.class, Submission.class));
    }

    @Test
    public void testGrantAccess() {
        Mockito.when(accessAuthority.isAuthorized(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Boolean.TRUE);

        Collection<ConfigAttribute> attributes = (Collection<ConfigAttribute>)Sets.newHashSet((ConfigAttribute)new SecurityConfig("ROLE_"));
        ResourceAccessVoter voter = new ResourceAccessVoter();
        int result = voter.vote(authentication, methodInvocation, attributes);
        Assert.assertEquals(AccessDecisionVoter.ACCESS_GRANTED, result);
    }

    @Test
    public void testDenyAccess() {
        Mockito.when(accessAuthority.isAuthorized(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Boolean.FALSE);

        Collection<ConfigAttribute> attributes = (Collection<ConfigAttribute>)Sets.newHashSet((ConfigAttribute)new SecurityConfig("ROLE_"));
        ResourceAccessVoter voter = new ResourceAccessVoter();
        int result = voter.vote(authentication, methodInvocation, attributes);
        Assert.assertEquals(AccessDecisionVoter.ACCESS_DENIED, result);
    }

    @Test
    public void testAbstain() {
        // Force the voter to abstain by passing it a config attribute it doesn't support
        Mockito.when(accessAuthority.isAuthorized(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Boolean.FALSE);

        Collection<ConfigAttribute> attributes = (Collection<ConfigAttribute>)Sets.newHashSet((ConfigAttribute)new SecurityConfig("DUD_"));
        ResourceAccessVoter voter = new ResourceAccessVoter();
        int result = voter.vote(authentication, methodInvocation, attributes);
        Assert.assertEquals(AccessDecisionVoter.ACCESS_ABSTAIN, result);
    }

}
