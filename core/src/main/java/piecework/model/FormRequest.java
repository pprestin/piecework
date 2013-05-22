/*
 * Copyright 2012 University of Washington
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
package piecework.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import piecework.security.Sanitizer;

/**
 * @author James Renfro
 */
@Document(collection = "request")
public class FormRequest {

    @Id
    private final String requestId;

    private final String processDefinitionKey;

    private final String processInstanceId;

    private final String remoteAddr;

    private final String remoteHost;

    private final int remotePort;

    private final String remoteUser;

    private final String certificateSubject;

    private final String certificateIssuer;

    @DBRef
    private final Interaction interaction;

    @DBRef
    private final Screen screen;

    private final String submissionType;

    private FormRequest() {
        this(new FormRequest.Builder());
    }
    private FormRequest(FormRequest.Builder builder) {
        this.requestId = builder.requestId;
        this.processDefinitionKey = builder.processDefinitionKey;
        this.processInstanceId = builder.processInstanceId;
        this.remoteAddr = builder.remoteAddr;
        this.remoteHost = builder.remoteHost;
        this.remotePort = builder.remotePort;
        this.remoteUser = builder.remoteUser;
        this.certificateSubject = builder.certificateSubject;
        this.certificateIssuer = builder.certificateIssuer;
        this.interaction = builder.interaction;
        this.screen = builder.screen;
        this.submissionType = builder.submissionType;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
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

    public String getCertificateSubject() {
        return certificateSubject;
    }

    public String getCertificateIssuer() {
        return certificateIssuer;
    }

    public Interaction getInteraction() {
        return interaction;
    }

    public Screen getScreen() {
        return screen;
    }

    public String getSubmissionType() {
        return submissionType;
    }

    public final static class Builder {

        private String requestId;
        private String processDefinitionKey;
        private String processInstanceId;
        private String remoteAddr;
        private String remoteHost;
        private int remotePort;
        private String remoteUser;
        private String certificateSubject;
        private String certificateIssuer;
        private Interaction interaction;
        private Screen screen;
        private String submissionType;

        public Builder() {
            super();
        }

        public Builder(FormRequest request, Sanitizer sanitizer) {
            this.requestId = sanitizer.sanitize(request.requestId);
            this.processDefinitionKey = sanitizer.sanitize(request.processDefinitionKey);
            this.processInstanceId = sanitizer.sanitize(request.processInstanceId);
            this.remoteAddr = sanitizer.sanitize(request.remoteAddr);
            this.remoteHost = sanitizer.sanitize(request.remoteHost);
            this.remotePort = request.remotePort;
            this.remoteUser = sanitizer.sanitize(request.remoteUser);
            this.certificateSubject = sanitizer.sanitize(request.certificateSubject);
            this.certificateIssuer = sanitizer.sanitize(request.certificateIssuer);
            this.interaction = request.interaction != null ? new Interaction.Builder(request.interaction, sanitizer).build() : null;
            this.screen = request.screen != null ? new Screen.Builder(request.screen, sanitizer).build() : null;
            this.submissionType = sanitizer.sanitize(request.submissionType);
        }

        public FormRequest build() {
            return new FormRequest(this);
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder processDefinitionKey(String processDefinitionKey) {
            this.processDefinitionKey = processDefinitionKey;
            return this;
        }

        public Builder processInstanceId(String processInstanceId) {
            this.processInstanceId = processInstanceId;
            return this;
        }

        public Builder remoteAddr(String remoteAddr) {
            this.remoteAddr = remoteAddr;
            return this;
        }

        public Builder remoteHost(String remoteHost) {
            this.remoteHost = remoteHost;
            return this;
        }

        public Builder remotePort(int remotePort) {
            this.remotePort = remotePort;
            return this;
        }

        public Builder remoteUser(String remoteUser) {
            this.remoteUser = remoteUser;
            return this;
        }

        public Builder certificateSubject(String certificateSubject) {
            this.certificateSubject = certificateSubject;
            return this;
        }

        public Builder certificateIssuer(String certificateIssuer) {
            this.certificateIssuer = certificateIssuer;
            return this;
        }

        public Builder interaction(Interaction interaction) {
            this.interaction = interaction;
            return this;
        }

        public Builder screen(Screen screen) {
            this.screen = screen;
            return this;
        }

        public Builder submissionType(String submissionType) {
            this.submissionType = submissionType;
            return this;
        }
    }

}
