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
package piecework.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import piecework.authorization.AuthorizationRole;
import piecework.exception.BadRequestError;
import piecework.exception.ForbiddenError;
import piecework.model.*;
import piecework.model.Process;

import java.util.Date;

import static org.mockito.Matchers.eq;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class SecurityUtilityTest {

    @Mock
    FormRequest formRequest;

    @Mock
    User principal;

    @Mock
    Process process;

    @Mock
    RequestDetails requestDetails;

    @Mock
    Task task;

    @Test
    public void verifyEntityIsAuthorizedOverseer() throws ForbiddenError, BadRequestError {
        Mockito.doReturn("123")
               .when(task).getTaskInstanceId();
        Mockito.doReturn("99883")
               .when(principal).getEntityId();
        Mockito.doReturn(Boolean.TRUE)
               .when(principal).hasRole(eq(process), eq(AuthorizationRole.OVERSEER));

        SecurityUtility.verifyEntityIsAuthorized(process, task, principal);
    }

    @Test
    public void verifyEntityIsAuthorizedCandidateOrAssignee() throws ForbiddenError, BadRequestError {
        Mockito.doReturn("123")
                .when(task).getTaskInstanceId();
        Mockito.doReturn("99883")
                .when(principal).getEntityId();
        Mockito.doReturn(Boolean.FALSE)
                .when(principal).hasRole(eq(process), eq(AuthorizationRole.OVERSEER));
        Mockito.doReturn(Boolean.TRUE)
                .when(task).isCandidateOrAssignee(eq(principal));

        SecurityUtility.verifyEntityIsAuthorized(process, task, principal);
    }

    @Test(expected = ForbiddenError.class)
    public void verifyEntityIsNotAuthorized() throws ForbiddenError, BadRequestError {
        Mockito.doReturn("123")
                .when(task).getTaskInstanceId();
        Mockito.doReturn("99883")
                .when(principal).getEntityId();
        Mockito.doReturn(Boolean.FALSE)
                .when(principal).hasRole(eq(process), eq(AuthorizationRole.OVERSEER));
        Mockito.doReturn(Boolean.FALSE)
                .when(task).isCandidateOrAssignee(eq(principal));

        SecurityUtility.verifyEntityIsAuthorized(process, task, principal);
    }

    @Test(expected = ForbiddenError.class)
     public void verifyEntityIsNotAuthorizedAnonymous() throws ForbiddenError, BadRequestError {
        Mockito.doReturn("123")
                .when(task).getTaskInstanceId();

        SecurityUtility.verifyEntityIsAuthorized(process, task, null);
    }

    @Test(expected = BadRequestError.class)
    public void verifyEntityIsNotAuthorizedNoProcess() throws ForbiddenError, BadRequestError {
        Mockito.doReturn("123")
                .when(task).getTaskInstanceId();

        SecurityUtility.verifyEntityIsAuthorized(null, task, null);
    }

    @Test(expected = ForbiddenError.class)
    public void verifyRequestIntegrityNoRemoteUser() throws ForbiddenError {
        Mockito.doReturn("testuser")
               .when(formRequest).getRemoteUser();

        SecurityUtility.verifyRequestIntegrity(formRequest, requestDetails);
    }

    @Test(expected = ForbiddenError.class)
    public void verifyRequestIntegrityUnmatchedRemoteUser() throws ForbiddenError {
        Mockito.doReturn("testuser")
                .when(formRequest).getRemoteUser();
        Mockito.doReturn("another")
                .when(requestDetails).getRemoteUser();

        SecurityUtility.verifyRequestIntegrity(formRequest, requestDetails);
    }

    // Having a different remote host is not necessarily an exception, but it will be logged
    @Test
    public void verifyRequestIntegrityUnmatchedRemoteHost() throws ForbiddenError {
        Mockito.doReturn("testuser")
                .when(formRequest).getRemoteUser();
        Mockito.doReturn("testuser")
                .when(requestDetails).getRemoteUser();

        Mockito.doReturn("somewhere.com")
                .when(formRequest).getRemoteHost();
        Mockito.doReturn("nowhere.com")
                .when(requestDetails).getRemoteHost();

        SecurityUtility.verifyRequestIntegrity(formRequest, requestDetails);
    }

    // Having a different remote address is not necessarily an exception, but it will be logged
    @Test
    public void verifyRequestIntegrityUnmatchedRemoteAddress() throws ForbiddenError {
        Mockito.doReturn("testuser")
                .when(formRequest).getRemoteUser();
        Mockito.doReturn("testuser")
                .when(requestDetails).getRemoteUser();

        Mockito.doReturn("127.0.0.1")
                .when(formRequest).getRemoteAddr();
        Mockito.doReturn("200.0.0.1")
                .when(requestDetails).getRemoteAddr();

        SecurityUtility.verifyRequestIntegrity(formRequest, requestDetails);
    }

    @Test(expected = ForbiddenError.class)
    public void verifyRequestIntegrityUnmatchedCertificateIssuer() throws ForbiddenError {
        Mockito.doReturn("Some CA")
                .when(formRequest).getCertificateIssuer();
        Mockito.doReturn("Some other CA")
                .when(requestDetails).getCertificateIssuer();

        SecurityUtility.verifyRequestIntegrity(formRequest, requestDetails);
    }

    @Test(expected = ForbiddenError.class)
    public void verifyRequestIntegrityUnmatchedCertificateSubject() throws ForbiddenError {
        Mockito.doReturn("abc.host.com")
                .when(formRequest).getCertificateSubject();
        Mockito.doReturn("def.host.com")
                .when(requestDetails).getCertificateSubject();

        SecurityUtility.verifyRequestIntegrity(formRequest, requestDetails);
    }

    @Test(expected = ForbiddenError.class)
    public void verifyRequestIntegrityUnmatchedCertificateSubjectAndIssuer() throws ForbiddenError {
        Mockito.doReturn("Some CA")
                .when(formRequest).getCertificateIssuer();
        Mockito.doReturn("Some other CA")
                .when(requestDetails).getCertificateIssuer();

        Mockito.doReturn("abc.host.com")
                .when(formRequest).getCertificateSubject();
        Mockito.doReturn("def.host.com")
                .when(requestDetails).getCertificateSubject();

        SecurityUtility.verifyRequestIntegrity(formRequest, requestDetails);
    }

    @Test(expected = ForbiddenError.class)
    public void verifyRequestIntegrityUnmatchedCertificateSubjectWithSameIssuer() throws ForbiddenError {
        Mockito.doReturn("Some CA")
                .when(formRequest).getCertificateIssuer();
        Mockito.doReturn("Some CA")
                .when(requestDetails).getCertificateIssuer();

        Mockito.doReturn("abc.host.com")
                .when(formRequest).getCertificateSubject();
        Mockito.doReturn("def.host.com")
                .when(requestDetails).getCertificateSubject();

        SecurityUtility.verifyRequestIntegrity(formRequest, requestDetails);
    }

    @Test(expected = ForbiddenError.class)
    public void verifyRequestIntegrityUnmatchedCertificateIssuerWithSameSubject() throws ForbiddenError {
        Mockito.doReturn("Some CA")
                .when(formRequest).getCertificateIssuer();
        Mockito.doReturn("Some other CA")
                .when(requestDetails).getCertificateIssuer();

        Mockito.doReturn("abc.host.com")
                .when(formRequest).getCertificateSubject();
        Mockito.doReturn("abc.host.com")
                .when(requestDetails).getCertificateSubject();

        SecurityUtility.verifyRequestIntegrity(formRequest, requestDetails);
    }

    @Test
    public void verifyRequestIntegrityUnExpiredRequest() throws ForbiddenError {
        Date now = new Date(new Date().getTime() - 60000);
        Mockito.doReturn(now)
                .when(formRequest).getRequestDate();

        SecurityUtility.verifyRequestIntegrity(formRequest, requestDetails);
    }

    @Test(expected = ForbiddenError.class)
    public void verifyRequestIntegrityExpiredRequest() throws ForbiddenError {
        Date yesterday = new Date(new Date().getTime() - (25 * 60 * 60000));
        Mockito.doReturn(yesterday)
               .when(formRequest).getRequestDate();

        SecurityUtility.verifyRequestIntegrity(formRequest, requestDetails);
    }

}
