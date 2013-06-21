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
import org.springframework.data.annotation.Id;
import piecework.security.Sanitizer;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author James Renfro
 */
@XmlRootElement(name = Candidate.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = Candidate.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Candidate implements Serializable {

    @XmlAttribute
    @XmlID
    @Id
    private final String candidateId;

    @XmlElement
    private final String label;

    @XmlElement
    private final String type;

    @XmlElement
    private final String role;

    @XmlElement
    private final String providerId;

    @XmlElement
    private final boolean isDeleted;

    private Candidate() {
        this(new Builder());
    }

    private Candidate(Builder builder) {
        this.candidateId = builder.candidateId;
        this.label = builder.label;
        this.type = builder.type;
        this.role = builder.role;
        this.providerId = builder.providerId;
        this.isDeleted = builder.isDeleted;
    }

    public String getCandidateId() {
        return candidateId;
    }

    public String getLabel() {
        return label;
    }

    public String getType() {
        return type;
    }

    public String getRole() {
        return role;
    }

    public String getProviderId() {
        return providerId;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public final static class Builder {

        private String candidateId;
        private String label;
        private String type;
        private String role;
        private String providerId;
        private boolean isDeleted;

        public Builder() {
            super();
        }

        public Builder(Candidate candidate, Sanitizer sanitizer) {
            this.candidateId = sanitizer.sanitize(candidate.candidateId);
            this.label = sanitizer.sanitize(candidate.label);
            this.type = sanitizer.sanitize(candidate.type);
            this.role = sanitizer.sanitize(candidate.role);
            this.providerId = sanitizer.sanitize(candidate.providerId);
            this.isDeleted = candidate.isDeleted;
        }

        public Candidate build() {
            return new Candidate(this);
        }

        public Builder candidateId(String candidateId) {
            this.candidateId = candidateId;
            return this;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder role(String role) {
            this.role = role;
            return this;
        }

        public Builder providerId(String providerId) {
            this.providerId = providerId;
            return this;
        }
    }

    public static class Constants {
        public static final String RESOURCE_LABEL = "Candidate";
        public static final String ROOT_ELEMENT_NAME = "candidate";
        public static final String TYPE_NAME = "CandidateType";
    }
}
