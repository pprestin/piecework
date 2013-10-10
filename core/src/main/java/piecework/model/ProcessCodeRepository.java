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
package piecework.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import piecework.enumeration.VersionControlSystem;
import piecework.security.Sanitizer;

import javax.xml.bind.annotation.*;

/**
 * @author James Renfro
 */
@XmlRootElement(name = ProcessCodeRepository.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = ProcessCodeRepository.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessCodeRepository {

    @XmlElement
    private final String url;

    @XmlElement
    private final String username;

    @XmlElement
    private final Secret password;

    @XmlElement
    private final VersionControlSystem software;

    private ProcessCodeRepository(ProcessCodeRepository.Builder builder) {
        this.url = builder.url;
        this.username = builder.username;
        this.password = builder.password;
        this.software = builder.software;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public Secret getPassword() {
        return password;
    }

    public VersionControlSystem getSoftware() {
        return software;
    }

    public final static class Builder {

        private String url;
        private String username;
        private Secret password;
        private VersionControlSystem software;

        public Builder() {
            super();
        }

        public Builder(ProcessCodeRepository repository, Sanitizer sanitizer) {

        }

        public ProcessCodeRepository build() {
            return new ProcessCodeRepository(this);
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(Secret password) {
            this.password = password;
            return this;
        }

        public Builder system(VersionControlSystem software) {
            this.software = software;
            return this;
        }

    }

    public static class Constants {
        public static final String RESOURCE_LABEL = "Process Repository";
        public static final String ROOT_ELEMENT_NAME = "repository";
        public static final String TYPE_NAME = "ProcessRepositoryType";
    }

}
