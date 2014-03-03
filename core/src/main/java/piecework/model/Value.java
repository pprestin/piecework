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
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.cxf.common.util.StringUtils;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * @author James Renfro
 */
@XmlRootElement(name = Value.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = Value.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value=File.class, name="file"),
    @JsonSubTypes.Type(value=Secret.class, name="secret"),
    @JsonSubTypes.Type(value=User.class, name="user")
})
public class Value implements Serializable {

    @XmlValue
    private final String value;

    public Value() {
        this(null);
    }

    public Value(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }

    public boolean isEmpty() {
        return StringUtils.isEmpty(value);
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static class Constants {
        public static final String RESOURCE_LABEL = "Value";
        public static final String ROOT_ELEMENT_NAME = "value";
        public static final String TYPE_NAME = "ValueType";
    }

}
