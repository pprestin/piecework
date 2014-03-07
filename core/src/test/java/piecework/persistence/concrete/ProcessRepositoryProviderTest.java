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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import piecework.exception.*;
import piecework.model.Entity;
import piecework.model.Process;
import piecework.persistence.ProcessProvider;
import piecework.repository.ProcessRepository;
import piecework.test.ProcessFactory;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessRepositoryProviderTest {

    @Mock
    ProcessRepository processRepository;

    @Mock
    Entity principal;

    @Test(expected = BadRequestError.class)
    public void verifyErrorOnNullKey() throws PieceworkException {
        ProcessProvider provider = new ProcessRepositoryProvider(processRepository, null, principal);
        provider.process();
    }

    @Test(expected = BadRequestError.class)
    public void verifyErrorOnEmptyKey() throws PieceworkException {
        ProcessProvider provider = new ProcessRepositoryProvider(processRepository, "", principal);
        provider.process();
    }

    @Test(expected = NotFoundError.class)
    public void verifyErrorOnNotFound() throws PieceworkException {
        Process mockProcess = Mockito.mock(Process.class);
        Mockito.doReturn(mockProcess)
                .when(processRepository).findOne(eq("TEST-1"));
        ProcessProvider provider = new ProcessRepositoryProvider(processRepository, "TEST-2", principal);
        provider.process();
    }

    @Test(expected = GoneError.class)
    public void verifyErrorOnDeleted() throws PieceworkException {
        Process mockProcess = Mockito.mock(Process.class);
        Mockito.doReturn(true)
                .when(mockProcess).isDeleted();
        Mockito.doReturn(mockProcess)
                .when(processRepository).findOne(eq("TEST"));
        ProcessProvider provider = new ProcessRepositoryProvider(processRepository, "TEST", principal);
        provider.process();
    }

    @Test(expected = GoneError.class)
    public void verifyErrorOnDeletedSecondCall() throws PieceworkException {
        Process mockProcess = Mockito.mock(Process.class);
        Mockito.doReturn(true)
                .when(mockProcess).isDeleted();
        Mockito.doReturn(mockProcess)
                .when(processRepository).findOne(eq("TEST"));
        ProcessProvider provider = new ProcessRepositoryProvider(processRepository, "TEST", principal);
        try {
            provider.process();
        } catch (GoneError e) {
            // Ignore first call;
        }
        provider.process();
    }

    @Test
    public void verifyFindOneCalled() throws PieceworkException {
        Process mockProcess = Mockito.mock(Process.class);
        Mockito.doReturn(mockProcess)
                .when(processRepository).findOne(eq("TEST"));
        ProcessProvider provider = new ProcessRepositoryProvider(processRepository, "TEST", principal);
        provider.process();
        Mockito.verify(processRepository, times(1)).findOne(eq("TEST"));
    }

    @Test
    public void verifyFindOneCalledOnlyOnce() throws PieceworkException {
        Process mockProcess = Mockito.mock(Process.class);
        Mockito.doReturn(mockProcess)
               .when(processRepository).findOne(eq("TEST"));
        ProcessProvider provider = new ProcessRepositoryProvider(processRepository, "TEST", principal);
        Process first = provider.process();
        Process second = provider.process();
        Mockito.verify(processRepository, times(1)).findOne(eq("TEST"));
        Assert.assertEquals(first, second);
    }

    @Test
    public void verifyWithViewContext() throws PieceworkException {
        Process process = new Process.Builder()
                .processDefinitionKey("TEST")
                .build();
        Mockito.doReturn(process)
                .when(processRepository).findOne(eq("TEST"));
        ProcessProvider provider = new ProcessRepositoryProvider(processRepository, "TEST", principal);
        Process stored = provider.process(ProcessFactory.viewContext());
        Assert.assertEquals("https://somehost.org/piecework/ui/process/TEST", stored.getLink());
    }

    @Test
    public void verifyProcessDefinitionKey() throws PieceworkException {
        Process process = new Process.Builder()
                .processDefinitionKey("TEST")
                .build();
        Mockito.doReturn(process)
                .when(processRepository).findOne(eq("TEST"));
        ProcessProvider provider = new ProcessRepositoryProvider(processRepository, process.getProcessDefinitionKey(), principal);
        Assert.assertEquals("TEST", provider.processDefinitionKey());
    }

    @Test
    public void verifyPrincipal() throws PieceworkException {
        Process mockProcess = Mockito.mock(Process.class);
        Mockito.doReturn(mockProcess)
                .when(processRepository).findOne(eq("TEST"));
        ProcessProvider provider = new ProcessRepositoryProvider(processRepository, "TEST", principal);
        Assert.assertEquals(principal, provider.principal());
    }

}
