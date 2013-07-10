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
import piecework.common.ViewContext;
import piecework.security.Sanitizer;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.UUID;

/**
 * @author James Renfro
 */
@XmlRootElement(name = Message.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = Message.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Message implements Serializable {

    @XmlAttribute
    @XmlID
    @Id
    private final String messageId;

    @XmlElement
    private final String text;

    @XmlElement
    private final String type;

    @XmlAttribute
    private final int ordinal;

    private Message() {
        this(new Message.Builder(), new ViewContext());
    }

    private Message(Message.Builder builder, ViewContext context) {
        this.messageId = builder.messageId;
        this.text = builder.text;
        this.type = builder.type;
        this.ordinal = builder.ordinal;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public final static class Builder {

        private String messageId;
        private String text;
        private String type;
        private int ordinal;

        public Builder() {
            super();
            this.messageId = UUID.randomUUID().toString();
        }

        public Builder(Message message, Sanitizer sanitizer) {
            this.messageId = message.messageId != null ? sanitizer.sanitize(message.messageId) : UUID.randomUUID().toString();;
            this.text = sanitizer.sanitize(message.text);
            this.type = sanitizer.sanitize(message.type);
            this.ordinal = message.ordinal;
        }

        public Message build() {
            return build(null);
        }

        public Message build(ViewContext context) {
            return new Message(this, context);
        }

        public Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder ordinal(int ordinal) {
            this.ordinal = ordinal;
            return this;
        }

    }

    public static class Constants {
        public static final String RESOURCE_LABEL = "Message";
        public static final String ROOT_ELEMENT_NAME = "message";
        public static final String TYPE_NAME = "MessageType";
    }
}
