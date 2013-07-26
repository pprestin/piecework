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

import piecework.common.ViewContext;
import piecework.security.Sanitizer;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author James Renfro
 */
public class File {

    @XmlElement
    private final String name;

    @XmlElement
    private final String contentType;

    @XmlElement
    private final String location;

    @XmlAttribute
    private final String link;


    private File() {
        this(new Builder(), null);
    }

    private File(Builder builder, ViewContext context) {
        this.contentType = builder.contentType;
        this.location = builder.location;
        this.name = builder.name;
        this.link = context != null ? context.getApplicationUri(builder.processDefinitionKey, builder.requestId, builder.variableName, builder.name) : null;
    }

    public String getName() {
        return name;
    }

    public String getContentType() {
        return contentType;
    }

    public String getLocation() {
        return location;
    }

    public String getLink() {
        return link;
    }

    public final static class Builder {

        private String name;
        private String contentType;
        private String location;
        private String processDefinitionKey;
        private String requestId;
        private String variableName;

        public Builder() {
            super();
        }

        public Builder(File file, Sanitizer sanitizer) {
            this.location = sanitizer.sanitize(file.location);
            this.contentType = sanitizer.sanitize(file.contentType);
            this.name = sanitizer.sanitize(file.name);
        }

        public File build(ViewContext context) {
            return new File(this, context);
        }

        public Builder name(String name) {
            this.name = name;
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

        public Builder processDefinitionKey(String processDefinitionKey) {
            this.processDefinitionKey = processDefinitionKey;
            return this;
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder variableName(String variableName) {
            this.variableName = variableName;
            return this;
        }

    }

    public static class Constants {
        public static final String RESOURCE_LABEL = "File";
        public static final String ROOT_ELEMENT_NAME = "file";
        public static final String TYPE_NAME = "FileType";
    }
}
