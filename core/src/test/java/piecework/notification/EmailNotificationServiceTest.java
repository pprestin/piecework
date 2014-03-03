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
package piecework.notification;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import piecework.enumeration.StateChangeType;
import piecework.model.Notification;
import piecework.model.User;
import piecework.service.GroupService;
import piecework.service.IdentityService;
import piecework.settings.NotificationSettings;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;

/**
 * @author James Renfro
 */
@RunWith(MockitoJUnitRunner.class)
public class EmailNotificationServiceTest {

    @InjectMocks
    EmailNotificationService notificationService;

    @Mock
    EmailDispatcher emailDispatcher;

    @Mock
    IdentityService identityService;

    @Mock
    GroupService groupService;

    @Mock
    NotificationSettings notificationSettings;

    @Test
    public void verifyNotForThisEvent() {
        Notification notification = new Notification.Builder()
                .put(Notification.Constants.EVENT, StateChangeType.CREATE_TASK.name())
                .build();
        Map<String, Object> scope = new HashMap<String, Object>();
        Assert.assertFalse(notificationService.send(notification, scope, StateChangeType.COMPLETE_TASK));
    }

    @Test
    public void verifyNoRecipients() {
        Notification notification = new Notification.Builder()
                .build();
        Map<String, Object> scope = new HashMap<String, Object>();
        Assert.assertFalse(notificationService.send(notification, scope, StateChangeType.COMPLETE_TASK));
    }

    @Test
    public void verifyNoSubject() {
        Mockito.doReturn(new User.Builder()
                .userId("testuser")
                .displayName("Jack Tester")
                .emailAddress("jack@testing.org")
                .build())
                .when(identityService).getUser(eq("testuser"));

        Notification notification = new Notification.Builder()
                .put("assignee", "testuser")
                .put("sender", "test.sender@nowhere.org")
                .build();
        Map<String, Object> scope = new HashMap<String, Object>();
        Assert.assertFalse(notificationService.send(notification, scope, StateChangeType.COMPLETE_TASK));
    }

    @Test
    public void verifyNoText() {
        Mockito.doReturn(new User.Builder()
                .userId("testuser")
                .displayName("Jack Tester")
                .emailAddress("jack@testing.org")
                .build())
            .when(identityService).getUser(eq("testuser"));

        Notification notification = new Notification.Builder()
                .put("assignee", "testuser")
                .put("sender", "test.sender@nowhere.org")
                .subject("Test notification")
                .build();
        Map<String, Object> scope = new HashMap<String, Object>();
        Assert.assertFalse(notificationService.send(notification, scope, StateChangeType.COMPLETE_TASK));
    }

    @Test
    public void verifyAssigneeRecipient() {
        Mockito.doReturn(new User.Builder()
                .userId("testuser")
                .displayName("Jack Tester")
                .emailAddress("jack@testing.org")
                .build())
                .when(identityService).getUser(eq("testuser"));

        Mockito.doReturn(Boolean.TRUE)
               .when(emailDispatcher).dispatch(anyString(), anyString(), any(List.class), any(List.class), anyString(), anyString());

        Notification notification = new Notification.Builder()
                .put("assignee", "testuser")
                .put("sender", "test.sender@nowhere.org")
                .subject("Test notification")
                .text("This is some sample text")
                .build();
        Map<String, Object> scope = new HashMap<String, Object>();
        Assert.assertTrue(notificationService.send(notification, scope, StateChangeType.COMPLETE_TASK));
        Mockito.verify(emailDispatcher, times(1))
               .dispatch(anyString(), anyString(), any(List.class), any(List.class), anyString(), anyString());
    }

    @Test
    public void verifyMultipleNotifications() {
        Mockito.doReturn(new User.Builder()
                .userId("testuser")
                .displayName("Jack Tester")
                .emailAddress("jack@testing.org")
                .build())
                .when(identityService).getUser(eq("testuser"));

        Mockito.doReturn(Boolean.TRUE)
                .when(emailDispatcher).dispatch(anyString(), anyString(), any(List.class), any(List.class), anyString(), anyString());

        Notification notification = new Notification.Builder()
                .put("assignee", "testuser")
                .put("sender", "test.sender@nowhere.org")
                .subject("Test notification")
                .text("This is some sample text")
                .build();
        Map<String, Object> scope = new HashMap<String, Object>();
        Assert.assertEquals(1, notificationService.send(Collections.singletonList(notification), scope, StateChangeType.COMPLETE_TASK));
        Mockito.verify(emailDispatcher, times(1))
                .dispatch(anyString(), anyString(), any(List.class), any(List.class), anyString(), anyString());
    }

}
