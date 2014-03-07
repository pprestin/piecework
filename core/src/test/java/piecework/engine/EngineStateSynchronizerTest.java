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
package piecework.engine;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import piecework.SystemUser;
import piecework.enumeration.ActionType;
import piecework.enumeration.StateChangeType;
import piecework.model.Process;
import piecework.model.ProcessInstance;
import piecework.model.Task;
import piecework.persistence.ModelProviderFactory;
import piecework.persistence.ProcessInstanceProvider;
import piecework.service.NotificationService;
import piecework.service.ProcessInstanceService;
import piecework.service.TaskService;
import piecework.settings.UserInterfaceSettings;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class EngineStateSynchronizerTest {

    @Mock
    EngineContext engineContext;

    @Mock
    EngineTask engineTask;

    @InjectMocks
    EngineStateSynchronizer engineStateSynchronizer;

    @Mock
    Mediator mediator;

    @Mock
    ModelProviderFactory modelProviderFactory;

    @Mock
    ProcessInstanceService processInstanceService;

    @Mock
    TaskService taskService;

    @Mock
    NotificationService notificationService;

    @Mock
    UserInterfaceSettings settings;

    @Mock
    Process process;

    @Mock
    ProcessInstance instance;

    @Mock
    ProcessInstanceProvider instanceProvider;

    @Test
    public void verifyStartProcess() {
        engineStateSynchronizer.onProcessInstanceEvent(StateChangeType.START_PROCESS, "123", engineContext);
        Mockito.verify(mediator).notify(any(StateChangeEvent.class));
    }

    @Test
    public void verifyCompleteProcess() {
        Mockito.doReturn(instance)
               .when(processInstanceService).complete(eq("123"), any(SystemUser.class));
        engineStateSynchronizer.onProcessInstanceEvent(StateChangeType.COMPLETE_PROCESS, "123", engineContext);
        Mockito.verify(mediator).notify(any(StateChangeEvent.class));
    }

    @Test
    public void verifyCreateTask() throws Exception {
        Mockito.doReturn("TEST")
               .when(instance).getProcessDefinitionKey();
        Mockito.doReturn("TEST")
                .when(engineTask).getProcessDefinitionKey();
        Mockito.doReturn("123")
               .when(instance).getProcessInstanceId();
        Mockito.doReturn("123")
               .when(engineTask).getProcessInstanceId();
        Mockito.doReturn("review")
               .when(engineTask).getTaskDefinitionKey();
        Mockito.doReturn(ActionType.COMPLETE)
               .when(engineTask).getActionType();
        Mockito.doReturn(instanceProvider)
               .when(modelProviderFactory).instanceProvider(eq("TEST"), eq("123"), any(SystemUser.class));
        Mockito.doReturn(instance)
               .when(instanceProvider).instance();
        Mockito.doReturn(process)
                .when(instanceProvider).process();
        Mockito.doReturn(Boolean.TRUE)
                .when(taskService).update(anyString(), any(Task.class));
        engineStateSynchronizer.onTaskEvent(StateChangeType.CREATE_TASK, engineTask, engineContext);
        Mockito.verify(mediator).notify(any(StateChangeEvent.class));
    }

}
