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

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import piecework.authorization.AuthorizationRole;
import piecework.common.ManyMap;
import piecework.enumeration.ActionType;
import piecework.enumeration.DataInjectionStrategy;
import piecework.exception.BadRequestError;
import piecework.exception.ForbiddenError;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.security.AccessTracker;
import piecework.security.data.LimitFieldsFilter;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.eq;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class SecurityUtilityTest {

    @Mock
    AccessTracker accessTracker;

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

    private final static String processDefinitionKey = "TEST";

    @Before
    public void setup() {
        Mockito.doReturn(processDefinitionKey)
               .when(formRequest).getProcessDefinitionKey();
    }

    @Test
    public void fieldsNoChildren() {
        Container container = new Container.Builder()
                .field(new Field.Builder()
                    .name("test-field-1")
                    .build())
                .field(new Field.Builder()
                        .name("test-field-2")
                        .build())
                .field(new Field.Builder()
                        .name("test-field-3")
                        .build())
                .build();

        Action action = new Action(container, "", DataInjectionStrategy.NONE);

        Activity activity = new Activity.Builder()
                .action(ActionType.CREATE, action)
                .build();

        Set<Field> fields = SecurityUtility.fields(activity, action);
        Assert.assertNotNull(fields);
        Assert.assertEquals(3, fields.size());
    }

    @Test
    public void fieldsOneChild() {
        Container child = new Container.Builder()
                .field(new Field.Builder()
                        .name("test-field-1")
                        .build())
                .field(new Field.Builder()
                        .name("test-field-2")
                        .build())
                .field(new Field.Builder()
                        .name("test-field-3")
                        .build())
                .ordinal(1)
                .build();

        Container container = new Container.Builder()
                .child(child)
                .activeChildIndex(1)
                .build();

        Action action = new Action(container, "", DataInjectionStrategy.NONE);

        Activity activity = new Activity.Builder()
                .action(ActionType.CREATE, action)
                .build();

        Set<Field> fields = SecurityUtility.fields(activity, action);
        Assert.assertNotNull(fields);
        Assert.assertEquals(3, fields.size());
    }

    @Test
    public void fieldsTwoChildrenFirstActive() {
        Container child1 = new Container.Builder()
                .field(new Field.Builder()
                        .name("test-field-1")
                        .build())
                .field(new Field.Builder()
                        .name("test-field-2")
                        .build())
                .field(new Field.Builder()
                        .name("test-field-3")
                        .build())
                .ordinal(1)
                .build();

        Container child2 = new Container.Builder()
                .field(new Field.Builder()
                        .name("test-field-4")
                        .build())
                .field(new Field.Builder()
                        .name("test-field-5")
                        .build())
                .ordinal(2)
                .build();

        Container container = new Container.Builder()
                .child(child1)
                .child(child2)
                .activeChildIndex(1)
                .build();

        Action action = new Action(container, "", DataInjectionStrategy.NONE);

        Activity activity = new Activity.Builder()
                .action(ActionType.CREATE, action)
                .build();

        Set<Field> fields = SecurityUtility.fields(activity, action);
        Assert.assertNotNull(fields);
        Assert.assertEquals(5, fields.size());
    }

    @Test
    public void fieldsTwoChildrenSecondActive() {
        Container child1 = new Container.Builder()
                .field(new Field.Builder()
                        .name("test-field-1")
                        .build())
                .field(new Field.Builder()
                        .name("test-field-2")
                        .build())
                .field(new Field.Builder()
                        .name("test-field-3")
                        .build())
                .ordinal(1)
                .build();

        Container child2 = new Container.Builder()
                .field(new Field.Builder()
                        .name("test-field-4")
                        .build())
                .field(new Field.Builder()
                        .name("test-field-5")
                        .build())
                .ordinal(2)
                .build();

        Container container = new Container.Builder()
                .child(child1)
                .child(child2)
                .activeChildIndex(2)
                .build();

        Action action = new Action(container, "", DataInjectionStrategy.NONE);

        Activity activity = new Activity.Builder()
                .action(ActionType.CREATE, action)
                .build();

        Set<Field> fields = SecurityUtility.fields(activity, action);
        Assert.assertNotNull(fields);
        Assert.assertEquals(5, fields.size());
    }

    @Test
    public void restrictedFieldsNullFields() {
        Set<Field> restrictedFields = SecurityUtility.restrictedFields(null);
        Assert.assertNotNull(restrictedFields);
        Assert.assertTrue(restrictedFields.isEmpty());
    }

    @Test
    public void restrictedFieldsEmptyFields() {
        Set<Field> restrictedFields = SecurityUtility.restrictedFields(Collections.<Field>emptySet());
        Assert.assertNotNull(restrictedFields);
        Assert.assertTrue(restrictedFields.isEmpty());
    }

    @Test
    public void restrictedFieldsNoRestrictedFields() {
        Set<Field> fields = new HashSet<Field>();
        fields.add(new Field.Builder()
                .name("test-3")
                .build());
        Set<Field> restrictedFields = SecurityUtility.restrictedFields(fields);
        Assert.assertNotNull(restrictedFields);
        Assert.assertTrue(restrictedFields.isEmpty());
    }

    @Test
     public void restrictedFieldsJustOneRestrictedField() {
        Set<Field> fields = new HashSet<Field>();
        fields.add(new Field.Builder()
                .name("test-3")
                .restricted()
                .build());
        Set<Field> restrictedFields = SecurityUtility.restrictedFields(fields);
        Assert.assertNotNull(restrictedFields);
        Assert.assertEquals(1, restrictedFields.size());
        Assert.assertEquals("test-3", restrictedFields.iterator().next().getName());
    }

    @Test
    public void restrictedFieldsPickOnlyRestrictedField() {
        Set<Field> fields = new HashSet<Field>();
        fields.add(new Field.Builder()
                .name("test-1")
                .build());
        fields.add(new Field.Builder()
                .name("test-2")
                .build());
        fields.add(new Field.Builder()
                .name("test-3")
                .restricted()
                .build());
        fields.add(new Field.Builder()
                .name("test-4")
                .restricted()
                .build());
        Set<Field> restrictedFields = SecurityUtility.restrictedFields(fields);
        Assert.assertNotNull(restrictedFields);
        Assert.assertEquals(2, restrictedFields.size());
    }

    @Test
    public void filterWithNoFilters() {
        ManyMap<String, Value> original = new ManyMap<String, Value>();
        original.putOne("test-1", new Value("one"));
        original.putOne("test-2", new Value("another"));
        ManyMap<String, Value> filtered = SecurityUtility.filter(original);
        Assert.assertEquals(original, filtered);
    }

    @Test
    public void filterWithLimitFieldFilterNoFields() {
        ManyMap<String, Value> original = new ManyMap<String, Value>();
        original.putOne("test-1", new Value("one"));
        original.putOne("test-2", new Value("another"));
        Set<Field> fields = new HashSet<Field>();
        ManyMap<String, Value> filtered = SecurityUtility.filter(original, new LimitFieldsFilter(fields, false));
        Assert.assertTrue(filtered.isEmpty());
    }

    @Test
    public void filterWithLimitFieldFilterNoMatchingFields() {
        ManyMap<String, Value> original = new ManyMap<String, Value>();
        original.putOne("test-1", new Value("one"));
        original.putOne("test-2", new Value("another"));
        Set<Field> fields = new HashSet<Field>();
        fields.add(new Field.Builder()
                .name("test-3")
                .build());
        ManyMap<String, Value> filtered = SecurityUtility.filter(original, new LimitFieldsFilter(fields, false));
        Assert.assertTrue(filtered.isEmpty());
    }

    @Test
    public void filterWithLimitFieldFilterOneMatchingField() {
        ManyMap<String, Value> original = new ManyMap<String, Value>();
        original.putOne("test-1", new Value("one"));
        original.putOne("test-2", new Value("another"));
        Set<Field> fields = new HashSet<Field>();
        fields.add(new Field.Builder()
                .name("test-2")
                .build());
        ManyMap<String, Value> filtered = SecurityUtility.filter(original, new LimitFieldsFilter(fields, false));
        Assert.assertEquals(1, filtered.size());
        Assert.assertEquals("another", filtered.getOne("test-2").toString());
    }

    /*
     * Check that SecurityUtility correctly removes restricted fields even if they
     * match the list of fields, when the includeRestrictedFields flag is set to FALSE
     */
    @Test
    public void filterWithLimitFieldFilterNoRestrictedOneMatchingRestrictedField() {
        ManyMap<String, Value> original = new ManyMap<String, Value>();
        original.putOne("test-1", new Value("one"));
        original.putOne("test-2", new Value("another"));
        Set<Field> fields = new HashSet<Field>();
        fields.add(new Field.Builder()
                .name("test-2")
                .restricted()
                .build());
        ManyMap<String, Value> filtered = SecurityUtility.filter(original, new LimitFieldsFilter(fields, false));
        Assert.assertTrue(filtered.isEmpty());
    }

    /*
     * Check that SecurityUtility correctly includes restricted fields if they
     * match the list of fields, and the includeRestrictedFields flag is set to TRUE
     */
    @Test
    public void filterWithLimitFieldFilterOneMatchingRestrictedField() {
        ManyMap<String, Value> original = new ManyMap<String, Value>();
        original.putOne("test-1", new Value("one"));
        original.putOne("test-2", new Value("another"));
        Set<Field> fields = new HashSet<Field>();
        fields.add(new Field.Builder()
                .name("test-2")
                .restricted()
                .build());
        ManyMap<String, Value> filtered = SecurityUtility.filter(original, new LimitFieldsFilter(fields, true));
        Assert.assertEquals(1, filtered.size());
        Assert.assertEquals("another", filtered.getOne("test-2").toString());
    }

    @Test
    public void verifyNullEntityCanInitiateWithAnonymousAllowed() throws PieceworkException {
        Process process = new Process.Builder()
                .processDefinitionKey("TEST")
                .allowAnonymousSubmission(true)
                .build();

        SecurityUtility.verifyEntityCanInitiate(process, null);
    }

    @Test(expected = ForbiddenError.class)
    public void verifyNullEntityCannotInitiateWithoutAnonymousAllowed() throws PieceworkException {
        Process process = new Process.Builder()
                .processDefinitionKey("TEST")
                .allowAnonymousSubmission(false)
                .build();

        SecurityUtility.verifyEntityCanInitiate(process, null);
    }

    @Test
    public void verifyEntityCanInitiateAsInitiator() throws PieceworkException {
        Process process = new Process.Builder()
                .processDefinitionKey("TEST")
                .allowAnonymousSubmission(false)
                .build();

        Mockito.doReturn(Boolean.TRUE)
                .when(principal).hasRole(eq(process), eq(AuthorizationRole.INITIATOR));

        SecurityUtility.verifyEntityCanInitiate(process, principal);
    }

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
                .when(principal).hasRole(eq(process), eq(AuthorizationRole.USER));
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

        SecurityUtility.verifyRequestIntegrity(accessTracker, processDefinitionKey, formRequest, requestDetails);
    }

    @Test(expected = ForbiddenError.class)
    public void verifyRequestIntegrityUnmatchedRemoteUser() throws ForbiddenError {
        Mockito.doReturn("testuser")
                .when(formRequest).getRemoteUser();
        Mockito.doReturn("another")
                .when(requestDetails).getRemoteUser();

        SecurityUtility.verifyRequestIntegrity(accessTracker, processDefinitionKey, formRequest, requestDetails);
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

        SecurityUtility.verifyRequestIntegrity(accessTracker, processDefinitionKey, formRequest, requestDetails);
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

        SecurityUtility.verifyRequestIntegrity(accessTracker, processDefinitionKey, formRequest, requestDetails);
    }

    @Test(expected = ForbiddenError.class)
    public void verifyRequestIntegrityUnmatchedCertificateIssuer() throws ForbiddenError {
        Mockito.doReturn("Some CA")
                .when(formRequest).getCertificateIssuer();
        Mockito.doReturn("Some other CA")
                .when(requestDetails).getCertificateIssuer();

        SecurityUtility.verifyRequestIntegrity(accessTracker, processDefinitionKey, formRequest, requestDetails);
    }

    @Test(expected = ForbiddenError.class)
    public void verifyRequestIntegrityUnmatchedCertificateSubject() throws ForbiddenError {
        Mockito.doReturn("abc.host.com")
                .when(formRequest).getCertificateSubject();
        Mockito.doReturn("def.host.com")
                .when(requestDetails).getCertificateSubject();

        SecurityUtility.verifyRequestIntegrity(accessTracker, processDefinitionKey, formRequest, requestDetails);
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

        SecurityUtility.verifyRequestIntegrity(accessTracker, processDefinitionKey, formRequest, requestDetails);
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

        SecurityUtility.verifyRequestIntegrity(accessTracker, processDefinitionKey, formRequest, requestDetails);
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

        SecurityUtility.verifyRequestIntegrity(accessTracker, processDefinitionKey, formRequest, requestDetails);
    }

    @Test
    public void verifyRequestIntegrityUnExpiredRequest() throws ForbiddenError {
        Date now = new Date(new Date().getTime() - 60000);
        Mockito.doReturn(now)
                .when(formRequest).getRequestDate();

        SecurityUtility.verifyRequestIntegrity(accessTracker, processDefinitionKey, formRequest, requestDetails);
    }

    @Test(expected = ForbiddenError.class)
    public void verifyRequestIntegrityExpiredRequest() throws ForbiddenError {
        Date yesterday = new Date(new Date().getTime() - (25 * 60 * 60000));
        Mockito.doReturn(yesterday)
               .when(formRequest).getRequestDate();

        SecurityUtility.verifyRequestIntegrity(accessTracker, processDefinitionKey, formRequest, requestDetails);
    }

}
