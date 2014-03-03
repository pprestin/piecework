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
package piecework.persistence.concrete;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.cache.Cache;
import piecework.enumeration.CacheName;
import piecework.exception.GoneError;
import piecework.exception.NotFoundError;
import piecework.model.Process;
import piecework.exception.PieceworkException;
import piecework.model.User;
import piecework.persistence.ProcessProvider;
import piecework.service.CacheService;
import piecework.test.ProcessFactory;

import static org.mockito.Matchers.eq;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class CachingProcessProviderTest {

    @Mock
    CacheService mockCacheService;

    @Mock
    ProcessProvider mockProcessProvider;

    @Mock
    Process mockCachedProcess;

    @Mock
    Process mockPersistedProcess;

    @Mock
    Cache.ValueWrapper valueWrapper;

    @Mock
    User principal;

    private Process cachedProcess;

    @Before
    public void setup() throws PieceworkException {
        Mockito.doReturn(principal)
                .when(mockProcessProvider).principal();
        Mockito.doReturn(mockPersistedProcess)
               .when(mockProcessProvider).process();
        Mockito.doReturn(valueWrapper)
                .when(mockCacheService).get(eq(CacheName.PROCESS), eq("CACHED-TEST"));

        cachedProcess = new Process.Builder()
                .processDefinitionKey("CACHED-TEST")
                .build();
    }

    @Test(expected = NotFoundError.class)
    public void verifyNotFoundOnCachedNull() throws PieceworkException {
        Mockito.doReturn("CACHED-TEST")
               .when(mockProcessProvider).processDefinitionKey();
        Mockito.doReturn(null)
               .when(valueWrapper).get();

        ProcessProvider processProvider = new CachingProcessProvider(mockCacheService, mockProcessProvider);
        processProvider.process();
    }

    @Test(expected = GoneError.class)
    public void verifyNotFoundOnCachedDeleted() throws PieceworkException {
        Mockito.doReturn(true)
                .when(mockCachedProcess).isDeleted();
        Mockito.doReturn("CACHED-TEST")
                .when(mockProcessProvider).processDefinitionKey();
        Mockito.doReturn(mockCachedProcess)
                .when(valueWrapper).get();

        ProcessProvider processProvider = new CachingProcessProvider(mockCacheService, mockProcessProvider);
        processProvider.process();
    }

    @Test
    public void verifyCachedReturned() throws PieceworkException {
        Mockito.doReturn("CACHED-TEST")
                .when(mockProcessProvider).processDefinitionKey();
        Mockito.doReturn(cachedProcess)
                .when(valueWrapper).get();

        ProcessProvider processProvider = new CachingProcessProvider(mockCacheService, mockProcessProvider);
        Process process = processProvider.process();
        Assert.assertEquals(cachedProcess, process);
    }

    @Test
    public void verifyNotCachedReturned() throws PieceworkException {
        Mockito.doReturn("TEST")
                .when(mockProcessProvider).processDefinitionKey();
        Mockito.doReturn(cachedProcess)
                .when(valueWrapper).get();

        ProcessProvider processProvider = new CachingProcessProvider(mockCacheService, mockProcessProvider);
        Process process = processProvider.process();
        Assert.assertEquals(mockPersistedProcess, process);
    }

    @Test
    public void verifyWithViewContext() throws PieceworkException {
        Mockito.doReturn("CACHED-TEST")
                .when(mockProcessProvider).processDefinitionKey();
        Mockito.doReturn(cachedProcess)
                .when(valueWrapper).get();
        ProcessProvider processProvider = new CachingProcessProvider(mockCacheService, mockProcessProvider);
        Process process = processProvider.process(ProcessFactory.viewContext());
        Assert.assertEquals("https://somehost.org/piecework/ui/process/CACHED-TEST", process.getLink());
    }

    @Test
    public void verifyProcessDefinitionKey() throws PieceworkException {
        Mockito.doReturn("CACHED-TEST")
                .when(mockProcessProvider).processDefinitionKey();
        Mockito.doReturn(cachedProcess)
                .when(valueWrapper).get();
        ProcessProvider provider = new CachingProcessProvider(mockCacheService, mockProcessProvider);
        Assert.assertEquals("CACHED-TEST", provider.processDefinitionKey());
    }

    @Test
    public void verifyPrincipal() throws PieceworkException {
        Mockito.doReturn("CACHED-TEST")
                .when(mockProcessProvider).processDefinitionKey();
        Mockito.doReturn(cachedProcess)
                .when(valueWrapper).get();
        ProcessProvider provider = new CachingProcessProvider(mockCacheService, mockProcessProvider);
        Assert.assertEquals(principal, provider.principal());
    }

}
