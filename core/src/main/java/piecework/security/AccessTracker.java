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
package piecework.security;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.MessageContextImpl;
import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.*;
import org.apache.cxf.message.Message;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import piecework.common.AccessLog;
import piecework.enumeration.AlarmSeverity;
import piecework.enumeration.CacheName;
import piecework.exception.PieceworkException;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessProvider;
import piecework.repository.AccessEventRepository;
import piecework.service.CacheService;
import piecework.settings.NotificationSettings;
import piecework.settings.SecuritySettings;
import piecework.util.ModelUtility;

import javax.annotation.PostConstruct;
import javax.mail.internet.InternetAddress;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author James Renfro
 */
@Service
public class AccessTracker implements RequestHandler {

    private static final Logger LOG = Logger.getLogger(AccessTracker.class);
    private static final String crLf = Character.toString((char)13) + Character.toString((char)10);

    @Autowired
    AccessEventRepository accessEventRepository;

    @Autowired
    CacheService cacheService;

    @Autowired
    NotificationSettings notificationSettings;

    @Autowired
    SecuritySettings securitySettings;


    public void alarm(final AlarmSeverity severity, final String message) {
        alarm(severity, message, "");
    }

    public void alarm(final AlarmSeverity severity, final String message, final Entity principal) {
        alarm(severity, message, principal != null ? principal.getEntityId() : null);
    }

    public void alarm(final AlarmSeverity severity, final String message, final String entityId) {
        try {
            SimpleEmail email = new SimpleEmail();
            email.setHostName(notificationSettings.getMailServerHost());
            email.setSmtpPort(notificationSettings.getMailServerPort());
            String adminEmail = notificationSettings.getAdminEmail();

            LOG.error("Alarming at severity " + severity + " : " + message);

            if (StringUtils.isEmpty(adminEmail)) {
                LOG.error("Unable to send alarm email, no admin email addressed configured.");
                return;
            }

            email.addTo(adminEmail);

            List<InternetAddress> toList = email.getToAddresses();
            if ( toList == null || toList.isEmpty() ) {
                LOG.error("No email addresses were found for " + adminEmail + ". No emails were sent.");
                return; // no recipients
            }

            String subject = notificationSettings.getApplicationName() + " is issuing " + severity + " alarm";
            StringBuilder body = new StringBuilder(message);

            if (StringUtils.isNotEmpty(entityId))
                body.append(crLf).append(crLf).append("Action attempted as principal ").append(entityId);
            else
                body.append(crLf).append(crLf).append("Action attempted by anonymous principal");

            email.setFrom(notificationSettings.getMailFromAddress(), notificationSettings.getMailFromLabel());
            email.setSubject(subject);
            email.setMsg(body.toString());

            LOG.debug("Subject: " + email.getSubject());
            LOG.debug(email.getMimeMessage());
            email.send();
        } catch (EmailException e) {
            LOG.error("Unable to send email with subject: Alarm from piecework: " + severity);
        }
    }

    @Override
    public Response handleRequest(Message message, ClassResourceInfo resourceClass) {
        MessageContext context = new MessageContextImpl(message);
        RequestDetails requestDetails = new RequestDetails.Builder(context, securitySettings).build();
        boolean isAnonymous = StringUtils.isEmpty(requestDetails.getRemoteUser());

        CacheName cacheName = isAnonymous ? CacheName.ACCESS_ANONYMOUS : CacheName.ACCESS_AUTHENTICATED;
        String key = isAnonymous ? requestDetails.getRemoteAddr() : requestDetails.getRemoteUser();

        Cache.ValueWrapper wrapper = cacheService.get(cacheName, key);

        AccessLog accessLog = null;
        AccessLog previousAccessLog = null;
        if (wrapper != null) {
            previousAccessLog = AccessLog.class.cast(wrapper.get());

            if (previousAccessLog != null && !previousAccessLog.isExpired(securitySettings.getAccessCacheInterval())) {
                accessLog = new AccessLog(previousAccessLog);
            }
        }

        if (accessLog == null)
            accessLog = new AccessLog(securitySettings.getAccessCountLimit());

        cacheService.put(cacheName, key, accessLog);

        if (accessLog.isAlarming()) {
            String alarmMessage = "Access attempt " + accessLog.getAccessCount() + " by " + key;
            LOG.warn(alarmMessage);
            alarm(AlarmSeverity.MINOR, alarmMessage, requestDetails.getRemoteUser());
//            return Response.status(Response.Status.FORBIDDEN).build();
        }
        return null;
    }

    public <P extends ProcessProvider> void track(final P modelProvider, final String secretId, final String key, final String reason, final boolean isAnonymousAllowed) throws PieceworkException {
        Process process = modelProvider.process();
        ProcessInstance instance = ModelUtility.instance(modelProvider);
        Entity principal = modelProvider.principal();
        accessEventRepository.save(new AccessEvent(process, instance, secretId, key, reason, principal, isAnonymousAllowed));
    }

}
