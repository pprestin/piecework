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
package piecework.command;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import piecework.Constants;
import piecework.engine.ProcessEngineFacade;
import piecework.exception.PieceworkException;
import piecework.manager.StorageManager;
import piecework.model.*;
import piecework.persistence.ProcessInstanceProvider;
import piecework.persistence.test.ProcessInstanceProviderStub;
import piecework.security.data.DataFilterService;
import piecework.common.ManyMap;
import piecework.validation.Validation;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.eq;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class CompletionCommandTest {

    @Mock
    private DataFilterService dataFilterService;

    @Mock
    private Entity principal;

    @Mock
    private piecework.model.Process process;

    @Mock
    private ProcessDeployment deployment;

    @Mock
    private ProcessEngineFacade processEngineFacade;

    @Mock
    private ProcessInstance instance;

    @Mock
    private StorageManager storageManager;

    @Mock
    private Task task;

    @Mock
    private User assignee;

    @Mock
    private Validation validation;

    @Test
    public void testSaveAsCandidateOrAssignee() throws PieceworkException {

        Map<String, List<Value>> data = new ManyMap<String, Value>();
        data.put("TEST", Collections.singletonList(new Value("ok")));

        Mockito.when(instance.getData())
                .thenReturn(data);

        Mockito.when(storageManager.archive(instance, data, Constants.ProcessStatuses.COMPLETE))
                .thenReturn(instance);

        ProcessInstanceProvider instanceProvider = new ProcessInstanceProviderStub(process, deployment, instance, principal);
        CompletionCommand command = new CompletionCommand(null, instanceProvider);
        ProcessInstance actual = command.execute(processEngineFacade, storageManager);
        Mockito.verify(storageManager).archive(eq(instance), eq(data), eq(Constants.ProcessStatuses.COMPLETE));
        Assert.assertEquals(instance, actual);
    }
}
