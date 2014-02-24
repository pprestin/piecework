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

import org.junit.Before;
import org.mockito.Spy;
import piecework.command.CommandListener;
import piecework.command.AbstractOperationCommand;
import piecework.enumeration.StateChangeType;
import piecework.model.Process;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import piecework.model.ProcessInstance;
import piecework.model.Task;
import piecework.persistence.ProcessInstanceProvider;
import piecework.persistence.test.ProcessInstanceProviderStub;

import java.util.HashSet;
import java.util.Set;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class MediatorTest {

    @InjectMocks
    Mediator mediator;

    @Spy
    Set<CommandListener> commandListeners = new HashSet<CommandListener>();

    @Mock
    CommandListener testCommandListener;

    @Mock
    CommandListener dudCommandListener;

    @Spy
    Set<EventListener> eventListeners = new HashSet<EventListener>();

    @Mock
    EventListener testEventListener;

    @Mock
    EventListener dudEventListener;

    @Mock
    Process process;

    @Mock
    ProcessInstance instance;

    @Mock
    Task task;

    @Mock
    AbstractOperationCommand command;

    // Need two process definition keys for listeners so we can check that only
    // the first one receives events (i.e. that the second one doesn't inappropriately
    // receive events that are only meant for the first one)
    private final static String TEST_PROCESS_DEFINITION_KEY = "TEST";
    private final static String DUD_PROCESS_DEFINITION_KEY = "DUD";

    @Before
    public void setup() throws Exception {
        // This will be the key for the events that get sent
        Mockito.when(process.getProcessDefinitionKey()).thenReturn(TEST_PROCESS_DEFINITION_KEY);
    }

    @Test
    public void testBefore() throws Exception {
        Mockito.when(command.getProcessDefinitionKey()).thenReturn(TEST_PROCESS_DEFINITION_KEY);
        Mockito.when(testCommandListener.getProcessDefinitionKey()).thenReturn(TEST_PROCESS_DEFINITION_KEY);
        Mockito.when(dudCommandListener.getProcessDefinitionKey()).thenReturn(DUD_PROCESS_DEFINITION_KEY);
        commandListeners.add(testCommandListener);
        commandListeners.add(dudCommandListener);
        // Ensure that initialization happens - normally Spring would take care of this
        mediator.init();

        // Run the mediator before method
        mediator.before(command);

        // Verify that the test command listener got called
        Mockito.verify(testCommandListener).before(command);

        // Verify that the dud command listener didn't get called
        Mockito.verify(dudCommandListener, Mockito.never()).before(command);
    }

    @Test
    public void testNotifyOnTaskCreated() throws Exception {
        Mockito.when(testEventListener.getProcessDefinitionKey()).thenReturn(TEST_PROCESS_DEFINITION_KEY);
        Mockito.when(dudEventListener.getProcessDefinitionKey()).thenReturn(DUD_PROCESS_DEFINITION_KEY);
        eventListeners.add(testEventListener);
        eventListeners.add(dudEventListener);
        // Ensure that initialization happens - normally Spring would take care of this
        mediator.init();

        // Build a state change event
        ProcessInstanceProvider instanceProvider = new ProcessInstanceProviderStub(process, null, instance, null);

        StateChangeEvent event = new StateChangeEvent.Builder(StateChangeType.CREATE_TASK)
                .instanceProvider(instanceProvider)
                .task(task)
                .build();

        // Run the mediator notify method
        mediator.notify(event);

        // Verify that the test event listener got called
        Mockito.verify(testEventListener).notify(event);

        // Verify that the dud event listener didn't get called
        Mockito.verify(dudEventListener, Mockito.never()).notify(event);
    }

}
