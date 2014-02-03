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

import org.springframework.core.env.Environment;
import piecework.Constants;

/**
 * @author James Renfro
 */
public class SecuritySettings {

    private final String certificateIssuerHeader;
    private final String certificateSubjectHeader;
    private final String actAsUserHeader;
    private final String keystoreFile;
    private final char[] keystorePassword;

    public SecuritySettings(Environment environment) {
        this.certificateIssuerHeader = environment.getProperty(Constants.Settings.CERTIFICATE_ISSUER_HEADER);
        this.certificateSubjectHeader = environment.getProperty(Constants.Settings.CERTIFICATE_SUBJECT_HEADER);
        this.actAsUserHeader = environment.getProperty(Constants.Settings.ACT_AS_USER_HEADER);
        this.keystoreFile = environment.getProperty(Constants.Settings.KEYSTORE_FILE);
        this.keystorePassword = environment.getProperty(Constants.Settings.KEYSTORE_PASSWORD, "").toCharArray();
    }

    public String getCertificateIssuerHeader() {
        return certificateIssuerHeader;
    }

    public String getCertificateSubjectHeader() {
        return certificateSubjectHeader;
    }

    public String getActAsUserHeader() {
        return actAsUserHeader;
    }

    public String getKeystoreFile() {
        return keystoreFile;
    }

    public char[] getKeystorePassword() {
        return keystorePassword;
    }
}
