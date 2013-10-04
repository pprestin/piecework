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
package piecework.handler;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import piecework.enumeration.ActionType;
import piecework.model.*;
import piecework.model.Process;
import piecework.test.ExampleFactory;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class ScreenHandlerTest {

    @InjectMocks
    ScreenHandler screenHandler;

    Process process;

    @Before
    public void setUp() {
        process = ExampleFactory.simpleThreeInteractionProcess();
    }

    @Test
    public void testStartScreen() throws Exception {
        Screen startScreen = screenHandler.currentScreen(process, null);
        Assert.assertEquals("Start Screen", startScreen.getTitle());
    }

    @Test
    public void testStartConfirmationScreen() throws Exception {
        Screen startScreen = screenHandler.nextScreen(process, null, ActionType.COMPLETE);
        Assert.assertEquals("Start Confirmation Screen", startScreen.getTitle());
    }

    @Test
    public void testTask1Screen() throws Exception {
        Task task = Mockito.mock(Task.class);
        Mockito.when(task.getTaskDefinitionKey()).thenReturn("TASK1");
        Screen startScreen = screenHandler.currentScreen(process, task);
        Assert.assertEquals("Task 1 Screen", startScreen.getTitle());
    }

    @Test
    public void testTask1ConfirmationScreen() throws Exception {
        Task task = Mockito.mock(Task.class);
        Mockito.when(task.getTaskDefinitionKey()).thenReturn("TASK1");
        Screen startScreen = screenHandler.nextScreen(process, task, ActionType.COMPLETE);
        Assert.assertEquals("Task 1 Confirmation Screen", startScreen.getTitle());
    }

    @Test
    public void testTask1RejectionScreen() throws Exception {
        Task task = Mockito.mock(Task.class);
        Mockito.when(task.getTaskDefinitionKey()).thenReturn("TASK1");
        Screen startScreen = screenHandler.nextScreen(process, task, ActionType.REJECT);
        Assert.assertEquals("Task 1 Rejection Screen", startScreen.getTitle());
    }

}
