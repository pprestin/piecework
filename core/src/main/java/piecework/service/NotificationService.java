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
package piecework.service;

import java.util.List;
import java.util.Map;
import piecework.model.Notification;

/**
 * public interface for sending out notifications
 * @author Jiefeng Shen
 */
public interface NotificationService {
    /** 
     * expand any macros in notifications and send the notification to recipients.
     * @param  notification notification to send.
     * @param  context      a map of key-value pairs to be used for macro expansion.
     */  
    public void send(Notification notification, Map<String, String> context);

    /** 
     * a convenience method for sending out a list of notifications. It simpply loops through
     * each notification and calls the method above for each notification.
     * @param  notifications a list of notification to send out.
     * @param  context      a map of key-value pairs to be used for macro expansion.
     */  
    public void send(List<Notification> notifications, Map<String, String> context);
}
