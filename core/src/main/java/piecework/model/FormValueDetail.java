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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Transient;
import piecework.security.Sanitizer;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * @author James Renfro
 */
@XmlRootElement(name = FormValueDetail.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = FormValueDetail.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FormValueDetail implements Serializable {

    @XmlElement
    private final String contentType;

    @XmlElement
    private final String location;

    @XmlTransient
    private final boolean restricted;

    @XmlTransient
    private final byte[] iv;

    private FormValueDetail() {
        this(new Builder());
    }

    private FormValueDetail(Builder builder) {
        this.contentType = builder.contentType;
        this.location = builder.location;
        this.restricted = builder.restricted;
        this.iv = builder.iv;
    }

    public String getContentType() {
        return contentType;
    }

    public String getLocation() {
        return location;
    }

    @JsonIgnore
    public boolean isRestricted() {
        return restricted;
    }

    @JsonIgnore
    public byte[] getIv() {
        return iv;
    }

    public final static class Builder {

        private String contentType;
        private String location;
        private boolean restricted;
        private byte[] iv;

        public Builder() {
            super();
        }

        public Builder(FormValueDetail formValue, Sanitizer sanitizer) {
            this.location = sanitizer.sanitize(formValue.location);
            this.contentType = sanitizer.sanitize(formValue.contentType);
            this.restricted = formValue.restricted;
        }

        public FormValueDetail build() {
            return new FormValueDetail(this);
        }

        public Builder restricted() {
            this.restricted = true;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder iv(byte[] iv) {
            this.iv = iv;
            return this;
        }
    }

    public static class Constants {
        public static final String RESOURCE_LABEL = "FormValueDetail";
        public static final String ROOT_ELEMENT_NAME = "formValueDetail";
        public static final String TYPE_NAME = "FormValueDetailType";
    }
}
