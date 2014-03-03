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
package piecework.notification.concrete;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.model.User;
import piecework.notification.EmailDispatcher;
import piecework.settings.NotificationSettings;

import javax.mail.internet.InternetAddress;
import java.util.List;

/**
 * @author James Renfro
 */
@Service
public class SimpleEmailDispatcher implements EmailDispatcher{

    private static final Logger LOG = Logger.getLogger(SimpleEmailDispatcher.class);

    @Autowired
    private NotificationSettings notificationSettings;

    @Override
    public boolean dispatch(String senderEmail, String senderName, List<User> recipients, List<User> bcc, String subject, String body) {
        try {
            SimpleEmail email = new SimpleEmail();
            email.setHostName(notificationSettings.getMailServerHost());
            email.setSmtpPort(notificationSettings.getMailServerPort());

            for (User u : recipients) {
                String emailAddr = u.getEmailAddress();
                if ( emailAddr != null && ! emailAddr.isEmpty() ) {
                    email.addTo(emailAddr, u.getDisplayName());
                }
            }
            List<InternetAddress> toList = email.getToAddresses();
            if ( toList == null || toList.isEmpty() ) {
                LOG.error("No email addresses were found for " + recipients + ". No emails were sent.");
                return false; // no recipients
            }

            if ( bcc != null && ! bcc.isEmpty() ) {
                for (User u : bcc) {
                    String emailAddr = u.getEmailAddress();
                    if ( emailAddr != null && ! emailAddr.isEmpty() ) {
                        email.addBcc(emailAddr, u.getDisplayName());
                    }
                }
            }
            email.setFrom(senderEmail, senderName);
            email.setSubject(subject);
            email.setMsg(body);

            LOG.debug("Subject: " + email.getSubject());
            LOG.debug(email.getMimeMessage());
            email.send();
            return true;
        } catch (EmailException e) {
            LOG.error("Unable to send email with subject " + subject);
        }
        return false;
    }
}
