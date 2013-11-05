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

import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import piecework.Constants;
import piecework.common.UuidGenerator;
import piecework.identity.IdentityHelper;
import piecework.service.IdentityService;
import piecework.persistence.ContentRepository;
import piecework.persistence.SubmissionRepository;
import piecework.security.EncryptionService;
import piecework.security.Sanitizer;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.validation.SubmissionTemplate;
import piecework.validation.SubmissionTemplateFactory;
import piecework.model.*;
import piecework.test.ExampleFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import java.util.List;

/**
 * @author James Renfro
 */
@Ignore
@RunWith(MockitoJUnitRunner.class)
public class SubmissionHandlerTest {

    @InjectMocks
    SubmissionHandler submissionHandler;

    @InjectMocks
    SubmissionTemplateFactory submissionTemplateFactory;

    @Mock
    ContentRepository contentRepository;

    @Mock
    EncryptionService encryptionService;

    @Mock
    IdentityHelper helper;

    @Mock
    Sanitizer sanitizer;

    @Mock
    SubmissionRepository submissionRepository;

    @Mock
    IdentityService userDetailsService;

    @Mock
    UuidGenerator uuidGenerator;

    @Mock
    HttpServletRequest servletRequest;

    piecework.model.Process process;
    String processInstanceId;

    @Before
    public void setUp() throws Exception {
        this.process = ExampleFactory.exampleProcess();
        this.processInstanceId = "123";
        Mockito.when(sanitizer.sanitize(Mockito.any(String.class))).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return String.class.cast(args[0]);
            }
        });
        Mockito.when(submissionRepository.save(Mockito.any(Submission.class))).thenAnswer(new Answer<Submission>() {
            @Override
            public Submission answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                Submission submission = Submission.class.cast(args[0]);
                return new Submission.Builder(submission, new PassthroughSanitizer(), false).submissionId("1").build();
            }
        });
    }

//    @Test
//    public void testHandleProcessInstanceObject() throws Exception {
//        MultivaluedMap<String, String> map = new MetadataMap<String, String>();
//
//        map.putSingle("employeeName", "Tester");
//
//        SubmissionTemplate template = submissionTemplateFactory.submissionTemplate(process, ExampleFactory.exampleContainer(Constants.ScreenTypes.WIZARD));
//        Submission actual = submissionHandler.handle(process, template, map);
//        Assert.assertNotNull(actual);
//
//        List<Value> values = actual.getData().get("employeeName");
//        Assert.assertEquals("Tester", values.get(0).getValue());
//    }

}
