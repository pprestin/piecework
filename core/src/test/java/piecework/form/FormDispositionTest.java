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
package piecework.form;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import piecework.common.ViewContext;
import piecework.enumeration.DataInjectionStrategy;
import piecework.model.*;
import piecework.model.Process;
import piecework.validation.Validation;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class FormDispositionTest {

    @Mock
    Action action;

    @Mock
    Process process;

    @Mock
    ProcessDeployment deployment;

    @Mock
    Explanation explanation;

    @Mock
    FormRequest request;

    @Mock
    Submission submission;

    @Mock
    Validation validation;

    private ViewContext context;

    private int count;

    @Before
    public void setup() {
        Mockito.doReturn("TEST")
                .when(process).getProcessDefinitionKey();
        Mockito.doReturn("classpath:/META-INF/some/base")
                .when(deployment).getBase();
        Mockito.doReturn("/this/is/the/path")
                .when(action).getLocation();
        Mockito.doReturn("https://some.remote.host.edu")
                .when(deployment).getRemoteHost();
        Mockito.doReturn("2345")
                .when(request).getRequestId();
        Mockito.doReturn("9803")
                .when(submission).getSubmissionId();
        Mockito.doReturn("5634")
                .when(request).getTaskId();
        Mockito.doReturn(submission)
                .when(validation).getSubmission();

        this.context = new ViewContext("http://localhost", "/piecework/ui", "/piecework/api", "/piecework/public", "v2");
    }

    @Test
    public void testCustomDisposition() {
        Mockito.doReturn(DataInjectionStrategy.DECORATE_HTML)
               .when(action).getStrategy();

        FormDisposition disposition = FormDisposition.Builder.build(process, deployment, action, context);

        Assert.assertEquals(FormDisposition.FormDispositionType.CUSTOM, disposition.getType());
        Assert.assertEquals("http://localhost", disposition.getHostUri().toString());
        Assert.assertEquals("http://localhost/piecework/ui/form/TEST", disposition.getPageUri().toString());
        Assert.assertEquals("classpath:/META-INF/some/base", disposition.getBase());
        Assert.assertEquals("/this/is/the/path", disposition.getPath());
        Assert.assertEquals(action, disposition.getAction());
    }

    @Test
    public void testRemoteDisposition() {
        Mockito.doReturn(DataInjectionStrategy.REMOTE)
                .when(action).getStrategy();

        FormDisposition disposition = FormDisposition.Builder.build(process, deployment, action, context);

        Assert.assertEquals(FormDisposition.FormDispositionType.REMOTE, disposition.getType());
        Assert.assertEquals("https://some.remote.host.edu", disposition.getHostUri().toString());
        Assert.assertEquals("https://some.remote.host.edu/this/is/the/path", disposition.getPageUri().toString());
        Assert.assertEquals("classpath:/META-INF/some/base", disposition.getBase());
        Assert.assertEquals("/this/is/the/path", disposition.getPath());
        Assert.assertEquals(action, disposition.getAction());
    }

    @Test
    public void testDefaultDisposition() {
        Mockito.doReturn(DataInjectionStrategy.NONE)
                .when(action).getStrategy();

        FormDisposition disposition = FormDisposition.Builder.build(process, deployment, action, context);

        Assert.assertEquals(FormDisposition.FormDispositionType.DEFAULT, disposition.getType());
        Assert.assertEquals("http://localhost", disposition.getHostUri().toString());
        Assert.assertEquals("http://localhost/piecework/ui/form/TEST", disposition.getPageUri().toString());
        Assert.assertEquals("classpath:/META-INF/some/base", disposition.getBase());
        Assert.assertEquals("/this/is/the/path", disposition.getPath());
        Assert.assertEquals(action, disposition.getAction());
    }

    @Test
    public void testCustomInvalidPageUri() throws URISyntaxException {
        Mockito.doReturn(DataInjectionStrategy.DECORATE_HTML)
                .when(action).getStrategy();

        FormDisposition disposition = FormDisposition.Builder.build(process, deployment, action, context);
        URI pageUri = disposition.getInvalidPageUri(submission);
        Assert.assertEquals("http://localhost/piecework/ui/form/TEST?submissionId=9803", pageUri.toString());
    }

    @Test
    public void testRemoteInvalidPageUri() throws URISyntaxException {
        Mockito.doReturn(DataInjectionStrategy.REMOTE)
                .when(action).getStrategy();

        FormDisposition disposition = FormDisposition.Builder.build(process, deployment, action, context);
        URI pageUri = disposition.getInvalidPageUri(submission);
        Assert.assertEquals("https://some.remote.host.edu/this/is/the/path?submissionId=9803", pageUri.toString());
    }

    @Test
    public void testDefaultInvalidPageUri() throws URISyntaxException {
        Mockito.doReturn(DataInjectionStrategy.NONE)
                .when(action).getStrategy();

        FormDisposition disposition = FormDisposition.Builder.build(process, deployment, action, context);
        URI pageUri = disposition.getInvalidPageUri(submission);
        Assert.assertEquals("http://localhost/piecework/ui/form/TEST?submissionId=9803", pageUri.toString());
    }

    @Test
    public void testCustomResponsePageUri() throws URISyntaxException {
        Mockito.doReturn(DataInjectionStrategy.DECORATE_HTML)
                .when(action).getStrategy();

        FormDisposition disposition = FormDisposition.Builder.build(process, deployment, action, context);
        URI pageUri = disposition.getResponsePageUri(request);
        Assert.assertEquals("http://localhost/piecework/ui/form/TEST?requestId=2345", pageUri.toString());
    }

    @Test
    public void testRemoteResponsePageUri() throws URISyntaxException {
        Mockito.doReturn(DataInjectionStrategy.REMOTE)
                .when(action).getStrategy();

        FormDisposition disposition = FormDisposition.Builder.build(process, deployment, action, context);
        URI pageUri = disposition.getResponsePageUri(request);
        Assert.assertEquals("https://some.remote.host.edu/this/is/the/path?requestId=2345", pageUri.toString());
    }

    @Test
    public void testDefaultResponsePageUri() throws URISyntaxException {
        Mockito.doReturn(DataInjectionStrategy.NONE)
                .when(action).getStrategy();

        FormDisposition disposition = FormDisposition.Builder.build(process, deployment, action, context);
        URI pageUri = disposition.getResponsePageUri(request);
        Assert.assertEquals("http://localhost/piecework/ui/form/TEST?requestId=2345", pageUri.toString());
    }


    @Test
    public void testCustomTaskPageUri() throws URISyntaxException {
        Mockito.doReturn(DataInjectionStrategy.DECORATE_HTML)
                .when(action).getStrategy();

        FormDisposition disposition = FormDisposition.Builder.build(process, deployment, action, context);
        URI pageUri = disposition.getPageUri(request, null, null, count);
        Assert.assertEquals("http://localhost/piecework/ui/form/TEST?taskId=5634", pageUri.toString());
    }

    @Test
    public void testRemoteTaskPageUri() throws URISyntaxException {
        Mockito.doReturn(DataInjectionStrategy.REMOTE)
                .when(action).getStrategy();

        FormDisposition disposition = FormDisposition.Builder.build(process, deployment, action, context);
        URI pageUri = disposition.getPageUri(request, null, null, count);
        Assert.assertEquals("https://some.remote.host.edu/this/is/the/path?taskId=5634", pageUri.toString());
    }

    @Test
    public void testDefaultTaskPageUri() throws URISyntaxException {
        Mockito.doReturn(DataInjectionStrategy.NONE)
                .when(action).getStrategy();

        FormDisposition disposition = FormDisposition.Builder.build(process, deployment, action, context);
        URI pageUri = disposition.getPageUri(request, null, null, count);
        Assert.assertEquals("http://localhost/piecework/ui/form/TEST?taskId=5634", pageUri.toString());
    }

    @Test
    public void testCustomTaskSubmissionPageUri() throws URISyntaxException {
        Mockito.doReturn(DataInjectionStrategy.DECORATE_HTML)
                .when(action).getStrategy();

        FormDisposition disposition = FormDisposition.Builder.build(process, deployment, action, context);
        URI pageUri = disposition.getPageUri(null, validation, null, count);
        Assert.assertEquals("http://localhost/piecework/ui/form/TEST?submissionId=9803", pageUri.toString());
    }

    @Test
    public void testRemoteTaskSubmissionPageUri() throws URISyntaxException {
        Mockito.doReturn(DataInjectionStrategy.REMOTE)
                .when(action).getStrategy();

        FormDisposition disposition = FormDisposition.Builder.build(process, deployment, action, context);
        URI pageUri = disposition.getPageUri(null, validation, null, count);
        Assert.assertEquals("https://some.remote.host.edu/this/is/the/path?submissionId=9803", pageUri.toString());
    }

    @Test
    public void testDefaultTaskSubmissionPageUri() throws URISyntaxException {
        Mockito.doReturn(DataInjectionStrategy.NONE)
                .when(action).getStrategy();

        FormDisposition disposition = FormDisposition.Builder.build(process, deployment, action, context);
        URI pageUri = disposition.getPageUri(null, validation, null, count);
        Assert.assertEquals("http://localhost/piecework/ui/form/TEST?submissionId=9803", pageUri.toString());
    }

    @Test
    public void testCustomTaskRequestPageUri() throws URISyntaxException {
        Mockito.doReturn(DataInjectionStrategy.DECORATE_HTML)
                .when(action).getStrategy();

        FormDisposition disposition = FormDisposition.Builder.build(process, deployment, action, context);
        URI pageUri = disposition.getPageUri(request, validation, explanation, count);
        Assert.assertEquals("http://localhost/piecework/ui/form/TEST?requestId=2345", pageUri.toString());
    }

    @Test
    public void testRemoteTaskRequestPageUri() throws URISyntaxException {
        Mockito.doReturn(DataInjectionStrategy.REMOTE)
                .when(action).getStrategy();

        FormDisposition disposition = FormDisposition.Builder.build(process, deployment, action, context);
        URI pageUri = disposition.getPageUri(request, validation, explanation, count);
        Assert.assertEquals("https://some.remote.host.edu/this/is/the/path?requestId=2345", pageUri.toString());
    }

    @Test
    public void testDefaultTaskRequestPageUri() throws URISyntaxException {
        Mockito.doReturn(DataInjectionStrategy.NONE)
                .when(action).getStrategy();

        FormDisposition disposition = FormDisposition.Builder.build(process, deployment, action, context);
        URI pageUri = disposition.getPageUri(request, validation, explanation, count);
        Assert.assertEquals("http://localhost/piecework/ui/form/TEST?requestId=2345", pageUri.toString());
    }

}
