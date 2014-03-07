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
package piecework.settings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author James Renfro
 */
@Service
public class NotificationSettings {

    @Autowired
    Environment environment;

    private String applicationName;
    private String mailServerHost;
    private int mailServerPort;
    private String mailFromAddress;
    private String mailFromLabel;
    private String mailToOverride;
    private String adminEmail;

    @PostConstruct
    public void init() {
        this.applicationName = environment.getProperty("application.name");
        this.mailServerHost = environment.getProperty("mail.server.host");
        this.mailServerPort = environment.getProperty("mail.server.port", Integer.class, 25);
        this.mailFromAddress = environment.getProperty("mail.from.address");
        this.mailFromLabel = environment.getProperty("mail.from.label");
        this.mailToOverride = environment.getProperty("mail.to.override");
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getMailServerHost() {
        return mailServerHost;
    }

    public int getMailServerPort() {
        return mailServerPort;
    }

    public String getMailFromAddress() {
        return mailFromAddress;
    }

    public String getMailFromLabel() {
        return mailFromLabel;
    }

    public String getMailToOverride() {
        return mailToOverride;
    }

    public String getAdminEmail() {
        return adminEmail;
    }
}
