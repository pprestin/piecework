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

import javax.xml.bind.annotation.*;
import java.util.Date;

/**
 * @author James Renfro
 */
@XmlRootElement(name = Secret.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = Secret.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Secret extends Value {

    @XmlTransient
    private final String id;

    @XmlTransient
    private final String name;

    @XmlTransient
    private final Date date;

    @XmlTransient
    private final byte[] ciphertext;

    @XmlTransient
    private final byte[] iv;

    private Secret() {
        this(new Builder());
    }

    private Secret(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.date = builder.date;
        this.ciphertext = builder.ciphertext;
        this.iv = builder.iv;
    }

    @JsonIgnore
    public String getId() {
        return id;
    }

    @JsonIgnore
    public String getName() {
        return name;
    }

    @JsonIgnore
    public Date getDate() {
        return date;
    }

    @JsonIgnore
    public byte[] getCiphertext() {
        return ciphertext;
    }

    @JsonIgnore
    public byte[] getIv() {
        return iv;
    }

    public boolean isEmpty() {
        return ciphertext == null || ciphertext.length == 0;
    }

    public final static class Builder {
        private String id;
        private String name;
        private Date date;
        private byte[] ciphertext;
        private byte[] iv;

        public Secret build() {
            return new Secret(this);
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder date(Date date) {
            this.date = date;
            return this;
        }

        public Builder ciphertext(byte[] ciphertext) {
            this.ciphertext = ciphertext;
            return this;
        }

        public Builder iv(byte[] iv) {
            this.iv = iv;
            return this;
        }
    }

    public static class Constants {
        public static final String RESOURCE_LABEL = "Secret";
        public static final String ROOT_ELEMENT_NAME = "secret";
        public static final String TYPE_NAME = "SecretType";
    }

}
