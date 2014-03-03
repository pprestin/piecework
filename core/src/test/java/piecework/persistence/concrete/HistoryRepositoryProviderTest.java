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

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import piecework.common.ViewContext;
import piecework.content.ContentResource;
import piecework.engine.ProcessEngineFacade;
import piecework.enumeration.OperationType;
import piecework.exception.NotFoundError;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.HistoryProvider;
import piecework.persistence.ProcessProvider;
import piecework.repository.*;
import piecework.service.IdentityService;
import piecework.test.ProcessFactory;

import java.util.Date;
import java.util.Set;

import static org.mockito.Matchers.eq;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class HistoryRepositoryProviderTest {

    private static final String PROCESS_DEFINITION_KEY = "Test";
    private static final String TEST_INSTANCE_ID = "98765";

    @Mock
    ProcessEngineFacade facade;

    @Mock
    ProcessRepository processRepository;

    @Mock
    ProcessInstanceRepository processInstanceRepository;

    @Mock
    AttachmentRepository attachmentRepository;

    @Mock
    ContentRepository contentRepository;

    @Mock
    ContentResource contentResource;

    @Mock
    DeploymentRepository deploymentRepository;

    @Mock
    IdentityService identityService;

    @Mock
    Entity principal;

    @Before
    public void setup() {
        ProcessDeployment deployment = ProcessFactory.multistepProcessDeployment("1234");
        Process process = ProcessFactory.process(PROCESS_DEFINITION_KEY, "Some Test Process", deployment);

        ProcessInstance instance = new ProcessInstance.Builder()
                .processInstanceId(TEST_INSTANCE_ID)
                .processInstanceLabel("A Specific Instance")
                .processDefinitionKey(PROCESS_DEFINITION_KEY)
                .task(new Task.Builder()
                        .taskInstanceId("23456")
                        .assigneeId("testuser")
                        .active()
                        .build())
                .startTime(new Date(1393780082262l))
                .endTime(new Date(1393780085262l))
                .operation("99234", OperationType.SUSPENSION, "Because", new Date(), "testuser")
                .build();

        Mockito.doReturn(process)
               .when(processRepository).findOne(eq(PROCESS_DEFINITION_KEY));

        Mockito.doReturn(instance)
               .when(processInstanceRepository).findOne(eq(TEST_INSTANCE_ID));
    }

    @Test
    public void verifyHistory() throws Exception {
        ProcessProvider processProvider = new ProcessRepositoryProvider(processRepository, PROCESS_DEFINITION_KEY, principal);
        HistoryProvider historyProvider = new HistoryRepositoryProvider(processProvider, processInstanceRepository, facade, attachmentRepository, contentRepository, deploymentRepository, identityService, TEST_INSTANCE_ID);

        History history = historyProvider.history(ProcessFactory.viewContext());
        Assert.assertEquals("Some Test Process", history.getProcessDefinitionLabel());
        Assert.assertEquals("A Specific Instance", history.getProcessInstanceLabel());
        Assert.assertEquals("https://somehost.org/piecework/ui/instance/Test/98765/history", history.getLink());
        Assert.assertEquals(1393780082262l, history.getStartTime().getTime());
        Assert.assertEquals(1393780085262l, history.getEndTime().getTime());

        Set<Event> events = history.getEvents();
        Assert.assertEquals(2, events.size());
    }

    @Test(expected = NotFoundError.class)
    public void verifyHistoryNullIfUnmatchedInstanceId() throws Exception {
        ProcessProvider processProvider = new ProcessRepositoryProvider(processRepository, PROCESS_DEFINITION_KEY, principal);
        HistoryProvider historyProvider = new HistoryRepositoryProvider(processProvider, processInstanceRepository, facade, attachmentRepository, contentRepository, deploymentRepository, identityService, "000");

        History history = historyProvider.history(ProcessFactory.viewContext());
        Assert.assertNull(history);
    }


}
