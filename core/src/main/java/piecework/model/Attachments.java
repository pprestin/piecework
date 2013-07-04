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
import org.springframework.data.mongodb.core.mapping.Document;
import piecework.common.model.User;
import piecework.common.view.ViewContext;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author James Renfro
 */
@XmlRootElement(name = Attachments.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = Attachments.Constants.TYPE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = Attachments.Constants.ROOT_ELEMENT_NAME)
public class Attachments {

    @XmlElementRef
    private List<Attachment> attachments;

    private Attachments() {
        this(new Attachments.Builder(), new ViewContext());
    }

    private Attachments(Attachments.Builder builder, ViewContext context) {
        this.attachments = builder.attachments;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public final static class Builder {

        private List<Attachment> attachments;

        public Builder() {
            this.attachments = new ArrayList<Attachment>();
        }

        public Builder attachments(Attachment attachment) {
            if (attachment != null)
                this.attachments.add(attachment);
            return this;
        }

        public Builder attachments(List<Attachment> attachments) {
            if (attachments != null)
                this.attachments.addAll(attachments);
            return this;
        }

        public Attachments build() {
            return new Attachments(this, new ViewContext());
        }

    }

    public static class Constants {
        public static final String RESOURCE_LABEL = "Attachments";
        public static final String ROOT_ELEMENT_NAME = "attachments";
        public static final String TYPE_NAME = "AttachmentsType";
    }
}
