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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import piecework.enumeration.CacheName;
import piecework.model.Authorization;
import piecework.repository.AuthorizationRepository;
import piecework.service.CacheService;

import java.util.ArrayList;
import java.util.Collection;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultAuthorizationProviderTest {

    @InjectMocks
    DefaultAuthorizationProvider defaultAuthorizationProvider;

    @Mock
    AuthorizationRepository authorizationRepository;

    @Mock
    CacheService cacheService;

    private Authorization testAuthorization1, testAuthorization2, testAuthorization3;

    @Before
    public void setup() {
        this.testAuthorization1 = new Authorization.Builder()
                .authorizationId("testgroup1")
                .authority(new ResourceAuthority.Builder()
                                .processDefinitionKey("TEST1")
                                .role(AuthorizationRole.USER)
                                .build()).build();
        this.testAuthorization2 = new Authorization.Builder()
                .authorizationId("testgroup2")
                .authority(new ResourceAuthority.Builder()
                        .processDefinitionKey("TEST2")
                        .role(AuthorizationRole.ADMIN)
                        .build()).build();
        this.testAuthorization3 = new Authorization.Builder()
                .authorizationId("testgroup3")
                .authority(new ResourceAuthority.Builder()
                        .processDefinitionKey("TEST3")
                        .role(AuthorizationRole.OVERSEER)
                        .build()).build();
    }

    @Test
    public void verifyAllUncached() {
        Iterable<Authorization> authorizations = Sets.newHashSet(testAuthorization1, testAuthorization2, testAuthorization3);
        Mockito.doReturn(authorizations)
                .when(authorizationRepository).findAll(any(Iterable.class));

        Mockito.doReturn(null)
               .when(cacheService).get(eq(CacheName.AUTHORIZATIONS), anyString());

        Collection<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("testgroup1"));
        authorities.add(new SimpleGrantedAuthority("testgroup2"));
        authorities.add(new SimpleGrantedAuthority("testgroup3"));

        AccessAuthority accessAuthority = defaultAuthorizationProvider.authority(authorities);
        Assert.assertTrue(accessAuthority.isAuthorized(AuthorizationRole.USER, "TEST1"));
        Assert.assertTrue(accessAuthority.isAuthorized(AuthorizationRole.ADMIN, "TEST2"));
        Assert.assertTrue(accessAuthority.isAuthorized(AuthorizationRole.OVERSEER, "TEST3"));

        Assert.assertFalse(accessAuthority.isAuthorized(AuthorizationRole.ADMIN, "TEST1"));
        Assert.assertFalse(accessAuthority.isAuthorized(AuthorizationRole.USER, "TEST2"));
        Assert.assertFalse(accessAuthority.isAuthorized(AuthorizationRole.SUPERUSER, "TEST3"));
    }

    @Test
    public void verifyAllCached() {
        Mockito.doReturn(new SimpleValueWrapper(testAuthorization1))
                .when(cacheService).get(eq(CacheName.AUTHORIZATIONS), eq("testgroup1"));
        Mockito.doReturn(new SimpleValueWrapper(testAuthorization2))
                .when(cacheService).get(eq(CacheName.AUTHORIZATIONS), eq("testgroup2"));
        Mockito.doReturn(new SimpleValueWrapper(testAuthorization3))
                .when(cacheService).get(eq(CacheName.AUTHORIZATIONS), eq("testgroup3"));

        Collection<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("testgroup1"));
        authorities.add(new SimpleGrantedAuthority("testgroup2"));
        authorities.add(new SimpleGrantedAuthority("testgroup3"));

        AccessAuthority accessAuthority = defaultAuthorizationProvider.authority(authorities);
        Assert.assertTrue(accessAuthority.isAuthorized(AuthorizationRole.USER, "TEST1"));
        Assert.assertTrue(accessAuthority.isAuthorized(AuthorizationRole.ADMIN, "TEST2"));
        Assert.assertTrue(accessAuthority.isAuthorized(AuthorizationRole.OVERSEER, "TEST3"));

        Assert.assertFalse(accessAuthority.isAuthorized(AuthorizationRole.ADMIN, "TEST1"));
        Assert.assertFalse(accessAuthority.isAuthorized(AuthorizationRole.USER, "TEST2"));
        Assert.assertFalse(accessAuthority.isAuthorized(AuthorizationRole.SUPERUSER, "TEST3"));
    }

    @Test
    public void verifyCachedNull() {
        Mockito.doReturn(new SimpleValueWrapper(testAuthorization1))
                .when(cacheService).get(eq(CacheName.AUTHORIZATIONS), eq("testgroup1"));
        Mockito.doReturn(new SimpleValueWrapper(testAuthorization2))
                .when(cacheService).get(eq(CacheName.AUTHORIZATIONS), eq("testgroup2"));
        Mockito.doReturn(new SimpleValueWrapper(null))
                .when(cacheService).get(eq(CacheName.AUTHORIZATIONS), eq("testgroup3"));

        Collection<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("testgroup1"));
        authorities.add(new SimpleGrantedAuthority("testgroup2"));
        authorities.add(new SimpleGrantedAuthority("testgroup3"));

        AccessAuthority accessAuthority = defaultAuthorizationProvider.authority(authorities);
        Assert.assertTrue(accessAuthority.isAuthorized(AuthorizationRole.USER, "TEST1"));
        Assert.assertTrue(accessAuthority.isAuthorized(AuthorizationRole.ADMIN, "TEST2"));

        // If testgroup3 has a null value cached, then we would expect only this authorization to fail
        Assert.assertFalse(accessAuthority.isAuthorized(AuthorizationRole.OVERSEER, "TEST3"));

        Assert.assertFalse(accessAuthority.isAuthorized(AuthorizationRole.ADMIN, "TEST1"));
        Assert.assertFalse(accessAuthority.isAuthorized(AuthorizationRole.USER, "TEST2"));
        Assert.assertFalse(accessAuthority.isAuthorized(AuthorizationRole.SUPERUSER, "TEST3"));
    }

    @Test
    public void verifyOneCached() {
        Iterable<Authorization> authorizations = Sets.newHashSet(testAuthorization2, testAuthorization3);
        Mockito.doReturn(authorizations)
                .when(authorizationRepository).findAll(any(Iterable.class));

        Mockito.doReturn(new SimpleValueWrapper(testAuthorization1))
                .when(cacheService).get(eq(CacheName.AUTHORIZATIONS), eq("testgroup1"));

        Collection<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("testgroup1"));
        authorities.add(new SimpleGrantedAuthority("testgroup2"));
        authorities.add(new SimpleGrantedAuthority("testgroup3"));

        AccessAuthority accessAuthority = defaultAuthorizationProvider.authority(authorities);
        Assert.assertTrue(accessAuthority.isAuthorized(AuthorizationRole.USER, "TEST1"));
        Assert.assertTrue(accessAuthority.isAuthorized(AuthorizationRole.ADMIN, "TEST2"));
        Assert.assertTrue(accessAuthority.isAuthorized(AuthorizationRole.OVERSEER, "TEST3"));

        Assert.assertFalse(accessAuthority.isAuthorized(AuthorizationRole.ADMIN, "TEST1"));
        Assert.assertFalse(accessAuthority.isAuthorized(AuthorizationRole.USER, "TEST2"));
        Assert.assertFalse(accessAuthority.isAuthorized(AuthorizationRole.SUPERUSER, "TEST3"));
    }

    @Test
    public void verifyDoesCache() {
        Iterable<Authorization> authorizations = Sets.newHashSet(testAuthorization1);
        Mockito.doReturn(authorizations)
                .when(authorizationRepository).findAll(any(Iterable.class));

        Collection<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("testgroup1"));

        AccessAuthority accessAuthority = defaultAuthorizationProvider.authority(authorities);
        Assert.assertTrue(accessAuthority.isAuthorized(AuthorizationRole.USER, "TEST1"));
        Mockito.verify(cacheService).put(eq(CacheName.AUTHORIZATIONS), eq("testgroup1"), eq(testAuthorization1));

        accessAuthority = defaultAuthorizationProvider.authority(authorities);
        Assert.assertTrue(accessAuthority.isAuthorized(AuthorizationRole.USER, "TEST1"));

        Mockito.verify(cacheService, times(2)).get(eq(CacheName.AUTHORIZATIONS), eq("testgroup1"));
    }

}
