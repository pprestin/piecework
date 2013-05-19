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
import org.springframework.data.mongodb.core.mapping.Document;
import piecework.Sanitizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author James Renfro
 */
@Document(collection = "request")
public class FormRequest {

    @Id
    private final String requestId;

    private final String remoteAddress;

    private final String remoteHost;

    private final String remotePort;

    private final String remoteUser;

    private FormRequest() {
        this(new FormRequest.Builder());
    }
    private FormRequest(FormRequest.Builder builder) {
        this.requestId = builder.requestId;
        this.remoteAddress = builder.remoteAddress;
        this.remoteHost = builder.remoteHost;
        this.remotePort = builder.remotePort;
        this.remoteUser = builder.remoteUser;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public final static class Builder {

        private String requestId;
        private String remoteAddress;
        private String remoteHost;
        private String remotePort;
        private String remoteUser;

        public Builder() {
            super();
        }

        public Builder(FormRequest instance, Sanitizer sanitizer) {
            this.requestId = sanitizer.sanitize(instance.requestId);
            this.remoteAddress = sanitizer.sanitize(instance.remoteAddress);
            this.remoteHost = sanitizer.sanitize(instance.remoteHost);
            this.remotePort = sanitizer.sanitize(instance.remotePort);
            this.remoteUser = sanitizer.sanitize(instance.remoteUser);
        }

        public FormRequest build() {
            return new FormRequest(this);
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder remoteAddress(String remoteAddress) {
            this.remoteAddress = remoteAddress;
            return this;
        }

        public Builder remoteHost(String remoteHost) {
            this.remoteHost = remoteHost;
            return this;
        }

        public Builder remotePort(String remotePort) {
            this.remotePort = remotePort;
            return this;
        }

        public Builder remoteUser(String remoteUser) {
            this.remoteUser = remoteUser;
            return this;
        }
    }

}
