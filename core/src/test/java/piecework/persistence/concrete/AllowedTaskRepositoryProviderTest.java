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

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import piecework.engine.ProcessEngineFacade;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.AllowedTaskProvider;
import piecework.persistence.ProcessInstanceProvider;
import piecework.persistence.ProcessProvider;
import piecework.repository.*;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.service.IdentityService;
import piecework.ui.streaming.StreamingAttachmentContent;

import java.io.IOException;

import static org.mockito.Matchers.eq;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class AllowedTaskRepositoryProviderTest {

    @Mock
    ProcessEngineFacade facade;

    @Mock
    IdentityService identityService;

    @Mock
    ProcessRepository processRepository;

    @Mock
    ProcessInstanceRepository processInstanceRepository;

    @Mock
    AttachmentRepository attachmentRepository;

    @Mock
    ContentRepository contentRepository;

    @Mock
    Content content;

    @Mock
    DeploymentRepository deploymentRepository;

    @Mock
    Entity principal;

    @Mock
    Process process;

    @Mock
    ProcessInstance instance;

    @Mock
    Task task;

    @Test
    public void verifyAttachmentContent() throws PieceworkException, IOException {
        Mockito.doReturn(process)
               .when(processRepository).findOne(eq("TEST"));
        Mockito.doReturn(instance)
               .when(processInstanceRepository).findOne(eq("1234"));

        ProcessProvider processProvider = new ProcessRepositoryProvider(processRepository, "TEST", principal);
        AllowedTaskProvider allowedTaskProvider = allowedTaskProvider(processProvider);

//        StreamingAttachmentContent attachment = allowedTaskProvider.attachment("233");
//        Assert.assertNotNull(attachment);
//        String expected = "This is some test data from an input stream";
//        String actual = IOUtils.toString(attachment.getContent().getInputStream());
//        Assert.assertEquals(expected, actual);
    }

    private AllowedTaskProvider allowedTaskProvider(ProcessProvider processProvider) {
        return new AllowedTaskRepositoryProvider(processProvider, processInstanceRepository, facade, attachmentRepository, contentRepository, deploymentRepository, identityService, new PassthroughSanitizer(), "1234");
    }

}
