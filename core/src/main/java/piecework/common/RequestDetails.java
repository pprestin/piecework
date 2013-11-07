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
import org.apache.cxf.jaxrs.ext.MessageContext;
import piecework.security.SecuritySettings;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

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
    private final String actAsUser;
    private final MediaType contentType;
    private final List<MediaType> acceptableMediaTypes;
    private final Map<String, List<String>> requestHeaders;
    private final String referrer;
    private final String userAgent;
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
        this.actAsUser = builder.actAsUser;
        this.contentType = builder.contentType;
        this.acceptableMediaTypes = builder.acceptableMediaTypes;
        this.requestHeaders = builder.requestHeaders;
        this.isServiceCall = builder.isServiceCall;
        this.referrer = builder.referrer;
        this.userAgent = builder.userAgent;
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

    public String getActAsUser() {
        return actAsUser;
    }

    public MediaType getContentType() {
        return contentType;
    }

    public List<MediaType> getAcceptableMediaTypes() {
        return acceptableMediaTypes;
    }

    public Map<String, List<String>> getRequestHeaders() {
        return requestHeaders;
    }

    public String getReferrer() {
        return referrer;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public boolean isServiceCall() {
        return isServiceCall;
    }

    public final static class Builder {

        private String remoteAddr;
        private String remoteUser;
        private String remoteHost;
        private int remotePort;
        private String actAsUser;
        private String certificateIssuer;
        private String certificateSubject;
        private boolean isServiceCall;
        private MediaType contentType;
        private List<MediaType> acceptableMediaTypes;
        private Map<String, List<String>> requestHeaders;
        private String referrer;
        private String userAgent;

        public Builder() {

        }

        public Builder(MessageContext context, SecuritySettings settings) {
            if (context != null) {
                HttpHeaders headers = context.getHttpHeaders();
                if (headers != null) {
                    if (StringUtils.isNotEmpty(settings.getCertificateIssuerHeader()))
                        this.certificateIssuer = headers.getHeaderString(settings.getCertificateIssuerHeader());
                    if (StringUtils.isNotEmpty(settings.getCertificateSubjectHeader()))
                        this.certificateSubject = headers.getHeaderString(settings.getCertificateSubjectHeader());
                    if (StringUtils.isNotEmpty(settings.getActAsUserHeader()))
                        this.actAsUser = headers.getHeaderString(settings.getActAsUserHeader());

                    this.acceptableMediaTypes = headers.getAcceptableMediaTypes();
                    this.contentType = headers.getMediaType();
                    this.referrer = headers.getHeaderString("Referer");
                    this.userAgent = headers.getHeaderString("User-Agent");
                    this.requestHeaders = headers.getRequestHeaders();
                }

                HttpServletRequest request = context.getHttpServletRequest();
                if (request != null) {
                    this.remoteAddr = request.getRemoteAddr();
                    this.remoteHost = request.getRemoteHost();
                    this.remotePort = request.getRemotePort();
                    this.remoteUser = request.getRemoteUser();
                }
            }
            this.isServiceCall = StringUtils.isNotEmpty(this.certificateIssuer) && StringUtils.isNotEmpty(this.certificateSubject);
        }

        public RequestDetails build() {
            return new RequestDetails(this);
        }

    }

}
