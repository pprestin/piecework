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
import piecework.model.*;
import piecework.model.Process;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceAuthorityTest {

    @Mock
    private piecework.model.Process testprocess1;

    @Mock
    private Process testprocess2;

    private ResourceAuthority userResourceAuthority;

    private ResourceAuthority overseerResourceAuthority;

    @Before
    public void setup() {
        this.userResourceAuthority = new ResourceAuthority.Builder()
                        .processDefinitionKey("TESTPROCESS1")
                        .role(AuthorizationRole.USER)
                        .build();

        this.overseerResourceAuthority = new ResourceAuthority.Builder()
                        .processDefinitionKey("TESTPROCESS2")
                        .role(AuthorizationRole.OVERSEER)
                        .build();

        Mockito.when(testprocess1.getProcessDefinitionKey()).thenReturn("TESTPROCESS1");
        Mockito.when(testprocess2.getProcessDefinitionKey()).thenReturn("TESTPROCESS2");
    }

    @Test
    public void userResourceAuthorityHasUserRoleOnlyForProcess1() {
        Assert.assertTrue(userResourceAuthority.hasRole(testprocess1, Sets.newHashSet(AuthorizationRole.USER)));
        Assert.assertFalse(userResourceAuthority.hasRole(testprocess1, Sets.newHashSet(AuthorizationRole.OVERSEER)));
        Assert.assertFalse(userResourceAuthority.hasRole(testprocess2, Sets.newHashSet(AuthorizationRole.USER)));
        Assert.assertFalse(userResourceAuthority.hasRole(testprocess2, Sets.newHashSet(AuthorizationRole.OVERSEER)));
    }

    @Test
    public void overseerResourceAuthorityHasOverseerRoleOnlyForProcess2() {
        Assert.assertTrue(overseerResourceAuthority.hasRole(testprocess2, Sets.newHashSet(AuthorizationRole.OVERSEER)));
        Assert.assertFalse(overseerResourceAuthority.hasRole(testprocess2, Sets.newHashSet(AuthorizationRole.USER)));
        Assert.assertFalse(overseerResourceAuthority.hasRole(testprocess1, Sets.newHashSet(AuthorizationRole.OVERSEER)));
        Assert.assertFalse(overseerResourceAuthority.hasRole(testprocess1, Sets.newHashSet(AuthorizationRole.USER)));
    }

    @Test
    public void userResourceAuthorityIsOnlyAuthorizedForProcess1AsUser() {
        Assert.assertTrue(userResourceAuthority.isAuthorized(AuthorizationRole.USER, "TESTPROCESS1"));
        Assert.assertFalse(userResourceAuthority.isAuthorized(AuthorizationRole.USER, "TESTPROCESS2"));
        Assert.assertFalse(userResourceAuthority.isAuthorized(AuthorizationRole.OVERSEER, "TESTPROCESS1"));
        Assert.assertFalse(userResourceAuthority.isAuthorized(AuthorizationRole.OVERSEER, "TESTPROCESS2"));
        Assert.assertFalse(userResourceAuthority.isAuthorized(AuthorizationRole.SUPERUSER, "TESTPROCESS1"));
    }

    @Test
    public void overseerResourceAuthorityIsOnlyAuthorizedForProcess1AsUser() {
        Assert.assertTrue(overseerResourceAuthority.isAuthorized(AuthorizationRole.OVERSEER, "TESTPROCESS2"));
        Assert.assertFalse(overseerResourceAuthority.isAuthorized(AuthorizationRole.USER, "TESTPROCESS2"));
        Assert.assertFalse(overseerResourceAuthority.isAuthorized(AuthorizationRole.USER, "TESTPROCESS1"));
        Assert.assertFalse(overseerResourceAuthority.isAuthorized(AuthorizationRole.OVERSEER, "TESTPROCESS1"));
        Assert.assertFalse(overseerResourceAuthority.isAuthorized(AuthorizationRole.SUPERUSER, "TESTPROCESS1"));
    }

}
