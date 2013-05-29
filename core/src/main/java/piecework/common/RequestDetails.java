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
package piecework.common;

import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @author James Renfro
 */
public class RequestDetails {

    private final String certificateIssuer;
    private final String certificateSubject;
    private final String remoteAddr;
    private final String remoteHost;
    private final int remotePort;
    private final String remoteUser;
    private final boolean isServiceCall;

    private RequestDetails() {
        this(new Builder());
    }

    private RequestDetails(Builder builder) {
        this.certificateIssuer = builder.certificateIssuer;
        this.certificateSubject = builder.certificateSubject;
        this.remoteAddr = builder.remoteAddr;
        this.remoteHost = builder.remoteHost;
        this.remotePort = builder.remotePort;
        this.remoteUser = builder.remoteUser;
        this.isServiceCall = builder.isServiceCall;
    }

    public String getCertificateIssuer() {
        return certificateIssuer;
    }

    public String getCertificateSubject() {
        return certificateSubject;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public String getRemoteUser() {
        return remoteUser;
    }

    public boolean isServiceCall() {
        return isServiceCall;
    }

    public final static class Builder {

        private String remoteAddr;
        private String remoteUser;
        private String remoteHost;
        private int remotePort;
        private String certificateIssuer;
        private String certificateSubject;
        private boolean isServiceCall;

        public Builder() {

        }

        public Builder(HttpServletRequest request, String certificateIssuerHeader, String certificateSubjectHeader) {
            if (StringUtils.isNotEmpty(certificateIssuerHeader))
                this.certificateIssuer = request.getHeader(certificateIssuerHeader);
            if (StringUtils.isNotEmpty(certificateSubjectHeader))
                this.certificateSubject = request.getHeader(certificateSubjectHeader);

            this.remoteAddr = request.getRemoteAddr();
            this.remoteHost = request.getRemoteHost();
            this.remotePort = request.getRemotePort();
            this.remoteUser = request.getRemoteUser();

            this.isServiceCall = StringUtils.isNotEmpty(this.certificateIssuer) && StringUtils.isNotEmpty(this.certificateSubject);
        }

        public RequestDetails build() {
            return new RequestDetails(this);
        }

    }

}
