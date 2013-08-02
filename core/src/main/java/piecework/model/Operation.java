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
import piecework.enumeration.OperationType;

import javax.xml.bind.annotation.*;
import java.util.Date;

/**
 * @author James Renfro
 */
@XmlRootElement(name = Operation.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = Operation.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Operation {

    @XmlElement
    private final OperationType type;

    @XmlElement
    private final String reason;

    @XmlElement
    private final String userId;

    @XmlElement
    private final Date date;

    public Operation() {
        this(null, null, null, null);
    }

    public Operation(OperationType type, String reason, Date date, String userId) {
        this.type = type;
        this.reason = reason;
        this.date = date;
        this.userId = userId;
    }

    public String getReason() {
        return reason;
    }

    public String getUserId() {
        return userId;
    }

    public OperationType getType() {
        return type;
    }

    public Date getDate() {
        return date;
    }

    public static class Constants {
        public static final String RESOURCE_LABEL = "Operation";
        public static final String ROOT_ELEMENT_NAME = "operation";
        public static final String TYPE_NAME = "OperationType";
    }

}
