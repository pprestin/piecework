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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import piecework.model.Process;

import java.util.Set;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class AccessAuthorityTest {

    private AccessAuthority accessAuthority;

    @Mock
    private Process testprocess1;

    @Mock
    private Process testprocess2;

    @Before
    public void setup() {
        this.accessAuthority = new AccessAuthority.Builder()
                .groupId("TESTGROUP1")
                .groupId("TESTGROUP2")
                .resourceAuthority(new ResourceAuthority.Builder()
                        .processDefinitionKey("TESTPROCESS1")
                        .role(AuthorizationRole.USER)
                        .build())
                .resourceAuthority(new ResourceAuthority.Builder()
                        .processDefinitionKey("TESTPROCESS2")
                        .role(AuthorizationRole.OVERSEER)
                        .build())
                .build();

        Mockito.when(testprocess1.getProcessDefinitionKey()).thenReturn("TESTPROCESS1");
        Mockito.when(testprocess2.getProcessDefinitionKey()).thenReturn("TESTPROCESS2");
    }


    @Test
    public void isAuthorizedForUserRoleTestProcess1() {
        Assert.assertTrue(accessAuthority.isAuthorized(AuthorizationRole.USER, "TESTPROCESS1"));
        Assert.assertFalse(accessAuthority.isAuthorized(AuthorizationRole.USER, "TESTPROCESS2"));
    }

    @Test
    public void isAuthorizedForOverseerRoleTestProcess2() {
        Assert.assertTrue(accessAuthority.isAuthorized(AuthorizationRole.OVERSEER, "TESTPROCESS2"));
        Assert.assertFalse(accessAuthority.isAuthorized(AuthorizationRole.OVERSEER, "TESTPROCESS1"));
    }

    @Test
    public void isNotAuthorizedForEitherRoleTestProcess3() {
        Assert.assertFalse(accessAuthority.isAuthorized(AuthorizationRole.USER, "TESTPROCESS3"));
        Assert.assertFalse(accessAuthority.isAuthorized(AuthorizationRole.OVERSEER, "TESTPROCESS3"));
    }

    @Test
    public void hasGroupOnlyForTestGroup1And2() {
        Assert.assertTrue(accessAuthority.hasGroup(Sets.newHashSet("TESTGROUP1")));
        Assert.assertTrue(accessAuthority.hasGroup(Sets.newHashSet("TESTGROUP1", "TESTGROUP2")));
        Assert.assertTrue(accessAuthority.hasGroup(Sets.newHashSet("TESTGROUP1", "TESTGROUP2", "TESTGROUP3")));
        Assert.assertFalse(accessAuthority.hasGroup(Sets.newHashSet("TESTGROUP3")));
    }

    @Test
    public void hasUserRoleOnlyForTestProcess1() {
        Assert.assertTrue(accessAuthority.hasRole(testprocess1, Sets.newHashSet(AuthorizationRole.USER)));
        Assert.assertFalse(accessAuthority.hasRole(testprocess1, Sets.newHashSet(AuthorizationRole.OVERSEER)));
        Assert.assertTrue(accessAuthority.hasRole(testprocess1, Sets.newHashSet(AuthorizationRole.OVERSEER, AuthorizationRole.USER)));
        Assert.assertFalse(accessAuthority.hasRole(testprocess1, Sets.newHashSet(AuthorizationRole.SUPERUSER)));
    }

    @Test
    public void hasOverseerRoleOnlyForTestProcess1() {
        Assert.assertTrue(accessAuthority.hasRole(testprocess2, Sets.newHashSet(AuthorizationRole.OVERSEER)));
        Assert.assertFalse(accessAuthority.hasRole(testprocess2, Sets.newHashSet(AuthorizationRole.USER)));
        Assert.assertTrue(accessAuthority.hasRole(testprocess2, Sets.newHashSet(AuthorizationRole.OVERSEER, AuthorizationRole.USER)));
        Assert.assertFalse(accessAuthority.hasRole(testprocess2, Sets.newHashSet(AuthorizationRole.SUPERUSER)));
    }

    @Test
    public void getProcessDefinitionKeysForUserRoleOnlyIncludesProcess1() {
        Set<String> processDefinitionKeys = accessAuthority.getProcessDefinitionKeys(Sets.newHashSet(AuthorizationRole.USER));
        Assert.assertEquals(1, processDefinitionKeys.size());
        Assert.assertEquals("TESTPROCESS1", processDefinitionKeys.iterator().next());
    }

    @Test
    public void getProcessDefinitionKeysForOverseerRoleOnlyIncludesProcess2() {
        Set<String> processDefinitionKeys = accessAuthority.getProcessDefinitionKeys(Sets.newHashSet(AuthorizationRole.OVERSEER));
        Assert.assertEquals(1, processDefinitionKeys.size());
        Assert.assertEquals("TESTPROCESS2", processDefinitionKeys.iterator().next());
    }

    @Test
    public void getProcessDefinitionKeysForBothRolesIncludesBothProcesses() {
        Set<String> processDefinitionKeys = accessAuthority.getProcessDefinitionKeys(Sets.newHashSet(AuthorizationRole.USER, AuthorizationRole.OVERSEER));
        Assert.assertEquals(2, processDefinitionKeys.size());
        Assert.assertTrue(processDefinitionKeys.contains("TESTPROCESS1"));
        Assert.assertTrue(processDefinitionKeys.contains("TESTPROCESS2"));
    }

    @Test
    public void getProcessDefinitionKeysForSuperuserRoleIncludesNoProcesses() {
        Set<String> processDefinitionKeys = accessAuthority.getProcessDefinitionKeys(Sets.newHashSet(AuthorizationRole.SUPERUSER));
        Assert.assertEquals(0, processDefinitionKeys.size());
    }

}
