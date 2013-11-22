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

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.io.StringReader;
import java.io.StringWriter;
import javax.mail.internet.InternetAddress;
import org.apache.log4j.Logger;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetails;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import piecework.model.Notification;
import piecework.service.NotificationService;
import piecework.model.User;
import piecework.identity.IdentityDetails;
import piecework.service.IdentityService;
import piecework.model.Group;
import piecework.service.GroupService;
import piecework.Constants;

/**
 * send out email notifications
 * @author Jiefeng Shen
 */
@Service
public class EmailNotificationService implements NotificationService {
    private static final Logger LOG = Logger.getLogger(EmailNotificationService.class);

    @Autowired
    Environment environment;

    @Autowired
    IdentityService userDetailsService;  // get user details such as user name and email address

    @Autowired
    GroupService groupService;	// get group members

    /** 
     * expand any macros in notifications and send the notification to recipients.
     * @param  notification notification to send.
     * @param  context      a map of key-value pairs to be used for macro expansion.
     */  
    public void send(Notification notification, Map<String, String> context) {
        // sanity check
        if ( notification == null ) {
            return;
        }

        String mailServerHost = environment.getProperty("mail.server.host");
        int mailServerPort = environment.getProperty("mail.server.port", Integer.class, 25);
        String mailFromAddress = environment.getProperty("mail.from.address");
        String mailFromLabel = environment.getProperty("mail.from.label");

        // get sender email
        String senderEmail = notification.getSenderEmail();
        if ( senderEmail == null || senderEmail.isEmpty() ) {
            senderEmail = mailFromAddress;
        }

        // get sender name/lablel
        String senderName = notification.getSenderName();
        if ( senderName == null || senderName.isEmpty() ) {
            senderName = mailFromLabel;
        }

        // recipients
        String recipientStr = notification.getRecipients();
        List<User> recipients = getUsers(recipientStr);
        if ( recipients == null || recipients.isEmpty() ) {
            return; // recipients are required
        }

        // bcc 
        String bccStr = notification.getBcc();
        List<User> bcc = getUsers(bccStr);

        // get subject
        String subject = notification.getSubject();
        MustacheFactory mf = new DefaultMustacheFactory();
        StringWriter writer = new StringWriter();
        Mustache mustache = mf.compile(new StringReader(subject), "subject");
        mustache.execute(writer, context);
        subject = writer.toString();

        // get body
        String body = notification.getText();
        writer = new StringWriter();
        mustache = mf.compile(new StringReader(body), "text");
        mustache.execute(writer, context);
        body = writer.toString();

        try {
            SimpleEmail email = new SimpleEmail();
            email.setHostName(mailServerHost);
            email.setSmtpPort(mailServerPort);

            for (User u : recipients) {
                String emailAddr = u.getEmailAddress();
                if ( emailAddr != null && ! emailAddr.isEmpty() ) {
                    email.addTo(emailAddr, u.getDisplayName());
                }
            }
            List<InternetAddress> toList = email.getToAddresses();
            if ( toList == null || toList.isEmpty() ) {
                LOG.error("No email addresses were found for " + recipientStr + ". No emails were sent.");
                return; // no recipients
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
        } catch (EmailException e) {
            LOG.error("Unable to send email with subject " + subject);
        }
    }

    /** 
     * a convenience method for sending out a list of notifications. It simpply loops through
     * each notification and calls send(Notification) for each notification.
     * @param  notifications a list of notification to send out.
     * @param  context      a map of key-value pairs to be used for macro expansion.
     */  
    public void send(List<Notification> notifications, Map<String, String> context) {

         // sanity check
         if ( notifications == null ) {
             return;
         }

         for (Notification n : notifications) {
             send(n, context);
         }
    }

    /** 
     * return a list of users objects for a list of recipient IDs.
     * @param userStr  a comma-separated list of user IDs, groupIds
     *                      or email addresses
     * @return              a list of user objects or null for unknown/invalid recipient IDs 
     */  
    public List<User> getUsers(String userStr)
    {   
        // sanity check
        if ( userStr == null || userStr.isEmpty() ) { 
            return null;
        }   

        // split string list
        String[] ids = userStr.split("[,;]");

        List<User> users = new ArrayList<User>();
        User.Builder  builder = null;
        for (String id : ids ) { 
            id = id.trim(); // remove leading/tailing spaces
            if ( id.indexOf('@') > 0 ) { 
                builder = new User.Builder();
                builder.emailAddress( id );
                users.add(builder.build());
            } else if ( id.startsWith("role_") || id.startsWith("ROLE_") ) { 
                // handle group
                String groupId = id.substring("role_".length());
                Group group = groupService.getGroupById(groupId);
                if ( group != null ) { 
                    List<User> members = group.getMembers();
                    users.addAll(members);
                }   
            } else { // assuming userId
                User recipient = userDetailsService.getUser(id);
                if ( recipient != null ) { 
                    users.add(recipient);
                }   
            }   
        }   

        return users;
    }   
}
